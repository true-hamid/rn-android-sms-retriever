import React from 'react';
import { useEffect, useState } from 'react';
import { StyleSheet, View, Text, Button, Pressable } from 'react-native';
import { getOtp, getSms, SMSRetrieverErrors } from 'rn-android-sms-retriever';

enum Tabs {
  Otp = 'Otp',
  Full_SMS = 'SMS',
}
export default function App() {
  const [activeTab, setActiveTab] = useState(Tabs.Otp);
  const [content, setContent] = useState('');
  const [reinitializeVisible, setReinitializeVisible] = useState(false);

  useEffect(() => {
    startReceiver();
  }, [activeTab]);

  const startReceiver = () => {
    activeTab === Tabs.Otp ? startOtp() : startSms();
  };

  const startOtp = async () => {
    setReinitializeVisible(false);
    try {
      const result = await getOtp(6);
      setContent(result.toString());
      setReinitializeVisible(true);
    } catch (e: any) {
      if (e.toString().includes(SMSRetrieverErrors.REGEX_MISMATCH)) {
        startOtp();
      }
    }
  };

  const startSms = async () => {
    setReinitializeVisible(false);
    try {
      const result = await getSms();
      setContent(result.toString());
      setReinitializeVisible(true);
    } catch (e) {
      console.log(e);
    }
  };

  const Tab = ({ title, id }: { title: string; id: Tabs }) => {
    const styles = getTabsStyles({ activeTab, id });

    return (
      <Pressable
        onPress={() => {
          setActiveTab(id);
          setContent('');
        }}
        style={styles.tabPressable}
      >
        <Text style={styles.title}>{title}</Text>
      </Pressable>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.tabsContainer}>
        <Tab title="Read OTP" id={Tabs.Otp} />
        <Tab title="Read SMS" id={Tabs.Full_SMS} />
      </View>

      <View style={styles.sectionContainer}>
        {reinitializeVisible && (
          <Button title="Reinitialize Receiver" onPress={startReceiver} />
        )}

        <Text style={styles.title}>
          {content ? `Received ${activeTab}` : `Waiting for ${activeTab}`}
        </Text>
        <View style={styles.descriptionContainer}>
          {content && <Text style={styles.description}>{`${content}`}</Text>}
        </View>
      </View>
    </View>
  );
}

const getTabsStyles = ({ activeTab, id }: { activeTab: Tabs; id: Tabs }) =>
  StyleSheet.create({
    tabPressable: {
      borderBottomColor: '#000',
      borderBottomWidth: activeTab == id ? 1 : 0,
    },
    title: { fontSize: 32 },
  });

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16 },
  tabsContainer: {
    flexDirection: 'row',
    alignItems: 'stretch',
    justifyContent: 'space-evenly',
  },
  sectionContainer: { margin: 16 },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: '#000',
  },
  descriptionContainer: {
    borderWidth: 1,
    borderColor: '#021',
    marginTop: 16,
    padding: 16,
    minHeight: 200,
  },
  description: {
    marginTop: 64,
    fontSize: 18,
    fontWeight: '400',
  },
});
