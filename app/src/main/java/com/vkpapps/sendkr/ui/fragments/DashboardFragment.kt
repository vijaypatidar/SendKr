package com.vkpapps.sendkr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.squareup.picasso.Picasso
import com.vkpapps.apmanager.APManager
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.connection.ClientHelper
import com.vkpapps.sendkr.interfaces.OnFragmentAttachStatusListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.interfaces.OnUserListRequestListener
import com.vkpapps.sendkr.interfaces.OnUsersUpdateListener
import com.vkpapps.sendkr.ui.activity.MainActivity
import com.vkpapps.sendkr.ui.adapter.ClientAdapter
import com.vkpapps.sendkr.ui.dialog.DialogsUtils
import com.vkpapps.sendkr.ui.fragments.base.MyFragment
import com.vkpapps.sendkr.utils.AdsUtils
import com.vkpapps.sendkr.utils.StorageManager
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
class DashboardFragment : MyFragment(), OnUsersUpdateListener {
    private var users: List<ClientHelper>? = null
    private var onUserListRequestListener: OnUserListRequestListener? = null
    private var clientAdapter: ClientAdapter? = null
    private var onFragmentAttachStatusListener: OnFragmentAttachStatusListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Nothing to display when user is client
        setHasOptionsMenu(true)
        users = onUserListRequestListener?.onRequestUsers()
        clientAdapter = ClientAdapter()
        val recyclerView: RecyclerView = view.findViewById(R.id.clientList)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        recyclerView.adapter = clientAdapter
        updateList()
        val code = File(StorageManager.userDir, "code.png")
        val picasso = Picasso.get()
        picasso.invalidate(code)
        picasso.load(code).fit().into(barCodeImage)

        AdsUtils.getAdRequest(adView)

        btnShutDown.setOnClickListener {
            DialogsUtils(requireContext()).closeGroup({
                controller?.popBackStack()
                if (MainActivity.isHost) {
                    APManager.getApManager(requireContext()).disableWifiAp()
                    MainActivity.serverHelper.shutDown()
                } else {
                    MainActivity.clientHelper.shutDown()
                }
                MainActivity.connected = false
                MainActivity.pendingRequest.clear()
                requireActivity().recreate()
            }, null)
        }
        barCodeImage.setOnClickListener {
            DialogsUtils(requireContext()).displayQRCode()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val findItem = menu.findItem(R.id.menu_transferring)
        findItem?.actionView?.findViewById<CardView>(R.id.transferringActionView)?.setOnClickListener {
            controller?.navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle()
                }
                override fun getActionId(): Int {
                    return R.id.action_navigation_dashboard_to_transferringFragment
                }

            })
        }
        menu.findItem(R.id.menu_sorting).isVisible = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onNavigationVisibilityListener = context as OnNavigationVisibilityListener
        onUserListRequestListener = context as OnUserListRequestListener
        onFragmentAttachStatusListener = context as OnFragmentAttachStatusListener
        onFragmentAttachStatusListener?.onFragmentAttached(this)
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentAttachStatusListener?.onFragmentDetached(this)
        onUserListRequestListener = null
        onNavigationVisibilityListener = null
        onFragmentAttachStatusListener = null
    }

    override fun onUserUpdated() {
        updateList()
    }

    private fun updateList() {
        onUserListRequestListener?.onRequestUsers()?.run {
            if (!this.isNullOrEmpty()) {
                clientAdapter?.setUsers(this)
                emptyClient?.visibility = View.GONE
            } else {
                emptyClient?.visibility = View.VISIBLE
            }
        }
    }
}