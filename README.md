# expo-liquid-glass-native

Expo module for LiquidGlass native Android components with beautiful glassmorphism effects.

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

## What the plugin does

The config plugin automatically:

1. Adds Compose plugin to `settings.gradle` pluginManagement
2. Adds Compose plugin and dependencies to `app/build.gradle`
3. Registers ViewManagers in `MainApplication.kt`

No manual native code configuration needed! 🎉

## Troubleshooting

### Build errors

If you encounter build errors, try:

```bash
# Clean build
cd android
./gradlew clean
cd ..

# Rebuild
npx expo prebuild --clean
npx expo run:android
```

### View not found errors

Make sure you've:
1. Added the plugin to `app.json`
2. Run `npx expo prebuild --clean`
3. Rebuilt the app

## License

MIT

## Contributing

PRs are welcome! Please open an issue first to discuss major changes.
