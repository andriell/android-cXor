package com.andriell.cxor.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CryptoFileInterface {
    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public void setInputStream(InputStream inputStream) throws IOException;

    public void setOutputStream(OutputStream outputStream) throws IOException;

    public byte[] getPassword();

    public void setPassword(byte[] password);

    public void save(byte[] data) throws IOException;

    public byte[] read() throws IOException;


    public String getDescription();
    public String[] getExtensions();
}
