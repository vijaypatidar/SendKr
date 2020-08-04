package com.vkpapps.thunder.ui.fragments.destinations

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import com.vkpapps.thunder.R
import com.vkpapps.thunder.ui.activity.MainActivity

/***
 * @author VIJAY PATIDAR
 */
class FragmentDestinationListener(private val activity: MainActivity) : OnDestinationChangedListener {
    private var previous = R.id.navigation_home
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        activity.requestViewModel.notifyPendingCountChange()
        when (destination.id) {
            R.id.navigation_home -> {
                activity.onNavVisibilityChange(true)
            }
            R.id.navigation_dashboard -> {
                activity.onNavVisibilityChange(true)
            }
            R.id.navigation_about -> {
                activity.onNavVisibilityChange(false)
            }
            R.id.navigation_setting -> {
                activity.onNavVisibilityChange(false)
            }
        }
        previous = destination.id
    }

}