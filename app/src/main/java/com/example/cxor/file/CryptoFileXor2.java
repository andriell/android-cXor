package com.example.cxor.file;

public class CryptoFileXor2 extends CryptoFileXorN {
    private String[] extensions = {"bin", "xor2"};

    @Override
    int getN() {
        return 8;
    }

    @Override
    public String getDescription() {
        return "Xor2";
    }

    @Override
    public String[] getExtensions() {
        return extensions;
    }
}
