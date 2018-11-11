package Objects;

import java.io.Serializable;

public class Location implements Serializable {
    private int id;
    private double lat;
    private double lon;
    private String name;
    private boolean depot;

    public Location(int id, double lat, double lon, String name) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.depot = false;
    }

    public int getId() {return id;}
    public void settId(int id) {this.id = id;}

    public double getLat() {return lat;}
    public void setLat(double lat) {this.lat = lat;}

    public double getLon() {return lon;}
    public void setLon(double lon) {this.lon = lon;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public boolean isDepot() {return depot;}
    public void setDepot(boolean depot) { this.depot = depot;}
}
