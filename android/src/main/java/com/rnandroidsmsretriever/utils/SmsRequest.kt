package com.rnandroidsmsretriever.utils

import com.facebook.react.bridge.Promise

data class SmsRequest(
  override val otpLength: Int,
  override val promise: Promise,
) : ConsentRequest(otpLength = otpLength, promise = promise)
