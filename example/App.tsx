import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View } from 'react-native';
import HomeScreen from './src/screens/HomeScreen.js';
import ButtonsScreen from './src/screens/ButtonsScreen.js';
import BottomTabsScreen from './src/screens/BottomTabsScreen.js';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState('Home');

  const handleNavigate = (screen: string) => {
    setCurrentScreen(screen);
  };

  const handleBack = () => {
    setCurrentScreen('Home');
  };

  return (
    <View style={styles.container}>
      {currentScreen === 'Home' && <HomeScreen onNavigate={handleNavigate} />}
      {currentScreen === 'Buttons' && <ButtonsScreen onBack={handleBack} />}
      {currentScreen === 'BottomTabs' && <BottomTabsScreen onBack={handleBack} />}
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
