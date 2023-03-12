package com.example.scratchpad

import android.annotation.SuppressLint
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
import com.example.scratchpad.ui.theme.ScratchPadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScratchPadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screen()
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
}

data class PointState (
    val x: Float,
    val y: Float,
    val stillDrawing: Boolean,
    val cancelEvent: Boolean = false,
    var partOfCompletePath: Boolean = false,
)

data class PathState (
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen() {
    val (fabResetOnClick, setFabResetOnClick) = remember { mutableStateOf<(() -> Unit)?>(null) }

    var drawingMode by remember { mutableStateOf<DrawingMode>(DrawingMode.Pen) }
    var drawingBackground by remember { mutableStateOf<Color>(Color.White) }
    var drawingColor by remember { mutableStateOf<Color>(Color.Black) }
    var strokeWidth by remember { mutableStateOf<Float>(12f) }

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,

        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        drawingMode = DrawingMode.Pen
                        strokeWidth = 12f
                        drawingColor = Color.Black
                    },
                ) {
                    Icon(painter = painterResource(R.drawable.pencil), contentDescription = "Pen", modifier = Modifier.size(26.dp))
                }
                FloatingActionButton(
                    onClick = {
                        drawingMode = DrawingMode.Erase
                        strokeWidth = 96f
                        drawingColor = Color.White
                    },
                ) {
                    Icon(painterResource(R.drawable.eraser), "Eraser", modifier = Modifier.size(26.dp))
                }
                FloatingActionButton(
                    onClick = {
                        fabResetOnClick?.invoke()
                        drawingMode = DrawingMode.Pen
                        strokeWidth = 12f
                        drawingColor = Color.Black
                    },
                ) {
                    Icon(painterResource(R.drawable.trash), "Reset", modifier = Modifier.size(26.dp))
                }
            }
        },

        content = {
            ScratchPadCanvas(
                drawingMode = drawingMode,
                drawingBackground = drawingBackground,
                drawingColor = drawingColor,
                strokeWidth = strokeWidth,
                setFabResetOnClick = setFabResetOnClick,
            )
        },
        bottomBar = {

        }
    )

    val context = LocalContext.current
    BackHandler() {
        val toast = Toast.makeText(context, "Back is disabled", Toast.LENGTH_SHORT)
        toast.show()
    }
}

enum class DrawingMode {
    Pen,
    Erase
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScratchPadCanvas(
    drawingMode: DrawingMode,
    drawingBackground: Color,
    drawingColor: Color,
    strokeWidth: Float,
    setFabResetOnClick: (() -> Unit) -> Unit
) {
    var size by remember { mutableStateOf(Size.Zero) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        scale = scale.coerceIn(0.2f, 2f)
        offset += offsetChange / scale
    }

    var completedPaths = remember { mutableStateListOf<PathState>() }
    var points = remember { mutableStateListOf<PointState>() }

    val useStrokeWidth = if (drawingMode == DrawingMode.Pen) strokeWidth else strokeWidth / scale

    LaunchedEffect(Unit) {
        setFabResetOnClick {
            completedPaths.clear()
            points.clear()
            scale = 1f
            offset = Offset.Zero
        }
    }
    Canvas(
        modifier = Modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .background(drawingBackground)
            .onGloballyPositioned {
                size = it.size.toSize()
            }
            .pointerInteropFilter {
                val mappedOffset = Offset(
                    it.x / scale - offset.x + size.width / 2 - size.width / 2 / scale,
                    it.y / scale - offset.y + size.height / 2 - size.height / 2 / scale
                )

                points += when (it.action) {
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
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
                transformOrigin = TransformOrigin(
                    0.5f - offset.x / size.width,
                    0.5f - offset.y / size.height
                )
            )
    ) {
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

            var newPoints = listOf<Offset>()

            for ((i, point) in points.withIndex()) {
                if (i == 0 || i == points.size - 1) {
                    newPoints += point
                }
                else {
                    newPoints += Offset(0.75f * point.x + 0.25f * points[i + 1].x, 0.75f * point.y + 0.25f * points[i + 1].y)
                    newPoints += Offset(0.25f * point.x + 0.75f * points[i + 1].x, 0.25f * point.y + 0.75f * points[i + 1].y)
                }
            }

            if (iterations == 1) {
                return newPoints
            }
            else {
                return chaikinSmoothing(newPoints, iterations - 1)
            }
        }

        for (path in completedPaths) {
            drawPath(path)
        }

        var currentPath: PathState? = null

        var incompleteStartIndex: Int = 0

        for ((i, point) in points.withIndex()) {
            if (!point.partOfCompletePath) {
                if (!point.cancelEvent) {
                    if (currentPath == null) {
                        currentPath = PathState(path = Path(), color = drawingColor, strokeWidth = useStrokeWidth)
                        currentPath.path.moveTo(point.x, point.y)
                    } else {
                        currentPath.path.lineTo(point.x, point.y)
                    }
                    drawPath(currentPath)
                }

                if (!point.stillDrawing) {
                    var removeLast = false
                    if (currentPath != null) {
                        //completedPaths += currentPath

                        var usedPoints = listOf<Offset>()
                        for (j in incompleteStartIndex..i) {
                            if (!points[j].cancelEvent) {
                                usedPoints += Offset(points[j].x, points[j].y)
                            }
                        }

                        if (drawingMode == DrawingMode.Pen) {
                            val smoothedPoints = chaikinSmoothing(usedPoints, 3)
                            val smoothedPath = Path()
                            for ((j, smoothedPoint) in smoothedPoints.withIndex()) {
                                if (j == 0) {
                                    smoothedPath.moveTo(smoothedPoint.x, smoothedPoint.y)
                                } else {
                                    smoothedPath.lineTo(smoothedPoint.x, smoothedPoint.y)
                                }
                            }
                            completedPaths += PathState(
                                path = smoothedPath,
                                color = drawingColor,
                                strokeWidth = useStrokeWidth
                            )
                        } else {
                            completedPaths += currentPath
                        }

                        if (point.cancelEvent) {
                            removeLast = true
                        }
                    }
                    currentPath = null

                    for (j in incompleteStartIndex..i) {
                        points[j].partOfCompletePath = true
                    }

                    if (removeLast) {
                        completedPaths.removeLast()
                    }

                    incompleteStartIndex = i + 1
                }
            } else {
                incompleteStartIndex++
            }
        }

        if (incompleteStartIndex - 1 >= 0) {
            points.removeRange(0, incompleteStartIndex - 1)
        }
    }
}