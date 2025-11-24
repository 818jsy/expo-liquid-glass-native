import React from 'react';
import { requireNativeComponent } from 'react-native';

const ButtonsContentView = requireNativeComponent('ButtonsContentView');

/**
 * ButtonsContent component that displays multiple LiquidButtons with shared backdrop
 * 
 * @param {Array<Object>} buttons - Array of button objects with properties:
 *   - title: string - Button text
 *   - enabled: boolean - Whether the button is enabled (default: true)
 *   - tint: string - Tint color in hex format (e.g., "#0088FF")
 *   - surfaceColor: string - Surface color in hex format with alpha (e.g., "#FFFFFF4D")
 *   - blurRadius: number - Blur radius in dp (default: 2)
 *   - lensX: number - Lens X radius in dp (default: 12)
 *   - lensY: number - Lens Y radius in dp (default: 24)
 * @param {function} onButtonPress - Callback when a button is pressed: (index: number) => void
 * @param {object} style - Style for the container
 */
const ButtonsContent = ({ 
  buttons = [],
  onButtonPress,
  style,
  ...props 
}) => {
  const handleButtonPress = (event) => {
    const index = event.nativeEvent.index;
    onButtonPress?.(index);
  };

  return (
    <ButtonsContentView
      style={[{ flex: 1 }, style]}
      buttons={buttons}
      onButtonPress={handleButtonPress}
      {...props}
    />
  );
};

export default ButtonsContent;

