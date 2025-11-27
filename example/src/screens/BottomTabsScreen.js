import React, { useState } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import BottomTabs from '../components/BottomTabs';

const BottomTabsScreen = () => {
  const [selectedTabIndex, setSelectedTabIndex] = useState(0);
  const tabLabels = ['Home', 'Search', 'Profile', 'Settings'];
  // assets 폴더의 아이콘 사용 예시
  // 방법 1: require()로 assets 폴더의 이미지 사용
  const tabIcons = [
    require('../../assets/apple.png'),
    require('../../assets/apple.png'),
    require('../../assets/apple.png'),
    require('../../assets/apple.png'),
  ];
  // 방법 2: drawable 리소스 이름 사용 (기존 방식)
  // const tabIcons = ['flight_40px', 'flight_40px', 'flight_40px', 'flight_40px'];

  return (
    <View style={styles.container}>
      {/* 배경용 네이티브 뷰 (투명하게) */}
      <BottomTabs
        selectedTabIndex={selectedTabIndex}
        tabsCount={4}
        tabLabels={tabLabels}
        tabIcons={tabIcons}
        iconTintEnabled={false}
        onTabSelected={(index) => setSelectedTabIndex(index)}
        style={styles.backgroundView}
      />
      
      {/* 중앙에 선택된 탭 정보 표시 */}
      <View style={styles.centerContent} pointerEvents="none">
        <Text style={styles.selectedTabText}>
          Selected Tab: {tabLabels[selectedTabIndex] || `Tab ${selectedTabIndex + 1}`}
        </Text>
        <Text style={styles.selectedTabIndex}>
          Index: {selectedTabIndex}
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  backgroundView: {
    ...StyleSheet.absoluteFillObject,
  },
  centerContent: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1,
  },
  selectedTabText: {
    fontSize: 24,
    fontWeight: '600',
    color: '#000',
    marginBottom: 8,
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  selectedTabIndex: {
    fontSize: 18,
    color: '#666',
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 6,
  },
});

export default BottomTabsScreen;


