package com.idoideas.stickermaker.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

public class Alerts {

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void show(View container, String msg) {
        Snackbar.make(container, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void withFeedback(View container, String msg, String btnTitle, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(container, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(btnTitle, onClickListener);
        snackbar.show();
    }
}