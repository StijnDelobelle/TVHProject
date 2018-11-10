package Objects;

public class Customer{

    // Requirements of the customer (number to be delivered)
    public Location location;
    public Machine machine;
    public boolean IsRouted;
    public boolean IsDepot; //True if it Depot Node
    public Type type;

    public enum Type {
        DROP, COLLECT
    }

    public Customer(Machine machine, Location location, Type type){
        this.machine = machine;
        this.location = location;
        this.type = type;
    }
}