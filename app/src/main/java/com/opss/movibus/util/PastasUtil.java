package com.opss.movibus.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by pedro on 29/08/17.
 */

public class PastasUtil {

    public static void criarPastas() {
        File file = new File(getPath());
        if (!file.exists())
            file.mkdir();
    }

    public static String getPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/MoviBus/";
    }
}
