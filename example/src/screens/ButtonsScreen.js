import React from 'react';
import { StyleSheet, ImageBackground, ScrollView, Dimensions, View } from 'react-native';
import LiquidButton from '../components/LiquidButton';

const wallpaperImage = require('../../assets/wallpaper_light.webp');
const { width: SCREEN_WIDTH, height: SCREEN_HEIGHT } = Dimensions.get('window');

const ButtonsScreen = () => {
  return (
    <View style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* 이미지 3장 위아래로 배치 */}
        <ImageBackground
          source={wallpaperImage}
          style={styles.imageSection}
          resizeMode="cover"
        />
        <ImageBackground
          source={wallpaperImage}
          style={styles.imageSection}
          resizeMode="cover"
        />
        <ImageBackground
          source={wallpaperImage}
          style={styles.imageSection}
          resizeMode="cover"
        />
      </ScrollView>
      
      {/* 버튼들은 배경 위에 고정 */}
      {/* 각 버튼은 네이티브에서 자동으로 뒤의 View를 캡처하여 사용 */}
      <View style={styles.buttonsContainer} pointerEvents="box-none">
        <LiquidButton
          title="Wallpaper Background"
          enabled={true}
          useRealtimeCapture={true}
          tint="transparent"
          onPress={() => {
            console.log('Wallpaper button pressed!');
          }}
          style={[styles.button, styles.button1]}
        />
        <LiquidButton
          title="Apple Background"
          enabled={true}
          useRealtimeCapture={true}
          surfaceColor="#00FFFFFF"
          onPress={() => {
            console.log('Apple button pressed!');
          }}
          style={[styles.button, styles.button2]}
        />
        <LiquidButton
          title="Icon Background"
          enabled={true}
          useRealtimeCapture={true}
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
          useRealtimeCapture={true}
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
          useRealtimeCapture={true}
          tint="#00FF88"
          surfaceColor="#00FFFFFF"
          onPress={() => {
            console.log('Adaptive icon button pressed!');
          }}
          style={[styles.button, styles.button5]}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'transparent',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
  },
  imageSection: {
    width: SCREEN_WIDTH,
    height: SCREEN_HEIGHT,
  },
  buttonsContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
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


