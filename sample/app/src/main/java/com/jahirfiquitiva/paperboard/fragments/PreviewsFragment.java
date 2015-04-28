package com.jahirfiquitiva.paperboard.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.jahirfiquitiva.paperboard.activities.viewer.FragmentStatePagerAdapter;
import com.jahirfiquitiva.paperboard.views.SlidingTabLayout;

import java.util.Locale;

import jahirfiquitiva.paperboard.sample.R;

public class PreviewsFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private MenuItem mSearchItem;
    private int mLastSelected = -1;
    private ViewPager mPager;
    private String[] tabs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.section_all_icons, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.section_two);

        mPager = (ViewPager) root.findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(6);
        mPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        mLastSelected = 0;

        SlidingTabLayout mTabs = (SlidingTabLayout) root.findViewById(R.id.tabs);
        mTabs.setViewPager(mPager);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.accent);
            }
        });
        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mSearchItem != null && mSearchItem.isActionViewExpanded())
                    mSearchItem.collapseActionView();
                if (mLastSelected > -1) {
                    IconsFragment frag = (IconsFragment) getChildFragmentManager().findFragmentByTag("page:" + mLastSelected);
                    if (frag != null)
                        frag.onQueryTextChange(null);
                }
                mLastSelected = position;
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        return root;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mSearchItem.collapseActionView();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        IconsFragment frag = (IconsFragment) getChildFragmentManager().findFragmentByTag("page:" + mPager.getCurrentItem());
        if (frag != null)
            frag.onQueryTextChange(s);
        return false;
    }

    @Override
    public boolean onClose() {
        IconsFragment frag = (IconsFragment) getChildFragmentManager().findFragmentByTag("page:" + mPager.getCurrentItem());
        if (frag != null)
            frag.onClose();
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_icons, menu);
        mSearchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setQueryHint(mLastSelected > -1 ?
                getString(R.string.search_x, tabs[mLastSelected]) : getString(R.string.search_icons));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toolbar appbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            appbar.setElevation(0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toolbar appbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            appbar.setElevation((int) getResources().getDimension(R.dimen.toolbar_elevation));
        }
    }

    class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.tabs);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = new Fragment();
            switch (position) {
                case 0:
                    f = IconsFragment.newInstance(R.array.latest);
                    break;
                case 1:
                    f = IconsFragment.newInstance(R.array.system);
                    break;
                case 2:
                    f = IconsFragment.newInstance(R.array.google);
                    break;
                case 3:
                    f = IconsFragment.newInstance(R.array.games);
                    break;
                case 4:
                    f = IconsFragment.newInstance(R.array.icon_pack);
                    break;
                case 5:
                    f = IconsFragment.newInstance(R.array.drawer);
                    break;
            }
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position].toUpperCase(Locale.getDefault());
        }

        @Override
        public int getCount() {
            return tabs.length;
        }
    }
}
