package com.nammahaadi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Muted,
    outline = Border,
    background = Surface,
    onBackground = OnSurface,
    error = Danger,
)

@Composable
fun NammaHaadiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
            headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
            titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
            bodyLarge = TextStyle(fontSize = 15.sp),
            bodyMedium = TextStyle(fontSize = 13.sp),
            bodySmall = TextStyle(fontSize = 11.sp),
            labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
            labelSmall = TextStyle(fontSize = 10.sp),
        ),
        content = content
    )
}
