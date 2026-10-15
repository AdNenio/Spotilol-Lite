package com.project.lol.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.project.lol.BuildConfig
import com.project.lol.R
import com.project.lol.ui.theme.SpotifyTheme
import com.project.lol.util.BuildInfo
import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Bluetooth
import com.project.lol.util.AppLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MonochromeAccent = Color(0xFFE0E0E0)
private const val SPLASH_MIN_MS = 4000L

class SplashActivity : ComponentActivity() {

    private fun fadeTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        super.onCreate(savedInstanceState)

        requestedOrientation = if (
            getSharedPreferences("spotilol_prefs", MODE_PRIVATE)
                .getBoolean("LandscapeMode", false)
        ) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContent {
            val prefs = remember { getSharedPreferences("spotilol_prefs", MODE_PRIVATE) }
            var intro by remember { mutableStateOf(true) }
            var onboarding by remember { mutableStateOf(false) }
            var checkDone by remember { mutableStateOf(false) }
            var checking by remember { mutableStateOf(false) }
            var preparing by remember { mutableStateOf(false) }
            var contentAlpha by remember { mutableFloatStateOf(1f) }
            val fastStartup = remember { prefs.getBoolean("FastStartup", false) }
            val onboardingAppear = remember { Animatable(0f) }
            var onboardingLeaving by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            val finishOnboarding: () -> Unit = {
                if (!onboardingLeaving) {
                    onboardingLeaving = true
                    prefs.edit().putBoolean("OnboardingDone", true).apply()
                    scope.launch {
                        onboardingAppear.animateTo(0f, tween(200, easing = LinearEasing))
                        onboarding = false
                        preparing = true
                    }
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { finishOnboarding() }

            LaunchedEffect(onboarding) {
                if (onboarding) {
                    onboardingLeaving = false
                    onboardingAppear.snapTo(0f)
                    onboardingAppear.animateTo(1f, tween(340, easing = LinearOutSlowInEasing))
                }
            }

            LaunchedEffect(Unit) {
                intro = false
                if (prefs.getBoolean("OnboardingDone", false)) {
                    preparing = true
                } else {
                    onboarding = true
                }
            }

            LaunchedEffect(preparing) {
                if (!preparing) return@LaunchedEffect
                checking = true
                checkDone = true
            }

            LaunchedEffect(checkDone) {
                if (!checkDone) return@LaunchedEffect
                if (!fastStartup) delay(SPLASH_MIN_MS)
                scope.launch {
                    animate(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = tween(150, easing = LinearEasing),
                        block = { value, _ -> contentAlpha = value }
                    )
                }
                val linkIntent = intent?.takeIf { it.action == Intent.ACTION_VIEW && it.data != null }
                startActivity(
                    if (linkIntent != null) {
                        Intent(linkIntent).setClass(this@SplashActivity, MainActivity::class.java)
                    } else {
                        Intent(this@SplashActivity, MainActivity::class.java)
                    }
                )
                fadeTransition()
                finish()
            }

            SpotifyTheme {
                Box(modifier = Modifier.graphicsLayer { alpha = contentAlpha }) {
                    when {
                        intro || checking -> LoadingScreen()
                        onboarding -> OnboardingScreen(
                            modifier = Modifier.graphicsLayer {
                                alpha = onboardingAppear.value
                                translationY = (1f - onboardingAppear.value) * 28.dp.toPx()
                            },
                            onAccept = {
                                val required = requiredPermissions()
                                if (required.isEmpty()) {
                                    finishOnboarding()
                                } else {
                                    permissionLauncher.launch(required.toTypedArray())
                                }
                            }
                        )
                        else -> LoadingScreen()
                    }
                }
            }
        }
    }

    private fun requiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissions += Manifest.permission.BLUETOOTH_CONNECT
        }
        return permissions
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LoadingIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp),
            color = Color.White
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.splash_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.splash_version_label, BuildConfig.VERSION_NAME, BuildInfo.id),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        OnboardingPhase(onAccept = onAccept)
    }
}

@Composable
private fun OnboardingPhase(onAccept: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.splash_onboarding_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.splash_onboarding_permissions_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.45f),
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(22.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OnboardingItem(
                icon = TablerIcons.Bell,
                title = stringResource(R.string.splash_onboarding_notifications_title),
                description = stringResource(R.string.splash_onboarding_notifications_desc)
            )
            Spacer(Modifier.height(10.dp))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            OnboardingItem(
                icon = TablerIcons.Bluetooth,
                title = stringResource(R.string.splash_onboarding_bluetooth_title),
                description = stringResource(R.string.splash_onboarding_bluetooth_desc)
            )
        }
        Spacer(Modifier.height(26.dp))
        OnboardingAction(
            label = stringResource(R.string.splash_onboarding_accept),
            onClick = onAccept
        )
    }
}


@Composable
private fun OnboardingItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun OnboardingAction(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(modifier = Modifier.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

