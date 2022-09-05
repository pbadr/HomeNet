package com.example.homenet.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.homenet.MainActivity
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.common.TAG
import java.lang.Exception
import java.lang.ref.WeakReference

class LocationEngineCallback internal constructor(context: Context) :
  LocationEngineCallback<LocationEngineResult> {

  private val activityWeakReference: WeakReference<Context>
  init { this.activityWeakReference = WeakReference(context) }

  override fun onSuccess(result: LocationEngineResult?) {
    Log.d(TAG, "onSuccess")
    if (result != null) {
      Log.d(TAG, "${
        Util.arePointsNear(
          arrayOf(result.lastLocation!!.latitude, result.lastLocation!!.longitude),
          Util.getHomeLocation(activityWeakReference.get()!!).toTypedArray()
        )
      }")
      if (Util.arePointsNear(
          arrayOf(result.lastLocation!!.latitude, result.lastLocation!!.longitude),
          Util.getHomeLocation(activityWeakReference.get()!!).toTypedArray()
      )) {
        Util.sendVicinityNotification(activityWeakReference.get()!!.applicationContext)
        }
    }
  }

  override fun onFailure(exception: Exception) {
    Log.d(TAG, "onFailure")

  }
}