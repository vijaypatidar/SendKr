package com.vkpapps.sendkr.ui.fragments.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.*

/***
 * @author VIJAY PATIDAR
 */
class MyPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragmentList: MutableList<Fragment>
    private val names: MutableList<String>
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return names[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(fragment: Fragment, name: String) {
        fragmentList.add(fragment)
        names.add(name)
    }

    init {
        fragmentList = ArrayList()
        names = ArrayList()
    }
}