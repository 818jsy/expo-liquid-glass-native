import React from 'react';
import { StyleSheet, View, Image } from 'react-native';
import LiquidButton from '../components/LiquidButton';

const wallpaperImage = require('../../assets/wallpaper_light.webp');
const appleImage = require('../../assets/apple.png');
const iconImage = require('../../assets/icon.png');
const splashImage = require('../../assets/splash-icon.png');
const adaptiveIconImage = require('../../assets/adaptive-icon.png');

const ButtonsScreen = () => {
  const wallpaperUri = Image.resolveAssetSource(wallpaperImage)?.uri;
  const appleUri = Image.resolveAssetSource(appleImage)?.uri;
  const iconUri = Image.resolveAssetSource(iconImage)?.uri;
  const splashUri = Image.resolveAssetSource(splashImage)?.uri;
  const adaptiveIconUri = Image.resolveAssetSource(adaptiveIconImage)?.uri;

  return (
    <View style={styles.container}>
      {/* 각 버튼마다 다른 배경 이미지 사용 */}
      <LiquidButton
        title="Wallpaper Background"
        enabled={true}
        backgroundImageUri={wallpaperUri}
        tint="transparent"
        onPress={() => {
          console.log('Wallpaper button pressed!');
        }}
        style={[styles.button, styles.button1]}
      />
      <LiquidButton
        title="Apple Background"
        enabled={true}
        backgroundImageUri={appleUri}
        surfaceColor="#00FFFFFF"
        onPress={() => {
          console.log('Apple button pressed!');
        }}
        style={[styles.button, styles.button2]}
      />
      <LiquidButton
        title="Icon Background"
        enabled={true}
        backgroundImageUri={iconUri}
        tint="#0088FF"
        surfaceColor="#00FFFFFF"
        onPress={() => {
          console.log('Icon button pressed!');
        }}
        style={[styles.button, styles.button3]}
      />
      <LiquidButton
        title="Splash Background"
        enabled={true}
        backgroundImageUri={splashUri}
        tint="#FF8D28"
        surfaceColor="#00FFFFFF"
        onPress={() => {
          console.log('Splash button pressed!');
        }}
        style={[styles.button, styles.button4]}
      />
      
      {/* Adaptive Icon Background */}
      <LiquidButton
        title="Adaptive Icon Background"
        enabled={true}
        backgroundImageUri={adaptiveIconUri}
        tint="#00FF88"
        surfaceColor="#00FFFFFF"
        onPress={() => {
          console.log('Adaptive icon button pressed!');
        }}
        style={[styles.button, styles.button5]}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'transparent',
  },
  button: {
    position: 'absolute',
    width: '90%',
    maxWidth: 500,
    minWidth: 350,
    height: 64,
    left: '50%',
    marginLeft: -175, // approximate half of minWidth
  },
  button1: {
    top: '10%',
  },
  button2: {
    top: '25%',
  },
  button3: {
    top: '40%',
  },
  button4: {
    top: '55%',
  },
  button5: {
    top: '70%',
  },
});

export default ButtonsScreen;


