package com.example.materialfilemanager.model.data

data class ShellFileItem(
	val name: String,
	val fullPath: String,
	val isDirectory: Boolean,
	val size: Long,
	val lastModified: Long
)
