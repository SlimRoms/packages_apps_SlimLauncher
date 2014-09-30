package com.slim.slimlauncher.settings;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.slim.slimlauncher.Launcher;
import com.slim.slimlauncher.R;
import com.slim.slimlauncher.util.XmlUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupRestoreFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "BackupRestoreFragment";

    private static final String KEY_BACKUP = "backup";
    private static final String KEY_RESTORE = "restore";

    String mBackupPath;
    String mTempPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.backup_restore_preferences);

        Preference backupSettings = findPreference(KEY_BACKUP);
        backupSettings.setOnPreferenceClickListener(this);

        Preference restoreSettings = findPreference(KEY_RESTORE);
        restoreSettings.setOnPreferenceClickListener(this);

        mBackupPath = getBackupFile(null).getAbsolutePath();
        mTempPath = mBackupPath + "/temp";

        if (new File(mTempPath).mkdirs())
            if (DEBUG) Log.d(TAG, "Cannot create temp path!");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        cleanup();

        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy_HHmm");
        String current = sdf.format(new Date());
        final String file = "settings_" + current;

        if (preference.getKey().equals("backup")) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.backup_title));
            View dialogLayout = mInflater.inflate(R.layout.backup_dialog, null);
            final EditText editText = (EditText) dialogLayout.findViewById(R.id.backup_name);
            editText.setText(file);
            builder.setView(dialogLayout);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String finalFile = editText.getText().toString() + ".zip";
                    if (backup(getBackupFile(finalFile))) {
                        toast(getString(R.string.backup_completed));
                    } else {
                        toast(getString(R.string.backup_failed));
                    }
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        } else if (preference.getKey().equals("restore")) {

            CharSequence[] items = getBackupFile(null).list();

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.restore_title));
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String file = getBackupFile(null)
                            + File.separator + getBackupFile(null).list()[i];
                    if (DEBUG) Log.d(TAG, "file=" + file);
                    restoreDialog(new File(file));
                }
            });
            builder.show();
        }
        return true;
    }

    private void cleanup() {
        if (!new File(mTempPath).exists()) return;
        for (File f : new File(mTempPath).listFiles()) {
            f.delete();
        }
        new File(mTempPath).delete();
    }

    private boolean backup(File file) {

        cleanup();

        if (!backupPrefs()) {
            return false;
        }

        if (!backupHomescreen()) {
            return false;
        }

        File[] source = new File(mTempPath).listFiles();
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

            for (File f : source) {
                String name = f.getName();
                FileInputStream in = new FileInputStream(f);
                out.putNextEntry(new ZipEntry(name));
                int len;
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        cleanup();

        return true;
    }

    private boolean restore(File file) {

        cleanup();

        new File(mTempPath).mkdirs();

        ZipInputStream zis;
        try {
            String name;
            zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze;
            byte[] buf = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                name = ze.getName();

                if (ze.isDirectory()) {
                    new File(mTempPath + File.separator + name).mkdirs();
                    continue;
                }

                FileOutputStream out = new FileOutputStream(mTempPath + File.separator + name);

                while ((count = zis.read(buf)) != -1) {
                    out.write(buf, 0, count);
                }
                out.close();
                zis.closeEntry();
            }
            zis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File sharedPrefs = new File(mTempPath + File.separator
                + SettingsProvider.SETTINGS_KEY + ".xml");

        return restoreHomescreen() || restorePreferences();
    }

    private void toast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
    }

    private File getBackupFile(String name) {
        File backupPath = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Slim"
                + File.separator + "Launcher");
        if (!backupPath.exists()) {
            backupPath.mkdirs();
        }
        if (name != null) {
            backupPath = new File(backupPath.getAbsolutePath()
                    + (name != null ? File.separator + name : ""));
        }
        return backupPath;
    }

    private File getSharedPrefsFile() {
        return new File(getSharedPrefsDir().getPath()
                + File.separator + SettingsProvider.SETTINGS_KEY + ".xml");
    }

    private File getSharedPrefsDir() {
        return new File("/data/data" + File.separator
                + mContext.getPackageName()
                + File.separator + "shared_prefs");
    }

    private boolean copy(File in, File out) throws IOException {

        if (DEBUG) Log.d(TAG, "in=" + in.getAbsolutePath() + " : out=" + out.getAbsolutePath());

        if (!in.exists()) {
            if (!in.createNewFile()) {
                return false;
            }
        }

        if (!out.exists()) {
            File path = new File(out.getParent());
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    return false;
                }
            }
        }

        InputStream is = new FileInputStream(in);
        OutputStream os = new FileOutputStream(out);

        byte[] buf = new byte[1024];
        int len;

        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }

        is.close();
        os.close();

        return true;
    }

    private int chmod(File path, int mode) {
        try {
            Class<?> fileUtils = Class.forName("android.os.FileUtils");
            Method setPermissions = fileUtils.getMethod("setPermissions",
                    String.class, int.class, int.class, int.class);
            return (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void restoreDialog(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(file.getName());
        View dialogLayout = mInflater.inflate(R.layout.manage_backup_dialog, null);
        builder.setView(dialogLayout);
        builder.setPositiveButton(R.string.restore_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (restore(file)) {
                    toast(getString(R.string.restore_completed));
                    showKillLauncherDialog();
                } else {
                    toast(getString(R.string.restore_failed));
                }
            }
        });
        builder.setNeutralButton(R.string.delete_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!file.delete()) {
                    toast(getString(R.string.backup_delete_failed));
                } else {
                    toast(getString(R.string.backup_delete_completed));
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void showKillLauncherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.kill_dialog_title);
        View dialogLayout = mInflater.inflate(R.layout.manage_backup_dialog, null);
        TextView tv = (TextView) dialogLayout.findViewById(R.id.text);
        tv.setText(R.string.warn_kill_launcher);
        builder.setView(dialogLayout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent startActivity = new Intent(mContext, Launcher.class);
                int id = 123456;
                PendingIntent pi = PendingIntent.getActivity(mContext, id,
                        startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }

    private boolean backupPrefs() {
        File file = getSharedPrefsFile();
        String name = file.getName();
        File outFile = new File(mTempPath + File.separator + name);

        try {
            copy(file, outFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean backupHomescreen() {
        String databases = mContext.getFilesDir().getParent() + File.separator + "databases";

        String dbName = "launcher.db";
        String dbJournalName = "launcher.db-journal";

        File launcherDb = new File(databases + File.separator + dbName);
        File launcherDbJournal = new File(databases + File.separator + dbJournalName);

        File outLauncherDb = new File(mTempPath + File.separator + dbName);
        File outLauncherDbJournal = new File(mTempPath + File.separator + dbJournalName);

        try {
            copy(launcherDb, outLauncherDb);
            copy(launcherDbJournal, outLauncherDbJournal);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean restoreHomescreen() {
        File backupLauncherDb = new File(mTempPath + File.separator + "launcher.db");
        File backupLauncherDbJournal = new File(mTempPath + File.separator + "launcher.db-journal");

        String databases = mContext.getFilesDir().getParent() + File.separator + "databases";

        File launcherDb = new File(databases + File.separator + "launcher.db");
        File launcherDbJournal = new File(databases + File.separator + "launcher.db-journal");

        try {
            copy(backupLauncherDb, launcherDb);
            copy(backupLauncherDbJournal, launcherDbJournal);
            chmod(launcherDb, 0660);
            chmod(launcherDbJournal, 0700);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean restorePreferences() {
        File backupPrefs = new File(mTempPath + File.separator
                + SettingsProvider.SETTINGS_KEY + ".xml");
        File prefs = getSharedPrefsFile();
        try {
            copy(backupPrefs, prefs);
            chmod(getSharedPrefsDir(), 0771);
            chmod(prefs, 0664);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
    }
 }
