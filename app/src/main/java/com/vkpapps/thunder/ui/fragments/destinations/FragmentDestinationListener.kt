package com.vkpapps.thunder.ui.fragments.destinations

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import com.vkpapps.thunder.R
import com.vkpapps.thunder.ui.activity.MainActivity

/***
 * @author VIJAY PATIDAR
 */
class FragmentDestinationListener(private val activity: MainActivity) : OnDestinationChangedListener {
    private val actionBar: ActionBar? = activity.supportActionBar
    private var previous = R.id.navigation_home
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        actionBar?.elevation = 0f
        when (destination.id) {
            R.id.navigation_home -> {
                activity.onNavVisibilityChange(true)
                actionBar?.show()
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