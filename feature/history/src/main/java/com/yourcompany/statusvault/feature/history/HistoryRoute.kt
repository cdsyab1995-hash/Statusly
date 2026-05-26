package com.yourcompany.statusvault.feature.history

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourcompany.statusvault.domain.model.MediaType
import com.yourcompany.statusvault.domain.model.SavedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val AccentGreen = Color(0xFF25D366)
private val AccentGreenDark = Color(0xFF128C4A)
private val SoftBackground = Color(0xFFF7FAF8)
private val SoftTile = Color(0xFFF2F6F3)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val VideoOverlay = Color(0x66000000)

private enum class SavedTab {
    IMAGE,
    VIDEO,
}

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(SavedTab.IMAGE) }

    val imageCount = uiState.items.count { it.mediaType == MediaType.IMAGE }
    val videoCount = uiState.items.count { it.mediaType == MediaType.VIDEO }
    val filteredItems = uiState.items.filter {
        when (selectedTab) {
            SavedTab.IMAGE -> it.mediaType == MediaType.IMAGE
            SavedTab.VIDEO -> it.mediaType == MediaType.VIDEO
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground),
    ) {
        SavedHeader(
            selectedTab = selectedTab,
            imageCount = imageCount,
            videoCount = videoCount,
            onSelectTab = { selectedTab = it },
        )

        if (filteredItems.isEmpty()) {
            EmptySavedPanel(isVideo = selectedTab == SavedTab.VIDEO)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                text = "Saved items on your device",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Everything you download from the Status tab will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }

                items(filteredItems, key = { it.id }) { item ->
                    SavedGridTile(item = item)
                }
            }
        }
    }
}

@Composable
private fun SavedHeader(
    selectedTab: SavedTab,
    imageCount: Int,
    videoCount: Int,
    onSelectTab: (SavedTab) -> Unit,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Saved",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentGreenDark,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Downloaded statuses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        MediaTabRow {
            SavedPill(
                label = "Photos $imageCount",
                selected = selectedTab == SavedTab.IMAGE,
                icon = Icons.Outlined.Image,
                onClick = { onSelectTab(SavedTab.IMAGE) },
                modifier = Modifier.weight(1f),
            )
            SavedPill(
                label = "Videos $videoCount",
                selected = selectedTab == SavedTab.VIDEO,
                icon = Icons.Outlined.VideoLibrary,
                onClick = { onSelectTab(SavedTab.VIDEO) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MediaTabRow(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun SavedPill(
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
                .background(if (selected) AccentGreen else Color.Transparent),
        )
    }
}

@Composable
private fun SavedGridTile(
    item: SavedItem,
) {
    Box(
        modifier = Modifier
            .shadow(10.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(26.dp))
            .background(SurfaceWhite),
    ) {
        SavedThumbnail(
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
                text = if (item.mediaType == MediaType.VIDEO) "Saved video" else "Saved photo",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SavedThumbnail(
    item: SavedItem,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var thumbnail by remember(item.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(item.id) {
        thumbnail = loadSavedThumbnail(context, item)
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
private fun EmptySavedPanel(
    isVideo: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceWhite)
                .padding(horizontal = 22.dp, vertical = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isVideo) Icons.Outlined.VideoLibrary else Icons.Outlined.Image,
                contentDescription = null,
                tint = AccentGreenDark,
            )
        }
        Text(
            text = if (isVideo) "No saved videos yet" else "No saved photos yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = "Download a status from the Status tab and it will appear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private suspend fun loadSavedThumbnail(
    context: Context,
    item: SavedItem,
    size: Size = Size(400, 400),
): Bitmap? = withContext(Dispatchers.IO) {
    val uri = Uri.parse(item.savedUri)
    runCatching {
        if (item.mediaType == MediaType.VIDEO) {
            loadSavedVideoFrame(context, uri, size)
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

private fun loadSavedVideoFrame(
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
