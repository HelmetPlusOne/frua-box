package com.helmetplusone.android.frua;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.preference.Preference;
import android.view.View;
import android.widget.*;
import com.helmetplusone.android.frua.R;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static com.helmetplusone.android.frua.FsAdapter.TO_PARENT_DIR;
import static java.util.Collections.singletonMap;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * User: helmetplusone
 * Date: 4/1/13
 */
class ModuleSwitcherDialogBuilder {
    private static final FileFilter DSN_FILTER = new RegexFileFilter("^.+\\.dsn$", CASE_INSENSITIVE);

    private final SettingsActivity context;
    private Dialog dialog;
    private File curdir;
    private FsAdapter adapter;
    private EditText chosen;

    ModuleSwitcherDialogBuilder(SettingsActivity context) {
        this.context = context;
    }

    // todo: proper array adapter
    Dialog build() {
        // dialog
        this.dialog = new Dialog(context);
        dialog.setContentView(R.layout.dir_chooser);
        dialog.setTitle(context.getResources().getString(R.string.pref_frua_directory_dialog_title));
        // buttons
        Button cancel = (Button) dialog.findViewById(R.id.dir_list_cancel);
        cancel.setOnClickListener(new CancelListener());
        Button ok = (Button) dialog.findViewById(R.id.dir_list_ok);
        ok.setOnClickListener(new OkListener());
        // list
        String path = context.getPrefStr(R.string.pref_frua_directory_key);
        this.curdir = new File(path);
        this.adapter = new FsAdapter(context, DSN_FILTER, R.layout.dir_chooser_list_item, R.id.dir_textview);
        fill();
        ListView lv = (ListView) dialog.findViewById(R.id.dir_list);
        lv.setOnItemClickListener(new ItemListener());
        lv.setAdapter(adapter);
        this.chosen = (EditText) dialog.findViewById(R.id.dir_list_chosen);
        this.chosen.setText(curdir.getAbsolutePath());

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

    Preference.OnPreferenceClickListener listener() {
        return new CreateListener();
    }

    private class CreateListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            build().show();
            return true;
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
            new SwitchModuleTask(context, dialog, curdir).execute();
//            if (installed) {
//                context.setPrefStr(R.string.pref_frua_directory_key, curdir.getAbsolutePath());
//                dialog.dismiss();
//            } else {
//                String template = context.getResources().getString(R.string.pref_frua_directory_dialog_alert);
//                String msg = StrSubstitutor.replace(template, singletonMap("fruadir", curdir.getAbsolutePath()));
//                new AlertDialog.Builder(context)
//                        .setMessage(msg)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.pref_frua_directory_dialog_alert_ok, null)
//                        .show();
//            }
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
            if (TO_PARENT_DIR.equals(text)) curdir = curdir.getParentFile();
            else curdir = new File(curdir, text);
            chosen.setText(curdir.getAbsolutePath());
            fill();
        }
    }

}
