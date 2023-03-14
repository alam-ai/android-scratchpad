package com.alam.scratchpad

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

class DrawingController(private val model: DrawingModel) {
    fun setPenMode() {
        model.drawingMode = DrawingMode.Pen
        model.strokeWidth = AppSettings.DefaultStrokeWidth
        model.drawingColor = AppSettings.DefaultDrawingColor
    }
    fun setEraseMode() {
        model.drawingMode = DrawingMode.Erase
        model.strokeWidth = AppSettings.DefaultEraserWidth / model.scale
        model.drawingColor = AppSettings.DefaultBackgroundColor
    }
    fun clearAll() {
        model.points.clear()
        model.paths.clear()
        model.offset = Offset.Zero
        setPenMode()
    }
    fun updateScale(delta: Float) {
        model.scale *= delta
        if (AppSettings.LimitScaling) {
            model.scale = model.scale.coerceIn(AppSettings.MinScaling, AppSettings.MaxScaling)
        }
    }
    fun updateOffset(delta: Offset) {
        model.offset += delta / model.scale
    }
    fun onOrientationChanged() {
        model.offset = Offset(
            model.fullSize.height / 2 + (model.offset.x - model.fullSize.width / 2),
            model.fullSize.width / 2 + (model.offset.y - model.fullSize.height / 2)
        )
    }
    fun addPoint(point: Offset) {
        model.points.add(point)
    }
    fun clearPoints() {
        model.points.clear()
    }
    fun getPointsPath(): Path {
        val points =
            if (model.drawingMode == DrawingMode.Pen)
                Utilities.chaikinSmoothing(model.points, AppSettings.SmoothingIterations)
            else model.points

        val path = Path()
        for ((i, point) in points.withIndex()) {
            if (i == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }
        return path
    }
    fun addPointsToPaths() {
        model.paths += DrawPath(
            path = getPointsPath(),
            color = model.drawingColor,
            strokeWidth = model.strokeWidth
        )
        clearPoints()
    }
    fun getMappedOffset(point: Offset): Offset {
        return Offset(
            point.x / model.scale - model.offset.x + model.size.width / 2 - model.size.width / 2 / model.scale,
            point.y / model.scale - model.offset.y + model.size.height / 2 - model.size.height / 2 / model.scale
        )
    }
    fun setFullSize(size: Size) {
        model.fullSize = size
    }
    fun setSize(size: Size) {
        model.size = size
    }
    fun getSize(): Size {
        return model.size
    }
    fun getDrawPaths(): List<DrawPath> {
        return model.paths
    }
    fun getOffset(): Offset {
        return model.offset
    }
    fun getScale(): Float {
        return model.scale
    }
    fun getDrawingBackground(): Color {
        return model.drawingBackground
    }
    fun getDrawingColor(): Color {
        return model.drawingColor
    }
    fun getStrokeWidth(): Float {
        return model.strokeWidth
    }
}