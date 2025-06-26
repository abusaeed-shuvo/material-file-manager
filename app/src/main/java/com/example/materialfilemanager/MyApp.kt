package com.example.materialfilemanager

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyApp : Application() {
	override fun onCreate() {
		super.onCreate()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			DynamicColors.applyToActivitiesIfAvailable(this)
		}
	}
}

object AdditionalFunction {

	fun getFormattedTime(time: Long, context: Context): String {
		val prefs = PreferenceManager.getDefaultSharedPreferences(context)

		val dateFormat =
			prefs.getString("date_pref", "MMM dd, yyyy: hh:mm a") ?: "MMM dd, yyyy: hh:mm a"

		val instant = Instant.ofEpochMilli(time)
		val zonedDateTime = instant.atZone(ZoneId.systemDefault())


		return DateTimeFormatter.ofPattern(dateFormat).format(zonedDateTime)
	}


	fun formatFileSize(sizeInBytes: Long): String {
		val kb = 1024
		val mb = kb * 1024
		val gb = mb * 1024

		return when {
			sizeInBytes >= gb -> String.format("%.2f GB", sizeInBytes.toDouble() / gb)
			sizeInBytes >= mb -> String.format("%.2f MB", sizeInBytes.toDouble() / mb)
			sizeInBytes >= kb -> String.format("%.2f KB", sizeInBytes.toDouble() / kb)
			else              -> "$sizeInBytes B"
		}
	}

	fun countContentInFolder(folder: File): Int {
		if (!folder.exists() || !folder.isDirectory) return 0

		val count = folder.listFiles()?.count() ?: 0

		return count
	}

	fun getLastModifiedDate(file: File, context: Context): String {
		val lastModified = file.lastModified()

		return AdditionalFunction.getFormattedTime(lastModified, context)

	}

	fun getFormattedFileSize(file: File): String {
		val sizeInBytes = file.length()
		return AdditionalFunction.formatFileSize(sizeInBytes)
	}
}