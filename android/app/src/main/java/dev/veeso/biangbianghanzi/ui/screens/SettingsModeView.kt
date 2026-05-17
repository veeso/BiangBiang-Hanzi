package dev.veeso.biangbianghanzi.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.ConfigurationCompat
import dev.veeso.biangbianghanzi.BuildConfig
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.services.CANTONESE
import dev.veeso.biangbianghanzi.services.SIMPLIFIED_CHINESE
import dev.veeso.biangbianghanzi.services.ReviewPromptPolicy
import dev.veeso.biangbianghanzi.services.TRADITIONAL_CHINESE
import dev.veeso.biangbianghanzi.ui.AppDesign
import dev.veeso.biangbianghanzi.ui.components.launchInAppReview
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModeView(repo: AppSettingsRepository = AppSettingsRepository(LocalContext.current)) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var languageSelectExpanded by remember { mutableStateOf(false) }

    val locales = ConfigurationCompat.getLocales(LocalConfiguration.current)
    val currentLocale: Locale = if (locales.isEmpty) Locale.ENGLISH else locales[0]!!

    val allLanguages = remember {
        Locale.getAvailableLocales()
            .filter { it.displayLanguage.isNotBlank() }
            .distinctBy { it.displayLanguage }
            .sortedBy { it.displayLanguage }
    }

    val reviewLaunchCount by repo.reviewLaunchCount.collectAsState(initial = 0)
    val reviewDismissed by repo.reviewPromptDismissed.collectAsState(initial = false)
    val reviewAttempts by repo.reviewAttempts.collectAsState(initial = 0)

    val chineseType by repo.chineseType.collectAsState(initial = SIMPLIFIED_CHINESE)
    val translationLanguage by repo.translationLanguage.collectAsState(
        initial = currentLocale.language,
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(
                    horizontal = AppDesign.horizontalPadding,
                    vertical = AppDesign.sectionSpacing,
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppDesign.sectionSpacing),
        ) {
            SettingsSection(title = "Translation language") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { languageSelectExpanded = true },
                        enabled = chineseType != CANTONESE,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(currentLanguageName(allLanguages, translationLanguage))
                    }

                    DropdownMenu(
                        expanded = languageSelectExpanded,
                        onDismissRequest = { languageSelectExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f),
                    ) {
                        allLanguages.forEach { locale ->
                            DropdownMenuItem(
                                text = { Text(locale.getDisplayLanguage(currentLocale)) },
                                onClick = {
                                    scope.launch { repo.setTranslationLanguage(locale.language) }
                                    languageSelectExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "Chinese variant") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val variants = listOf(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE, CANTONESE)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        variants.forEachIndexed { index, value ->
                            SegmentedButton(
                                selected = chineseType == value,
                                onClick = { scope.launch { repo.setChineseType(value) } },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = variants.size,
                                ),
                                label = { Text(chineseTypeLabel(value)) },
                            )
                        }
                    }
                    if (chineseType == CANTONESE) {
                        Text(
                            "Translation is disabled in Cantonese mode. Hanzi will be romanized to Jyutping.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SettingsSection(title = "Report a bug") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = { openGitHubIssues(context) }) {
                        Text("Open GitHub Issues")
                    }
                    Button(onClick = { sendBugEmail(context) }) {
                        Text("Send Email")
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                SettingsSection(title = "Debug") {
                    Text(
                        "review launchCount=$reviewLaunchCount dismissed=$reviewDismissed " +
                            "attempts=$reviewAttempts/${ReviewPromptPolicy.MAX_ATTEMPTS} " +
                            "(fires at ${ReviewPromptPolicy.LAUNCH_THRESHOLD})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { scope.launch { repo.resetReviewPrompt() } }) {
                            Text("Reset")
                        }
                        Button(onClick = { scope.launch { launchInAppReview(context) } }) {
                            Text("Force review now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDesign.stackSpacing),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

private fun currentLanguageName(locales: List<Locale>, current: String): String =
    locales.find { it.language == current }?.displayLanguage ?: current

private fun chineseTypeLabel(value: String): String = when (value) {
    SIMPLIFIED_CHINESE -> "Simplified"
    TRADITIONAL_CHINESE -> "Traditional"
    CANTONESE -> "Cantonese"
    else -> "Unknown"
}

private fun openGitHubIssues(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        "https://github.com/veeso/BiangBiang-Hanzi/issues/new".toUri(),
    )
    context.startActivity(intent)
}

private fun sendBugEmail(context: Context) {
    val subject = "[Android] Bug report – BiangBiang Hanzi"
    val body = """
Description:

Steps to reproduce:

Device:
Android version:
"""
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:info@veeso.dev?subject=${subject}&body=${body}".toUri()
    }
    context.startActivity(Intent.createChooser(intent, "Send bug report"))
}
