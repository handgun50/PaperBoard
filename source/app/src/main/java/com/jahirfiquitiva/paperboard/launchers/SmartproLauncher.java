package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class SmartProLauncher {
    public SmartProLauncher(Context context) {
        Intent smartlauncherIntent = new Intent("ginlemon.smartlauncher.setGSLTHEME");
        smartlauncherIntent.putExtra("package", context.getPackageName());
        context.startActivity(smartlauncherIntent);
    }
}
