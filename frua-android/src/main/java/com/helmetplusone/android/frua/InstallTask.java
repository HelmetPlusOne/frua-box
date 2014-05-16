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

import static com.helmetplusone.android.frua.tools.Installer.MODULE_1_ID;
import static com.helmetplusone.android.frua.tools.Installer.MODULE_2_ID;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
// TODO: proper progress
class InstallTask extends AsyncTask<Void, Void, String> {
    private static final String SUCCESS = "";
    private static final Logger logger = getLogger(InstallTask.class);

    private final SettingsActivity context;
    private final Dialog dialog;
    private final File source;
    private final File target;
    private final ProgressDialog progress;
    private final AtomicBoolean cancelledByUser = new AtomicBoolean(false);

    InstallTask(SettingsActivity context, Dialog dialog, File source, File target) {
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
    protected String doInBackground(Void... params) {
        try {
            Installer installer = new Installer();
            installer.install(context, source, target);
            installer.installModule(context, MODULE_1_ID, target);
            installer.installModule(context, MODULE_2_ID, target);
            context.updateInstalledState(target);
            return SUCCESS;
        } catch (Exception e) {
            cancel(false);
            clean();
            return e.getMessage();
        }
    }

    private void clean() {
        try {
            cleanDirectory(target);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void onCancelled(String s) {
        this.progress.dismiss();
        if(cancelledByUser.get()) return;
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
