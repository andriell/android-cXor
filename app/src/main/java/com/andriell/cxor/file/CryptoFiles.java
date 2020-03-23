package com.andriell.cxor.file;


import java.io.File;

public class CryptoFiles {
    private static CryptoFiles instance = null;
    private static int default_index = 3;

    public static CryptoFiles getInstance() {
        if (instance == null) {
            instance = new CryptoFiles();
        }
        return instance;
    }

    private CryptoFileInterface[] cryptoFiles;

    private CryptoFiles() {
        cryptoFiles = new CryptoFileInterface[5];
        cryptoFiles[0] = new CryptoFileText();
        cryptoFiles[1] = new CryptoFileXor1();
        cryptoFiles[2] = new CryptoFileXor1Zipped();
        cryptoFiles[3] = new CryptoFileXor2();
        cryptoFiles[4] = new CryptoFileXor2Zipped();
    }

    public String[] getDescriptions()
    {
        String[] r = new String[cryptoFiles.length];
        for (int i = 0; i < cryptoFiles.length; i++) {
            r[i] = cryptoFiles[i].getDescription();
        }
        return r;
    }

    public File renameFile(File file, CryptoFileInterface cryptoFile) {
        int dotIndex = file.getAbsolutePath().lastIndexOf('.');
        String extension = file.getAbsolutePath().substring(dotIndex + 1);
        String[] extensions = cryptoFile.getExtensions();
        for (String e : extensions) {
            if (e.equals(extension)) {
                return file;
            }
        }
        return new File(file.getAbsolutePath().substring(0, dotIndex + 1) + extensions[0]);
    }

    /**
     * Return index crypto file to open a file by description or extension of file format
     * @param s String
     * @return int
     */
    public int getCryptoFileIndex(String s) {
        if (s == null) {
            return default_index;
        }
        for (int i = 0; i < cryptoFiles.length; i++) {
            if (s.equals(cryptoFiles[i].getDescription())) {
                return i;
            }
            String[] extensions = cryptoFiles[i].getExtensions();
            for (String extension: extensions) {
                if (s.equals(extension)) {
                    return i;
                }
            }
        }
        return default_index;
    }

    public CryptoFileInterface getCryptoFile(int i) {
        if (i < 0 || i >= cryptoFiles.length) {
            return null;
        }
        return cryptoFiles[i];
    }

}
