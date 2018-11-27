package com.cryptandroid.max.crypterandroid20.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cryptandroid.max.crypterandroid20.R;
import com.cryptandroid.max.crypterandroid20.controllers.FileController;
import com.cryptandroid.max.crypterandroid20.controllers.WindowsController;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FilesWindow extends Fragment implements View.OnClickListener, CheckBox.OnCheckedChangeListener {
    public enum STATE {DEFAULT, MOVE, COPY, CHOOSE_FOLDER, CHOOSE_FILES};
    private WindowsController windowsController;
    private RecyclerView fileList;
    private File currentFolder = new File("/");
    private File[] files;
    private boolean[] checkedFiles;
    private FileListAdapter flAdapter;
    private TextView fileManagerPathHead;
    private Button btn_home;
    private Button btn_finish;
    private Button btn_menu;
    private TextView countFilesTitle;
    private CheckBox chb_selectAll;
    private STATE state;
    private STATE oldState;
    private File[] filesToAction;

    public Fragment setManager(WindowsController windowsController) {
        this.windowsController = windowsController;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_manager, null);
        countFilesTitle = (TextView)view.findViewById(R.id.numChooseFiles);
        btn_home = (Button)view.findViewById(R.id.btn_home);
        btn_home.setOnClickListener(this);
        btn_finish = (Button)view.findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(this);
        btn_menu = (Button)view.findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(this);
        registerForContextMenu(btn_menu);
        chb_selectAll = (CheckBox)view.findViewById(R.id.select_all_items);
        chb_selectAll.setOnCheckedChangeListener(this);
        fileList = (RecyclerView)view.findViewById(R.id.fileList);
        fileList.setLayoutManager(new LinearLayoutManager(getContext()));
        flAdapter = new FileListAdapter();
        fileList.setAdapter(flAdapter);
        fileManagerPathHead = (TextView)view.findViewById(R.id.fileManagerPathHead);
        browseTo(new File("/"));
        setState(STATE.DEFAULT);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuItem newFolder = menu.add(Menu.NONE, 0, 0, getResources().getString(R.string.option_fm_menu0));
        MenuItem copy = menu.add(Menu.NONE, 1, 1, getResources().getString(R.string.option_fm_menu1));
        MenuItem move = menu.add(Menu.NONE, 2, 2, getResources().getString(R.string.option_fm_menu2));
        MenuItem delete = menu.add(Menu.NONE, 3, 3, getResources().getString(R.string.option_fm_menu3));
        MenuItem paste = menu.add(Menu.NONE, 4, 4, getResources().getString(R.string.option_fm_menu4));
        MenuItem cancel = menu.add(Menu.NONE, 5, 5, getResources().getString(R.string.option_fm_menu5));

        if (state == STATE.COPY || state == STATE.MOVE) {
            newFolder.setEnabled(false);
            copy.setEnabled(false);
            move.setEnabled(false);
            paste.setEnabled(true);
            delete.setEnabled(false);
            cancel.setEnabled(true);
        } else {
            newFolder.setEnabled(true);
            copy.setEnabled(true);
            move.setEnabled(true);
            paste.setEnabled(false);
            delete.setEnabled(true);
            cancel.setEnabled(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case 0:// новая папка
                MessageWindow.showNewFolderDialog(this);
                break;

            case 1://копировать
                oldState = state;
                state = STATE.COPY;
                break;

            case 2://переместить
                oldState = state;
                state = STATE.MOVE;
                break;

            case 3://удалить
                deleteAllElements();
                break;

            case 4://вставить
                if (filesToAction != null & filesToAction.length != 0) {
                    try {
                        for (File a : filesToAction) {
                            if (a.isDirectory()) {
                                    FileUtils.copyDirectory(
                                            a,
                                            new File(currentFolder.getAbsolutePath()+"/"+a.getName()),
                                            true);
                                    if (state == STATE.MOVE) {
                                        FileUtils.deleteDirectory(a);
                                    }
                            } else {
                                FileUtils.copyFileToDirectory(a, currentFolder, true);

                                if (state == STATE.MOVE) {
                                    FileUtils.forceDelete(a);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

                state = oldState;
                filesToAction = null;
                browseTo(currentFolder);
                break;

            case 5://отмена
                state = oldState;
                filesToAction = null;
                break;
        }

        if (id == 1 || id == 2) {//Если копировать или перемещать
            int count = 0;

            for (boolean a : checkedFiles) {
                if (a) {
                    count++;
                }
            }

            filesToAction = new File[count];
            count = 0;

            for (int i = 0; i < files.length; i++) {
                if (checkedFiles[i]) {
                    filesToAction[count++] = new File(files[i].getAbsolutePath());
                }
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int position;

        switch (view.getId()) {
            case R.id.itemName:
                position = fileList.getChildLayoutPosition((View)view.getParent().getParent());

                if (files[position].isDirectory()) {
                    browseTo(files[position]);
                } else {
                    Uri uri = Uri.parse("file://"+files[position].getAbsolutePath());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                    if (intent.resolveActivity(windowsController.getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        intent.setDataAndType(uri, "text/plain");
                        startActivity(intent);
                    }
                }

                break;

            case R.id.btn_home:
                browseTo(new File("/"));
                break;

            case R.id.btn_menu:
                windowsController.openContextMenu(view);
                break;

            case R.id.btn_finish:
                int count = 0;

                //подсчет файлов для работы
                for (int i = 0; i < checkedFiles.length; i++) {
                    if (checkedFiles[i] && (files[i].isFile() && state == STATE.CHOOSE_FILES ||
                        files[i].isDirectory() && state == STATE.CHOOSE_FOLDER)) {
                        count++;
                    }
                }

                if (count > 0) {
                    File [] filesAction = new File[count];
                    count = 0;

                    for (int i = 0; i < checkedFiles.length; i++) {
                        if (checkedFiles[i] && (files[i].isFile() && state == STATE.CHOOSE_FILES ||
                                files[i].isDirectory() && state == STATE.CHOOSE_FOLDER)) {
                            filesAction[count++] = new File(files[i].getAbsolutePath());
                        }
                    }

                    FileController.setFiles(filesAction);

                    switch (state) {
                        case CHOOSE_FILES:
                            windowsController.notifyFiles();
                            break;
                        case CHOOSE_FOLDER:
                            windowsController.notifyDefaultFolder();
                            break;
                    }
                }

                finish();
                break;

            case R.id.itemCheck:
                CheckBox v = (CheckBox)view;
                position = fileList.getChildLayoutPosition((View)view.getParent().getParent());
                checkedFiles[position] = v.isChecked();
                boolean all_select = true;

                for (boolean a : checkedFiles) {
                    if (!a) {
                        all_select = false;
                        break;
                    }
                }

                setTitleCountFiles();
                chb_selectAll.setChecked(all_select);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.select_all_items:
                boolean all_selected = true;

                for(boolean a : checkedFiles) {
                    if (!a) {
                        all_selected = false;
                        break;
                    }
                }

                if (all_selected && !b || !all_selected && b) {
                    for (int i = 0; i < checkedFiles.length; i++) {
                        checkedFiles[i] = !all_selected;
                    }
                }

                setTitleCountFiles();
                flAdapter.notifyDataSetChanged();
                break;
        }
    }


    /**
     * навигация по папкам
     */
    private void browseTo(final File f) {
        if(f.isDirectory() && f.canRead()) {
            currentFolder = f;
            files = currentFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.canRead()) {
                        return true;
                    }
                    return false;
                }
            });
            chb_selectAll.setChecked(false);
            checkedFiles = new boolean[files.length];
            fileManagerPathHead.setText(currentFolder.getAbsolutePath());
            setTitleCountFiles();
            setTitleCountFiles();
            flAdapter.notifyDataSetChanged();
            fileList.scrollToPosition(0);
        }
    }

    /**
     * отобразить количество выбранных элементов
     */
    public int setTitleCountFiles() {
        int count = 0;

        for (int i = 0; i < checkedFiles.length; i++) {
            if (checkedFiles[i]) {
                count++;
            }
        }

        countFilesTitle.setText(getResources().getString(R.string.txtv_choose_items)+ " " + count);
        return  count;
    }

    /**
     * Установить сосотояние
     * @param st - состояние
     */
    public void setState(STATE st) {
        state = st;
        switch (state) {
            case DEFAULT:
                if (btn_finish != null) {
                    btn_finish.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                if (btn_finish != null) {
                    btn_finish.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * Создать новую папку
     * @param name имя новой папки
     */
    public void mkNewFolder(String name) {
        if (name.isEmpty()) {
            return;
        }

        for (File a : files) {
            if (a.getName().equals(name) && a.isDirectory()) {
                Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.error_repeat_name_folder), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }

        File newFolder = new File(currentFolder.getAbsolutePath() + "/" + name);

        if (!newFolder.mkdirs()) {
            Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.error_cant_mkdir), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        browseTo(currentFolder);
    }

    /**
     * Удалить все отмеченные файлы и папки
     */
    public void deleteAllElements() {
        try {
            for (int i = 0; i < files.length; i++) {
                if (checkedFiles[i]) {
                    if (files[i].isDirectory()) {
                        FileUtils.deleteDirectory(files[i]);
                    } else {
                        FileUtils.forceDelete(files[i]);
                    }
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }

        browseTo(currentFolder);
    }

    /**
     * Нажатие клавиши НАЗАД
     */
    public void onBackPressed() {
        if (currentFolder.getParentFile() != null) {
            browseTo(currentFolder.getParentFile());
        } else {
            finish();
        }
    }

        /**
         * Закончить работу с файловым менеджером
         */
    private void finish() {
        switch (state) {
            case CHOOSE_FILES:
                windowsController.setCurrentPage(0);
                break;

            case CHOOSE_FOLDER:
                windowsController.setCurrentPage(2);
                break;

            default:
                windowsController.setCurrentPage(0);
                break;
        }

        setState(STATE.DEFAULT);
        browseTo(new File("/"));
    }

    /**
     * @class Адаптер списка файлов
     */
    public class FileListAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setName(files[position].getName());
            holder.setCheckBox(checkedFiles[position]);

            if (files[position].isDirectory()) {
                holder.setIcon(R.drawable.folder);
            } else {
                holder.setIcon(R.drawable.file);
            }
        }

        @Override
        public int getItemCount() {
            return files.length;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CheckBox checkBox;
        private ImageView icon;

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setCheckBox(boolean checkBox) {
            this.checkBox.setChecked(checkBox);
        }

        public void setIcon(int resource) {
            this.icon.setImageResource(resource);
        }

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.itemName);
            name.setOnClickListener(FilesWindow.this);
            icon = (ImageView)itemView.findViewById(R.id.itemIcon);
            checkBox = (CheckBox)itemView.findViewById(R.id.itemCheck);
            checkBox.setOnClickListener(FilesWindow.this);
        }
    }
}

