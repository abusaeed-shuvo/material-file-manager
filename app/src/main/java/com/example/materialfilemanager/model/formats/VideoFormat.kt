package com.example.materialfilemanager.model.formats

enum class VideoFormat(val ext: String) {
	MP4("mp4"),
	MKV("mkv"),
	WEBM("webm"),
	AVI("avi"),
	MOV("mov"),
	FLV("flv"),
	WMV("wmv"),
	M4V("m4v"),
	TS("ts"),
	THREE_GP("3gp");

	companion object {
		fun isVideoFile(ext: String): Boolean {
			val format = ext.lowercase()

			return VideoFormat.entries.any { it.ext == format }
		}
	}

}