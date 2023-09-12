package com.rnandroidsmsretriever.utils

import com.facebook.react.bridge.Promise

data class OTPRequest(
  override val promise: Promise,
  override val otpLength: Int,
) : ConsentRequest(promise = promise, otpLength = otpLength)
