package com.example.materialfilemanager.view.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.FragmentHomeBinding
import com.example.materialfilemanager.model.data.StorageInfo
import com.example.materialfilemanager.view.adapter.StorageListAdapter
import java.io.File

class HomeFragment : Fragment() {
	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!


	private val sdCardLauncher =
		registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
			if (uri != null) {
				requireContext().contentResolver.takePersistableUriPermission(
					uri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
				)

				// Save to SharedPreferences
				val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
				prefs.edit { putString("sd_root_uri", uri.toString()) }

				// Navigate to file list
				val action =
					HomeFragmentDirections.actionHomeFragmentToFileListFragment(uri.toString())
				findNavController().navigate(action)
			}
		}

	// Call this to request access
	private fun requestSdCardAccess() {
		sdCardLauncher.launch(null)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentHomeBinding.inflate(inflater, container, false)
		// Inflate the layout for this fragment
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val storageList = mutableListOf<StorageInfo>()


		// Internal
		getStorageStats(Environment.getExternalStorageDirectory())?.let {
			storageList.add(
				StorageInfo(
					name = "Internal Storage",
					usedBytes = it.first,
					totalBytes = it.second,
					iconRes = R.drawable.ic_internal,
					rootPath = Environment.getExternalStorageDirectory().absolutePath
				)
			)
		}


		//SD card
		val dirs = ContextCompat.getExternalFilesDirs(requireContext(), null)
		if (dirs.size > 1) {
			val sd = dirs[1]
			val sdRoot = sd?.path?.substringBefore("/Android")?.let { File(it) }
			val sdCardRoot = File(sd.absolutePath.substringBefore("/Android"))

			sdRoot?.let {
				getStorageStats(it)?.let { stats ->
					storageList.add(
						StorageInfo(
							name = "SD Card(Legacy)",
							usedBytes = stats.first,
							totalBytes = stats.second,
							iconRes = R.drawable.ic_sd_card,
							rootPath = sdCardRoot.absolutePath
						)
					)
				}
			}
		}


		if (storageList.isEmpty()) {
			binding.tvNoStorageAvailable.visibility = View.VISIBLE
		} else {
			binding.tvNoStorageAvailable.visibility = View.GONE
			binding.recyclerviewViewStorage.apply {
				layoutManager = LinearLayoutManager(requireContext())
				adapter = StorageListAdapter(storageList, onItemClick = {
					val action =
						HomeFragmentDirections.actionHomeFragmentToFileListFragment(it.rootPath)
					findNavController().navigate(action)
				})
			}
			binding.btnBrowseSdCard.setOnClickListener {
				requestSdCardAccess()
			}
		}

	}


	private fun getStorageStats(file: File): Pair<Long, Long>? {
		return try {
			val stats = StatFs(file.path)
			val blockSize = stats.blockSizeLong
			val total = stats.blockCountLong * blockSize
			val available = stats.availableBlocksLong * blockSize
			val used = total - available

			Pair(used, total)

		} catch (e: Exception) {
			null
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}