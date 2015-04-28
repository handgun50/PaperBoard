package com.jahirfiquitiva.paperboard.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jahirfiquitiva.paperboard.adapters.IconsAdapter;

import java.util.Timer;
import java.util.TimerTask;

import jahirfiquitiva.paperboard.sample.R;

public class IconsFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private IconsAdapter mAdapter;
    private Timer mTimer;
    private Handler mHandler;

    private final static long SEARCH_DELAY_MS = 150;

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
        performSearch(s);
        return false;
    }

    @Override
    public boolean onClose() {
        performSearch(null);
        return false;
    }

    private void performSearch(final String query) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = new Timer();
        mHandler = new Handler();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.filter(query);
                    }
                });
            }
        }, SEARCH_DELAY_MS);
    }
}