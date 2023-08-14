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
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Matcher
import java.util.regex.Pattern

class RnAndroidSmsRetrieverModule internal constructor(private val reactContext: ReactApplicationContext) :
  RnAndroidSmsRetrieverSpec(reactContext), ActivityEventListener {

  private var onSuccessCallback: Callback? = null
  private var onFailureCallback: Callback? = null
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
              Log.d(
                "SmsRetrieverModule",
                "SMS Broadcast started"
              )
            } catch (e: ActivityNotFoundException) {
              // Handle the exception ...
              Log.d(
                "SmsRetrieverModule",
                "Exception happened: ${e.localizedMessage}"
              )
              onFailureCallback?.invoke(e.localizedMessage)
            }
          }

          CommonStatusCodes.TIMEOUT -> {
            // Time out occurred, handle the error.
            onFailureCallback?.invoke("Timeout!")
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
    Log.d("SmsRetrieverModule", "OnActivityResult started")
    try {
      when (requestCode) {
        SMS_CONSENT_REQUEST ->
          if (resultCode == Activity.RESULT_OK && intent != null) {
            Log.d("SmsRetrieverModule", "We got the result and it's OK")
            // Get SMS message content
            val message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            val p: Pattern = Pattern.compile("(\\d{6})")
            val m: Matcher? = message?.let { p.matcher(it) }
            m?.find()?.let {
              if (it) {
                Log.d("SmsRetrieverModule", "found a pattern matching!")
                // We obtained the OTP
                onSuccessCallback?.invoke(m.group(0) as String)
              }
            }
          }
          else {
            Log.d("SmsRetrieverModule", "Consent denied!")
            onFailureCallback?.invoke("Consent denied by user")
          }
      }
    } catch (t: Throwable) {
      onFailureCallback?.invoke(t.localizedMessage)
    }
  }

  override fun onNewIntent(p0: Intent?) {

  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  @ReactMethod
  fun startListeningForOtp(onSuccess: Callback, onFailure: Callback) {
    Log.d("SmsRetrieverModule", "Initiated start listening")
    // We firstly assign our callbacks
    this.onSuccessCallback = onSuccess
    this.onFailureCallback = onFailure
    // Then initialize the UserConsent
    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      reactContext.registerReceiver(
        smsVerificationReceiver,
        intentFilter,
        SmsRetriever.SEND_PERMISSION,
        Handler(Looper.getMainLooper())
      )
      Log.d("SmsRetrieverModule", "Registered our Receiver!")
    }
    val client = SmsRetriever.getClient(reactContext)
    client.startSmsUserConsent(null)
    Log.d("SmsRetrieverModule", "Started our UserConsent")
  }

  companion object {
    const val NAME = "RnAndroidSmsRetriever"
  }
}
