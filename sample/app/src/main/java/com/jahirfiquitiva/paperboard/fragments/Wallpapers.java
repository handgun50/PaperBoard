package com.jahirfiquitiva.paperboard.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jahirfiquitiva.paperboard.activities.DetailedWallpaper;
import com.jahirfiquitiva.paperboard.adapters.WallsGridAdapter;
import com.jahirfiquitiva.paperboard.utilities.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jahirfiquitiva.paperboard.sample.R;

public class Wallpapers extends Fragment {

    private static final int DEFAULT_COLUMNS_PORTRAIT = 2;
    private static final int DEFAULT_COLUMNS_LANDSCAPE = 3;
    public static String NAME = "name";
    public static String WALL = "wall";

    private ArrayList<HashMap<String, String>> arraylist;
    private ViewGroup root;
    private ProgressBar mProgress;
    private int mColumnCount;
    private int numColumns = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.section_wallpapers, container, false);
        mProgress = (ProgressBar) root.findViewById(R.id.progress);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.section_four);

        boolean isLandscape = isLandscape();
        int mColumnCountPortrait = DEFAULT_COLUMNS_PORTRAIT;
        int mColumnCountLandscape = DEFAULT_COLUMNS_LANDSCAPE;
        int newColumnCount = isLandscape ? mColumnCountLandscape : mColumnCountPortrait;
        if (mColumnCount != newColumnCount) {
            mColumnCount = newColumnCount;
            numColumns = mColumnCount;
        }

        new DownloadJSON().execute();
        return root;
    }

    public boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create an array
            arraylist = new ArrayList<>();
            // Retrieve JSON Objects from the given URL address
            JSONObject jsonobject = JSONParser
                    .getJSONfromURL(getResources().getString(R.string.json_file_url));

            try {
                // Locate the array name in JSON
                JSONArray jsonarray = jsonobject.getJSONArray("wallpapers");

                for (int i = 0; i < jsonarray.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    jsonobject = jsonarray.getJSONObject(i);
                    // Retrive JSON Objects
                    map.put("name", jsonobject.getString("name"));
                    map.put("author", jsonobject.getString("author"));
                    map.put("wall", jsonobject.getString("url"));
                    // Set the JSON Objects into the array
                    arraylist.add(map);
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), getString(R.string.json_error_toast), Toast.LENGTH_LONG).show();
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            GridView mGridView = (GridView) root.findViewById(R.id.gridView);
            mGridView.setNumColumns(numColumns);
            WallsGridAdapter mGridAdapter = new WallsGridAdapter(getActivity(), arraylist, numColumns);
            mGridView.setAdapter(mGridAdapter);
            if (mProgress != null)
                mProgress.setVisibility(View.GONE);

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String, String> data = arraylist.get(position);
                    String wallurl = data.get((Wallpapers.WALL));
                    Intent intent = new Intent(getActivity(), DetailedWallpaper.class);
                    intent.putExtra("wall", wallurl);
                    startActivity(intent);
                }
            });
        }
    }
}
