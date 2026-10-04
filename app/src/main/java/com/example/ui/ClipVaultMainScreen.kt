package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClipItem
import com.example.ui.components.AddClipDialog
import com.example.ui.components.BackgroundSetupDialog
import com.example.ui.components.BackgroundSyncCard
import com.example.ui.components.ClearConfirmDialog
import com.example.ui.components.ClipCard
import com.example.ui.components.EditClipDialog
import com.example.ui.components.FeatureTourDialog
import com.example.ui.components.PrivacyDialog
import com.example.ui.components.SearchAndFilterBar
import com.example.ui.components.SettingsDialog
import com.example.ui.components.UseCasesDialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipVaultMainScreen(
    viewModel: ClipVaultViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ClipVaultTopBar(
                onTourClick = { viewModel.openFeatureTour() },
                onUseCasesClick = { viewModel.openUseCases() },
                onPrivacyClick = { viewModel.openPrivacyInfo() },
                onSettingsClick = { viewModel.openSettings() }
            )
        },
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Paste from Clipboard Action button
                Button(
                    onClick = {
                        val captured = viewModel.captureCurrentClipboard()
                        if (!captured) {
                            viewModel.triggerHapticFeedback()
                            viewModel.openAddDialog()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("quick_paste_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Paste Clip",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Add Clip FAB
                FloatingActionButton(
                    onClick = { viewModel.openAddDialog() },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_clip_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new clip",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Mini Badge / Quick Catch Bar
            HeroCatchBanner(
                onTourClick = { viewModel.openFeatureTour() },
                onPrivacyClick = { viewModel.openPrivacyInfo() }
            )

            // Background Auto-Capture Monitoring Card
            BackgroundSyncCard(
                isServiceActive = uiState.isBackgroundMonitorActive,
                isAccessibilityGranted = uiState.isAccessibilityGranted,
                onToggleService = { viewModel.setBackgroundMonitorActive(it) },
                onOpenSetup = { viewModel.openBackgroundSetupDialog() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Search Bar & Filter Chips
            SearchAndFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                activeTab = uiState.activeTab,
                onTabSelected = { viewModel.onTabSelected(it) },
                totalCount = uiState.totalCount,
                pinnedCount = uiState.pinnedCount,
                onClearAllClicked = { viewModel.openClearConfirmDialog() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Clips List or Empty State
            if (uiState.clips.isEmpty()) {
                EmptyStateView(
                    searchQuery = uiState.searchQuery,
                    activeTab = uiState.activeTab,
                    onAddSampleClips = { viewModel.resetToStarterPack() },
                    onAddNewClip = { viewModel.openAddDialog() }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("clips_list"),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 80.dp // ensure space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.clips,
                        key = { it.id }
                    ) { clip ->
                        ClipCard(
                            clip = clip,
                            searchQuery = uiState.searchQuery,
                            onCopy = { viewModel.copyClipToClipboard(clip) },
                            onPinToggle = { viewModel.togglePin(clip) },
                            onEdit = { viewModel.openEditDialog(clip) },
                            onDelete = { viewModel.deleteClip(clip) },
                            onMaskToggle = { viewModel.toggleMask(clip) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddDialog) {
        AddClipDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { content, isPinned -> viewModel.addClip(content, isPinned) }
        )
    }

    uiState.editingClip?.let { clipToEdit ->
        EditClipDialog(
            clip = clipToEdit,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { newContent, copyAfterSave ->
                viewModel.saveEditedClip(clipToEdit, newContent, copyAfterSave)
            }
        )
    }

    if (uiState.showClearConfirmDialog) {
        ClearConfirmDialog(
            pinnedCount = uiState.pinnedCount,
            onClearUnpinned = { viewModel.clearUnpinnedClips() },
            onClearAll = { viewModel.clearAllClips() },
            onDismiss = { viewModel.closeClearConfirmDialog() }
        )
    }

    if (uiState.showFeatureTour) {
        FeatureTourDialog(
            onDismiss = { viewModel.closeFeatureTour() }
        )
    }

    if (uiState.showUseCases) {
        UseCasesDialog(
            onDismiss = { viewModel.closeUseCases() },
            onAddSampleSnippet = { snippet -> viewModel.addClip(snippet, isPinned = false) }
        )
    }

    if (uiState.showPrivacyInfo) {
        PrivacyDialog(
            onDismiss = { viewModel.closePrivacyInfo() }
        )
    }

    if (uiState.showBackgroundSetupDialog) {
        BackgroundSetupDialog(
            onOpenSettings = { viewModel.openAccessibilitySettings() },
            onDismiss = { viewModel.closeBackgroundSetupDialog() }
        )
    }

    if (uiState.showSettings) {
        SettingsDialog(
            autoTrim = uiState.autoTrimWhitespace,
            onAutoTrimChange = { viewModel.setAutoTrim(it) },
            haptics = uiState.hapticFeedback,
            onHapticsChange = { viewModel.setHaptics(it) },
            maskSecrets = uiState.maskSecretsByDefault,
            onMaskSecretsChange = { viewModel.setMaskSecrets(it) },
            bgMonitor = uiState.isBackgroundMonitorActive,
            onBgMonitorChange = { viewModel.setBackgroundMonitorActive(it) },
            onOpenBackgroundSetup = { viewModel.openBackgroundSetupDialog() },
            onRestoreStarterClips = { viewModel.resetToStarterPack() },
            onDismiss = { viewModel.closeSettings() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipVaultTopBar(
    onTourClick: () -> Unit,
    onUseCasesClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(PrimaryIndigo, AccentPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "ClipVault",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "by AILoopLabs",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onTourClick,
                modifier = Modifier.testTag("top_bar_tour_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = "Feature Tour",
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onUseCasesClick,
                modifier = Modifier.testTag("top_bar_use_cases_button")
            ) {
                Icon(
                    imageVector = Icons.Default.WorkOutline,
                    contentDescription = "Use Cases",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onPrivacyClick,
                modifier = Modifier.testTag("top_bar_privacy_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Privacy Info",
                    tint = SuccessEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("top_bar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun HeroCatchBanner(
    onTourClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SuccessEmerald)
                )
                Text(
                    text = "100% Local · Zero Tracking",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Verify",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessEmerald,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onPrivacyClick() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Text(
                    text = "Feature Tour",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onTourClick() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    searchQuery: String,
    activeTab: FilterTab,
    onAddSampleClips: () -> Unit,
    onAddNewClip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimaryIndigo.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Info else Icons.Default.Assignment,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotEmpty()) {
            Text(
                text = "No clips match \"$searchQuery\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try searching for a different keyword or check other categories.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (activeTab == FilterTab.PINNED) {
            Text(
                text = "No pinned clips yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Star any important clip to keep it at the top and survive Clear All.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Your ClipVault is empty",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Copy text anywhere on your device or tap below to load starter clips.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onAddSampleClips,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("empty_state_load_samples_button")
                ) {
                    Text("Load Starter Clips", fontSize = 12.sp)
                }

                Button(
                    onClick = onAddNewClip,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.testTag("empty_state_add_clip_button")
                ) {
                    Text("+ Add Clip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
