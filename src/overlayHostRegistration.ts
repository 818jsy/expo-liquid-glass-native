let overlayHostRegistered = false;

export function ensureOverlayHostRegistered() {
  if (overlayHostRegistered) {
    return;
  }

  const { Platform, AppRegistry } = require('react-native');
  if (Platform.OS === 'web') {
    return;
  }

  const ExpoLiquidGlassNativeOverlayHost =
    require('./ExpoLiquidGlassNativeOverlayHost').default;

  AppRegistry.registerComponent(
    'ExpoLiquidGlassNativeOverlayHost',
    () => ExpoLiquidGlassNativeOverlayHost
  );
  overlayHostRegistered = true;
}
