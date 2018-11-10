package Objects;

public class Location {
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

    public int getId(){return id;}

    public void setDepot(boolean depot) { this.depot = depot;}
}
