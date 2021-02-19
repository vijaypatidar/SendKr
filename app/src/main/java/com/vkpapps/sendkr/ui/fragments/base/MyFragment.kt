package com.vkpapps.sendkr.ui.fragments.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import kotlinx.android.synthetic.main.fragment_media_base.*

/***
 * @author VIJAY PATIDAR
 *
 *
 * Base class that provide basic method needed for sharing file,
 * handle navigation bar visibility and provide NavController
 * for handling destination.
 */
abstract class MyFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    protected var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    protected var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    protected var controller: NavController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        initSelectionUI()

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

    protected open fun onSelectAll(){}
    protected open fun onSelectNon(){}
    protected open fun onSendSelected(){}

    private fun initSelectionUI() {
        selectionView?.btnSendFiles?.setOnClickListener {
            onSendSelected()
        }
        selectionView?.btnSelectNon?.setOnClickListener {
            onSelectNon()
        }
        selectionView?.btnSelectAll?.setOnClickListener {
            onSelectAll()
        }
    }

    override fun onRefresh(){

    }
}