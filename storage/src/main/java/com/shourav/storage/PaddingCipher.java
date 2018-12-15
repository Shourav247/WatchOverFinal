package com.shourav.storage;

public enum PaddingCipher {
    NoPadding("NoPadding"),
    PKCS5Padding("PKCS5Padding"),
    PKCS1Padding("PKCS1Padding"),
    OAEPWithSHA_1AndMGF1Padding("OAEPWithSHA-1AndMGF1Padding"),
    OAEPWithSHA_256AndMGF1Padding("OAEPWithSHA-256AndMGF1Padding");

    private String mName;

    private PaddingCipher(String name) {
        mName = name;
    }

    /**
     * Get the algorithm name of the enum value.
     *
     * @return The algorithm name
     */
    public String getAlgorithmName() {
        return mName;
    }
}
