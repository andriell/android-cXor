package com.andriell.cxor.file;

import com.andriell.cxor.crypto.Constants;
import com.andriell.cxor.crypto.Gzip;

import java.io.IOException;

public class CryptoFileXor2Zipped extends CryptoFileXor2 {
    private String[] extensions = {"xor2z"};

    public void save(byte[] data) throws IOException {
        checkDataSize(data);
        data = Gzip.compress(data);
        super.save(data);
    }

    public byte[] read() throws IOException {
        byte[] r = super.read();
        try {
            r = Gzip.decompress(r, Constants.MAX_SIZE);
        } catch (IOException e) {
            throw new IOException("Incorrect password", e);
        }
        return r;
    }

    @Override
    public String getDescription() {
        return "Xor2 zipped";
    }

    @Override
    public String[] getExtensions() {
        return extensions;
    }
}
