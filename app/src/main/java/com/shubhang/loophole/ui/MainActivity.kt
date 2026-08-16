package com.shubhang.loophole.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shubhang.loophole.appContainer
import com.shubhang.loophole.settings.AddTileResult
import com.shubhang.loophole.ui.theme.LoopholeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = appContainer
        setContent {
            LoopholeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: DevSettingsViewModel = viewModel(
                        factory = DevSettingsViewModel.factory(container.devSettings)
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // One-shot event: the system reports the outcome through a
                    // callback, and the screen clears it once it has been shown.
                    var addTileResult by remember { mutableStateOf<AddTileResult?>(null) }

                    LoopholeScreen(
                        uiState = uiState,
                        packageName = packageName,
                        canAddQuickSettingsTile = container.quickSettingsTileManager.isSupported,
                        onToggle = viewModel::onToggle,
                        onOpenDeveloperOptions = { container.developerOptionsLauncher.open() },
                        onAddQuickSettingsTile = {
                            container.quickSettingsTileManager.requestAddTile { result ->
                                addTileResult = result
                            }
                        },
                        addTileResult = addTileResult,
                        onAddTileResultShown = { addTileResult = null },
                    )
                }
            }
        }
    }
}
