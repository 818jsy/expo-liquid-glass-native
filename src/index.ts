import { AppRegistry, Platform } from 'react-native';

import ExpoLiquidGlassNativeOverlayHost from './ExpoLiquidGlassNativeOverlayHost';

if (Platform.OS !== 'web') {
  AppRegistry.registerComponent(
    'ExpoLiquidGlassNativeOverlayHost',
    () => ExpoLiquidGlassNativeOverlayHost
  );
}

// Reexport the native module. On web, it will be resolved to ExpoLiquidGlassNativeModule.web.ts
// and on native platforms to ExpoLiquidGlassNativeModule.ts
// Note: Default export is optional - only exported if module is available
export { default as ExpoLiquidGlassNativeModule } from './ExpoLiquidGlassNativeModule';
export { default as ExpoLiquidGlassNativeView } from './ExpoLiquidGlassNativeView';
export { default as LiquidButtonView } from './LiquidButtonView';
export { default as BottomTabsContentView } from './BottomTabsContentView';
export * from  './ExpoLiquidGlassNative.types';
