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
import android.util.Log
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.rnandroidsmsretriever.utils.ConsentError
import com.rnandroidsmsretriever.utils.ConsentRequest
import com.rnandroidsmsretriever.utils.OTPRequest
import com.rnandroidsmsretriever.utils.SmsRequest
import java.util.regex.Matcher
import java.util.regex.Pattern

class RnAndroidSmsRetrieverModule internal constructor(
  private val reactContext: ReactApplicationContext
) : RnAndroidSmsRetrieverSpec(reactContext), ActivityEventListener {

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
            } catch (e: ActivityNotFoundException){
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
    activity: Activity?,
    requestCode: Int,
    resultCode: Int,
    intent: Intent?
  ) {
    when (requestCode) {
      SMS_CONSENT_REQUEST ->
        if (resultCode == Activity.RESULT_OK && intent != null) {
          // Get SMS message content
          val message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
          val requiredOtpLength = consentRequest.otpLength
          val p: Pattern = Pattern.compile("(\\d{$requiredOtpLength})")
          val m: Matcher? = message?.let { p.matcher(it) }
          m?.find()?.let {
            if (it) {
              // it's our message, extract the required data from it
              when (consentRequest) {
                is OTPRequest -> {
                  consentRequest.promise.resolve(m.group(0) as String)
                }

                is SmsRequest -> {
                  consentRequest.promise.resolve(message)
                }
              }
            }
          }
        }
        else {
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
      otpLength = otpLength,
      promise = promise,
    )
    // Then initialize the UserConsent
    initializeConsent()
  }

  @ReactMethod
  override fun getSms(otpLength: Int, promise: Promise) {
    Log.d("SmsRetrieverModule", "Initiated start listening")
    // We firstly assign our consent request
    this.consentRequest = SmsRequest(
      otpLength = otpLength,
      promise = promise,
    )
    // Then initialize the UserConsent
    initializeConsent()
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  fun initializeConsent() {
    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      reactContext.registerReceiver(
        smsVerificationReceiver,
        intentFilter,
        SmsRetriever.SEND_PERMISSION,
        Handler(Looper.getMainLooper())
      )
    }
    val client = SmsRetriever.getClient(reactContext)
    client.startSmsUserConsent(null)
  }

  companion object {
    const val NAME = "RnAndroidSmsRetriever"
  }
}
