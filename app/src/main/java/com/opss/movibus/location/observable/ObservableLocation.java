package com.opss.movibus.location.observable;

import android.location.Location;

import java.io.Serializable;
import java.util.Observable;

public class ObservableLocation extends Observable implements Serializable {

    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.setChanged();
        this.notifyObservers(location);
    }

}
