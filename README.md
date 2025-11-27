# expo-liquid-glass-native

Expo module for LiquidGlass native Android components with beautiful glassmorphism effects.

> **Note:** This library is an Expo adaptation of [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) by Kyant0. It provides native Android components with liquid glass effects for React Native/Expo applications. iOS support is planned for future releases.

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
- ✅ Register ViewManagers in `MainApplication.kt`

No manual native code configuration needed! 🎉

## Usage

### LiquidButton

```tsx
import { LiquidButtonView } from 'expo-liquid-glass-native';

function MyComponent() {
  return (
    <LiquidButtonView
      title="Button"
      enabled={true}
      tint="#0088FF"
      surfaceColor="#FFFFFF4D"
      blurRadius={2}
      onPress={() => {
        console.log('Button pressed!');
      }}
      style={{ width: 200, height: 50 }}
    />
  );
}
```

**Props:**
- `title?: string` - Button text
- `enabled?: boolean` - Whether the button is enabled (default: true)
- `tint?: string` - Tint color in hex format (e.g., "#0088FF")
- `surfaceColor?: string` - Surface color in hex format with alpha (e.g., "#FFFFFF4D")
- `blurRadius?: number` - Blur radius in dp (default: 2)
- `lensX?: number` - Lens X radius in dp (default: 12)
- `lensY?: number` - Lens Y radius in dp (default: 24)
- `onPress?: (event: { nativeEvent: {} }) => void` - Press event handler
- `style?: ViewStyle` - Style object

### BottomTabs

```tsx
import { BottomTabsContentView } from 'expo-liquid-glass-native';
import { Image } from 'react-native';

function MyTabs() {
  const [selectedTab, setSelectedTab] = useState(0);
  
  const tabIcons = [
    require('./assets/home.png'),
    require('./assets/search.png'),
    require('./assets/profile.png'),
  ];
  
  return (
    <BottomTabsContentView
      selectedTabIndex={selectedTab}
      tabsCount={3}
      tabLabels={['Home', 'Search', 'Profile']}
      tabIcons={tabIcons}
      iconTintEnabled={true}
      onTabSelected={(event) => {
        setSelectedTab(event.nativeEvent.index);
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
- `tabIcons?: (number | string)[]` - Array of icon sources (require() or URI strings)
- `iconTintEnabled?: boolean` - Whether to apply content color tint to icons (default: true)
- `onTabSelected?: (event: { nativeEvent: { index: number } }) => void` - Tab selection handler
- `style?: ViewStyle` - Style object

## Requirements

- **Android**: minSdkVersion 24
- **iOS**: Not yet implemented (PRs welcome!)
- **Expo SDK**: 54+

## License

MIT

## Contributing

PRs are welcome! Please open an issue first to discuss major changes.
