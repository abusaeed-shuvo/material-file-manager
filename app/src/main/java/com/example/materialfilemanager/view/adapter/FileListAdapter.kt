package com.example.materialfilemanager.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.materialfilemanager.AdditionalFunction.countContentInFolder
import com.example.materialfilemanager.AdditionalFunction.getFormattedFileSize
import com.example.materialfilemanager.AdditionalFunction.getLastModifiedDate
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.ItemFileBinding
import java.io.File

class FileListAdapter(
	private val onClick: (File) -> Unit, private val onLongClick: (File) -> Unit
) : ListAdapter<File, FileListAdapter.FileViewHolder>(DiffCallBack) {

	private val selectedFiles = mutableSetOf<File>()

	fun getSelectedFiles() = selectedFiles.toList()
	fun clearSelection() {
		selectedFiles.clear()
		notifyDataSetChanged()
	}

	fun toggleSelection(file: File) {
		val index = currentList.indexOf(file)
		if (selectedFiles.contains(file)) selectedFiles.remove(file) else selectedFiles.add(file)
		notifyItemChanged(index)
	}

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
			root.setOnLongClickListener {
				onLongClick(file)
				true
			}
			tvFileDateModified.text = getLastModifiedDate(file, root.context)

			tvFileSize.text = if (file.isDirectory) {
				"${countContentInFolder(file)} items"
			} else {
				getFormattedFileSize(file)
			}
			root.isChecked = selectedFiles.contains(file)

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


	}

}