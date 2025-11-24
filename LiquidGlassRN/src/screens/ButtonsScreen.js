import React from 'react';
import { StyleSheet, View } from 'react-native';
import ButtonsContent from '../components/ButtonsContent';

const ButtonsScreen = () => {
  const buttons = [
    {
      title: 'Transparent Liquid Button',
      enabled: true,
    },
    {
      title: 'Surface Liquid Button',
      enabled: true,
      surfaceColor: '#FFFFFF4D',
    },
    {
      title: 'Tinted Liquid Button',
      enabled: true,
      tint: '#0088FF',
    },
    {
      title: 'Tinted Liquid Button',
      enabled: true,
      tint: '#FF8D28',
    },
  ];

  return (
    <View style={styles.container}>
      <ButtonsContent
        buttons={buttons}
        onButtonPress={(index) => {
          console.log('Button pressed:', index);
        }}
        style={styles.content}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
  },
});

export default ButtonsScreen;

