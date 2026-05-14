package com.nammahaadi.app.ui.utils

import androidx.compose.ui.graphics.Color

fun safeParseColor(colorHex: String, defaultColor: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        defaultColor
    }
}
