package com.example.materialfilemanager.view.ui.filedisplay

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.materialfilemanager.databinding.FragmentImageDisplayBinding
import com.example.materialfilemanager.view.adapter.ImagePagerAdapter
import com.example.materialfilemanager.view.adapter.ThumbnailListAdapter
import com.example.materialfilemanager.viewmodel.FileViewModel

class ImageDisplayFragment : Fragment() {

	private var _binding: FragmentImageDisplayBinding? = null
	private val binding get() = _binding!!

	private lateinit var pagerAdapter: ImagePagerAdapter
	private lateinit var thumbnailAdapter: ThumbnailListAdapter
	private val viewModel: FileViewModel by activityViewModels()
	private lateinit var imageUris: List<Uri>
	private val args: ImageDisplayFragmentArgs by navArgs()
	var showTopBarAndBottomBar = true

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentImageDisplayBinding.inflate(inflater, container, false)
		// Inflate the layout for this fragment
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		imageUris = args.imageUris.map { it.toUri() }
		val displayImgPos = args.imgIndex
		val uri = imageUris[displayImgPos]

		val file = uri.toFile()
		val fileName = file.nameWithoutExtension
		updateToolbar(fileName)

		val viewPager = binding.imageViewPager2
		val thumbnailRecycler = binding.recyclerViewThumbnail

		pagerAdapter = ImagePagerAdapter(imageUris) { uri ->
			showTopBarAndBottomBar = !showTopBarAndBottomBar
			val controller = WindowInsetsControllerCompat(requireActivity().window, requireView())

			if (showTopBarAndBottomBar) {
				binding.recyclerViewThumbnail.visibility = View.VISIBLE
				(requireActivity() as AppCompatActivity).supportActionBar?.show()
				WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)

				WindowInsetsControllerCompat(requireActivity().window, requireView()).show(
						WindowInsetsCompat.Type.statusBars()
					)
			} else {
				binding.recyclerViewThumbnail.visibility = View.INVISIBLE
				(requireActivity() as AppCompatActivity).supportActionBar?.hide()
				controller.hide(WindowInsetsCompat.Type.systemBars())
				controller.systemBarsBehavior =
					WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


			}
		}
		viewPager.adapter = pagerAdapter
		viewPager.setCurrentItem(displayImgPos, false)
		viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				thumbnailAdapter.selectedIndex = position
				thumbnailAdapter.notifyDataSetChanged()
				thumbnailRecycler.smoothScrollToPosition(position)

				val fileName = imageUris[position].toFile().nameWithoutExtension
				updateToolbar(fileName)
			}
		})

		thumbnailAdapter = ThumbnailListAdapter(imageUris, onClick = { position ->
			viewPager.currentItem = position
		})

		thumbnailRecycler.adapter = thumbnailAdapter
		thumbnailRecycler.layoutManager =
			LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

		viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				thumbnailAdapter.selectedIndex = position
				thumbnailAdapter.notifyDataSetChanged()
				thumbnailRecycler.smoothScrollToPosition(position)
			}
		})

		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					findNavController().popBackStack()
				}

			})

	}

	private fun updateToolbar(title: String) {

		val activity = requireActivity() as AppCompatActivity
		val currentTitle = title

		activity.supportActionBar?.title = currentTitle
	}


	override fun onDestroyView() {
		super.onDestroyView()
		(requireActivity() as AppCompatActivity).supportActionBar?.show()
		WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)

		WindowInsetsControllerCompat(requireActivity().window, requireView()).show(
				WindowInsetsCompat.Type.statusBars()
			)
		_binding = null
	}


}