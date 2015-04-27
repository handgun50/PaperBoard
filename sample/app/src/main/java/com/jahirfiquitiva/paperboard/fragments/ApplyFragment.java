package com.jahirfiquitiva.paperboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.adapters.LaunchersAdapter;
import com.jahirfiquitiva.paperboard.sort.InstalledLauncherComparator;
import com.jahirfiquitiva.paperboard.utilities.Util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jahirfiquitiva.paperboard.sample.R;

public class ApplyFragment extends Fragment {

    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";

    private String intentString;
    private final List<Launcher> launchers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RecyclerView root = (RecyclerView) inflater.inflate(R.layout.section_apply, container, false);

        // Splits all launcher  arrays by the | delimiter {name}|{package}
        final String[] launcherArray = getResources().getStringArray(R.array.launchers);
        for (String launcher : launcherArray)
            launchers.add(new Launcher(launcher.split("\\|")));
        Collections.sort(launchers, new InstalledLauncherComparator(getActivity()));

        final ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.section_three);

        final LaunchersAdapter adapter = new LaunchersAdapter(getActivity(), launchers,
                new LaunchersAdapter.ClickListener() {
                    @Override
                    public void onClick(int position) {
                        if (launchers.get(position).name.equals("Google Now Launcher"))
                            gnlDialog();
                        else if (Util.launcherIsInstalled(getActivity(), launchers.get(position).packageName))
                            openLauncher(launchers.get(position).name);
                        else
                            openInPlayStore(launchers.get(position));
                    }
                });
        root.setLayoutManager(new LinearLayoutManager(getActivity()));
        root.setAdapter(adapter);

        return root;
    }

    private void openLauncher(String name) {
        final String className = "com.jahirfiquitiva.paperboard" + ".launchers."
                + Character.toUpperCase(name.charAt(0))
                + name.substring(1).toLowerCase().replace(" ", "").replace("launcher", "")
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
                if (constructor != null)
                    constructor.newInstance(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openInPlayStore(final Launcher launcher) {
        intentString = MARKET_URL + launcher.packageName;
        final String LauncherName = launcher.name;
        final String cmName = "CM Theme Engine";
        String dialogContent;

        if (LauncherName.equals(cmName)) {
            dialogContent = launcher.name + getResources().getString(R.string.cm_dialog_content);
            intentString = "http://download.cyanogenmod.org/";
        } else {
            dialogContent = launcher.name + getResources().getString(R.string.lni_content);
            intentString = MARKET_URL + launcher.packageName;
        }

        new MaterialDialog.Builder(getActivity())
                .title(launcher.name + getResources().getString(R.string.lni_title))
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

        public final String name;
        public final String packageName;
        private int isInstalled = -1;

        public Launcher(String[] values) {
            name = values[0];
            packageName = values[1];
        }

        public boolean isInstalled(Context context) {
            if (isInstalled == -1)
                isInstalled = Util.launcherIsInstalled(context, packageName) ? 1 : 0;
            // Caches this value, checking if a launcher is installed is intensive on processing
            return isInstalled == 1;
        }
    }

    private void gnlDialog() {
        final String appLink = MARKET_URL + getResources().getString(R.string.extraapp);
        new MaterialDialog.Builder(getActivity())
                .title(R.string.gnl_title)
                .content(R.string.gnl_content)
                .positiveText(R.string.lni_yes)
                .negativeText(R.string.lni_no)
                .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  super.onPositive(dialog);
                                  Intent intent = new Intent(Intent.ACTION_VIEW);
                                  intent.setData(Uri.parse(appLink));
                                  startActivity(intent);
                              }
                          }
                ).show();
    }
}
