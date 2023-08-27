package com.rnandroidsmsretriever

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.rnandroidsmsretriever.utils.ConsentError
import com.rnandroidsmsretriever.utils.ConsentRequest
import com.rnandroidsmsretriever.utils.Logger
import com.rnandroidsmsretriever.utils.OTPRequest
import com.rnandroidsmsretriever.utils.SmsRequest
import java.util.regex.Matcher
import java.util.regex.Pattern

class RnAndroidSmsRetrieverModule internal constructor(
  private val reactContext: ReactApplicationContext
) : RnAndroidSmsRetrieverSpec(reactContext), ActivityEventListener {
  var TAG = "SMS_RETRIEVER"
  private lateinit var consentRequest: ConsentRequest
  private val SMS_CONSENT_REQUEST = 22071998

  override fun getName(): String {
    return NAME
  }

  init {
    reactContext.addActivityEventListener(this)
  }

  private val smsVerificationReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
        val extras = intent.extras
        val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

        when (smsRetrieverStatus.statusCode) {
          CommonStatusCodes.SUCCESS -> {
            // Get consent intent
            val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
            try {
              reactContext.startActivityForResult(
                consentIntent,
                SMS_CONSENT_REQUEST,
                null,
              )
            } catch (e: ActivityNotFoundException) {
              consentRequest.promise.reject(
                e.localizedMessage,
                ConsentError.ActivityNotFound.code,
                Throwable(ConsentError.ActivityNotFound.code)
              )
            }
          }

          CommonStatusCodes.TIMEOUT -> {
            // Time out occurred, handle the error.
            consentRequest.promise.reject(
              "Timeout, no message received!",
              ConsentError.Timeout.code,
              Throwable(ConsentError.Timeout.code)
            )
          }
        }
      }
    }
  }

  override fun onActivityResult(
    activity: Activity?, requestCode: Int, resultCode: Int, intent: Intent?
  ) {
    when (requestCode) {
      SMS_CONSENT_REQUEST -> if (resultCode == Activity.RESULT_OK && intent != null) {
        // Get SMS message content
        val message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
        when (consentRequest) {
          is OTPRequest -> {
            val requiredOtpLength = consentRequest.otpLength
            val p: Pattern = Pattern.compile("(\\d{$requiredOtpLength})")
            val m: Matcher? = message?.let { p.matcher(it) }
            Logger.debug(TAG, "onActivityResult!: $message")
            m?.find()?.let {
              if (it) {
                Logger.debug(TAG, "onActivityResult_match: $m")

                consentRequest.promise.resolve(m.group(0) as String)

              } else {
                Logger.debug(TAG, "onActivityResult_no_match: $m")
                consentRequest.promise.reject(
                  "The message received doesn't include the otp length requested",
                  ConsentError.RegexMismatch.code,
                  Throwable(ConsentError.RegexMismatch.code),
                )
              }
            }
          }

          is SmsRequest -> {
            Logger.debug(TAG, "onActivityResult_SmsRequest: $message")
            consentRequest.promise.resolve(message)
          }
        }
      } else {
        Logger.debug(TAG, "onActivityResult_match: kkk")

        consentRequest.promise.reject(
          "Consent denied by user",
          ConsentError.Denied.code,
          Throwable(ConsentError.Denied.code),
        )
      }
    }
  }

  override fun onNewIntent(p0: Intent?) {

  }

  @ReactMethod
  override fun getOtp(otpLength: Int, promise: Promise) {
    // We firstly assign our consent request
    this.consentRequest = OTPRequest(
      promise = promise,
      otpLength = otpLength,
    )
    // Then initialize the UserConsent
    initializeConsent()
  }

  @ReactMethod
  override fun getSms(promise: Promise) {
    Logger.debug("SmsRetrieverModule", "Initiated start listening")
    // We firstly assign our consent request
    this.consentRequest = SmsRequest(
      promise = promise,
    )
    // Then initialize the UserConsent
    initializeConsent()
  }

  private fun initializeConsent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        reactContext.unregisterReceiver(smsVerificationReceiver)
        Logger.debug(TAG, "initializeConsent_unregisterReceiver")
        registerReceiver()
      } catch (e: IllegalArgumentException) {
        var msg = e.message
        if (msg!!.contains("Receiver not registered", ignoreCase = true)) {
          Logger.debug(TAG, "initializeConsent_exception_handled: $msg")
          registerReceiver()
        } else {
          Logger.debug(TAG, "initializeConsent_exception_unhandled: $msg ")
          consentRequest.promise.reject(
            "Receiver Exception",
            ConsentError.ReceiverException.code,
            Throwable(ConsentError.ReceiverException.code),
          )
        }
      }
    }
    val client = SmsRetriever.getClient(reactContext)
    client.startSmsUserConsent(null)
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  fun registerReceiver() {
    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    reactContext.registerReceiver(
      smsVerificationReceiver,
      intentFilter,
      SmsRetriever.SEND_PERMISSION,
      Handler(Looper.getMainLooper())
    )
  }

  companion object {
    const val NAME = "RnAndroidSmsRetriever"
  }
}
