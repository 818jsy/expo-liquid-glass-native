import * as React from 'react';
import { requireNativeViewManager } from 'expo-modules-core';
import { StyleSheet, View } from 'react-native';
const NativeView = requireNativeViewManager('ExpoLiquidGlassNative');
export default function ExpoLiquidGlassNativeView(props) {
    const { children, style, ...nativeProps } = props;
    return (<View style={[styles.container, style]}>
      <NativeView {...nativeProps} style={StyleSheet.absoluteFill}/>
      <View pointerEvents="box-none" style={styles.content}>
        {children}
      </View>
    </View>);
}
const styles = StyleSheet.create({
    container: {
        position: 'relative',
        overflow: 'hidden',
    },
    content: {
        flexShrink: 0,
    },
});
//# sourceMappingURL=ExpoLiquidGlassNativeView.js.map