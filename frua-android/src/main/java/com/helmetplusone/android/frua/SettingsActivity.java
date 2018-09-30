package com.helmetplusone.android.frua;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.helmetplusone.android.frua.R;
import com.helmetplusone.android.frua.tools.DosBoxLauncher;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Map;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;

/**
 * User: helmetplusone
 * Date: 3/29/13
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Menu menu;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        // may not work in 2.2
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    // todo: check FRUA installed on startup
    protected void onStart() {
        super.onStart();
        // frua dir
        final String dir;
        String curdir = getPrefStr(R.string.pref_frua_directory_key);
        if(0 == curdir.length()) {
            File fruadir = new File(getExternalFilesDir(null), "frua");
            fruadir.mkdirs();
            dir = fruadir.getAbsolutePath();
        } else dir = curdir;
        setPrefStr(R.string.pref_frua_directory_key, dir);
        // version
        setPrefStr(R.string.pref_app_version_key, version());
        // force summary updates
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        for(String key : sp.getAll().keySet()) {
            onSharedPreferenceChanged(sp, key);
        }
        // bind actions
        DirChooserDialogBuilder dirChooser = new DirChooserDialogBuilder(this);
        pref(R.string.pref_frua_directory_key).setOnPreferenceClickListener(dirChooser.listener());
        ModuleSwitcherDialogBuilder switcher = new ModuleSwitcherDialogBuilder(this);
        pref(R.string.pref_frua_active_module_key).setOnPreferenceClickListener(switcher.listener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        boolean res = super.onCreateOptionsMenu(menu);
        return res;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean res = super.onPrepareOptionsMenu(menu);
        updateInstalledState(new File(getPrefStr(R.string.pref_frua_directory_key)));
        return res;
    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
        updateInstalledState(new File(getPrefStr(R.string.pref_frua_directory_key)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_run:
                start();
                return true;
            case R.id.menu_add_module:
                new ModuleChooserDialogBuilder(this).build().show();
                return true;
            case R.id.menu_install:
                new FileChooserDialogBuilder(this).build().show();
                return true;
            case R.id.menu_download:
                openBrowser(R.string.menu_download_link, R.string.menu_download_dialog_warning);
                return true;
            case R.id.menu_modules:
                openBrowser(R.string.menu_modules_link, R.string.menu_modules_dialog_warning);
                return true;
            case R.id.menu_docs:
                openBrowser(R.string.menu_docs_link, R.string.menu_docs_dialog_warning);
                return true;
            case R.id.menu_quit:
                finish();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        Object val = sharedPreferences.getAll().get(key);
        if(null != val && val instanceof String) {
            String st = (String) val;
            if(st.length() > 0) pref.setSummary(st);
        }
    }

    private void start() {
        try {
            String fruadir = getPrefStr(R.string.pref_frua_directory_key);
            String cpucore = getPrefStr(R.string.pref_cpu_cores_key);
            String conf = FruaBoxConf.CONF
                    .replace("{{fruapath}}", fruadir)
                    .replace("{{cpucore}}", cpucore);
            File confFile = new File(getExternalFilesDir(null), "fruabox.conf");
            writeStringToFile(confFile, conf);
            Intent intent = new Intent(this, DosBoxLauncher.class);
            Bundle bundle = new Bundle();
            bundle.putInt("cycles", getPrefInt(R.string.pref_cpu_cycles_key));
            bundle.putInt("frameskip", getPrefInt(R.string.pref_frameskip_key));
            bundle.putBoolean("sound", getPrefBool(R.string.pref_audio_enabled_key));
            bundle.putBoolean("fmode", getPrefBool(R.string.pref_fmode_key));
            intent.putExtras(bundle);
            startActivityForResult(intent, 42);
        } catch (Exception e) {
            throw new UnhandledException(e);
        }
    }

    private String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void openBrowser(int linkId, int warningId) {
        String link = getResources().getString(linkId);
        String warning = getResources().getString(warningId);
        new AlertDialog.Builder(this)
                .setMessage(warning)
                .setNegativeButton(R.string.cancel_button_text, null)
                .setPositiveButton(R.string.open_browser_button_text, new OpenBrowserListener(link))
                .show();
    }

    String getPrefStr(int keyId) {
        String key = getResources().getString(keyId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Object val = sp.getAll().get(key);
        if(null == val) return "";
        if (!(val instanceof String)) throw new IllegalArgumentException("Setting key: [" + key + "], " +
                " type is: [" + val.getClass().getName() + "], expected type: [String]");
        return (String) val;
    }

    void setPrefStr(int keyId, String val) {
        String key = getResources().getString(keyId);
        setPrefStr(key, val);
    }

    void setPrefStr(String key, String val) {
        if(null == val) throw new IllegalArgumentException("Provided values is null, key: [" + key + "]");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, val);
        editor.commit();
        Preference pref = findPreference(key);
        pref.setSummary(val);
    }

    int getPrefInt(int keyId) {
        String key = getResources().getString(keyId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(sp.getAll().get(key).toString());
    }

    boolean getPrefBool(int keyId) {
        String key = getResources().getString(keyId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Object val = sp.getAll().get(key);
        if(null == val) return false;
        if (!(val instanceof Boolean)) throw new IllegalArgumentException("Setting key: [" + key + "], " +
                " type is: [" + val.getClass().getName() + "], expected type: [Boolean]");
        return (Boolean) val;
    }

    void setPrefBool(int keyId, boolean val) {
        String key = getResources().getString(keyId);
        setPrefBool(key, val);
    }

    void setPrefBool(String key, boolean val) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    Preference pref(int keyId) {
        String key = getResources().getString(keyId);
        return findPreference(key);
    }

    boolean checkFruaInstalled(File dir) {
        if(!dir.exists()) return false;
        if(!dir.isDirectory()) return false;
        File[] files = dir.listFiles((FileFilter) FILE);
        if(null == files) return false;
        for(File fi : files) {
            if("start.bat".equalsIgnoreCase(fi.getName())) return true;
        }
        return false;
    }

    boolean updateInstalledState(File dir) {
        boolean res = checkFruaInstalled(dir);
        setPrefBool(R.string.pref_frua_installed_state_key, res);
        runOnUiThread(new UpdateUiInstalled(res));
        return res;
    }

    private class UpdateUiInstalled implements Runnable {
        private final boolean fruaInstalled;

        private UpdateUiInstalled(boolean fruaInstalled) {
            this.fruaInstalled = fruaInstalled;
        }

        @Override
        public void run() {
            pref(R.string.pref_skip_settings_on_start_key).setEnabled(false);
            pref(R.string.pref_smooth_video_key).setEnabled(false);
            pref(R.string.pref_frua_active_module_key).setEnabled(fruaInstalled);
            if(null != menu) {
                menu.findItem(R.id.menu_run).setEnabled(fruaInstalled);
                menu.findItem(R.id.menu_add_module).setEnabled(fruaInstalled);
            }
        }
    }

    String replace(int id, String key, String value) {
        String template = getResources().getString(id);
        Map<String, String> map = Collections.singletonMap(key, value);
        return StrSubstitutor.replace(template, map);
    }

    private class OpenBrowserListener implements DialogInterface.OnClickListener {
        private final String link;

        private OpenBrowserListener(String link) {
            this.link = link;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
