package com.alam.scratchpad

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

class DrawingController(private val model: DrawingModel) {
    fun setPenMode() {
        model.drawingMode = DrawingMode.Pen
        //model.strokeWidth = AppSettings.DefaultStrokeWidth
        //model.drawingColor = AppSettings.DefaultDrawingColor
        model.drawingColor = AppSettings.AvailableDrawingColors[model.cycleDrawingColorCurrentIndex]
        model.strokeWidth = AppSettings.AvailableStrokeWidths[model.cycleStrokeWidthCurrentIndex]
    }
    fun setEraseMode() {
        model.drawingMode = DrawingMode.Erase
        model.strokeWidth = AppSettings.AvailableStrokeWidths[model.cycleStrokeWidthCurrentIndex]
        model.drawingColor = AppSettings.DefaultBackgroundColor
    }
    fun clearAll() {
        model.points.clear()
        model.paths.clear()
        model.offset = Offset.Zero
        setPenMode()
    }
    fun cycleDrawingColor() {
        val nextIndex = (model.cycleDrawingColorCurrentIndex + 1) % AppSettings.AvailableDrawingColors.size
        model.cycleDrawingColorCurrentIndex = nextIndex
        model.drawingColor = AppSettings.AvailableDrawingColors[nextIndex]
    }
    fun cycleStrokeWidth() {
        val nextIndex = (model.cycleStrokeWidthCurrentIndex + 1) % AppSettings.AvailableStrokeWidths.size
        model.cycleStrokeWidthCurrentIndex = nextIndex
        model.strokeWidth = AppSettings.AvailableStrokeWidths[nextIndex]
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
//        val points =
//            if (model.drawingMode == DrawingMode.Pen)
//                Utilities.chaikinSmoothing(model.points, AppSettings.SmoothingIterations)
//            else model.points
        val points = Utilities.chaikinSmoothing(model.points, AppSettings.SmoothingIterations)

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
            strokeWidth = getStrokeWidth()
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
    fun getDrawingMode(): DrawingMode {
        return model.drawingMode
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
        return if (model.drawingMode != DrawingMode.Erase) {
            model.strokeWidth
        } else {
            model.strokeWidth * AppSettings.EraserStrokeWidthMultiplier / model.scale
        }
    }
    fun getStrokeWidthIndex(): Int {
        return model.cycleStrokeWidthCurrentIndex
    }
    fun getDrawingColorIndex(): Int {
        return model.cycleDrawingColorCurrentIndex
    }
}