import { NativeModule } from 'expo';
import { ExpoLiquidGlassNativeModuleEvents } from './ExpoLiquidGlassNative.types';
declare class ExpoLiquidGlassNativeModule extends NativeModule<ExpoLiquidGlassNativeModuleEvents> {
    PI: number;
    setValueAsync(value: string): Promise<void>;
    hello(): string;
}
declare const _default: typeof ExpoLiquidGlassNativeModule;
export default _default;
//# sourceMappingURL=ExpoLiquidGlassNativeModule.web.d.ts.map