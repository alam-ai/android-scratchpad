package com.alam.scratchpad

import androidx.compose.ui.graphics.Color

class AppSettings {
    companion object {
        val DefaultStrokeWidth = 16f
        val DefaultEraserWidth = 96f
        val DefaultBackgroundColor = Color.White
        val DefaultDrawingColor = Color.Black

        val BackButtonDisabled = true

        val LimitScaling = true
        val MinScaling = 0.2f
        val MaxScaling = 2.0f

        val SmoothingIterations = 3
    }
}