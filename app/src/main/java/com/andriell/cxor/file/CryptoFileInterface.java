package com.andriell.cxor.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CryptoFileInterface {
    InputStream getInputStream();

    OutputStream getOutputStream();

    void setInputStream(InputStream inputStream);

    void setOutputStream(OutputStream outputStream);

    byte[] getPassword();

    void setPassword(byte[] password);

    void save(byte[] data) throws IOException;

    byte[] read() throws IOException;

    String getDescription();

    String getMimeType();

    String[] getExtensions();
}
