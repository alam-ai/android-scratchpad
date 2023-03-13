package com.alam.scratchpad

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class DrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
)