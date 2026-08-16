package com.shubhang.loophole.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubhang.loophole.R
import com.shubhang.loophole.settings.AddTileResult

/**
 * The app's snackbar: rounded to match the cards, tinted from the theme, and
 * led by a badged icon that reflects the outcome.
 *
 * This replaces a platform Toast deliberately — Toast.setView is deprecated
 * from API 30, so a system toast cannot be styled to match the app.
 */
@Composable
fun LoopholeSnackbar(message: String, result: AddTileResult?) {
    val isSuccess = result == AddTileResult.ADDED

    Snackbar(
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSuccess) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.16f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isSuccess) R.drawable.ic_check else R.drawable.ic_dev_mode_tile
                    ),
                    contentDescription = null,
                    tint = if (isSuccess) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
