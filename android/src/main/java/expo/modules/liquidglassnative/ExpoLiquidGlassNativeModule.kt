package expo.modules.liquidglassnative

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.uimanager.UIManagerHelper

class ExpoLiquidGlassNativeModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoLiquidGlassNative")

    Constant("PI") {
      Math.PI
    }

    Events("onChange")

    Function("hello") {
      "Hello world! 👋"
    }

    AsyncFunction("setValueAsync") { value: String ->
      sendEvent("onChange", mapOf(
        "value" to value
      ))
    }

    // View를 Bitmap으로 캡처하는 함수
    AsyncFunction("captureViewAtPosition") { 
      viewTag: Int, 
      x: Double, 
      y: Double, 
      width: Double, 
      height: Double,
      promise: Promise 
    ->
      try {
        val reactContext = appContext.reactContext as? ReactContext
          ?: return@AsyncFunction promise.reject(
            "NO_CONTEXT", 
            "React context not available", 
            null
          )
        
        // UI 스레드에서 실행
        reactContext.currentActivity?.runOnUiThread {
          try {
            // React Native의 UIManager를 통해 View 찾기
            val reactApplicationContext = reactContext as? com.facebook.react.bridge.ReactApplicationContext
            val uiManager = reactApplicationContext?.getNativeModule(UIManagerModule::class.java)
            
            if (uiManager == null) {
              promise.reject("NO_UI_MANAGER", "UIManagerModule not available", null)
              return@runOnUiThread
            }
            
            uiManager.addUIBlock { nativeViewHierarchyManager ->
              try {
                val view = nativeViewHierarchyManager.resolveView(viewTag) as? View
                  ?: run {
                    promise.reject("NO_VIEW", "View not found for tag: $viewTag", null)
                    return@addUIBlock
                  }
                
                // View를 Bitmap으로 캡처
                val bitmap = Bitmap.createBitmap(
                  view.width, 
                  view.height, 
                  Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                
                // 지정된 위치의 부분만 잘라내기
                val xInt = x.toInt().coerceAtLeast(0)
                val yInt = y.toInt().coerceAtLeast(0)
                val widthInt = width.toInt().coerceAtMost(bitmap.width - xInt)
                val heightInt = height.toInt().coerceAtMost(bitmap.height - yInt)
                
                val croppedBitmap = if (xInt < bitmap.width && yInt < bitmap.height && widthInt > 0 && heightInt > 0) {
                  Bitmap.createBitmap(bitmap, xInt, yInt, widthInt, heightInt)
                } else {
                  bitmap
                }
                
                // Bitmap을 파일로 저장하고 URI 반환
                val cacheDir = reactContext.cacheDir
                val file = File(cacheDir, "captured_background_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                  croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                
                val uri = FileProvider.getUriForFile(
                  reactContext,
                  "${reactContext.packageName}.fileprovider",
                  file
                )
                
                promise.resolve(uri.toString())
              } catch (e: Exception) {
                promise.reject("CAPTURE_ERROR", "Failed to capture view: ${e.message}", e)
              }
            }
          } catch (e: Exception) {
            promise.reject("CAPTURE_ERROR", "Failed to access UIManager: ${e.message}", e)
          }
        } ?: promise.reject("NO_ACTIVITY", "Activity not available", null)
      } catch (e: Exception) {
        promise.reject("CAPTURE_ERROR", "Failed to capture view: ${e.message}", e)
      }
    }

    // Enables the module to be used as a native view. Definition components that are accepted as part of
    // the view definition: Prop, Events.
    View(ExpoLiquidGlassNativeView::class) {
      // Defines a setter for the `url` prop.
      Prop("url") { view: ExpoLiquidGlassNativeView, url: URL ->
        view.webView.loadUrl(url.toString())
      }
      // Defines an event that the view can send to JavaScript.
      Events("onLoad")
    }

    // LiquidButton View
    View(LiquidButtonView::class) {
      Prop("title") { view: LiquidButtonView, title: String? ->
        view.updateProps(title = title)
      }
      Prop("enabled") { view: LiquidButtonView, enabled: Boolean ->
        view.updateProps(enabled = enabled)
      }
      Prop("tint") { view: LiquidButtonView, tint: String? ->
        view.updateProps(tint = tint)
      }
      Prop("surfaceColor") { view: LiquidButtonView, surfaceColor: String? ->
        view.updateProps(surfaceColor = surfaceColor)
      }
      Prop("blurRadius") { view: LiquidButtonView, blurRadius: Double ->
        view.updateProps(blurRadius = blurRadius.toFloat())
      }
      Prop("lensX") { view: LiquidButtonView, lensX: Double ->
        view.updateProps(lensX = lensX.toFloat())
      }
      Prop("lensY") { view: LiquidButtonView, lensY: Double ->
        view.updateProps(lensY = lensY.toFloat())
      }
      Prop("imageUri") { view: LiquidButtonView, imageUri: String? ->
        view.updateProps(imageUri = imageUri)
      }
      Prop("backgroundImageUri") { view: LiquidButtonView, backgroundImageUri: String? ->
        view.updateProps(backgroundImageUri = backgroundImageUri)
      }
      Prop("useRealtimeCapture") { view: LiquidButtonView, useRealtimeCapture: Boolean ->
        view.updateProps(useRealtimeCapture = useRealtimeCapture)
      }
      Prop("renderBackgroundContent") { view: LiquidButtonView, renderBackgroundContent: Boolean ->
        view.updateProps(renderBackgroundContent = renderBackgroundContent)
      }
      Events("onPress")
    }

    // BottomTabsContentView
    View(BottomTabsContentView::class) {
      Prop("selectedTabIndex") { view: BottomTabsContentView, index: Int ->
        view.updateProps(selectedTabIndex = index)
      }
      Prop("tabsCount") { view: BottomTabsContentView, count: Int ->
        view.updateProps(tabsCount = count)
      }
      Prop("tabLabels") { view: BottomTabsContentView, labels: List<String>? ->
        view.updateProps(tabLabels = labels)
      }
      Prop("tabIcons") { view: BottomTabsContentView, icons: List<String>? ->
        view.updateProps(tabIcons = icons)
      }
      Prop("iconTintEnabled") { view: BottomTabsContentView, enabled: Boolean ->
        view.updateProps(iconTintEnabled = enabled)
      }
      Events("onTabSelected")
    }
  }
}
