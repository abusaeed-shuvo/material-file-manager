package com.example.materialfilemanager.view.ui.documentfilelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.materialfilemanager.databinding.FragmentDocumentFileListBinding
import com.example.materialfilemanager.view.adapter.DocumentFileListAdapter
import com.example.materialfilemanager.viewmodel.FileViewModel

class DocumentFileListFragment : Fragment() {
	private var _binding: FragmentDocumentFileListBinding? = null
	private val binding get() = _binding!!

	private val viewModel: FileViewModel by activityViewModels()
	private lateinit var adapter: DocumentFileListAdapter
	private val args: DocumentFileListFragmentArgs by navArgs()


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentDocumentFileListBinding.inflate(inflater, container, false)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val homeDir = args.rootPath
		setupRecyclerView()


		viewModel.fileDocumentList.observe(viewLifecycleOwner) { files ->
			adapter.submitList(files)
			binding.loadingIndicator.visibility = View.GONE
		}

		val rootUri = args.rootPath.toUri()
		if (viewModel.currentDirectory.value == null) {
			val rootDoc = DocumentFile.fromTreeUri(requireContext(), rootUri)
			if (rootDoc != null && rootDoc.canRead()) {
				binding.loadingIndicator.visibility = View.VISIBLE
				viewModel.loadFromDocumentFile(rootDoc)
			} else {
				Toast.makeText(requireContext(), "Invalid URI", Toast.LENGTH_SHORT).show()
			}
		}
		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner, object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					val current = viewModel.currentDocumentDirectory.value
					val parent = current?.parentFile
					if (parent != null && parent.uri.toString() != args.rootPath) {
						viewModel.loadFromDocumentFile(parent)
						updateToolbar(parent)
					} else {
						findNavController().popBackStack()
					}
				}
			})

	}

	private fun setupRecyclerView() {
		adapter = DocumentFileListAdapter(onClick = { file ->
			if (file.isDirectory) {
				viewModel.loadFromDocumentFile(file)
				updateToolbar(file)
			} else {
				Toast.makeText(requireContext(), "Clicked: ${file.name}", Toast.LENGTH_SHORT).show()
			}
		}, onLongClick = {
			Toast.makeText(requireContext(), "LongClicked", Toast.LENGTH_SHORT).show()
		})
		binding.recyclerviewViewDocumentFiles.layoutManager = LinearLayoutManager(requireContext())

		binding.recyclerviewViewDocumentFiles.adapter = adapter
	}

	private fun updateToolbar(file: DocumentFile?) {
		val title = file?.name ?: "Storage"
		(requireActivity() as AppCompatActivity).supportActionBar?.title = title
	}


	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}