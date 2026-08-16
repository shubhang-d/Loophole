package com.shubhang.loophole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubhang.loophole.R
import com.shubhang.loophole.settings.AddTileResult
import com.shubhang.loophole.ui.components.Header
import com.shubhang.loophole.ui.components.HeroToggleCard
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
    onToggle: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onAddQuickSettingsTile: () -> Unit,
    addTileResult: AddTileResult? = null,
    onAddTileResultShown: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Kept separately from addTileResult so the snackbar keeps its icon for the
    // whole time it is on screen, not just until the event is consumed.
    var shown by remember { mutableStateOf<AddTileResult?>(null) }

    val message = addTileResult?.messageRes()?.let { stringResource(it) }
    LaunchedEffect(addTileResult) {
        if (addTileResult == null) return@LaunchedEffect
        if (message != null) {
            shown = addTileResult
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Header()

            HeroToggleCard(
                enabled = uiState.isEnabled,
                onToggle = onToggle
            )

            FilledTonalButton(
                onClick = onOpenDeveloperOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_gear),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Open Developer Options", fontWeight = FontWeight.SemiBold)
            }

            if (canAddQuickSettingsTile) {
                OutlinedButton(
                    onClick = onAddQuickSettingsTile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Add tile to Quick Settings", fontWeight = FontWeight.SemiBold)
                }
            }

            if (uiState.isPermissionDenied) {
                PermissionCard(packageName = packageName)
            }

            HowToCard()
        }
    }
}

/** Dismissal is the user's own doing, so it passes without a message. */
private fun AddTileResult.messageRes(): Int? = when (this) {
    AddTileResult.ADDED -> R.string.tile_add_added
    AddTileResult.ALREADY_ADDED -> R.string.tile_add_already_added
    AddTileResult.FAILED -> R.string.tile_add_failed
    AddTileResult.DISMISSED -> null
}
