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
import com.vkpapps.thunder.R
import com.vkpapps.thunder.connection.ClientHelper
import com.vkpapps.thunder.interfaces.OnFragmentAttachStatusListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.interfaces.OnUserListRequestListener
import com.vkpapps.thunder.interfaces.OnUsersUpdateListener
import com.vkpapps.thunder.ui.adapter.ClientAdapter

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
        if (users == null) return
        clientAdapter = ClientAdapter(users, view)
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
        clientAdapter?.notifyDataSetChangedAndHideIfNull()
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onNavigationVisibilityListener = context as OnNavigationVisibilityListener
        onUserListRequestListener = context as OnUserListRequestListener
        onFragmentAttachStatusListener = context as OnFragmentAttachStatusListener
        onFragmentAttachStatusListener?.onFragmentAttached(this)
        users = onUserListRequestListener?.onRequestUsers()
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentAttachStatusListener?.onFragmentDetached(this)
        onUserListRequestListener = null
        onNavigationVisibilityListener = null
        onFragmentAttachStatusListener = null
    }

    override fun onUserUpdated() {
        clientAdapter?.notifyDataSetChangedAndHideIfNull()
    }
}