package com.vkpapps.thunder.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener

class PhotoViewFragment : Fragment() {
    private var imageView: AppCompatImageView? = null
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        imageView = view.findViewById(R.id.imageView)
        imageView?.adjustViewBounds = true
        val arguments = arguments
        if (arguments != null) {
            val path = arguments.getString("Uri")
            if (path != null) Picasso.get().load(Uri.parse(path)).into(imageView)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = context
            onNavigationVisibilityListener!!.onNavVisibilityChange(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring)?.isVisible = false
        menu.findItem(R.id.menu_sorting)?.isVisible = false
    }


    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        imageView = null
    }
}