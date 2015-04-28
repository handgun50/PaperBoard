package com.jahirfiquitiva.paperboard.activities.viewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.jahirfiquitiva.paperboard.utilities.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerActivity extends AppCompatActivity {

    private static final String TAG = "ViewerActivity";
    private static final boolean DEBUG = true;
    public static final String EXTRA_CURRENT_ITEM_POSITION = "extra_current_item_position";
    public static final String EXTRA_OLD_ITEM_POSITION = "extra_old_item_position";

    private ArrayList<HashMap<String, String>> mData;
    private ViewerPageAdapter mAdapter;
    public Toolbar mToolbar;
    private Timer mTimer;

    public static final int TOOLBAR_FADE_OFFSET = 2750;
    public static final int TOOLBAR_FADE_DURATION = 400;

    private static final String STATE_CURRENT_POSITION = "state_current_position";
    private static final String STATE_OLD_POSITION = "state_old_position";
    private int mCurrentPosition;
    private int mOriginalPosition;
    private boolean startedPostponedTransition;
    public boolean mFinishedTransition;
    private int mStatusBarHeight;
    private boolean mIsReturning;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void invalidateTransition() {
        if (startedPostponedTransition || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;
        startedPostponedTransition = true;
        startPostponedEnterTransition();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupSharedElementCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        final SharedElementCallback mCallback = new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                LOG("onMapSharedElements(List<String>, Map<String, View>)", mIsReturning);
                if (mIsReturning) {
                    View sharedView = mAdapter.getCurrentDetailsFragment().getSharedElement();
                    names.clear();
                    sharedElements.clear();

                    final String transName = sharedView.getTransitionName();
                    names.add(transName);
                    sharedElements.put(transName, sharedView);
                }

                View decor = getWindow().getDecorView();
                View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);

                if (navigationBar != null && !sharedElements.containsKey(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))
                        names.add(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                    sharedElements.put(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME, navigationBar);
                }

                if (mToolbar != null && !sharedElements.containsKey(mToolbar.getTransitionName())) {
                    if (!names.contains(mToolbar.getTransitionName()))
                        names.add(mToolbar.getTransitionName());
                    sharedElements.put(mToolbar.getTransitionName(), mToolbar);
                }

                if (statusBar != null && !sharedElements.containsKey(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME)) {
                    if (!names.contains(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
                        names.add(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                    sharedElements.put(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, statusBar);
                }

                LOG("=== names: " + names.toString(), mIsReturning);
                LOG("=== sharedElements: " + Util.setToString(sharedElements.keySet()), mIsReturning);
            }

            @Override
            public void onSharedElementStart(List<String> names, List<View> sharedElements, List<View> sharedElementSnapshots) {
                LOG("onMapSharedElements(List<String>, Map<String, View>)", mIsReturning);
                logSharedElementsInfo(names, sharedElements);

                final int black = getResources().getColor(android.R.color.black);
                final int duration = 200;
                final int primaryColor = getResources().getColor(R.color.primary);
                final int primaryColorDark = getResources().getColor(R.color.primary_dark);

                View decor = getWindow().getDecorView();
                View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);

                if (!mIsReturning) {
                    int viewerOverlayColor = getResources().getColor(R.color.viewer_overlay);
                    ObjectAnimator.ofObject(mToolbar, "backgroundColor", new ArgbEvaluator(), primaryColor, viewerOverlayColor)
                            .setDuration(duration)
                            .start();
                    if (navigationBar != null)
                        ObjectAnimator.ofObject(navigationBar, "backgroundColor", new ArgbEvaluator(), primaryColorDark, black)
                                .setDuration(duration)
                                .start();
                    if (statusBar != null)
                        ObjectAnimator.ofObject(statusBar, "backgroundColor", new ArgbEvaluator(), primaryColorDark, black)
                                .setDuration(duration)
                                .start();
                } else {
                    mToolbar.setBackgroundColor(primaryColor);

                    if (navigationBar != null)
                        ObjectAnimator.ofObject(navigationBar, "backgroundColor", new ArgbEvaluator(), black, primaryColorDark)
                                .setDuration(duration)
                                .start();
                    if (statusBar != null)
                        ObjectAnimator.ofObject(statusBar, "backgroundColor", new ArgbEvaluator(), black, primaryColorDark)
                                .setDuration(duration)
                                .start();
                }
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                LOG("onSharedElementEnd(List<String>, List<View>, List<View>)", mIsReturning);
                logSharedElementsInfo(sharedElementNames, sharedElements);
            }

            private void logSharedElementsInfo(List<String> names, List<View> sharedElements) {
                LOG("=== names: " + names.toString(), mIsReturning);
                for (View view : sharedElements) {
                    int[] loc = new int[2];
                    view.getLocationInWindow(loc);
                    Log.i(TAG, "=== " + view.getTransitionName() + ": " + "(" + loc[0] + ", " + loc[1] + ")");
                }
            }
        };
        setEnterSharedElementCallback(mCallback);
    }

    private static void LOG(String message, boolean isReturning) {
        if (DEBUG) {
            Log.i(TAG, String.format("%s: %s", isReturning ? "RETURNING" : "ENTERING", message));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
        outState.putInt(STATE_OLD_POSITION, mOriginalPosition);
    }

    public int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                mStatusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }
        return mStatusBarHeight;
    }

    public int getNavigationBarHeight(boolean portraitOnly, boolean landscapeOnly) {
        final Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // Cancel out for tablets~
            return 0;
        }

        final Resources r = getResources();
        int id;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (portraitOnly) return 0;
            id = r.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        } else {
            if (landscapeOnly) return 0;
            id = r.getIdentifier("navigation_bar_height", "dimen", "android");
        }
        if (id > 0)
            return r.getDimensionPixelSize(id);
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startedPostponedTransition = false;
            postponeEnterTransition();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        mToolbar.setLayoutParams(params);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mCurrentPosition = getIntent().getExtras().getInt(EXTRA_CURRENT_ITEM_POSITION);
                mOriginalPosition = mCurrentPosition;
            }
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
            mOriginalPosition = savedInstanceState.getInt(STATE_OLD_POSITION);
        }

        if (getIntent() != null) {
            //noinspection unchecked
            mData = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("wallData");
        }

        mAdapter = new ViewerPageAdapter(this, mData,
                getIntent() != null ? getIntent().getStringExtra("bitmapInfo") : null, mCurrentPosition);
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mCurrentPosition);

        // When the view pager is swiped, fragments are notified if they're active or not
        // And the menu updates based on the color mode (light or dark).
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int previousState;
            boolean userScrollChange;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ViewerPageFragment noActive = (ViewerPageFragment) getSupportFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                if (noActive != null)
                    noActive.setIsActive(false);
                mCurrentPosition = position;
                ViewerPageFragment active = (ViewerPageFragment) getSupportFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                if (active != null) {
                    active.setIsActive(true);
                }
                mAdapter.mCurrentPage = position;
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING)
                    userScrollChange = true;
                else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE)
                    userScrollChange = false;

                previousState = state;
            }
        });

        mFinishedTransition = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        setupSharedElementCallback();

        // Callback used to know when the user swipes up to show system UI
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.VISIBLE) {
                    invokeToolbar(false);
                    systemUIFocus = false; // this is inverted by the method below
                    systemUIFocusChange();
                }
            }
        });

        // Prevents nav bar from overlapping toolbar options in landscape
        mToolbar.setPadding(
                mToolbar.getPaddingLeft(),
                mToolbar.getPaddingTop(),
                getNavigationBarHeight(false, true),
                mToolbar.getPaddingBottom()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ViewerPageFragment active = (ViewerPageFragment) getSupportFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
        if (active != null) {
            active.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private boolean systemUIFocus = false;

    public void systemUIFocusChange() {
        systemUIFocus = !systemUIFocus;
        if (systemUIFocus) {
            showSystemUI();
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
            }
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    systemUIFocus = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideSystemUI();
                        }
                    });
                }
            }, TOOLBAR_FADE_OFFSET);
        } else hideSystemUI();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        mToolbar.animate().cancel();
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        // Resume the fade animation
        invokeToolbar(false);
    }

    public interface ToolbarFadeListener {
        void onFade();

    }

    public void invokeToolbar(boolean tapped) {
        invokeToolbar(tapped, null);
    }

    public void invokeToolbar(boolean tapped, final ToolbarFadeListener listener) {
        mToolbar.animate().cancel();
        if (tapped && mToolbar.getAlpha() > 0f) {
            // User tapped to hide the toolbar immediately
            mToolbar.animate().setDuration(TOOLBAR_FADE_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (listener != null) listener.onFade();
                        }
                    }).alpha(0f).setStartDelay(0).start();
        } else {
            mToolbar.setAlpha(1f);
            mToolbar.animate().setDuration(TOOLBAR_FADE_DURATION).setStartDelay(TOOLBAR_FADE_OFFSET).alpha(0f).start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start the toolbar fader
        invokeToolbar(false);
        systemUIFocusChange();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mToolbar.animate().cancel();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        if (getIntent() != null)
            data.putExtra(EXTRA_OLD_ITEM_POSITION, getIntent().getIntExtra(EXTRA_CURRENT_ITEM_POSITION, 0));
        data.putExtra(EXTRA_CURRENT_ITEM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }
}