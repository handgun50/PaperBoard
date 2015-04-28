package com.jahirfiquitiva.paperboard.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jahirfiquitiva.paperboard.adapters.IconsAdapter;

import jahirfiquitiva.paperboard.sample.R;

public class IconsFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private IconsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView grid = (RecyclerView) inflater.inflate(R.layout.icons_grid, container, false);
        mAdapter = new IconsAdapter(getActivity(), getArguments().getInt("iconsArrayId", 0));
        grid.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.icon_grid_width)));
        grid.setAdapter(mAdapter);
        return grid;
    }

    public static IconsFragment newInstance(int iconsArray) {
        IconsFragment fragment = new IconsFragment();
        Bundle args = new Bundle();
        args.putInt("iconsArrayId", iconsArray);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mAdapter.filter(s);
        return false;
    }

    @Override
    public boolean onClose() {
        mAdapter.filter(null);
        return false;
    }
}