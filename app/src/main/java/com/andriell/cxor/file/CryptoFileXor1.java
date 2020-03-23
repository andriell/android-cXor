package com.andriell.cxor.file;

public class CryptoFileXor1 extends CryptoFileXorN {
    private String[] extensions = {"xor"};

    @Override
    int getN() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "Xor";
    }

    @Override
    public String[] getExtensions() {
        return extensions;
    }
}
