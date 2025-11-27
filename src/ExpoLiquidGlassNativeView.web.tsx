import * as React from 'react';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';

export default function ExpoLiquidGlassNativeView(props: ExpoLiquidGlassNativeViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
