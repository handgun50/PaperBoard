package com.jahirfiquitiva.paperboard.adapters;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.jahirfiquitiva.paperboard.dialogs.IconDialog;

import java.util.ArrayList;
import java.util.Locale;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.IconsHolder> implements View.OnClickListener {

    private final AppCompatActivity mContext;
    private ArrayList<Integer> mThumbs;
    private String[] iconNames;
    private ArrayList<Integer> mFiltered;

    public IconsAdapter(AppCompatActivity context, int iconArrayId) {
        this.mContext = context;
        loadIcon(iconArrayId);
    }

    public synchronized void filter(CharSequence s) {
        if (s == null || s.toString().trim().isEmpty()) {
            if (mFiltered != null) {
                mFiltered = null;
                notifyDataSetChanged();
            }
        } else {
            if (mFiltered != null)
                mFiltered.clear();
            mFiltered = new ArrayList<>();
            for (int i = 0; i < iconNames.length; i++) {
                final String name = iconNames[i];
                if (name.toLowerCase(Locale.getDefault())
                        .contains(s.toString().toLowerCase(Locale.getDefault()))) {
                    mFiltered.add(mThumbs.get(i));
                }
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public IconsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new IconsHolder(inflater.inflate(R.layout.item_icon, parent, false));
    }

    @Override
    public void onBindViewHolder(IconsHolder holder, int position) {
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        holder.icon.startAnimation(anim);
        holder.icon.setImageResource(mFiltered != null ?
                mFiltered.get(position) : mThumbs.get(position));

        holder.view.setTag(position);
        holder.view.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mFiltered != null ? mFiltered.size() : mThumbs.size();
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        String name = iconNames[position].toLowerCase(Locale.getDefault());
        int resId = mThumbs.get(position);
        IconDialog.create(resId, name).show(mContext.getSupportFragmentManager(), "ICON_VIEWER");
    }

    class IconsHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView icon;

        IconsHolder(View v) {
            super(v);
            view = v;
            icon = (ImageView) v.findViewById(R.id.icon_img);
        }
    }

    private void loadIcon(int iconArrayid) {
        mThumbs = new ArrayList<>();

        final Resources r = mContext.getResources();
        final String p = mContext.getPackageName();

        iconNames = r.getStringArray(iconArrayid);
        for (String extra : iconNames) {
            int res = r.getIdentifier(extra, "drawable", p);
            if (res != 0) {
                final int thumbRes = r.getIdentifier(extra, "drawable", p);
                if (thumbRes != 0)
                    mThumbs.add(thumbRes);
            }
        }
    }
}
