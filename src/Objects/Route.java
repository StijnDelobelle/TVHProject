package Objects;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable {
    private ArrayList<Truck> trucks;
    private int totalCost;

    public Route(ArrayList<Truck> trucks, int totalCost) {
        this.trucks = trucks;
        this.totalCost = totalCost;
    }

    public ArrayList<Truck> getTrucks() {return trucks;}
    public void setTrucks(ArrayList<Truck> trucks) {this.trucks = trucks;}

    public int getTotalCost() {return totalCost;}
    public void setTotalCost(int totalCost) {this.totalCost = totalCost;}
}
