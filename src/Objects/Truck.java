package Objects;

import java.io.*;
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
    private int aantal_ritten;

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
        this.aantal_ritten = 1;
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

    public int getAantal_ritten() { return aantal_ritten; }
    public void setAantal_ritten(int aantal_ritten) { this.aantal_ritten = aantal_ritten; }

    public void addTijdLaden(int tijd) {this.tijdLaden += tijd;}
    public void lessTijdLaden(int tijd) {this.tijdLaden -= tijd;}


    public void addLoad(int load) {this.currentLoad += load;}
    public void lessLoad(int load) {this.currentLoad -= load;}

    public void addStopToRoute(Stop stop) {

            if(stop.getLocation().getId() == 19)
            {
                String i = "";
            }

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

//    /** Add customer to truck route **/
//    public void addPointToRoute(Ride ride) {
//        int distance = distanceMatrix[ride.getFromLocation().getId()][ride.getToLocation().getId()];
//        int time = timeMatrix[ride.getFromLocation().getId()][ride.getToLocation().getId()];
//        int load = (ride.getMachine() != null)? ride.getMachine().getMachineType().getVolume() : 0;
//        int serviceTime = (ride.getMachine() != null)? ride.getMachine().getMachineType().getServiceTime() : 0;
//        Request.Type type = ride.getType();
//
//        this.route.add(ride);
//
//        this.currentDistance += distance;
//        this.currentLoad += load;
//        this.currentLocation = ride.getToLocation();
//
//        if(type == Request.Type.COLLECT || type == Request.Type.TEMPORARYCOLLECT )
//            this.loadedMachines.add(ride.getMachine());
//        else if(type == Request.Type.DROP )
//            this.loadedMachines.remove(ride.getMachine());
//
//        // Tijd om alles nog af te laden
//        int tijdAfladen = 0;
//        if(type == Request.Type.END){
//            for(Machine machine : this.loadedMachines){
//                tijdAfladen += machine.getMachineType().getServiceTime();
//            }
//        }
//
//        this.currentWorkTime += (time + serviceTime + tijdAfladen);
//
//        /** Update machine locations **/
//        for(Machine machine : loadedMachines){
//            machines.get(machine.getId()).setLocation(currentLocation);
//        }
//    }

    public boolean CheckIfLoadFits(int load) {
        return ((getCurrentLoad() + load <= TRUCK_CAPACITY));
    }

    public boolean CheckIfTimeFits(int rideTime, int serviceTime, Location fromLocation) {
        int homeTime = timeMatrix[fromLocation.getId()][this.endLocation.getId()];
        return ((getCurrentWorkTime() + serviceTime +  rideTime + serviceTime + homeTime <= TIME_CAPACITY));
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