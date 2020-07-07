package com.vkpapps.thunder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.model.AudioInfo;
import com.vkpapps.thunder.ui.adapter.AudioAdapter;
import com.vkpapps.thunder.utils.PermissionUtils;
import com.vkpapps.thunder.utils.StorageManager;

import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class AudioFragment extends Fragment implements AudioAdapter.OnAudioSelectedListener {

    private OnNavigationVisibilityListener onNavigationVisibilityListener;
    private StorageManager storageManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storageManager = new StorageManager(requireContext());

        if (PermissionUtils.checkStoragePermission(view.getContext())) {
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            List<AudioInfo> allSong = storageManager.getAllAudioFromDevice();
            AudioAdapter audioAdapter = new AudioAdapter(allSong, this, view.getContext());
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerView.setAdapter(audioAdapter);
            recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
                @Override
                public boolean onFling(int velocityX, int velocityY) {
                    onNavigationVisibilityListener.onNavVisibilityChange(velocityY < 0);
                    return false;
                }
            });
            audioAdapter.notifyDataSetChanged();
        } else {
            Navigation.findNavController(view).popBackStack();
            PermissionUtils.askStoragePermission(getActivity(), 101);
        }
    }


    @Override
    public void onAudioSelected(AudioInfo audioMode) {

    }

    @Override
    public void onAudioLongSelected(AudioInfo audioinfo) {
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
