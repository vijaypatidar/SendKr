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

            adapter.addFragment(new PhotoFragment(), "Photos");
            adapter.addFragment(new AudioFragment(), "Music");
            adapter.addFragment(new VideoFragment(), "Videos");
//        adapter.addFragment(new FileFragment(),"Files");

            viewPager.setAdapter(adapter);
        } else {
            Navigation.findNavController(view).popBackStack();
            PermissionUtils.askStoragePermission(getActivity(), 101);
        }


    }
}