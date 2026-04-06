import { NativeModule } from 'expo';
import { ExpoLiquidGlassNativeModuleEvents } from './ExpoLiquidGlassNative.types';
declare class ExpoLiquidGlassNativeModule extends NativeModule<ExpoLiquidGlassNativeModuleEvents> {
    PI: number;
    hello(): string;
    setValueAsync(value: string): Promise<void>;
}
declare const _default: ExpoLiquidGlassNativeModule;
export default _default;
//# sourceMappingURL=ExpoLiquidGlassNativeModule.d.ts.map