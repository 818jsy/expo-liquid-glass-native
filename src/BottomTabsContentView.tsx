import * as React from 'react';
import { requireNativeComponent, ViewProps } from 'react-native';

export type BottomTabsContentViewProps = ViewProps & {
  selectedTabIndex?: number;
  tabsCount?: number;
  tabLabels?: string[];
  tabIcons?: string[];
  iconTintEnabled?: boolean;
  onTabSelected?: (event: { nativeEvent: { index: number } }) => void;
};

const NativeBottomTabsContentView = requireNativeComponent<BottomTabsContentViewProps>('BottomTabsContentView');

export default function BottomTabsContentView(props: BottomTabsContentViewProps) {
  return <NativeBottomTabsContentView {...props} />;
}

