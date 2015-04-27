package com.jahirfiquitiva.paperboard.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jahirfiquitiva.paperboard.activities.DetailedWallpaperActivity;
import com.jahirfiquitiva.paperboard.adapters.WallpaperAdapter;
import com.jahirfiquitiva.paperboard.utilities.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jahirfiquitiva.paperboard.sample.R;

public class WallpapersFragment extends Fragment {

    public static final String NAME = "name";
    public static final String WALL = "wall";

    private ArrayList<HashMap<String, String>> data;
    private ProgressBar mProgress;
    private WallpaperAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.section_wallpapers, container, false);
        mProgress = (ProgressBar) root.findViewById(R.id.progress);
        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.gridView);

        mAdapter = new WallpaperAdapter(getActivity(),
                new WallpaperAdapter.ClickListener() {
                    @Override
                    public void onClick(int position) {
                        final HashMap<String, String> data = WallpapersFragment.this.data.get(position);
                        final String wallurl = data.get((WallpapersFragment.WALL));
                        final Intent intent = new Intent(getActivity(), DetailedWallpaperActivity.class)
                                .putExtra("wall", wallurl);
                        startActivity(intent);
                    }
                });
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.wallpaper_grid_width)));
        mRecyclerView.setAdapter(mAdapter);

        final ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.section_four);

        new DownloadJSON().execute();
        return root;
    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Create an array
            data = new ArrayList<>();
            // Retrieve JSON Objects from the given URL address
            JSONObject json = JSONParser
                    .getJSONfromURL(getResources().getString(R.string.json_file_url));
            if (json != null) {
                try {
                    // Locate the array name in JSON
                    JSONArray jsonarray = json.getJSONArray("wallpapers");

                    for (int i = 0; i < jsonarray.length(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        json = jsonarray.getJSONObject(i);
                        // Retrieve JSON Objects
                        map.put("name", json.getString("name"));
                        map.put("author", json.getString("author"));
                        map.put("wall", json.getString("url"));
                        // Set the JSON Objects into the array
                        data.add(map);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getString(R.string.json_error_toast), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.json_error_toast), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            mAdapter.setData(data);
            if (mProgress != null)
                mProgress.setVisibility(View.GONE);
        }
    }
}
