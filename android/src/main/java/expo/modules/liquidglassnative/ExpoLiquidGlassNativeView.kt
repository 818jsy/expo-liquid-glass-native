package expo.modules.liquidglassnative

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

private data class GlassProps(
  val tint: Color = Color(0xFFFFFFFF),
  val surfaceColor: Color = Color(0x14FFFFFF),
  val blurRadius: Float = 1f,
  val lensX: Float = 28f,
  val lensY: Float = 28f,
  val cornerRadius: Float = 28f,
  val imageUri: String? = null,
  val backgroundImageUri: String? = null,
  val useRealtimeCapture: Boolean = true,
  val renderBackgroundContent: Boolean = false
)

open class ExpoLiquidGlassNativeView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private var props by mutableStateOf(GlassProps())
  private var cachedBackdropBitmap by mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
  private var observedRootView: View? = null

  private val realtimeCapturePreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (!props.useRealtimeCapture) {
      true
    } else {
      requestBackdropCapture()
      true
    }
  }

  private val composeView = ComposeView(context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    isClickable = false
    isFocusable = false
    setContent {
      val backdrop = rememberLayerBackdrop(
        onDraw = {
          if (props.useRealtimeCapture) {
            cachedBackdropBitmap?.let { imageBitmap ->
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
          cachedBackdropBitmap = null
        } else {
          requestBackdropCapture()
        }
        updateRealtimeCaptureObservation()
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

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    updateRealtimeCaptureObservation()
    requestBackdropCapture()
  }

  override fun onDetachedFromWindow() {
    teardownRealtimeCaptureObservation()
    super.onDetachedFromWindow()
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
    updateRealtimeCaptureObservation()
    requestBackdropCapture()
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
      val excludedViews = collectOverlayExclusionSet(rootView)

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
        val hiddenViews = excludedViews
          .filter { it.alpha != 0f }
          .associateWith { it.alpha }
        try {
          hiddenViews.keys.forEach { excludedView ->
            excludedView.alpha = 0f
          }
          rootView.draw(canvas)
        } finally {
          hiddenViews.forEach { (excludedView, alpha) ->
            excludedView.alpha = alpha
          }
        }
      } finally {
        canvas.restoreToCount(saveCount)
      }

      bitmap
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Exclude the glass view itself and any siblings rendered above it in the ancestor chain.
   * Those views are foreground overlays from the glass perspective and should not become
   * part of the sampled backdrop bitmap.
   */
  private fun collectOverlayExclusionSet(rootView: View): Set<View> {
    val excludedViews = linkedSetOf<View>()
    excludedViews += this

    var current: View = this
    while (current !== rootView) {
      val parent = current.parent as? ViewGroup ?: break
      val currentIndex = parent.indexOfChild(current)
      if (currentIndex >= 0) {
        for (index in currentIndex + 1 until parent.childCount) {
          collectViewSubtree(parent.getChildAt(index), excludedViews)
        }
      }
      current = parent
    }

    return excludedViews
  }

  private fun collectViewSubtree(view: View, excludedViews: MutableSet<View>) {
    excludedViews += view
    if (view is ViewGroup) {
      for (index in 0 until view.childCount) {
        collectViewSubtree(view.getChildAt(index), excludedViews)
      }
    }
  }

  private fun updateRealtimeCaptureObservation() {
    val reactContext = appContext.reactContext as? ReactContext
    val activity = reactContext?.currentActivity
    val rootView = activity?.window?.decorView
    val shouldObserve = props.useRealtimeCapture && isAttachedToWindow && rootView != null

    if (!shouldObserve) {
      teardownRealtimeCaptureObservation()
      return
    }

    if (observedRootView === rootView) {
      return
    }

    teardownRealtimeCaptureObservation()
    if (rootView?.viewTreeObserver?.isAlive == true) {
      rootView.viewTreeObserver.addOnPreDrawListener(realtimeCapturePreDrawListener)
      observedRootView = rootView
    }
  }

  private fun teardownRealtimeCaptureObservation() {
    val rootView = observedRootView ?: return
    if (rootView.viewTreeObserver.isAlive) {
      rootView.viewTreeObserver.removeOnPreDrawListener(realtimeCapturePreDrawListener)
    }
    observedRootView = null
  }

  private fun requestBackdropCapture() {
    if (!props.useRealtimeCapture || width <= 0 || height <= 0) {
      if (!props.useRealtimeCapture) {
        cachedBackdropBitmap = null
      }
      return
    }

    cachedBackdropBitmap = captureBackdropBitmap()?.asImageBitmap()
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
