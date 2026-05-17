package dev.veeso.biangbianghanzi.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.veeso.biangbianghanzi.ui.screens.CameraModeView
import dev.veeso.biangbianghanzi.ui.screens.HistoryModeView
import dev.veeso.biangbianghanzi.ui.screens.SettingsModeView
import dev.veeso.biangbianghanzi.ui.screens.TextModeView

private enum class Tab(val label: String, val icon: ImageVector) {
    Text("Text", Icons.Default.TextFields),
    Camera("Camera", Icons.Default.CameraAlt),
    History("History", Icons.Default.History),
    Settings("Settings", Icons.Default.Settings),

}

@Composable
fun MainScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Text) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                Tab.Text -> TextModeView()
                Tab.Camera -> CameraModeView()
                Tab.History -> HistoryModeView()
                Tab.Settings -> SettingsModeView()
            }
        }
    }
}
