package com.shubhang.loophole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubhang.loophole.R
import com.shubhang.loophole.settings.AddTileResult
import com.shubhang.loophole.settings.TileType
import com.shubhang.loophole.ui.components.Header
import com.shubhang.loophole.ui.components.HeroToggleContainer
import com.shubhang.loophole.ui.components.HowToCard
import com.shubhang.loophole.ui.components.LoopholeSnackbar
import com.shubhang.loophole.ui.components.PermissionCard

/**
 * Stateless: renders [uiState] and reports events, so it is previewable and
 * testable without a ViewModel. [MainActivity] supplies the callbacks.
 */
@Composable
fun LoopholeScreen(
    uiState: DevSettingsUiState,
    packageName: String,
    canAddQuickSettingsTile: Boolean,
    onToggleDevOptions: () -> Unit,
    onToggleUsbDebugging: () -> Unit,
    onToggleWirelessDebugging: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onOpenWirelessDebugging: () -> Unit,
    onAddQuickSettingsTile: (TileType) -> Unit,
    addTileResult: Pair<TileType, AddTileResult>? = null,
    onAddTileResultShown: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var shown by remember { mutableStateOf<AddTileResult?>(null) }

    val message = addTileResult?.let { (tileType, result) ->
        result.messageRes(tileType)?.let { stringResource(it) }
    }

    LaunchedEffect(addTileResult) {
        if (addTileResult == null) return@LaunchedEffect
        if (message != null) {
            shown = addTileResult.second
            snackbarHostState.showSnackbar(message)
            shown = null
        }
        onAddTileResultShown()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                LoopholeSnackbar(message = data.visuals.message, result = shown)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header()

            HeroToggleContainer(
                devEnabled = uiState.isEnabled,
                onToggleDev = onToggleDevOptions,
                onOpenDeveloperOptions = onOpenDeveloperOptions,
                usbEnabled = uiState.isUsbDebuggingEnabled,
                onToggleUsb = onToggleUsbDebugging,
                wirelessEnabled = uiState.isWirelessDebuggingEnabled,
                onToggleWireless = onToggleWirelessDebugging,
                isWirelessSupported = uiState.isWirelessDebuggingSupported,
                onOpenWirelessDebugging = onOpenWirelessDebugging,
                canAddTile = canAddQuickSettingsTile,
                onAddUsbTile = { onAddQuickSettingsTile(TileType.USB_DEBUG) },
                onAddWirelessTile = { onAddQuickSettingsTile(TileType.WIRELESS_DEBUG) }
            )

            if (canAddQuickSettingsTile) {
                OutlinedButton(
                    onClick = { onAddQuickSettingsTile(TileType.DEV_MODE) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Add Dev Mode tile to Quick Settings", fontWeight = FontWeight.SemiBold)
                }
            }

            if (uiState.isPermissionDenied) {
                PermissionCard(packageName = packageName)
            }

            HowToCard()
        }
    }
}

private fun AddTileResult.messageRes(tileType: TileType): Int? = when (this) {
    AddTileResult.ADDED -> when (tileType) {
        TileType.DEV_MODE -> R.string.tile_add_added
        TileType.USB_DEBUG -> R.string.tile_usb_add_added
        TileType.WIRELESS_DEBUG -> R.string.tile_wireless_add_added
    }
    AddTileResult.ALREADY_ADDED -> when (tileType) {
        TileType.DEV_MODE -> R.string.tile_add_already_added
        TileType.USB_DEBUG -> R.string.tile_usb_add_already_added
        TileType.WIRELESS_DEBUG -> R.string.tile_wireless_add_already_added
    }
    AddTileResult.FAILED -> R.string.tile_add_failed
    AddTileResult.DISMISSED -> null
}
