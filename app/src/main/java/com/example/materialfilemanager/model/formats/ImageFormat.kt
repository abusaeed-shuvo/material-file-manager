package com.example.materialfilemanager.model.formats

enum class ImageFormat(val extension: String) {
	JPG("jpg"),
	JPEG("jpeg"),
	PNG("png"),
	WEBP("webp"),
	GIF("gif"),
	BMP("bmp");


	companion object {
		fun isImageFile(file: String): Boolean {
			val ext = file.lowercase()
			return ImageFormat.entries.any() { it.extension == ext }
		}
	}
}