import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getOtp(otpLength: number): Promise<number>;
  getSms(): Promise<number>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RnAndroidSmsRetriever');
