package com.jahirfiquitiva.paperboard.adapters;

import android.content.Context;
import android.graphics.Bitmap;
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

import com.jahirfiquitiva.paperboard.fragments.WallpapersFragment;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import jahirfiquitiva.paperboard.sample.R;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallsHolder> {

    public interface ClickListener {
        void onClick(WallsHolder view, int index, boolean longClick);
    }

    private ArrayList<HashMap<String, String>> data;
    private final Context context;
    private boolean usePalette = true;
    private final ClickListener mCallback;
    private final Map<String, Palette> mPaletteCache = new WeakHashMap<>();

    public WallpaperAdapter(Context context, ClickListener callback) {
        this.context = context;
        this.data = new ArrayList<>();
        this.mCallback = callback;
    }

    public void setData(ArrayList<HashMap<String, String>> data) {
        this.data = data;
        notifyDataSetChanged();
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
        holder.wall.setTag(wallurl);

        Ion.with(context)
                .load(wallurl)
                .asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        holder.progressBar.setVisibility(View.GONE);
                        if (e != null) {
                            e.printStackTrace();
                        } else if (holder.wall.getTag() != null && holder.wall.getTag().equals(wallurl)) {
                            holder.wall.setImageBitmap(result);
                            if (usePalette) {
                                Palette p;
                                if (mPaletteCache.containsKey(wallurl)) {
                                    p = mPaletteCache.get(wallurl);
                                } else {
                                    p = new Palette.Builder(result).generate();
                                    mPaletteCache.put(wallurl, p);
                                }
                                if (p != null) {
                                    Palette.Swatch wallSwatch = p.getVibrantSwatch();
                                    if (wallSwatch != null) {
                                        holder.titleBg.setBackgroundColor(wallSwatch.getRgb());
                                        holder.titleBg.setAlpha(1);
                                        holder.name.setTextColor(wallSwatch.getTitleTextColor());
                                        holder.name.setAlpha(1);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class WallsHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public final View view;
        public final ImageView wall;
        public final TextView name;
        public final ProgressBar progressBar;
        public final LinearLayout titleBg;

        WallsHolder(View v) {
            super(v);
            view = v;
            wall = (ImageView) v.findViewById(R.id.wall);
            name = (TextView) v.findViewById(R.id.name);
            progressBar = (ProgressBar) v.findViewById(R.id.progress);
            titleBg = (LinearLayout) v.findViewById(R.id.titlebg);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int index = getLayoutPosition();
            if (mCallback != null)
                mCallback.onClick(this, index, false);
        }

        @Override
        public boolean onLongClick(View v) {
            int index = getLayoutPosition();
            if (mCallback != null)
                mCallback.onClick(this, index, true);
            return false;
        }
    }
}