package com.cryptandroid.max.crypterandroid20;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Класс криптографических преобразований
 * Created by MAX on 10.06.2018.
 */

public class Crypter {
    private Cipher cipher;
    private IvParameterSpec ivParameterSpec;
    private SecretKeySpec secretKey;
    private byte [] namePassDigest;

    public Crypter(String name, String password, int algKeyLen) {
        name = name.isEmpty() ? "max" : name;
        password = password.isEmpty() ? "max" : password;

        try {
            cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
            MessageDigest bytesDigest = MessageDigest.getInstance("SHA-256");
            byte [] passDigest = bytesDigest.digest(password.getBytes());
            byte [] nameDigest = bytesDigest.digest(name.getBytes());
            namePassDigest = bytesDigest.digest((password+name).getBytes());
            secretKey = new SecretKeySpec(Arrays.copyOf(passDigest, algKeyLen/8), "AES");
            ivParameterSpec = new IvParameterSpec(Arrays.copyOf(nameDigest, cipher.getBlockSize()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Инициализация работы
     * @param mode - режим работы
     */
    public void initOperation(Settings.MODE mode) {
        try {
            if (mode == Settings.MODE.CRYPT) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            } else if (mode == Settings.MODE.DECRYPT) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            }

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить результат криптографического преобразования
     * @param in - входной массив байт
     * @return зашифрованный/расшифрованный массив байт
     */
    public byte[] getOperationResult(byte[] in) {
        return cipher.update(in);
    }

    /**
     * Завершить операцию
     */
    public byte[] endOperation() {
        try {
            return cipher.doFinal();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Вернуть зашифрованный массив хэш данных
     * @return cipher.update
     */
    public byte[] getCryptDigest() {
        if (namePassDigest != null) {
            return cipher.update(namePassDigest);
        }

        return null;
    }

    /**
     * Проверить зашифрованные хэш данные
     * @param digest - хэш
     * @return - true или false
     */
    public boolean testCryptDigest(byte[] digest) {
        return Arrays.equals(namePassDigest, cipher.update(digest));
    }

}
