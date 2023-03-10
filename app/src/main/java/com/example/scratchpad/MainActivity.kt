package com.example.scratchpad

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import com.example.scratchpad.ui.theme.ScratchPadTheme
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScratchPadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    App()
                }
            }
        }

//        val windowInsetsController =
//            WindowCompat.getInsetsController(window, window.decorView)
//        // Configure the behavior of the hidden system bars.
//        windowInsetsController?.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//
//        //windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
//        windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //state = "Landscape" // this will automatically change the text to landscape

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //state = "Potrait"   // this will automatically change the text to potrait

        }
    }

}

data class PointState (
    val x: Float,
    val y: Float,
    val stillDrawing: Boolean,
    val cancelEvent: Boolean = false,
    var partOfCompletePath: Boolean = false
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun App() {
    var floatingButtonPosition = FabPosition.Center

//    val configuration = LocalConfiguration.current
//    when (configuration.orientation) {
//        Configuration.ORIENTATION_LANDSCAPE -> {
//            floatingButtonPosition = FabPosition.Center
//        }
//        Configuration.ORIENTATION_PORTRAIT -> {
//            floatingButtonPosition = FabPosition.Center
//        }
//    }

    val (fabOnClick, setFabOnClick) = remember { mutableStateOf<(() -> Unit)?>(null) }

    Scaffold(
        floatingActionButtonPosition = floatingButtonPosition,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    fabOnClick?.invoke()
                },
            ) {
                //Icon(Icons.Filled.KeyboardArrowUp, "Tools")
                Icon(Icons.Default.Delete, "Delete")
            }
        },
        content = {
            ScratchPadCanvas(setFabOnClick)
        },
    )

    val context = LocalContext.current
    BackHandler() {
        val toast = Toast.makeText(context, "Back is disabled", Toast.LENGTH_SHORT)
        ///toast.setGravity(Gravity.TOP, Gravity.CENTER, 0)
        toast.show()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScratchPadCanvas(setFabOnClick: (() -> Unit) -> Unit) {
    var size by remember { mutableStateOf(Size.Zero) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        scale = scale.coerceIn(0.2f, 2f)
        offset += offsetChange / scale
    }

    var completedPaths = remember { mutableStateListOf<Path>() }
    var points = remember { mutableStateListOf<PointState>() }

    LaunchedEffect(Unit) {
        println("LAUNCHED")
        setFabOnClick {
            println("clicked")

            completedPaths.clear()
            points.clear()
        }
    }
        Canvas(
            modifier = Modifier
                .clip(RectangleShape)
                .fillMaxSize()
                .background(Color.White)
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
            fun drawPath(path: Path) {
                drawPath(
                    color = Color.Black,
                    path = path,
                    style = Stroke(
                        width = 12f, // / scale,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    blendMode = BlendMode.Clear
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

            var currentPath: Path? = null

            var incompleteStartIndex: Int = 0

            for ((i, point) in points.withIndex()) {
                if (!point.partOfCompletePath) {
                    if (!point.cancelEvent) {
                        if (currentPath == null) {
                            currentPath = Path()
                            currentPath.moveTo(point.x, point.y)
                        } else {
                            currentPath.lineTo(point.x, point.y)
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

                            val smoothedPoints = chaikinSmoothing(usedPoints, 3)
                            val smoothedPath = Path()
                            for ((j, smoothedPoint) in smoothedPoints.withIndex()) {
                                if (j == 0) {
                                    smoothedPath.moveTo(smoothedPoint.x, smoothedPoint.y)
                                }
                                else {
                                    smoothedPath.lineTo(smoothedPoint.x, smoothedPoint.y)
                                }
                            }
                            completedPaths += smoothedPath


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