package sjosten.android.gasfinder.parser;

public class GasStation {
    private Coordinate coord;
    private String street;
    private String city;

    public GasStation(Coordinate coord, String street, String city) {
        this.coord = coord;
        this.street = street;
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String toString() {
        return coord.toString() + "\n Street: " + street + "\nCity: " + city;
    }
}
