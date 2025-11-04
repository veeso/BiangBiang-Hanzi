package dev.veeso.biangbianghanzi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.veeso.biangbianghanzi.ui.MainScreen
import dev.veeso.biangbianghanzi.ui.theme.BiangBiangHanziTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiangBiangHanziTheme {
                MainScreen()
            }
        }
    }
}
