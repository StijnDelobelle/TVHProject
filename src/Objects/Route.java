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

    public Route(Route r) {
        this.trucks = r.trucks;
        this.totalDistance = r.totalDistance;
    }

    public ArrayList<Truck> getTrucks() {return trucks;}
    public void setTrucks(ArrayList<Truck> trucks) {this.trucks = trucks;}


    public int getTotalDistance()
    {
        return totalDistance;
    }

    public void setTotalDistance(int distance)
    {
        totalDistance = distance;
    }
}
