package com.example.materialfilemanager.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel : ViewModel() {

	private val _currentDirectory = MutableLiveData<File>()
	val currentDirectory = _currentDirectory

	private val _fileList = MutableLiveData<List<File>>()
	val fileList = _fileList

	fun loadFiles(dir: File) {
		_currentDirectory.value = dir

		viewModelScope.launch(Dispatchers.IO) {
			val files =
				dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
				?: emptyList()
			withContext(Dispatchers.Main) {
				_fileList.value = files
			}
		}
	}

	fun goUp() {
		_currentDirectory.value?.parentFile?.let { loadFiles(it) }
	}

}