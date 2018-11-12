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
    private ArrayList<Customer> route = new ArrayList<>();

    public Truck(int id, Location startLocation, Location eindLocation) {
        this.id = id;
        this.currentLoad = 0;
        this.currentDistance = 0;
        this.currentWorkTime = 0;
        this.startLocation = startLocation;
        this.endLocation = eindLocation;
        this.currentLocation = startLocation;
        this.route.clear();
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

    public ArrayList<Customer> getRoute() {return route;}
    public void setRoute(ArrayList<Customer> route) {this.route = route;}

    public void addLoad(int load) {this.currentLoad += load;}
    public void lessLoad(int load) {this.currentLoad -= load;}

    /** Add customer to truck route **/
    public void addPointToRoute(Customer Customer, int time , int distance, int load) {
        route.add(Customer);
        this.currentLoad += load;
        this.currentLocation = Customer.getLocation();
        this.currentWorkTime += time;
        this.currentDistance += distance;
    }

    public boolean CheckIfLoadFits(int dem) {
        return ((getCurrentLoad() + dem <= TRUCK_CAPACITY));
    }

    public boolean CheckIfTimeFits(int dem) {
        return ((getCurrentWorkTime() + dem <= TIME_CAPACITY));
    }
}