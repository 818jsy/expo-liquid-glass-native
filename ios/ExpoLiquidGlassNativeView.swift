import ExpoModulesCore
import UIKit

class ExpoLiquidGlassNativeView: ExpoView {
  private let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .systemUltraThinMaterial))
  private let tintView = UIView()
  private let surfaceView = UIView()

  required init(appContext: AppContext? = nil) {
    super.init(appContext: appContext)
    clipsToBounds = true

    blurView.isUserInteractionEnabled = false
    tintView.isUserInteractionEnabled = false
    surfaceView.isUserInteractionEnabled = false

    addSubview(blurView)
    addSubview(tintView)
    addSubview(surfaceView)
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    blurView.frame = bounds
    tintView.frame = bounds
    surfaceView.frame = bounds
    sendSubviewToBack(surfaceView)
    sendSubviewToBack(tintView)
    sendSubviewToBack(blurView)
  }

  func applyTint(_ value: String?) {
    tintView.backgroundColor = UIColor.fromHex(value)?.withAlphaComponent(0.18)
  }

  func applySurfaceColor(_ value: String?) {
    surfaceView.backgroundColor = UIColor.fromHex(value) ?? UIColor.white.withAlphaComponent(0.16)
  }

  func applyCornerRadius(_ value: Double?) {
    layer.cornerRadius = CGFloat(value ?? 24)
  }
}

private extension UIColor {
  static func fromHex(_ value: String?) -> UIColor? {
    guard let value else {
      return nil
    }

    var input = value.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
    if input == "TRANSPARENT" {
      return .clear
    }
    if input.hasPrefix("#") {
      input.removeFirst()
    }

    let scanner = Scanner(string: input)
    var hexNumber: UInt64 = 0
    guard scanner.scanHexInt64(&hexNumber) else {
      return nil
    }

    switch input.count {
    case 6:
      return UIColor(
        red: CGFloat((hexNumber & 0xFF0000) >> 16) / 255,
        green: CGFloat((hexNumber & 0x00FF00) >> 8) / 255,
        blue: CGFloat(hexNumber & 0x0000FF) / 255,
        alpha: 1
      )
    case 8:
      return UIColor(
        red: CGFloat((hexNumber & 0xFF000000) >> 24) / 255,
        green: CGFloat((hexNumber & 0x00FF0000) >> 16) / 255,
        blue: CGFloat((hexNumber & 0x0000FF00) >> 8) / 255,
        alpha: CGFloat(hexNumber & 0x000000FF) / 255
      )
    default:
      return nil
    }
  }
}
