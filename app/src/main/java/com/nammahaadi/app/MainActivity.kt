package com.nammahaadi.app

import android.os.Bundle
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.nammahaadi.app.navigation.AppNavHost
import com.nammahaadi.app.ui.theme.NammaHaadiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Custom exit animation for splash screen
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scaleX = android.view.animation.ScaleAnimation(
                1f, 0f, 1f, 0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 500L
                interpolator = AnticipateInterpolator()
                fillAfter = true
            }

            splashScreenView.iconView.startAnimation(scaleX)
            
            // Fade out the whole splash screen
            splashScreenView.view.animate()
                .alpha(0f)
                .setDuration(500L)
                .setInterpolator(AnticipateInterpolator())
                .withEndAction { splashScreenView.remove() }
                .start()
        }

        enableEdgeToEdge()
        setContent {
            NammaHaadiTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppNavHost(windowSizeClass = windowSizeClass)
            }
        }
    }
}
