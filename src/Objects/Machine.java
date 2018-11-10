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

    public int getId() {return id;}
    public void settId(int id) {this.id = id;}

    public MachineType getMachineType() {return machineType;}
    public void setMachineType(MachineType machineType) {this.machineType = machineType;}

    public Location getLocation() {return location;}
    public void setLocation(Location location) {this.location = location;}
}