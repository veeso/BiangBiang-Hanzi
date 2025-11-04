package dev.veeso.biangbianghanzi.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier


@Composable
fun CameraModeView() {

    Scaffold(
        bottomBar = {
            Text("Hello")
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Text("Hello")
        }
    }
}
