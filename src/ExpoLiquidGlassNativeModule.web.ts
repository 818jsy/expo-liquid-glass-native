import { registerWebModule, NativeModule } from 'expo';

import { ExpoLiquidGlassNativeModuleEvents } from './ExpoLiquidGlassNative.types';

class ExpoLiquidGlassNativeModule extends NativeModule<ExpoLiquidGlassNativeModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoLiquidGlassNativeModule, 'ExpoLiquidGlassNativeModule');
