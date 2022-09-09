package com.example.homenet.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.homenet.utils.Util
import com.google.android.gms.location.*
import java.util.*

class LocationService : Service() {

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  var latitude: Double = 0.0
  var longitude: Double = 0.0

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate() {
    requestLocationUpdates()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    startTimer()
    return START_STICKY
  }

  override fun onBind(intent: Intent): IBinder? {
    // We don't provide binding, so return null
    return null
  }

  private fun startTimer() {
    val timer = Timer()
    val timerTask = object : TimerTask() {
      override fun run() {
        if (latitude != 0.0 && longitude != 0.0) {
          if (Util.arePointsNear(
              arrayOf(latitude, longitude),
              Util.getHomeLocation(this@LocationService.applicationContext).toTypedArray()
            )) {
            Util.sendVicinityNotification(this@LocationService.applicationContext)
          }
        }
      }
    }

    timer.schedule(
      timerTask,
      0,
      Util.LOCATION_INTERVAL
    )
  }

  private fun requestLocationUpdates() {

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    val permission = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (permission == PackageManager.PERMISSION_GRANTED) {
      fusedLocationClient.requestLocationUpdates(Util.locationRequest(), object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          val location: Location? = locationResult.lastLocation
            latitude = location?.latitude!!
            longitude = location.longitude
        }
      }, null)
    }
  }

}