package com.shubhang.loophole

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shubhang.loophole.ui.theme.LoopholeTheme
import com.shubhang.loophole.widget.refreshLoopholeWidgets
import com.shubhang.loophole.widget.refreshWirelessDebugWidgets
import android.widget.Toast
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoopholeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoopholeScreen()
                }
            }
        }
    }
}

/**
 * Prompts the system to add the tile to Quick Settings via a one-tap
 * dialog (API 33+). This avoids relying on the user finding it in the QS editor,
 * where a freshly installed custom tile can take a SystemUI restart to appear.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestAddTile(context: Context, serviceClass: Class<*>, labelRes: Int, iconRes: Int, onResult: (Int) -> Unit) {
    val statusBar = context.getSystemService(StatusBarManager::class.java) ?: return
    statusBar.requestAddTileService(
        ComponentName(context, serviceClass),
        context.getString(labelRes),
        Icon.createWithResource(context, iconRes),
        context.mainExecutor
    ) { result ->
        onResult(result)
    }
}

@Composable
fun LoopholeScreen() {
    val context = LocalContext.current

    var devModeEnabled by remember { mutableStateOf(DevMode.isEnabled(context)) }
    var wirelessEnabled by remember { mutableStateOf(WirelessDevMode.isEnabled(context)) }
    var devTileAdded by remember { mutableStateOf(TileSettings.isDevTileAdded(context)) }
    var wirelessTileAdded by remember { mutableStateOf(TileSettings.isWirelessTileAdded(context)) }
    var permissionDenied by remember { mutableStateOf(false) }

    // Re-read the flags whenever the activity resumes so the UI reflects changes
    // made from the Quick Settings tile, the widgets, or the system settings app.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                devModeEnabled = DevMode.isEnabled(context)
                wirelessEnabled = WirelessDevMode.isEnabled(context)
                devTileAdded = TileSettings.isDevTileAdded(context)
                wirelessTileAdded = TileSettings.isWirelessTileAdded(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scope = rememberCoroutineScope()
    fun toggleDevMode(target: Boolean) {
        val ok = DevMode.setEnabled(context, target)
        permissionDenied = !ok
        if (!ok) {
            Toast.makeText(context, "WRITE_SECURE_SETTINGS not granted", Toast.LENGTH_LONG).show()
        }
        devModeEnabled = DevMode.isEnabled(context)
        // Keep the home-screen widget in sync with the change made from the app.
        scope.launch { refreshLoopholeWidgets(context) }
    }

    fun toggleWirelessDebug(target: Boolean) {
        val ok = WirelessDevMode.setEnabled(context, target)
        permissionDenied = !ok
        if (!ok) {
            Toast.makeText(context, "WRITE_SECURE_SETTINGS not granted", Toast.LENGTH_LONG).show()
        }
        wirelessEnabled = WirelessDevMode.isEnabled(context)
        // Keep the home-screen widget in sync with the change made from the app.
        scope.launch { refreshWirelessDebugWidgets(context) }
    }

    Scaffold { padding ->
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
                title = "Developer Options",
                iconRes = R.drawable.ic_dev_mode_tile,
                enabled = devModeEnabled,
                onToggle = { toggleDevMode(!devModeEnabled) },
                descriptionOn = "Tap to turn off before opening a banking app.",
                descriptionOff = "Tap to turn on when you want to develop."
            )

            HeroToggleCard(
                title = "Wireless Debugging",
                iconRes = R.drawable.ic_wireless_debug,
                enabled = wirelessEnabled,
                onToggle = { toggleWirelessDebug(!wirelessEnabled) },
                descriptionOn = "Tap to turn off Wireless Debugging.",
                descriptionOff = "Tap to turn on Wireless Debugging."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { DevMode.openDeveloperOptions(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_gear),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Dev Options", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = { WirelessDevMode.openWirelessDebugging(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_wireless_debug),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Wireless Debug", fontWeight = FontWeight.SemiBold)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!devTileAdded) {
                                requestAddTile(
                                    context,
                                    DevModeTileService::class.java,
                                    R.string.tile_label,
                                    R.drawable.ic_dev_mode_tile
                                ) { result ->
                                    if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ||
                                        result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED
                                    ) {
                                        TileSettings.setDevTileAdded(context, true)
                                        devTileAdded = true
                                    }
                                }
                            }
                        },
                        enabled = !devTileAdded,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (devTileAdded) "Dev QS Tile Added" else "Add Dev QS Tile",
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!devTileAdded) {
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!wirelessTileAdded) {
                                requestAddTile(
                                    context,
                                    WirelessDebugTileService::class.java,
                                    R.string.tile_wireless_label,
                                    R.drawable.ic_wireless_debug
                                ) { result ->
                                    if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ||
                                        result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED
                                    ) {
                                        TileSettings.setWirelessTileAdded(context, true)
                                        wirelessTileAdded = true
                                    }
                                }
                            }
                        },
                        enabled = !wirelessTileAdded,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (wirelessTileAdded) "Wireless QS Tile Added" else "Add Wireless QS Tile",
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!wirelessTileAdded) {
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (permissionDenied) {
                PermissionCard(packageName = context.packageName)
            }

            HowToCard()
        }
    }
}

@Composable
fun Header() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Loophole",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "One-tap Developer Options",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HeroToggleCard(
    title: String,
    iconRes: Int,
    enabled: Boolean,
    onToggle: () -> Unit,
    descriptionOn: String,
    descriptionOff: String
) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "heroContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "heroContent"
    )
    val iconBadgeColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "iconBadge"
    )

    ElevatedCard(
        onClick = onToggle,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (enabled) descriptionOn else descriptionOff,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() }
                )
                Text(
                    text = if (enabled) "ON" else "OFF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun PermissionCard(packageName: String) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Permission needed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Loophole needs WRITE_SECURE_SETTINGS, a signature-level permission " +
                    "that can't be requested at runtime. Grant it once over adb, then reopen " +
                    "the app:",
                style = MaterialTheme.typography.bodyMedium
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "adb shell pm grant $packageName " +
                        "android.permission.WRITE_SECURE_SETTINGS",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun HowToCard() {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Faster access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TipRow(
                title = "Quick Settings tile",
                body = "Pull down the shade, tap Edit, and drag in the \"Dev Mode\" tile."
            )
            TipRow(
                title = "Home-screen widget",
                body = "Long-press the home screen, choose Widgets, and add Loophole. " +
                    "Tap it to toggle; tap the gear to open Developer Options."
            )
        }
    }
}

@Composable
private fun TipRow(title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
