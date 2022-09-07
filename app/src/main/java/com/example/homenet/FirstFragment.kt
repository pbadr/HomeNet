package com.example.homenet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.homenet.databinding.FragmentFirstBinding
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

  private lateinit var permissionsManager: PermissionsManager

  private var _binding: FragmentFirstBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentFirstBinding.inflate(inflater, container, false)
    return binding.root

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonFirst.setOnClickListener(navigateToSecondFragment)
    Log.d("LOCATION", "Checking location")
    // Check if location permissions are not granted
    if (PermissionsManager.areLocationPermissionsGranted(activity)) {
      binding.buttonFirst.isEnabled = true
    } else {
      permissionsManager = PermissionsManager(object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
          Toast.makeText(
            activity, "Accept location",
            Toast.LENGTH_SHORT
          ).show()
        }

        override fun onPermissionResult(granted: Boolean) {
          if (granted) {
            Log.d("GRANTED", "OK")
            return
          }

          activity?.finish()
        }
      })
      permissionsManager.requestLocationPermissions(activity)
    }
}

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private val navigateToSecondFragment =  View.OnClickListener {
    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
  }
}