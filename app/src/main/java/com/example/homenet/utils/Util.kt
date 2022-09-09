package com.example.homenet.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.homenet.R
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.mapbox.common.TAG
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class Util {
  companion object {

    const val LOCATION_INTERVAL: Long = 60000 // 1 minute

    var PERMISSIONS = arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun locationRequest(): LocationRequest {
      return LocationRequest.create()
        .setInterval(LOCATION_INTERVAL)
        .setMaxWaitTime(LOCATION_INTERVAL)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    }

    fun getMarker(context: Context): Bitmap {
      val vectorDrawable = VectorDrawableCompat.create(
        context.resources,
        R.drawable.red_marker,
        context.theme
      )
      return vectorDrawable!!.toBitmap()
    }

    fun getHomeLocation(context: Context): List<Double> {
      val file = File(context.filesDir, context.getString(R.string.home_location))
      if (!file.exists())
        throw FileNotFoundException()

      return file.readText().split(",").map { it.toDouble() }
    }

    fun arePointsNear(checkPoint: Array<Double>, centerPoint: Array<Double>): Boolean {
      val ky = 40000 / 360
      val kx = cos(Math.PI * centerPoint[0] / 180.0) * ky
      val dx = abs(centerPoint[1] - checkPoint[1]) * kx
      val dy = abs(centerPoint[0] - checkPoint[0]) * ky

      return sqrt(dx * dx + dy * dy) <= 0.05
    }

    fun sendVicinityNotification(context: Context) {
//      if (!isMobileDataOn(context))
//        return

      val notificationBuilder = NotificationCompat.Builder(context, "VN_01")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("You are at home!")
        .setContentText("You might need to change to WiFi")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "CH_VN"
        val descriptionText = "Channel Vicinity"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("VN_01", name, importance).apply {
          description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
      }

      Log.d(TAG, "Sending notification")
      with(NotificationManagerCompat.from(context)) {
        notify(1, notificationBuilder.build())
      }
    }

    private fun isMobileDataOn(context: Context): Boolean {
      val mobileData: Int = Settings.Global.getInt(
        context.contentResolver,
        "mobile_data",
        1
      )

      return mobileData == 1
    }
  }
}
