package Heuristiek;

import Objects.*;
import java.util.*;

public class Problem {

    public static int TRUCK_CAPACITY = 0;
    public static int TIME_CAPACITY = 0;

    public static int[][] timeMatrix = new int[28][28];
    public static int[][] distanceMatrix = new int[28][28];

    public static ArrayList<Location> locations = new ArrayList<>();
    public static ArrayList<Location> depots = new ArrayList<>();
    public static ArrayList<Truck> trucks = new ArrayList<Truck>();
    public static ArrayList<MachineType> machineTypes = new ArrayList<MachineType>();
    public static ArrayList<Machine> machines = new ArrayList<Machine>();
    public static ArrayList<Customer> customers = new ArrayList<>();

    public static Route bestRoute = null;


    public void solve() throws Exception{

        //Heuristiek.Problem Parameters
        int NoOfCustomers = customers.size();
        int NoOfVehicles = 40;
        int VehicleCap = 100;

        //Tabu Parameter
        int TABU_Horizon = 10;


        for(Location c : depots)
        {
            c.setDepot(true);
        }

        //Compute the greedy Solution
        System.out.println("Attempting to resolve Objects.Truck Routing Problem (Problem) for "+NoOfCustomers+
                " Customers and "+NoOfVehicles+" trucks"+" with "+VehicleCap + " units of capacity\n");

        Solution s = new Solution(NoOfCustomers,NoOfVehicles,VehicleCap, trucks);

        ArrayList<Route> ar = new ArrayList<>();
        double minCost = Integer.MAX_VALUE;


        for(int i = 0;i<1;i++)
        {
            Route r = s.GreedySolution(customers,distanceMatrix,timeMatrix,i);

            if(r.totalCost<minCost)
            {
                minCost = r.totalCost;
                bestRoute = r;
            }
        }

        s.SolutionPrint("Greedy Solution",bestRoute);
        s.WriteFile(bestRoute);

        //s.TabuSearch(TABU_Horizon,distanceMatrix,timeMatrix,bestRoute);
        //s.SolutionPrint("Solution after TabuSearch Heuristic Neighborhood Search", bestRoute);

    }
}