package com.shubhang.loophole.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubhang.loophole.R

@Composable
fun Header() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Loophole",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Developer & Debugging Toggles",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Unified Hero Toggle Container encapsulating:
 * 1. Master Developer Options toggle with live state.
 * 2. System Settings navigation shortcut.
 * 3. Integrated USB Debugging row.
 * 4. Integrated Wireless Debugging row with direct system settings deep-link.
 */
@Composable
fun HeroToggleContainer(
    devEnabled: Boolean,
    onToggleDev: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    usbEnabled: Boolean,
    onToggleUsb: () -> Unit,
    wirelessEnabled: Boolean,
    onToggleWireless: () -> Unit,
    isWirelessSupported: Boolean,
    onOpenWirelessDebugging: () -> Unit,
    canAddTile: Boolean,
    onAddUsbTile: () -> Unit,
    onAddWirelessTile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (devEnabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "heroContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (devEnabled) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "heroContent"
    )
    val iconBadgeColor by animateColorAsState(
        targetValue = if (devEnabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "iconBadge"
    )

    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Master Dev Mode Toggle Header
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
                        painter = painterResource(R.drawable.ic_dev_mode_tile),
                        contentDescription = null,
                        tint = if (devEnabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                Switch(
                    checked = devEnabled,
                    onCheckedChange = { onToggleDev() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDev() },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Developer Options",
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
                AnimatedContent(
                    targetState = devEnabled,
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
                    text = if (devEnabled) {
                        "Tap to turn off before opening banking/secured apps."
                    } else {
                        "Tap to turn on when you want to develop."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            FilledTonalButton(
                onClick = onOpenDeveloperOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_gear),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Open Developer Options",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                color = contentColor.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Nested Secondary Toggles
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // USB Debugging Row
                IntegratedToggleRow(
                    title = "USB Debugging",
                    subtitle = if (usbEnabled) "Enabled" else "Disabled",
                    iconRes = R.drawable.ic_usb_tile,
                    enabled = usbEnabled,
                    onToggle = onToggleUsb,
                    parentContentColor = contentColor,
                    isParentEnabled = devEnabled,
                    canAddTile = canAddTile,
                    onAddTile = onAddUsbTile
                )

                // Wireless Debugging Row with Direct Pairing/Settings Link
                IntegratedToggleRow(
                    title = "Wireless Debugging",
                    subtitle = if (!isWirelessSupported) {
                        stringResource(R.string.requires_android_11)
                    } else if (wirelessEnabled) {
                        "Enabled • Tap arrow to pair device"
                    } else {
                        "Disabled • Tap arrow to configure"
                    },
                    iconRes = R.drawable.ic_wireless_tile,
                    enabled = wirelessEnabled,
                    onToggle = onToggleWireless,
                    isParentEnabled = devEnabled,
                    isSupported = isWirelessSupported,
                    unsupportedBadge = if (!isWirelessSupported) stringResource(R.string.requires_android_11) else null,
                    parentContentColor = contentColor,
                    onOpenSettings = onOpenWirelessDebugging,
                    canAddTile = canAddTile,
                    onAddTile = onAddWirelessTile
                )
            }
        }
    }
}

@Composable
private fun IntegratedToggleRow(
    title: String,
    subtitle: String,
    iconRes: Int,
    enabled: Boolean,
    onToggle: () -> Unit,
    parentContentColor: Color,
    modifier: Modifier = Modifier,
    isParentEnabled: Boolean = true,
    isSupported: Boolean = true,
    unsupportedBadge: String? = null,
    onOpenSettings: (() -> Unit)? = null,
    canAddTile: Boolean = false,
    onAddTile: (() -> Unit)? = null,
) {
    val isInteractive = isParentEnabled && isSupported
    val effectiveContentColor = if (isParentEnabled) parentContentColor else parentContentColor.copy(alpha = 0.38f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = effectiveContentColor.copy(alpha = 0.08f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (onOpenSettings != null && isInteractive) {
                            Modifier.clickable { onOpenSettings() }
                        } else {
                            Modifier.clickable(enabled = isInteractive) { onToggle() }
                        }
                    )
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled && isInteractive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                effectiveContentColor.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (enabled && isInteractive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            effectiveContentColor.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = effectiveContentColor
                        )
                        if (onOpenSettings != null && isInteractive) {
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = "Open $title Settings",
                                tint = effectiveContentColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (!isSupported && unsupportedBadge != null) {
                        Text(
                            text = unsupportedBadge,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (!isParentEnabled) {
                        Text(
                            text = "Requires Developer Options",
                            style = MaterialTheme.typography.bodySmall,
                            color = effectiveContentColor.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = effectiveContentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (canAddTile && onAddTile != null && isInteractive) {
                    IconButton(
                        onClick = onAddTile,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "Add Quick Settings Tile",
                            tint = effectiveContentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Switch(
                    checked = enabled && isInteractive,
                    enabled = isInteractive,
                    onCheckedChange = { if (isInteractive) onToggle() }
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
                title = "Quick Settings tiles & dialogs",
                body = "Pull down the shade to toggle Developer Options, USB, or Wireless Debugging directly."
            )
            TipRow(
                title = "Adaptive home-screen widget",
                body = "Add the Loophole widget to your home screen. Resize horizontally for quick one-tap toggle controls."
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
