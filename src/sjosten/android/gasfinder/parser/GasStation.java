package sjosten.android.gasfinder.parser;

public class GasStation {
    private Coordinate coord;
    private String name;
    private String street;
    private String city;
    private long id;
    
    // This acts as the default constructor, a gas station must have both a coordinate
    // (i.e. location) and a name (e.g. Preem or Shell)
    public GasStation(Coordinate coord, String name) {
        this(coord, name, "", "");
    }
    
    public GasStation(Coordinate coord, String name, String street, String city) {
        this.coord = coord;
        this.name = name;
        this.street = street;
        this.city = city;
    }

    public String getName() {
    	return name;
    }
    
    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }
    
    public Coordinate getCoordinate() {
    	return coord;
    }
    
    public long getId() {
    	return id;
    }
    
    // This is simply in case it is used in the database, id is unique for each station
    public void setId(long id) {
    	this.id = id;
    }

    public String toString() {
        return coord.toString() + "\nName: " + name + "\n Street: " + street + "\nCity: " + city;
    }
}
