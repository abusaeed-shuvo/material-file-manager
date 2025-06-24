package com.example.materialfilemanager.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.materialfilemanager.AdditionalFunction
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.ItemFileBinding
import java.io.File

class FileListAdapter(private val onClick: (File) -> Unit) :
	ListAdapter<File, FileListAdapter.FileViewHolder>(DiffCallBack) {
	override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int
	): FileViewHolder {

		return FileViewHolder(
			ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)
	}

	override fun onBindViewHolder(
		holder: FileViewHolder, position: Int
	) {
		holder.bind(getItem(position))


	}

	inner class FileViewHolder(private val binding: ItemFileBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(file: File) = with(binding) {
			tvFileName.text = file.name
			iconFile.setImageResource(if (file.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)
			root.setOnClickListener {
				onClick(file)
			}
			tvFileDateModified.text = getLastModifiedDate(file)

			tvFileSize.text = if (file.isDirectory) {
				"${countContentInFolder(file)}"
			} else {
				getFormattedFileSize(file)
			}

		}
	}

	companion object {
		private val DiffCallBack = object : DiffUtil.ItemCallback<File>() {
			override fun areItemsTheSame(
				oldItem: File, newItem: File
			): Boolean = oldItem.absolutePath == newItem.absolutePath

			override fun areContentsTheSame(
				oldItem: File, newItem: File
			): Boolean =
				oldItem.length() == newItem.length() && oldItem.lastModified() == newItem.lastModified()

		}

		private fun countContentInFolder(folder: File): Int {
			if (!folder.exists() || !folder.isDirectory) return 0

			val count = folder.listFiles()?.count() ?: 0

			return count
		}

		private fun getLastModifiedDate(file: File): String {
			val lastModified = file.lastModified()

			return AdditionalFunction.getFormattedTime(lastModified)

		}

		private fun getFormattedFileSize(file: File): String {
			val sizeInBytes = file.length()
			return AdditionalFunction.formatFileSize(sizeInBytes)
		}
	}

}