package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFileRequestPrepareListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.HistoryInfo
import com.vkpapps.thunder.model.RawRequestInfo
import com.vkpapps.thunder.room.liveViewModel.HistoryViewModel
import com.vkpapps.thunder.ui.adapter.HistoryAdapter
import com.vkpapps.thunder.utils.AdsUtils
import com.vkpapps.thunder.utils.StorageManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.selection_options.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class HomeFragment : Fragment(), HistoryAdapter.OnHistorySelectListener {
    private var selectedCount = 0
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var navController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        navController = Navigation.findNavController(view)
        val controller = Navigation.findNavController(view)
        progressBarInternal.max = 100
        val storage = StorageManager(requireContext())
        progressBarInternal.progress = (storage.internal.freeSpace * 100 / storage.internal.totalSpace).toInt()

        photo.setOnClickListener {
            controller.navigate(getDestination(0))
        }
        audio.setOnClickListener {
            controller.navigate(getDestination(1))
        }
        video.setOnClickListener {
            controller.navigate(getDestination(2))
        }
        files.setOnClickListener {
            controller.navigate(getDestination(3))
        }

        internal.setOnClickListener {
            val internal = StorageManager(requireContext()).internal
            Navigation.findNavController(view).navigate(object : NavDirections {
                override fun getActionId(): Int {
                    return R.id.fileFragment
                }

                override fun getArguments(): Bundle {
                    val bundle = Bundle()
                    bundle.putString(FileFragment.FILE_ROOT, internal.absolutePath)
                    bundle.putString(FileFragment.FRAGMENT_TITLE, "Internal Storage")
                    return bundle
                }
            })
        }
        external.setOnClickListener {

            Navigation.findNavController(view).navigate(object : NavDirections {
                override fun getActionId(): Int {
                    return R.id.fileFragment
                }

                override fun getArguments(): Bundle {
                    val bundle = Bundle()
                    bundle.putString(FileFragment.FILE_ROOT, "/storage/")
                    bundle.putString(FileFragment.FRAGMENT_TITLE, "External Storage")
                    return bundle
                }
            })

        }
        setupHistory()

        AdsUtils.getAdRequest(adView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
        val findItem = menu.findItem(R.id.menu_transferring)
        findItem?.actionView?.findViewById<CardView>(R.id.transferringActionView)?.setOnClickListener {
            navController?.navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle()
                }

                override fun getActionId(): Int {
                    return R.id.action_navigation_home_to_transferringFragment
                }

            })
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share -> {
                Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener = null
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

    private fun getDestination(des: Int): NavDirections {
        return object : NavDirections {
            override fun getArguments(): Bundle {
                val bundle = Bundle()
                bundle.putInt(GenericFragment.PARAM_DESTINATION, des)
                return bundle
            }

            override fun getActionId(): Int {
                return R.id.action_navigation_home_to_navigation_generic
            }

        }
    }

    private fun hideShowSendButton() {
        if (selectionSection.visibility == View.VISIBLE && selectedCount > 0) {
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
            return
        }
        if (selectedCount == 0) {
            selectionSection.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fragment_fade_exit)
            selectionSection.visibility = View.GONE
            onNavigationVisibilityListener?.onNavVisibilityChange(true)
        } else {
            selectionSection.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fragment_fade_enter)
            selectionSection.visibility = View.VISIBLE
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
        }
    }

    private fun setupHistory() {
        history.layoutManager = LinearLayoutManager(requireContext())
        val adapter = HistoryAdapter(requireContext(), this)
        history.adapter = adapter
        history.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectedCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        val historyViewModel = ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java)
        val historyInfos = ArrayList<HistoryInfo>()
        historyViewModel.historyInfos.observe(requireActivity(), androidx.lifecycle.Observer {
            CoroutineScope(IO).launch {
                historyInfos.clear()
                historyInfos.addAll(it)
                withContext(Main) {
                    adapter.setHistoryInfos(it)
                }
            }
        })

        btnSendFiles.setOnClickListener {

            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                historyInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.source, it.type
                        ))
                    }
                }
                selectedCount = 0
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                    Toast.makeText(requireContext(), "${selected.size} files added to send queue", Toast.LENGTH_SHORT).show()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        btnNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
                historyInfos.forEach {
                    it.isSelected = false
                }
                selectedCount = 0
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

        btnAll.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                selectedCount = 0
                historyInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
    }

    override fun onHistorySelected(historyInfo: HistoryInfo) {
        selectedCount++
        hideShowSendButton()
    }

    override fun onHistoryDeselected(historyInfo: HistoryInfo) {
        selectedCount--
        hideShowSendButton()
    }
}