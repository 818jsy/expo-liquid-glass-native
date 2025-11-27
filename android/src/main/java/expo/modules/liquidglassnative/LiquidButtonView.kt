package expo.modules.liquidglassnative

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import expo.modules.liquidglassnative.components.LiquidButton

class LiquidButtonView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
    private val onPress by EventDispatcher<Map<String, Any>>()

    private data class ButtonProps(
        val title: String = "Button",
        val enabled: Boolean = true,
        val tint: Color = Color.Unspecified,
        val surfaceColor: Color = Color.Unspecified,
        val blurRadius: Float = 2f,
        val lensX: Float = 12f,
        val lensY: Float = 24f
    )

    private var props by mutableStateOf(ButtonProps())

    private val composeView = ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setContent {
            val backdrop = rememberLayerBackdrop()
            val density = LocalDensity.current

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
        lensY: Float? = null
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
            lensY = lensY ?: props.lensY
        )
    }
}

