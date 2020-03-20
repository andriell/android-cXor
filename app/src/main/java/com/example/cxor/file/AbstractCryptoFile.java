package com.example.cxor.file;


import com.example.cxor.crypto.CircularBytes;
import com.example.cxor.crypto.Constants;
import com.example.cxor.crypto.PasswordSequence;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public abstract class AbstractCryptoFile implements CryptoFileInterface {
    private static Random random;
    private File file;
    private byte[] password;

    static {
        random = new Random();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) throws IOException {
        if (file == null) {
            throw new IOException("The file is not loaded");
        }
        if (file.length() > Constants.MAX_SIZE) {
            throw new IOException("The file is too large");
        }
        this.file = file;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    void checkDataSize(byte[] data)  throws IOException {
        if (data.length > Constants.MAX_SIZE) {
            throw new IOException("The data is too large");
        }
    }

    BufferedOutputStream getBufferedOutputStream() throws IOException {
        if (file == null) {
            throw new IOException("The file is not set");
        }
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    BufferedInputStream getBufferedInputStream() throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("The file is not set");
        }
        return new BufferedInputStream(new FileInputStream(file));
    }

    PasswordSequence getPasswordSequence() throws IOException {
        PasswordSequence sequence = null;
        try {
            sequence = new PasswordSequence(getPassword());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        return sequence;
    }

    int getFileSizeInt() throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("The file is not set");
        }
        return (int) file.length();
    }



    public static byte randomByte() {
        return (byte) (random.nextInt() & 0xFF);
    }

    public static byte[] randomBytes(int l) {
        byte[] r = new byte[l];
        random.nextBytes(r);
        return r;
    }

    public static CircularBytes randomCircularBytes(int l) {
        byte[] bytes = new byte[l];
        random.nextBytes(bytes);
        return new CircularBytes(bytes);
    }
}
