import * as React from 'react';
import { View } from 'react-native';
export default function ExpoLiquidGlassNativeView(props) {
    const { children, style, tint, surfaceColor, blurRadius = 12, cornerRadius = 24, ...rest } = props;
    return (<View {...rest} style={[
            {
                overflow: 'hidden',
                borderRadius: cornerRadius,
                backgroundColor: surfaceColor ?? 'rgba(255,255,255,0.16)',
                backdropFilter: `blur(${blurRadius}px)`,
                WebkitBackdropFilter: `blur(${blurRadius}px)`,
                borderWidth: 1,
                borderColor: tint ?? 'rgba(255,255,255,0.18)',
            },
            style,
        ]}>
      {children}
    </View>);
}
//# sourceMappingURL=ExpoLiquidGlassNativeView.web.js.map