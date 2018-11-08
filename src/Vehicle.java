import java.util.ArrayList;

class Vehicle
{
    public int VehId;
    public ArrayList<Customer> Route = new ArrayList<Customer>();
    public int capacity;
    public int timeCapacity;
    public int load;
    public int curLoc;
    public int startLoc;
    public int curWorkTime;
    public boolean Closed;

    public Vehicle(int id, int cap,int timeCap, int curLoc)
    {
        this.VehId = id;
        this.capacity = cap;
        this.timeCapacity = timeCap;
        this.load = 0;
        this.curLoc = curLoc; //In depot Initially
        this.Closed = false;
        this.Route.clear();
    }

    public void AddNode(Customer Customer,int time )//Add Customer to Vehicle Route
    {
        Route.add(Customer);
        this.load +=  Customer.c;
        this.curLoc = Customer.locationId;
        this.curWorkTime += time;
    }

    public boolean CheckIfFits(int dem) //Check if we have Capacity Violation
    {
        return ((load + dem <= capacity));
    }

    public boolean CheckIfTimeFits(int dem) //Check if we have Time Violation
    {
        return ((curWorkTime + dem <= timeCapacity));
    }
}