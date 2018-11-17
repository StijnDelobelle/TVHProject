package Objects;

import java.io.Serializable;
import java.util.ArrayList;

import static Heuristiek.Problem.distanceMatrix;

public class Route implements Serializable {
    private ArrayList<Truck> trucks;
    private int totalDistance;

    public Route(ArrayList<Truck> trucks) {
        this.trucks = trucks;
    }

    public ArrayList<Truck> getTrucks() {return trucks;}
    public void setTrucks(ArrayList<Truck> trucks) {this.trucks = trucks;}

    public int getTotalDistance() {
        totalDistance = 0;
        for(Truck truck : trucks)
        {
            for(Ride ride : truck.getRoute()){
                if(ride.getPickupLocation() == null)
                    totalDistance += distanceMatrix[ride.getFromLocation().getId()][ride.getToLocation().getId()];
                else
                    totalDistance += distanceMatrix[ride.getFromLocation().getId()][ride.getPickupLocation().getId()] + distanceMatrix[ride.getPickupLocation().getId()][ride.getToLocation().getId()];
            }
        }
        return totalDistance;
    }
}
