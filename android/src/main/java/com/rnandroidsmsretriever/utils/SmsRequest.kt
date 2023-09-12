package com.rnandroidsmsretriever.utils

import com.facebook.react.bridge.Promise

data class SmsRequest(
  override val promise: Promise,
) : ConsentRequest(promise = promise)
