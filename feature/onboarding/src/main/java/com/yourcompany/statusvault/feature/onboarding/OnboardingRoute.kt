package com.yourcompany.statusvault.feature.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourcompany.statusvault.core.common.SelectedSourceHolder
import com.yourcompany.statusvault.domain.model.SourceApp

@Composable
fun OnboardingRoute(
    onContinue: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val takeFlags = result.data?.flags?.and(flags) ?: flags
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        viewModel.saveGrant(uri.toString())
    }

    OnboardingScreen(
        selectedSourceApp = uiState.selectedSourceApp,
        hasGrant = uiState.hasGrant,
        onSelectSourceApp = viewModel::onSourceAppSelected,
        onPickDirectory = {
            SelectedSourceHolder.sourceApp = uiState.selectedSourceApp
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid"),
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            launcher.launch(intent)
        },
        onContinue = {
            SelectedSourceHolder.sourceApp = uiState.selectedSourceApp
            onContinue()
        },
    )
}

@Composable
fun OnboardingScreen(
    selectedSourceApp: SourceApp,
    hasGrant: Boolean,
    onSelectSourceApp: (SourceApp) -> Unit,
    onPickDirectory: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        HeroSection()
        Spacer(modifier = Modifier.height(20.dp))
        AccountSelector(
            selectedSourceApp = selectedSourceApp,
            onSelectSourceApp = onSelectSourceApp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SetupSteps(
            hasGrant = hasGrant,
            onPickDirectory = onPickDirectory,
        )
        Spacer(modifier = Modifier.height(18.dp))
        ContinuePanel(
            hasGrant = hasGrant,
            onContinue = onContinue,
        )
    }
}

@Composable
private fun HeroSection() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "PRIVATE ON YOUR PHONE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "Save viewed statuses in a few simple taps",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = "Watch a status in WhatsApp, connect its folder once, then come back here to keep photos and videos locally.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun AccountSelector(
    selectedSourceApp: SourceApp,
    onSelectSourceApp: (SourceApp) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = "Choose your source",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Pick the app whose statuses you want to save.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SourceChip(
                    label = "WhatsApp",
                    selected = selectedSourceApp == SourceApp.WHATSAPP,
                    onClick = { onSelectSourceApp(SourceApp.WHATSAPP) },
                    modifier = Modifier.weight(1f),
                )
                SourceChip(
                    label = "WA Business",
                    selected = selectedSourceApp == SourceApp.WHATSAPP_BUSINESS,
                    onClick = { onSelectSourceApp(SourceApp.WHATSAPP_BUSINESS) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SourceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ButtonDefaults.outlinedButtonColors(
        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        colors = colors,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SetupSteps(
    hasGrant: Boolean,
    onPickDirectory: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "How it works",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            StepRow(
                index = "1",
                title = "Watch a status first",
                body = "Open WhatsApp and view the photo or video you want to keep.",
            )
            StepRow(
                index = "2",
                title = "Connect the status folder",
                body = "This one-time step lets the app scan the statuses you already viewed.",
            )
            StepRow(
                index = "3",
                title = "Save it to your gallery",
                body = "Your saved items stay local and appear again in History.",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(18.dp),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = if (hasGrant) "Folder connected" else "Folder not connected yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (hasGrant) {
                            "You're ready to scan statuses."
                        } else {
                            "Choose the WhatsApp status folder to continue."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            OutlinedButton(
                onClick = onPickDirectory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = if (hasGrant) "Reconnect Status Folder" else "Choose Status Folder",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StepRow(
    index: String,
    title: String,
    body: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = index,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ContinuePanel(
    hasGrant: Boolean,
    onContinue: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (hasGrant) {
                    "Everything is ready"
                } else {
                    "Connect the folder before continuing"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (hasGrant) {
                    "Go to the scan page and start saving your viewed statuses."
                } else {
                    "Once the folder is connected, the scan page can show the statuses you've already watched."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onContinue,
                enabled = hasGrant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = "Continue to Scan",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
