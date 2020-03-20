package com.example.cxor.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class CryptoFileText extends AbstractCryptoFile {
    private String[] extensions = {"txt"};

    public void save(byte[] data) throws IOException {
        checkDataSize(data);
        BufferedOutputStream os = getBufferedOutputStream();
        for (byte b : data) {
            os.write(b);
        }
        os.flush();
        os.close();
    }

    public byte[] read() throws IOException {
        BufferedInputStream is = getBufferedInputStream();
        int fileSize = getFileSizeInt();
        byte[] r;
        r = new byte[fileSize];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) is.read();
        }
        return r;
    }

    @Override
    public String getDescription() {
        return "Text";
    }

    @Override
    public String[] getExtensions() {
        return extensions;
    }
}
