package com.alam.scratchpad

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class DrawingModel {
    var size by mutableStateOf(Size.Zero)
    var scale by mutableStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)

    var points = mutableStateListOf<Offset>() // current path points, possibly not smoothed yet
    var paths = mutableStateListOf<DrawPath>() // smoothed paths, completed

    var drawingMode by mutableStateOf(DrawingMode.Pen)
    var drawingBackground by mutableStateOf(AppSettings.DefaultBackgroundColor)
    var drawingColor by mutableStateOf(AppSettings.DefaultDrawingColor)
    var strokeWidth by mutableStateOf(AppSettings.DefaultStrokeWidth)
}