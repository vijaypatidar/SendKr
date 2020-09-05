package com.vkpapps.sendkr.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnSuccessListener
import com.vkpapps.sendkr.room.liveViewModel.HistoryViewModel
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.utils.KeyValue
import com.vkpapps.sendkr.utils.StorageManager
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File


/***
 * @author VIJAY PATIDAR
 */
class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val keyValue = KeyValue(requireContext())
        customDownloadPath.text = StorageManager(requireContext()).downloadDir.absolutePath

        btnClearHistory.setOnClickListener {
            DialogsUtils(requireContext()).clearHistoryDialog({
                ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java).deleteAll()
                Toast.makeText(requireContext(), getString(R.string.history_cleared_message), Toast.LENGTH_LONG).show()
            }, null)
        }

        btnChangeCustomPath.setOnClickListener {
            DialogsUtils(requireContext()).selectDir(object : OnSuccessListener<DocumentFile> {
                override fun onSuccess(t: DocumentFile) {
                    if (t.canWrite()) {
                        keyValue.customStoragePath = DocumentFile.fromFile(File(t.uri.toFile(), "SendKr")).uri.toFile().absolutePath
                        Toast.makeText(requireContext(), "Download location changed", Toast.LENGTH_SHORT).show()
                    } else {
                        KeyValue(requireContext()).customStoragePath = null
                        Toast.makeText(requireContext(), "Invalid Download location", Toast.LENGTH_SHORT).show()
                    }
                    customDownloadPath.text = StorageManager(requireContext()).downloadDir.absolutePath
                }
            }, null)
        }

        btnAbout.setOnClickListener {
            Navigation.findNavController(view).navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle().apply {
                    }
                }

                override fun getActionId(): Int {
                    return R.id.action_navigation_setting_to_navigation_about
                }
            })
        }
        checkboxShowHiddenFile.isChecked = keyValue.showHiddenFile
        btnShowHiddenFile.setOnClickListener {
            keyValue.showHiddenFile = !keyValue.showHiddenFile
            checkboxShowHiddenFile.isChecked = keyValue.showHiddenFile
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring).isVisible = false
        menu.findItem(R.id.menu_sorting).isVisible = false
    }


}