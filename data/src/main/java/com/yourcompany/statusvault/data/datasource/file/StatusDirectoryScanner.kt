package com.yourcompany.statusvault.data.datasource.file

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.yourcompany.statusvault.domain.model.MediaType
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.model.StatusItem
import com.yourcompany.statusvault.domain.model.StatusScanDebug
import javax.inject.Inject

data class StatusScanResult(
    val items: List<StatusItem>,
    val debug: StatusScanDebug,
)

private data class SafDocumentNode(
    val documentId: String,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val lastModified: Long,
)

class StatusDirectoryScanner @Inject constructor() {

    fun scan(
        context: Context,
        treeUri: Uri,
        sourceApp: SourceApp,
        savedIds: Set<String>,
    ): StatusScanResult {
        val statusDirectoryIds = resolveStatusDirectoryIds(
            context = context,
            treeUri = treeUri,
            sourceApp = sourceApp,
        )

        val allEntries = statusDirectoryIds.flatMap { directoryId ->
            queryChildDocuments(
                context = context,
                treeUri = treeUri,
                documentId = directoryId,
            )
        }
        val fileEntries = allEntries.filterNot(::isDirectory)
        val scannableFiles = fileEntries.mapNotNull { file ->
            resolveMediaType(file)?.let { mediaType -> file to mediaType }
        }
        val items = scannableFiles
            .distinctBy { it.first.documentId }
            .map { (file, mediaType) ->
                val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, file.documentId)
                val id = documentUri.toString()
                StatusItem(
                    id = id,
                    sourceApp = sourceApp,
                    mediaType = mediaType,
                    sourceUri = id,
                    fileName = file.displayName.ifBlank { "unknown" },
                    mimeType = file.mimeType.takeIf { it.isNotBlank() }
                        ?: if (mediaType == MediaType.VIDEO) "video/mp4" else "image/jpeg",
                    modifiedAt = file.lastModified,
                    isSaved = savedIds.contains(id),
                )
            }
            .sortedByDescending { it.modifiedAt }

        val debug = StatusScanDebug(
            grantedPath = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()?.let(Uri::decode),
            resolvedStatusPaths = statusDirectoryIds.map(Uri::decode),
            totalEntries = allEntries.size,
            fileEntries = fileEntries.size,
            imageCandidates = scannableFiles.count { it.second == MediaType.IMAGE },
            videoCandidates = scannableFiles.count { it.second == MediaType.VIDEO },
            filteredOutEntries = fileEntries.size - scannableFiles.size,
            sampleNames = fileEntries.map { it.displayName }.filter { it.isNotBlank() }.take(8),
        )

        return StatusScanResult(
            items = items,
            debug = debug,
        )
    }

    fun hasUsableStatusDirectory(
        context: Context,
        treeUri: Uri,
        sourceApp: SourceApp,
    ): Boolean {
        return resolveStatusDirectoryIds(
            context = context,
            treeUri = treeUri,
            sourceApp = sourceApp,
        ).isNotEmpty()
    }

    private fun resolveStatusDirectoryIds(
        context: Context,
        treeUri: Uri,
        sourceApp: SourceApp,
    ): List<String> {
        val treeDocumentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull().orEmpty()
        if (treeDocumentId.isBlank()) return emptyList()

        val knownCandidates = resolveKnownStatusDirectoryIds(
            treeDocumentId = treeDocumentId,
            sourceApp = sourceApp,
        )
        val directMatches = knownCandidates.filter { candidateId ->
            isDirectoryDocument(
                context = context,
                treeUri = treeUri,
                documentId = candidateId,
            )
        }

        val discoveredMatches = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<Pair<String, Int>>()
        queue.add(treeDocumentId to 0)

        while (queue.isNotEmpty() && visited.size < MAX_VISITED_DIRECTORIES) {
            val (currentId, depth) = queue.removeFirst()
            if (!visited.add(currentId) || depth > MAX_SEARCH_DEPTH) continue

            val childNodes = queryChildDocuments(
                context = context,
                treeUri = treeUri,
                documentId = currentId,
            )

            childNodes.forEach { child ->
                if (isDirectory(child)) {
                    if (matchesStatusDirectory(child, sourceApp)) {
                        discoveredMatches += child.documentId
                    }
                    queue.add(child.documentId to (depth + 1))
                }
            }
        }

        return (directMatches + discoveredMatches).distinct()
    }

    private fun resolveMediaType(file: SafDocumentNode): MediaType? {
        val fileName = file.displayName
        if (fileName.endsWith(".tmp", ignoreCase = true)) return null
        if (fileName.equals(".nomedia", ignoreCase = true)) return null
        if (file.size <= 0L) return null

        val mime = file.mimeType.lowercase()
        return when {
            mime.startsWith("image/") -> MediaType.IMAGE
            mime.startsWith("video/") -> MediaType.VIDEO
            IMAGE_EXTENSIONS.any { fileName.endsWith(it, ignoreCase = true) } -> MediaType.IMAGE
            VIDEO_EXTENSIONS.any { fileName.endsWith(it, ignoreCase = true) } -> MediaType.VIDEO
            else -> null
        }
    }

    private fun matchesStatusDirectory(
        node: SafDocumentNode,
        sourceApp: SourceApp,
    ): Boolean {
        return isDirectory(node) && (
            node.displayName.equals(".Statuses", ignoreCase = true) ||
                matchesExpectedDocumentId(node.documentId, sourceApp)
            )
    }

    private fun resolveKnownStatusDirectoryIds(
        treeDocumentId: String,
        sourceApp: SourceApp,
    ): List<String> {
        val volume = treeDocumentId.substringBefore(':', missingDelimiterValue = "")
        if (volume.isBlank()) return emptyList()

        val packageName = when (sourceApp) {
            SourceApp.WHATSAPP -> "com.whatsapp"
            SourceApp.WHATSAPP_BUSINESS -> "com.whatsapp.w4b"
        }
        val appFolder = when (sourceApp) {
            SourceApp.WHATSAPP -> "WhatsApp"
            SourceApp.WHATSAPP_BUSINESS -> "WhatsApp Business"
        }

        val normalizedTree = normalizeDocumentId(treeDocumentId)
        return listOf(
            "$volume:Android/media/$packageName/$appFolder/Media/.Statuses",
            "$volume:Android/media/$packageName/$appFolder/Media/Statuses",
        ).filter { candidate ->
            normalizeDocumentId(candidate).startsWith(normalizedTree)
        }
    }

    private fun queryChildDocuments(
        context: Context,
        treeUri: Uri,
        documentId: String,
    ): List<SafDocumentNode> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            documentId,
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )

        return runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            SafDocumentNode(
                                documentId = cursor.getStringOrEmpty(idIndex),
                                displayName = cursor.getStringOrEmpty(nameIndex),
                                mimeType = cursor.getStringOrEmpty(mimeIndex),
                                size = cursor.getLongOrZero(sizeIndex),
                                lastModified = cursor.getLongOrZero(modifiedIndex),
                            ),
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun isDirectoryDocument(
        context: Context,
        treeUri: Uri,
        documentId: String,
    ): Boolean {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        val projection = arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE)
        return runCatching {
            context.contentResolver.query(documentUri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use false
                val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                cursor.getStringOrEmpty(mimeIndex) == DocumentsContract.Document.MIME_TYPE_DIR
            } ?: false
        }.getOrDefault(false)
    }

    private fun isDirectory(node: SafDocumentNode): Boolean {
        return node.mimeType == DocumentsContract.Document.MIME_TYPE_DIR
    }

    private fun matchesExpectedDocumentId(
        documentId: String,
        sourceApp: SourceApp,
    ): Boolean {
        val normalized = normalizeDocumentId(documentId).lowercase()
        val packageName = when (sourceApp) {
            SourceApp.WHATSAPP -> "com.whatsapp"
            SourceApp.WHATSAPP_BUSINESS -> "com.whatsapp.w4b"
        }.lowercase()
        val appFolder = when (sourceApp) {
            SourceApp.WHATSAPP -> "whatsapp"
            SourceApp.WHATSAPP_BUSINESS -> "whatsapp business"
        }

        return normalized.contains("android/media/$packageName/$appFolder/media/.statuses") ||
            normalized.contains("android/media/$packageName/$appFolder/media/statuses") ||
            normalized.endsWith("/media/.statuses") ||
            normalized.endsWith("/media/statuses") ||
            normalized.endsWith("/.statuses")
    }

    private fun normalizeDocumentId(documentId: String): String {
        val decoded = Uri.decode(documentId)
        return decoded.substringAfter(':', missingDelimiterValue = decoded).lowercase()
    }

    private fun android.database.Cursor.getStringOrEmpty(index: Int): String {
        if (index < 0 || isNull(index)) return ""
        return getString(index).orEmpty()
    }

    private fun android.database.Cursor.getLongOrZero(index: Int): Long {
        if (index < 0 || isNull(index)) return 0L
        return getLong(index)
    }

    private companion object {
        const val MAX_SEARCH_DEPTH = 8
        const val MAX_VISITED_DIRECTORIES = 120
        val IMAGE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".webp")
        val VIDEO_EXTENSIONS = setOf(".mp4", ".3gp", ".mkv")
    }
}
