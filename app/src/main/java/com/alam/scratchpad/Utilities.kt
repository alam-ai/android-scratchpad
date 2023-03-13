package com.alam.scratchpad

import androidx.compose.ui.geometry.Offset

class Utilities {
    companion object {
        // Implementation of Chaikin's Algorithm for corner smoothing
        // adapted from: https://observablehq.com/@pamacha/chaikins-algorithm
        fun chaikinSmoothing(points: List<Offset>, iterations: Int, smoothFirstPoint: Boolean = false): List<Offset> {
            if (iterations == 0) {
                return points
            }

            if (points.size < 2) {
                return points
            }

            val newPoints = mutableListOf<Offset>()

            for ((i, point) in points.withIndex()) {
                if ((!smoothFirstPoint && i == 0) || i == points.size - 1) {
                    newPoints += point
                } else {
                    newPoints += Offset(
                        0.75f * point.x + 0.25f * points[i + 1].x,
                        0.75f * point.y + 0.25f * points[i + 1].y
                    )
                    newPoints += Offset(
                        0.25f * point.x + 0.75f * points[i + 1].x,
                        0.25f * point.y + 0.75f * points[i + 1].y
                    )
                }
            }

            return if (iterations == 1) newPoints else chaikinSmoothing(newPoints, iterations - 1)
        }
    }
}