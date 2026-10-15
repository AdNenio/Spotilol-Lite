package com.project.lol.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.lol.R
import compose.icons.TablerIcons
import compose.icons.tablericons.Volume
import compose.icons.tablericons.Volume2
import compose.icons.tablericons.Volume3
import kotlin.math.roundToInt

@Composable
fun VolumeDialog(
    fraction: Float,
    onChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                stringResource(R.string.main_volume_title),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = volumeIcon(fraction),
                    contentDescription = null,
                    tint = if (fraction <= 0f) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (fraction <= 0f) stringResource(R.string.main_volume_muted)
                    else stringResource(R.string.main_volume_percent, (fraction * 100).roundToInt()),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(14.dp))
                val sliderState = rememberSliderState(
                    value = fraction.coerceIn(0f, 1f),
                    trackRange = 0f..1f
                )

                LaunchedEffect(fraction) {
                    sliderState.value = fraction.coerceIn(0f, 1f)
                }

                Slider(
                    state = sliderState,
                    onValueChange = onChange,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VolumePresetButton(
                        label = stringResource(R.string.volume_preset_mute),
                        modifier = Modifier.weight(1f)
                    ) { onChange(0f) }
                    VolumePresetButton(
                        label = stringResource(R.string.volume_preset_half),
                        modifier = Modifier.weight(1f)
                    ) { onChange(0.5f) }
                    VolumePresetButton(
                        label = stringResource(R.string.volume_preset_max),
                        modifier = Modifier.weight(1f)
                    ) { onChange(1f) }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.main_close))
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun VolumePresetButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun volumeIcon(fraction: Float): ImageVector = when {
    fraction <= 0f -> TablerIcons.Volume
    fraction < 0.75f -> TablerIcons.Volume2
    else -> TablerIcons.Volume3
}