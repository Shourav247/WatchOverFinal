package com.example.shourav.watchover.utils;

import android.accounts.AuthenticatorException;
import android.annotation.TargetApi;
import android.app.AuthenticationRequiredException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.shourav.watchover.FileProviderHelper;
import com.example.shourav.watchover.WatchOver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pvryan.easycrypt.asymmetric.ECAsymmetric;
import com.pvryan.easycrypt.symmetric.ECSymmetric;
import com.shourav.storage.DoEncrypt;
import com.shourav.storage.ProvideFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.RandomStringGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import io.reactivex.Completable;
import io.reactivex.Single;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

public class PreferenceGetter {
    private static final String TAG = "PreferenceGetter";

    public static final String WATCHOVER_EXTENSION = "watched";
    public static final String MATCHING_TEXT = "Match_Me_Like_Cinderella";
    private static final String PREF_IS_PASSWORD_SET = "password_enabled";
    private static final String PREF_PUBLIC_KEY = "public_key";
    private static final String TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding";
    private static final String PREF_ENCRYPTED_MATCHING_TEXT = "encrypted_matching_text";

    private static final String PREF_ENCRYPTED_PASS_MAP = "encrypted_pass_map";
    private static final String PREF_ENCRYPTED_EXTENSION_MAP = "encrypted_extension_map";
    public static final String SALT = "53xY";
    public static final String IVX = "abcdefghijklmnop";
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String KEY_SPEC_ALGORITHM = "AES";
    public static final String PROVIDER = "BC";
    public static final int OUTPUT_KEY_LENGTH = 256;



    public static void setIsPasswordEnabled(boolean isPasswordEnabled) {
        PreferenceHelper.set(PREF_IS_PASSWORD_SET, isPasswordEnabled);
    }

    public static boolean isPasswordEnabled() {
        return PreferenceHelper.getBoolean(PREF_IS_PASSWORD_SET);
    }

    private static void encryptMatchingText(PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NullPointerException {
        String encrypted = encryptString(MATCHING_TEXT, publicKey);
        if (encrypted == null) {
            throw new NullPointerException("Encrypted text can't be null");
        }
        PreferenceHelper.set(PREF_ENCRYPTED_MATCHING_TEXT, encrypted);
    }

    public static Single<String> decryptStringAsync(String string, PrivateKey privateKey) {
        return Single.fromCallable(() -> decryptString(string, privateKey));
    }

    public static Single<String> encryptStringAsync(String string, PublicKey publicKey) {
        return Single.fromCallable(() -> encryptString(string, publicKey));
    }

    private static String decryptString(String string, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] bytes = Base64.decode(string, Base64.DEFAULT);
        byte[] finalBytes = cipher.doFinal(bytes);
        return new String(finalBytes);
    }

    private static String encryptString(String string, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(string.getBytes());
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private static void savePublicKey(PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = fact.getKeySpec(publicKey,
                X509EncodedKeySpec.class);
        String string = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
        PreferenceHelper.set(PREF_PUBLIC_KEY, string);
    }

    public static PublicKey getPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] data = Base64.decode(PreferenceHelper.getString(PREF_PUBLIC_KEY), Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(spec);
    }

    public static String getEncryptedMatchingText() {
        return PreferenceHelper.getString(PREF_ENCRYPTED_MATCHING_TEXT);
    }

    public static Completable startKeyGeneratorAsync(String password) {
        return Completable.fromAction(() -> startKeyGenerator(password));
    }

    private static void startKeyGenerator(String password) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException {
        KeyPair keyPair = generateKeys(password);
        PublicKey publicKey = keyPair.getPublic();
        PreferenceGetter.encryptMatchingText(publicKey);
        PreferenceGetter.savePublicKey(publicKey);
    }

    private static KeyPair generateKeys(String password) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initGeneratorWithKeyGenParameterSpec(keyGen, password);
        } else {
            initGeneratorWithKeyPairGeneratorSpec(keyGen, password);
        }

        return keyGen.generateKeyPair();
    }


    private static void initGeneratorWithKeyPairGeneratorSpec(KeyPairGenerator keyGen, String password) throws InvalidAlgorithmParameterException {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 20);
        KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(WatchOver.getInstance())
                .setAlias(password)
                .setSerialNumber(BigInteger.ONE)
                .setSubject(new X500Principal("CN=${password} CA Certificate"))
                .setStartDate(startDate.getTime())
                .setEndDate(endDate.getTime());
        keyGen.initialize(builder.build());

    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void initGeneratorWithKeyGenParameterSpec(KeyPairGenerator keyGen, String password) throws InvalidAlgorithmParameterException {

        KeyGenParameterSpec builder = new KeyGenParameterSpec.Builder(password, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();
        keyGen.initialize(builder);
    }

    public static Single<KeyPair> getKeyPairAsync(String password) {
        return Single.fromCallable(() -> getKeyPair(password));
    }

    private static KeyPair getKeyPair(String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(password, null);
        PublicKey publicKey = keyStore.getCertificate(password).getPublicKey();
        Log.e(TAG, "getKeyPair: "+ password);
        return new KeyPair(publicKey, privateKey);
    }

    public static Single<Boolean> authenticateUser(String password) {
        return Single.fromCallable(() -> authUser(password));
    }

    private static boolean authUser(String pass) {
        String match = getKeyPairAsync(pass)
                .flatMap(keyPair -> PreferenceGetter.decryptStringAsync(PreferenceGetter.getEncryptedMatchingText(), keyPair.getPrivate()))
                .blockingGet();
        return MATCHING_TEXT.equals(match);
    }

    public static String encryptFile(String path) throws Exception {
        SecretKey secretKey = generateSecretKey();
        byte[] encodedBits = encode(secretKey, FileUtils.readFileToByteArray(new File(path)));
        FileUtils.writeByteArrayToFile(new File(watchify(path)), encodedBits);
        Log.e(TAG, "encryptFile: Secret key"+getSecretKeyString(secretKey));
        return getSecretKeyString(secretKey);
    }

    public static void decryptFile(String path, String password, String outPath) throws Exception {
        SecretKey secretKey = getSecretKey(password);
        byte[] decodedBits = decode(secretKey, FileUtils.readFileToByteArray(new File(path)));
        FileUtils.writeByteArrayToFile(new File(outPath), decodedBits);
    }


    public static String watchify(String path) {
        if (path == null) {
            return null;
        }
        File mydir = new File(Environment.getExternalStorageDirectory() + "/.WatchOver/");
        if(!mydir.exists()) {
            mydir.mkdirs();
        } else {
            Log.d("error", mydir.getAbsolutePath()+" already exists");
        }

        path = path.concat("."+WATCHOVER_EXTENSION);
        Uri uri = Uri.fromFile(new File(path));
        String fileName = uri.getLastPathSegment();
        return Environment.getExternalStorageDirectory() + "/.WatchOver/"+fileName;
//        return path;
    }

    public static String deWatchify(String path) {
        if (path == null) {
            return null;
        }

        return path.replace("."+WATCHOVER_EXTENSION, "");
    }

    public static void findAndDecryptFile(boolean isAuthenticated, String path, String userPassword) throws Exception {
        if (!isAuthenticated) {
            throw new AuthenticatorException("Wrong password");
        }
        String md5 = calculateMD5(new File(path));
        String password = getPasswordMap().get(md5);
        if (password == null) {
            throw new IllegalAccessException("File password or extension not found");
        }
        KeyPair keyPair = getKeyPair(userPassword);
        String decryptedPassword = decryptString(password, keyPair.getPrivate());
        Log.e(TAG, "findAndDecryptFile: Password"+password );
        Log.e(TAG, "findAndDecryptFile: Decrypted key: "+decryptedPassword );
        String outPath = deWatchify(path);
        decryptFile(path, decryptedPassword, outPath);
    }


    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            Log.i(TAG, "calculateMD5: Calculated digest for +"
                    +Uri.fromFile(updateFile).getLastPathSegment()+" is :"+output);
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }

    }

    public static byte[] encode(SecretKey yourKey, byte[] fileData) throws Exception {
        byte[] data = yourKey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length, KEY_SPEC_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher.doFinal(fileData);
    }

    public static byte[] decode(SecretKey yourKey, byte[] fileData) throws Exception {
        byte[] decrypted;
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, yourKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        decrypted = cipher.doFinal(fileData);
        return decrypted;
    }

    public static void storeNewEncryptedPassword(String encryptedPassword, String md5) {
        Map<String, String> oldMap = getPasswordMap();
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        oldMap.put(md5, encryptedPassword);
        Gson gson = new Gson();
        PreferenceHelper.set(PREF_ENCRYPTED_PASS_MAP, gson.toJson(oldMap));
    }

    public static String getSecretKeyString(SecretKey secretKey) {
        return Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
    }

    public static SecretKey generateSecretKey(){
        return getSecretKey(null);
    }

    public static SecretKey getSecretKey(String password) {
        if (password == null || password.isEmpty()) {
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator keyGenerator = null;
            try {
                keyGenerator = KeyGenerator.getInstance(KEY_SPEC_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            keyGenerator.init(OUTPUT_KEY_LENGTH, secureRandom);
            return keyGenerator.generateKey();
        }
        byte[] decodedKey = Base64.decode(password, Base64.NO_WRAP);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_SPEC_ALGORITHM);
    }

    public static Map<String, String> getPasswordMap() {
        Gson gson = new Gson();
        String json = PreferenceHelper.getString(PREF_ENCRYPTED_PASS_MAP);
        return gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
    }

}
