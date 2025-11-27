package expo.modules.liquidglassnative

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalDensity
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
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import expo.modules.liquidglassnative.components.LiquidButton

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
        return ComposeView(reactContext)
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
    
    private data class ButtonProps(
        val title: String = "Button",
        val enabled: Boolean = true,
        val tint: Color = Color.Unspecified,
        val surfaceColor: Color = Color.Unspecified,
        val blurRadius: Float = 2f,
        val lensX: Float = 12f,
        val lensY: Float = 24f
    )
    
    private fun updateViewContent(view: ComposeView, props: ButtonProps) {
        view.tag = props
        val reactContext = view.context as ReactContext
        
        view.setContent {
            val backdrop = rememberLayerBackdrop()
            val density = LocalDensity.current
            
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
    
    private fun sendPressEvent(reactContext: ReactContext, view: android.view.View) {
        val event: WritableMap = Arguments.createMap()
        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(view.id, "topPress", event)
    }
}
