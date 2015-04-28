package com.jahirfiquitiva.paperboard.activities.viewer;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.fragments.WallpapersFragment;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.util.HashMap;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageFragment extends Fragment {

    private HashMap<String, String> mData;
    private boolean isActive;
    private String mBitmapInfo;
    private int mIndex;

    private ImageView mPhoto;

    public String getTitle() {
        return mData.get(WallpapersFragment.NAME);
    }

    public String getUrl() {
        return mData.get(WallpapersFragment.WALL);
    }

    public static ViewerPageFragment create(int index, HashMap<String, String> data, String info) {
        ViewerPageFragment frag = new ViewerPageFragment();
        frag.mData = data;
        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putString("bitmapInfo", info);
        args.putSerializable("data", data);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIndex = getArguments().getInt("index");
        mBitmapInfo = getArguments().getString("bitmapInfo");
        // noinspection unchecked
        mData = (HashMap<String, String>) getArguments().getSerializable("data");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WallpapersFragment.performOption(getActivity(), item.getItemId() == R.id.apply ? 0 : 1, mData);
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_viewer, container, false);
        mPhoto = (ImageView) view;
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeToolbar();
            }
        });
        ViewCompat.setTransitionName(mPhoto, "view_" + mIndex);
        return view;
    }

    public ViewerPageFragment setIsActive(boolean active) {
        isActive = active;
        if (getActivity() != null && isActive)
            getActivity().setTitle(getTitle());
        return this;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImage();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && isActive)
            getActivity().setTitle(getTitle());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadImage() {
        if (mBitmapInfo != null) {
            // Sets the initial cached thumbnail while the rest of loading takes place
            BitmapInfo bi = Ion.getDefault(getActivity())
                    .getBitmapCache()
                    .get(mBitmapInfo);
            if (bi != null) {
                mPhoto.setImageBitmap(bi.bitmap);
                ((ViewerActivity) getActivity()).invalidateTransition();
            }
        } else {
            ((ViewerActivity) getActivity()).invalidateTransition();
        }

        ViewerActivity act = (ViewerActivity) getActivity();
        if (act == null)
            return;
        else if (!act.mFinishedTransition && isActive) {
            // If the activity transition didn't finish yet, wait for it to do so
            // So that the photo view attacher attaches correctly.
            act.getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    ViewerActivity act = (ViewerActivity) getActivity();
                    if (act == null)
                        return;
                    act.getWindow().getEnterTransition().removeListener(this);
                    act.mFinishedTransition = true;
                    if (isAdded())
                        loadImage();
                }
            });
            return;
        }

        // Load the full size image into the view from the file
        Ion.with(mPhoto)
                .crossfade(true)
                .load(getUrl())
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        if (!isAdded()) {
                            return;
                        } else if (e != null) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.error)
                                    .content(e.getLocalizedMessage())
                                    .cancelable(false)
                                    .positiveText(android.R.string.ok)
                                    .show();
                            ((ViewerActivity) getActivity()).invalidateTransition();
                            return;
                        }
                        // If no cached image was loaded, finish the transition now that there is an image displayed
                        ((ViewerActivity) getActivity()).invalidateTransition();
                    }
                });
    }

    public void invokeToolbar() {
        invokeToolbar(null);
    }

    public void invokeToolbar(ViewerActivity.ToolbarFadeListener callback) {
        if (getActivity() != null) {
            ViewerActivity act = (ViewerActivity) getActivity();
            act.invokeToolbar(true, callback);
            act.systemUIFocusChange();
        }
    }

    @NonNull
    public View getSharedElement() {
        return mPhoto;
    }
}