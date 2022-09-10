package com.example.homenet.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class VicinityNotification : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Log.d("BROADCAST_RECEIVER", "Hello world")

    with(NotificationManagerCompat.from(context)) { cancel(1) }
  }
}