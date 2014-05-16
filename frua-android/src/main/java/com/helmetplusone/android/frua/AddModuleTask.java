package com.helmetplusone.android.frua;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import com.helmetplusone.android.frua.R;
import com.helmetplusone.android.frua.tools.Installer;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
// todo: messages
class AddModuleTask extends AsyncTask<Void, Void, String> {
    private static final String SUCCESS = "";
    private static final Logger logger = getLogger(InstallTask.class);

    private final SettingsActivity context;
    private final Dialog dialog;
    private final File source;
    private final File target;
    private final ProgressDialog progress;
    private final AtomicBoolean cancelledByUser = new AtomicBoolean(false);

    AddModuleTask(SettingsActivity context, Dialog dialog, File source, File target) {
        this.context = context;
        this.dialog = dialog;
        this.source = source;
        this.target = target;
        this.progress = new ProgressDialog(context);
        this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progress.setCancelable(true);
        this.progress.setMessage(context.replace(R.string.install_progress_message, "fruadir", target.getAbsolutePath()));
        this.progress.setCanceledOnTouchOutside(false);
        this.progress.setOnCancelListener(new CancelListener());
    }

    @Override
    protected void onPreExecute() {
        this.progress.show();
    }

    @Override
    // todo: module cleaning
    protected String doInBackground(Void... params) {
        try {
            Installer installer = new Installer();
            installer.installModule(source, target);
            context.updateInstalledState(target);
            return SUCCESS;
        } catch (Exception e) {
            cancel(false);
            return e.getMessage();
        }
    }

    @Override
    protected void onCancelled(String s) {
        this.progress.dismiss();
        if (cancelledByUser.get()) return;
        new AlertDialog.Builder(context)
                .setTitle(R.string.error_title)
                .setMessage(context.replace(R.string.install_error_text, "text", s))
                .setPositiveButton(R.string.dismiss_button_text, null)
                .show();
    }

    @Override
    protected void onPostExecute(String s) {
        this.progress.dismiss();
        new AlertDialog.Builder(context)
                .setTitle(R.string.install_successful_title)
                .setMessage(context.replace(R.string.install_successful_text, "fruadir", target.getAbsolutePath()))
                .setPositiveButton(R.string.dismiss_button_text, null)
                .show();
        dialog.dismiss();
    }

    private class CancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            cancelledByUser.set(true);
        }
    }

}
