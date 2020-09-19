package com.vkpapps.sendkr.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.vkpapps.sendkr.BuildConfig
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.ui.dialog.PrivacyDialog
import com.vkpapps.sendkr.ui.fragments.base.MyFragment

/***
 * @author VIJAY PATIDAR
 */
class AboutFragment : MyFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val versionCode = view.findViewById<TextView>(R.id.versionCode)
        versionCode.text = BuildConfig.VERSION_NAME
        val btnPrivacyPolicy = view.findViewById<LinearLayout>(R.id.btnPrivacyPolicy)
        btnPrivacyPolicy.setOnClickListener {
            PrivacyDialog(requireActivity()).promptUser()
        }
        val btnDeveloper = view.findViewById<LinearLayout>(R.id.btnDeveloper)
        btnDeveloper.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))))
        }

        onNavigationVisibilityListener?.onNavVisibilityChange(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring)?.isVisible = false
        menu.findItem(R.id.menu_sorting)?.isVisible = false
    }
}