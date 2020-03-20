package com.example.cxor.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.close();
        byte[] r = bos.toByteArray();
        bos.close();
        return r;
    }

    public static byte[] decompress(byte[] compressed, int l) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        byte[] r = new byte[l];
        int off = 0;
        while (true) {
            int read = gis.read(r, off, 1024);
            off += read;
            if (read <= 0) {
                break;
            }
        }
        return Arrays.copyOf(r, off + 1);
    }

}
