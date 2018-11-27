package Heuristiek;

import Objects.*;
import java.util.*;

public class Problem {

    /** Globale variabelen **/

    public static int[][] timeMatrix;
    public static int[][] distanceMatrix;

    public static int TRUCK_CAPACITY = 0;
    public static int TIME_CAPACITY = 0;

    public static ArrayList<Location> locations = new ArrayList<>();
    public static ArrayList<Location> depots = new ArrayList<>();
    public static ArrayList<Truck> trucks = new ArrayList<>();
    public static ArrayList<MachineType> machineTypes = new ArrayList<>();
    public static ArrayList<Machine> machines = new ArrayList<>();
    public static ArrayList<Request> requests = new ArrayList<>();

    public static void solve() throws Exception{

        Solution solution = new Solution();

        /** InitialSolution **/
        solution.InitialSolution(requests, trucks);
        //solution.MakeFeasible();

        Route best = solution.meta();
        solution.WriteFileNieuw(best);

        /** TabuSearch Heuristic Neighborhood Search **/
        //solution.TabuSearch(10);
        //solution.WriteFile();
    }
}