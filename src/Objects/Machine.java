package Objects;

public class Machine {
    private int id;
    private MachineType machineType;
    private Location location;

    public Machine(int id, MachineType machineType, Location location){
        this.id = id;
        this.machineType = machineType;
        this.location = location;
    }

    public int getId() {
        return id;
    }
    public MachineType getMachineType(){return machineType;}
    public Location getLocation(){return location;}
}