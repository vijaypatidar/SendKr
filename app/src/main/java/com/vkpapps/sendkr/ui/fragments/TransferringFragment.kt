package com.vkpapps.sendkr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnFileStatusChangeListener
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.room.liveViewModel.RequestViewModel
import com.vkpapps.sendkr.ui.adapter.RequestAdapter
import com.vkpapps.sendkr.utils.AdsUtils.getAdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/***
 * @author VIJAY PATIDAR
 */
class TransferringFragment : Fragment() {
    private var job: Job? = null
    private var onFileStatusChangeListener: OnFileStatusChangeListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfering, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        try {
            val recyclerView: RecyclerView = view.findViewById(R.id.requestList)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.itemAnimator = DefaultItemAnimator()

            //adapter
            val adapter = RequestAdapter(requireContext(), onFileStatusChangeListener!!)
            recyclerView.adapter = adapter
            val viewModel = ViewModelProvider(requireActivity()).get(RequestViewModel::class.java)
            viewModel.requestInfosLiveData.observe(requireActivity(), { requestInfos: List<RequestInfo> ->
                if (requestInfos.isNotEmpty()) {
                    adapter.setRequestInfos(requestInfos)
                    view.findViewById<View>(R.id.emptyRequestList).visibility = View.GONE
                } else {
                    view.findViewById<View>(R.id.emptyRequestList).visibility = View.VISIBLE
                }
            })

            val adView: AdView = view.findViewById(R.id.adView)
            getAdRequest(adView)

            job = CoroutineScope(Main).launch {
                while (!isDetached) {
                    viewModel.notifyDataSetChanged()
                    delay(1000)
                }
            }
        } catch (e: Exception) {
            Navigation.findNavController(view).popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFileStatusChangeListener) {
            onFileStatusChangeListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        job?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring).isVisible = false
        menu.findItem(R.id.menu_sorting).isVisible = false
    }
}