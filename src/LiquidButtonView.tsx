import * as React from 'react';
import { requireNativeComponent, ViewProps } from 'react-native';

export type LiquidButtonViewProps = ViewProps & {
  title?: string;
  enabled?: boolean;
  onPress?: (event: { nativeEvent: {} }) => void;
  tint?: string;
  surfaceColor?: string;
  blurRadius?: number;
  lensX?: number;
  lensY?: number;
};

const NativeLiquidButtonView = requireNativeComponent<LiquidButtonViewProps>('LiquidButtonView');

export default function LiquidButtonView(props: LiquidButtonViewProps) {
  return <NativeLiquidButtonView {...props} />;
}

