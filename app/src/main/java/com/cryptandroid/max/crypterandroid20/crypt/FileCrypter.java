package com.cryptandroid.max.crypterandroid20.crypt;

import android.os.AsyncTask;
import com.cryptandroid.max.crypterandroid20.R;
import com.cryptandroid.max.crypterandroid20.controllers.FileController;
import com.cryptandroid.max.crypterandroid20.screens.MainWindow;
import com.cryptandroid.max.crypterandroid20.sets.Settings;
import org.apache.commons.io.FileUtils;
import org.spongycastle.crypto.InvalidCipherTextException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import static com.cryptandroid.max.crypterandroid20.sets.HardSettings.HEADER_SIZE_BYTES;
import static com.cryptandroid.max.crypterandroid20.sets.HardSettings.MAX_SIZE_BYTES;

public class FileCrypter extends AsyncTask<Void, Long, Void> {
    private CryptHelper crypter;
    private MainWindow mainWindow;
    private File[] files;
    private String message;
    private InputStream in = null;
    private OutputStream out = null;

    /** SETTINGS **/
    private ALGORITHMS algorithm; // GOST / AES / THREEFISH
    private boolean mode; // each / safe
    private boolean defaultm; // default name and password
    private String defname; // default name
    private String defpassword; // default password
    private boolean speed_mode; // speed mode
    private boolean hidepatch; // hide path
    private boolean ownerPath; // owner path
    private boolean deleteAfterAction; // delete after action
    private String defPath; // default path for files
    private MODE encmode;
    /** SETTINGS **/

    public FileCrypter(MainWindow mainWindow, String name, String password, MODE mode) {
        files = FileController.getFiles();
        this.mainWindow = mainWindow;
        getSettings();
        crypter = new CryptHelper(algorithm);
        encmode = mode;

        if (defaultm) {
            crypter.setKey(defname, defpassword);
        } else {
            crypter.setKey(name, password);
        }
    }

    private void getSettings() {
        HashMap<String, Object> sets = Settings.getSettings(mainWindow.getActivity());

        switch (Integer.parseInt(sets.get("algname").toString())) {
            case 1:
                algorithm = ALGORITHMS.GOST;
                break;

            case 2:
                algorithm = ALGORITHMS.AES;
                break;

            case 3:
                algorithm = ALGORITHMS.THREEFISH;
                break;

            default:
                algorithm = ALGORITHMS.GOST;
        }

        mode = (boolean)sets.get("modeEach");
        defaultm = (boolean)sets.get("defaultm");
        defname = sets.get("name").toString();
        defpassword = sets.get("password").toString();
        speed_mode = (boolean)sets.get("speed_mode");
        hidepatch = (boolean)sets.get("hidepath");
        ownerPath = (boolean)sets.get("ownerpath");
        deleteAfterAction = (boolean)sets.get("deleteAfterAction");
        defPath = sets.get("defaultFolder").toString();
    }

    @Override
    protected Void doInBackground(Void... voids) {
       Exception ex = null;

        try {
            switch (encmode) {
                case ENCRYPT:
                    crypt();
                    break;
                case DECRYPT:
                    decrypt();
                    break;
            }
        } catch (IOException e) {
            //e.printStackTrace();
            ex = e;
        } catch (InvalidCipherTextException e) {
            //e.printStackTrace();
            ex = e;
        } catch (Exception e) {
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

    public void crypt() throws Exception {
        long allBytesLong = 0;
        long currentSize = 0;
        long allFilesLong = files.length;
        boolean firstStep = true;
        byte[] buffer;
        byte[] actionBuffer;
        byte[] headerInfo;
        String EXT = mode ? ".caf" : ".cafs";
        File dstFolder = FileController.getNewActionFolder(defPath, "crypt");
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

            if (mode || firstStep) {
                String newFileName = "/" + (hidepatch ? i + EXT : FileController.getNameFile(files[i].getName()) + EXT);
                File outputFile = new File(dstFolder.getAbsolutePath() + newFileName);
                outputFile.createNewFile();
                out = new FileOutputStream(outputFile);
                crypter.init(encmode);
                actionBuffer = crypter.getCryptDigest(); // actionBuffer размером 64 байт
                out.write(actionBuffer);//запись хэша
            }

            firstStep = false;
            headerInfo = getHeadersToCrypt(files[i]); // 120 байт
            actionBuffer = crypter.process(headerInfo); // размер actionBuffer = 128 байт
            out.write(actionBuffer);//запись заголовков нового формата

            long fileSize = files[i].length();

            if (speed_mode) { // шифрование только первых байт
                int header_size = HEADER_SIZE_BYTES;

                if (fileSize < HEADER_SIZE_BYTES) {
                    header_size = (int)fileSize;
                }

                buffer = new byte[header_size];
                in.read(buffer);
                actionBuffer = crypter.process(buffer);
                out.write(actionBuffer);
                currentSize += header_size;
                publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
            }


            buffer = new byte[MAX_SIZE_BYTES];

            while (in.available() > MAX_SIZE_BYTES) {
                in.read(buffer);

                if (speed_mode)
                    out.write(buffer);
                else {
                    actionBuffer = crypter.process(buffer);
                    out.write(actionBuffer);
                }

                currentSize += buffer.length;
                publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
            }

            if (in.available() > 0) {
                buffer = new byte[in.available()];
                in.read(buffer);

                if (speed_mode)
                    out.write(buffer);
                else {
                    actionBuffer = crypter.process(buffer);
                    out.write(actionBuffer);
                }

                currentSize += buffer.length;
                publishProgress(currentSize, allBytesLong, (long)i, allFilesLong);//отображение статуса
            }

            //Завершение работы с файлом
            in.close();


            if (mode) {
                out.close();
            }

            if (deleteAfterAction) {
                files[i].delete();
            }

            if (isCancelled()) return;
        }

        out.close();
    }

    public void decrypt() throws IOException, InvalidCipherTextException {
        long allBytesLong = 0;
        long allFilesLong = files.length;
        long currentSize = 0;
        int BLOCK_SIZE = crypter.getBlockSize();
        byte[] buffer;
        byte[] actionBuffer;
        File decryptedFile;
        File dstFolder = FileController.getNewActionFolder(defPath, "decrypt");
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
            publishProgress(currentSize, allBytesLong, (long) i, allFilesLong); // отображение статуса
            buffer = new byte[64];
            in = new FileInputStream(files[i]);
            in.read(buffer);
            crypter.init(encmode);

            if (!crypter.testCryptDigest(buffer)) { //если имя или пароль не верны
                message = mainWindow.getResources().getString(R.string.wrong_user_data);
                cancel(false);
                return;
            }

            while (in.available() > 0) {
                HashMap<String, Object> headers = null;

                try {
                    headers = getHeadersDecrypt();
                } catch (InvalidCipherTextException e) {
                    message = mainWindow.getResources().getString(R.string.wrong_user_data);
                    cancel(false);
                    return;
                }

                decryptedFile = new File(headers.get("filename").toString());
                currentSize += 64 + 128;

                // выбор файла для сохранения
                if (ownerPath) { // если использовать пути по умолчанию
                    while (decryptedFile.exists()){ // генерировать уникальное имя
                        decryptedFile = new File(FileController.getUniqueFileName(decryptedFile));//создать файл с похожим именем
                    }
                } else {
                    String name = decryptedFile.getName();
                    decryptedFile = new File(dstFolder.getAbsolutePath() + "/" + name);
                }

                if (!decryptedFile.createNewFile()) { // не удалось создать файл
                    message = mainWindow.getResources().getString(R.string.error_cant_create_new_file) + decryptedFile.getParentFile().getAbsolutePath();
                    cancel(false);
                    return;
                }

                long fileSize = (long)headers.get("filesize");
                long currentFileSize = 0;
                out = new FileOutputStream(decryptedFile);

                if (speed_mode) { // дешифрование только первых байт
                    int header_size = HEADER_SIZE_BYTES + BLOCK_SIZE;

                    if (fileSize < HEADER_SIZE_BYTES) {
                        header_size = (int) ( (fileSize/BLOCK_SIZE + 1)*BLOCK_SIZE);
                    }

                    buffer = new byte[header_size];
                    in.read(buffer);
                    actionBuffer = crypter.process(buffer);
                    out.write(actionBuffer);
                    currentSize += header_size;
                    currentFileSize += actionBuffer.length;
                    publishProgress(currentSize, allBytesLong, (long) i, allFilesLong); // отображение статуса
                }

                buffer = speed_mode ? new byte[MAX_SIZE_BYTES] : new byte[MAX_SIZE_BYTES + BLOCK_SIZE];

                while (currentFileSize + MAX_SIZE_BYTES < fileSize) {
                    in.read(buffer);

                    if (speed_mode)
                        out.write(buffer);
                    else {
                        actionBuffer = crypter.process(buffer); // размер actionBuffer = MAX_SIZE_BYTES
                        out.write(actionBuffer);
                    }

                    currentFileSize += MAX_SIZE_BYTES;
                    currentSize += buffer.length;
                    publishProgress(currentSize, allBytesLong, (long) i, allFilesLong); // отображение статуса
                }

                if (currentFileSize != fileSize) {
                    buffer = speed_mode ? new byte[(int)(fileSize-currentFileSize)] :
                            new byte[(int)(((fileSize-currentFileSize)/BLOCK_SIZE+1)*BLOCK_SIZE)];

                    in.read(buffer);

                    if (speed_mode)
                        out.write(buffer);
                    else {
                        actionBuffer = crypter.process(buffer);
                        out.write(actionBuffer);
                    }

                    currentSize += buffer.length;
                    publishProgress(currentSize, allBytesLong, (long) i, allFilesLong); // отображение статуса
                }

                //Завершение работы с файлом
                out.close();

                if (isCancelled()) return;
            }

            in.close();

            if (deleteAfterAction) {
                files[i].delete();
            }
        }
    }

    /**
     * Получить байты заголовков нового формата файла
     * @param file - файл
     * @return - массив байт заголловков размером 120 байт
     */
    private byte[] getHeadersToCrypt(File file) throws Exception {
        byte[] name = file.getAbsolutePath().getBytes();
        byte[] nameSize = FileController.IntToByteArray(name.length);
        byte[] fileSize = FileController.LongToByteArray(file.length());
        int size = name.length + nameSize.length + fileSize.length;

        if (size > 120) {
            throw new Exception("bad headers of file");
        }

        byte[] result = new byte[120];
        new Random().nextBytes(result); // байты с (12 + name.length) по 120 случайные

        System.arraycopy(nameSize, 0, result, 0, 4);
        System.arraycopy(fileSize, 0, result, 4, 8);
        System.arraycopy(name,     0, result, 12, name.length);

        return result;
    }

    /**
     * Получить полное имя файла-оригинала
     * @return полное имя
     */
    private HashMap<String, Object> getHeadersDecrypt() throws IOException, InvalidCipherTextException {
        byte[] headerSize = new byte[128];
        byte[] headers;
        in.read(headerSize);
        HashMap<String, Object> ret = new HashMap<>();
        headers = crypter.process(headerSize);
        int nameSizeByte = FileController.byteArrayToInt(Arrays.copyOfRange(headers, 0, 4));
        long fileSizeByte = FileController.byteArrayToLong(Arrays.copyOfRange(headers, 4, 12));
        String fileName = new String(Arrays.copyOfRange(headers, 12, 12 + nameSizeByte));
        ret.put("filename", fileName);
        ret.put("filesize", fileSizeByte);
        return ret;
    }

}
