package com.jahirfiquitiva.paperboard.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jahirfiquitiva.paperboard.adapters.IconsAdapter;

import jahirfiquitiva.paperboard.sample.R;

public class IconsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView grid = (RecyclerView) inflater.inflate(R.layout.icons_grid, container, false);
        final IconsAdapter icAdapter = new IconsAdapter(getActivity(), getArguments().getInt("iconsArrayId", 0));
        grid.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.icon_grid_width)));
        grid.setAdapter(icAdapter);
        return grid;
    }

    public static IconsFragment newInstance(int iconsArray) {
        IconsFragment fragment = new IconsFragment();
        Bundle args = new Bundle();
        args.putInt("iconsArrayId", iconsArray);
        fragment.setArguments(args);
        return fragment;
    }
}