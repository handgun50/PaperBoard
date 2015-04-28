package com.jahirfiquitiva.paperboard.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkmmte.requestmanager.AppInfo;

import java.util.List;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> implements View.OnClickListener {

    public interface ClickListener {
        void onClick(int index);
    }

    private final List<AppInfo> mApps;
    private final ClickListener mCallback;

    public RequestAdapter(List<AppInfo> apps, ClickListener callback) {
        this.mApps = apps;
        this.mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_request, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo app = mApps.get(position);
        holder.txtName.setText(app.getName());
        holder.imgIcon.setImageDrawable(app.getImage());
        holder.chkSelected.setChecked(app.isSelected());

        holder.view.setTag(position);
        holder.view.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int index = (Integer) v.getTag();
            if (mCallback != null)
                mCallback.onClick(index);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView imgIcon;
        final TextView txtName;
        final CheckBox chkSelected;

        public ViewHolder(View v) {
            super(v);
            view = v;
            imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
            txtName = (TextView) v.findViewById(R.id.txtName);
            chkSelected = (CheckBox) v.findViewById(R.id.chkSelected);
        }
    }
}