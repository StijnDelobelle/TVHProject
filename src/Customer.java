class Customer{

    // Requirements of the customer (number to be delivered)
    public int c;
    public int locationId;
    public int machineId;
    public boolean IsRouted;
    public boolean IsDepot; //True if it Depot Node


    //1 = drop, 2 = collect

    public Customer(int locationId, int machineId){
        //this.x = x;
        //this.y = y;
        this.locationId = locationId;
        this.machineId = machineId;
    }
}