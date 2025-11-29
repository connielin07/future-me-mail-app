package com.example.futurememailapp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.threetenabp.AndroidThreeTen

class MainApplication : Application() {

    companion object {
        @Volatile
        var currentFcmToken: String? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        FirebaseApp.initializeApp(this)
        fetchFcmToken()
    }

    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FutureMe-FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                currentFcmToken = token
                Log.d("FutureMe-FCM", "FCM token: $token")
            }
    }
}
