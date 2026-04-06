import ExpoModulesCore

public class ExpoLiquidGlassNativeModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoLiquidGlassNative")

    Constant("PI") {
      Double.pi
    }

    Events("onChange")

    Function("hello") {
      return "Hello world! 👋"
    }

    AsyncFunction("setValueAsync") { (value: String) in
      self.sendEvent("onChange", [
        "value": value
      ])
    }

    View(ExpoLiquidGlassNativeView.self) {
      Prop("tint") { (view: ExpoLiquidGlassNativeView, tint: String?) in
        view.applyTint(tint)
      }
      Prop("surfaceColor") { (view: ExpoLiquidGlassNativeView, surfaceColor: String?) in
        view.applySurfaceColor(surfaceColor)
      }
      Prop("cornerRadius") { (view: ExpoLiquidGlassNativeView, cornerRadius: Double?) in
        view.applyCornerRadius(cornerRadius)
      }
    }
  }
}
