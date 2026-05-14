package com.nammahaadi.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nammahaadi.app.MainActivity
import com.nammahaadi.app.R

/**
 * NammaHaadiFcmService — Firebase Cloud Messaging handler.
 *
 * HOW IT WORKS:
 *  1. When a new Alert is created in your backend/Firestore, your server sends
 *     an FCM push notification using the Firebase Admin SDK.
 *  2. This service receives the push and shows a system notification.
 *  3. Tapping the notification opens the app to the Alerts screen.
 *
 * HOW TO SEND A NOTIFICATION FROM YOUR SERVER (Node.js example):
 *
 *   const admin = require('firebase-admin');
 *   await admin.messaging().send({
 *     notification: { title: "⚠️ Road Alert", body: "Flooding on MG Road" },
 *     data: { screen: "alerts", severity: "WARNING" },
 *     topic: "road_alerts"   // all subscribed devices get it
 *   });
 *
 * HOW TO SEND A TEST NOTIFICATION (Firebase Console):
 *   Firebase Console → Engage → Messaging → Send your first message
 *   Target topic: road_alerts
 */
class NammaHaadiFcmService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "namma_haadi_alerts"
        const val CHANNEL_NAME = "Road Alerts"
        const val TOPIC_ROAD_ALERTS = "road_alerts"
    }

    /**
     * Called when a new FCM token is generated (first launch or token refresh).
     * Send this token to your server to enable device-specific pushes,
     * OR subscribe to a topic to send to all users at once.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Subscribe to the road_alerts topic so all app users receive alerts
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
            .subscribeToTopic(TOPIC_ROAD_ALERTS)

        // Optional: send token to your REST API for targeted pushes
        // val prefs = getSharedPreferences("fcm", MODE_PRIVATE)
        // prefs.edit().putString("fcm_token", token).apply()
        // sendTokenToServer(token)
    }

    /**
     * Called when a push message arrives while the app is in foreground.
     * Android shows the notification automatically when app is in background.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Namma Haadi Alert"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val screen = message.data["screen"] ?: "alerts"

        showNotification(title, body, screen)
    }

    private fun showNotification(title: String, body: String, screen: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required on Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time road alerts for your area"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tapping the notification opens the app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", screen)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
