package com.cryptandroid.max.crypterandroid20.screens;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import com.cryptandroid.max.crypterandroid20.R;
import com.cryptandroid.max.crypterandroid20.controllers.FileController;
import com.cryptandroid.max.crypterandroid20.controllers.WindowsController;
import java.io.File;

public class SettingsWindow extends PreferenceFragmentCompat {
    private WindowsController windowsController;
    private SharedPreferences sp;
    private android.support.v7.preference.Preference defaultFolder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_layout);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String defFolder = sp.getString("defaultFolder", getResources().getString(R.string.defaultFolderPath));
        defaultFolder = (android.support.v7.preference.Preference)findPreference("defaultFolder");
        defaultFolder.setSummary(defFolder);

        defaultFolder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                windowsController.setCurrentPage(1);
                windowsController.setFileManagerState(FilesWindow.STATE.CHOOSE_FOLDER);
                return false;
            }
        });
    }

    public Fragment setManager(WindowsController windowsController) {
        this.windowsController = windowsController;
        return this;
    }

    public void notifyDefaultFolder() {
        File[] files = FileController.getFiles();

        if (files.length < 1)
            return;

        if (sp != null && defaultFolder != null) {
            sp.edit().putString("defaultFolder", files[0].getAbsolutePath()).commit();
            defaultFolder.setSummary(files[0].getAbsolutePath());
        }
    }
}
