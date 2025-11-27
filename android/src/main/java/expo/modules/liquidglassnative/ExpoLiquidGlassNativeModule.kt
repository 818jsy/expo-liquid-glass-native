package expo.modules.liquidglassnative

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.net.URL

class ExpoLiquidGlassNativeModule : Module() {
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoLiquidGlassNative')` in JavaScript.
    Name("ExpoLiquidGlassNative")

    // Defines constant property on the module.
    Constant("PI") {
      Math.PI
    }

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      "Hello world! 👋"
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { value: String ->
      // Send an event to JavaScript.
      sendEvent("onChange", mapOf(
        "value" to value
      ))
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
