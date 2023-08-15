import React from 'react';
import { useEffect, useState } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import { NativeModules } from 'react-native';
const { RnAndroidSmsRetriever } = NativeModules

export default function App() {
  const [otp, setOtp] = useState("");

  const getOtp = async () => {
    try {
      const result = await RnAndroidSmsRetriever.getSms(6);
      setOtp(result);
    } catch (e){
      console.error(e);
    }
  }

  useEffect(() => {
    getOtp();
  },[]);

  return (
    <View style={{flex: 1}}>
      <Text style = { styles.sectionTitle}>SMS Retriever</Text>
      { otp != "" ? <Text style = { styles.sectionDescription}>Received {otp}</Text> : <Text style = { styles.sectionDescription}>Waiting for otp</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 28,
    fontWeight: '700',
    textAlign: 'center',
    padding: 24,
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
