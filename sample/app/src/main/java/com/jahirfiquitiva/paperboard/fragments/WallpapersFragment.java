package com.jahirfiquitiva.paperboard.fragments;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.activities.viewer.ViewerActivity;
import com.jahirfiquitiva.paperboard.adapters.WallpaperAdapter;
import com.jahirfiquitiva.paperboard.utilities.JSONParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jahirfiquitiva.paperboard.sample.R;

import static com.jahirfiquitiva.paperboard.activities.viewer.ViewerActivity.EXTRA_CURRENT_ITEM_POSITION;

public class WallpapersFragment extends Fragment {

    public static final String NAME = "name";
    public static final String WALL = "wall";

    private ArrayList<HashMap<String, String>> data;
    private ProgressBar mProgress;
    private WallpaperAdapter mAdapter;

    private void openViewer(WallpaperAdapter.WallsHolder root, int index) {
        BitmapInfo bi = Ion.with(root.wall).getBitmapInfo();

        final Intent intent = new Intent(getActivity(), ViewerActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("wallData", data);
        extras.putInt(EXTRA_CURRENT_ITEM_POSITION, index);
        extras.putString("bitmapInfo", bi != null ? bi.key : null);
        intent.putExtras(extras);

        final String transName = "view_" + index;
        ViewCompat.setTransitionName(root.view, transName);
        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), root.view, transName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Somehow this works (setting status bar color in both MainActivity and here)
            //to avoid image glitching through on when ViewActivity is first created.
            //TODO: Look into why this works and whether some code is unnecessary
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
            View statusBar = getActivity().getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
            if (statusBar != null) {
                statusBar.post(new Runnable() {
                    @Override
                    public void run() {
                        ActivityCompat.startActivityForResult(getActivity(), intent, 2000, options.toBundle());
                    }
                });
                return;
            }
        }
        ActivityCompat.startActivityForResult(getActivity(), intent, 2000, options.toBundle());
    }

    public static void performOption(final Activity context, int imageIndex, final HashMap<String, String> data) {
        if (imageIndex == 0) {
            new MaterialDialog.Builder(context)
                    .title(R.string.apply)
                    .content(R.string.confirm_apply)
                    .positiveText(R.string.yes)
                    .negativeText(android.R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            final MaterialDialog downloadDialog = new MaterialDialog.Builder(context)
                                    .content(R.string.downloading_wallpaper)
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .show();
                            Ion.with(context)
                                    .load(data.get(WallpapersFragment.WALL))
                                    .asBitmap()
                                    .setCallback(new FutureCallback<Bitmap>() {
                                        @Override
                                        public void onCompleted(Exception e, Bitmap result) {
                                            if (e != null) {
                                                downloadDialog.dismiss();
                                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            downloadDialog.setContent(context.getString(R.string.setting_wall_title));
                                            WallpaperManager wm = WallpaperManager.getInstance(context);
                                            try {
                                                wm.setBitmap(result);
                                                Toast.makeText(context, R.string.set_as_wall_done, Toast.LENGTH_LONG).show();
                                            } catch (IOException e2) {
                                                Toast.makeText(context, e2.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            downloadDialog.dismiss();
                                        }
                                    });
                        }
                    }).show();
        } else {
            // Save
            final MaterialDialog downloadDialog = new MaterialDialog.Builder(context)
                    .content(R.string.downloading_wallpaper)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();

            Ion.with(context)
                    .load(data.get(WallpapersFragment.WALL))
                    .asBitmap()
                    .setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, final Bitmap result) {
                            if (e != null) {
                                downloadDialog.dismiss();
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }
                            saveWallpaper(context, data, downloadDialog, result);
                        }
                    });
        }
    }

    private static void saveWallpaper(final Activity context, final HashMap<String, String> data,
                                      final MaterialDialog downloadDialog, final Bitmap result) {
        downloadDialog.setContent(context.getString(R.string.saving_wallpaper));
        new Thread(new Runnable() {
            @Override
            public void run() {
                final File paperboardFolder = new File(
                        context.getString(R.string.walls_save_location,
                                Environment.getExternalStorageDirectory().getAbsolutePath()));
                paperboardFolder.mkdirs();
                final File destFile = new File(paperboardFolder, data.get(WallpapersFragment.NAME) + ".png");
                if (!destFile.exists()) {
                    try {
                        result.compress(Bitmap.CompressFormat.PNG, 100,
                                new FileOutputStream(destFile));
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,
                                        context.getString(R.string.wallpaper_downloaded,
                                                destFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (final Exception e) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    context.getString(R.string.wallpaper_downloaded,
                                            destFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void showOptions(final int imageIndex) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.wallpaper)
                .items(R.array.wallpaper_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, final int i, CharSequence charSequence) {
                        performOption(getActivity(), imageIndex, data.get(imageIndex));
                    }
                }).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.section_wallpapers, container, false);
        mProgress = (ProgressBar) root.findViewById(R.id.progress);
        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.gridView);

        mAdapter = new WallpaperAdapter(getActivity(),
                new WallpaperAdapter.ClickListener() {
                    @Override
                    public void onClick(WallpaperAdapter.WallsHolder view, int position, boolean longClick) {
                        if (longClick)
                            showOptions(position);
                        else
                            openViewer(view, position);
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
