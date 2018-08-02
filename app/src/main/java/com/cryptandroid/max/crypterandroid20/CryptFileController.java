package com.cryptandroid.max.crypterandroid20;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by MAX on 10.06.2018.
 */

public class CryptFileController extends AsyncTask<Void, Long, Void> {
    private final int MAX_SIZE_BYTES = 10*1024*1024;
    private final int HEADER_SIZE_BYTES = 200;
    private final String EXTENSION_EACH = ".caf"; //Crypto Android File
    private final String EXTENSION_SAFE = ".cafs"; //Crypto Android File Safe
    private boolean eachFiles;
    private boolean hideNames;
    private boolean speedAction;
    private boolean deleteFiles;
    private boolean ownerPath;
    private String EXT;
    private Crypter crypter;
    private MainWindow mainWindow;
    private Settings.MODE mode;
    private File[] files;
    private String message;
    private InputStream in = null;
    private OutputStream out = null;

    public CryptFileController(MainWindow mainWindow, String name, String password, Settings.MODE mode) {
        files = FileController.getFiles();
        this.mode = mode;
        this.mainWindow = mainWindow;
        String[] lenKeys = mainWindow.getResources().getStringArray(R.array.key_length);
        int lenKeyOPtion = Integer.parseInt(Settings.getSettings(mainWindow.getActivity()).get("safety").toString()) - 1;
        int algKeyLen = Integer.parseInt(lenKeys[lenKeyOPtion]);
        crypter = new Crypter(name, password, algKeyLen);
        eachFiles = (boolean)Settings.getSettings(mainWindow.getActivity()).get("modeEach");
        hideNames = (boolean)Settings.getSettings(mainWindow.getActivity()).get("hidepath");
        speedAction = (boolean)Settings.getSettings(mainWindow.getActivity()).get("speed_mode");
        deleteFiles = (boolean)Settings.getSettings(mainWindow.getActivity()).get("deleteAfterAction");
        ownerPath = (boolean)Settings.getSettings(mainWindow.getActivity()).get("ownerpath");
        EXT = eachFiles ? EXTENSION_EACH : EXTENSION_SAFE;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Exception ex = null;

        try {
            switch (mode) {
                case CRYPT:
                    crypt();
                    break;
                case DECRYPT:
                    decrypt();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            ex = e;
        } finally {

            if (ex != null) {
                message += ex.getMessage();
                cancel(false);
            }

            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (isCancelled()) {
            message += "\n" + mainWindow.getResources().getString(R.string.cancel_work);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        mainWindow.setProgress(values[0], values[1], values[2], values[3]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        message = mainWindow.getResources().getString(R.string.good_work);
        mainWindow.finishOperation(message);
    }

    @Override
    protected void onCancelled() {
        mainWindow.finishOperation(message);
    }

    public void crypt() throws IOException {
        long allBytesLong = 0;
        long currentSize = 0;
        long allFilesLong = files.length;
        boolean firstStep = true;
        byte[] buffer;
        byte[] actionBuffer;
        byte[][] headerInfo;
        File dstFolder = FileController.getNewActionFolder(Settings.getSettings(mainWindow.getActivity()).get("defaultFolder").toString(), "crypt");
        FileUtils.forceMkdir(dstFolder);

        //подсчет места
        for (File a : files) {
            allBytesLong += a.length();
        }

        //если не хватает места
        if (allBytesLong > dstFolder.getUsableSpace()) {
            message = mainWindow.getResources().getString(R.string.need_space);
            cancel(false);
            return;
        }

        for (int i = 0; i < allFilesLong; i++) {
            publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
            in = new FileInputStream(files[i]);

            if (eachFiles || firstStep) {
                String newFileName = "/" + (hideNames ? i + EXT : FileController.getNameFile(files[i].getName()) + EXT);
                File outputFile = new File(dstFolder.getAbsolutePath() + newFileName);
                outputFile.createNewFile();
                out = new FileOutputStream(outputFile);
                crypter.initOperation(Settings.MODE.CRYPT);
                actionBuffer = crypter.getCryptDigest();
                out.write(actionBuffer);//запись хэша
            }

            firstStep = false;
            headerInfo = getHeadersToCrypt(files[i]);
            buffer = headerInfo[0];
            actionBuffer = crypter.getOperationResult(buffer);
            out.write(actionBuffer);//запись длины заголовков
            buffer = headerInfo[1];
            actionBuffer = crypter.getOperationResult(buffer);
            out.write(actionBuffer);//запись заголовков нового формата

            long fileSize = files[i].length();

            if (speedAction) { // шифрование только первых байт
                int header_size = HEADER_SIZE_BYTES;

                if (fileSize < HEADER_SIZE_BYTES) {
                    header_size = (int)fileSize;
                }

                buffer = new byte[header_size];
                in.read(buffer);
                actionBuffer = crypter.getOperationResult(buffer);
                out.write(actionBuffer);
                currentSize += buffer.length;
                publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
            }

            if (fileSize > MAX_SIZE_BYTES) { //если файл слишком большой
                buffer = new byte[MAX_SIZE_BYTES];

                for (int j = 0; j < fileSize / MAX_SIZE_BYTES; j++) {
                    in.read(buffer);

                    if (speedAction) {
                        out.write(buffer);
                    } else {
                        actionBuffer = crypter.getOperationResult(buffer);
                        out.write(actionBuffer);
                    }

                    currentSize += buffer.length;
                    publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
                }

                fileSize = fileSize % MAX_SIZE_BYTES;
            }

            if (fileSize != 0) {
                buffer = new byte[(int)fileSize];
                in.read(buffer);

                if (speedAction) {
                    out.write(buffer);
                } else {
                    actionBuffer = crypter.getOperationResult(buffer);
                    out.write(actionBuffer);
                }

                currentSize += buffer.length;
                publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
            }

            //Завершение работы с файлом
            in.close();


            if (eachFiles) {
                crypter.endOperation();
                out.close();
            }

            if (deleteFiles) {
                files[i].delete();
            }

            if (isCancelled()) return;
        }

        crypter.endOperation();
        out.close();
    }

    public void decrypt() throws IOException {
        long allBytesLong = 0;
        long allFilesLong = files.length;
        long currentSize = 0;
        byte[] buffer;
        byte[] actionBuffer;
        File decryptedFile;
        File dstFolder = FileController.getNewActionFolder(Settings.getSettings(mainWindow.getActivity()).get("defaultFolder").toString(), "decrypt");
        FileUtils.forceMkdir(dstFolder);

        //подсчет места
        for (File a : files) {
            allBytesLong += a.length();
        }

        //если не хватает места
        if (allBytesLong > dstFolder.getUsableSpace()) {
            message = mainWindow.getResources().getString(R.string.need_space);
            cancel(false);
            return;
        }

        for (int i = 0; i < allFilesLong; i++) {
            publishProgress(currentSize, allBytesLong, (long) i, allFilesLong);//отображение статуса
            buffer = new byte[32];
            in = new FileInputStream(files[i]);
            in.read(buffer);
            crypter.initOperation(Settings.MODE.DECRYPT);
            currentSize += buffer.length;

            if (!crypter.testCryptDigest(buffer)) {//если имя или пароль не верны
                message = mainWindow.getResources().getString(R.string.wrong_user_data);
                cancel(false);
                return;
            }

            while (in.available() > 0) {
                ArrayList<Object> headers = getHeadersDecrypt();
                decryptedFile = new File(headers.get(0).toString());
                currentSize += decryptedFile.getAbsolutePath().getBytes().length;

                if (ownerPath) {//если использовать пути по умолчанию
                    while (decryptedFile.exists()){
                        decryptedFile = new File(FileController.getUniqueFileName(decryptedFile));//создать файл с похожим именем
                    }
                } else {
                    String name = decryptedFile.getName();
                    decryptedFile = new File(dstFolder.getAbsolutePath() + "/" + name);
                }

                if (!decryptedFile.createNewFile()) {
                    message = mainWindow.getResources().getString(R.string.error_cant_create_new_file) + decryptedFile.getParentFile().getAbsolutePath();
                    cancel(false);
                    return;
                }

                long fileSize = (long)headers.get(1);
                out = new FileOutputStream(decryptedFile);
                currentSize += 16;

                if (speedAction) { // шифрование только первых байт
                    int header_size = HEADER_SIZE_BYTES;

                    if (fileSize < HEADER_SIZE_BYTES) {
                        header_size = (int) fileSize;
                    }

                    buffer = new byte[header_size];
                    in.read(buffer);
                    actionBuffer = crypter.getOperationResult(buffer);
                    out.write(actionBuffer);
                    currentSize += buffer.length;
                    publishProgress(currentSize, allBytesLong, (long) i, allFilesLong);//отображение статуса
                }

                if (fileSize > MAX_SIZE_BYTES) { //если файл слишком большой
                    buffer = new byte[MAX_SIZE_BYTES];

                    for (int j = 0; j < fileSize / MAX_SIZE_BYTES; j++) {
                        in.read(buffer);

                        if (speedAction) {
                            out.write(buffer);
                        } else {
                            actionBuffer = crypter.getOperationResult(buffer);
                            out.write(actionBuffer);
                        }

                        currentSize += buffer.length;
                        publishProgress(currentSize, allBytesLong, (long) i, allFilesLong);//отображение статуса
                    }

                    fileSize = fileSize % MAX_SIZE_BYTES;
                }

                if (fileSize != 0) {
                    buffer = new byte[(int)fileSize];
                    in.read(buffer);

                    if (speedAction) {
                        out.write(buffer);
                    } else {
                        actionBuffer = crypter.getOperationResult(buffer);
                        out.write(actionBuffer);
                    }

                    currentSize += buffer.length;
                    publishProgress(currentSize, allBytesLong, (long) i, allFilesLong);//отображение статуса
                }

                //Завершение работы с файлом
                //
                out.close();

                if (isCancelled()) return;
            }

            in.close();
            out.write(crypter.endOperation());

            if (deleteFiles) {
                files[i].delete();
            }
        }
    }

    /**
     * Получить байты заголовков нового формата файла
     * @param file - файл
     * @return - массив байт заголловков и длины заголовков
     */
    private byte[][] getHeadersToCrypt(File file) {
        byte[] name = file.getAbsolutePath().getBytes();
        byte[] nameSize = FileController.LongToByteArray(name.length);
        byte[] fileSize = FileController.LongToByteArray(file.length());
        byte[] result = new byte[name.length + 16];
        System.arraycopy(nameSize, 0, result, 0, 8);
        System.arraycopy(name, 0, result, 8, name.length);
        System.arraycopy(fileSize, 0, result, 8 + name.length, fileSize.length);
        byte [][] ret = new byte[2][];
        ret[0] = FileController.LongToByteArray(result.length);
        ret[1] = result;
        return ret;

    }

    /**
     * Получить полное имя файла-оригинала
     * @return полное имя
     */
    @NonNull
    private ArrayList<Object> getHeadersDecrypt() throws IOException {
        byte[] headerSize = new byte[8];
        in.read(headerSize);
        long headerSizeLong = FileController.byteArrayToLong(crypter.getOperationResult(headerSize));
        byte[] headers = new byte[(int)headerSizeLong];
        in.read(headers);//crypted headers
        headers = crypter.getOperationResult(headers); //decrypted headers
        long nameSizeLong = FileController.byteArrayToLong(Arrays.copyOf(headers, 8));
        byte[] name = Arrays.copyOfRange(headers, 8,(int) nameSizeLong + 8);
        long fileSize = FileController.byteArrayToLong(Arrays.copyOfRange(headers, headers.length - 8, headers.length));
        ArrayList<Object> result = new ArrayList<>(2);
        result.add(0, new String(name));
        result.add(1, fileSize);
        return result;
    }

}
