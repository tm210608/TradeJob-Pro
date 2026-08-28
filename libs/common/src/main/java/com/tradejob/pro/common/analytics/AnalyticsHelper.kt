package com.tradejob.pro.common.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val crashlytics: FirebaseCrashlytics? by lazy {
        try { FirebaseCrashlytics.getInstance() } catch (e: Exception) { null }
    }

    private val analytics: FirebaseAnalytics? by lazy {
        try { FirebaseAnalytics.getInstance(context) } catch (e: Exception) { null }
    }

    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        try {
            analytics?.logEvent(name) {
                params.forEach { (key, value) -> param(key, value) }
            }
        } catch (e: Exception) {
            // Silent fail - analytics is non-critical
        }
    }

    fun logError(throwable: Throwable, message: String? = null) {
        try {
            message?.let { crashlytics?.log(it) }
            crashlytics?.recordException(throwable)
        } catch (e: Exception) {
            // Silent fail
        }
    }

    fun setUserId(userId: String?) {
        try {
            crashlytics?.setUserId(userId ?: "")
        } catch (e: Exception) {
            // Silent fail
        }
    }
}
