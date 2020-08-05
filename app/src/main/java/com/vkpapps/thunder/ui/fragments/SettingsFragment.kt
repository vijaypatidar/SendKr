package com.vkpapps.thunder.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vkpapps.thunder.R
import com.vkpapps.thunder.room.liveViewModel.HistoryViewModel
import com.vkpapps.thunder.utils.StorageManager
import kotlinx.android.synthetic.main.fragment_settings.*

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
        customDownloadPath.text = StorageManager(requireContext()).downloadDir.absolutePath

        btnClearHistory.setOnClickListener {
            ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java).deleteAll()
            Toast.makeText(requireContext(), getString(R.string.history_cleared_message), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring).isVisible = false
        menu.findItem(R.id.menu_sorting).isVisible = false
    }


}