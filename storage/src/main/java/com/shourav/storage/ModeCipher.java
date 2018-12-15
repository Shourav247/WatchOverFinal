package com.shourav.storage;

public enum ModeCipher {
    /**
     * Cipher Block Chaining Mode
     */
    CBC("CBC"),

    /**
     * Electronic Codebook Mode
     */
    ECB("ECB");

    private String mName;

    private ModeCipher(String name) {
        mName = name;
    }


    public String getAlgorithmName() {
        return mName;
    }
}
