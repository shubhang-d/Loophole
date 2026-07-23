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
 * Prompts the system to add a Quick Settings tile via a one-tap dialog (API 33+).
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestAddTile(context: Context, serviceClass: Class<*>, labelRes: Int, iconRes: Int) {
    val statusBar = context.getSystemService(StatusBarManager::class.java) ?: return
    statusBar.requestAddTileService(
        ComponentName(context, serviceClass),
        context.getString(labelRes),
        Icon.createWithResource(context, iconRes),
        context.mainExecutor
    ) { /* result code — no action needed */ }
}

@Composable
fun LoopholeScreen() {
    val context = LocalContext.current

    var devEnabled by remember { mutableStateOf(DevMode.isEnabled(context)) }
    var usbEnabled by remember { mutableStateOf(UsbDebug.isEnabled(context)) }
    var wirelessEnabled by remember { mutableStateOf(WirelessDebug.isEnabled(context)) }
    val wirelessSupported = remember { WirelessDebug.isSupported() }
    var permissionDenied by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                devEnabled = DevMode.isEnabled(context)
                usbEnabled = UsbDebug.isEnabled(context)
                wirelessEnabled = WirelessDebug.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scope = rememberCoroutineScope()

    fun toggleDev(target: Boolean) {
        val ok = DevMode.setEnabled(context, target)
        permissionDenied = !ok
        devEnabled = DevMode.isEnabled(context)
        scope.launch { refreshLoopholeWidgets(context) }
    }

    fun toggleUsb(target: Boolean) {
        val ok = UsbDebug.setEnabled(context, target)
        permissionDenied = !ok
        usbEnabled = UsbDebug.isEnabled(context)
        scope.launch { refreshLoopholeWidgets(context) }
    }

    fun toggleWireless(target: Boolean) {
        val ok = WirelessDebug.setEnabled(context, target)
        permissionDenied = !ok
        wirelessEnabled = WirelessDebug.isEnabled(context)
        scope.launch { refreshLoopholeWidgets(context) }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header()

            HeroToggleCard(
                enabled = devEnabled,
                title = "Developer Options",
                descriptionOn = "Tap to turn off before opening a banking app.",
                descriptionOff = "Tap to turn on when you want to develop.",
                iconRes = R.drawable.ic_dev_mode_tile,
                onToggle = { toggleDev(!devEnabled) }
            )

            ToggleCard(
                title = "USB Debugging",
                description = "Enable ADB over USB connection.",
                enabled = usbEnabled,
                iconRes = R.drawable.ic_usb_tile,
                onToggle = { toggleUsb(!usbEnabled) }
            )

            ToggleCard(
                title = "Wireless Debugging",
                description = if (wirelessSupported) "Enable ADB over Wi-Fi network." else "Requires Android 11+ (API 30+).",
                enabled = wirelessEnabled,
                supported = wirelessSupported,
                iconRes = R.drawable.ic_wireless_tile,
                onToggle = { if (wirelessSupported) toggleWireless(!wirelessEnabled) }
            )

            FilledTonalButton(
                onClick = { DevMode.openDeveloperOptions(context) },
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Add Quick Settings Tiles",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = { requestAddTile(context, DevModeTileService::class.java, R.string.tile_label, R.drawable.ic_dev_mode_tile) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Add Dev Mode Tile")
                    }
                    OutlinedButton(
                        onClick = { requestAddTile(context, UsbDebugTileService::class.java, R.string.usb_tile_label, R.drawable.ic_usb_tile) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Add USB Debugging Tile")
                    }
                    if (wirelessSupported) {
                        OutlinedButton(
                            onClick = { requestAddTile(context, WirelessDebugTileService::class.java, R.string.wireless_tile_label, R.drawable.ic_wireless_tile) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Add Wireless Debugging Tile")
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
            text = "One-tap Developer & Debugging Toggles",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HeroToggleCard(
    enabled: Boolean,
    title: String,
    descriptionOn: String,
    descriptionOff: String,
    iconRes: Int,
    onToggle: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "heroContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "heroContent"
    )
    val iconBadgeColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "iconBadge"
    )

    ElevatedCard(
        onClick = onToggle,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconBadgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
                AnimatedContent(
                    targetState = enabled,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "status"
                ) { on ->
                    Text(
                        text = if (on) "ON" else "OFF",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (enabled) descriptionOn else descriptionOff,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ToggleCard(
    title: String,
    description: String,
    enabled: Boolean,
    supported: Boolean = true,
    iconRes: Int,
    onToggle: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = { if (supported) onToggle() },
                enabled = supported
            )
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
                title = "Quick Settings tiles",
                body = "Pull down the shade, tap Edit, and drag in the Dev Mode, USB Debugging, or Wireless Debugging tiles."
            )
            TipRow(
                title = "Home-screen widget",
                body = "Long-press the home screen, choose Widgets, and add Loophole. " +
                    "Tap DEV, USB, or WIFI to toggle instantly; tap the gear to open Developer Options."
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
