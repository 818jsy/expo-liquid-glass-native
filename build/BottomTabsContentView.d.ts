import * as React from 'react';
import { ViewProps } from 'react-native';
export type BottomTabsContentViewProps = ViewProps & {
    selectedTabIndex?: number;
    tabsCount?: number;
    tabLabels?: string[];
    tabIcons?: string[];
    iconTintEnabled?: boolean;
    onTabSelected?: (event: {
        nativeEvent: {
            index: number;
        };
    }) => void;
};
export default function BottomTabsContentView(props: BottomTabsContentViewProps): React.JSX.Element;
//# sourceMappingURL=BottomTabsContentView.d.ts.map