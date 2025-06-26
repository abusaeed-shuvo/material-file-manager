package com.example.materialfilemanager.model.data

import androidx.documentfile.provider.DocumentFile
import java.io.File

sealed class FileItem {
	data class Local(val file: File) : FileItem()
	data class Document(val documentFile: DocumentFile) : FileItem()

	val isDirectory: Boolean
		get() = when (this) {
			is Local    -> file.isDirectory
			is Document -> documentFile.isDirectory
		}

	val name: String
		get() = when (this) {
			is Document -> documentFile.name ?: ""
			is Local    -> file.name
		}
}