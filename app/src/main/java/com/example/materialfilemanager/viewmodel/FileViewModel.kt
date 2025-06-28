package com.example.materialfilemanager.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.materialfilemanager.model.data.ShellFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FileViewModel : ViewModel() {

	private val _fileList = MutableLiveData<List<File>>()
	val fileList get() = _fileList

	private var currentFileDir: File? = null
	private var clipboardFiles = emptyList<File>()
	private var isCutOperation = false

	fun loadFiles(dir: File) {

		currentFileDir = dir

		viewModelScope.launch(Dispatchers.IO) {
			val items = try {
				dir.listFiles()
					?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
				?: emptyList()


			} catch (e: SecurityException) {
				loadFilesWithShizuku(dir.absolutePath)
				return@launch
			}

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

	fun copyFiles(files: List<File>) {
		clipboardFiles = files
		isCutOperation = false
	}

	fun cutFiles(files: List<File>) {
		clipboardFiles = files
		isCutOperation = true
	}

	fun pasteFiles(onComplete: (success: Boolean, message: String) -> Unit) {
		val targetDir = currentFileDir ?: return
		viewModelScope.launch(Dispatchers.IO) {
			var success = true
			var errorMessage = ""

			for (file in clipboardFiles) {
				try {
					val targetFile = File(targetDir, file.name)
					if (file.isDirectory) {
						file.copyRecursively(targetFile, overwrite = true)
					} else {
						file.copyTo(targetFile, overwrite = true)
					}

					if (isCutOperation) {
						if (!file.deleteRecursively()) {
							errorMessage += "Failed to delete ${file.name} after cut. \n"
							success = false
						}
					}
				} catch (e: Exception) {
					errorMessage += "Error processing ${file.name}: ${e.message}\n"
					success = false
				}

				clipboardFiles = emptyList()
				isCutOperation = false

				withContext(Dispatchers.Main) {
					onComplete(success, if (success) "Operation completed" else errorMessage.trim())
					loadFiles(targetDir)
				}
			}
		}
	}

	fun deleteFiles(files: List<File>, onComplete: (success: Boolean, message: String) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			var success = true
			var message = ""

			for (file in files) {
				try {
					if (!file.deleteRecursively()) {
						message += "Failed to delete ${file.name}\n"
						success = false
					}
				} catch (e: Exception) {
					message += "Error deleting ${file.name}: ${e.message}\n"
					success = false
				}
			}

			withContext(Dispatchers.IO) {
				onComplete(success, if (success) "Deleted Successfully" else message.trim())
				currentFileDir?.let { loadFiles(it) }
			}

		}
	}

	fun loadFilesWithShizuku(path: String) {
		viewModelScope.launch(Dispatchers.IO) {


			try {
				val binder = Shizuku.getBinder()
				val service = IShizukuService.Stub.asInterface(binder)

				val process =
					service.newProcess(arrayOf("sh", "-c", "ls -Al \"$path\""), null, null)

				val output = FileInputStream(process.inputStream.fileDescriptor)
					.bufferedReader().use { it.readLines() }

				val fileItems = output
					.drop(1) // drop first line if it's like "total 0"
					.mapNotNull { line ->
						parseLsLine(line, path)
					}

				withContext(Dispatchers.Main) {
					_fileList.value =
						fileItems.map { File(it.fullPath) } // Convert to File if needed
				}

			} catch (e: Exception) {
				withContext(Dispatchers.Main) {
					_fileList.value = emptyList()
				}
			}
		}
	}

	private fun parseLsLine(line: String, parentPath: String): ShellFileItem? {
		val tokens = line.trim().split(Regex("\\s+"))
		if (tokens.size < 9) return null

		val permissions = tokens[0]
		val isDir = permissions.startsWith("d")
		val name = tokens.drop(8).joinToString(" ")
		val size = tokens[4].toLongOrNull() ?: 0L
		val fullPath = "$parentPath/$name"

		// Extract and parse date
		val month = tokens[5]
		val day = tokens[6]
		val timeOrYear = tokens[7]

		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val dateStr = if (timeOrYear.contains(":")) {
			// If recent: add current year
			"$month $day $currentYear $timeOrYear"
		} else {
			// If old: use given year
			"$month $day $timeOrYear 00:00"
		}

		val formatter = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.ENGLISH)
		val lastModified = try {
			formatter.parse(dateStr)?.time ?: 0L
		} catch (e: Exception) {
			0L
		}

		return ShellFileItem(
			name = name,
			isDirectory = isDir,
			fullPath = fullPath,
			size = size,
			lastModified = lastModified
		)
	}


}

//fun executeShellCommandViaShizuku(
//	command: String,
//	onResult: (String) -> Unit,
//	onError: (String) -> Unit
//) {
//	viewModelScope.launch(Dispatchers.IO) {
//
//		try {
//			val binder = Shizuku.getBinder()
//			val service = IShizukuService.Stub.asInterface(binder)
//			val process = service.newProcess(arrayOf("sh", "-c", command), null, null)
//
//			val output =
//				BufferedReader(InputStreamReader(FileInputStream(process.inputStream.fileDescriptor))).use { it.readText() }
//
//			val error =
//				BufferedReader(InputStreamReader(FileInputStream(process.errorStream.fileDescriptor))).use { it.readText() }
//
//			withContext(Dispatchers.Main) {
//				if (error.isNotBlank()) {
//					onError(error)
//				} else {
//					onResult(output)
//				}
//			}
//		} catch (e: Exception) {
//			withContext(Dispatchers.Main) {
//				onError("Shizuku command error: ${e.message}")
//			}
//		}
//	}
//}