package com.example.materialfilemanager.model.formats

import java.io.File

enum class ImageFormat(val extension: String) {
	JPG("jpg"),
	JPEG("jpeg"),
	PNG("png"),
	WEBP("webp"),
	GIF("gif"),
	BMP("bmp");


	companion object {
		fun isImageFile(file: File): Boolean {
			val ext = file.extension.lowercase()
			return ImageFormat.entries.any() { it.extension == ext }
		}
	}
}