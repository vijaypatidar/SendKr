package com.vkpapps.thunder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.aysnc.PrepareVideoList;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.model.VideoInfo;
import com.vkpapps.thunder.ui.adapter.VideoAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/***
 * @author VIJAY PATIDAR
 */
public class VideoFragment extends Fragment implements PrepareVideoList.OnVideoListPrepareListener, VideoAdapter.OnVideoSelectListener {

    private List<VideoInfo> videoInfos = new ArrayList<>();
    private VideoAdapter adapter;
    private OnNavigationVisibilityListener onNavigationVisibilityListener;
    private AppCompatImageButton btnSend;
    private int selectedCount = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnSend = view.findViewById(R.id.btnSend);

        RecyclerView recyclerView = view.findViewById(R.id.videoList);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        adapter = new VideoAdapter(videoInfos, view, this);
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
    public void onVideoListPrepared(@NotNull List<? extends VideoInfo> videoInfos) {
        this.videoInfos.clear();
        this.videoInfos.addAll(videoInfos);
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

    @Override
    public void onVideoSelected(@NonNull VideoInfo videoInfo) {
        selectedCount++;
        hideShowSendButton();
    }

    @Override
    public void onVideoDeselected(@NonNull VideoInfo videoInfo) {
        selectedCount--;
        hideShowSendButton();
    }
    private void hideShowSendButton() {
        if (btnSend.getVisibility()== View.VISIBLE&&selectedCount>0)return;
        if (selectedCount==0){
            btnSend.setAnimation( AnimationUtils.loadAnimation(requireContext(),R.anim.slide_out_to_bottom));
            btnSend.setVisibility(View.GONE);
            onNavigationVisibilityListener.onNavVisibilityChange(true);
        }else{
            btnSend.setAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_in_from_bottom));
            btnSend.setVisibility(View.VISIBLE);
            onNavigationVisibilityListener.onNavVisibilityChange(false);
        }
    }
}