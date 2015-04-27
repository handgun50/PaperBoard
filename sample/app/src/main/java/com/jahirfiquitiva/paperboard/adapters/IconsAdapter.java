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
                        .title(makeTextReadable(name))
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

    private String makeTextReadable(String name) {
        String partialConvertedText = name.replaceAll("_", " ");
        String[] text = partialConvertedText.split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (text[0].length() > 0) {
            sb.append(Character.toUpperCase(text[0].charAt(0))).append(text[0].subSequence(1, text[0].length()).toString().toLowerCase());
            for (int i = 1; i < text.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(text[i].charAt(0))).append(text[i].subSequence(1, text[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
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
        final String packageName = mContext.getPackageName();
        addIcon(mContext.getResources(), packageName, iconArrayid);
    }

    private void addIcon(Resources resources, String packageName, int list) {
        iconNames = resources.getStringArray(list);
        for (String extra : iconNames) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                final int thumbRes = resources.getIdentifier(extra, "drawable", packageName);
                if (thumbRes != 0)
                    mThumbs.add(thumbRes);
            }
        }
    }

}
