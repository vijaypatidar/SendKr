package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import com.google.android.gms.ads.AdView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.model.constaints.StatusType
import com.vkpapps.thunder.room.liveViewModel.RequestViewModel
import com.vkpapps.thunder.ui.adapter.RequestAdapter
import com.vkpapps.thunder.utils.AdsUtils.getAdRequest
import kotlinx.android.synthetic.main.fragment_transfering.*

/***
 * @author VIJAY PATIDAR
 */
class TransferringFragment : Fragment() {
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfering, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val recyclerView: RecyclerView = view.findViewById(R.id.requestList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (onNavigationVisibilityListener != null) {
                    onNavigationVisibilityListener!!.onNavVisibilityChange(velocityY < 0)
                }
                return false
            }
        }
        //adapter
        val adapter = RequestAdapter(requireContext())
        recyclerView.adapter = adapter
        val viewModel = ViewModelProvider(requireActivity()).get(RequestViewModel::class.java)
        viewModel.allRequestInfo.observe(requireActivity(), Observer { requestInfos: List<RequestInfo> ->
            if (requestInfos.size > 0) {
                adapter.setRequestInfos(requestInfos)
                setTransferringDetail(requestInfos)
                view.findViewById<View>(R.id.emptyRequestList).visibility = View.GONE
            } else {
                view.findViewById<View>(R.id.emptyRequestList).visibility = View.VISIBLE
            }
        })
        val adView: AdView = view.findViewById(R.id.adView)
        getAdRequest(adView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring).isVisible = false
    }

    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = context
            onNavigationVisibilityListener!!.onNavVisibilityChange(false)
        }
    }

    private fun setTransferringDetail(requestInfos: List<RequestInfo>) {
        var sentCount = 0
        var receivedCount = 0
        var failedCount = 0
        var pendingCount = 0
        requestInfos.forEach {

            when (it.status) {
                StatusType.STATUS_ONGOING -> pendingCount++
                StatusType.STATUS_COMPLETED -> {
                    sentCount++
                    receivedCount++
                }
                StatusType.STATUS_FAILED -> failedCount++
                StatusType.STATUS_PENDING -> pendingCount++
            }

        }
        sent.text = "sent($sentCount)"
        received.text = "received($receivedCount)"
        failed.text = "failed($failedCount)"

    }
}