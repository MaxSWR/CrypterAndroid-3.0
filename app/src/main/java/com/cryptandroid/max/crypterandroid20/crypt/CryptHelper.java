package com.cryptandroid.max.crypterandroid20.crypt;

import com.cryptandroid.max.crypterandroid20.engine.GOST3412_2015Engine;
import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.ThreefishEngine;
import org.spongycastle.crypto.modes.CFBBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class CryptHelper {
    private BufferedBlockCipher cipher;
    private int BIT_BLOCK_SIZE;
    private byte[] key;
    ParametersWithIV iv;

    public CryptHelper(ALGORITHMS alg) {
        BlockCipher engine;

        switch (alg) {
            case GOST:
                BIT_BLOCK_SIZE = 128;
                engine = new GOST3412_2015Engine();
                break;

            case AES:
                BIT_BLOCK_SIZE = 128;
                engine = new AESEngine();
                break;

            case THREEFISH:
                BIT_BLOCK_SIZE = 256;
                engine = new ThreefishEngine(BIT_BLOCK_SIZE);
                break;

            default:
                BIT_BLOCK_SIZE = 128;
                engine = new GOST3412_2015Engine();
        }

        cipher = new PaddedBufferedBlockCipher(new CFBBlockCipher(engine, BIT_BLOCK_SIZE));
    }

    public void setKey(String name, String password) {
        try {
            MessageDigest bytesDigest = MessageDigest.getInstance("SHA-256");
            key = bytesDigest.digest((name+password).getBytes());
            iv = new ParametersWithIV(null, bytesDigest.digest((password+name).getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void init(MODE mode) {
        boolean forEncryption = mode == MODE.ENCRYPT ? true : false;
        cipher.getUnderlyingCipher().init(forEncryption, iv);
        cipher.init(forEncryption, new KeyParameter(key));
    }

    public byte[] process(byte[] in) throws InvalidCipherTextException {
        byte[] buff = new byte[cipher.getOutputSize(in.length)];
        int outLen = cipher.processBytes(in, 0, in.length, buff, 0);
        int s = cipher.doFinal(buff, outLen);
        return Arrays.copyOfRange(buff, 0, outLen + s);
    }

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    public byte[] getCryptDigest() throws InvalidCipherTextException {
        byte[] b = new byte[60];
        new Random().nextBytes(b); // с 32 по 60 байт случайны

        for (int i = 0; i < 32; i++)
            b[i] = key[i] ;

        return process(b);
    }

    public boolean testCryptDigest(byte[] in) {
        byte[] b = new byte[0];

        try {
            b = process(in);
        } catch (InvalidCipherTextException e) {
            return false;
        }

        for (int i = 0; i < 32; i++) {
            if (b[i] != key[i])
                return false;
        }

        return true;
    }
}
