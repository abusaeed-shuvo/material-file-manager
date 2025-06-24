package com.example.materialfilemanager.model.formats

import java.io.File

enum class FileTypes() {
	TEXT, IMAGE, VIDEO, UNKNOWN;

	companion object {
		private val imageExt = ImageFormat.entries.map { it.extension }.toSet()
		private val videoExt = VideoFormat.entries.map { it.ext }.toSet()
		private val textExt = TextFormat.entries.map { it.extension }.toSet()

		fun getFileType(file: File): FileTypes {
			val ext = file.extension.lowercase()
			return when (ext) {
				in imageExt -> IMAGE
				in videoExt -> VIDEO
				in textExt  -> TEXT
				else        -> UNKNOWN
			}
		}
	}
}