package com.vkpapps.thunder.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.squareup.picasso.Picasso
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.interfaces.OnFragmentAttachStatusListener
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener
import com.vkpapps.thunder.model.User
import com.vkpapps.thunder.utils.StorageManager
import com.vkpapps.thunder.utils.UserUtils
import kotlinx.android.synthetic.main.fragment_user_detail.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author VIJAY PATIDAR
 */
class ProfileFragment : Fragment() {
    private var user: User = App.user
    private var onNavigationVisibilityListener: OnNavigationVisibilityListener? = null
    private var onFragmentAttachStatusListener: OnFragmentAttachStatusListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        userPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 1)
        }
        val profiles = File(StorageManager(requireContext()).profiles, user.userId)
        if (profiles.exists()) {
            Picasso.get().load(profiles).into(userPic)
        }
        val editTextName = view.findViewById<EditText>(R.id.userName)
        editTextName.setText(user.name)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener { v: View ->
            val name = editTextName.text.toString().trim { it <= ' ' }
            if (name.isNotEmpty()) {
                user.name = name
                editTextName.clearFocus()
                UserUtils(v.context).setUser(user)
                Toast.makeText(view.context, "Profile Updated", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(v).popBackStack()
                savePic(userPic)
                //hide keyboard
                try {
                    val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                editTextName.error = "name required!"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_transferring).isVisible = false
    }

    private fun savePic(view: View) {
        val root = StorageManager(view.context).profiles
        val shareBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(shareBitmap)
        view.draw(canvas)
        val f = File(root, user.userId)
        try {
            val fo = FileOutputStream(f)
            shareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fo)
            Picasso.get().invalidate(f)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectedImageUri = data.data
                if (null != selectedImageUri) {
                    userPic?.setImageURI(selectedImageUri)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = context
            onNavigationVisibilityListener?.onNavVisibilityChange(false)
        }
        if (context is OnFragmentAttachStatusListener) {
            onFragmentAttachStatusListener = context
            onFragmentAttachStatusListener?.onFragmentAttached(this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        onNavigationVisibilityListener?.onNavVisibilityChange(true)
        onFragmentAttachStatusListener?.onFragmentDetached(this)
        onFragmentAttachStatusListener = null
        onNavigationVisibilityListener = null
    }
}