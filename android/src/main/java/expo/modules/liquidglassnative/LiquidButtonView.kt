package expo.modules.liquidglassnative

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.facebook.react.bridge.ReactContext
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import expo.modules.liquidglassnative.components.LiquidButton
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.geometry.Offset

class LiquidButtonView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
    private val onPress by EventDispatcher<Map<String, Any>>()

    private data class ButtonProps(
        val title: String = "Button",
        val enabled: Boolean = true,
        val tint: Color = Color.Unspecified,
        val surfaceColor: Color = Color.Unspecified,
        val blurRadius: Float = 2f,
        val lensX: Float = 12f,
        val lensY: Float = 24f,
        val imageUri: String? = null,
        val backgroundImageUri: String? = null,
        val useRealtimeCapture: Boolean = false,
        val renderBackgroundContent: Boolean = false
    )

    private var props by mutableStateOf(ButtonProps())

    private val composeView = ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setContent {
            val density = LocalDensity.current
            val cachedBitmap = remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

            val backdrop = rememberLayerBackdrop(
                onDraw = {
                    if (props.useRealtimeCapture) {
                        cachedBitmap.value?.let { imageBitmap ->
                            drawContext.canvas.drawImage(
                                image = imageBitmap,
                                topLeftOffset = Offset.Zero,
                                paint = Paint()
                            )
                        } ?: drawContent()
                    } else {
                        drawContent()
                    }
                }
            )

            LaunchedEffect(props.useRealtimeCapture) {
                if (!props.useRealtimeCapture) {
                    cachedBitmap.value = null
                    return@LaunchedEffect
                }

                while (true) {
                    cachedBitmap.value = captureBackdropBitmap()?.asImageBitmap()
                    delay(16)
                }
            }

            val backgroundImageUri = props.backgroundImageUri ?: props.imageUri

            if (backgroundImageUri != null || props.useRealtimeCapture) {
                BackdropDemoScaffold(
                    backdrop = backdrop,
                    backgroundImageUri = backgroundImageUri,
                    useRealtimeCapture = props.useRealtimeCapture,
                    renderBackgroundContent = props.renderBackgroundContent
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidButton(
                            onClick = {
                                if (props.enabled) {
                                    onPress(mapOf())
                                }
                            },
                            backdrop = backdrop,
                            modifier = Modifier,
                            isInteractive = props.enabled,
                            tint = props.tint,
                            surfaceColor = props.surfaceColor,
                            blurRadius = with(density) { props.blurRadius.dp.toPx() },
                            lensX = with(density) { props.lensX.dp.toPx() },
                            lensY = with(density) { props.lensY.dp.toPx() }
                        ) {
                            BasicText(
                                props.title,
                                style = TextStyle(
                                    color = if (props.tint.isSpecified) Color.White else Color.Black,
                                    fontSize = 15f.sp
                                )
                            )
                        }
                    }
                }
            } else {
                // 배경이 없는 경우
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidButton(
                        onClick = {
                            if (props.enabled) {
                                onPress(mapOf())
                            }
                        },
                        backdrop = backdrop,
                        modifier = Modifier,
                        isInteractive = props.enabled,
                        tint = props.tint,
                        surfaceColor = props.surfaceColor,
                        blurRadius = with(density) { props.blurRadius.dp.toPx() },
                        lensX = with(density) { props.lensX.dp.toPx() },
                        lensY = with(density) { props.lensY.dp.toPx() }
                    ) {
                        BasicText(
                            props.title,
                            style = TextStyle(
                                color = if (props.tint.isSpecified) Color.White else Color.Black,
                                fontSize = 15f.sp
                            )
                        )
                    }
                }
            }
        }
    }

    init {
        addView(composeView)
    }

    fun updateProps(
        title: String? = null,
        enabled: Boolean? = null,
        tint: String? = null,
        surfaceColor: String? = null,
        blurRadius: Float? = null,
        lensX: Float? = null,
        lensY: Float? = null,
        imageUri: String? = null,
        backgroundImageUri: String? = null,
        useRealtimeCapture: Boolean? = null,
        renderBackgroundContent: Boolean? = null
    ) {
        props = props.copy(
            title = title ?: props.title,
            enabled = enabled ?: props.enabled,
            tint = tint?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    Color.Unspecified
                }
            } ?: props.tint,
            surfaceColor = surfaceColor?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (e: Exception) {
                    Color.Unspecified
                }
            } ?: props.surfaceColor,
            blurRadius = blurRadius ?: props.blurRadius,
            lensX = lensX ?: props.lensX,
            lensY = lensY ?: props.lensY,
            imageUri = imageUri ?: props.imageUri,
            backgroundImageUri = backgroundImageUri ?: props.backgroundImageUri,
            useRealtimeCapture = useRealtimeCapture ?: props.useRealtimeCapture,
            renderBackgroundContent = renderBackgroundContent ?: props.renderBackgroundContent
        )
    }

    private fun captureBackdropBitmap(): Bitmap? {
        return try {
            val reactContext = appContext.reactContext as? ReactContext ?: return null
            val activity = reactContext.currentActivity ?: return null
            val rootView = activity.window?.decorView ?: return null

            val width = composeView.width.coerceAtLeast(1)
            val height = composeView.height.coerceAtLeast(1)
            if (width <= 0 || height <= 0 || rootView.visibility != View.VISIBLE) {
                return null
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val composeLocation = IntArray(2)
            composeView.getLocationInWindow(composeLocation)
            val rootLocation = IntArray(2)
            rootView.getLocationInWindow(rootLocation)

            val saveCount = canvas.save()
            try {
                val offsetX = composeLocation[0] - rootLocation[0]
                val offsetY = composeLocation[1] - rootLocation[1]
                canvas.translate(-offsetX.toFloat(), -offsetY.toFloat())

                rootView.background?.draw(canvas)
                if (rootView is ViewGroup) {
                    drawViewGroupChildren(rootView, canvas, setOf(this, composeView), rootLocation)
                } else if (rootView != this && rootView != composeView) {
                    rootView.draw(canvas)
                }
            } finally {
                canvas.restoreToCount(saveCount)
            }

            bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun drawViewGroupChildren(
        parent: ViewGroup,
        canvas: Canvas,
        excludeViews: Set<View>,
        parentLocation: IntArray
    ) {
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            if (child in excludeViews || child.visibility != View.VISIBLE) {
                continue
            }

            try {
                val childLocation = IntArray(2)
                child.getLocationInWindow(childLocation)
                val childOffsetX = childLocation[0] - parentLocation[0]
                val childOffsetY = childLocation[1] - parentLocation[1]

                val saveCount = canvas.save()
                try {
                    canvas.translate(childOffsetX.toFloat(), childOffsetY.toFloat())
                    if (child is ViewGroup) {
                        val childParentLocation = IntArray(2)
                        child.getLocationInWindow(childParentLocation)
                        drawViewGroupChildren(child, canvas, excludeViews, childParentLocation)
                    } else {
                        child.draw(canvas)
                    }
                } finally {
                    canvas.restoreToCount(saveCount)
                }
            } catch (_: Exception) {
            }
        }
    }
}
