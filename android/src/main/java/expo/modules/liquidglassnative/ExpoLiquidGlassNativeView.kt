package expo.modules.liquidglassnative

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Rect
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.PixelCopy
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.interfaces.fabric.ReactSurface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.findViewTreeCompositionContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import kotlin.math.roundToInt

private const val REALTIME_CAPTURE_SCALE = 0.5f

private data class GlassProps(
  val tint: Color = Color(0xFFFFFFFF),
  val surfaceColor: Color = Color(0x14FFFFFF),
  val blurRadius: Float = 4f,
  val lensX: Float = 50f,
  val lensY: Float = 50f,
  val cornerRadius: Float = 28f,
  val imageUri: String? = null,
  val backgroundImageUri: String? = null,
  val useRealtimeCapture: Boolean = true,
  val renderBackgroundContent: Boolean = false,
  val renderInSeparateWindow: Boolean = false,
  val overlayId: String? = null,
  val captureRectX: Float? = null,
  val captureRectY: Float? = null,
  val captureRectWidth: Float? = null,
  val captureRectHeight: Float? = null
)

open class ExpoLiquidGlassNativeView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private val logTag = "ExpoLiquidGlassNative"
  private var props by mutableStateOf(GlassProps())
  private var cachedBackdropBitmap by mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
  private var isFrameCaptureScheduled = false
  private var isPixelCopyInFlight = false
  private val mainHandler = Handler(Looper.getMainLooper())
  private var reusableCaptureBitmap: Bitmap? = null
  private var popupWindow: PopupWindow? = null
  private var popupContainer: FrameLayout? = null
  private var overlaySurface: ReactSurface? = null
  private var overlayRootView: ReactRootView? = null

  private val realtimeCaptureFrameRunnable = object : Runnable {
    override fun run() {
      isFrameCaptureScheduled = false
      if (!props.useRealtimeCapture || !isAttachedToWindow) {
        return
      }

      requestBackdropCapture()
      scheduleRealtimeCapture()
    }
  }

  private val composeView = ComposeView(context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    isClickable = false
    isFocusable = false
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
      val backdrop = rememberLayerBackdrop(
        onDraw = {
          if (props.useRealtimeCapture) {
            cachedBackdropBitmap?.let { imageBitmap ->
              drawImage(
                image = imageBitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
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
        updateRealtimeCaptureLoop()
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
    composeView.visibility = View.VISIBLE
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
    updatePresentationHost()
    updateRealtimeCaptureLoop()
    requestBackdropCapture()
  }

  override fun onDetachedFromWindow() {
    dismissPopupWindow()
    teardownRealtimeCaptureLoop()
    reusableCaptureBitmap = null
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
    renderBackgroundContent: Boolean? = null,
    renderInSeparateWindow: Boolean? = null,
    overlayId: String? = null,
    captureRectX: Float? = null,
    captureRectY: Float? = null,
    captureRectWidth: Float? = null,
    captureRectHeight: Float? = null
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
      renderBackgroundContent = renderBackgroundContent ?: props.renderBackgroundContent,
      renderInSeparateWindow = renderInSeparateWindow ?: props.renderInSeparateWindow,
      overlayId = overlayId ?: props.overlayId,
      captureRectX = captureRectX ?: props.captureRectX,
      captureRectY = captureRectY ?: props.captureRectY,
      captureRectWidth = captureRectWidth ?: props.captureRectWidth,
      captureRectHeight = captureRectHeight ?: props.captureRectHeight
    )
    updateOutline()
    updatePresentationHost()
    updateRealtimeCaptureLoop()
    requestBackdropCapture()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    updatePresentationHost()
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

      val bitmapWidth = (width * REALTIME_CAPTURE_SCALE).roundToInt().coerceAtLeast(1)
      val bitmapHeight = (height * REALTIME_CAPTURE_SCALE).roundToInt().coerceAtLeast(1)
      val bitmap = obtainReusableCaptureBitmap(bitmapWidth, bitmapHeight) ?: return null
      val canvas = Canvas(bitmap)
      canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

      val viewLocation = IntArray(2)
      getLocationInWindow(viewLocation)
      val rootLocation = IntArray(2)
      rootView.getLocationInWindow(rootLocation)

      val saveCount = canvas.save()
      try {
        val offsetX = viewLocation[0] - rootLocation[0]
        val offsetY = viewLocation[1] - rootLocation[1]
        canvas.scale(REALTIME_CAPTURE_SCALE, REALTIME_CAPTURE_SCALE)
        canvas.translate(-offsetX.toFloat(), -offsetY.toFloat())
        rootView.background?.draw(canvas)
        if (rootView is ViewGroup) {
          drawViewGroupChildren(rootView, canvas, excludedViews, rootLocation)
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

  private fun updateRealtimeCaptureLoop() {
    if (!props.useRealtimeCapture || !isAttachedToWindow) {
      teardownRealtimeCaptureLoop()
      return
    }

    scheduleRealtimeCapture()
  }

  private fun teardownRealtimeCaptureLoop() {
    removeCallbacks(realtimeCaptureFrameRunnable)
    isFrameCaptureScheduled = false
  }

  private fun scheduleRealtimeCapture() {
    if (isFrameCaptureScheduled) {
      return
    }
    isFrameCaptureScheduled = true
    postOnAnimation(realtimeCaptureFrameRunnable)
  }

  private fun requestBackdropCapture() {
    if (!props.useRealtimeCapture || width <= 0 || height <= 0) {
      if (!props.useRealtimeCapture) {
        cachedBackdropBitmap = null
      }
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && requestPixelCopyBackdrop()) {
      return
    }

    cachedBackdropBitmap = captureBackdropBitmap()?.asImageBitmap()
  }

  private fun requestPixelCopyBackdrop(): Boolean {
    if (isPixelCopyInFlight) {
      return true
    }

    val reactContext = appContext.reactContext as? ReactContext ?: return false
    val activity = reactContext.currentActivity ?: return false
    val window = activity.window ?: return false
    val decorView = window.peekDecorView() ?: return false
    if (!decorView.isAttachedToWindow) {
      return false
    }

    val density = resources.displayMetrics.density
    val captureWidthPx = ((props.captureRectWidth ?: width / density) * density).toInt().coerceAtLeast(1)
    val captureHeightPx = ((props.captureRectHeight ?: height / density) * density).toInt().coerceAtLeast(1)
    val bitmapWidthPx = (captureWidthPx * REALTIME_CAPTURE_SCALE).roundToInt().coerceAtLeast(1)
    val bitmapHeightPx = (captureHeightPx * REALTIME_CAPTURE_SCALE).roundToInt().coerceAtLeast(1)
    val viewLocation = IntArray(2)
    getLocationInWindow(viewLocation)
    val captureX = props.captureRectX?.let { (it * density).toInt() } ?: viewLocation[0]
    val captureY = props.captureRectY?.let { (it * density).toInt() } ?: viewLocation[1]
    val srcRect = Rect(
      captureX,
      captureY,
      captureX + captureWidthPx,
      captureY + captureHeightPx
    )
    val bitmap = obtainReusableCaptureBitmap(bitmapWidthPx, bitmapHeightPx) ?: return false

    isPixelCopyInFlight = true
    return try {
      PixelCopy.request(window, srcRect, bitmap, { copyResult ->
        isPixelCopyInFlight = false
        if (copyResult == PixelCopy.SUCCESS) {
          cachedBackdropBitmap = bitmap.asImageBitmap()
        }
      }, mainHandler)
      true
    } catch (_: IllegalArgumentException) {
      isPixelCopyInFlight = false
      Log.w(logTag, "PixelCopy request failed with IllegalArgumentException")
      false
    }
  }

  private fun obtainReusableCaptureBitmap(width: Int, height: Int): Bitmap? {
    val currentBitmap = reusableCaptureBitmap
    if (currentBitmap != null && currentBitmap.width == width && currentBitmap.height == height && !currentBitmap.isRecycled) {
      return currentBitmap
    }

    return try {
      Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
        reusableCaptureBitmap = it
      }
    } catch (_: OutOfMemoryError) {
      reusableCaptureBitmap = null
      null
    }
  }

  private fun updatePresentationHost() {
    if (!props.renderInSeparateWindow || !isAttachedToWindow) {
      dismissPopupWindow()
      if (composeView.parent == this) {
        composeView.visibility = View.VISIBLE
      }
      return
    }

    val activity = (appContext.reactContext as? ReactContext)?.currentActivity ?: return
    val decorView = activity.window?.decorView ?: return
    val popup = ensurePopupWindow()
    val container = popupContainer ?: return

    val density = resources.displayMetrics.density
    val popupX = props.captureRectX?.let { (it * density).toInt() } ?: 0
    val popupY = props.captureRectY?.let { (it * density).toInt() } ?: 0
    val popupWidth = ((props.captureRectWidth ?: width / density) * density).toInt().coerceAtLeast(1)
    val popupHeight = ((props.captureRectHeight ?: height / density) * density).toInt().coerceAtLeast(1)

    popup.width = popupWidth
    popup.height = popupHeight
    container.layoutParams = FrameLayout.LayoutParams(popupWidth, popupHeight)

    if (!popup.isShowing) {
      popup.showAtLocation(decorView, Gravity.TOP or Gravity.START, popupX, popupY)
    } else {
      popup.update(popupX, popupY, popupWidth, popupHeight)
    }

    container.post {
      if (popupContainer !== container || popupWindow !== popup || !popup.isShowing) {
        return@post
      }

      installPopupOwnersForVisibleHierarchy(container)
      attachPopupContent(container)
      composeView.visibility = View.VISIBLE
    }
  }

  private fun ensurePopupWindow(): PopupWindow {
    popupWindow?.let { return it }

    val activity = (appContext.reactContext as? ReactContext)?.currentActivity
    val container = FrameLayout(context).apply {
      clipChildren = false
      clipToPadding = false
      setBackgroundColor(android.graphics.Color.TRANSPARENT)
      layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
      )
      if (activity != null) {
        installPopupOwners(this, activity)
      }
    }
    popupContainer = container

    return PopupWindow(
      container,
      FrameLayout.LayoutParams.WRAP_CONTENT,
      FrameLayout.LayoutParams.WRAP_CONTENT,
      false
    ).apply {
      isTouchable = false
      isFocusable = false
      isClippingEnabled = false
      setBackgroundDrawable(null)
      popupWindow = this
    }
  }

  private fun dismissPopupWindow() {
    overlayRootView?.unmountReactApplication()
    overlayRootView = null
    overlaySurface?.stop()
    overlaySurface?.detach()
    overlaySurface = null
    popupWindow?.dismiss()
    popupWindow = null
    popupContainer = null

    if (composeView.parent !== this) {
      (composeView.parent as? ViewGroup)?.removeView(composeView)
      addView(composeView, 0)
    }
  }

  private fun updateOverlaySurface(container: FrameLayout) {
    val overlayId = props.overlayId
    if (overlayId.isNullOrBlank()) {
      overlayRootView?.let { view ->
        container.removeView(view)
      }
      overlayRootView?.unmountReactApplication()
      overlayRootView = null
      overlaySurface?.view?.let { view ->
        container.removeView(view)
      }
      overlaySurface?.stop()
      overlaySurface?.detach()
      overlaySurface = null
      return
    }

    val activity = (appContext.reactContext as? ReactContext)?.currentActivity ?: return
    val reactApplication = activity.application as? ReactApplication ?: return

    val currentReactContext = appContext.reactContext as? ReactContext
    val useBridgelessOverlayHost = currentReactContext?.isBridgeless == true

    if (!useBridgelessOverlayHost) {
      val reactNativeHost = reactApplication.reactNativeHost
      val reactInstanceManager = reactNativeHost.reactInstanceManager
      val currentRootView = overlayRootView
      if (currentRootView == null) {
        val newRootView = ReactRootView(context).apply {
          isClickable = false
          isFocusable = false
          setIsFabric(false)
          startReactApplication(
            reactInstanceManager,
            "ExpoLiquidGlassNativeOverlayHost",
            Bundle().apply {
              putString("overlayId", overlayId)
            }
          )
        }
        overlayRootView = newRootView
        container.addView(
          newRootView,
          FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
          )
        )
      } else if (currentRootView.parent !== container) {
        (currentRootView.parent as? ViewGroup)?.removeView(currentRootView)
        container.addView(
          currentRootView,
          FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
          )
        )
      }

      overlaySurface?.view?.let { view ->
        container.removeView(view)
      }
      overlaySurface?.stop()
      overlaySurface?.detach()
      overlaySurface = null
      return
    }

    val reactHost = reactApplication.reactHost ?: return

    val currentSurface = overlaySurface
    if (currentSurface == null || currentSurface.moduleName != "ExpoLiquidGlassNativeOverlayHost") {
      currentSurface?.stop()
      currentSurface?.detach()

      val initialProps = Bundle().apply {
        putString("overlayId", overlayId)
      }
      val newSurface = reactHost.createSurface(activity, "ExpoLiquidGlassNativeOverlayHost", initialProps)
      overlaySurface = newSurface
      newSurface.view?.let { view ->
        view.isClickable = false
        view.isFocusable = false
        if (view.parent !== container) {
          (view.parent as? ViewGroup)?.removeView(view)
          container.addView(
            view,
            FrameLayout.LayoutParams(
              FrameLayout.LayoutParams.MATCH_PARENT,
              FrameLayout.LayoutParams.MATCH_PARENT
            )
          )
        }
      }
      newSurface.start()
      return
    }

    currentSurface.view?.let { view ->
      if (view.parent !== container) {
        (view.parent as? ViewGroup)?.removeView(view)
        container.addView(
          view,
          FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
          )
        )
      }
    }
  }

  private fun attachPopupContent(container: FrameLayout) {
    composeView.setParentCompositionContext(findViewTreeCompositionContext())

    if (composeView.parent !== container) {
      (composeView.parent as? ViewGroup)?.removeView(composeView)
      container.addView(
        composeView,
        FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT
        )
      )
    }

    updateOverlaySurface(container)
  }

  private fun installPopupOwnersForVisibleHierarchy(container: FrameLayout) {
    val activity = (appContext.reactContext as? ReactContext)?.currentActivity ?: return

    installPopupOwners(container, activity)
    installPopupOwners(composeView, activity)

    overlaySurface?.view?.let { overlayView ->
      installPopupOwners(overlayView, activity)
    }

    val popupRoot = runCatching { container.rootView }.getOrNull()
    if (popupRoot != null && popupRoot !== container) {
      installPopupOwners(popupRoot, activity)
    }
  }

  private fun installPopupOwners(targetView: View, activity: android.app.Activity) {
    setOwnerIfPresent(
      ownerClassName = "androidx.lifecycle.LifecycleOwner",
      helperClassName = "androidx.lifecycle.ViewTreeLifecycleOwner",
      targetView = targetView,
      owner = activity
    )
    setOwnerIfPresent(
      ownerClassName = "androidx.lifecycle.ViewModelStoreOwner",
      helperClassName = "androidx.lifecycle.ViewTreeViewModelStoreOwner",
      targetView = targetView,
      owner = activity
    )
    setOwnerIfPresent(
      ownerClassName = "androidx.savedstate.SavedStateRegistryOwner",
      helperClassName = "androidx.savedstate.ViewTreeSavedStateRegistryOwner",
      targetView = targetView,
      owner = activity
    )
  }

  private fun setOwnerIfPresent(
    ownerClassName: String,
    helperClassName: String,
    targetView: View,
    owner: Any
  ) {
    runCatching {
      val ownerClass = Class.forName(ownerClassName)
      if (!ownerClass.isInstance(owner)) {
        return
      }

      val helperClass = Class.forName(helperClassName)
      val setMethod = helperClass.getMethod("set", View::class.java, ownerClass)
      setMethod.invoke(null, targetView, owner)
    }.onFailure { error ->
      Log.w(logTag, "Failed to install popup owner for $helperClassName", error)
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
