package com.project.lol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.lol.R
import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Download
import compose.icons.tablericons.Music
import compose.icons.tablericons.PlayerPlay

@Composable
fun BatteryOptimizationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TablerIcons.PlayerPlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.battery_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.battery_dialog_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                BatteryBenefitRow(
                    icon = TablerIcons.Music,
                    label = stringResource(R.string.battery_dialog_benefit_playback)
                )
                BatteryBenefitRow(
                    icon = TablerIcons.Bell,
                    label = stringResource(R.string.battery_dialog_benefit_controls)
                )
                BatteryBenefitRow(
                    icon = TablerIcons.Download,
                    label = stringResource(R.string.battery_dialog_benefit_downloads)
                )
                BatteryBenefitRow(
                    icon = painterResource(R.drawable.ic_timer),
                    label = stringResource(R.string.battery_dialog_benefit_timer)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.battery_dialog_reassure),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.battery_dialog_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.battery_dialog_confirm),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.battery_dialog_dismiss),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun BatteryBenefitRow(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BatteryBenefitRow(icon: Painter, label: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}