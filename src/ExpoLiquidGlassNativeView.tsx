import * as React from 'react';
import { requireNativeViewManager } from 'expo-modules-core';
import { StyleSheet, View } from 'react-native';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

const NativeView: React.ComponentType<ExpoLiquidGlassNativeViewProps> =
  requireNativeViewManager('ExpoLiquidGlassNative');

export default function ExpoLiquidGlassNativeView(props: ExpoLiquidGlassNativeViewProps) {
  const { children, style, ...nativeProps } = props;

  return (
    <View style={[styles.container, style]}>
      <NativeView {...nativeProps} style={StyleSheet.absoluteFill} />
      <View pointerEvents="box-none" style={StyleSheet.absoluteFill}>
        {children}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
  },
});
