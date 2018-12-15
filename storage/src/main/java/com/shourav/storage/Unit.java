package com.shourav.storage;

import java.text.DecimalFormat;

public enum Unit {
    B(1),
    KB(Unit.BYTES),
    MB(Unit.BYTES * Unit.BYTES),
    GB(Unit.BYTES * Unit.BYTES * Unit.BYTES),
    TB(Unit.BYTES * Unit.BYTES * Unit.BYTES * Unit.BYTES);

    private long inBytes;
    private static final int BYTES = 1024;

    private Unit(long bytes) {
        this.inBytes = bytes;
    }

    public long inBytes() {
        return inBytes;
    }

    public static String readableSizeUnit(long bytes) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (bytes < KB.inBytes()) {
            return df.format(bytes / (float) B.inBytes()) + " B";
        } else if (bytes < MB.inBytes()) {
            return df.format(bytes / (float) KB.inBytes()) + " KB";
        } else if (bytes < GB.inBytes()) {
            return df.format(bytes / (float) MB.inBytes()) + " MB";
        } else {
            return df.format(bytes / (float) GB.inBytes()) + " GB";
        }
    }
}
