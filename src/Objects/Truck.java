package Objects;

import java.io.Serializable;
import java.util.ArrayList;
import static Heuristiek.Problem.*;

public class Truck  implements Serializable {
    private int id;
    private int currentLoad;
    private int currentDistance;
    private int currentWorkTime;
    private Location startLocation;
    private Location endLocation;
    private Location currentLocation;
    private ArrayList<Ride> route = new ArrayList<>();
    private ArrayList<Machine> loadedMachines = new ArrayList<>();

    public Truck(int id, Location startLocation, Location eindLocation) {
        this.id = id;
        this.currentLoad = 0;
        this.currentDistance = 0;
        this.currentWorkTime = 0;
        this.startLocation = startLocation;
        this.endLocation = eindLocation;
        this.currentLocation = startLocation;
        this.route.clear();
        this.loadedMachines.clear();
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

    public Location getEndLocation() {return endLocation;}
    public void setEndLocation(Location endLocation) {this.endLocation = endLocation;}

    public Location getCurrentLocation() {return currentLocation;}
    public void setCurrentLocation(Location currentLocation) {this.currentLocation = currentLocation;}

    public ArrayList<Ride> getRoute() {return route;}
    public void setRoute(ArrayList<Ride> route) {this.route = route;}

    public ArrayList<Machine> getLoadedMachines() {return loadedMachines;}
    public void setLoadedMachines(ArrayList<Machine> loadedMachines) {this.loadedMachines = loadedMachines;}

    public void addLoad(int load) {this.currentLoad += load;}
    public void lessLoad(int load) {this.currentLoad -= load;}

    /** Add customer to truck route **/
    public void addPointToRoute(Ride ride) {
        int distance = distanceMatrix[ride.getFromLocation().getId()][ride.getToLocation().getId()];
        int time = timeMatrix[ride.getFromLocation().getId()][ride.getToLocation().getId()];
        int load = (ride.getMachine() != null)? ride.getMachine().getMachineType().getVolume() : 0;
        int serviceTime = (ride.getMachine() != null)? ride.getMachine().getMachineType().getServiceTime() : 0;
        Request.Type type = ride.getType();

        this.route.add(ride);

        this.currentDistance += distance;
        this.currentLoad += load;
        this.currentLocation = ride.getToLocation();

        if(type == Request.Type.COLLECT || type == Request.Type.TEMPORARYCOLLECT )
            this.loadedMachines.add(ride.getMachine());
        else if(type == Request.Type.DROP )
            this.loadedMachines.remove(ride.getMachine());

        // Tijd om alles nog af te laden
        int tijdAfladen = 0;
        if(type == Request.Type.END){
            for(Machine machine : this.loadedMachines){
                tijdAfladen += machine.getMachineType().getServiceTime();
            }
        }

        this.currentWorkTime += (time + serviceTime + tijdAfladen);

        /** Update machine locations **/
        for(Machine machine : loadedMachines){
            machines.get(machine.getId()).setLocation(currentLocation);
        }
    }

    public boolean CheckIfLoadFits(int load) {
        return ((getCurrentLoad() + load <= TRUCK_CAPACITY));
    }

    public boolean CheckIfTimeFits(int rideTime, int serviceTime, Location fromLocation) {
        int homeTime = timeMatrix[fromLocation.getId()][this.endLocation.getId()];
        return ((getCurrentWorkTime() + serviceTime +  rideTime + serviceTime + homeTime <= TIME_CAPACITY));
    }
}