package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.squareup.picasso.Picasso
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.connection.ClientHelper
import com.vkpapps.thunder.interfaces.OnFragmentAttachStatusListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.interfaces.OnUserListRequestListener
import com.vkpapps.thunder.interfaces.OnUsersUpdateListener
import com.vkpapps.thunder.ui.adapter.ClientAdapter
import com.vkpapps.thunder.ui.dialog.DialogsUtils
import com.vkpapps.thunder.utils.AdsUtils
import com.vkpapps.thunder.utils.StorageManager
import com.vkpapps.thunder.utils.WifiApUtils
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
class DashboardFragment : Fragment(), OnUsersUpdateListener {
    private var users: List<ClientHelper>? = null
    private var onUserListRequestListener: OnUserListRequestListener? = null
    private var clientAdapter: ClientAdapter? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFragmentAttachStatusListener: OnFragmentAttachStatusListener? = null
    private var navController: NavController? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Nothing to display when user is client
        setHasOptionsMenu(true)
        navController = Navigation.findNavController(view)
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

        Picasso.get().load(File(StorageManager(App.context).userDir, "code.png")).fit().into(barCodeImage)

        AdsUtils.getAdRequest(adView)

        btnShutDown.setOnClickListener {
            WifiApUtils.disableWifiAp()
        }
        barCodeImage.setOnClickListener {
            DialogsUtils(requireContext()).displayQRCode()
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