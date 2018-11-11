package Objects;

import java.io.Serializable;

public class MachineType  implements Serializable {
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
    public void settId(int id) {this.id = id;}

    public int getVolume() {return  volume;}
    public void setVolume(int volume) {this.volume = volume;}

    public int getServiceTime() {return serviceTime;}
    public void setServiceTime(int serviceTime) {this.serviceTime = serviceTime;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
}
