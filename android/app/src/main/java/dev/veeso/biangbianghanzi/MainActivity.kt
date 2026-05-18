package dev.veeso.biangbianghanzi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.veeso.biangbianghanzi.config.chineseConfig
import dev.veeso.biangbiangui.ui.BiangBiangRoot

/**
 * Config-only entry point: the BiangBiangUI library renders every screen
 * and owns History, the rate prompt, TTS and the OCR pipeline (including
 * `registerLaunch`). The app supplies only [chineseConfig] + two
 * transliterators.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiangBiangRoot(chineseConfig(this))
        }
    }
}
