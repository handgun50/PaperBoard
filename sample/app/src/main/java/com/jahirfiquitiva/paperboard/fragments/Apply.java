package com.jahirfiquitiva.paperboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.jahirfiquitiva.dashboardsample.R;

public class Apply extends Fragment {
    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    private Context context;
    private String intentString, dialogTitle, dialogContent, cmName;
    List<Launcher> launchers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.section_apply, null);

        context = getActivity();

        // Splits all launcher arrays by the | delimiter {name}|{package}
        String[] launcherArray = getResources().getStringArray(R.array.launchers);
        for (String launcher : launcherArray) {
            launchers.add(new Launcher(launcher.split("\\|")));
        }

        ActionBar toolbar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(R.string.section_three);
        toolbar.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));

        ListView launcherslist = (ListView) root.findViewById(R.id.launcherslist);

        LaunchersAdapter adapter = new LaunchersAdapter(getActivity(), launchers);
        launcherslist.setAdapter(adapter);
        launcherslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (LauncherIsInstalled(launchers.get(position).packageName)) {
                    openLauncher(launchers.get(position).name);
                } else {
                    openInPlayStore(launchers.get(position));
                }
            }
        });

        return root;
    }

    private boolean LauncherIsInstalled(String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    // Reflection is ugly, I know! Please don't kill me :(
    public void openLauncher(String name) {
        // Turns Launcher name "Something Pro" to "com.jahirfiquitiva.paperboard.launchers.SomethingproLauncher"
        String className = "com.jahirfiquitiva.paperboard" + ".launchers."
                + Character.toUpperCase(name.charAt(0))
                + name.substring(1).toLowerCase().replace(" ", "").replace("launcher","")
                + "Launcher";

        Class<?> cl = null;
        try {
            cl = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("LAUNCHER CLASS MISSING", "Launcher class for: '" + name + "' missing!");
        }
        if (cl != null) {
            Constructor<?> constructor = null;
            try {
                constructor = cl.getConstructor(Context.class);
            } catch (NoSuchMethodException e) {
                Log.e("LAUNCHER CLASS CONS",
                        "Launcher class for: '" + name + "' is missing a constructor!");
            }
            try {
                if (constructor != null) {
                    constructor.newInstance(getActivity());
                }
            } catch (java.lang.InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void openInPlayStore(final Launcher launcher) {

        intentString = MARKET_URL + launcher.packageName;

        String LauncherName = launcher.name;
        cmName = "CM Theme Engine";

        if (LauncherName.equals(cmName)){
            dialogContent = launcher.name + getResources().getString(R.string.cm_dialog_content);
            intentString = "http://download.cyanogenmod.org/";
        } else {
            dialogContent = launcher.name + getResources().getString(R.string.lni_content);
            intentString = MARKET_URL + launcher.packageName;
        }

        dialogTitle = launcher.name + getResources().getString(R.string.lni_title);

        new MaterialDialog.Builder(context)
                .title(dialogTitle)
                .content(dialogContent)
                .positiveText(R.string.lni_yes)
                .negativeText(R.string.lni_no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(intentString));
                        startActivity(intent);
                    }
                }).show();
    }

    public class Launcher {
        public String name;
        public String packageName;

        public Launcher(String[] values) {
            name = values[0];
            packageName = values[1];
        }
    }

    class LaunchersAdapter extends ArrayAdapter<Launcher> {
        List<Launcher> launchers;

        LaunchersAdapter(Context context, List<Launcher> launchers) {
            super(context, R.layout.launcher_item, R.id.launchername, launchers);
            this.launchers = launchers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            LauncherHolder holder;

            if (item == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                item = inflater.inflate(R.layout.launcher_item, parent, false);
                holder = new LauncherHolder(item);
                item.setTag(holder);
            } else {
                holder = (LauncherHolder) item.getTag();

            }
            // Turns Launcher name "Something Pro" to "l_something_pro"
            int iconResource = context.getResources().getIdentifier(
                    "ic_" + launchers.get(position).name.toLowerCase().replace(" ", "_"),
                    "drawable",
                    context.getPackageName()
            );

            holder.icon.setImageResource(iconResource);
            holder.launchername.setText(launchers.get(position).name);

            if (LauncherIsInstalled(launchers.get(position).packageName)) {
                holder.isInstalled.setText(R.string.installed);
                holder.isInstalled.setTextColor(getResources().getColor(R.color.green));
            } else {
                holder.isInstalled.setText(R.string.noninstalled);
                holder.isInstalled.setTextColor(getResources().getColor(R.color.red));
            }

            return item;
        }

        class LauncherHolder {
            ImageView icon;
            TextView launchername;
            TextView isInstalled;

            LauncherHolder(View v) {
                icon = (ImageView) v.findViewById(R.id.launchericon);
                launchername = (TextView) v.findViewById(R.id.launchername);
                isInstalled = (TextView) v.findViewById(R.id.launcherinstalled);
            }

        }
    }

}
