package com.vkpapps.sendkr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.sendkr.App.Companion.isPhone
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.loader.PrepareAppList
import com.vkpapps.sendkr.model.AppInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.ui.adapter.AppAdapter
import com.vkpapps.sendkr.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_app.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
class AppFragment : Fragment(), AppAdapter.OnAppSelectListener {

    private val appInfos = ArrayList<AppInfo>()
    private var adapter: AppAdapter = AppAdapter(appInfos, this)
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    var selectedCount = 0
    private var navController: NavController? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        navController = Navigation.findNavController(view)
        appList.adapter = adapter
        val spanCount = if (isPhone) 1 else 2
        appList.layoutManager = GridLayoutManager(requireContext(), spanCount)
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
                adapter.notifyDataSetChanged()
                loadingApps.visibility = View.GONE
            }
        }

        selectionView.btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                appInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, FileType.FILE_TYPE_APP, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
                        ))
                    }
                    if (it.obbUri != null && it.isObbSelected) {
                        it.isObbSelected = false
                        selected.add(RawRequestInfo(
                                it.obbName!!, it.obbUri!!, FileType.FILE_TYPE_APP, MathUtils.getFileSize(DocumentFile.fromFile(it.obbUri!!.toFile())
                        )))
                    }

                }
                selectedCount = 0
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView.btnSelectNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                appInfos.forEach {
                    it.isSelected = false
                    if (it.obbUri != null) {
                        it.isObbSelected = false
                    }
                }
                selectedCount = 0
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        selectionView.btnSelectAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectedCount = 0
                appInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                    if (it.obbUri != null) {
                        it.isObbSelected = true
                        selectedCount++
                    }
                }
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val findItem = menu.findItem(R.id.menu_transferring)
        findItem?.actionView?.findViewById<CardView>(R.id.transferringActionView)?.setOnClickListener {
            navController?.navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle()
                }

                override fun getActionId(): Int {
                    return R.id.action_navigation_app_to_transferringFragment
                }

            })
        }
        menu.findItem(R.id.menu_sorting).isVisible = false

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
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)
        selectionView.changeVisibility(selectedCount)
    }

}