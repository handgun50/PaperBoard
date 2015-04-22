package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class HoloHDLauncher {
    public HoloHDLauncher(Context context) {
        Intent holohdApply = new Intent(Intent.ACTION_MAIN);
        holohdApply.setComponent(new ComponentName("com.mobint.hololauncher.hd", "com.mobint.hololauncher.SettingsActivity"));
        context.startActivity(holohdApply);
    }
}
