import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

const NativeView: React.ComponentType<ExpoLiquidGlassNativeViewProps> =
  requireNativeView('ExpoLiquidGlassNative');

export default function ExpoLiquidGlassNativeView(props: ExpoLiquidGlassNativeViewProps) {
  return <NativeView {...props} />;
}
