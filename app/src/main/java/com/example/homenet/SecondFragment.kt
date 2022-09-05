package com.example.homenet

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import com.example.homenet.databinding.FragmentSecondBinding
import com.example.homenet.services.LocationService
import com.example.homenet.utils.Util
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.io.File

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

  private lateinit var file: File

  private lateinit var permissionsManager: PermissionsManager

  private var mapView: MapView? = null
  private lateinit var annotationApi: AnnotationPlugin
  private lateinit var pointAnnotationManager: PointAnnotationManager

  private var _binding: FragmentSecondBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private var locationEngine: LocationEngine? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentSecondBinding.inflate(inflater, container, false)
    return binding.root
  }

  @RequiresApi(Build.VERSION_CODES.P)
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    mapView = binding.mapView
    annotationApi = mapView?.annotations!!
    pointAnnotationManager = annotationApi.createPointAnnotationManager()

    binding.buttonSecond.setOnClickListener {
      Intent(activity, LocationService::class.java).also { intent ->
        this.context?.startService(intent)
      }
    }

    val locationManager = this.context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!locationManager.isLocationEnabled) {
      Toast.makeText(this.context, "Please enable your location", Toast.LENGTH_SHORT)
        .show()
      Intent(activity, MainActivity::class.java).also { intent ->
        this.context?.startActivity(intent)
      }

      return
    }

    if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
      onMapReady()
    } else {
      permissionsManager = PermissionsManager(object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
          Toast.makeText(
            activity, "You need to accept location permissions.",
            Toast.LENGTH_SHORT
          ).show()
        }

        override fun onPermissionResult(granted: Boolean) {
          if (granted) {
            initLocationComponent()
            setupGesturesListener()
          } else {
            activity?.finish()
          }
        }
      })
      permissionsManager.requestLocationPermissions(activity)
    }
  }

  private fun onMapReady() {
    mapView?.getMapboxMap()?.setCamera(
      CameraOptions.Builder()
        .zoom(14.0)
        .build()
    )
    mapView?.getMapboxMap()?.loadStyleUri(
      Style.MAPBOX_STREETS
    ) {
      initLocationComponent()
      setupGesturesListener()
    }

    mapView?.getMapboxMap()?.addOnMapLongClickListener(onMapLongClickListener)
  }

  private val onMapLongClickListener = OnMapLongClickListener {
    val image = Util.getMarker(this.requireContext())

    // Save new location and set marker
    file = File(context?.filesDir, getString(R.string.home_location))
    file.writeText("${it.latitude()},${it.longitude()}")

    // Remove already existing annotation
    mapView?.annotations?.removeAnnotationManager(pointAnnotationManager)
    pointAnnotationManager = annotationApi.createPointAnnotationManager()
    val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
      .withPoint(Point.fromLngLat(it.longitude(), it.latitude()))
      .withIconImage(image)
    pointAnnotationManager.create(pointAnnotationOptions)
    false
  }

  private fun setupGesturesListener() {
    mapView?.gestures?.addOnMoveListener(onMoveListener)
  }

  private fun initLocationComponent() {
    val locationComponentPlugin = mapView?.location
    locationComponentPlugin?.updateSettings {
      this.enabled = true
      this.locationPuck = LocationPuck2D(
        bearingImage = this@SecondFragment.context?.let {
          AppCompatResources.getDrawable(
            it,
            com.mapbox.maps.R.drawable.mapbox_user_puck_icon
          )
        },
        scaleExpression = interpolate {
          linear()
          zoom()
          stop {
            literal(0.0)
            literal(0.6)
          }
          stop {
            literal(20.0)
            literal(1.0)
          }
        }.toJson()
      )
    }

    // Check if user already has a location saved
    file = File(context?.filesDir, getString(R.string.home_location))

    // If it does, set a marker to that location
    if (file.exists()) {
      val location = file.readText().split(",").map { it.toDouble() }
      pointAnnotationManager.create(
        PointAnnotationOptions()
          .withPoint(Point.fromLngLat(location[0], location[1]))
          .withIconImage(Util.getMarker(this.requireContext()))
      )
    }

    locationComponentPlugin?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    locationComponentPlugin?.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
  }

  private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
    mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().bearing(it).build())
  }

  private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
    mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(it).build())
    mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(it)
  }

  private val onMoveListener = object : OnMoveListener {
    override fun onMoveBegin(detector: MoveGestureDetector) {
      onCameraTrackingDismissed()
    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
      return false
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {}
  }

  private fun onCameraTrackingDismissed() {
    mapView?.location
      ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    mapView?.location
      ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    mapView?.gestures?.removeOnMoveListener(onMoveListener)
  }

  override fun onDestroyView() {
    super.onDestroyView()

    locationEngine = null
    mapView?.location
      ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    mapView?.location
      ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    mapView?.gestures?.removeOnMoveListener(onMoveListener)
  }
}