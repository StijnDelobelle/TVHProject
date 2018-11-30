package Heuristiek;

import Objects.*;

import java.sql.Timestamp;
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

        Timestamp time1 = new Timestamp(System.currentTimeMillis());
        System.out.println("Starting with optimalisation => " + time1);
        solution.meta();

        Timestamp time2 = new Timestamp(System.currentTimeMillis());
        System.out.println("Starting with making feasible => " + time2);
        solution.MakeFeasible();
        //solution.meta();

        solution.WriteFileNieuw();
    }
}