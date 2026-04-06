import React from 'react';
import { ExpoLiquidGlassNativeView } from 'expo-liquid-glass-native';
import { StyleSheet, Text, View } from 'react-native';

const LiquidButton = ({ 
  title = 'Button',
  tint,
  surfaceColor,
  blurRadius = 8,
  lensX = 24,
  lensY = 24,
  imageUri,
  backgroundImageUri,
  useRealtimeCapture = false,
  renderBackgroundContent = false,
  style,
  ...props
}) => {
  return (
    <ExpoLiquidGlassNativeView
      tint={tint}
      surfaceColor={surfaceColor}
      blurRadius={blurRadius}
      lensX={lensX}
      lensY={lensY}
      imageUri={imageUri}
      backgroundImageUri={backgroundImageUri}
      useRealtimeCapture={useRealtimeCapture}
      renderBackgroundContent={renderBackgroundContent}
      style={style}
      {...props}
    >
      <View style={styles.content}>
        <Text style={styles.label}>{title}</Text>
      </View>
    </ExpoLiquidGlassNativeView>
  );
};

const styles = StyleSheet.create({
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    color: '#111',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default LiquidButton;
