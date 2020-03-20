package com.example.cxor.file;


import com.example.cxor.crypto.Constants;
import com.example.cxor.crypto.Gzip;

import java.io.IOException;

public class CryptoFileXor1Zipped extends CryptoFileXor1 {
    private String[] extensions = {"xorz"};

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
        return "Xor zipped";
    }

    @Override
    public String[] getExtensions() {
        return extensions;
    }
}
