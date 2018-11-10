package Objects;

import java.util.ArrayList;

import static Heuristiek.Problem.*;

public class Truck
{
    private int id;
    public ArrayList<Customer> Route = new ArrayList<Customer>();
    public int actualLoad;
    public Location currentLocation;
    public Location startLocation;
    public Location endLocation;

    public int curWorkTime;
    public int curDistance;
    public boolean Closed;

    //TODO: beter uitlezen vanuit ReadInput


    public Truck(int id, Location startLocation, Location eindLocation, Location currentLocation)
    {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = eindLocation;
        this.actualLoad = 0;
        this.currentLocation = currentLocation; //In depots Initially
        this.Closed = false;
        this.curWorkTime = 0;
        this.curDistance = 0;
        this.Route.clear();
    }

    public void AddNode(Customer Customer, int time , int distance)//Add Objects.Customer to Objects.Truck Objects.Route
    {
        Route.add(Customer);
        this.actualLoad +=  Customer.machine.getMachineType().getVolume();
        this.currentLocation = Customer.location;
        this.curWorkTime += time;
        this.curDistance += distance;
    }

    public boolean CheckIfFits(int dem) //Check if we have Capacity Violation
    {
        return ((actualLoad + dem <= TRUCK_CAPACITY));
    }

    public boolean CheckIfTimeFits(int dem) //Check if we have Time Violation
    {
        return ((curWorkTime + dem <= TIME_CAPACITY));
    }

    public int getID(){return id;}
}