package com.rnandroidsmsretriever.utils

import com.facebook.react.bridge.Promise

open class ConsentRequest(open val promise: Promise, open val otpLength: Int?=0) {
}
