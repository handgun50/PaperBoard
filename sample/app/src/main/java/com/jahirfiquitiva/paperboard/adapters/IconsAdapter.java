package com.jahirfiquitiva.paperboard.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.utilities.Util;

import java.util.ArrayList;
import java.util.Locale;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.IconsHolder> {

    private final Context mContext;
    private ArrayList<Integer> mThumbs;
    private String[] iconNames;

    public IconsAdapter(Context context, int iconArrayId) {
        this.mContext = context;
        loadIcon(iconArrayId);
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
        holder.icon.setImageResource(mThumbs.get(position));

        holder.view.setTag(position);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                String name = iconNames[position].toLowerCase(Locale.getDefault());
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .customView(R.layout.dialog_icon, false)
                        .title(Util.makeTextReadable(name))
                        .positiveText(R.string.close)
                        .build();
                if (dialog.getCustomView() != null) {
                    ImageView dialogIcon = (ImageView) dialog.getCustomView().findViewById(R.id.dialogicon);
                    dialogIcon.setImageResource(mThumbs.get(position));
                }
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mThumbs.size();
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
