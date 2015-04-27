package com.jahirfiquitiva.paperboard.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.pkmmte.requestmanager.AppInfo;
import com.pkmmte.requestmanager.PkRequestManager;

import java.util.LinkedList;
import java.util.List;

import jahirfiquitiva.paperboard.sample.R;

public class RequestFragment extends Fragment {

    // Request Manager
    private PkRequestManager mRequestManager;

    // App List
    private final List<AppInfo> mApps = new LinkedList<>();

    // List & Adapter
    private RecyclerView mList;
    private RequestAdapter mAdapter;
    private View mProgress;
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.section_icon_request, container, false);

        showNewAdviceDialog();

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.section_five);

        // Populate your RecyclerView with your apps
        mList = (RecyclerView) root.findViewById(R.id.appList);
        mList.setVisibility(View.GONE);

        // Setup RecyclerView and adapter
        mAdapter = new RequestAdapter(mApps, new ClickListener() {
            @Override
            public void onClick(int position) {
                // Mark the app as selected
                AppInfo mApp = mApps.get(position);
                mApp.setSelected(!mApp.isSelected());
                mApps.set(position, mApp);

                // Let the adapter know you selected something
                mAdapter.notifyDataSetChanged();
            }
        });
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setAdapter(mAdapter);

        // Progress
        mProgress = root.findViewById(R.id.progress);

        new GrabApplicationsTask().execute();

        fab = (FloatingActionButton) root.findViewById(R.id.send_btn);
        fab.hide(true);
        fab.attachToRecyclerView(mList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestManager.setActivity(getActivity());
                if (mRequestManager.getNumSelected() < 1)
                    mRequestManager.sendRequest(true, false);
                else
                    mRequestManager.sendRequestAsync();
                Toast.makeText(getActivity(), getString(R.string.building_request), Toast.LENGTH_LONG).show();
            }
        });

        return root;
    }

    private class GrabApplicationsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                mRequestManager = PkRequestManager.getInstance(getActivity());
                mRequestManager.setDebugging(false);
                mRequestManager.loadAppsIfEmpty();
                // Get the list of apps
                mApps.addAll(mRequestManager.getApps());
            } catch (Exception ex) {
                //could happen that the activity detaches :D
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
            if (mList != null)
                mList.setVisibility(View.VISIBLE);
            if (fab != null)
                fab.show(true);
            if (mProgress != null)
                mProgress.setVisibility(View.GONE);
        }
    }

    public interface ClickListener {
        void onClick(int index);
    }

    private class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> implements View.OnClickListener {

        private final List<AppInfo> mApps;
        private final ClickListener mCallback;

        public RequestAdapter(List<AppInfo> apps, ClickListener callback) {
            this.mApps = apps;
            this.mCallback = callback;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ViewHolder(inflater.inflate(R.layout.request_item, parent, false));
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

    private void showNewAdviceDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!prefs.getBoolean("dontshowagain", false)) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.advice)
                    .content(R.string.request_advice)
                    .positiveText(R.string.close)
                    .neutralText(R.string.dontshow)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().putBoolean("dontshowagain", false).commit();
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().putBoolean("dontshowagain", true).commit();
                        }
                    }).show();
        }

    }
}