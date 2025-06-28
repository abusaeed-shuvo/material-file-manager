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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.materialfilemanager.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener


class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var navController: NavController
	lateinit var toolbar: AppBarLayout

	private fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
		val granted = grantResult == PackageManager.PERMISSION_GRANTED
		// Do stuff based on the result and the request code
	}

	private val REQUEST_PERMISSION_RESULT_LISTENER =
		OnRequestPermissionResultListener { requestCode: Int, grantResult: Int ->
			this.onRequestPermissionsResult(
				requestCode, grantResult
			)
		}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)
		setSupportActionBar(binding.toolBar)

		Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
		checkPermission(0)

		val drawerLayout = binding.main
		val navView = binding.navDrawer
		toolbar = binding.topAppBar

		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
		navController = navHostFragment.navController


		appBarConfiguration = AppBarConfiguration(
			navController.graph, drawerLayout
		)

		setupActionBarWithNavController(navController, drawerLayout)
		navView.setupWithNavController(navController)

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
		MaterialAlertDialogBuilder(this).setTitle("Storage Permission Required")
			.setMessage("To manage your files, the app needs access to your device's storage.")
			.setPositiveButton("Grant Access") { _, _ ->
				navigateToStorageSettings()
			}.setNegativeButton("Cancel") { _, _ ->
				Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
			}.setCancelable(false).show()
	}

	private fun navigateToStorageSettings() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
				data = "package:$packageName".toUri()
			}
			startActivityForResult(intent, 1001)
		} else {
			ActivityCompat.requestPermissions(
				this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000
			)
		}
	}

	override fun onSupportNavigateUp(): Boolean {

		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}

	override fun onRequestPermissionsResult(
		requestCode: Int, permissions: Array<out String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

	override fun onDestroy() {
		// ...
		super.onDestroy()
		Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
		// ...
	}

	private fun checkPermission(code: Int): Boolean {
		if (Shizuku.isPreV11()) {
			// Pre-v11 is unsupported
			return false
		}

		if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
			// Granted
			return true
		} else if (Shizuku.shouldShowRequestPermissionRationale()) {
			// Users choose "Deny and don't ask again"
			return false
		} else {
			// Request the permission
			Shizuku.requestPermission(code)
			return false
		}
	}
}