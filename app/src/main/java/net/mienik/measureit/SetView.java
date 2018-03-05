package net.mienik.measureit;

import java.io.Serializable;

/**
 * Created by DawidM on 2017-08-22.
 */

class SetView implements Serializable {

    private boolean tmpDHT;
    private boolean tmpVer;
    private boolean humidity;
    private boolean latitude;
    private boolean longitude;
    private boolean lux;

    SetView() {
        longitude = false;
        latitude = false;
        humidity = false;
        tmpVer = false;
        tmpDHT = false;
        lux = false;
    }


    public void setTmpDHT(boolean tmpDHT) {
        this.tmpDHT = tmpDHT;
    }

    public boolean getTmpDHT() {
        return tmpDHT;
    }


    public void setTmpVer(boolean tmpVer) {
        this.tmpVer = tmpVer;
    }

    public boolean getTmpVer() {
        return tmpVer;
    }


    public void setHumidity(boolean humidity) {
        this.humidity = humidity;
    }

    public boolean getHumidity() {
        return humidity;
    }


    public void setLatitude(boolean latitude) {
        this.latitude = latitude;
    }

    public boolean getLatitude() {
        return latitude;
    }


    public void setLongitude(boolean longitude) {
        this.longitude = longitude;
    }

    public boolean getLongitude() {
        return longitude;
    }


    public void setLux(boolean lux) {
        this.lux = lux;
    }

    public boolean getLux() {
        return lux;
    }

    public void clearObject() {
        longitude = false;
        latitude = false;
        humidity = false;
        tmpVer = false;
        tmpDHT = false;
        lux = false;
    }
}
