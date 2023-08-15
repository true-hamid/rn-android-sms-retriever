import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-android-sms-retriever' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const RnAndroidSmsRetrieverModule = isTurboModuleEnabled
  ? require('./NativeRnAndroidSmsRetriever').default
  : NativeModules.RnAndroidSmsRetriever;

const RnAndroidSmsRetriever = RnAndroidSmsRetrieverModule
  ? RnAndroidSmsRetrieverModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export async function getOtp (otpLength: number): Promise<number> {
  return await RnAndroidSmsRetriever.getOtp(otpLength);
}

export async function getSms (otpLength: number): Promise<number> {
  return await RnAndroidSmsRetriever.getSms(otpLength);
}
