package com.example.materialfilemanager.viewmodel

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel : ViewModel() {

	private val _currentDocumentDirectory = MutableLiveData<DocumentFile?>()
	val currentDocumentDirectory = _currentDocumentDirectory

	private val _fileDocumentList = MutableLiveData<List<DocumentFile>>()
	val fileDocumentList = _fileDocumentList

	fun loadFromDocumentFile(dir: DocumentFile) {
		_currentDocumentDirectory.value = dir

		viewModelScope.launch(Dispatchers.IO) {
			val files =
				dir.listFiles().filter { it.name != null }
					.sortedWith(compareBy({ !it.isDirectory }, { it.name?.lowercase() ?: "" }))
			withContext(Dispatchers.Main) {
				_fileDocumentList.value = files
			}
		}
	}

	fun goUpDocument() {
		val parent = _currentDocumentDirectory.value?.parentFile
		if (parent != null) {
			loadFromDocumentFile(parent)
		}
	}

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