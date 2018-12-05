package Objects;

import java.util.ArrayList;
import static Heuristiek.Problem.*;
import java.io.Serializable;

public class Truck  implements Serializable {
    private int id;
    private int currentLoad;
    private int currentDistance;
    private int currentWorkTime;
    private Location startLocation;
    private Location endLocation;
    private Location currentLocation;
    private ArrayList<Stop> stops = new ArrayList<>();
    private int serviceTime;
    private int tijdLaden;

    //Enkel gebruikt voor initiele oplossing!!
    private ArrayList<Machine> loadedMachines = new ArrayList<>();

    public Truck(int id, Location startLocation, Location eindLocation) {
        this.id = id;
        this.currentLoad = 0;
        this.currentDistance = 0;
        this.currentWorkTime = 0;
        this.startLocation = startLocation;
        this.endLocation = eindLocation;
        this.currentLocation = startLocation;
        this.loadedMachines.clear();
        this.tijdLaden=0;
    }

    // For clone
    public Truck(int id, Location startLocation, Location eindLocation, ArrayList<Stop> stops, int tijdLaden) {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = eindLocation;
        this.stops = stops;
        this.tijdLaden = tijdLaden;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getCurrentLoad() {return currentLoad;}
    public void setCurrentLoad(int currentLoad) {this.currentLoad = currentLoad;}

    public int getCurrentDistance() {return currentDistance;}
    public void setCurrentDistance(int currentDistance) {this.currentDistance = currentDistance;}

    public int getCurrentWorkTime() {return currentWorkTime;}
    public void setCurrentWorkTime(int currentWorkTime) {this.currentWorkTime = currentWorkTime;}

    public Location getStartLocation() {return startLocation;}
    public void setStartLocation(Location startLocation) {this.startLocation = startLocation;}

    public int getTijdLaden() {return tijdLaden;}

    public Location getEndLocation() {return endLocation;}
    public void setEndLocation(Location endLocation) {this.endLocation = endLocation;}

    public Location getCurrentLocation() {return currentLocation;}
    public void setCurrentLocation(Location currentLocation) {this.currentLocation = currentLocation;}

    public ArrayList<Stop> getStops() {return stops;}

    public ArrayList<Machine> getLoadedMachines() {return loadedMachines;}
    public void setLoadedMachines(ArrayList<Machine> loadedMachines) {this.loadedMachines = loadedMachines;}
    public void AddLoadedMachines(Machine machine) {this.loadedMachines.add(machine);}

    public void addTijdLaden(int tijd) {this.tijdLaden += tijd;}
    public void lessTijdLaden(int tijd) {this.tijdLaden -= tijd;}

    public void addLoad(int load) {this.currentLoad += load;}
    public void lessLoad(int load) {this.currentLoad -= load;}

    public void addStopToRoute(Stop stop) {

        int load = 0;

        this.stops.add(stop);

        for(Machine m : stop.getcollect())
        {
            this.loadedMachines.add(m);
            tijdLaden += m.getMachineType().getServiceTime();
            load += m.getMachineType().getVolume();
        }

        for(Machine m : stop.getdrop())
        {
            if(stop.depo == false)
            {
                this.loadedMachines.remove(m);
            }
            tijdLaden += m.getMachineType().getServiceTime();
            load -= m.getMachineType().getVolume();
        }

        this.currentLoad += load;
        //this.currentDistance = distance;
        //this.currentWorkTime += (time + tijdLaden);

        /** Update machine locations **/
        for(Machine machine : loadedMachines){
            machines.get(machine.getId()).setLocation(currentLocation);
        }
    }

    public void addStopToRoute(int index, Stop stop) {

        this.stops.add(index,stop);

    }

    public void removeStop(int index)
    {
        this.stops.remove(index);
    }

    public boolean CheckIfTimeFitsStop(int time, int serviceTime) {
        return ((time + serviceTime  + serviceTime <= TIME_CAPACITY));
    }

    public boolean CheckIfTimeFitsStop() {
        return currentWorkTime <= TIME_CAPACITY;
    }

    public void DeleteStop(int index)
    {
        this.stops.remove(index);
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = this.serviceTime + serviceTime;
    }

    public int getServiceTime()
    {
        return serviceTime;
    }
}