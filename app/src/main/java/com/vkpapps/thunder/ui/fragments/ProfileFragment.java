package com.vkpapps.thunder.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.App;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.interfaces.OnFragmentAttachStatusListener;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.model.User;
import com.vkpapps.thunder.utils.StorageManager;
import com.vkpapps.thunder.utils.UserUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * @author VIJAY PATIDAR
 */
public class ProfileFragment extends Fragment {

    private User user;
    private ImageView userPic;
    private OnNavigationVisibilityListener onNavigationVisibilityListener;
    private OnFragmentAttachStatusListener onFragmentAttachStatusListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = App.getUser();
        userPic = view.findViewById(R.id.userPic);
        userPic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 1);
        });
        File profiles = new File(new StorageManager(requireContext()).getProfiles(), user.userId);
        if (profiles.exists()) {
            Picasso.get().load(profiles).into(userPic);
        }

        EditText editTextName = view.findViewById(R.id.userName);
        editTextName.setText(user.getName());
        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            if (!name.isEmpty()) {
                user.setName(name);
                editTextName.clearFocus();
                new UserUtils(v.getContext()).setUser(user);
                Toast.makeText(view.getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
                savePic(userPic);
                //hide keyboard
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                editTextName.setError("name required!");
            }
        });
    }

    private void savePic(View view) {
        File root = new StorageManager(view.getContext()).getProfiles();
        Bitmap shareBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(shareBitmap);
        view.draw(canvas);

        File f = new File(root, user.userId);
        try {
            FileOutputStream fo = new FileOutputStream(f);
            shareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fo);
            Picasso.get().invalidate(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    userPic.setImageURI(selectedImageUri);
                }
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = (OnNavigationVisibilityListener) context;
            onNavigationVisibilityListener.onNavVisibilityChange(false);
        }
        if (context instanceof OnFragmentAttachStatusListener) {
            this.onFragmentAttachStatusListener = (OnFragmentAttachStatusListener) context;
            onFragmentAttachStatusListener.onFragmentAttached(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNavigationVisibilityListener.onNavVisibilityChange(true);
        onFragmentAttachStatusListener.onFragmentDetached(this);
        onFragmentAttachStatusListener = null;
        onNavigationVisibilityListener = null;
    }
}
