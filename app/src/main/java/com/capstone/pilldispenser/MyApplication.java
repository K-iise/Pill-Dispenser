package com.capstone.pilldispenser;
import android.app.Application;

import java.io.OutputStream;

public class MyApplication extends Application {
    private OutputStream outputStream;

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}