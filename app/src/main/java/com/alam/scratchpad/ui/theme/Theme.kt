package com.alam.scratchpad.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xB2FFFFFF),
    //primary = Color(0xCCFFFFFF),
    primaryContainer = Color(0xFFF1F1F1),
    //primaryContainer = Color(0xB2F1F1F1),
    onPrimaryContainer = Color.Black,
    //onPrimaryContainer = Color(0xB2000000),
    background = Color.White,
    surface = Color.White,
    inversePrimary = Color.White

    //secondary = Color.Black,
    //background = Color.Black,
    //surface = Color.Black,
    //onPrimary = Color.Black,
    //onSecondary = Color.Black,
    //onBackground = Color.Black,
    //onSurface = Color.Black,
    //onSecondaryContainer = Color.Black,
    //onSurfaceVariant = Color.Black,
    //tertiary = Color.Black,
    //tertiaryContainer = Color.Black,

)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xB2FFFFFF),
    //primary = Color(0xCCFFFFFF),
    primaryContainer = Color(0xFFF1F1F1),
    //primaryContainer = Color(0xB2F1F1F1),
    onPrimaryContainer = Color.Black,
    //onPrimaryContainer = Color(0xB2000000),
    background = Color.White,
    surface = Color.White,
    inversePrimary = Color.White

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun ScratchPadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    //dynamicColor: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}