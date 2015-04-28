package com.jahirfiquitiva.paperboard.activities.base;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.jahirfiquitiva.paperboard.utilities.Util;
import com.mikepenz.materialdrawer.Drawer;

import java.util.List;
import java.util.Map;

import jahirfiquitiva.paperboard.sample.R;

import static com.jahirfiquitiva.paperboard.activities.viewer.ViewerActivity.EXTRA_CURRENT_ITEM_POSITION;
import static com.jahirfiquitiva.paperboard.activities.viewer.ViewerActivity.EXTRA_OLD_ITEM_POSITION;

/**
 * @author Aidan Follestad (afollestad)
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private static final boolean DEBUG = true;

    public Bundle mTmpState;
    public boolean mIsReentering;
    public RecyclerView mRecyclerView;
    protected Toolbar toolbar;
    public Drawer.Result drawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupSharedElementCallback();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupSharedElementCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        final SharedElementCallback mCallback = new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                LOG("onMapSharedElements(List<String>, Map<String, View>)", mIsReentering);
                boolean shouldAdd = true;
                int oldPosition = mTmpState != null ? mTmpState.getInt(EXTRA_OLD_ITEM_POSITION) : 0;
                int currentPosition = mTmpState != null ? mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION) : 0;
                mTmpState = null;
                if (mIsReentering) {
                    shouldAdd = currentPosition != oldPosition;
                }
                if (shouldAdd && mRecyclerView != null) {
                    View newSharedView = mRecyclerView.findViewWithTag(currentPosition);
                    if (newSharedView != null) {
                        newSharedView = newSharedView.findViewById(R.id.wall);
                        final String transName = newSharedView.getTransitionName();
                        names.clear();
                        names.add(transName);
                        sharedElements.clear();
                        sharedElements.put(transName, newSharedView);
                    }
                }

                //Somehow this works (setting status bar color in both MediaFragment and here)
                //to avoid image glitching through on when ViewActivity is first created.
                getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));

                View decor = getWindow().getDecorView();
                View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);

                if (navigationBar != null && !sharedElements.containsKey(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))
                        names.add(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                    sharedElements.put(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME, navigationBar);
                }

                if (toolbar != null && !sharedElements.containsKey(toolbar.getTransitionName())) {
                    if (!names.contains(toolbar.getTransitionName()))
                        names.add(toolbar.getTransitionName());
                    sharedElements.put(toolbar.getTransitionName(), toolbar);
                }

                if (statusBar != null && !sharedElements.containsKey(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
                        names.add(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                    sharedElements.put(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, statusBar);
                }

                LOG("=== names: " + names.toString(), mIsReentering);
                LOG("=== sharedElements: " + Util.setToString(sharedElements.keySet()), mIsReentering);
            }

            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                LOG("onSharedElementStart(List<String>, List<View>, List<View>)", mIsReentering);
                logSharedElementsInfo(sharedElementNames, sharedElements);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                LOG("onSharedElementEnd(List<String>, List<View>, List<View>)", mIsReentering);
                logSharedElementsInfo(sharedElementNames, sharedElements);

                if (mIsReentering) {
                    View statusBar = getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                    if (statusBar != null) {
                        statusBar.post(new Runnable() {
                            @Override
                            public void run() {
                                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
                                mIsReentering = false;
                            }
                        });
                    }
                }
            }

            private void logSharedElementsInfo(List<String> names, List<View> sharedElements) {
                LOG("=== names: " + names.toString(), mIsReentering);
                for (View view : sharedElements) {
                    int[] loc = new int[2];
                    view.getLocationInWindow(loc);
                    Log.i(TAG, "=== " + view.getTransitionName() + ": " + "(" + loc[0] + ", " + loc[1] + ")");
                }
            }
        };
        setExitSharedElementCallback(mCallback);
    }

    private static void LOG(String message, boolean isReentering) {
        if (DEBUG) {
            Log.i(TAG, String.format("%s: %s", isReentering ? "REENTERING" : "EXITING", message));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mIsReentering = true;
        mTmpState = new Bundle(data.getExtras());
        int oldPosition = mTmpState.getInt(EXTRA_OLD_ITEM_POSITION);
        int currentPosition = mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION);
        if (oldPosition != currentPosition && mRecyclerView != null) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
        if (mRecyclerView != null) {
            postponeEnterTransition();
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mRecyclerView.requestLayout();
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2000 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Viewer activity returned, restore status bar color to transparent
            final int primaryColorDark = getResources().getColor(R.color.primary_dark);
            if (drawer != null) {
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
                drawer.setStatusBarColor(primaryColorDark);
            } else {
                getWindow().setStatusBarColor(primaryColorDark);
            }
        }
    }
}
