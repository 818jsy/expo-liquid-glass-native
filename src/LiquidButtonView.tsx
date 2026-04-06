import * as React from 'react';

import ExpoLiquidGlassNativeView from './ExpoLiquidGlassNativeView';
import type { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

export type LiquidButtonViewProps = ExpoLiquidGlassNativeViewProps;

// Backward-compatible alias. Prefer ExpoLiquidGlassNativeView for new code.
export default function LiquidButtonView(props: LiquidButtonViewProps) {
  return <ExpoLiquidGlassNativeView {...props} />;
}
