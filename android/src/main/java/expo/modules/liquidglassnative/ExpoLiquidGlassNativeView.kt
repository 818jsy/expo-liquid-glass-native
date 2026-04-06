package expo.modules.liquidglassnative

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.facebook.react.bridge.ReactContext
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousRoundedRectangle
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import kotlinx.coroutines.delay

private data class GlassProps(
  val tint: Color = Color.Unspecified,
  val surfaceColor: Color = Color(0x1AFFFFFF),
  val blurRadius: Float = 8f,
  val lensX: Float = 24f,
  val lensY: Float = 24f,
  val cornerRadius: Float = 24f,
  val imageUri: String? = null,
  val backgroundImageUri: String? = null,
  val useRealtimeCapture: Boolean = false,
  val renderBackgroundContent: Boolean = false
)

open class ExpoLiquidGlassNativeView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private var props by mutableStateOf(GlassProps())

  private val composeView = ComposeView(context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    isClickable = false
    isFocusable = false
    setContent {
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
          GlassBackground(
            backdrop = backdrop,
            props = props
          )
        }
      } else {
        GlassBackground(
          backdrop = backdrop,
          props = props
        )
      }
    }
  }

  init {
    clipChildren = false
    clipToPadding = false
    addView(composeView, 0)
    updateOutline()
  }

  override fun onViewAdded(child: View?) {
    super.onViewAdded(child)
    if (child != null && child != composeView) {
      bringChildToFront(child)
    }
  }

  fun updateProps(
    tint: String? = null,
    surfaceColor: String? = null,
    blurRadius: Float? = null,
    lensX: Float? = null,
    lensY: Float? = null,
    cornerRadius: Float? = null,
    imageUri: String? = null,
    backgroundImageUri: String? = null,
    useRealtimeCapture: Boolean? = null,
    renderBackgroundContent: Boolean? = null
  ) {
    props = props.copy(
      tint = tint?.toComposeColor() ?: props.tint,
      surfaceColor = surfaceColor?.toComposeColor() ?: props.surfaceColor,
      blurRadius = blurRadius ?: props.blurRadius,
      lensX = lensX ?: props.lensX,
      lensY = lensY ?: props.lensY,
      cornerRadius = cornerRadius ?: props.cornerRadius,
      imageUri = imageUri ?: props.imageUri,
      backgroundImageUri = backgroundImageUri ?: props.backgroundImageUri,
      useRealtimeCapture = useRealtimeCapture ?: props.useRealtimeCapture,
      renderBackgroundContent = renderBackgroundContent ?: props.renderBackgroundContent
    )
    updateOutline()
  }

  private fun updateOutline() {
    clipToOutline = props.cornerRadius > 0f
    outlineProvider = object : ViewOutlineProvider() {
      override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, view.width, view.height, props.cornerRadius * resources.displayMetrics.density)
      }
    }
    invalidateOutline()
  }

  private fun captureBackdropBitmap(): Bitmap? {
    return try {
      val reactContext = appContext.reactContext as? ReactContext ?: return null
      val activity = reactContext.currentActivity ?: return null
      val rootView = activity.window?.decorView ?: return null

      val width = width.coerceAtLeast(1)
      val height = height.coerceAtLeast(1)
      if (width <= 0 || height <= 0 || rootView.visibility != View.VISIBLE) {
        return null
      }

      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)

      val viewLocation = IntArray(2)
      getLocationInWindow(viewLocation)
      val rootLocation = IntArray(2)
      rootView.getLocationInWindow(rootLocation)

      val saveCount = canvas.save()
      try {
        val offsetX = viewLocation[0] - rootLocation[0]
        val offsetY = viewLocation[1] - rootLocation[1]
        canvas.translate(-offsetX.toFloat(), -offsetY.toFloat())

        rootView.background?.draw(canvas)
        if (rootView is ViewGroup) {
          drawViewGroupChildren(rootView, canvas, setOf(this), rootLocation)
        } else if (rootView != this) {
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

@androidx.compose.runtime.Composable
private fun GlassBackground(
  backdrop: LayerBackdrop,
  props: GlassProps
) {
  Box(
    Modifier
      .fillMaxSize()
      .drawBackdrop(
        backdrop = backdrop,
        shape = { ContinuousRoundedRectangle(props.cornerRadius.dp) },
        effects = {
          vibrancy()
          blur(props.blurRadius.dp.toPx())
          lens(props.lensX.dp.toPx(), props.lensY.dp.toPx())
        },
        onDrawSurface = {
          if (props.tint.isSpecified) {
            drawRect(props.tint.copy(alpha = 0.18f))
          }
          if (props.surfaceColor.isSpecified) {
            drawRect(props.surfaceColor)
          }
        }
      )
  )
}

private fun String.toComposeColor(): Color {
  return try {
    Color(android.graphics.Color.parseColor(this))
  } catch (_: Exception) {
    Color.Unspecified
  }
}
