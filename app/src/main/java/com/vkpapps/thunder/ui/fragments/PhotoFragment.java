package com.vkpapps.thunder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.aysnc.PreparePhotoList;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.model.PhotoInfo;
import com.vkpapps.thunder.ui.adapter.PhotoAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/***
 * @author VIJAY PATIDAR
 */
public class PhotoFragment extends Fragment implements PreparePhotoList.OnPhotoListPrepareListener {
    private OnNavigationVisibilityListener onNavigationVisibilityListener;
    private List<PhotoInfo> photoInfos = new ArrayList<>();
    private PhotoAdapter photoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.photoList);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        photoAdapter = new PhotoAdapter(photoInfos, view);
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                onNavigationVisibilityListener.onNavVisibilityChange(velocityY < 0);
                return false;
            }
        });
        photoAdapter.notifyDataSetChangedAndHideIfNull();

        PreparePhotoList preparePhotoList = new PreparePhotoList(this);
        preparePhotoList.execute();
    }


    @Override
    public void onPhotoListPrepared(@NotNull List<? extends PhotoInfo> photoInfo) {
        this.photoInfos.clear();
        this.photoInfos.addAll(photoInfo);
        if (photoAdapter != null) photoAdapter.notifyDataSetChangedAndHideIfNull();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            onNavigationVisibilityListener = (OnNavigationVisibilityListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNavigationVisibilityListener = null;
    }
}