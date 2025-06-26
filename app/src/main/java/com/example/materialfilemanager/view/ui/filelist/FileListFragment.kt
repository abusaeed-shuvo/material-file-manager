package com.example.materialfilemanager.view.ui.filelist

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
	private lateinit var menuHost: MenuHost
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
		val rootPath = args.rootPath
		menuHost = requireActivity()

		setupMenu()
		setupRecyclerView()

		if (rootPath != null) {

			val file = File(rootPath)
			viewModel.loadFiles(file)
			updateToolbar()

		}


		viewModel.fileList.observe(viewLifecycleOwner) { items ->
			val fileList = items
			binding.loadingIndicator.visibility = View.GONE
			adapter.submitList(fileList)
		}

		binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
			when (menuItem.itemId) {


				else -> false
			}
		}


		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner, object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					val currentDir = viewModel.getCurrentDirectory()

					if (isSelectionMode) {
						isSelectionMode = false
						updateToolbar()
					} else {
						if (currentDir == null || currentDir.path == rootPath) {
							findNavController().popBackStack()
						} else {
							viewModel.goUp()
							updateToolbar()
						}
					}

				}
			})
	}

	private fun setupMenu() {
		menuHost.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(
				menu: Menu, menuInflater: MenuInflater
			) {
				menuInflater.inflate(R.menu.menu_file_list, menu)
			}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				return when (menuItem.itemId) {
					R.id.action_sort           -> {
						showSortAndFilterBottomSheet()
						true
					}

					R.id.action_add_new_file   -> {
						showAddDialog(false)

						true
					}

					R.id.action_add_new_folder -> {
						showAddDialog(true)
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
	}


	private fun setupRecyclerView() {
		binding.recyclerviewViewFiles.layoutManager = LinearLayoutManager(requireContext())
		adapter = FileListAdapter(onClick = { file ->
			if (isSelectionMode) {
				adapter.toggleSelection(file)
				updateToolbar()
			} else {
				if (file.isDirectory) {
					viewModel.loadFiles(file)
					updateToolbar()
				} else {
					val currentDir = viewModel.getCurrentDirectory()
					when (FileTypes.getFileType(file)) {
						FileTypes.IMAGE   -> {
							val imageUris = currentDir?.listFiles()
								                ?.filter { ImageFormat.isImageFile(it.extension) }
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

		}, onLongClick = { file ->
			if (!isSelectionMode) {
				isSelectionMode = true
			}
			adapter.toggleSelection(file)
			updateToolbar()
		})

		binding.recyclerviewViewFiles.adapter = adapter
	}


	private fun showAddDialog(isFolder: Boolean) {

		val currentDirectory = viewModel.getCurrentDirectory() ?: return

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


	private fun updateToolbar() {
		val selectedCount = adapter.getSelectedFiles().size
		val name = viewModel.getCurrentDirectoryName()
		val activity = requireActivity() as AppCompatActivity

		if (isSelectionMode && selectedCount > 0) {
			activity.supportActionBar?.title = "$selectedCount selected"
			binding.bottomAppBar.visibility = View.VISIBLE
		} else {
			isSelectionMode = false
			adapter.clearSelection()
			activity.supportActionBar?.title = name
			binding.bottomAppBar.visibility = View.GONE

		}
		menuHost.invalidateMenu()
	}

	fun showSortAndFilterBottomSheet() {
		val dialog = BottomSheetDialog(requireContext())
		val dialogBinding = BottomSheetSortAndFilterBinding.inflate(layoutInflater)
		dialog.setContentView(dialogBinding.root)

		val prefs = PreferenceManager.getDefaultSharedPreferences(context)

		dialogBinding.apply {

		}

		dialog.show()
	}


	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

}