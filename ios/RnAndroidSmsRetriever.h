
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNRnAndroidSmsRetrieverSpec.h"

@interface RnAndroidSmsRetriever : NSObject <NativeRnAndroidSmsRetrieverSpec>
#else
#import <React/RCTBridgeModule.h>

@interface RnAndroidSmsRetriever : NSObject <RCTBridgeModule>
#endif

@end
