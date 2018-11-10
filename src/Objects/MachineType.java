package Objects;

import java.util.Objects;

public class MachineType {
    private int id;
    private int volume;
    private int serviceTime;
    private String name;

    public MachineType(int id, int volume, int serviceTime, String name){
        this.id = id;
        this.volume = volume;
        this.serviceTime = serviceTime;
        this.name = name;
    }

    public int getId() {return id;}
    public int getVolume() {return  volume;}
    public int getServiceTime() {return serviceTime;}
    public String getName() {return name;}
}
