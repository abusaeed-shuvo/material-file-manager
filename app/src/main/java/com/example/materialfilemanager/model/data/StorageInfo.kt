package com.example.materialfilemanager.model.data

import androidx.annotation.DrawableRes

data class StorageInfo(
	val name: String,
	val usedBytes: Long,
	val totalBytes: Long,
	@DrawableRes val iconRes: Int,
	val rootPath: String
)
