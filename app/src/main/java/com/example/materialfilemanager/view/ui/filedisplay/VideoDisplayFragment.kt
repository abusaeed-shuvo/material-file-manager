package com.example.materialfilemanager.view.ui.filedisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import com.example.materialfilemanager.databinding.FragmentVideoDisplayBinding


class VideoDisplayFragment : Fragment() {
	private var _binding: FragmentVideoDisplayBinding? = null
	private val binding get() = _binding!!

	private lateinit var player: ExoPlayer

	private val args: VideoDisplayFragmentArgs by navArgs()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentVideoDisplayBinding.inflate(inflater, container, false)
		// Inflate the layout for this fragment
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val videoPath = args.vidPath.toUri()

		player = ExoPlayer.Builder(requireContext()).build()
		binding.playerView.player = player

		val mediaItem = MediaItem.fromUri(videoPath)
		player.setMediaItem(mediaItem)
		player.prepare()
		player.playWhenReady = true


	}


	override fun onDestroyView() {
		super.onDestroyView()
		player.release()
		_binding = null
	}

}