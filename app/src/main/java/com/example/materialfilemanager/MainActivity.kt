package com.example.materialfilemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.materialfilemanager.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolBar)
		



		if (!hasStoragePermission()) {
			showStoragePermissionDialog()
		}
	}

	private fun hasStoragePermission(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			Environment.isExternalStorageManager()
		} else {
			ContextCompat.checkSelfPermission(
				this, Manifest.permission.READ_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_GRANTED
		}
	}

	private fun showStoragePermissionDialog() {
		MaterialAlertDialogBuilder(this)
			.setTitle("Storage Permission Required")
			.setMessage("To manage your files, the app needs access to your device's storage.")
			.setPositiveButton("Grant Access") { _, _ ->
				navigateToStorageSettings()
			}
			.setNegativeButton("Cancel") { _, _ ->
				Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
			}
			.setCancelable(false)
			.show()
	}

	private fun navigateToStorageSettings() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
				data = "package:$packageName".toUri()
			}
			startActivityForResult(intent, 1001)
		} else {
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
				1000
			)
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 1000 && grantResults.isNotEmpty() &&
		    grantResults[0] == PackageManager.PERMISSION_GRANTED
		) {
			recreate() // Reload activity to trigger navGraph
		} else {
			Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == 1001 && hasStoragePermission()) {
			recreate() // Reload activity to trigger navGraph
		}
	}
}