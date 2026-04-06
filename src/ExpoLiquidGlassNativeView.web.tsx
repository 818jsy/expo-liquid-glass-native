import * as React from 'react';
import { View } from 'react-native';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

export default function ExpoLiquidGlassNativeView(props: ExpoLiquidGlassNativeViewProps) {
  const {
    children,
    style,
    tint = '#FFFFFF',
    surfaceColor = 'transparent',
    blurRadius = 4,
    cornerRadius = 28,
    ...rest
  } = props;

  return (
    <View
      {...rest}
      style={[
        {
          overflow: 'hidden',
          borderRadius: cornerRadius,
          backgroundColor: surfaceColor,
          backdropFilter: `blur(${blurRadius}px)`,
          WebkitBackdropFilter: `blur(${blurRadius}px)`,
          borderWidth: 1,
          borderColor: tint,
        } as never,
        style,
      ]}
    >
      {children}
    </View>
  );
}
