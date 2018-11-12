package Objects;

public class Ride {
    private Location fromLocation;
    private Location toLocation;
    private Customer fromCustomer;
    private Customer toCustomer;

    public Ride(Location fromLocation, Location toLocation, Customer fromCustomer, Customer toCustomer)
    {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.fromCustomer = fromCustomer;
        this.toCustomer = toCustomer;
    }

    public Location getFromLocation() {return fromLocation;}
    public void setFromLocation(Location fromLocation) {this.fromLocation = fromLocation;}

    public Location getToLocation() {return toLocation;}
    public void setToLocation(Location toLocation) {this.toLocation = toLocation;}

    public Customer getFromCustomer() {return fromCustomer;}
    public void setFromCustomer(Customer fromCustomer) {this.fromCustomer = fromCustomer;}

    public Customer getToCustomer() {return toCustomer;}
    public void setToCustomer(Customer toCustomer) {this.toCustomer = toCustomer;}
}
