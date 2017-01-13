package com.moreno.fartbomb.util;

import java.io.*;
import java.util.regex.*;

import android.app.*;
import android.content.*;
import android.net.*;
import android.util.Log;

import com.loopj.android.http.RequestParams;
import com.moreno.fartbomb.data.User;
import com.moreno.fartbomb.network.*;

public class FartBombTools {
    private static final String LOG_TAG = FartBombTools.class.getSimpleName();
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Static access method that creates a DialogBuilder with a {@link AlertDialog.THEME_HOLO_LIGHT} theme.
     * 
     * @param context the context to display the dialog in
     * @param title the title of the dialog
     * @param message the message do display
     * @return an {@code AlertDialog.Builder} object containing the parameters and a default 'OK' button.<br>
     *         To show the dialog on the screen,
     *         use the void method show() on the return object after<br>
     *         modifying the default buttons.
     */
    public static AlertDialog.Builder buildDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(true);

        // set dialog message
        alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        // create alert dialog
        return alertDialogBuilder;

    }

    /**
     * Creates a ProgressDialog with the {@link ProgressDialog.THEME_HOLO_DARK} style and the parameter values.
     * 
     * @param context the context to display the dialog in
     * @param message the message to show the user. This value can be null.
     * @param indeterminate {@code true} if the dialog should be indeterminate
     * @return a {@link ProgressDialog} object ready to be displayed on the screen.
     */
    public static ProgressDialog createProgressDialog(Context context, String message, boolean indeterminate) {
        ProgressDialog dialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(indeterminate);
        if (message != null) {
            dialog.setMessage(message);
        }
        return dialog;
    }

    /**
     * Uses the {@link ConnectivityManager} to check the status of the mobile connection.
     * 
     * @param context the context of the activity.
     * @return {@code true} if the network is available.
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Read a file from raw resources into stream and write stream to string line by line
     * 
     * @param ctx the context where the resources live
     * @param resId the resource id eg. R.raw.text
     * @return the string containing the lines of the file
     */
    public static String readTextResource(Context ctx, int resId) {
        InputStream is = ctx.getResources().openRawResource(resId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = "";
        try {
            do {
                line = reader.readLine();
                sb.append(line);
            } while (line != null);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading file", e);
        }
        return sb.toString();
    }

    /**
     * Sends request that pulls the most recent data from the server
     * 
     * @param context the context to use
     * @param user the active user
     */
    public static void syncServerData(Context context, User user) {
        RequestParams params = new RequestParams();
        params.put("userId", "" + user.getUserId());
        FartBombRestClient.post(Servlet.USER_INFO.toString(), params, new SyncHandler(context, user));
    }

    /**
     * Validates an email address pattern using regex. <br>
     * Note: Does not check the email address' existence.
     * 
     * @param email the email to validate.
     * @return {@code true} if the email matches the regular expression: {@code ^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
     *         "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z] 2,})$}
     */
    public static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
