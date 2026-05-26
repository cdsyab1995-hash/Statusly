package com.yourcompany.statusvault.feature.statuslist

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Size
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yourcompany.statusvault.core.common.SelectedPreviewHolder
import com.yourcompany.statusvault.core.common.SelectedSourceHolder
import com.yourcompany.statusvault.domain.model.MediaType
import com.yourcompany.statusvault.domain.model.SourceApp
import com.yourcompany.statusvault.domain.model.StatusItem
import com.yourcompany.statusvault.domain.model.StatusScanDebug
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val AccentGreen = Color(0xFF25D366)
private val AccentGreenDark = Color(0xFF128C4A)
private val SoftBackground = Color(0xFFF7FAF8)
private val SoftTile = Color(0xFFF2F6F3)
private val SoftBorder = Color(0xFFE2ECE6)
private val FrostedSurface = Color(0xCC111713)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val VideoOverlay = Color(0x66000000)

private enum class ContentTab {
    IMAGE,
    VIDEO,
}

private data class WhatsAppTarget(
    val sourceApp: SourceApp,
    val label: String,
    val packageName: String,
)

private val whatsAppTargets = listOf(
    WhatsAppTarget(
        sourceApp = SourceApp.WHATSAPP,
        label = "WhatsApp",
        packageName = "com.whatsapp",
    ),
    WhatsAppTarget(
        sourceApp = SourceApp.WHATSAPP_BUSINESS,
        label = "WA Business",
        packageName = "com.whatsapp.w4b",
    ),
)

@Composable
fun StatusListRoute(
    viewModel: StatusListViewModel = hiltViewModel(),
    onOpenPreview: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(ContentTab.IMAGE) }
    var selectedSource by remember { mutableStateOf(SelectedSourceHolder.sourceApp) }
    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val readOnlyFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val readWriteFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        val persisted = runCatching {
            context.contentResolver.takePersistableUriPermission(uri, readWriteFlags)
            true
        }.recoverCatching {
            context.contentResolver.takePersistableUriPermission(uri, readOnlyFlags)
            true
        }.getOrElse {
            false
        }

        if (persisted) {
            viewModel.saveGrant(uri.toString())
        } else {
            Toast.makeText(
                context,
                "Unable to keep folder access on this device. Please try authorizing again.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    LaunchedEffect(selectedSource) {
        SelectedSourceHolder.sourceApp = selectedSource
        viewModel.loadStatuses(sourceApp = selectedSource)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(lifecycleOwner, selectedSource) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStatuses(sourceApp = selectedSource, fromReturn = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val imageCount = uiState.items.count { it.mediaType == MediaType.IMAGE }
    val videoCount = uiState.items.count { it.mediaType == MediaType.VIDEO }
    val filteredItems = uiState.items.filter {
        when (selectedTab) {
            ContentTab.IMAGE -> it.mediaType == MediaType.IMAGE
            ContentTab.VIDEO -> it.mediaType == MediaType.VIDEO
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StatusHeader(
                selectedSource = selectedSource,
                selectedTab = selectedTab,
                imageCount = imageCount,
                videoCount = videoCount,
                onSourceSelected = { selectedSource = it },
                onSelectTab = { selectedTab = it },
                onRefresh = { viewModel.loadStatuses(sourceApp = selectedSource) },
            )

            when {
                uiState.isLoading -> {
                    ScanStatePanel(
                        title = "Scanning viewed statuses...",
                        description = "We will refresh automatically when you come back from WhatsApp.",
                    )
                }

                filteredItems.isEmpty() -> {
                    EmptyStatusPanel(
                        hasGrant = uiState.hasGrant,
                        needsDirectoryReconnect = uiState.needsDirectoryReconnect,
                        grantPathHint = uiState.grantPathHint,
                        scanDebug = uiState.scanDebug,
                        title = if (uiState.hasGrant) {
                            "No viewed statuses detected yet"
                        } else if (uiState.needsDirectoryReconnect) {
                            "Reconnect WhatsApp access"
                        } else {
                            "Authorize WhatsApp access first"
                        },
                        description = if (uiState.hasGrant) {
                            "Open at least 1 status in WhatsApp, then return here. We will scan automatically."
                        } else {
                            "Authorize once and we will only read the status directory, never your chat content."
                        },
                        helperMessage = uiState.message,
                        onOpenWhatsApp = {
                            openPreferredWhatsApp(
                                context = context,
                                packageManager = context.packageManager,
                                preferredSource = selectedSource,
                                onLaunched = { launchedSource ->
                                    if (selectedSource != launchedSource) {
                                        selectedSource = launchedSource
                                    }
                                },
                            )
                        },
                        onPickDirectory = {
                            SelectedSourceHolder.sourceApp = selectedSource
                            runCatching {
                                directoryLauncher.launch(buildSuggestedInitialUri(selectedSource))
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    "Unable to open the folder picker on this device.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        },
                    )
                }

                else -> {
                    StatusResultGrid(
                        items = filteredItems,
                        message = uiState.message,
                        onSave = viewModel::save,
                        onPreview = {
                            SelectedPreviewHolder.statusItem = it
                            onOpenPreview()
                        },
                    )
                }
            }
        }
    }
}

private fun openPreferredWhatsApp(
    context: Context,
    packageManager: PackageManager,
    preferredSource: SourceApp,
    onLaunched: (SourceApp) -> Unit,
) {
    val orderedTargets = buildList {
        add(whatsAppTargets.first { it.sourceApp == preferredSource })
        addAll(whatsAppTargets.filterNot { it.sourceApp == preferredSource })
    }

    val installedTarget = orderedTargets.firstOrNull { target ->
        isPackageInstalled(packageManager, target.packageName)
    }

    val launchIntent = installedTarget?.let { target ->
        buildLaunchIntent(packageManager, target.packageName)
    }

    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        when {
            launchIntent != null -> {
                onLaunched(installedTarget.sourceApp)
                SelectedSourceHolder.sourceApp = installedTarget.sourceApp
                context.startActivity(launchIntent)
            }

            fallbackIntent.resolveActivity(packageManager) != null -> {
                Toast.makeText(context, "WhatsApp app not found. Opening fallback link.", Toast.LENGTH_SHORT).show()
                context.startActivity(fallbackIntent)
            }

            else -> {
                Toast.makeText(context, "Unable to open WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Unable to open WhatsApp.", Toast.LENGTH_SHORT).show()
    }
}

private fun isPackageInstalled(
    packageManager: PackageManager,
    packageName: String,
): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: Exception) {
        false
    }
}

private fun buildLaunchIntent(
    packageManager: PackageManager,
    packageName: String,
): Intent? {
    val queryIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setPackage(packageName)
    }
    val resolveInfo = packageManager.queryIntentActivities(queryIntent, 0).firstOrNull()
    return resolveInfo?.activityInfo?.let { activityInfo ->
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(activityInfo.packageName, activityInfo.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
    } ?: packageManager.getLaunchIntentForPackage(packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

@Composable
private fun StatusHeader(
    selectedSource: SourceApp,
    selectedTab: ContentTab,
    imageCount: Int,
    videoCount: Int,
    onSourceSelected: (SourceApp) -> Unit,
    onSelectTab: (ContentTab) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Statusly",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentGreenDark,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Status Saver for WA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SoftTile),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    tint = AccentGreenDark,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SegmentedRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SourcePill(
                label = "WhatsApp",
                selected = selectedSource == SourceApp.WHATSAPP,
                onClick = { onSourceSelected(SourceApp.WHATSAPP) },
                modifier = Modifier.weight(1f),
            )
            SourcePill(
                label = "WA Business",
                selected = selectedSource == SourceApp.WHATSAPP_BUSINESS,
                onClick = { onSourceSelected(SourceApp.WHATSAPP_BUSINESS) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MediaTabRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ContentPill(
                label = "Photos $imageCount",
                selected = selectedTab == ContentTab.IMAGE,
                icon = Icons.Outlined.Image,
                onClick = { onSelectTab(ContentTab.IMAGE) },
                modifier = Modifier.weight(1f),
            )
            ContentPill(
                label = "Videos $videoCount",
                selected = selectedTab == ContentTab.VIDEO,
                icon = Icons.Outlined.VideoLibrary,
                onClick = { onSelectTab(ContentTab.VIDEO) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentedRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(SoftTile)
            .border(1.dp, SoftBorder, RoundedCornerShape(28.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun SourcePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = if (selected) AccentGreen else Color.Transparent,
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ContentPill(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AccentGreen.copy(alpha = 0.18f)),
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AccentGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (selected) AccentGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (selected) 56.dp else 24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (selected) AccentGreen else Color.Transparent,
                ),
        )
    }
}

@Composable
private fun MediaTabRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ScanStatePanel(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = AccentGreen,
            strokeWidth = 3.dp,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun EmptyStatusPanel(
    hasGrant: Boolean,
    needsDirectoryReconnect: Boolean,
    grantPathHint: String?,
    scanDebug: StatusScanDebug?,
    title: String,
    description: String,
    helperMessage: String?,
    onOpenWhatsApp: () -> Unit,
    onPickDirectory: () -> Unit,
) {
    val needsAuthorization = !hasGrant || needsDirectoryReconnect

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(22.dp))

        FlowIllustration()

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )

        if (!helperMessage.isNullOrBlank()) {
            Text(
                text = helperMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Button(
            onClick = if (needsAuthorization) onPickDirectory else onOpenWhatsApp,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = if (needsAuthorization) "Authorize WhatsApp access" else "Go to WhatsApp",
                fontWeight = FontWeight.SemiBold,
            )
        }

        TextButton(
            onClick = if (needsAuthorization) onOpenWhatsApp else onPickDirectory,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                text = if (needsAuthorization) "Open WhatsApp first" else "Authorize again",
                color = AccentGreenDark,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = "Supports WhatsApp / WhatsApp Business\nOnly reads the status directory, never chat content.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )

        val formattedGrantPath = formatGrantPath(grantPathHint)
        AnimatedVisibility(visible = !formattedGrantPath.isNullOrBlank()) {
            DebugPathCard(
                title = "Connected folder",
                body = formattedGrantPath.orEmpty(),
                modifier = Modifier.padding(top = 18.dp),
            )
        }

        AnimatedVisibility(visible = scanDebug != null) {
            scanDebug?.let {
                ScanDebugCard(
                    debug = it,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        FaqCard(
            modifier = Modifier.padding(top = 14.dp),
            items = listOf(
                "Statuses appear only after you have viewed them in WhatsApp.",
                "Video statuses may appear only after playback starts and the file finishes caching.",
                "If Android opens the wrong folder, tap Authorize again and confirm the WhatsApp folder.",
            ),
        )
    }
}

@Composable
private fun FlowIllustration() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceWhite)
            .border(1.dp, SoftBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FlowNode("WA", AccentGreen.copy(alpha = 0.18f), AccentGreenDark)
        Text("->", color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowNode("Scan", Color(0xFFEAF6EE), AccentGreenDark)
        Text("->", color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowNode("Save", Color(0xFFF4F7F5), MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun FlowNode(
    label: String,
    background: Color,
    color: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DebugPathCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ScanDebugCard(
    debug: StatusScanDebug,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Scan debug",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Resolved dirs: ${debug.resolvedStatusPaths.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Entries ${debug.totalEntries} | Files ${debug.fileEntries} | Photos ${debug.imageCandidates} | Videos ${debug.videoCandidates}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (debug.sampleNames.isNotEmpty()) {
                Text(
                    text = "Samples: ${debug.sampleNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatGrantPath(grantPathHint: String?): String? {
    if (grantPathHint.isNullOrBlank()) return null
    val uri = runCatching { Uri.parse(grantPathHint) }.getOrNull() ?: return grantPathHint
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
    return documentId?.let(Uri::decode) ?: grantPathHint
}

private fun buildSuggestedInitialUri(sourceApp: SourceApp): Uri {
    val documentId = when (sourceApp) {
        SourceApp.WHATSAPP -> "primary:Android/media/com.whatsapp"
        SourceApp.WHATSAPP_BUSINESS -> "primary:Android/media/com.whatsapp.w4b"
    }
    return DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        documentId,
    )
}

@Composable
private fun FaqCard(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Quick tips",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusResultGrid(
    items: List<StatusItem>,
    message: String?,
    onSave: (StatusItem) -> Unit,
    onPreview: (StatusItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!message.isNullOrBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }
            }
        }

        items(items, key = { it.id }) { item ->
            StatusGridTile(
                item = item,
                onSave = { onSave(item) },
                onPreview = { onPreview(item) },
            )
        }
    }
}

@Composable
private fun StatusGridTile(
    item: StatusItem,
    onSave: () -> Unit,
    onPreview: () -> Unit,
) {
    Box(
        modifier = Modifier
            .shadow(10.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(26.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onPreview),
    ) {
        StatusThumbnail(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f),
        )

        if (item.mediaType == MediaType.VIDEO) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(VideoOverlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                text = formatModifiedAt(item.modifiedAt),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
            )
        }

        IconButton(
            onClick = onSave,
            enabled = !item.isSaved,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (item.isSaved) Color(0xCC1F8F5F) else Color(0xBFFFFFFF),
                ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = "Save",
                tint = if (item.isSaved) Color.White else AccentGreenDark,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun StatusThumbnail(
    item: StatusItem,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var thumbnail by remember(item.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(item.id) {
        thumbnail = loadThumbnail(context, item)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(SoftTile),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (item.mediaType == MediaType.VIDEO) Icons.Outlined.VideoLibrary else Icons.Outlined.Image,
                    contentDescription = null,
                    tint = AccentGreenDark.copy(alpha = 0.75f),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = if (item.mediaType == MediaType.VIDEO) "Video" else "Photo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
fun PreviewRoute(
    viewModel: StatusListViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val item = SelectedPreviewHolder.statusItem

    BackHandler(onBack = onBack)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (item == null) {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    PreviewScreen(
        item = item,
        onBack = onBack,
        onShare = { shareStatus(context, item) },
        onSave = {
            viewModel.save(item)
            SelectedPreviewHolder.statusItem = item.copy(isSaved = true)
        },
        onForward = { forwardStatusToWhatsApp(context, item) },
    )
}

@Composable
private fun PreviewScreen(
    item: StatusItem,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onForward: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D0A))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.mediaType == MediaType.VIDEO) {
                VideoPreview(
                    sourceUri = item.sourceUri,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PhotoPreview(
                    sourceUri = item.sourceUri,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        PreviewTopBar(
            title = if (item.mediaType == MediaType.VIDEO) "Video" else "Photo",
            subtitle = formatModifiedAt(item.modifiedAt),
            onDismiss = onBack,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp),
        )

        AdaptivePreviewActionBar(
            isSaved = item.isSaved,
            onShare = onShare,
            onSave = onSave,
            onForward = onForward,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun PreviewTopBar(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        IconButton(
            onClick = {},
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "More",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun AdaptivePreviewActionBar(
    isSaved: Boolean,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val fullRow = subcompose("full") {
            PreviewActionBarRow(
                variant = ActionBarVariant.Full,
                isSaved = isSaved,
                onShare = onShare,
                onSave = onSave,
                onForward = onForward,
            )
        }.map { it.measure(looseConstraints) }

        val fullHeight = fullRow.maxOfOrNull { it.height } ?: 0
        val fullFits = fullRow.maxOfOrNull { it.width } ?: 0 <= constraints.maxWidth

        val compactRow = if (!fullFits) {
            subcompose("compact") {
                PreviewActionBarRow(
                    variant = ActionBarVariant.Compact,
                    isSaved = isSaved,
                    onShare = onShare,
                    onSave = onSave,
                    onForward = onForward,
                )
            }.map { it.measure(looseConstraints) }
        } else {
            emptyList()
        }

        val compactFits = compactRow.isNotEmpty() &&
            (compactRow.maxOfOrNull { it.width } ?: 0) <= constraints.maxWidth

        val chosen = when {
            fullFits -> fullRow
            compactFits -> compactRow
            else -> subcompose("stacked") {
                PreviewActionBarColumn(
                    isSaved = isSaved,
                    onShare = onShare,
                    onSave = onSave,
                    onForward = onForward,
                )
            }.map { it.measure(looseConstraints) }
        }

        val width = chosen.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = chosen.maxOfOrNull { it.height } ?: fullHeight

        layout(width.coerceIn(constraints.minWidth, constraints.maxWidth), height) {
            chosen.forEach { it.placeRelative(0, 0) }
        }
    }
}

@Composable
private fun PreviewActionBarRow(
    variant: ActionBarVariant,
    isSaved: Boolean,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onForward: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FrostedSurface,
        shape = RoundedCornerShape(26.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PreviewActionChip(
                label = if (variant == ActionBarVariant.Full) "Share" else "Share",
                icon = Icons.Outlined.Share,
                tint = Color.White,
                onClick = onShare,
                modifier = Modifier.weight(1f),
            )
            PreviewSaveChip(
                isSaved = isSaved,
                compact = variant == ActionBarVariant.Compact,
                onClick = onSave,
                modifier = Modifier.weight(if (variant == ActionBarVariant.Full) 1.2f else 1f),
            )
            PreviewActionChip(
                label = if (variant == ActionBarVariant.Full) "Forward" else "Send",
                icon = Icons.Outlined.Send,
                tint = Color.White,
                onClick = onForward,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PreviewActionBarColumn(
    isSaved: Boolean,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onForward: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FrostedSurface,
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PreviewSaveChip(
                isSaved = isSaved,
                compact = false,
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreviewActionChip(
                    label = "Share",
                    icon = Icons.Outlined.Share,
                    tint = Color.White,
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                )
                PreviewActionChip(
                    label = "Forward",
                    icon = Icons.Outlined.Send,
                    tint = Color.White,
                    onClick = onForward,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private enum class ActionBarVariant {
    Full,
    Compact,
}

@Composable
private fun PreviewActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                color = tint,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun PreviewSaveChip(
    isSaved: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isSaved) AccentGreen.copy(alpha = 0.7f) else AccentGreen,
        onClick = onClick,
        enabled = !isSaved,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = if (isSaved) "Saved" else if (compact) "Save" else "Download",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun PhotoPreview(
    sourceUri: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var scale by remember(sourceUri) { mutableFloatStateOf(1f) }

    LaunchedEffect(sourceUri) {
        val item = StatusItem(
            id = sourceUri,
            sourceApp = SourceApp.WHATSAPP,
            mediaType = MediaType.IMAGE,
            sourceUri = sourceUri,
            fileName = "",
            mimeType = "image/jpeg",
            modifiedAt = 0L,
        )
        bitmap = loadThumbnail(context, item, Size(2000, 2000))
    }

    BoxWithConstraints(
        modifier = modifier
            .background(Color(0xFF090D0A))
            .pointerInput(sourceUri) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                    ),
                contentScale = ContentScale.Fit,
            )
        } ?: CircularProgressIndicator(color = AccentGreen)
    }
}

@Composable
private fun VideoPreview(
    sourceUri: String,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember(sourceUri) { mutableStateOf(true) }
    var videoViewRef by remember(sourceUri) { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { viewContext ->
                VideoView(viewContext).apply {
                    videoViewRef = this
                    setVideoURI(Uri.parse(sourceUri))
                    setOnPreparedListener { player ->
                        player.isLooping = true
                        start()
                        isPlaying = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { videoView ->
                videoViewRef = videoView
                if (videoView.tag != sourceUri) {
                    videoView.tag = sourceUri
                    videoView.setVideoURI(Uri.parse(sourceUri))
                }
                if (isPlaying && !videoView.isPlaying) {
                    videoView.seekTo(1)
                    videoView.start()
                }
            },
        )

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable {
                    isPlaying = !isPlaying
                    videoViewRef?.let { view ->
                        if (isPlaying) {
                            view.start()
                        } else {
                            view.pause()
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

private suspend fun loadThumbnail(
    context: Context,
    item: StatusItem,
    size: Size = Size(400, 400),
): Bitmap? = withContext(Dispatchers.IO) {
    val uri = Uri.parse(item.sourceUri)
    runCatching {
        if (item.mediaType == MediaType.VIDEO) {
            loadVideoFrame(context, uri, size)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(size.width, size.height)
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }
        }
    }.getOrNull()
}

private fun loadVideoFrame(
    context: Context,
    uri: Uri,
    size: Size,
): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        runCatching {
            return context.contentResolver.loadThumbnail(uri, size, null)
        }
    }

    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: retriever.getScaledFrameAtTime(
                500_000L,
                MediaMetadataRetriever.OPTION_CLOSEST,
                size.width,
                size.height,
            )
            ?: retriever.getFrameAtTime()
    } finally {
        retriever.release()
    }
}

private fun shareStatus(
    context: Context,
    item: StatusItem,
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType
        putExtra(Intent.EXTRA_STREAM, Uri.parse(item.sourceUri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(shareIntent, "Share status").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }.onFailure {
        Toast.makeText(context, "Unable to share this status", Toast.LENGTH_SHORT).show()
    }
}

private fun forwardStatusToWhatsApp(
    context: Context,
    item: StatusItem,
) {
    val preferredPackage = when (item.sourceApp) {
        SourceApp.WHATSAPP -> "com.whatsapp"
        SourceApp.WHATSAPP_BUSINESS -> "com.whatsapp.w4b"
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType
        putExtra(Intent.EXTRA_STREAM, Uri.parse(item.sourceUri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage(preferredPackage)
    }

    runCatching {
        context.startActivity(sendIntent)
    }.onFailure {
        Toast.makeText(context, "Unable to forward to WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun formatModifiedAt(timestamp: Long): String {
    if (timestamp <= 0L) return "Ready"
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
