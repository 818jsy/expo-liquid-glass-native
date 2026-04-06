import * as React from 'react';
import { requireNativeViewManager } from 'expo-modules-core';
import { Platform, StyleSheet, View } from 'react-native';

import { ExpoLiquidGlassNativeViewProps } from './ExpoLiquidGlassNative.types';
import { clearOverlayContent, setOverlayContent } from './overlayRegistry';

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
  const containerRef = React.useRef<View>(null);
  const overlayIdRef = React.useRef(`lg-overlay-${Math.random().toString(36).slice(2, 10)}`);
  const [overlayRect, setOverlayRect] = React.useState({ x: 0, y: 0, width: 0, height: 0 });

  const useSeparateOverlayWindow = Platform.OS === 'android' && useRealtimeCapture;

  React.useEffect(() => {
    if (!useSeparateOverlayWindow) {
      clearOverlayContent(overlayIdRef.current);
      return;
    }

    setOverlayContent(overlayIdRef.current, children);
    return () => {
      clearOverlayContent(overlayIdRef.current);
    };
  }, [children, useSeparateOverlayWindow]);

  React.useEffect(() => {
    if (!useSeparateOverlayWindow) {
      return;
    }

    let frame = 0;
    let mounted = true;

    const measureOverlayRect = () => {
      containerRef.current?.measureInWindow((x, y, width, height) => {
        if (!mounted) {
          return;
        }

        setOverlayRect((prev) =>
          prev.x === x && prev.y === y && prev.width === width && prev.height === height
            ? prev
            : { x, y, width, height }
        );
      });
      frame = requestAnimationFrame(measureOverlayRect);
    };

    measureOverlayRect();

    return () => {
      mounted = false;
      cancelAnimationFrame(frame);
    };
  }, [useSeparateOverlayWindow]);

  return (
    <View ref={containerRef} style={[styles.container, style]}>
      <NativeView
        {...nativeProps}
        tint={tint}
        surfaceColor={surfaceColor}
        blurRadius={blurRadius}
        lensX={lensX}
        lensY={lensY}
        cornerRadius={cornerRadius}
        useRealtimeCapture={useRealtimeCapture}
        renderInSeparateWindow={useSeparateOverlayWindow}
        overlayId={useSeparateOverlayWindow ? overlayIdRef.current : undefined}
        captureRectX={useSeparateOverlayWindow ? overlayRect.x : undefined}
        captureRectY={useSeparateOverlayWindow ? overlayRect.y : undefined}
        captureRectWidth={useSeparateOverlayWindow ? overlayRect.width : undefined}
        captureRectHeight={useSeparateOverlayWindow ? overlayRect.height : undefined}
        style={StyleSheet.absoluteFill}
      />
      {!useSeparateOverlayWindow ? (
        <View pointerEvents="box-none" style={styles.content}>
          {children}
        </View>
      ) : (
        <View pointerEvents="none" style={[styles.content, styles.placeholderContent]}>
          {children}
        </View>
      )}
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
  placeholderContent: {
    opacity: 0,
  },
});
