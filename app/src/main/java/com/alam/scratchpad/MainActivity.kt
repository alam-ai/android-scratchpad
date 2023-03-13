package com.alam.scratchpad

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alam.scratchpad.ui.theme.ScratchPadTheme

class MainActivity : ComponentActivity() {
    private val drawingController = DrawingController(DrawingModel())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScratchPadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(drawingController)
                }
            }
        }

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawingController.onOrientationChanged()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    drawingController: DrawingController
) {
    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        drawingController.setPenMode()
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.pencil),
                        contentDescription = "Pen",
                        modifier = Modifier.size(26.dp)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        drawingController.setEraseMode()
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.eraser),
                        contentDescription = "Eraser",
                        modifier = Modifier.size(26.dp)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        drawingController.clearAll()
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.trash),
                        contentDescription = "Reset",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        content = {
            ScratchPadCanvas(
                drawingController = drawingController
            )
        }
    )

    if (AppSettings.BackButtonDisabled) {
        val context = LocalContext.current
        BackHandler {
            val toast = Toast.makeText(context, "Back is disabled", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScratchPadCanvas(
    drawingController: DrawingController
) {
    val transformState = rememberTransformableState { scalingDelta, offsetDelta, _ ->
        drawingController.updateScale(scalingDelta)
        drawingController.updateOffset(offsetDelta)
    }

    val size = drawingController.getSize()
    val scale = drawingController.getScale()
    val offset = drawingController.getOffset()
    val drawingBackground = drawingController.getDrawingBackground()
    val drawingColor = drawingController.getDrawingColor()
    val strokeWidth = drawingController.getStrokeWidth()

    Canvas(
        modifier = Modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .background(drawingBackground)
            .onGloballyPositioned {
                drawingController.setSize(it.size.toSize())
            }
            .pointerInteropFilter {
                val mappedOffset = drawingController.getMappedOffset(Offset(it.x, it.y))

                if (it.pointerCount == 1) {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            drawingController.addPoint(mappedOffset)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            drawingController.addPoint(mappedOffset)
                        }
                        MotionEvent.ACTION_UP -> {
                            drawingController.addPoint(mappedOffset)
                            drawingController.addPointsToPaths()
                        }
                        else -> {
                            // cancel likely from swipe from edge (back)
                            drawingController.clearPoints()
                        }
                    }
                } else {
                    // cancel from panning / zooming with 2 fingers
                    drawingController.clearPoints()
                }
                true
            }
            .transformable(state = transformState)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
                transformOrigin = TransformOrigin(
                    0.5f - offset.x / size.width,
                    0.5f - offset.y / size.height
                ),
            )
    ) {
        fun drawPath(path: DrawPath) {
            drawPath(
                color = path.color,
                path = path.path,
                style = Stroke(
                    width = path.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
            )
        }

        // draw existing paths
        for (path in drawingController.getDrawPaths()) {
            drawPath(path)
        }

        // draw the current path from the points still being smoothed and added to
        val pointsPath = drawingController.getPointsPath()
        drawPath(
            DrawPath(
                path = pointsPath,
                color = drawingColor,
                strokeWidth = strokeWidth
            )
        )
    }
}