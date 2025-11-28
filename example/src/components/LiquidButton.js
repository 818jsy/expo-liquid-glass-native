import React from 'react';
import { LiquidButtonView } from 'expo-liquid-glass-native';

/**
 * LiquidButton component that wraps the native Android LiquidGlass button
 * 
 * @param {Object} props
 * @param {string} props.title - Button text
 * @param {boolean} props.enabled - Whether the button is enabled
 * @param {Function} props.onPress - Callback function when button is pressed
 * @param {string} props.tint - Tint color in hex format (e.g., "#0088FF")
 * @param {string} props.surfaceColor - Surface color in hex format with alpha (e.g., "#FFFFFF4D")
 * @param {number} props.blurRadius - Blur radius in dp (default: 2)
 * @param {number} props.lensX - Lens X radius in dp (default: 12)
 * @param {number} props.lensY - Lens Y radius in dp (default: 24)
 * @param {string} props.imageUri - URI of the background image (optional, deprecated - use backgroundImageUri)
 * @param {string} props.backgroundImageUri - URI of the background image for this specific button (optional)
 * @param {boolean} props.useRealtimeCapture - Use realtime screen capture instead of image (default: false)
 * @param {boolean} props.renderBackgroundContent - Render background content in Compose (default: false)
 * @param {Object} props.style - Style object for the button
 */
const LiquidButton = ({ 
  title = 'Button', 
  enabled = true, 
  onPress, 
  tint,
  surfaceColor,
  blurRadius = 2,
  lensX = 12,
  lensY = 24,
  imageUri,
  backgroundImageUri,
  useRealtimeCapture = false,
  renderBackgroundContent = false,
  style, 
  ...props 
}) => {
  const handlePress = (event) => {
    if (onPress) {
      onPress(event);
    }
  };

  return (
    <LiquidButtonView
      title={title}
      enabled={enabled}
      tint={tint}
      surfaceColor={surfaceColor}
      blurRadius={blurRadius}
      lensX={lensX}
      lensY={lensY}
      imageUri={imageUri}
      backgroundImageUri={backgroundImageUri}
      useRealtimeCapture={useRealtimeCapture}
      renderBackgroundContent={renderBackgroundContent}
      onPress={handlePress}
      style={style}
      {...props}
    />
  );
};

export default LiquidButton;

