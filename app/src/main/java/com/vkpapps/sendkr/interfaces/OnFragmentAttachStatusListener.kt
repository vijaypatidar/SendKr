package com.vkpapps.sendkr.interfaces

import androidx.fragment.app.Fragment

/**
 * @author VIJAY PATIDAR
 */
interface OnFragmentAttachStatusListener {
    fun onFragmentAttached(fragment: Fragment)
    fun onFragmentDetached(fragment: Fragment)
}