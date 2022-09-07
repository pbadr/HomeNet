package com.example.homenet.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.homenet.MainActivity
import com.example.homenet.utils.LocationEngineCallback
import com.example.homenet.utils.Util
import com.mapbox.android.core.location.*
import com.mapbox.common.TAG

class LocationService : Service() {

  private var serviceLooper: Looper? = null
  private var serviceHandler: ServiceHandler? = null
  private lateinit var locationEngine: LocationEngine
  private var callback: LocationEngineCallback

  init {
    callback = LocationEngineCallback(context = this)
  }

  private inner class ServiceHandler(looper: Looper) : Handler(looper) {

    override fun handleMessage(msg: Message) {
      // Normally we would do some work here, like download a file.
      // For our sample, we just sleep for 5 seconds.
      locationEngine = LocationEngineProvider
        .getBestLocationEngine(this@LocationService.applicationContext)

      try {
        if (ActivityCompat.checkSelfPermission(
            this@LocationService.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
          ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this@LocationService.applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
          ) != PackageManager.PERMISSION_GRANTED
        ) { return }

        Log.d(TAG, "Creating location engine")
        locationEngine.requestLocationUpdates(
          Util.locationBuilder(),
          callback,
          Looper.getMainLooper()
        )
        locationEngine.getLastLocation(callback)
      } catch (e: InterruptedException) {
        // Restore interrupt status.
        Thread.currentThread().interrupt()
      }

      // Stop the service using the startId, so that we don't stop
      // the service in the middle of handling another job
      stopSelf(msg.arg1)
    }
  }

  override fun onCreate() {
    // Start up the thread running the service.  Note that we create a
    // separate thread because the service normally runs in the process's
    // main thread, which we don't want to block.  We also make it
    // background priority so CPU-intensive work will not disrupt our UI.
    Log.d(TAG, "Created service")
    HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
      start()

      // Get the HandlerThread's Looper and use it for our Handler
      serviceLooper = looper
      serviceHandler = ServiceHandler(looper)
    }
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Toast.makeText(this,
      "Tracking location as a background service",
      Toast.LENGTH_SHORT).show()

    // For each start request, send a message to start a job and deliver the
    // start ID so we know which request we're stopping when we finish the job
    serviceHandler?.obtainMessage()?.also { msg ->
      msg.arg1 = startId
      serviceHandler?.sendMessage(msg)
    }

    // If we get killed, after returning from here, restart
    return START_STICKY
  }

  override fun onBind(intent: Intent): IBinder? {
    // We don't provide binding, so return null
    return null
  }

  override fun onDestroy() {
    locationEngine.removeLocationUpdates(callback)
    Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
  }
}