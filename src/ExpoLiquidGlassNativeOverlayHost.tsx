import * as React from 'react';
import { StyleSheet, View } from 'react-native';

import { getOverlayContent, subscribeToOverlay } from './overlayRegistry';

type OverlayHostProps = {
  overlayId?: string;
};

export default function ExpoLiquidGlassNativeOverlayHost({ overlayId }: OverlayHostProps) {
  const [, forceUpdate] = React.useReducer((value) => value + 1, 0);

  React.useEffect(() => {
    if (!overlayId) {
      return;
    }

    return subscribeToOverlay(overlayId, forceUpdate);
  }, [overlayId]);

  if (!overlayId) {
    return null;
  }

  return (
    <View style={styles.container} pointerEvents="box-none">
      {getOverlayContent(overlayId)}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
