import { NativeModules } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-android-sms-retriever' doesn't seem to be linked. Make sure: \n\n` +
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

export async function getOtp(otpLength: number): Promise<number> {
  return await RnAndroidSmsRetriever.getOtp(otpLength);
}

export async function getSms(): Promise<number> {
  return await RnAndroidSmsRetriever.getSms();
}

export enum SMSRetrieverErrors {
  ACTIVITY_NOT_FOUND = 'ACTIVITY_NOT_FOUND',
  CONSENT_TIMEOUT = 'CONSENT_TIMEOUT',
  CONSENT_DENIED = 'CONSENT_DENIED',
  RECEIVER_EXCEPTION = 'RECEIVER_EXCEPTION',
  REGEX_MISMATCH = 'REGEX_MISMATCH',
}
