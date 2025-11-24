# Expo Liquid Glass Native

Native Liquid Glass UI components for React Native with Expo. Supports both **iOS** and **Android** platforms with native implementations.

## Installation

```bash
npm install expo-liquid-glass-native
```

## Setup

### 1. Install dependencies

```bash
npx expo install expo-dev-client
```

### 2. Configure app.json

Add the plugin to your `app.json`:

```json
{
  "expo": {
    "plugins": [
      "expo-liquid-glass-native/plugins/withLiquidGlass"
    ]
  }
}
```

### 3. Setup AndroidLiquidGlass dependency

This library requires the [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) project to be available. You need to:

1. Clone the AndroidLiquidGlass repository:
```bash
git clone https://github.com/Kyant0/AndroidLiquidGlass.git
```

2. Place it in your project root or configure the path in the plugin.

### 4. Rebuild native code

```bash
npx expo prebuild --clean
npx expo run:android
```

## Components

### LiquidButton

A glassmorphic button component with blur and lens effects.

```jsx
import { LiquidButton } from 'expo-liquid-glass-native';

<LiquidButton
  title="Click Me"
  onPress={() => console.log('Pressed')}
  tint="#0088FF"
  surfaceColor="#FFFFFF4D"
  blurRadius={2}
  lensX={12}
  lensY={24}
/>
```

#### Props

- `title` (string): Button text
- `enabled` (boolean): Whether the button is enabled (default: true)
- `onPress` (function): Callback when button is pressed
- `tint` (string): Tint color in hex format (e.g., "#0088FF")
- `surfaceColor` (string): Surface color in hex format with alpha (e.g., "#FFFFFF4D")
- `blurRadius` (number): Blur radius in dp (default: 2)
- `lensX` (number): Lens X radius in dp (default: 12)
- `lensY` (number): Lens Y radius in dp (default: 24)
- `style` (object): Style object for the button

### BottomTabs

A glassmorphic bottom tabs component.

```jsx
import { BottomTabs } from 'expo-liquid-glass-native';

<BottomTabs
  selectedTabIndex={0}
  tabsCount={4}
  tabLabels={['Home', 'Search', 'Profile', 'Settings']}
  tabIcons={[
    require('./assets/home.png'),
    require('./assets/search.png'),
    require('./assets/profile.png'),
    require('./assets/settings.png'),
  ]}
  iconTintEnabled={false}
  onTabSelected={(index) => console.log('Tab selected:', index)}
/>
```

#### Props

- `selectedTabIndex` (number): Currently selected tab index (default: 0)
- `tabsCount` (number): Number of tabs (default: 3)
- `tabLabels` (string[]): Array of tab labels (optional)
- `tabIcons` (number[]|string[]): Array of icon sources (require() or URI strings) (optional)
- `iconTintEnabled` (boolean): Whether to apply content color tint to icons (default: true)
- `onTabSelected` (function): Callback when a tab is selected: (index: number) => void
- `style` (object): Style for the container

## Platform Support

- ✅ **Android** - Full support with native Jetpack Compose
- 🚧 **iOS** - Coming soon

## Requirements

- React Native 0.81+
- Expo SDK 54+
- Android API 21+ (iOS support coming soon)
- AndroidLiquidGlass project (cloned locally, Android only)

## License

MIT

