import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View, ImageBackground } from 'react-native';
import { LiquidButton, BottomTabs } from 'expo-liquid-glass-native';
import { useState } from 'react';

export default function App() {
  const [selectedTab, setSelectedTab] = useState(0);

  return (
    <ImageBackground
      source={require('./assets/wallpaper_light.webp')}
      style={styles.container}
      resizeMode="cover"
    >
      <View style={styles.content}>
        <View style={styles.buttonContainer}>
          <LiquidButton
            title="Transparent Button"
            enabled={true}
            onPress={() => console.log('Transparent pressed')}
            style={styles.button}
          />
          <LiquidButton
            title="Surface Button"
            enabled={true}
            surfaceColor="#5FFFFFF"
            onPress={() => console.log('Surface pressed')}
            style={styles.button}
          />
          <LiquidButton
            title="Tinted Button"
            enabled={true}
            tint="#0088FF"
            onPress={() => console.log('Tinted pressed')}
            style={styles.button}
          />
        </View>

        <View style={styles.tabsContainer}>
          <BottomTabs
            selectedTabIndex={selectedTab}
            tabsCount={4}
            tabLabels={['Home', 'Search', 'Profile', 'Settings']}
            onTabSelected={(index) => {
              console.log('Tab selected:', index);
              setSelectedTab(index);
            }}
            style={styles.tabs}
          />
        </View>
      </View>
      <StatusBar style="auto" />
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    padding: 20,
  },
  buttonContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: 16,
  },
  button: {
    minWidth: 200,
    height: 48,
  },
  tabsContainer: {
    height: 100,
    marginBottom: 20,
  },
  tabs: {
    flex: 1,
  },
});
