package com.vkpapps.thunder.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.room.liveViewModel.RequestViewModel
import com.vkpapps.thunder.ui.adapter.RequestAdapter
import com.vkpapps.thunder.utils.AdsUtils.getAdRequest

/***
 * @author VIJAY PATIDAR
 */
class TransferringFragment : Fragment() {
    var pendingTransferringCount: AppCompatTextView? = null
    var pendingTransferringCountProgress: View? = null
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

        //adapter
        val adapter = RequestAdapter(requireContext())
        recyclerView.adapter = adapter
        val viewModel = ViewModelProvider(requireActivity()).get(RequestViewModel::class.java)
        viewModel.requestInfosLiveData.observe(requireActivity(), Observer { requestInfos: List<RequestInfo> ->
            if (requestInfos.isNotEmpty()) {
                adapter.setRequestInfos(requestInfos)
                view.findViewById<View>(R.id.emptyRequestList).visibility = View.GONE
            } else {
                view.findViewById<View>(R.id.emptyRequestList).visibility = View.VISIBLE
            }
        })
        viewModel.pendingRequestCountLiveData.observe(requireActivity(), Observer {
            pendingTransferringCount?.text = if (it != 0) {
                pendingTransferringCountProgress?.visibility = View.VISIBLE
                if (it <= 100) it.toString() else "99+"
            } else {
                pendingTransferringCountProgress?.visibility = View.GONE
                ""
            }
        })
        val adView: AdView = view.findViewById(R.id.adView)
        getAdRequest(adView)


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.transferring_menu, menu)
        menu.findItem(R.id.menu_transferring).isVisible = false
        menu.findItem(R.id.menu_sorting).isVisible = false
        menu.findItem(R.id.menu_transferring_count).actionView.apply {
            pendingTransferringCount = findViewById(R.id.pendingTransferringCount)
            pendingTransferringCountProgress = findViewById(R.id.pendingTransferringCountProgress)
        }
    }
}