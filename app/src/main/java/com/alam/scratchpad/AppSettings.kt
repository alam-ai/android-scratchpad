package com.alam.scratchpad

import androidx.compose.ui.graphics.Color

class AppSettings {
    companion object {
        val DefaultBackgroundColor = Color.White
        val DefaultDrawingColor = Color.Black

        val AvailableDrawingColors = listOf(
            Color.Black,
            Color(0xFF21409A), //Color.Blue,
            //Color(0xFF039C4B), //Color.Green
            Color(0xFFF44546), //Color.Red,
        )

        val AvailableStrokeWidths = listOf(
            16f,
            48f,
        )

        val DefaultStrokeWidth = AvailableStrokeWidths[0]

        val EraserStrokeWidthMultiplier = 3f

        val LimitScaling = true
        val MinScaling = 0.2f
        val MaxScaling = 2.0f

        val SmoothingIterations = 2
    }
}