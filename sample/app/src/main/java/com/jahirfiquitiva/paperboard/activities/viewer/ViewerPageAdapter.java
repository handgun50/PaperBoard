package com.jahirfiquitiva.paperboard.activities.viewer;

import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import java.util.List;

import nexbit.icons.moonshine.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageAdapter extends FragmentStatePagerAdapter {

    private final List<Integer> mMedia;
    private String mInfo;
    public int mCurrentPage;
    private ViewerPageFragment mCurrentFragment;
    private final String[] mTitles;

    public ViewerPageAdapter(AppCompatActivity context, List<Integer> media, String info, int initialOffset) {
        super(context.getSupportFragmentManager());
        mMedia = media;
        mInfo = info;
        mCurrentPage = initialOffset;
        mTitles = context.getResources().getStringArray(R.array.wallpaper_names);
    }

    @Override
    public Fragment getItem(int position) {
        String info = null;
        if (mCurrentPage == position) {
            info = mInfo;
            mInfo = null;
        }
        return ViewerPageFragment.create(mMedia.get(position), mTitles[position], position, info)
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