import React from 'react';
import { StyleSheet, View, ImageBackground } from 'react-native';
import LiquidButton from '../components/LiquidButton';

const ButtonsScreen = () => {
  return (
    <ImageBackground
      source={require('../../assets/wallpaper_light.webp')}
      style={styles.container}
      resizeMode="cover"
    >
      <View style={styles.buttonContainer}>
        <LiquidButton
          title="Transparent Liquid Button"
          enabled={true}
          onPress={() => {}}
          style={styles.button}
        />
        <LiquidButton
          title="Surface Liquid Button"
          enabled={true}
          surfaceColor="#FFFFFF4D"
          onPress={() => {}}
          style={styles.button}
        />
        <LiquidButton
          title="Tinted Liquid Button"
          enabled={true}
          tint="#0088FF"
          onPress={() => {}}
          style={styles.button}
        />
        <LiquidButton
          title="Tinted Liquid Button"
          enabled={true}
          tint="#FF8D28"
          onPress={() => {}}
          style={styles.button}
        />
      </View>
    </ImageBackground>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  buttonContainer: {
    width: '100%',
    gap: 16,
    alignItems: 'center',
  },
  button: {
    width: '100%',
    height: 48,
  },
});

export default ButtonsScreen;

