package com.andriell.cxor.crypto;

import java.io.Serializable;

public class HiddenString implements Serializable {
    char[] data = null;
    char[] dataHidden = null;
    char[] dataCopy = null;
    int dataHiddenLength = 0;
    int dataCopyLength = 0;

    public HiddenString() {
    }

    public HiddenString(String s) {
        setData(s.toCharArray());
    }

    public HiddenString(char[] data) {
        setData(data);
    }

    public void setData(String s) {
        setData(s.toCharArray());
    }

    public void setData(char[] data) {
        this.data = data;
        this.dataHidden = new char[data.length];
        this.dataCopy = new char[data.length];
        dataHiddenLength = 0;
        dataCopyLength = 0;
        boolean isPass = false;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '"') {
                //<editor-fold desc="counting quotes">
                int quotes = 1;
                for (int j = i + 1; j < data.length; j++) {
                    if (data[j] != '"') {
                        break;

                    }
                    quotes++;
                }
                //</editor-fold>
                int end = i + quotes;
                if (quotes % 2 == 0) {
                    for (; i < end; i += 2) {
                        dataHidden[dataHiddenLength++] = isPass ? 'x' : '"';
                        dataCopy[dataCopyLength++] = data[i];
                    }
                } else {
                    i++;
                    end--;
                    for (; i < end; i += 2) {
                        dataHidden[dataHiddenLength++] = 'x';
                        dataCopy[dataCopyLength++] = data[i];
                    }
                    isPass = !isPass;
                }
            }
            if (i >= data.length) {
                break;
            }
            dataHidden[dataHiddenLength++] = isPass ? (data[i] == '\n' ? '\n' : 'x') : data[i];
            dataCopy[dataCopyLength++] = data[i];
        }
    }

    public void clear() {
        this.data = null;
        this.dataHidden = null;
    }

    public String copy(int start, int l) {
        if (start < 0 || l <= 0 || start > dataCopyLength - 1) {
            return "";
        }
        if (l > dataCopyLength) {
            l = dataCopyLength;
        }
        return new String(dataCopy, start, l);
    }

    public String getStringHidden() {
        if (dataHidden == null) {
            return "";
        }
        return new String(dataHidden, 0, dataHiddenLength);
    }

    public String getString() {
        if (data == null) {
            return "";
        }
        return new String(data);
    }
}
