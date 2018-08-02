package com.cryptandroid.max.crypterandroid20;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Set;

public class WindowsController extends FragmentActivity {
    private ViewPager pager;
    private PagerAdapter windowAdapter;
    private final int PAGE_COUNT = 3;
    private Fragment[] fragmentsList = new Fragment[PAGE_COUNT];

    @Override
    public void onBackPressed() {
        if (pager != null) {
            int position = pager.getCurrentItem();

            if (position == 1) {
                FilesWindow tmp = (FilesWindow)fragmentsList[position];
                tmp.onBackPressed();
            } else {
                super.onBackPressed();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_windows_controller);
        pager = (ViewPager) findViewById(R.id.pager);
        windowAdapter = new WindowAdapter(getSupportFragmentManager());
        pager.setAdapter(windowAdapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(pager);

        //установка изображений на вкладки
        int[] imageResId = {
                R.drawable.main, R.drawable.folder, R.drawable.settings
        };

        for (int i = 0; i < imageResId.length; i++) {
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }
    }

    public void setCurrentPage(int position) {
        if (pager != null) {
            pager.setCurrentItem(position);
        } else {
            super.onBackPressed();
        }
    }

    public void setFileManagerState(FilesWindow.STATE st) {
        if (fragmentsList[1] != null) {
            FilesWindow filesWindow = (FilesWindow)fragmentsList[1];
            filesWindow.setState(st);
        }
    }

    public void notifyFiles() {
        MainWindow mainWindow = (MainWindow)fragmentsList[0];
        mainWindow.notifyFiles();
    }

    public void notifyDefaultFolder() {
        SettingsWindow settingsWindow = (SettingsWindow)fragmentsList[2];
        settingsWindow.notifyDefaultFolder();
    }

    /**
     * @class Адаптер перелистывания окон приложения
     */
    private class WindowAdapter extends FragmentPagerAdapter {


        public WindowAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment window = null;

            switch (position) {
                case 0:
                    fragmentsList[position] = new MainWindow().setManager(WindowsController.this);
                    break;
                case 1:
                    fragmentsList[position] = new FilesWindow().setManager(WindowsController.this);
                    FilesWindow fileWindow = new FilesWindow();
                    fileWindow.setState(FilesWindow.STATE.DEFAULT);
                    fragmentsList[position] = fileWindow.setManager(WindowsController.this);
                    break;
                case 2:
                    fragmentsList[position] = new SettingsWindow().setManager(WindowsController.this);
                    break;
                default:
                    position = 0;
                    fragmentsList[position] = new MainWindow().setManager(WindowsController.this);
            }

            return fragmentsList[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }



}


