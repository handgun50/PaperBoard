package com.jahirfiquitiva.paperboard.sort;

import android.content.Context;

import com.jahirfiquitiva.paperboard.fragments.ApplyFragment;

import java.util.Comparator;

/**
 * @author Aidan Follestad (afollestad)
 */
public class InstalledLauncherComparator implements Comparator<ApplyFragment.Launcher> {

    private final Context mContext;

    public InstalledLauncherComparator(Context context) {
        mContext = context;
    }

    @Override
    public int compare(ApplyFragment.Launcher lhs, ApplyFragment.Launcher rhs) {
        if (!lhs.isInstalled(mContext) && rhs.isInstalled(mContext)) {
            // Left is not installed, right is, push left down towards the bottom.
            return 1;
        } else if (lhs.isInstalled(mContext) && !rhs.isInstalled(mContext)) {
            // Left is installed, right isn't, pull left up towards the top.
            return -1;
        } else {
            // Sort alphabetically if they're at the same position.
            return lhs.name.compareTo(rhs.name);
        }
    }
}
