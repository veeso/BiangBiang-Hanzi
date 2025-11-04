package dev.veeso.biangbianghanzi.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import dev.veeso.biangbianghanzi.ui.screens.*

const val TEXT_MODE_VIEW = 0;
const val CAMERA_MODE_VIEW = 1
const val SETTINGS_MODE_VIEW = 2

@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(TEXT_MODE_VIEW) }
    val items = listOf("Text", "Camera", "Settings")
    val icons = listOf(Icons.Default.TextFields, Icons.Default.CameraAlt, Icons.Default.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                TEXT_MODE_VIEW -> TextModeView()
                CAMERA_MODE_VIEW -> CameraModeView()
                SETTINGS_MODE_VIEW -> SettingsModeView()
            }
        }
    }
}
