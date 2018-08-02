package com.cryptandroid.max.crypterandroid20;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

/**
 * Created by MAX on 30.05.2018.
 */

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
