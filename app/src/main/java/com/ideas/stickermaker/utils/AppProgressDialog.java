package com.ideas.stickermaker.utils;

import android.app.Dialog;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;

import com.ideas.stickermaker.R;

public class AppProgressDialog {


    public static void snack(View container, String msg, String title, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar
                .make(container, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(title, listener);
        snackbar.show();
    }


    public static void show(Dialog mProgressDialog) {
        try {
            if (mProgressDialog.isShowing())
                return;
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setContentView(R.layout.layout_progress_bar);
            // ((TextView) mProgressDialog.findViewById(R.id.title)).setText(msg);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // startAnim(mProgressDialog);
            mProgressDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hide(Dialog mProgressDialog) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            //stopAnim(mProgressDialog);
            mProgressDialog.dismiss();
        }
    }

  /*  private static void startAnim(Dialog mProgressDialog) {
        ((AVLoadingIndicatorView) mProgressDialog.findViewById(R.id.avi)).smoothToShow();//show();
        // or avi.smoothToShow();
    }

    private static void stopAnim(Dialog mProgressDialog) {
        ((AVLoadingIndicatorView) mProgressDialog.findViewById(R.id.avi)).smoothToHide();
        // or avi.smoothToHide();
    }*/


}