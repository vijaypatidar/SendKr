package com.vkpapps.thunder.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.vkpapps.thunder.R
import com.vkpapps.thunder.ui.activity.MainActivity
import com.vkpapps.thunder.ui.fragments.viewpager.MyPagerAdapter
import com.vkpapps.thunder.utils.PermissionUtils.askStoragePermission
import com.vkpapps.thunder.utils.PermissionUtils.checkStoragePermission

/***
 * @author VIJAY PATIDAR
 */
class GenericFragment : Fragment() {
    private var destination = 0
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments
        if (arguments != null) {
            destination = arguments.getInt(PARAM_DESTINATION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_generic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        navController = Navigation.findNavController(view)
        if (checkStoragePermission(view.context)) {
            val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)
            val viewPager: ViewPager = view.findViewById(R.id.viewPager)
            tabLayout.setupWithViewPager(viewPager)
            val adapter = MyPagerAdapter(childFragmentManager)
            val photoFragment = PhotoFragment()
            adapter.addFragment(photoFragment, "Photos")
            val audioFragment = AudioFragment()
            adapter.addFragment(audioFragment, "Music")
            val videoFragment = VideoFragment()
            adapter.addFragment(videoFragment, "Videos")
            viewPager.adapter = adapter
            viewPager.currentItem = destination
        } else {
            Navigation.findNavController(view).popBackStack()
            askStoragePermission(activity, MainActivity.ASK_PERMISSION_FROM_GENERIC_FRAGMENT)
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
                    return R.id.action_navigation_files_to_transferringFragment
                }

            })
        }

    }

    companion object {
        const val PARAM_DESTINATION = "DESTINATION"
    }
}