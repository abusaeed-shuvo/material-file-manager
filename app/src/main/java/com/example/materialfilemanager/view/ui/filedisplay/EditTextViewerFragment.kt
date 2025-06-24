package com.example.materialfilemanager.view.ui.filedisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.example.materialfilemanager.R
import com.example.materialfilemanager.databinding.FragmentEditTextViewerBinding
import java.io.File
import java.io.IOException


class EditTextViewerFragment : Fragment() {
	private var _binding: FragmentEditTextViewerBinding? = null
	private val binding get() = _binding!!

	private val args: EditTextViewerFragmentArgs by navArgs()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		_binding = FragmentEditTextViewerBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val filePath = args.filePath
		val file = File(filePath)
		if (file.exists() && file.canRead()) {
			binding.textEditView.setText(file.readText())
		}

		val menuHost: MenuHost = requireActivity()

		menuHost.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(
				menu: Menu, menuInflater: MenuInflater
			) {
				menuInflater.inflate(R.menu.menu_edit_text, menu)
			}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				return when (menuItem.itemId) {
					R.id.action_save_text -> {
						saveTextToFile()
						true
					}

					else                  -> false
				}
			}

		}, viewLifecycleOwner, Lifecycle.State.RESUMED)

	}

	private fun saveTextToFile() {
		val text = binding.textEditView.text.toString()
		val file = File(args.filePath)
		try {
			file.writeText(text)
			Toast.makeText(requireContext(), "Saved Successfully!", Toast.LENGTH_SHORT).show()
		} catch (e: IOException) {
			Toast.makeText(requireContext(), "Failed to save: ${e.message}", Toast.LENGTH_SHORT)
				.show()
		}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}