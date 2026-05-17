package dev.veeso.biangbianghanzi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.ui.MainScreen
import dev.veeso.biangbianghanzi.ui.theme.BiangBiangHanziTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val repo = AppSettingsRepository(applicationContext)
            lifecycleScope.launch { repo.registerLaunch() }
        }
        enableEdgeToEdge()
        setContent {
            BiangBiangHanziTheme {
                MainScreen()
            }
        }
    }
}
