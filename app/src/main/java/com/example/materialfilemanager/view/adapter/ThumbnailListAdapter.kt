package com.example.materialfilemanager.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.materialfilemanager.databinding.ItemThumbnailBinding

class ThumbnailListAdapter(
	private val imageUris: List<Uri>,
	private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ThumbnailListAdapter.ThumbnailHolder>() {
	var selectedIndex = 0
	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ThumbnailHolder {
		return ThumbnailHolder(
			ItemThumbnailBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(
		holder: ThumbnailHolder,
		position: Int
	) {
		holder.bind(imageUris[position], position == selectedIndex)
	}

	override fun getItemCount(): Int = imageUris.size

	inner class ThumbnailHolder(private val binding: ItemThumbnailBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(uri: Uri, isSelected: Boolean) {

			binding.apply {
				imgThumbnail.setImageURI(uri)
				root.alpha = if (isSelected) 1f else 0.5f
				root.setOnClickListener {
					onClick(absoluteAdapterPosition)
				}
			}
		}
	}
}