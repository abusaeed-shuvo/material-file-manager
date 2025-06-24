package com.example.materialfilemanager.view.ui.filelist

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.BottomSheetSortAndFilterBinding
import com.example.materialfilemanager.databinding.DialogRemoveFileBinding
import com.example.materialfilemanager.databinding.FragmentFileListBinding
import com.example.materialfilemanager.model.formats.FileTypes
import com.example.materialfilemanager.model.formats.ImageFormat
import com.example.materialfilemanager.view.adapter.FileListAdapter
import com.example.materialfilemanager.viewmodel.FileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.IOException


class FileListFragment : Fragment() {
	private var _binding: FragmentFileListBinding? = null
	private val binding get() = _binding!!

	private val viewModel: FileViewModel by activityViewModels()
	private lateinit var adapter: FileListAdapter
	private val args: FileListFragmentArgs by navArgs()
	private var isSelectionMode = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentFileListBinding.inflate(inflater, container, false)
		// Inflate the layout for this fragment
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val homeDir = args.rootPath
		val menuHost: MenuHost = requireActivity()

		menuHost.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(
				menu: Menu, menuInflater: MenuInflater
			) {
				menuInflater.inflate(R.menu.menu_file_list, menu)
			}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				return when (menuItem.itemId) {
					R.id.action_sort           -> {
						Toast.makeText(requireContext(), "Sort", Toast.LENGTH_SHORT).show()
						true
					}

					R.id.action_add_new_file   -> {
						Toast.makeText(requireContext(), "File", Toast.LENGTH_SHORT).show()

						true
					}

					R.id.action_add_new_folder -> {
						Toast.makeText(requireContext(), "Folder", Toast.LENGTH_SHORT).show()
						true
					}

					R.id.action_search         -> {
						Toast.makeText(requireContext(), "Search", Toast.LENGTH_SHORT).show()

						true
					}

					R.id.action_refresh        -> {
						Toast.makeText(requireContext(), "Refresh", Toast.LENGTH_SHORT).show()
						true
					}


					else                       -> false
				}
			}

		}, viewLifecycleOwner, Lifecycle.State.RESUMED)
		setupRecyclerView()

		updateToolbar()

		viewModel.fileList.observe(viewLifecycleOwner) { files ->
			adapter.submitList(files)
			binding.loadingIndicator.visibility = View.GONE
		}

		if (viewModel.currentDirectory.value == null) {
			val rootFile = File(args.rootPath)
			binding.loadingIndicator.visibility = View.VISIBLE
			viewModel.loadFiles(rootFile)
		} else {
			Toast.makeText(requireContext(), "Invalid path: ${args.rootPath}", Toast.LENGTH_SHORT)
				.show()
		}

		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner, object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					val currentDir = viewModel.currentDirectory.value
					if (currentDir != null) {
						if (currentDir.path == homeDir) {
							findNavController().popBackStack()
						} else {
							viewModel.goUp()
							updateToolbar(currentDir.parentFile!!)
						}
					}


				}

			})

	}


	private fun setupRecyclerView() {
		binding.recyclerviewViewFiles.layoutManager = LinearLayoutManager(requireContext())
		adapter = FileListAdapter { file ->
			if (file.isDirectory) {
				viewModel.loadFiles(file)
				updateToolbar(file)
			} else {
				val currentDir = viewModel.currentDirectory.value

				when (FileTypes.getFileType(file)) {
					FileTypes.IMAGE   -> {
						val imageUris =
							currentDir.listFiles()?.filter { ImageFormat.isImageFile(it.extension) }
								?.map { it.toUri().toString() } ?: emptyList()

						val position = imageUris.indexOf(file.toUri().toString())
						val action =
							FileListFragmentDirections.actionFileListFragmentToImageDisplayFragment(
								imageUris = imageUris.toTypedArray(), imgIndex = position
							)
						findNavController().navigate(action)
					}

					FileTypes.VIDEO   -> {
						val videoPath =
							file.path ?: "https://youtu.be/dQw4w9WgXcQ?si=NwimAxzVOlX1hyej"
						val action =
							FileListFragmentDirections.actionFileListFragmentToVideoDisplayFragment(
								vidPath = videoPath
							)
						findNavController().navigate(action)
					}

					FileTypes.UNKNOWN -> {
						Toast.makeText(
							requireContext(), "Clicked: ${file.name}", Toast.LENGTH_SHORT
						).show()
					}

					FileTypes.TEXT    -> {
						val textPath = file.path ?: ""

						val action =
							FileListFragmentDirections.actionFileListFragmentToEditTextViewerFragment(
								textPath
							)

						findNavController().navigate(action)
					}
				}

			}
		}

		binding.recyclerviewViewFiles.adapter = adapter
	}


	private fun showAddDialog(isFolder: Boolean) {

		val currentDirectory = viewModel.currentDirectory.value ?: return

		val dialogBinding = DialogRemoveFileBinding.inflate(layoutInflater)
		val inputField = dialogBinding.inputFileName
		val type = if (isFolder) "Folder" else "File"

		MaterialAlertDialogBuilder(requireContext()).setTitle("Create $type:")
			.setMessage("Add a $type:").setView(dialogBinding.root)
			.setPositiveButton("Create") { _, _ ->
				val name = inputField.text?.toString()?.trim()
				if (!name.isNullOrBlank()) {
					val target = File(currentDirectory, name)
					if (isFolder) {
						if (target.mkdir()) {
							viewModel.loadFiles(currentDirectory)
						} else {
							Toast.makeText(
								requireContext(), "Failed to create folder!", Toast.LENGTH_SHORT
							).show()
						}
					} else {
						try {
							if (target.createNewFile()) {
								viewModel.loadFiles(currentDirectory)
							} else {
								Toast.makeText(
									requireContext(), "Failed to create file!", Toast.LENGTH_SHORT
								).show()
							}
						} catch (e: IOException) {
							Toast.makeText(
								requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT
							).show()
						}
					}
				}
			}.setNegativeButton("Cancel", null).show()

	}


	private fun updateToolbar(file: File? = viewModel.currentDirectory.value) {

		val activity = requireActivity() as AppCompatActivity
		val currentFile = file
		val currentTitle = currentFile?.name ?: "Storage"

		activity.supportActionBar?.title = currentTitle
	}

	fun showSortAndFilterBottomSheet(context: Context) {
		val dialog = BottomSheetDialog(context)
		val dialogBinding = BottomSheetSortAndFilterBinding.inflate(LayoutInflater.from(context))

		dialog.setContentView(dialogBinding.root)

		val prefs = PreferenceManager.getDefaultSharedPreferences(context)

	}


	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}