package Heuristiek;

import Objects.*;
import java.util.*;

public class Problem {

    /** Globale variabelen **/

    public static Route bestRoute = null;

    public static int[][] timeMatrix;
    public static int[][] distanceMatrix;

    public static int TRUCK_CAPACITY = 0;
    public static int TIME_CAPACITY = 0;

    public static ArrayList<Location> locations = new ArrayList<>();
    public static ArrayList<Location> depots = new ArrayList<>();
    public static ArrayList<Truck> allTrucks = new ArrayList<>();
    public static ArrayList<MachineType> machineTypes = new ArrayList<>();
    public static ArrayList<Machine> machines = new ArrayList<>();
    public static ArrayList<Customer> customers = new ArrayList<>();

    public void solve() throws Exception{

        /** GreedySolution **/
        Solution solution = new Solution(allTrucks);
        int minCost = Integer.MAX_VALUE;
        for(int i = 0;i<1;i++)
        {
            Route route = solution.GreedySolution(customers,distanceMatrix,timeMatrix,i);

            if(route.getTotalCost() < minCost) {
                minCost = route.getTotalCost();
                bestRoute = route;
            }
        }

        solution.SolutionPrint("Greedy",bestRoute);
        solution.WriteFile(bestRoute);

        /** TabuSearch Heuristic Neighborhood Search **/
        // solution.TabuSearch(10,distanceMatrix,timeMatrix,bestRoute);
        // solution.SolutionPrint("Solution after TabuSearch Heuristic Neighborhood Search", bestRoute);
    }
}