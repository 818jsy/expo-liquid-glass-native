import { registerWebModule, NativeModule } from 'expo';
class ExpoLiquidGlassNativeModule extends NativeModule {
    PI = Math.PI;
    async setValueAsync(value) {
        this.emit('onChange', { value });
    }
    hello() {
        return 'Hello world! 👋';
    }
}
export default registerWebModule(ExpoLiquidGlassNativeModule, 'ExpoLiquidGlassNativeModule');
//# sourceMappingURL=ExpoLiquidGlassNativeModule.web.js.map