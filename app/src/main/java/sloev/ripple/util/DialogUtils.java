package sloev.ripple.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;
import sloev.ripple.R;

/**
 * Created by johannes on 09/03/15.
 * taken from:
 * package com.quickblox.sample.location.activities.dialogutils
 */
public class DialogUtils {

    private static Toast toast;

    public static void show(Context context, String message) {
        if (message == null) {
            return;
        }
        if (toast == null && context != null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }
        if (toast != null) {
            toast.setText(message);
            toast.show();
        }
    }

    public static void showLong(Context context, String message) {
        if (message == null) {
            return;
        }
        if (toast == null && context != null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        }
        if (toast != null) {
            toast.setText(message);
            toast.show();
        }
    }

    public static android.app.Dialog createDialog(Context context, int titleId, int messageId,
                                                  DialogInterface.OnClickListener positiveButtonListener,
                                                  DialogInterface.OnClickListener negativeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.dialog_ok, positiveButtonListener);
        builder.setNegativeButton(R.string.dialog_cancel, negativeButtonListener);

        return builder.create();
    }

    public static android.app.Dialog createDialog(Context context, int titleId, int messageId, View view,
                                                  DialogInterface.OnClickListener positiveClickListener,
                                                  DialogInterface.OnClickListener negativeClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_ok, positiveClickListener);
        builder.setNegativeButton(R.string.dialog_cancel, negativeClickListener);

        return builder.create();
    }
}