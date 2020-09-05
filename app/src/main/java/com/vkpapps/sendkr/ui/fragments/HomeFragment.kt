package com.vkpapps.sendkr.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StatFs
import android.view.*
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.model.HistoryInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.room.liveViewModel.HistoryViewModel
import com.vkpapps.sendkr.ui.adapter.HistoryAdapter
import com.vkpapps.sendkr.utils.AdsUtils
import com.vkpapps.sendkr.utils.KeyValue
import com.vkpapps.sendkr.utils.MathUtils
import com.vkpapps.sendkr.utils.StorageManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class HomeFragment : Fragment(), HistoryAdapter.OnHistorySelectListener {
    private var selectedCount = 0
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var navController: NavController? = null
    private val historyViewModel: HistoryViewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        navController = Navigation.findNavController(view)
        val controller = Navigation.findNavController(view)

        var externalStoragePath = "/storage/"

        photo.setOnClickListener {
            controller.navigate(getDestination(0))
        }
        audio.setOnClickListener {
            controller.navigate(getDestination(1))
        }
        video.setOnClickListener {
            controller.navigate(getDestination(2))
        }

        documents.setOnClickListener {
            openQuickAccess(QuickAccessFragment.TYPE_DOCUMENTS)
        }
        apk.setOnClickListener {
            openQuickAccess(QuickAccessFragment.TYPE_APK)
        }
        zips.setOnClickListener {
            openQuickAccess(QuickAccessFragment.TYPE_ZIPS)
        }

        internal.setOnClickListener {
            val internal = StorageManager(requireContext()).internal
            Navigation.findNavController(view).navigate(object : NavDirections {
                override fun getActionId(): Int {
                    return R.id.fileFragment
                }

                override fun getArguments(): Bundle {
                    val bundle = Bundle()
                    bundle.putString(FileFragment.FILE_ROOT, Uri.fromFile(internal).toString())
                    bundle.putString(FileFragment.FRAGMENT_TITLE, "Internal Storage")
                    return bundle
                }
            })
        }
        external.setOnClickListener {
            Navigation.findNavController(view).navigate(object : NavDirections {
                override fun getActionId(): Int {
                    return R.id.action_navigation_home_to_files
                }

                override fun getArguments(): Bundle {
                    val bundle = Bundle()
                    bundle.putString(FileFragment.FILE_ROOT, DocumentFile.fromFile(File(externalStoragePath)).uri.toString())
                    bundle.putString(FileFragment.FRAGMENT_TITLE, "External Storage")
                    return bundle
                }
            })
        }
        setupHistory()

        AdsUtils.getAdRequest(adView)

        try {
            val statFs = StatFs(StorageManager(requireContext()).internal.path)
            internalProgressText.text = "${MathUtils.longToStringSizeGb(statFs.availableBytes.toDouble())} GB free"
            val progress = ((statFs.totalBytes - statFs.availableBytes) * 100 / statFs.totalBytes).toInt()
            progressBarInternal.progress = progress
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            ContextCompat.getExternalFilesDirs(requireContext(), null).forEach {
                if (!it.absoluteFile.startsWith("/storage/emulated/0/")) {
                    external.visibility = View.VISIBLE
                    externalStoragePath = it.absolutePath
                    val indexOf = externalStoragePath.indexOf("/Android")
                    if (indexOf != -1)
                        externalStoragePath = externalStoragePath.subSequence(0, indexOf).toString()
                    val statFs = StatFs(it.path)
                    externalProgressText.text = "${MathUtils.longToStringSizeGb(statFs.availableBytes.toDouble())} GB free"
                    val progress = ((statFs.totalBytes - statFs.availableBytes) * 100 / statFs.totalBytes).toInt()
                    progressBarExternal.progress = progress
                    KeyValue(requireContext()).externalStoragePath = externalStoragePath
                } else {
                    KeyValue(requireContext()).externalStoragePath = null
                    if (!DocumentFile.fromFile(StorageManager(requireContext()).downloadDir).exists()) {
                        KeyValue(requireContext()).customStoragePath = null
                    }
                }
            }
        } catch (e: Exception) {
            external.visibility = View.GONE
            e.printStackTrace()
        }
    }

    private fun openQuickAccess(type: Int = QuickAccessFragment.TYPE_DOCUMENTS) {
        navController?.navigate(object : NavDirections {
            override fun getActionId(): Int {
                return R.id.action_navigation_home_to_quickAccessFragment
            }

            override fun getArguments(): Bundle {
                return Bundle().apply {
                    putInt(QuickAccessFragment.PARAM_TYPE, type)
                }
            }
        })

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
        //hide filter button
        menu.findItem(R.id.menu_sorting).isVisible = false
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share -> {
                val share = resources.getString(R.string.app_share_message)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, share)
                requireActivity().startActivity(Intent.createChooser(intent, "Share with"))
            }
            R.id.menu_feedback -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:vkramotiya987@gmail.com")
                intent.putExtra(Intent.EXTRA_SUBJECT, "SendKr feedback")
                intent.putExtra(Intent.EXTRA_TEXT, "Hi! Developer\n")
                requireActivity().startActivity(Intent.createChooser(intent, "Send with"))
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
                bundle.putInt(MediaFragment.PARAM_DESTINATION, des)
                return bundle
            }

            override fun getActionId(): Int {
                return R.id.action_navigation_home_to_navigation_files
            }
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
        val historyInfos = ArrayList<HistoryInfo>()
        historyViewModel.historyInfos.observe(requireActivity(), {
            CoroutineScope(IO).launch {
                historyInfos.clear()
                historyInfos.addAll(it)
                withContext(Main) {
                    adapter.setHistoryInfos(it)
                    noHistory?.visibility = if (it.isNotEmpty()) View.GONE else View.VISIBLE
                }
            }
        })

        selectionView.btnSendFiles.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                historyInfos.forEach {
                    if (it.isSelected) {
                        it.isSelected = false
                        selected.add(RawRequestInfo(
                                it.name, it.uri, it.type, MathUtils.getFileSize(DocumentFile.fromFile(it.uri.toFile()))
                        ))
                    }
                }
                selectedCount = 0
                withContext(Main) {
                    adapter.notifyDataSetChanged()
                    hideShowSendButton()
                    Toast.makeText(requireContext(), "${selected.size} files added to send queue", Toast.LENGTH_SHORT).show()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }

        selectionView.btnSelectNon.setOnClickListener {
            if (selectedCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                historyInfos.forEach {
                    it.isSelected = false
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
                historyInfos.forEach {
                    it.isSelected = true
                    selectedCount++
                }
                withContext(Main) {
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

    override fun onHistoryDeleteRequestSelected(historyInfo: HistoryInfo) {
        historyViewModel.delete(historyInfo.id)
    }

    override fun onHistoryDeselected(historyInfo: HistoryInfo) {
        selectedCount--
        hideShowSendButton()
    }

    private fun hideShowSendButton() {
        onNavigationVisibilityListener?.onNavVisibilityChange(selectedCount == 0)
        selectionView?.changeVisibility(selectedCount)
    }
}
