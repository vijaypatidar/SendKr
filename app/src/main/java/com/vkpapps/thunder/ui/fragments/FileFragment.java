package com.vkpapps.thunder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.thunder.R;
import com.vkpapps.thunder.interfaces.OnNavigationVisibilityListener;
import com.vkpapps.thunder.ui.adapter.FileAdapter;

import java.io.File;
/***
 * @author VIJAY PATIDAR
 */
public class FileFragment extends Fragment {

    public static final String FILE_ROOT = "FILE_ROOT";
    public static final String FRAGMENT_TITLE = "FRAGMENT_TITLE";
    private OnNavigationVisibilityListener onNavigationVisibilityListener;

    private String rootDir;

    public FileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(FILE_ROOT)) {
            rootDir = getArguments().getString(FILE_ROOT);
        } else {
            rootDir = "/storage/emulated/0/";
        }

        if (getArguments() != null && getArguments().containsKey(FRAGMENT_TITLE)) {
            String title = getArguments().getString(FRAGMENT_TITLE);
            ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setTitle(title);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FileAdapter adapter = new FileAdapter(DocumentFile.fromFile(new File(rootDir)), view);
        RecyclerView recyclerView = view.findViewById(R.id.fileList);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (onNavigationVisibilityListener != null)
                    onNavigationVisibilityListener.onNavVisibilityChange(velocityY < 0);
                return false;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter.notifyDataSetChangedAndHideIfNull();
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