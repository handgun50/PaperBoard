package com.jahirfiquitiva.paperboard.activities.viewer;

import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageAdapter extends FragmentStatePagerAdapter {

    private final ArrayList<HashMap<String, String>> mMedia;
    private String mInfo;
    public int mCurrentPage;
    private ViewerPageFragment mCurrentFragment;

    public ViewerPageAdapter(AppCompatActivity context, ArrayList<HashMap<String, String>> media, String info, int initialOffset) {
        super(context.getSupportFragmentManager());
        mMedia = media;
        mInfo = info;
        mCurrentPage = initialOffset;
    }

    @Override
    public Fragment getItem(int position) {
        String info = null;
        if (mCurrentPage == position) {
            info = mInfo;
            mInfo = null;
        }
        return ViewerPageFragment.create(position, mMedia.get(position), info)
                .setIsActive(mCurrentPage == position);
    }

    @Override
    public int getCount() {
        return mMedia.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentFragment = (ViewerPageFragment) object;
    }

    public ViewerPageFragment getCurrentDetailsFragment() {
        return mCurrentFragment;
    }
}