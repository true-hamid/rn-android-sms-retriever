package com.rnandroidsmsretriever

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Callback

abstract class RnAndroidSmsRetrieverSpec internal constructor(context: ReactApplicationContext) :
  NativeRnAndroidSmsRetrieverSpec(context) {
  abstract fun getOtp(otpLength: Int, promise: Promise)

  abstract fun getSms(otpLength: Int,promise: Promise)
}
