package Objects;

public class Route {
    private Truck[] trucks;
    private int totalCost;

    public Route(Truck[] trucks, int totalCost) {
        this.trucks = trucks;
        this.totalCost = totalCost;
    }

    public Truck[] getTrucks() {return trucks;}
    public void setTrucks(Truck[] trucks) {this.trucks = trucks;}

    public int getTotalCost() {return totalCost;}
    public void setTotalCost(int totalCost) {this.totalCost = totalCost;}
}
