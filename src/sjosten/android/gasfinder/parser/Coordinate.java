package sjosten.android.gasfinder.parser;

public class Coordinate {
    private double longitude;
    private double latitude;

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String toString() {
        return "Latitude: " + latitude + "\nLongitude: " + longitude;
    }
}
