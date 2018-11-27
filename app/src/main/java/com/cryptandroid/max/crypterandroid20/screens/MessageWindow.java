package com.cryptandroid.max.crypterandroid20.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.cryptandroid.max.crypterandroid20.R;

public class MessageWindow {
    /**
     * Показать диалоговое окно с вводом имени и пароля
     */
    public static void showUserDataDialog(final MainWindow mainWindow) {
        final Resources r = mainWindow.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(mainWindow.getContext());
        builder.setTitle(r.getString(R.string.title_dialog_user_data));
        LayoutInflater inflater = mainWindow.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_template,null);
        builder.setView(layout);

        builder.setPositiveButton(r.getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TextView nameTV = (TextView)layout.findViewById(R.id.et_name);
                TextView passTV = (TextView)layout.findViewById(R.id.et_password);
                TextView passRepeatTV = (TextView)layout.findViewById(R.id.et_repeat_password);

                if (passTV.getText().toString().equals(passRepeatTV.getText().toString())) {
                    mainWindow.crypt(nameTV.getText().toString(), passTV.getText().toString());
                } else {
                    Toast toast = Toast.makeText(mainWindow.getContext(),
                            r.getString(R.string.error_repeat_password), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Показать диалоговое окно с вводом пароля для расшифровывания
     */
    public static void showPassDialog(final MainWindow mainWindow) {
        final Resources r = mainWindow.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(mainWindow.getContext());
        builder.setTitle(r.getString(R.string.title_dialog_user_data));
        LayoutInflater inflater = mainWindow.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_template,null);
        layout.findViewById(R.id.et_repeat_password).setVisibility(View.GONE);
        layout.findViewById(R.id.txtv3).setVisibility(View.GONE);
        builder.setView(layout);

        builder.setPositiveButton(r.getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TextView nameTV = (TextView)layout.findViewById(R.id.et_name);
                TextView passTV = (TextView)layout.findViewById(R.id.et_password);
                mainWindow.decrypt(nameTV.getText().toString(), passTV.getText().toString());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Показать диалоговое окно с вводом имени новой папки
     */
    public static void showNewFolderDialog(final FilesWindow mainWindow) {
        final Resources r = mainWindow.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(mainWindow.getContext());
        builder.setTitle(r.getString(R.string.title_dialog_new_folder));
        LayoutInflater inflater = mainWindow.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_template,null);
        layout.findViewById(R.id.et_repeat_password).setVisibility(View.GONE);
        layout.findViewById(R.id.txtv3).setVisibility(View.GONE);
        layout.findViewById(R.id.et_password).setVisibility(View.GONE);
        layout.findViewById(R.id.txtv2).setVisibility(View.GONE);
        builder.setView(layout);

        builder.setPositiveButton(r.getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TextView nameTV = (TextView)layout.findViewById(R.id.et_name);
                mainWindow.mkNewFolder(nameTV.getText().toString());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
