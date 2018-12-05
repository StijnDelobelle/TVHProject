package Objects;

import java.io.Serializable;

public class Request implements Serializable {
    private int id;
    private Machine machine;
    private MachineType machineType;
    private Location location;
    private Type type;
    private boolean isDone;
    private int inTruckId;

    public Request(int id, Machine machine, MachineType machineType, Location location, Type type) {
        this.id = id;
        this.machine = machine;
        this.machineType = machineType;
        this.location = location;
        this.type = type;
        this.isDone = false;
    }

    // For clone
    public Request(Request request) {
        this.id = request.id;
        this.machine = request.machine;
        this.machineType = request.machineType;
        this.location = request.location;
        this.type = request.type;
        this.isDone = request.isDone;
        this.inTruckId = request.inTruckId;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public void setInTruckId(int id)
    {
        this.inTruckId = id;
    }

    public int getInTruckId()
    {
        return inTruckId;
    }

    public Machine getMachine() {return machine;}
    public void setMachine(Machine machine) {this.machine = machine;}

    public MachineType getMachineType() {return machineType;}
    public void setMachineType(MachineType machineType) {this.machineType = machineType;}

    public Location getLocation() {return location;}
    public void setLocation(Location location) {this.location = location;}

    public Type getType() {return type;}
    public void setType(Type type) {this.type = type;}

    public boolean isDone() {return isDone;}
    public void setDone(boolean isDone) {this.isDone = isDone;}

    public enum Type {
        DROP, COLLECT, TEMPORARYCOLLECT, END,NOTHING,START
    }
}