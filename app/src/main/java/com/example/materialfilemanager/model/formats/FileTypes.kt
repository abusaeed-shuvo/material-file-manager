package com.example.materialfilemanager.model.formats

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.materialfilemanager.R
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

		fun getFileIcon(file: File): Int {
			val ext = file.extension.lowercase()
			return if (file.isDirectory) {
				R.drawable.ic_folder
			} else {
				when (ext) {
					in imageExt -> R.drawable.ic_img
					in videoExt -> R.drawable.ic_avi
					in textExt  -> R.drawable.ic_txt

					else        -> R.drawable.ic_other_file
				}
			}
		}

		fun loadThumbnailInto(context: Context, file: File, imageView: ImageView) {
			Glide.with(context)
				.load(file)
				.placeholder(getFileIcon(file))
				.error(getFileIcon(file))
				.centerCrop()
				.into(imageView)
		}
	}

}