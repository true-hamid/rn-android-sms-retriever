package com.rnandroidsmsretriever

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

abstract class RnAndroidSmsRetrieverSpec internal constructor(context: ReactApplicationContext) :
  ReactContextBaseJavaModule(context){
    abstract fun getOtp(otpLength: Int, promise: Promise)

    abstract fun getSms(otpLength: Int,promise: Promise)
  }
