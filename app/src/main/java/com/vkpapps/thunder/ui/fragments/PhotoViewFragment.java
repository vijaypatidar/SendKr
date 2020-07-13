package com.vkpapps.thunder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;

import java.io.File;

public class PhotoViewFragment extends Fragment {
    private AppCompatImageView imageView;
    private OnNavigationVisibilityListener onNavigationVisibilityListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.imageView);
        imageView.setAdjustViewBounds(true);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String path = arguments.getString("PATH");
            if (path != null)
                Picasso.get().load(new File(path)).into(imageView);

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = (OnNavigationVisibilityListener) context;
            onNavigationVisibilityListener.onNavVisibilityChange(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNavigationVisibilityListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageView = null;
    }

}