package Objects;

import java.io.Serializable;

public class Customer implements Serializable {
    private Location location;
    private Machine machine;
    private boolean visited;
    private Type type;

    public Customer(Machine machine, Location location, Type type){
        this.machine = machine;
        this.location = location;
        this.type = type;
        this.visited = false;
    }

    public Location getLocation() {return location;}
    public void setLocation(Location location) {this.location = location;}

    public Machine getMachine() {return machine;}
    public void setMachine(Machine machine) {this.machine = machine;}

    public boolean isVisited() {return visited;}
    public void setVisited(boolean visited) {this.visited = visited;}

    public Type getType() {return type;}
    public void setType(Type type) {this.type = type;}

    public enum Type {
        DROP, COLLECT
    }
}