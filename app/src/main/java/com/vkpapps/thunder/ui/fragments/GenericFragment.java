package com.vkpapps.thunder.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.ui.fragments.viewpager.MyPagerAdapter;
import com.vkpapps.thunder.utils.PermissionUtils;

/***
 * @author VIJAY PATIDAR
 */
public class GenericFragment extends Fragment {
    public static final String PARAM_DESTINATION = "DESTINATION";
    private int destination = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            destination = arguments.getInt(PARAM_DESTINATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PermissionUtils.checkStoragePermission(view.getContext())) {
            TabLayout tabLayout = view.findViewById(R.id.tabLayout);
            ViewPager viewPager = view.findViewById(R.id.viewPager);
            tabLayout.setupWithViewPager(viewPager);

            MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());
            PhotoFragment photoFragment = new PhotoFragment();
            adapter.addFragment(photoFragment, "Photos");
            AudioFragment audioFragment = new AudioFragment();
            adapter.addFragment(audioFragment, "Music");
            VideoFragment videoFragment = new VideoFragment();
            adapter.addFragment(videoFragment, "Videos");
            viewPager.setAdapter(adapter);

            if (destination > 0) {
                viewPager.setCurrentItem(destination);
            }
        } else {
            Navigation.findNavController(view).popBackStack();
            PermissionUtils.askStoragePermission(getActivity(), 101);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}