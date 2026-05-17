package dev.veeso.biangbianghanzi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.veeso.biangbianghanzi.models.HistoryEntry
import dev.veeso.biangbianghanzi.models.HistoryVariant
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.services.AudioPlayerService
import dev.veeso.biangbianghanzi.ui.AppDesign
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryModeView() {
    val context = LocalContext.current
    val repo = remember { AppSettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val history by repo.history.collectAsState(initial = emptyList())

    val audio = remember { AudioPlayerService(context) }
    DisposableEffect(Unit) {
        onDispose { audio.shutdown() }
    }

    val audioState by audio.state.collectAsState()
    val isSpeaking = audioState == AudioPlayerService.State.SPEAKING

    var speakingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) speakingId = null
    }

    var showTransliterated by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateListOf<String>() }
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear history") },
            text = { Text("All history entries will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repo.clearHistory() }
                        expanded.clear()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) },
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppDesign.horizontalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No history yet. Saved entries from Text and Camera appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = AppDesign.horizontalPadding,
                                vertical = AppDesign.stackSpacing,
                            ),
                        verticalArrangement = Arrangement.spacedBy(AppDesign.stackSpacing),
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            SegmentedButton(
                                selected = !showTransliterated,
                                onClick = { showTransliterated = false },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                label = { Text("Original") },
                            )
                            SegmentedButton(
                                selected = showTransliterated,
                                onClick = { showTransliterated = true },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                label = { Text("Transliterated") },
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = { showClearConfirm = true },
                                enabled = history.isNotEmpty(),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                items(history, key = { it.id }) { entry ->
                    val rowSpeaking = isSpeaking && speakingId == entry.id
                    HistoryRow(
                        entry = entry,
                        showTransliterated = showTransliterated,
                        isExpanded = expanded.contains(entry.id),
                        isSpeaking = rowSpeaking,
                        onToggleExpand = {
                            if (expanded.contains(entry.id)) {
                                expanded.remove(entry.id)
                            } else {
                                expanded.add(entry.id)
                            }
                        },
                        onSpeak = {
                            if (rowSpeaking) {
                                audio.stop()
                                speakingId = null
                            } else {
                                val lang = if (entry.variant == HistoryVariant.CANTONESE) {
                                    AudioPlayerService.Language.CANTONESE
                                } else {
                                    AudioPlayerService.Language.MANDARIN
                                }
                                speakingId = entry.id
                                audio.speak(entry.original, lang)
                            }
                        },
                        onDelete = { scope.launch { repo.deleteHistory(entry.id) } },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    showTransliterated: Boolean,
    isExpanded: Boolean,
    isSpeaking: Boolean,
    onToggleExpand: () -> Unit,
    onSpeak: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart },
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = AppDesign.horizontalPadding),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    horizontal = AppDesign.horizontalPadding,
                    vertical = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (showTransliterated) entry.transliteration else entry.original,
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleExpand() },
            )
            IconButton(onClick = onSpeak) {
                Icon(
                    if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isSpeaking) "Stop" else "Speak",
                )
            }
        }
    }
}
