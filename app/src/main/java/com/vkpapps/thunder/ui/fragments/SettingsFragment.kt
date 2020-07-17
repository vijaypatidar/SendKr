package com.vkpapps.thunder.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        customDownloadPath.text = StorageManager(requireContext()).downloadDir.absolutePath

        btnClearHistory.setOnClickListener {
            ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java).deleteAll()
        }
    }

}