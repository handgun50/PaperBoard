package com.jahirfiquitiva.paperboard.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.utilities.Util;

import jahirfiquitiva.paperboard.sample.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconDialog extends DialogFragment {

    public static IconDialog create(int resId, String name) {
        IconDialog dialog = new IconDialog();
        Bundle args = new Bundle();
        args.putInt("res_id", resId);
        args.putString("name", name);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String name = getArguments().getString("name");
        final int resId = getArguments().getInt("res_id");
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_icon, false)
                .title(Util.makeTextReadable(name))
                .positiveText(R.string.close)
                .build();
        if (dialog.getCustomView() != null) {
            ImageView dialogIcon = (ImageView) dialog.getCustomView().findViewById(R.id.dialogicon);
            dialogIcon.setImageResource(resId);
        }
        return dialog;
    }
}
