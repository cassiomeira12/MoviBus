package com.opss.movibus.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReverseGeo extends AsyncTask<LatLng, Void, String> {

    private Context context;

    private OnTaskComplete mListener;

    public ReverseGeo(Context context, OnTaskComplete mListener) {
        this.context = context;
        this.mListener = mListener;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onTaskComplete(address);
        Log.i("CASSIO", address);
        super.onPostExecute(address);
    }

    @Override
    protected String doInBackground(LatLng... latLngs) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        LatLng location = latLngs[0];

        List<Address> addresses = null;
        String printAddress = "";

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException ex) {
            printAddress = "Sem Endereço";
        }

        if (addresses != null && addresses.size() > 0) {

            Address address = addresses.get(0);
            ArrayList<String> addressList = new ArrayList<>();

//            Log.i("CASSIO getAdminArea()", "" + address.getAdminArea());
//            Log.i("CASSIO getCountryCode()", "" + address.getCountryCode());
//            Log.i("CASSIO getCountryName()", "" + address.getCountryName());
//            Log.i("CASSIO getFeatureName()", "" + address.getFeatureName());
//            Log.i("CASSIO getLocality()", "" + address.getLocality());
//            Log.i("CASSIO getPostalCode()", "" + address.getPostalCode());
//            Log.i("CASSIO getPremises()", "" + address.getPremises());
//            Log.i("CASSIO getSubAdmArea()", "" + address.getSubAdminArea());
//            Log.i("CASSIO getSubLocality()", "" + address.getSubLocality());
//            Log.i("CASSIO getSubThorfare()", "" + address.getSubThoroughfare());
//            Log.i("CASSIO getThoroufare()", "" + address.getThoroughfare());

            String rua = address.getThoroughfare();
            String numero = address.getFeatureName();
            String bairro = address.getSubLocality();

            printAddress = rua + ", " + numero + " - " + bairro;

//            for (int i=0; i<=address.getMaxAddressLineIndex(); i++) {
//                addressList.add(address.getAddressLine(i));
//            }
//            printAddress = TextUtils.join(",", addressList);
        }

        return printAddress;
    }

    public interface OnTaskComplete {
        public void onTaskComplete(String result);
    }
}
