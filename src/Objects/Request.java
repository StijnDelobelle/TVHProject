package Objects;

import java.io.Serializable;

public class Request implements Serializable {
    private Machine machine;
    private MachineType machineType;
    private Location location;
    private Type type;

    public Request(Machine machine, MachineType machineType, Location location, Type type) {
        this.machine = machine;
        this.machineType = machineType;
        this.location = location;
        this.type = type;
    }

    public Machine getMachine() {return machine;}
    public void setMachine(Machine machine) {this.machine = machine;}

    public MachineType getMachineType() {return machineType;}
    public void setMachineType(MachineType machineType) {this.machineType = machineType;}

    public Location getLocation() {return location;}
    public void setLocation(Location location) {this.location = location;}

    public Type getType() {return type;}
    public void setType(Type type) {this.type = type;}

    public enum Type {
        DROP, COLLECT, TEMPORARYCOLLECT, END
    }
}