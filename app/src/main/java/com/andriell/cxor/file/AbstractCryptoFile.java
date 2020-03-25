package com.andriell.cxor.file;


import com.andriell.cxor.crypto.CircularBytes;
import com.andriell.cxor.crypto.Constants;
import com.andriell.cxor.crypto.PasswordSequence;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public abstract class AbstractCryptoFile implements CryptoFileInterface {
    private static Random random;

    private InputStream inputStream;
    private OutputStream outputStream;

    private byte[] password;

    static {
        random = new Random();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
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
        if (outputStream == null) {
            throw new IOException("Output stream is not set");
        }
        return new BufferedOutputStream(outputStream);
    }

    BufferedInputStream getBufferedInputStream() throws IOException {
        if (inputStream == null) {
            throw new IOException("Input stream is not set");
        }
        return new BufferedInputStream(inputStream);
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
        if (inputStream == null) {
            throw new IOException("Input stream is not set");
        }
        return inputStream.available();
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

    @Override
    public String getMimeType() {
        return "application/x-binary";
    }
}
