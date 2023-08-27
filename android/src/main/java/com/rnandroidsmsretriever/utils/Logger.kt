package com.rnandroidsmsretriever.utils

import android.util.Log

object Logger {
    private var DISABLE_DEBUGGING = false
    fun debug(TAG: String?, msg: String?) {
        if (DISABLE_DEBUGGING) return
        Log.d(TAG, msg!!)
    }

    fun verbose(TAG: String?, msg: String?) {
        if (DISABLE_DEBUGGING) return
        Log.v(TAG, msg!!)
    }

    fun error(TAG: String?, msg: String?) {
        if (DISABLE_DEBUGGING) return
        Log.e(TAG, msg!!)
    }

    fun info(TAG: String?, msg: String?) {
        if (DISABLE_DEBUGGING) return
        Log.i(TAG, msg!!)
    }
}
