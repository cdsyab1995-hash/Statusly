package com.yourcompany.statusvault.feature.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val AccentGreen = Color(0xFF25D366)
private val AccentGreenDark = Color(0xFF128C4A)
private val SoftBackground = Color(0xFFF7FAF8)
private val SurfaceWhite = Color(0xFFFFFFFF)

@Composable
fun SettingsRoute(
    privacyPolicyUrl: String? = null,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.16f))
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = AccentGreenDark,
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Privacy, access and product info",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SettingsCard(
            icon = Icons.Outlined.Widgets,
            title = "Status source",
            body = "Supports WhatsApp and WhatsApp Business. If scanning stops working, reconnect folder access here.",
        )
        SettingsCard(
            icon = Icons.Outlined.Lock,
            title = "Privacy",
            body = "All scanning and saving stay on your device. The app only reads the status directory and never your chat content.",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            onClick = {
                if (privacyPolicyUrl.isNullOrBlank()) {
                    Toast.makeText(
                        context,
                        "Privacy policy URL is not configured yet.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@SettingsCard
                }

                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }.onFailure {
                    Toast.makeText(
                        context,
                        "Unable to open the privacy policy.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )
        SettingsCard(
            icon = Icons.Outlined.Settings,
            title = "About Statusly",
            body = "This build focuses on fast scanning, clearer previews, and a simpler save flow for viewed statuses.",
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.14f))
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentGreenDark,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            trailing?.invoke()
        }
    }
}
