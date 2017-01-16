package com.example.admin.mybike;

import android.net.Uri;


public class VehicleHandler {

    private String _name, _distance, _oildate, _chaindate;
    private byte[] _imageURI;
    private int _id;



    public VehicleHandler(int id, String name, String distance, String date, String address, byte[] imageURI) {
        _id = id;
        _name = name;
        _distance = distance;
        _oildate = date;
        _chaindate = address;
        _imageURI = imageURI;
    }

    public int getId() { return _id; }

    public String getName() {
        return _name;
    }

    public String getDistance() {
        return _distance;
    }

    public String getDate() {
        return _oildate;
    }

    public String getAddress() {
        return _chaindate;
    }


    public byte[] getImageURI() {
        return _imageURI; }

    public void setimageURI(byte[] _imageURI) {
        this._imageURI = _imageURI;
    }

}
