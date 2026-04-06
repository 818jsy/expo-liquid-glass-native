import * as React from 'react';
import { requireNativeViewManager } from 'expo-modules-core';
import { StyleSheet, View } from 'react-native';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

const NativeView: React.ComponentType<ExpoLiquidGlassNativeViewProps> =
  requireNativeViewManager('ExpoLiquidGlassNative');

export default function ExpoLiquidGlassNativeView(props: ExpoLiquidGlassNativeViewProps) {
  const {
    children,
    style,
    tint = '#FFFFFF',
    surfaceColor = '#14FFFFFF',
    blurRadius = 1,
    lensX = 28,
    lensY = 28,
    cornerRadius = 28,
    useRealtimeCapture = true,
    ...nativeProps
  } = props;

  return (
    <View style={[styles.container, style]}>
      <NativeView
        {...nativeProps}
        tint={tint}
        surfaceColor={surfaceColor}
        blurRadius={blurRadius}
        lensX={lensX}
        lensY={lensY}
        cornerRadius={cornerRadius}
        useRealtimeCapture={useRealtimeCapture}
        style={StyleSheet.absoluteFill}
      />
      <View pointerEvents="box-none" style={styles.content}>
        {children}
      </View>
    </View>
  );
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
