package expo.modules.liquidglassnative

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import expo.modules.liquidglassnative.components.LiquidButton
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.compose.foundation.layout.BoxScope

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
            
            // 각 View가 독립적으로 backdrop 생성
            val backdrop = rememberLayerBackdrop(
                onDraw = { drawContent() }
            )
            
            // 각 View의 배경 이미지 URI
            val backgroundImageUri = props.backgroundImageUri ?: props.imageUri
            
            // 배경 이미지가 있으면 BackdropDemoScaffold로 렌더링
            if (backgroundImageUri != null) {
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
}

