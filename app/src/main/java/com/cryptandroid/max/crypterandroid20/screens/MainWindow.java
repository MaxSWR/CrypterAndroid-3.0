package com.cryptandroid.max.crypterandroid20.screens;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cryptandroid.max.crypterandroid20.R;
import com.cryptandroid.max.crypterandroid20.controllers.FileController;
import com.cryptandroid.max.crypterandroid20.controllers.WindowsController;
import com.cryptandroid.max.crypterandroid20.crypt.FileCrypter;
import com.cryptandroid.max.crypterandroid20.crypt.MODE;
import com.cryptandroid.max.crypterandroid20.sets.Settings;
import java.io.File;

/**
 * Created by MAX on 30.05.2018.
 */

public class MainWindow extends Fragment implements View.OnClickListener {
    private WindowsController windowsController;
    private Button btn_choose_files;
    private Button btn_clear_files;
    private Button btn_crypt;
    private Button btn_decrypt;
    private Button btn_cancel;
    private LinearLayout ll_files_info;
    private TextView txv_files_head;
    private TextView txv_files;
    private TextView txtv_progress_bytes;
    private TextView txtv_progress_files;
    private ProgressBar pb_progress;
    private LinearLayout ll_progress;
    private String progress_bytes;
    private String progress_files;
    private enum STATE {CHOOSE_FILES, INFO_FILES, PROGRESS};
    private FileCrypter fileCrypter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_window, null);
        btn_choose_files = (Button)view.findViewById(R.id.chooseFiles);
        btn_choose_files.setOnClickListener(this);
        btn_clear_files = (Button)view.findViewById(R.id.btn_clear);
        btn_clear_files.setOnClickListener(this);
        btn_crypt = (Button)view.findViewById(R.id.btn_crypt);
        btn_crypt.setOnClickListener(this);
        btn_cancel = (Button)view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);
        btn_decrypt = (Button)view.findViewById(R.id.btn_decrypt);
        btn_decrypt.setOnClickListener(this);
        ll_files_info = (LinearLayout)view.findViewById(R.id.ll_files_onfo);
        txv_files = (TextView)view.findViewById(R.id.txv_files);
        txv_files_head = (TextView)view.findViewById(R.id.txv_files_head);
        ll_progress = (LinearLayout)view.findViewById(R.id.ll_progress);
        txtv_progress_bytes = (TextView)view.findViewById(R.id.txtv_progress_bytes);
        txtv_progress_files = (TextView)view.findViewById(R.id.txtv_progress_files);
        pb_progress = (ProgressBar)view.findViewById(R.id.pb_progress);
        progress_bytes = getResources().getString(R.string.txtv_progress_bytes);
        progress_files = getResources().getString(R.string.txtv_progress_files);
        showCurrentState(STATE.CHOOSE_FILES);

        new FileCrypter(this, "", "", MODE.DECRYPT);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chooseFiles:
                windowsController.setCurrentPage(1);
                windowsController.setFileManagerState(FilesWindow.STATE.CHOOSE_FILES);
                break;

            case R.id.btn_clear:
                clearFiles();
                break;

            case R.id.btn_crypt:
                boolean useDefault = (boolean) Settings.getSettings(windowsController).get("defaultm");

                if (useDefault) {
                    String name = Settings.getSettings(windowsController).get("name").toString();
                    String password = Settings.getSettings(windowsController).get("password").toString();
                    crypt(name, password);
                } else {
                    MessageWindow.showUserDataDialog(this);
                }

                break;

            case R.id.btn_decrypt:
                MessageWindow.showPassDialog(this);
                break;

            case R.id.btn_cancel:
                if (fileCrypter != null & fileCrypter.getStatus() == AsyncTask.Status.RUNNING) {
                    fileCrypter.cancel(false);
                }

                clearFiles();
                break;
        }
    }

    public Fragment setManager(WindowsController windowsController) {
        this.windowsController = windowsController;
        return this;
    }

    /**
     * Уведомлять активность о наличии файлов для работы
     */
    public void notifyFiles() {
        File[] files = FileController.getFiles();
        txv_files_head.setText(getResources().getString(R.string.txtv_choose_files) + " "+files.length);
        String filesInfo = "";

        for (File a : files) {
            filesInfo+=a.getAbsolutePath()+"\n\n";
        }

        txv_files.setText(filesInfo);
        showCurrentState(STATE.INFO_FILES);
    }

    /**
     * Отображать текущий этап работы
     */
    public void showCurrentState(STATE st) {
        switch (st) {
            case CHOOSE_FILES:
                btn_choose_files.setEnabled(true);
                ll_progress.setVisibility(View.GONE);
                ll_files_info.setVisibility(View.GONE);
                break;

            case INFO_FILES:
                btn_choose_files.setEnabled(true);
                ll_progress.setVisibility(View.GONE);
                ll_files_info.setVisibility(View.VISIBLE);
                break;

            case PROGRESS:
                btn_choose_files.setEnabled(false);
                ll_files_info.setVisibility(View.GONE);
                ll_progress.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Очистить список файлов для работы
     */
    public void clearFiles() {
        FileController.setFiles(new File[0]);
        showCurrentState(STATE.CHOOSE_FILES);
    }

    /**
     * Установить значение прогресса
     */
    public void setProgress(long bytesCurrent, long bytesAll, long fileCurrent, long fileAll) {
        int percent = (int) ((double)bytesCurrent*100/(double)bytesAll);
        pb_progress.setProgress(percent);
        String bytes = progress_bytes.replace("*", ""+bytesCurrent/1024);
        bytes = bytes.replace("?", ""+bytesAll/1024);
        String files = progress_files.replace("*", ""+fileCurrent);
        files = files.replace("?", ""+fileAll);
        txtv_progress_bytes.setText(bytes);
        txtv_progress_files.setText(files);
    }

    /**
     * Функция шифрования
     */
    public void crypt(String name, String password) {
        showCurrentState(STATE.PROGRESS);
        fileCrypter = new FileCrypter(this, name, password, MODE.ENCRYPT);
        fileCrypter.execute();
    }

    /**
     * Функция расшифрования
     */
    public void decrypt(String name, String password) {
        showCurrentState(STATE.PROGRESS);
        fileCrypter = new FileCrypter(this, name, password, MODE.DECRYPT);
        fileCrypter.execute();
    }

    public void finishOperation(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.show();
        showCurrentState(STATE.CHOOSE_FILES);
    }
}
