package com.example.myapplication;

import android.util.Log;
import android.widget.Toast;

public class Utils {
    public static void log(String msg) {
        Log.e("IPC", msg);
    }

    public static void toast(String msg) {
        Toast.makeText(BaseApp.context, msg, Toast.LENGTH_SHORT).show();
        log(msg);
    }
}
