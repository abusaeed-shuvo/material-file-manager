package com.example.materialfilemanager.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel : ViewModel() {

	private val _fileList = MutableLiveData<List<File>>()
	val fileList get() = _fileList

	private var currentFileDir: File? = null


	fun loadFiles(dir: File) {

		currentFileDir = dir

		viewModelScope.launch(Dispatchers.IO) {
			val items = dir.listFiles()
				            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
			            ?: emptyList()

			withContext(Dispatchers.Main) {
				_fileList.value = items
			}
		}
	}


	fun goUp() {

		currentFileDir?.parentFile?.let { loadFiles(it) }

	}

	fun getCurrentDirectoryName(): String {

		return currentFileDir?.name ?: "Storage"

	}

	fun getCurrentDirectory(): File? = currentFileDir

}