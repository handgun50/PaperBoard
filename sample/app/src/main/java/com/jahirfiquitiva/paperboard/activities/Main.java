package com.jahirfiquitiva.paperboard.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import jahirfiquitiva.paperboard.sample.R;
import com.jahirfiquitiva.paperboard.adapters.ChangelogAdapter;
import com.jahirfiquitiva.paperboard.utilities.Preferences;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pkmmte.requestmanager.AppInfo;
import com.pkmmte.requestmanager.PkRequestManager;
import com.pkmmte.requestmanager.RequestSettings;

import java.util.LinkedList;
import java.util.List;


public class Main extends AppCompatActivity {

    public Drawer.Result result = null;
    public AccountHeader.Result headerResult = null;
    public String thaApp, thaHome, thaPreviews, thaApply, thaWalls, thaRequest, thaCredits;
    public String version, drawerVersion;
    public int currentItem;
    private boolean firstrun, enable_features;
    private Preferences mPrefs;
    private boolean withLicenseChecker = false;
    private Context context;
    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";

    private PkRequestManager mRequestManager;
    private List<AppInfo> mApps = new LinkedList<AppInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab a reference to the manager and store it in a variable. This helps make code shorter.
        mRequestManager = PkRequestManager.getInstance(this);
        mRequestManager.setDebugging(false);
        // Set your custom settings. Email address is required! Everything else is set to default.
        mRequestManager.setSettings(new RequestSettings.Builder()
                .addEmailAddress(getResources().getString(R.string.email_id))
                .emailSubject(getResources().getString(R.string.email_request_subject))
                .emailPrecontent(getResources().getString(R.string.request_precontent))
                .saveLocation(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.request_save_location))
                .build());

        context = this;
        mPrefs = new Preferences(Main.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        thaApp = getResources().getString(R.string.app_name);
        thaHome = getResources().getString(R.string.section_one);
        thaPreviews = getResources().getString(R.string.section_two);
        thaApply = getResources().getString(R.string.section_three);
        thaWalls = getResources().getString(R.string.section_four);
        thaRequest = getResources().getString(R.string.section_five);
        thaCredits = getResources().getString(R.string.section_six);

        drawerVersion = "v " + getResources().getString(R.string.current_version);

        currentItem = 1;

        headerResult = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withSelectionFirstLine(getResources().getString(R.string.app_long_name))
                .withSelectionSecondLine(drawerVersion)
                .withSavedInstance(savedInstanceState)
                .build();

        enable_features = mPrefs.isFeaturesEnabled();
        firstrun = mPrefs.isFirstRun();

        result = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(thaHome).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(1),
                        new PrimaryDrawerItem().withName(thaPreviews).withIcon(GoogleMaterial.Icon.gmd_palette).withIdentifier(2),
                        new PrimaryDrawerItem().withName(thaApply).withIcon(GoogleMaterial.Icon.gmd_open_in_browser).withIdentifier(3),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(thaCredits).withIdentifier(6)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {

                        if (drawerItem != null) {

                            switch (drawerItem.getIdentifier()) {
                                case 1: switchFragment(1, thaApp, "Home"); break;
                                case 2: switchFragment(2, thaPreviews, "Previews"); break;
                                case 3: switchFragment(3, thaApply, "Apply"); break;
                                case 4:
                                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                                    if (isConnected) {
                                        switchFragment(4, thaWalls, "Wallpapers");
                                    } else {
                                        showNotConnectedDialog();
                                    }
                                    break;
                                case 5: switchFragment(5, thaRequest, "Request"); break;
                                case 6: switchFragment(6, thaCredits, "Credits"); break;
                            }
                        }
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        result.getListView().setVerticalScrollBarEnabled(false);

        runLicenseChecker();

        if (savedInstanceState == null) {
            result.setSelectionByIdentifier(currentItem);
        }

    }

    public void switchFragment(int itemId, String title, String fragment) {
        currentItem = itemId;
        getSupportActionBar().setTitle(title);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        tx.replace(R.id.main, Fragment.instantiate(Main.this, "com.jahirfiquitiva.paperboard.fragments." + fragment));
        tx.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else if (result != null && currentItem != 1) {
            result.setSelection(0);
        } else if (result != null) {
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody =
                        getResources().getString(R.string.share_one) +
                        getResources().getString(R.string.iconpack_designer) +
                        getResources().getString(R.string.share_two) +
                        MARKET_URL + getPackageName();
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, (getResources().getString(R.string.share_title))));
                break;

            case R.id.sendemail:
                StringBuilder emailBuilder = new StringBuilder();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getResources().getString(R.string.email_id)));
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));

                emailBuilder.append("\n \n \nOS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")");
                emailBuilder.append("\nOS API Level: " + Build.VERSION.SDK_INT);
                emailBuilder.append("\nDevice: " + Build.DEVICE);
                emailBuilder.append("\nManufacturer: " + Build.MANUFACTURER);
                emailBuilder.append("\nModel (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")");
                PackageInfo appInfo = null;
                try {
                    appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                emailBuilder.append("\nApp Version Name: " + appInfo.versionName);
                emailBuilder.append("\nApp Version Code: " + appInfo.versionCode);

                intent.putExtra(Intent.EXTRA_TEXT, emailBuilder.toString());
                startActivity(Intent.createChooser(intent, (getResources().getString(R.string.send_title))));
                break;

            case R.id.changelog:
                changelog();
                break;
        }
        return true;
    }

    public void addItemsToDrawer() {
        IDrawerItem walls = new PrimaryDrawerItem().withName(thaWalls).withIcon(GoogleMaterial.Icon.gmd_landscape).withIdentifier(4);
        IDrawerItem request = new PrimaryDrawerItem().withName(thaRequest).withIcon(GoogleMaterial.Icon.gmd_forum).withIdentifier(5);
        if (enable_features) {
            result.addItem(walls, 3);
            result.addItem(request, 4);
        }
    }

    private void runLicenseChecker() {
        if (firstrun) {
            if (withLicenseChecker) {
                checkLicense();
            } else {
                mPrefs.setFeaturesEnabled(true);
                addItemsToDrawer();
                showChangelogDialog();
            }
        } else {
            if (withLicenseChecker) {
                if (!enable_features) {
                    showNotLicensedDialog();
                } else {
                    addItemsToDrawer();
                    showChangelogDialog();
                }
            } else {
                addItemsToDrawer();
                showChangelogDialog();
            }
        }
    }

    private void changelog() {

        new MaterialDialog.Builder(this)
                .title(R.string.changelog_dialog_title)
                .adapter(new ChangelogAdapter(this, R.array.fullchangelog), null)
                .positiveText(R.string.nice)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mPrefs.setNotFirstrun();
                    }
                })
                .show();
    }

    private void showChangelogDialog() {

        String launchinfo = getSharedPreferences("PrefsFile", MODE_PRIVATE).getString("version", "0");
        if (!launchinfo.equals(getResources().getString(R.string.current_version))) {
            changelog();
        }
        storeSharedPrefs();
    }

    protected void storeSharedPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences("PrefsFile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("version", getResources().getString(R.string.current_version));
        editor.commit();
    }

    private void showNotConnectedDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.no_conn_title)
                .content(R.string.no_conn_content)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        int nSelection = currentItem - 1;
                        if (result != null) {
                            result.setSelection(nSelection);
                        }

                    }
                })
                .show();
    }

    public void checkLicense() {
        String installer = getPackageManager().getInstallerPackageName(getPackageName());
        try {
            if (installer.equals("com.google.android.feedback") ||
                    installer.equals("com.android.vending")) {
                new MaterialDialog.Builder(this)
                        .title(R.string.license_success_title)
                        .content(R.string.license_success)
                        .positiveText(R.string.close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                enable_features = true;
                                mPrefs.setFeaturesEnabled(true);
                                addItemsToDrawer();
                                showChangelogDialog();
                            }
                        })
                        .show();
            } else {
                showNotLicensedDialog();
            }
        } catch (Exception e) {
            showNotLicensedDialog();
        }
    }

    private void showNotLicensedDialog() {
        enable_features = false;
        mPrefs.setFeaturesEnabled(false);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.license_failed_title)
                .content(R.string.license_failed)
                .positiveText(R.string.download)
                .negativeText(R.string.exit)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + getPackageName()));
                        startActivity(browserIntent);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        finish();
                    }
                })
                .show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

}