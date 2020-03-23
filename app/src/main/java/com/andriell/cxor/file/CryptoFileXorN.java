package com.andriell.cxor.file;

import com.andriell.cxor.crypto.CircularBytes;
import com.andriell.cxor.crypto.PasswordSequence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public abstract class CryptoFileXorN extends AbstractCryptoFile {
    abstract int getN();

    public void save(byte[] data) throws IOException {
        checkDataSize(data);
        BufferedOutputStream os = getBufferedOutputStream();
        PasswordSequence sequence = getPasswordSequence();
        int n = getN();
        if (n > 0) {
            CircularBytes cb = randomCircularBytes(n);
            for (int i = 0; i < n; i++) {
                os.write(sequence.read() ^ cb.read());
            }
            for (byte b : data) {
                os.write(sequence.read() ^ b ^ cb.read());
            }
        } else {
            for (byte b : data) {
                os.write(sequence.read() ^ b);
            }
        }

        os.flush();
        os.close();
    }

    public byte[] read() throws IOException {
        BufferedInputStream is = getBufferedInputStream();
        int fileSize = getFileSizeInt();

        PasswordSequence sequence = getPasswordSequence();
        int n = getN();
        byte[] r;
        if (n > 0) {
            byte[] bytes = new byte[n];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (sequence.read() ^ is.read());
            }
            CircularBytes cb = new CircularBytes(bytes);
            r = new byte[fileSize - n];
            for (int i = 0; i < r.length; i++) {
                r[i] = (byte) (sequence.read() ^ is.read() ^ cb.read());
            }
        } else {
            r = new byte[fileSize];
            for (int i = 0; i < r.length; i++) {
                r[i] = (byte) (sequence.read() ^ is.read());
            }
        }
        is.close();
        return r;
    }
}
