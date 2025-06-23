package com.example.materialfilemanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.materialfilemanager.AdditionalFunction.formatFileSize
import com.example.materialfilemanager.databinding.ItemStorageBinding
import com.example.materialfilemanager.model.data.StorageInfo

class StorageListAdapter(
	private val items: List<StorageInfo>,
	private val onItemClick: (StorageInfo) -> Unit
) :
	RecyclerView.Adapter<StorageListAdapter.StorageViewHolder>() {
	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): StorageViewHolder {
		return StorageViewHolder(
			ItemStorageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		)
	}

	override fun onBindViewHolder(
		holder: StorageViewHolder,
		position: Int
	) {
		val item = items[position]
		holder.bind(item)
	}

	override fun getItemCount(): Int = items.size

	inner class StorageViewHolder(private val binding: ItemStorageBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(item: StorageInfo) = with(binding) {
			ivStorageIcon.setImageDrawable(
				ContextCompat.getDrawable(
					binding.root.context,
					item.iconRes
				)
			)
			tvStorageName.text = item.name
			val usedSize = item.usedBytes
			val totalSize = item.totalBytes
			val storageSize = "${formatFileSize(usedSize)}/${formatFileSize(totalSize)}"
			val percentageUsed = (usedSize.toDouble() / totalSize.toDouble() * 100).toInt()

			tvStorageSize.text = storageSize
			tvStorageUsedPercentage.text = "$percentageUsed%"
			storageIndicator.setProgress(percentageUsed, true)
			root.setOnClickListener {
				onItemClick(item)
			}
		}
	}
}