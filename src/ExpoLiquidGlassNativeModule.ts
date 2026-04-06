import { NativeModule, requireNativeModule } from 'expo';

import { ExpoLiquidGlassNativeModuleEvents } from './ExpoLiquidGlassNative.types';

declare class ExpoLiquidGlassNativeModule extends NativeModule<ExpoLiquidGlassNativeModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoLiquidGlassNativeModule>('ExpoLiquidGlassNative');
