package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vkpapps.thunder.R
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.room.liveViewModel.HistoryViewModel
import com.vkpapps.thunder.ui.adapter.HistoryAdapter
import com.vkpapps.thunder.utils.KeyValue
import com.vkpapps.thunder.utils.StorageManager
import kotlinx.android.synthetic.main.fragment_home.*

/***
 * @author VIJAY PATIDAR
 */
class HomeFragment : Fragment() {
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val controller = Navigation.findNavController(view)
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

            val external = StorageManager(requireContext()).external
            if (external != null) {
                Navigation.findNavController(view).navigate(object : NavDirections {
                    override fun getActionId(): Int {
                        return R.id.fileFragment
                    }

                    override fun getArguments(): Bundle {
                        val bundle = Bundle()
                        bundle.putString(FileFragment.FILE_ROOT, external.absolutePath)
                        bundle.putString(FileFragment.FRAGMENT_TITLE, "External Storage")
                        return bundle
                    }
                })
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(Intent.createChooser(intent, "Choose External Storage"), 123)
            }

        }
        setupHistory()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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

    private fun setupHistory() {
        history.layoutManager = LinearLayoutManager(requireContext())
        val adapter = HistoryAdapter(requireContext())
        history.adapter = adapter
        history.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onNavigationVisibilityListener?.onNavVisibilityChange(velocityY < 0)
                return false
            }
        }

        val historyViewModel = ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java)
        historyViewModel.historyInfos.observe(requireActivity(), androidx.lifecycle.Observer {
            adapter.setHistoryInfos(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            Logger.d("===============================================${data?.data}")
            KeyValue(requireContext()).externalStoragePath = "/storage/"
        }
    }
}