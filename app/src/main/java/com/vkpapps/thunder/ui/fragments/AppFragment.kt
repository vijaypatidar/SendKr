package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.loader.PrepareAppList
import com.vkpapps.thunder.model.AppInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.ui.adapter.AppAdapter
import kotlinx.android.synthetic.main.fragment_app.*
import kotlinx.android.synthetic.main.selection_options.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
class AppFragment : Fragment(), AppAdapter.OnAppSelectListener {

    private val appInfos = ArrayList<AppInfo>()
    private var adapter: AppAdapter? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    var selectedCount = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppAdapter(appInfos, this)
        appList.adapter = adapter
        appList.layoutManager = LinearLayoutManager(requireContext())
        appList.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        CoroutineScope(IO).launch {
            val list = PrepareAppList.appList
            appInfos.addAll(list)
            withContext(Main) {
                adapter?.notifyDataSetChanged()
            }
        }

        btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                appInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.source, FileType.FILE_TYPE_APP
                        ))
                    }
                    if (it.obbSource != null && it.isObbSelected) {
                        it.isObbSelected = false
                        selected.add(RawRequestInfo(
                                it.obbName!!, it.obbSource!!, FileType.FILE_TYPE_ANY
                        ))
                    }

                }
                selectedCount = 0
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                    Toast.makeText(requireContext(), "${selected.size} apps added to send queue", Toast.LENGTH_SHORT).show()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        btnNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                appInfos.forEach {
                    it.isSelected = false
                    if (it.obbSource != null) {
                        it.isObbSelected = false
                    }
                }
                selectedCount = 0
                withContext(Dispatchers.Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        btnAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectedCount = 0
                appInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                    if (it.obbSource != null) {
                        it.isObbSelected = true
                        selectedCount++
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = context
        }
        if (context is OnFileRequestPrepareListener) {
            onFileRequestPrepareListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener = null
        onFileRequestPrepareListener = null
    }

    override fun onAppSelected(appInfo: AppInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onAppDeselected(appInfo: AppInfo) {
        selectedCount--
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        if (selectionSection.visibility == View.VISIBLE && selectedCount > 0) return
        if (selectedCount > 0) {
            selectionSection.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fragment_fade_enter)
            selectionSection.visibility = View.VISIBLE
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
        } else {
            selectionSection.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fragment_fade_exit)
            selectionSection.visibility = View.GONE
            onNavigationVisibilityListener?.onNavVisibilityChange(true)
        }
    }
}