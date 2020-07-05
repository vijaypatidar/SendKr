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
import com.vkpapps.thunder.aysnc.PrepareVideoList;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.model.PhotoInfo;
import com.vkpapps.thunder.ui.adapter.VideoAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/***
 * @author VIJAY PATIDAR
 */
public class VideoFragment extends Fragment implements PrepareVideoList.OnVideoListPrepareListener {

    private List<PhotoInfo> photoInfos = new ArrayList<>();
    private VideoAdapter adapter;
    private OnNavigationVisibilityListener onNavigationVisibilityListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.videoList);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new VideoAdapter(photoInfos, view);
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                onNavigationVisibilityListener.onNavVisibilityChange(velocityY < 0);
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChangedAndHideIfNull();

        PrepareVideoList prepareVideoList = new PrepareVideoList(this);
        prepareVideoList.execute();
    }


    @Override
    public void onVideoListPrepared(@NotNull List<? extends PhotoInfo> photoInfos) {
        this.photoInfos.clear();
        this.photoInfos.addAll(photoInfos);
        if (adapter != null) adapter.notifyDataSetChangedAndHideIfNull();
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