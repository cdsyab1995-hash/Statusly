package com.yourcompany.statusvault.data.datasource.file

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import javax.inject.Inject

class MediaStoreSaver @Inject constructor() {
    fun saveToGallery(
        context: Context,
        sourceUri: String,
        displayName: String,
        mimeType: String,
        mediaDirectory: String = "Pictures/StatusVault",
        videoDirectory: String = "Movies/StatusVault",
    ): String? {
        val resolver = context.contentResolver
        val parsedUri = sourceUri.toUri()
        val collection = if (mimeType.startsWith("video/")) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val relativePath = if (mimeType.startsWith("video/")) videoDirectory else mediaDirectory

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val targetUri = resolver.insert(collection, contentValues) ?: return null

        return try {
            resolver.openInputStream(parsedUri).use { input ->
                resolver.openOutputStream(targetUri).use { output ->
                    if (input == null || output == null) return null
                    input.copyTo(output)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val completed = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                resolver.update(targetUri, completed, null, null)
            }

            targetUri.toString()
        } catch (_: Exception) {
            resolver.delete(targetUri, null, null)
            null
        }
    }
}
