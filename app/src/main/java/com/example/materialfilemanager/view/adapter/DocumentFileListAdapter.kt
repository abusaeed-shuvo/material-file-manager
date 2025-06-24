package com.example.materialfilemanager.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.materialfilemanager.AdditionalFunction
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.ItemFileBinding

class DocumentFileListAdapter(
	private val onClick: (DocumentFile) -> Unit,
	private val onLongClick: (DocumentFile) -> Unit
) : ListAdapter<DocumentFile, DocumentFileListAdapter.DocumentFileViewHolder>(DiffCallBack) {
	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): DocumentFileViewHolder {
		return DocumentFileViewHolder(
			ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)
	}

	override fun onBindViewHolder(
		holder: DocumentFileViewHolder,
		position: Int
	) {
		holder.bind(getItem(position))
	}

	inner class DocumentFileViewHolder(private val binding: ItemFileBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(file: DocumentFile) = with(binding) {
			tvFileName.text = file.name
			tvFileDateModified.text = getLastModifiedDate(file)
			tvFileSize.text = if (file.isDirectory) {
				"${file.listFiles().size} items"
			} else {
				getFormattedFileSize(file)
			}
			iconFile.setImageResource(
				if (file.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
			)
			root.setOnClickListener { onClick }
			root.setOnLongClickListener {
				onLongClick
				true
			}

		}
	}

	companion object {
		private val DiffCallBack = object : DiffUtil.ItemCallback<DocumentFile>() {
			override fun areItemsTheSame(
				oldItem: DocumentFile,
				newItem: DocumentFile
			): Boolean = oldItem.uri == newItem.uri

			override fun areContentsTheSame(
				oldItem: DocumentFile,
				newItem: DocumentFile
			): Boolean = oldItem.uri == newItem.uri && oldItem.name == newItem.name


		}

		private fun countContentInFolder(folder: DocumentFile): Int {
			if (!folder.exists() || !folder.isDirectory) return 0

			val count = folder.listFiles().count()

			return count
		}

		private fun getLastModifiedDate(file: DocumentFile): String {
			val lastModified = file.lastModified()

			return AdditionalFunction.getFormattedTime(lastModified)

		}

		private fun getFormattedFileSize(file: DocumentFile): String {
			val sizeInBytes = file.length()
			return AdditionalFunction.formatFileSize(sizeInBytes)
		}
	}
}