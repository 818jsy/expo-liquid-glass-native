import type { ViewProps } from 'react-native';

export type ExpoLiquidGlassNativeModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};

export type ExpoLiquidGlassNativeViewProps = ViewProps & {
  tint?: string;
  surfaceColor?: string;
  blurRadius?: number;
  lensX?: number;
  lensY?: number;
  cornerRadius?: number;
  imageUri?: string;
  backgroundImageUri?: string;
  useRealtimeCapture?: boolean;
  renderBackgroundContent?: boolean;
  renderInSeparateWindow?: boolean;
  overlayId?: string;
  captureRectX?: number;
  captureRectY?: number;
  captureRectWidth?: number;
  captureRectHeight?: number;
};
