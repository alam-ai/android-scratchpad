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
import androidx.compose.ui.geometry.Size
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
import androidx.lifecycle.ViewModel
import com.alam.scratchpad.ui.theme.ScratchPadTheme

class MainActivity : ComponentActivity() {
    private var viewModel = DrawingViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScratchPadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(viewModel)
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

        viewModel.offset = Offset(
            viewModel.size.height / 2 + (viewModel.offset.x - viewModel.size.width / 2),
            viewModel.size.width / 2 + (viewModel.offset.y - viewModel.size.height / 2)
        )
    }

}

enum class DrawingMode {
    Pen,
    Erase
}

data class PointState(
    val x: Float,
    val y: Float,
    val stillDrawing: Boolean,
    val cancelEvent: Boolean = false,
    var partOfCompletePath: Boolean = false,
)

data class PathState(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
)

class DrawingViewModel: ViewModel() {
    var size by mutableStateOf(Size.Zero)
    var scale by mutableStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)

    var points = mutableStateListOf<PointState>()
    var paths = mutableStateListOf<PathState>()

    var drawingMode by mutableStateOf(DrawingMode.Pen)
    var drawingBackground by mutableStateOf(Color.White)
    var drawingColor by mutableStateOf(Color.Black)
    var strokeWidth by mutableStateOf(12f)
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    viewModel: DrawingViewModel
) {
    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,

        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.drawingMode = DrawingMode.Pen
                        viewModel.strokeWidth = 12f
                        viewModel.drawingColor = Color.Black
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
                        viewModel.drawingMode = DrawingMode.Erase
                        viewModel.strokeWidth = 96f / viewModel.scale
                        viewModel.drawingColor = Color.White
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.eraser),
                        "Eraser",
                        modifier = Modifier.size(26.dp)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        viewModel.points.clear()
                        viewModel.paths.clear()
                        viewModel.offset = Offset.Zero
                        viewModel.drawingMode = DrawingMode.Pen
                        viewModel.strokeWidth = 12f
                        viewModel.drawingColor = Color.Black
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.trash),
                        "Reset",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },

        content = {
            ScratchPadCanvas(
                viewModel = viewModel
            )
        },
        bottomBar = {

        }
    )

    val context = LocalContext.current
    BackHandler {
        val toast = Toast.makeText(context, "Back is disabled", Toast.LENGTH_SHORT)
        toast.show()
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScratchPadCanvas(
    viewModel: DrawingViewModel
) {
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        viewModel.scale *= zoomChange
        viewModel.scale = viewModel.scale.coerceIn(0.2f, 2f)
        viewModel.offset += offsetChange / viewModel.scale
    }

    Canvas(
        modifier = Modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .background(viewModel.drawingBackground)
            .onGloballyPositioned {
                viewModel.size = it.size.toSize()
            }
            .pointerInteropFilter {
                val mappedOffset = Offset(
                    it.x / viewModel.scale - viewModel.offset.x + viewModel.size.width / 2 - viewModel.size.width / 2 / viewModel.scale,
                    it.y / viewModel.scale - viewModel.offset.y + viewModel.size.height / 2 - viewModel.size.height / 2 / viewModel.scale
                )

                viewModel.points += when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        PointState(mappedOffset.x, mappedOffset.y, true)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        PointState(mappedOffset.x, mappedOffset.y, true)
                    }
                    MotionEvent.ACTION_UP -> {
                        PointState(mappedOffset.x, mappedOffset.y, false)
                    }
                    else -> {
                        PointState(
                            mappedOffset.x,
                            mappedOffset.y,
                            stillDrawing = false,
                            cancelEvent = true
                        )
                    }
                }
                true
            }
            .transformable(state = state)
            .graphicsLayer(
                scaleX = viewModel.scale,
                scaleY = viewModel.scale,
                translationX = viewModel.offset.x,
                translationY = viewModel.offset.y,
                transformOrigin = TransformOrigin(
                    0.5f - viewModel.offset.x / viewModel.size.width,
                    0.5f - viewModel.offset.y / viewModel.size.height
                ),
            )
    ) {
        //drawCircle(color = Color.Red, radius = 25f, center = Offset(0f, 0f))
        //drawCircle(color = Color.Yellow, radius = 50f, center = Offset(size.width/2 - offset.x, size.height/2 - offset.y))
        //drawCircle(color = Color.Blue, radius = 10f, center = startingOffset)

        fun drawPath(path: PathState) {
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

        fun chaikinSmoothing(points: List<Offset>, iterations: Int): List<Offset> {
            if (iterations == 0) {
                return points
            }

            if (points.size < 2) {
                return points
            }

            val newPoints = mutableListOf<Offset>()

            for ((i, point) in points.withIndex()) {
                if (i == 0 || i == points.size - 1) {
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

        for (path in viewModel.paths) {
            drawPath(path)
        }

        var currentPath: PathState? = null

        var incompleteStartIndex = 0

        for ((i, point) in viewModel.points.withIndex()) {
            if (!point.partOfCompletePath) {
                if (!point.cancelEvent) {
                    if (currentPath == null) {
                        currentPath = PathState(
                            path = Path(),
                            color = viewModel.drawingColor,
                            strokeWidth = viewModel.strokeWidth
                        )
                        currentPath.path.moveTo(point.x, point.y)
                    } else {
                        currentPath.path.lineTo(point.x, point.y)
                    }
                    drawPath(currentPath)
                }

                if (!point.stillDrawing) {
                    var removeLast = false
                    if (currentPath != null) {
                        val usedPoints = mutableListOf<Offset>()
                        for (j in incompleteStartIndex..i) {
                            if (!viewModel.points[j].cancelEvent) {
                                usedPoints += Offset(viewModel.points[j].x, viewModel.points[j].y)
                            }
                        }

                        if (viewModel.drawingMode == DrawingMode.Pen) {
                            val smoothedPoints = chaikinSmoothing(usedPoints, 3)
                            val smoothedPath = Path()
                            for ((j, smoothedPoint) in smoothedPoints.withIndex()) {
                                if (j == 0) {
                                    smoothedPath.moveTo(smoothedPoint.x, smoothedPoint.y)
                                } else {
                                    smoothedPath.lineTo(smoothedPoint.x, smoothedPoint.y)
                                }
                            }
                            viewModel.paths += PathState(
                                path = smoothedPath,
                                color = viewModel.drawingColor,
                                strokeWidth = viewModel.strokeWidth
                            )
                        } else {
                            viewModel.paths += currentPath
                        }

                        if (point.cancelEvent) {
                            removeLast = true
                        }
                    }
                    currentPath = null

                    for (j in incompleteStartIndex..i) {
                        viewModel.points[j].partOfCompletePath = true
                    }

                    if (removeLast) {
                        viewModel.paths.removeLast()
                    }

                    incompleteStartIndex = i + 1
                }
            } else {
                incompleteStartIndex++
            }
        }

        if (incompleteStartIndex - 1 >= 0) {
            viewModel.points.removeRange(0, incompleteStartIndex - 1)
        }
    }
}