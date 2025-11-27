import React, { useState, useMemo } from 'react';
import { View, Image } from 'react-native';
import { BottomTabsContentView } from 'expo-liquid-glass-native';

const BottomTabsView = BottomTabsContentView;

/**
 * BottomTabs component that displays the native Android LiquidBottomTabs demo
 * 
 * @param {number} selectedTabIndex - Currently selected tab index (default: 0)
 * @param {number} tabsCount - Number of tabs (default: 3)
 * @param {string[]} tabLabels - Array of tab labels (optional)
 * @param {number[]|string[]} tabIcons - Array of icon sources (require() or URI strings) (optional)
 * @param {boolean} iconTintEnabled - Whether to apply content color tint to icons (default: true)
 * @param {function} onTabSelected - Callback when a tab is selected: (index: number) => void
 * @param {object} style - Style for the container
 */
const BottomTabs = ({ 
  selectedTabIndex: controlledSelectedTabIndex,
  tabsCount = 3,
  tabLabels,
  tabIcons,
  iconTintEnabled = true,
  onTabSelected,
  style,
  ...props 
}) => {
  const [internalSelectedTabIndex, setInternalSelectedTabIndex] = useState(0);
  
  const selectedTabIndex = controlledSelectedTabIndex !== undefined 
    ? controlledSelectedTabIndex 
    : internalSelectedTabIndex;
  
  const handleTabSelected = (event) => {
    const index = event.nativeEvent.index;
    if (controlledSelectedTabIndex === undefined) {
      setInternalSelectedTabIndex(index);
    }
    onTabSelected?.(index);
  };

  // Convert icon sources to URIs
  const tabIconUris = useMemo(() => {
    if (!tabIcons) return null;
    return tabIcons.map(icon => {
      if (typeof icon === 'number') {
        // require()로 로드된 이미지
        try {
          const source = Image.resolveAssetSource(icon);
          if (source?.uri) {
            // console.log('Resolved asset URI:', source.uri);
            // file:// URI를 그대로 사용
            return source.uri;
          }
          return null;
        } catch (e) {
          console.warn('Failed to resolve asset source:', e);
          return null;
        }
      } else if (typeof icon === 'string') {
        // 이미 URI 문자열이거나 drawable 리소스 이름
        return icon;
      }
      return null;
    });
  }, [tabIcons]);

  return (
    <BottomTabsView
      style={[{ flex: 1 }, style]}
      selectedTabIndex={selectedTabIndex}
      tabsCount={tabsCount}
      tabLabels={tabLabels}
      tabIcons={tabIconUris}
      iconTintEnabled={iconTintEnabled}
      onTabSelected={handleTabSelected}
      {...props}
    />
  );
};

export default BottomTabs;

