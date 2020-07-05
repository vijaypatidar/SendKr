package com.vkpapps.thunder.ui.fragments.destinations

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vkpapps.thunder.R
/***
 * @author VIJAY PATIDAR
 */
class FragmentDestinationListener(private val activity: AppCompatActivity) : OnDestinationChangedListener {
    private val actionBar: ActionBar? = activity.supportActionBar
    private val navView: BottomNavigationView = activity.findViewById(R.id.nav_view)
    private var previous = R.id.navigation_home
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        actionBar?.elevation = 0f
        when (destination.id) {
            R.id.navigation_home -> {
                showNavView(true)
                actionBar?.show()
                actionBar?.setBackgroundDrawable(activity.resources.getDrawable(R.color.colorPrimary, activity.theme))
            }
            R.id.navigation_dashboard -> {
                showNavView(true)
                actionBar?.show()
                actionBar?.setBackgroundDrawable(activity.resources.getDrawable(R.color.colorPrimary, activity.theme))
            }
            R.id.navigation_about -> {
                hideNavView(true)
            }
            R.id.navigation_profile -> {
                actionBar?.setBackgroundDrawable(activity.resources.getDrawable(R.color.profile_frag_background, activity.theme))
                hideNavView(true)
            }
            R.id.navigation_setting -> {
                hideNavView(true)
            }
        }

        previous = destination.id
    }

    private fun hideNavView(anim: Boolean) {
        if (navView.visibility == BottomNavigationView.GONE) return
        if (anim) {
            val animation = AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_bottom)
            navView.animation = animation
        } else {
            navView.clearAnimation()
        }
        navView.visibility = BottomNavigationView.GONE
    }

    private fun showNavView(anim: Boolean) {
        if (navView.visibility == BottomNavigationView.VISIBLE) return
        if (anim) {
            val animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_bottom)
            navView.animation = animation
        } else {
            navView.clearAnimation()
        }
        navView.visibility = BottomNavigationView.VISIBLE
    }
}