package Objects;

import java.io.Serializable;

public class Ride implements Serializable {
    private Location fromLocation;
    private Location toLocation;
    private Location pickupLocation; //Type DROP
    private Machine machine;
    private Request.Type type;

    public Ride(Location fromLocation, Location toLocation, Location pickupLocation, Machine machine, Request.Type type)
    {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.pickupLocation = pickupLocation;
        this.machine = machine;
        this.type = type;
    }

    public Location getFromLocation() {return fromLocation;}
    public void setFromLocation(Location fromLocation) {this.fromLocation = fromLocation;}

    public Location getToLocation() {return toLocation;}
    public void setToLocation(Location toLocation) {this.toLocation = toLocation;}

    public Location getPickupLocation() {return pickupLocation;}
    public void setPickupLocation(Location pickupLocation) {this.pickupLocation = pickupLocation;}

    public Machine getMachine() {return machine;}
    public void setMachine(Machine machine) {this.machine = machine;}

    public Request.Type getType() {return type;}
    public void setType(Request.Type type) {this.type = type;}

}
