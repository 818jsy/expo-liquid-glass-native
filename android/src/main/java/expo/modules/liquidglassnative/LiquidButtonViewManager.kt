package expo.modules.liquidglassnative

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import expo.modules.liquidglassnative.components.LiquidButton
import expo.modules.liquidglassnative.R

class LiquidButtonViewManager : SimpleViewManager<ComposeView>() {
    
    companion object {
        const val REACT_CLASS = "LiquidButtonView"
        const val EVENT_ON_PRESS = "onPress"
    }
    
    override fun getName(): String {
        return REACT_CLASS
    }
    
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        return MapBuilder.of(
            "topPress",
            MapBuilder.of("registrationName", EVENT_ON_PRESS)
        )
    }

    override fun createViewInstance(reactContext: ThemedReactContext): ComposeView {
        return ComposeView(reactContext).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            // 하드웨어 가속 레이어를 사용하여 뒤의 콘텐츠를 캡처할 수 있도록 설정
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
    }

    @ReactProp(name = "title")
    fun setTitle(view: ComposeView, title: String?) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(title = title ?: "Button"))
    }

    @ReactProp(name = "enabled", defaultBoolean = true)
    fun setEnabled(view: ComposeView, enabled: Boolean) {
        view.isEnabled = enabled
        view.alpha = if (enabled) 1.0f else 0.5f
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(enabled = enabled))
    }
    
    @ReactProp(name = "tint")
    fun setTint(view: ComposeView, tintColor: String?) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        val tint = if (tintColor != null) {
            try {
                Color(android.graphics.Color.parseColor(tintColor))
            } catch (e: Exception) {
                Color.Unspecified
            }
        } else {
            Color.Unspecified
        }
        updateViewContent(view, props.copy(tint = tint))
    }
    
    @ReactProp(name = "surfaceColor")
    fun setSurfaceColor(view: ComposeView, surfaceColorStr: String?) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        val surfaceColor = if (surfaceColorStr != null) {
            try {
                Color(android.graphics.Color.parseColor(surfaceColorStr))
            } catch (e: Exception) {
                Color.Unspecified
            }
        } else {
            Color.Unspecified
        }
        updateViewContent(view, props.copy(surfaceColor = surfaceColor))
    }
    
    @ReactProp(name = "blurRadius", defaultFloat = 2f)
    fun setBlurRadius(view: ComposeView, blurRadius: Float) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(blurRadius = blurRadius))
    }
    
    @ReactProp(name = "lensX", defaultFloat = 12f)
    fun setLensX(view: ComposeView, lensX: Float) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(lensX = lensX))
    }
    
    @ReactProp(name = "lensY", defaultFloat = 24f)
    fun setLensY(view: ComposeView, lensY: Float) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(lensY = lensY))
    }
    
    @ReactProp(name = "imageUri")
    fun setImageUri(view: ComposeView, imageUri: String?) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(imageUri = imageUri))
    }
    
    @ReactProp(name = "backgroundImageUri")
    fun setBackgroundImageUri(view: ComposeView, backgroundImageUri: String?) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(backgroundImageUri = backgroundImageUri))
    }
    
    @ReactProp(name = "useRealtimeCapture", defaultBoolean = false)
    fun setUseRealtimeCapture(view: ComposeView, useRealtimeCapture: Boolean) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(useRealtimeCapture = useRealtimeCapture))
    }
    
    @ReactProp(name = "renderBackgroundContent", defaultBoolean = false)
    fun setRenderBackgroundContent(view: ComposeView, renderBackgroundContent: Boolean) {
        val props = (view.tag as? ButtonProps) ?: ButtonProps()
        updateViewContent(view, props.copy(renderBackgroundContent = renderBackgroundContent))
    }
    
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
    
    
    private fun updateViewContent(view: ComposeView, props: ButtonProps) {
        view.tag = props
        val reactContext = view.context as ReactContext
        
        view.setContent {
            val density = LocalDensity.current
            val coroutineScope = rememberCoroutineScope()
            
            // 캐시된 Bitmap 상태
            val cachedBitmap = remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
            val lastSize = remember { mutableStateOf<Pair<Int, Int>?>(null) }
            
            // 각 View가 독립적으로 backdrop 생성
            // useRealtimeCapture일 때는 뒤의 Android View를 직접 그리도록 수정
            // 주기적으로 캡처하여 캐시에 저장하고, onDraw에서는 캐시된 Bitmap 사용
            val backdrop = rememberLayerBackdrop(
                onDraw = {
                    if (props.useRealtimeCapture) {
                        // 캐시된 Bitmap이 있으면 사용
                        cachedBitmap.value?.let { imageBitmap ->
                            val paint: androidx.compose.ui.graphics.Paint = Paint()
                            drawContext.canvas.drawImage(
                                image = imageBitmap,
                                topLeftOffset = Offset.Zero,
                                paint = paint
                            )
                        } ?: run {
                            // Bitmap이 아직 생성되지 않았으면 기본 콘텐츠 그리기
                            drawContent()
                        }
                    } else {
                        drawContent()
                    }
                }
            )
            
            // 주기적으로 배경 캡처 (매 프레임이 아닌 적절한 주기로)
            LaunchedEffect(props.useRealtimeCapture) {
                if (!props.useRealtimeCapture) return@LaunchedEffect
                
                // 주기적으로 캡처 (16ms마다, 약 60fps)
                while (true) {
                    try {
                        val composeView = view
                        
                        // View의 현재 크기 가져오기
                        val bitmapWidth = composeView.width.coerceAtLeast(1)
                        val bitmapHeight = composeView.height.coerceAtLeast(1)
                        
                        if (bitmapWidth > 0 && bitmapHeight > 0) {
                            // Activity의 DecorView 가져오기 (화면 전체)
                            val activity = (reactContext as? com.facebook.react.bridge.ReactContext)?.currentActivity
                            
                            val rootView = activity?.window?.decorView
                            
                            if (rootView != null && rootView.visibility == View.VISIBLE) {
                                val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
                                val androidCanvas = Canvas(bitmap)
                                
                                // 현재 View의 실제 위치 계산 (화면 기준)
                                val composeViewLocation = IntArray(2)
                                composeView.getLocationInWindow(composeViewLocation)
                                
                                // Root view의 실제 위치 계산
                                val rootViewLocation = IntArray(2)
                                rootView.getLocationInWindow(rootViewLocation)
                                
                                // Canvas save
                                val saveCount = androidCanvas.save()
                                
                                try {
                                    // Root view의 위치를 기준으로 현재 View의 상대 위치로 이동
                                    val offsetX = composeViewLocation[0] - rootViewLocation[0]
                                    val offsetY = composeViewLocation[1] - rootViewLocation[1]
                                    androidCanvas.translate(-offsetX.toFloat(), -offsetY.toFloat())
                                    
                                    // 화면 전체 (Root view)를 그리기
                                    try {
                                        // Root view의 배경 그리기
                                        rootView.background?.draw(androidCanvas)
                                        
                                        // Root view의 자식들을 그리기 (현재 ComposeView 제외)
                                        if (rootView is ViewGroup) {
                                            drawViewGroupChildren(rootView, androidCanvas, composeView, rootViewLocation)
                                        } else {
                                            // ViewGroup이 아니면 직접 그리기
                                            if (rootView != composeView) {
                                                rootView.draw(androidCanvas)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("LiquidButtonViewManager", "Error drawing root view: ${e.message}")
                                    }
                                } finally {
                                    androidCanvas.restoreToCount(saveCount)
                                }
                                
                                // 새 Bitmap을 캐시에 저장
                                cachedBitmap.value = bitmap.asImageBitmap()
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LiquidButtonViewManager", "Error creating bitmap: ${e.message}")
                    }
                    
                    // 16ms마다 업데이트 (약 60fps)
                    delay(16)
                }
            }
            
            // 각 View의 배경 이미지 URI
            val backgroundImageUri = props.backgroundImageUri ?: props.imageUri
            
            // 배경 이미지가 있거나 실시간 캡처를 사용하는 경우 BackdropDemoScaffold로 렌더링
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
                                    sendPressEvent(reactContext, view)
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
                                sendPressEvent(reactContext, view)
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
    
    private fun sendPressEvent(reactContext: ReactContext, view: android.view.View) {
        val event: WritableMap = Arguments.createMap()
        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(view.id, "topPress", event)
    }
    
    // ViewGroup의 자식들을 재귀적으로 그리는 헬퍼 함수
    private fun drawViewGroupChildren(
        parent: ViewGroup,
        canvas: Canvas,
        excludeView: View,
        parentLocation: IntArray
    ) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            
            // 제외할 View는 건너뛰기
            if (child == excludeView || child.visibility != View.VISIBLE) {
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
                    
                    // 자식이 ViewGroup이면 재귀적으로 그리기
                    if (child is ViewGroup) {
                        val childParentLocation = IntArray(2)
                        child.getLocationInWindow(childParentLocation)
                        drawViewGroupChildren(child, canvas, excludeView, childParentLocation)
                    } else {
                        // 일반 View는 직접 그리기
                        child.draw(canvas)
                    }
                } catch (e: Exception) {
                    // RuntimeShader 등 지원하지 않는 기능은 무시
                    android.util.Log.d("LiquidButtonViewManager", "Skipping child draw: ${e.message}")
                } finally {
                    canvas.restoreToCount(saveCount)
                }
            } catch (e: Exception) {
                // 무시
            }
        }
    }
}
