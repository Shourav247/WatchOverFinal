package com.shourav.storage;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Security {

    private static final String TAG = "Security";


    public static byte[] encrypt(byte[] content, int encryptionMode, final byte[] secretKey, final byte[] ivx) {
        if (secretKey.length != 16 || ivx.length != 16) {
            Log.w(TAG, "Set the encryption parameters correctly. The must be 16 length long each");
            return null;
        }

        try {
            SecretKey secretkey = new SecretKeySpec(secretKey, AlgorithmCipher.AES.getAlgorithmName());
            IvParameterSpec IV = new IvParameterSpec(ivx);
            String transformation = TransformationCipher.AES_CBC_PKCS5Padding;
            Cipher decipher = Cipher.getInstance(transformation);
            decipher.init(encryptionMode, secretkey, IV);
            return decipher.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to encrypt/descrypt - Unknown Algorithm", e);
            return null;
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "Failed to encrypt/descrypt- Unknown Padding", e);
            return null;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Failed to encrypt/descrypt - Invalid Key", e);
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Failed to encrypt/descrypt - Invalid Algorithm Parameter", e);
            return null;
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "Failed to encrypt/descrypt", e);
            return null;
        } catch (BadPaddingException e) {
            Log.e(TAG, "Failed to encrypt/descrypt", e);
            return null;
        }
    }

    public String xor(String msg, String key) {
        try {
            final String UTF_8 = "UTF-8";
            byte[] msgArray;

            msgArray = msg.getBytes(UTF_8);

            byte[] keyArray = key.getBytes(UTF_8);

            byte[] out = new byte[msgArray.length];
            for (int i = 0; i < msgArray.length; i++) {
                out[i] = (byte) (msgArray[i] ^ keyArray[i % keyArray.length]);
            }
            return new String(out, UTF_8);
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

}
