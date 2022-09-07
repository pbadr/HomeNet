package com.example.homenet

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.homenet.databinding.FragmentFirstBinding
import com.example.homenet.utils.Util
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

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

    // Check if location permissions are not granted
    if (PermissionsManager.areLocationPermissionsGranted(activity)) {
      binding.buttonFirst.isEnabled = true
    } else {
      permissionRequestLauncher.launch(Util.PERMISSIONS)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private val navigateToSecondFragment =  View.OnClickListener {
    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
  }

  private val permissionRequestLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    // If all permission entries are true (it.value == true)
    val granted = permissions.entries.all { it.value }
    if (granted)
      binding.buttonFirst.isEnabled = true
  }
}