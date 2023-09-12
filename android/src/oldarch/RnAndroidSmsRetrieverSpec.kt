package com.rnandroidsmsretriever

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.Promise

abstract class RnAndroidSmsRetrieverSpec internal constructor(context: ReactApplicationContext) :
  ReactContextBaseJavaModule(context) {

  abstract fun getOtp(otpLength: Int, promise: Promise)

  abstract fun getSms(promise: Promise)

}
