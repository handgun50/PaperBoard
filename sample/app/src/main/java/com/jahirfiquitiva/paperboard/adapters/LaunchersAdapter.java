package com.jahirfiquitiva.paperboard.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jahirfiquitiva.paperboard.fragments.ApplyFragment;

import java.util.List;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class LaunchersAdapter extends RecyclerView.Adapter<LaunchersAdapter.LauncherHolder> implements View.OnClickListener {

    public interface ClickListener {
        void onClick(int index);
    }

    private final Context mContext;
    private final List<ApplyFragment.Launcher> launchers;
    private final ClickListener mCallback;

    public LaunchersAdapter(Context context, List<ApplyFragment.Launcher> launchers, ClickListener callback) {
        this.mContext = context;
        this.launchers = launchers;
        this.mCallback = callback;
    }

    @Override
    public LauncherHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new LauncherHolder(inflater.inflate(R.layout.item_launcher, parent, false));
    }

    @Override
    public void onBindViewHolder(LauncherHolder holder, int position) {
        // Turns Launcher name "Something Pro" to "l_something_pro"
        int iconResource = mContext.getResources().getIdentifier(
                "ic_" + launchers.get(position).name.toLowerCase().replace(" ", "_"),
                "drawable",
                mContext.getPackageName()
        );

        holder.icon.setImageResource(iconResource);
        holder.launchername.setText(launchers.get(position).name);

        if (launchers.get(position).isInstalled(mContext)) {
            holder.isInstalled.setText(R.string.installed);
            holder.isInstalled.setTextColor(mContext.getResources().getColor(R.color.green));
        } else {
            holder.isInstalled.setText(R.string.noninstalled);
            holder.isInstalled.setTextColor(mContext.getResources().getColor(R.color.red));
        }

        holder.view.setTag(position);
        holder.view.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return launchers.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int index = (Integer) v.getTag();
            if (mCallback != null)
                mCallback.onClick(index);
        }
    }

    class LauncherHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView icon;
        final TextView launchername;
        final TextView isInstalled;

        LauncherHolder(View v) {
            super(v);
            view = v;
            icon = (ImageView) v.findViewById(R.id.launchericon);
            launchername = (TextView) v.findViewById(R.id.launchername);
            isInstalled = (TextView) v.findViewById(R.id.launcherinstalled);
        }
    }
}