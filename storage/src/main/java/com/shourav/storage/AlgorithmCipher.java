package com.shourav.storage;


public enum AlgorithmCipher {

    AES("AES"),

    /**
     * The Digital Encryption Standard.
     */
    DES("DES"),


    DESede("DESede"),

    /**
     * The RSA encryption algorithm
     */
    RSA("RSA");

    private String mName;

    private AlgorithmCipher(String name) {
        mName = name;
    }


    public String getAlgorithmName() {
        return mName;
    }
}
