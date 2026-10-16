package com.project.lol.ui.components

import android.content.SharedPreferences
import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.project.lol.R
import com.project.lol.ui.screens.SettingsContent
import com.project.lol.util.BuildInfo
import compose.icons.TablerIcons
import compose.icons.tablericons.Settings
import compose.icons.tablericons.X

private const val ScrimOpacity = 0.32f

@Composable
fun SettingsDialog(
    visible: Boolean,
    onClose: () -> Unit,
    prefs: SharedPreferences,
    amoledThemeState: Boolean,
    onAmoledThemeChange: (Boolean) -> Unit,
    hideTopBar: Boolean,
    onHideTopBarChange: (Boolean) -> Unit,
    landscapeMode: Boolean,
    onLandscapeModeChange: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onLoadProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onClearCache: () -> Unit,
    onClearData: () -> Unit,
    onClearThirdParty: () -> Unit,
    onDebugToggle: (Boolean) -> Unit = {},
    blockServiceWorker: Boolean,
    onBlockServiceWorkerChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (visible) {
            Dialog(
                onDismissRequest = onClose,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                val dialogWindow = rememberCurrentDialogWindow()
                SideEffect { dialogWindow?.setDimAmount(0f) }

                val enterState = remember { MutableTransitionState(false).apply { targetState = true } }

                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(animationSpec = tween(180)),
                    exit = fadeOut(animationSpec = tween(120))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = ScrimOpacity))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onClose() }
                            .systemBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(animationSpec = tween(220)) + scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(220)
                            ),
                            exit = fadeOut(animationSpec = tween(120))
                        ) {
                            Surface(
                                modifier = Modifier
                                    .widthIn(max = 440.dp)
                                    .fillMaxWidth(0.9f)
                                    .heightIn(max = 640.dp)
                                    .fillMaxHeight(0.84f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { },
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SettingsDialogHeader(onClose = onClose)
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                    )
                                    SettingsContent(
                                        modifier = Modifier.weight(1f),
                                        prefs = prefs,
                                        amoledThemeState = amoledThemeState,
                                        onAmoledThemeChange = onAmoledThemeChange,
                                        hideTopBar = hideTopBar,
                                        onHideTopBarChange = onHideTopBarChange,
                                        landscapeMode = landscapeMode,
                                        onLandscapeModeChange = onLandscapeModeChange,
                                        keepScreenOn = keepScreenOn,
                                        onKeepScreenOnChange = onKeepScreenOnChange,
                                        onSaveProfile = onSaveProfile,
                                        onLoadProfile = onLoadProfile,
                                        onDeleteProfile = onDeleteProfile,
                                        onClearCache = onClearCache,
                                        onClearData = onClearData,
                                        onClearThirdParty = onClearThirdParty,
                                        onDebugToggle = onDebugToggle,
                                        blockServiceWorker = blockServiceWorker,
                                        onBlockServiceWorkerChange = onBlockServiceWorkerChange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCurrentDialogWindow(): Window? {
    val view = LocalView.current
    return remember(view) { view.findDialogWindow() }
}

private fun View.findDialogWindow(): Window? {
    var parent: ViewParent? = this.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) return parent.window
        parent = (parent as? View)?.parent
    }
    return null
}

@Composable
private fun SettingsDialogHeader(onClose: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 6.dp, top = 10.dp, bottom = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TablerIcons.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_dialog_version, versionName, BuildInfo.id),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = TablerIcons.X,
                    contentDescription = stringResource(R.string.settings_dialog_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
