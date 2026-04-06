# expo-liquid-glass-native

Expo module for LiquidGlass native Android components with beautiful glassmorphism effects.

> **Note:** This library is an Expo adaptation of [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) by Kyant0. It provides native Android components with liquid glass effects for React Native/Expo applications. iOS support is planned for future releases.

In the current implementation, `ExpoLiquidGlassNativeView` is the primary component, and `LiquidButtonView` is a backward-compatible alias for the same underlying view.

## Demo

<div align="center">
  <img src="assets/demo.gif" width="150" alt="Demo" />
</div>

## Installation

### Step 1: Install the package

```bash
npm install expo-liquid-glass-native
```

or

```bash
yarn add expo-liquid-glass-native
```

### Step 2: Add plugin to `app.json`

Add the plugin to your `app.json` (or `app.config.js`):

```json
{
  "expo": {
    "plugins": [
      "expo-liquid-glass-native"
    ]
  }
}
```

### Step 3: Prebuild native code

Run prebuild to generate native code with the plugin applied:

```bash
npx expo prebuild --clean
```

**Note:** If you're using Expo Development Build, the prebuild will happen automatically when you run:

```bash
npx expo run:android
```

That's it! The config plugin will automatically:
- ✅ Add Compose plugin to `settings.gradle`
- ✅ Add Compose dependencies to `app/build.gradle`
- ✅ Add Kotlin JVM toolchain configuration for Java 17

No manual native code configuration needed! 🎉

## Usage

### ExpoLiquidGlassNativeView

For new code, prefer using `ExpoLiquidGlassNativeView` directly. It renders the glass surface natively and places React Native `children` as overlay content above it.

```tsx
import { ExpoLiquidGlassNativeView } from 'expo-liquid-glass-native';
import { Text, View } from 'react-native';

function MyGlassCard() {
  return (
    <ExpoLiquidGlassNativeView
      tint="transparent"
      surfaceColor="#22FFFFFF"
      blurRadius={8}
      lensX={24}
      lensY={24}
      cornerRadius={28}
      useRealtimeCapture={true}
      style={{ width: 240, padding: 20, borderRadius: 28 }}
    >
      <View>
        <Text style={{ fontSize: 18, fontWeight: '600' }}>Liquid Glass</Text>
        <Text style={{ marginTop: 6 }}>Overlay content rendered above the blur.</Text>
      </View>
    </ExpoLiquidGlassNativeView>
  );
}
```

### LiquidButton

#### Basic Usage

```tsx
import { LiquidButtonView } from 'expo-liquid-glass-native';
import { Text, View } from 'react-native';

function MyComponent() {
  return (
    <LiquidButtonView
      tint="#0088FF"
      surfaceColor="#FFFFFF4D"
      blurRadius={8}
      cornerRadius={24}
      style={{ width: 200, paddingVertical: 14, borderRadius: 24 }}
    >
      <View style={{ alignItems: 'center' }}>
        <Text style={{ color: '#111', fontWeight: '600' }}>Button</Text>
      </View>
    </LiquidButtonView>
  );
}
```

#### With Realtime Background Capture

The `useRealtimeCapture` prop allows the button to capture the screen content behind it in real-time, creating a beautiful glassmorphism effect that reflects the actual background:

```tsx
import { LiquidButtonView } from 'expo-liquid-glass-native';
import { ScrollView, ImageBackground, Text, View } from 'react-native';

function MyComponent() {
  return (
    <ScrollView>
      <ImageBackground source={require('./assets/wallpaper.jpg')}>
        <LiquidButtonView
          useRealtimeCapture={true}
          tint="transparent"
          surfaceColor="#00FFFFFF"
          blurRadius={8}
          cornerRadius={24}
          style={{ width: 200, paddingVertical: 14, borderRadius: 24 }}
        >
          <View style={{ alignItems: 'center' }}>
            <Text style={{ color: '#111', fontWeight: '600' }}>Glass Button</Text>
          </View>
        </LiquidButtonView>
      </ImageBackground>
    </ScrollView>
  );
}
```

**Props:**
- `tint?: string` - Tint color in hex format (e.g., "#0088FF" or "transparent")
- `surfaceColor?: string` - Surface color in hex format with alpha (e.g., "#FFFFFF4D" or "#00FFFFFF" for transparent)
- `blurRadius?: number` - Blur radius in dp
- `lensX?: number` - Lens X radius in dp (default: 12)
- `lensY?: number` - Lens Y radius in dp (default: 24)
- `cornerRadius?: number` - Corner radius in dp
- `imageUri?: string` - URI of the background image (deprecated, use `backgroundImageUri` instead)
- `backgroundImageUri?: string` - URI of the background image for this specific button
- `useRealtimeCapture?: boolean` - Use realtime screen capture instead of image. Captures the entire screen behind the button (default: false)
- `renderBackgroundContent?: boolean` - Render background content in Compose (default: false)
- `children?: React.ReactNode` - Overlay content rendered above the native glass surface
- `style?: ViewStyle` - Style object

**Note:** `LiquidButtonView` is currently just a backward-compatible alias of `ExpoLiquidGlassNativeView`.

**Note:** When `useRealtimeCapture` is enabled, the component captures the screen content behind it in real time. Overlay `children` are excluded from the blurred backdrop so foreground content stays sharp.

### BottomTabs

```tsx
import { BottomTabsContentView } from 'expo-liquid-glass-native';
import { Image } from 'react-native';
import { useState, useMemo } from 'react';

function MyTabs() {
  const [selectedTab, setSelectedTab] = useState(0);
  
  // Convert require() images to URIs
  const tabIcons = useMemo(() => {
    const icons = [
      require('./assets/home.png'),
      require('./assets/search.png'),
      require('./assets/profile.png'),
    ];
    
    return icons.map(icon => {
      try {
        const source = Image.resolveAssetSource(icon);
        return source?.uri || null;
      } catch (e) {
        console.warn('Failed to resolve asset source:', e);
        return null;
      }
    }).filter(Boolean);
  }, []);
  
  return (
    <BottomTabsContentView
      selectedTabIndex={selectedTab}
      tabsCount={3}
      tabLabels={['Home', 'Search', 'Profile']}
      tabIcons={tabIcons}
      iconTintEnabled={true}
      onTabSelected={(event) => {
        setSelectedTab(event.nativeEvent.index);
        console.log('Tab selected:', event.nativeEvent.index);
      }}
      style={{ flex: 1 }}
    />
  );
}
```

**Props:**
- `selectedTabIndex?: number` - Currently selected tab index (default: 0)
- `tabsCount?: number` - Number of tabs (default: 3)
- `tabLabels?: string[]` - Array of tab labels
- `tabIcons?: string[]` - Array of icon URIs (use `Image.resolveAssetSource()` to convert require() images to URIs)
- `iconTintEnabled?: boolean` - Whether to apply content color tint to icons (default: true)
- `onTabSelected?: (event: { nativeEvent: { index: number } }) => void` - Tab selection handler
- `style?: ViewStyle` - Style object

**Note:** `tabIcons` should be an array of URI strings. Use `Image.resolveAssetSource()` to convert `require()` images to URIs.

## Features

- ✨ **Beautiful Glassmorphism Effects** - Native Android components with liquid glass effects
- 🎨 **Realtime Background Capture** - Capture screen content behind the component in real time
- 🧩 **Overlay Children Support** - Render React Native children above the native glass layer
- 📱 **ScrollView Compatible** - Works with ScrollViews and other scrollable containers
- 🎯 **Customizable** - Adjust blur radius, lens size, colors, and more
- 🚀 **Performance Optimized** - Hardware-accelerated rendering with efficient caching

## Requirements

- **Android**: minSdkVersion 24
- **iOS**: Not yet implemented (PRs welcome!)
- **Expo SDK**: 54+

## Examples

Check out the [example](./example) directory for complete usage examples, including:
- Basic button usage
- Buttons with realtime background capture
- Bottom tabs navigation
- ScrollView integration

## Performance Tips

1. **Realtime Capture**: Use `useRealtimeCapture` sparingly. Consider using static `backgroundImageUri` when possible.
2. **Blur Radius**: Lower blur radius values perform better than higher values.
3. **Caching**: The library automatically caches captured bitmaps for better performance.

## Known Limitations

- iOS support is planned for future releases
- Realtime capture may have slight performance impact on lower-end devices
- Some complex Android view hierarchies may not capture exactly as expected

## License

See [LICENSE](LICENSE) for details.

## Contributing

PRs are welcome! Please open an issue first to discuss major changes before submitting a pull request.
