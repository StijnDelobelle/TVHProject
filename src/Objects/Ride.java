package Objects;

public class Ride {
    private boolean rided;
    private Location fromLocation;
    private Location toLocation;
    private Location pickupLocation; //Bij drops tussenstop maken
    private Machine machine;

    public Ride(Location fromLocation, Location toLocation, Location pickupLocation, Machine machine)
    {
        this.rided = false;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.pickupLocation = pickupLocation;
        this.machine = machine;
    }

    public boolean getRided() {return rided;}
    public void setRided(boolean rided) {this.rided = rided;}

    public Location getFromLocation() {return fromLocation;}
    public void setFromLocation(Location fromLocation) {this.fromLocation = fromLocation;}

    public Location getToLocation() {return toLocation;}
    public void setToLocation(Location toLocation) {this.toLocation = toLocation;}

    public Location getPickupLocation() {return pickupLocation;}
    public void setPickupLocation(Location pickupLocation) {this.pickupLocation = pickupLocation;}

    public Machine getMachine() {return machine;}
    public void setMachine(Machine machine) {this.machine = machine;}

}
