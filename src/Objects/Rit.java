package Objects;

public class Rit {
    private Location fromLocation;
    private Location toLocation;
    private Machine machine;
    private Request.Type type;

    public Rit(Location fromLocation, Location toLocation, Machine machine, Request.Type type)
    {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.machine = machine;
        this.type = type;
    }

    public Location getFromLocation() {return fromLocation;}
    public void setFromLocation(Location fromLocation) {this.fromLocation = fromLocation;}

    public Location getToLocation() {return toLocation;}
    public void setToLocation(Location toLocation) {this.toLocation = toLocation;}

    public Machine getMachine() {return machine;}
    public void setMachine(Machine machine) {this.machine = machine;}

    public Request.Type getType() {return type;}
    public void setType(Request.Type type) {this.type = type;}
}
