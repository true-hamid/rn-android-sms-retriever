package com.rnandroidsmsretriever.utils

sealed class ConsentError(val code: String) {
  object ActivityNotFound: ConsentError(code = "ACTIVITY_NOT_FOUND")
  object Timeout: ConsentError(code = "CONSENT_TIMEOUT")
  object Denied: ConsentError(code = "CONSENT_DENIED")
}
