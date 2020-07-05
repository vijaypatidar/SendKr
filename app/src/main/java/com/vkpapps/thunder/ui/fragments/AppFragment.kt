package com.vkpapps.thunder.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.vkpapps.thunder.R
import com.vkpapps.thunder.aysnc.PrepareAppList
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.AppInfo
import com.vkpapps.thunder.ui.adapter.AppAdapter
import com.vkpapps.thunder.ui.dialog.LoadingDialogs

/***
 * @author VIJAY PATIDAR
 */
class AppFragment : Fragment(), PrepareAppList.OnAppListPrepareListener {

    private val appInfos = ArrayList<AppInfo>()
    private var adapter: AppAdapter? = null
    private var loadingDialog: AlertDialog? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.appList)
        adapter = AppAdapter(appInfos)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        val prepareAppList = PrepareAppList(this);
        prepareAppList.execute()
        loadingDialog = LoadingDialogs(requireContext()).loadingDialog
        loadingDialog?.show()
    }

    override fun onAppListPrepared(appInfos: List<AppInfo>) {
        this.appInfos.clear();
        this.appInfos.addAll(appInfos)
        adapter?.notifyDataSetChanged()
        loadingDialog?.cancel()
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
}