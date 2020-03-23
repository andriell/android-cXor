package com.andriell.cxor.crypto;

public class CircularBytes {
    private byte[] bytes;
    private int i;

    public CircularBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte read() {
        if (i < 0 || i >= bytes.length) {
            i = 0;
        }
        return bytes[i++];
    }
}
