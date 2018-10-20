package com.opss.movibus.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

public class LocationAddress {
    private static final String TAG = "LocationAddress";

    public static void getAddressFromLocation(final double lat, final double lon, final Context context, Handler handler) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;

                try {
                    List<Address> addressList = geocoder.getFromLocation(lat, lon, 1);

                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sd = new StringBuilder();

                        for (int i=0; i<address.getMaxAddressLineIndex(); i++) {
                            sd.append(address.getAddressLine(i)).append("\n");
                        }

                        sd.append(address.getLocality()).append("\n");
                        sd.append(address.getPostalCode()).append("\n");
                        sd.append(address.getCountryName());

                        result = sd.toString();
                    }

                } catch(IOException ex) {

                }
            }
        };

        thread.start();
    }

}
