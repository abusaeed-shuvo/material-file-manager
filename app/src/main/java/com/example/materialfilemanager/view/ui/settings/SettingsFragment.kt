package com.example.materialfilemanager.view.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.example.materialfilemanager.R

class SettingsFragment : PreferenceFragmentCompat(),
	SharedPreferences.OnSharedPreferenceChangeListener {

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
	}

	override fun onResume() {
		super.onResume()
		preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
	}

	override fun onPause() {
		super.onPause()
		preferenceScreen.sharedPreferences
			?.unregisterOnSharedPreferenceChangeListener(this)
	}

	override fun onSharedPreferenceChanged(
		sharedPreferences: SharedPreferences,
		key: String?
	) {
		if (key == "theme_pref") {
			when (sharedPreferences.getString(key, "system")) {
				"light"  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
				"dark"   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
				"system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
			}
		}
	}


}