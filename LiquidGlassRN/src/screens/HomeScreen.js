import React from 'react';
import { StyleSheet, Text, View, TouchableOpacity, ScrollView } from 'react-native';

const HomeScreen = ({ onNavigate }) => {
  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Backdrop Catalog</Text>
      
      <View style={styles.section}>
        <Text style={styles.subtitle}>Liquid glass components</Text>
        <TouchableOpacity style={styles.listItem} onPress={() => onNavigate('Buttons')}>
          <Text style={styles.listItemText}>Buttons</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.listItem} onPress={() => onNavigate('BottomTabs')}>
          <Text style={styles.listItemText}>Bottom tabs</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#E8E8E8',
  },
  title: {
    fontSize: 28,
    fontWeight: '500',
    color: '#000',
    padding: 16,
    paddingTop: 40,
    paddingBottom: 16,
  },
  section: {
    marginTop: 8,
  },
  subtitle: {
    fontSize: 15,
    fontWeight: '500',
    color: '#0088FF',
    paddingHorizontal: 16,
    paddingTop: 24,
    paddingBottom: 8,
  },
  listItem: {
    padding: 16,
  },
  listItemText: {
    fontSize: 17,
    color: '#000',
  },
});

export default HomeScreen;

