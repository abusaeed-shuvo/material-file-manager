package com.example.materialfilemanager.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.ItemImageBinding

class ImagePagerAdapter(private val imageUris: List<Uri>, private val onClick: (Uri) -> Unit) :
	RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {
	override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int
	): ImageViewHolder {
		return ImageViewHolder(
			ItemImageBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(
		holder: ImageViewHolder, position: Int
	) {
		holder.bind(imageUris[position])
	}

	override fun getItemCount(): Int = imageUris.size

	inner class ImageViewHolder(private val binding: ItemImageBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(uri: Uri) = with(binding) {
			Glide.with(root.context)
				.load(uri)
				.placeholder(R.drawable.ic_img)
				.error(R.drawable.ic_error)
				.into(imageView)

			root.setOnClickListener { onClick(uri) }
		}
	}
}