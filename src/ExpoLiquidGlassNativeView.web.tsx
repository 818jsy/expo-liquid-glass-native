import * as React from 'react';
import { View } from 'react-native';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

export default function ExpoLiquidGlassNativeView(props: ExpoLiquidGlassNativeViewProps) {
  const {
    children,
    style,
    tint,
    surfaceColor,
    blurRadius = 12,
    cornerRadius = 24,
    ...rest
  } = props;

  return (
    <View
      {...rest}
      style={[
        {
          overflow: 'hidden',
          borderRadius: cornerRadius,
          backgroundColor: surfaceColor ?? 'rgba(255,255,255,0.16)',
          backdropFilter: `blur(${blurRadius}px)`,
          WebkitBackdropFilter: `blur(${blurRadius}px)`,
          borderWidth: 1,
          borderColor: tint ?? 'rgba(255,255,255,0.18)',
        } as never,
        style,
      ]}
    >
      {children}
    </View>
  );
}
