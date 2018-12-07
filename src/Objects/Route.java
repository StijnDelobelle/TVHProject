package Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

public class Route implements Serializable {
    private ArrayList<Truck> trucks = new ArrayList<>();
    private int totalDistance;

    public Route(ArrayList<Truck> trucks) {
        this.trucks = trucks;
    }

    public Route(Route r) {
        this.totalDistance = r.totalDistance;

        trucks = new ArrayList<>();
        for(Truck truck : r.getTrucks()){
            ArrayList<Stop> stops = new ArrayList<>();
            this.trucks.add(new Truck(truck.getId(), truck.getStartLocation(), truck.getEndLocation(), stops, truck.getTijdLaden()));
        }

        // Per truck
        for(int truckIndex = 0; truckIndex < this.trucks.size(); truckIndex++)
        {
            // Per stop
            for(Stop stop : r.getTrucks().get(truckIndex).getStops())
            {

                LinkedList<Machine> collect = new LinkedList<>();
                LinkedList<Machine> drop = new LinkedList<>();

                for(Machine machine : stop.getcollect()) {
                    collect.add(machine);
                }
                for(Machine machine : stop.getdrop()) {
                    drop.add(machine);
                }

                this.trucks.get(truckIndex).getStops().add(new Stop(stop.getLocation(), collect, drop, stop.depo));
            }
        }
    }

    public ArrayList<Truck> getTrucks() {return trucks;}
    public void setTrucks(ArrayList<Truck> trucks) {this.trucks = trucks;}
    public void setTruck(int index, Truck truck) {
        this.trucks.remove(index);
        this.trucks.add(index, truck);
    }

    public int getTotalDistance()
    {
        return totalDistance;
    }
    public void setTotalDistance(int distance)
    {
        totalDistance = distance;
    }
}
