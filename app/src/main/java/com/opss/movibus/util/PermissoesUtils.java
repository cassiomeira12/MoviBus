package com.opss.movibus.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by pedro on 02/10/17.
 */

public class PermissoesUtils {

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static int REQUEST_LOCATION = 1;
    public static int REQUEST_STORAGE= 2;
    public static int REQUST_CAMERA= 3;

    public static boolean verificarPermissao(Context context, int request, String permissao) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(context, permissao);
        if (permission == PackageManager.PERMISSION_GRANTED)
            return true;
        else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{permissao}, request);
        }

        return false;
    }

    public static boolean verificarPermissaoLocation(Activity activity, Context context) {
        // Check if we have write permission
        int permissionCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCoarse == PackageManager.PERMISSION_GRANTED && permissionFine == permissionFine) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            //ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_LOCATION);
        }

        return false;
    }

    public static boolean resquestLocationPermission(Context context) {
        boolean fineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return fineLocation && coarseLocation;
    }

    public static boolean verificarPermissaoArmazenamento(AppCompatActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_STORAGE
            );

            return false;
        }

        return true;
    }

}
