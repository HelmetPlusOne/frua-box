package com.helmetplusone.android.frua;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import com.helmetplusone.android.frua.R;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonMap;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
class FileChooserDialogBuilder {
    private final SettingsActivity context;
    private Dialog dialog;
    private File curdir;
    private FsAdapter adapter;
    private EditText chosen;

    FileChooserDialogBuilder(SettingsActivity context) {
        this.context = context;
    }

    Dialog build() {
        // dialog
        this.dialog = new Dialog(context);
        dialog.setContentView(R.layout.dir_chooser);
        dialog.setTitle(context.getResources().getString(R.string.menu_install_dialog_title));
        // buttons
        Button cancel = (Button) dialog.findViewById(R.id.dir_list_cancel);
        cancel.setOnClickListener(new CancelListener());
        Button ok = (Button) dialog.findViewById(R.id.dir_list_ok);
        ok.setOnClickListener(new OkListener());
        // list
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.curdir = new File(path);
        this.adapter = new FsAdapter(context, TRUE, R.layout.dir_chooser_list_item, R.id.dir_textview);
        fill();
        ListView lv = (ListView) dialog.findViewById(R.id.dir_list);
        lv.setOnItemClickListener(new ItemListener());
        lv.setAdapter(adapter);
        this.chosen = (EditText) dialog.findViewById(R.id.dir_list_chosen);
        this.chosen.setText("");

        return dialog;
    }

    private void fill() {
        try {
            this.adapter.flip(curdir);
        } catch (IOException e) {
            String template = context.getResources().getString(R.string.pref_frua_directory_dialog_error);
            String msg = StrSubstitutor.replace(template, singletonMap("fruadir", curdir.getAbsolutePath()));
            new AlertDialog.Builder(context)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(R.string.pref_frua_directory_dialog_alert_ok, new ErrorListener())
                    .show();
        }
    }

    private class CancelListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    }

    private class OkListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String source = chosen.getText().toString();
            if (0 == source.length()) return;
            String target = context.getPrefStr(R.string.pref_frua_directory_key);
            new InstallTask(context, dialog, new File(source), new File(target)).execute();
        }
    }

    private class ErrorListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    private class ItemListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView tv = (TextView) view.findViewById(R.id.dir_textview);
            String text = tv.getText().toString();
            File ch = FsAdapter.TO_PARENT_DIR.equals(text) ? curdir.getParentFile() : new File(curdir, text);
            if (ch.isFile()) {
                chosen.setText(ch.getAbsolutePath());
            }
            else {
                curdir = ch;
                fill();
            }
        }
    }
}
