package dev.veeso.biangbianghanzi.ui.screens

import android.os.LocaleList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.services.SIMPLIFIED_CHINESE
import dev.veeso.biangbianghanzi.services.TRADITIONAL_CHINESE
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModeView(repo: AppSettingsRepository = AppSettingsRepository(LocalContext.current)) {
    val scope = rememberCoroutineScope()
    var languageSelectExpanded by remember { mutableStateOf(false) }

    val currentLocale: Locale =
        LocaleList.getDefault().get(0) // first is active

    val allLanguages = Locale.getAvailableLocales()
        .filter { it.displayLanguage.isNotBlank() }
        .distinctBy { it.displayLanguage }
        .sortedBy { it.displayLanguage }

    // repo values
    val chineseType by repo.chineseType.collectAsState(initial = SIMPLIFIED_CHINESE)
    val translationLanguage by repo.translationLanguage.collectAsState(initial = currentLocale.language)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Translation language
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Translation language",
                    style = MaterialTheme.typography.titleMedium
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { languageSelectExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currentLanguageName(allLanguages, translationLanguage))
                    }

                    DropdownMenu(
                        expanded = languageSelectExpanded,
                        onDismissRequest = { languageSelectExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        allLanguages.forEach { locale ->
                            DropdownMenuItem(
                                text = { Text(locale.getDisplayLanguage(Locale.getDefault())) },
                                onClick = {
                                    scope.launch { repo.setTranslationLanguage(locale.language) }
                                    languageSelectExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Chinese type section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Chinese variant", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE).forEach { value ->
                        FilterChip(
                            selected = chineseType == value,
                            onClick = { scope.launch { repo.setChineseType(value) } },
                            label = { Text(chineseTypeLabel(value)) }
                        )
                    }
                }
            }

        }
    }


}

private fun currentLanguageName(locales: List<Locale>, current: String): String {
    val currentLocale = locales.find { it.language == current };
    return currentLocale?.displayLanguage ?: current
}

private fun chineseTypeLabel(value: String): String {
    return when (value) {
        SIMPLIFIED_CHINESE -> "Simplified Chinese"
        TRADITIONAL_CHINESE -> "Traditional Chinese"
        else -> "Unknown"
    }
}