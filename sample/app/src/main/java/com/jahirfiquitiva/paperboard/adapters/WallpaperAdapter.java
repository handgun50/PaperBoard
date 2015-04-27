package com.jahirfiquitiva.paperboard.adapters;

import android.content.Context;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.jahirfiquitiva.paperboard.fragments.WallpapersFragment;
import com.jahirfiquitiva.paperboard.utilities.PaletteTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jahirfiquitiva.paperboard.sample.R;

import static com.jahirfiquitiva.paperboard.utilities.PaletteTransformation.PaletteCallback;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallsHolder> implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int index = (Integer) v.getTag();
            if (mCallback != null)
                mCallback.onClick(index);
        }
    }

    public interface ClickListener {
        void onClick(int index);
    }

    private final ArrayList<HashMap<String, String>> data;
    private final Context context;
    private boolean usePalette = true;
    private final ClickListener mCallback;

    public WallpaperAdapter(Context context, ArrayList<HashMap<String, String>> data, ClickListener callback) {
        this.context = context;
        this.data = data;
        this.mCallback = callback;
    }

    @Override
    public WallsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new WallsHolder(inflater.inflate(R.layout.item_wallpaper, parent, false));
    }

    @Override
    public void onBindViewHolder(final WallsHolder holder, int position) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        HashMap<String, String> jsondata = data.get(position);

        holder.name.setText(jsondata.get(WallpapersFragment.NAME));
        final String wallurl = jsondata.get(WallpapersFragment.WALL);
        holder.wall.startAnimation(anim);

        //noinspection SuspiciousNameCombination
        Picasso.with(context)
                .load(wallurl)
                .centerCrop()
                .noFade()
                .transform(PaletteTransformation.instance())
                .into(holder.wall,
                        new PaletteCallback(holder.wall) {
                            @Override
                            public void onSuccess(Palette palette) {
                                holder.progressBar.setVisibility(View.GONE);
                                if (usePalette) {
                                    if (palette != null) {
                                        Palette.Swatch wallSwatch = palette.getVibrantSwatch();
                                        if (wallSwatch != null) {
                                            holder.titleBg.setBackgroundColor(wallSwatch.getRgb());
                                            holder.titleBg.setAlpha(1);
                                            holder.name.setTextColor(wallSwatch.getTitleTextColor());
                                            holder.name.setAlpha(1);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError() {
                            }
                        });

        holder.view.setTag(position);
        holder.view.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class WallsHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView wall;
        final TextView name;
        final ProgressBar progressBar;
        final LinearLayout titleBg;
        final MaterialRippleLayout content;

        WallsHolder(View v) {
            super(v);
            view = v;
            wall = (ImageView) v.findViewById(R.id.wall);
            name = (TextView) v.findViewById(R.id.name);
            progressBar = (ProgressBar) v.findViewById(R.id.progress);
            titleBg = (LinearLayout) v.findViewById(R.id.titlebg);
            content = (MaterialRippleLayout) v.findViewById(R.id.walls_ripple);
        }
    }
}
