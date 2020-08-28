package com.vkpapps.sendkr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.interfaces.OnFileRequestPrepareListener
import com.vkpapps.sendkr.interfaces.OnNavigationVisibilityListener
import com.vkpapps.sendkr.model.FileInfo
import com.vkpapps.sendkr.model.RawRequestInfo
import com.vkpapps.sendkr.room.liveViewModel.QuickAccessViewModel
import com.vkpapps.sendkr.ui.adapter.FileAdapter
import com.vkpapps.sendkr.ui.fragments.dialog.FilePropertyDialogFragment
import com.vkpapps.sendkr.ui.fragments.dialog.FilterDialogFragment
import com.vkpapps.sendkr.utils.MathUtils
import kotlinx.android.synthetic.main.fragment_quick_access.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/***
 * @author VIJAY PATIDAR
 */
class QuickAccessFragment : Fragment(), FileAdapter.OnFileSelectListener, FilterDialogFragment.OnFilterListener, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private var sortBy = FilterDialogFragment.SORT_BY_NAME
        const val PARAM_TYPE = "com.vkpapps.sendkr.TYPE_TO_SHOW"
        const val TYPE_DOCUMENTS = 0
        const val TYPE_ZIPS = 1
        const val TYPE_APK = 2
    }


    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFileRequestPrepareListener: OnFileRequestPrepareListener? = null
    private var selectCount = 0
    private var title: String? = "default"
    private var typeToShow = 0
    private var adapter: FileAdapter? = null
    private var fileInfos: ArrayList<FileInfo>? = null
    private val quickAccessViewModel: QuickAccessViewModel by lazy { ViewModelProvider(requireActivity()).get(QuickAccessViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requireArguments().containsKey(PARAM_TYPE)) {
            typeToShow = arguments?.getInt(PARAM_TYPE, 0)!!
        }
        title = when (typeToShow) {
            TYPE_APK -> {
                "APKs"
            }
            TYPE_ZIPS -> {
                "Zips"
            }
            TYPE_DOCUMENTS -> {
                "Documents"
            }
            else -> {
                "Unknown"
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quick_access, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        mySwipeRefreshLayout?.setOnRefreshListener(this)
        quickList?.onFlingListener = object : OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (selectCount == 0)
                    onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }
        quickList?.layoutManager = LinearLayoutManager(requireContext())

        when (typeToShow) {
            TYPE_APK -> {
                quickAccessViewModel.apkLiveData.observe(requireActivity(), {
                    setAdapter(it)
                })
            }
            TYPE_ZIPS -> {
                quickAccessViewModel.zipsLiveData.observe(requireActivity(), {
                    setAdapter(it)
                })
            }
            TYPE_DOCUMENTS -> {
                quickAccessViewModel.documentsLiveData.observe(requireActivity(), {
                    setAdapter(it)
                })
            }
        }

        selectionView.btnSendFiles.setOnClickListener {
            if (selectCount == 0) return@setOnClickListener
            CoroutineScope(IO).launch {
                val selected = ArrayList<RawRequestInfo>()
                fileInfos?.forEach {
                    try {
                        if (it.isSelected) {
                            it.isSelected = false
                            selected.add(RawRequestInfo(
                                    it.name, it.uri, it.type, it.size
                            ))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                selectCount = 0
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
                onFileRequestPrepareListener?.sendFiles(selected)
            }
        }
        selectionView.btnSelectAll.setOnClickListener {
            CoroutineScope(IO).launch {
                selectCount = 0
                fileInfos?.forEach {
                    it.isSelected = true
                    selectCount++
                }
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }
        selectionView.btnSelectNon.setOnClickListener {
            CoroutineScope(IO).launch {
                selectCount = 0
                fileInfos?.forEach {
                    it.isSelected = false
                }
                withContext(Main) {
                    adapter?.notifyDataSetChanged()
                    hideShowSendButton()
                }
            }
        }

    }

    private fun setAdapter(list: ArrayList<FileInfo>) {
        Logger.d("[QuickAccessFragments][setAdapter]")
        this.fileInfos = list
        selectCount = 0
        CoroutineScope(IO).launch {
            list.forEach {
                it.isSelected = false
            }
            withContext(Main) {
                adapter = FileAdapter(this@QuickAccessFragment, findNavController(), list)
                quickList?.adapter = adapter
                loadingFile?.visibility = View.GONE
                if (fileInfos?.size == 0) {
                    emptyQuickList?.visibility = View.VISIBLE
                } else {
                    emptyQuickList?.visibility = View.GONE
                }
            }
            mySwipeRefreshLayout.hide()
        }
        hideShowSendButton()
    }

    override fun onResume() {
        super.onResume()
        // change title
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (supportActionBar != null) {
            supportActionBar.title = title
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val findItem = menu.findItem(R.id.menu_transferring)
        findItem?.actionView?.findViewById<CardView>(R.id.transferringActionView)?.setOnClickListener {
            findNavController().navigate(object : NavDirections {
                override fun getArguments(): Bundle {
                    return Bundle()
                }

                override fun getActionId(): Int {
                    return R.id.action_quickAccessFragment_to_transferringFragment
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_sorting) {
            FilterDialogFragment(sortBy, this).show(requireActivity().supportFragmentManager, "SortBy")
            true
        } else
            super.onOptionsItemSelected(item)

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


    private fun hideShowSendButton() {
        onNavigationVisibilityListener?.onNavVisibilityChange(selectCount == 0)
        selectionView.changeVisibility(selectCount)
    }

    override fun onFileDeselected(fileInfo: FileInfo) {
        selectCount--
        hideShowSendButton()
    }

    override fun onFileLongClickListener(fileInfo: FileInfo) {
        findNavController().navigate(object : NavDirections {
            override fun getArguments(): Bundle {
                return Bundle().apply {
                    putString(FilePropertyDialogFragment.PARAM_FILE_ID, fileInfo.id)
                    putString(FilePropertyDialogFragment.PARAM_FILE_URI, fileInfo.uri.toString())
                    putString(FilePropertyDialogFragment.PARAM_FILE_SIZE, MathUtils.longToStringSize(fileInfo.size.toDouble()))
                }
            }

            override fun getActionId(): Int {
                return R.id.filePropertyDialogFragment
            }
        })
    }


    override fun onFileSelected(fileInfo: FileInfo) {
        selectCount++
        hideShowSendButton()
    }


    override fun onFilterBy(sortBy: Int) {
        QuickAccessViewModel.sortBy = sortBy
        quickAccessViewModel.sortData()
    }

    override fun onRefresh() {
        Logger.d("[QuickAccessFragments][onRefresh]")
        quickAccessViewModel.refreshData()
    }
}