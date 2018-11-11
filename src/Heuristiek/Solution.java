package Heuristiek;

import Objects.*;
import Main.*;

import java.io.*;
import java.util.*;

import static Heuristiek.Problem.*;

public class Solution  {
    private Route bestRoute;
    private int Cost;
    private int BestSolutionCost;
    private ArrayList<Truck> greedyTrucks;
    private ArrayList<Machine> greedyMachines;
    private ArrayList<Customer> greedyCustomers;

    private ArrayList<Truck> VehiclesForBestSolution;
    private ArrayList<Integer> PastSolutions;


    public void GreedySolution(ArrayList<Customer> argCustomers, ArrayList<Truck> argTrucks,  ArrayList<Machine> argMachines) {

        /** Nieuwe route uittesten **/

        Cost = 0;
        int candidateCost;
        int truckIndex = 0;

        greedyCustomers = (ArrayList<Customer>) deepClone(argCustomers);
        greedyTrucks = (ArrayList<Truck>) deepClone(argTrucks);
        greedyMachines = (ArrayList<Machine>) deepClone(argMachines);

        // TODO: Zorg dat de truck start vanaf het dichtste punt!


        // Doorgaan tot alle requests afgehandeld zijn
        while (VisitedAllCustomers(greedyCustomers))
        {
            Customer candidate = null;
            int minCost = Integer.MAX_VALUE;

            for (Customer customer : greedyCustomers) {
                if (!customer.isVisited())
                {
                    // Past de mogelijke volgende stop in de truck?
                    if (greedyTrucks.get(truckIndex).CheckIfLoadFits(customer.getMachine().getMachineType().getVolume()))
                    {
                        // Past de tijd nog van deze plaats naar volgende en terug nog in de max tijd?
                        int tijdHeen = timeMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][customer.getLocation().getId()];
                        int tijdTerug = timeMatrix[customer.getLocation().getId()][greedyTrucks.get(truckIndex).getEndLocation().getId()];
                        int tijdSerivce = customer.getMachine().getMachineType().getServiceTime();

                        if(greedyTrucks.get(truckIndex).CheckIfTimeFits(tijdSerivce + tijdHeen + tijdSerivce + tijdTerug))
                        {
                            // Bereken de afstand van de kandidaat
                            candidateCost = distanceMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][customer.getLocation().getId()];
                            if (minCost > candidateCost) {
                                minCost = candidateCost;
                                candidate = customer;
                            }
                        }
                    }
                }
            }

            if (candidate == null)
            {
                // Geen enkele customer past
                if (truckIndex + 1 < greedyTrucks.size() ) //We hebben nog trucks ter beschikking
                {
                    // Maximum capaciteit voor deze truck is bereikt (geen kandidaten) -> Dus terugkeren naar depots.
                    if (!depots.contains(greedyTrucks.get(truckIndex).getCurrentLocation()))
                    {
                        Customer depot = new Customer(greedyMachines.get(0), greedyTrucks.get(truckIndex).getStartLocation(), Customer.Type.COLLECT);

                        int rideTime = timeMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][greedyTrucks.get(truckIndex).getEndLocation().getId()];
                        int rideDistance = distanceMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][greedyTrucks.get(truckIndex).getEndLocation().getId()];

                        greedyTrucks.get(truckIndex).addPointToRoute(depot, rideTime, rideDistance);
                        Cost += rideDistance;
                    }
                    // Next truck
                    truckIndex++;
                }
                else
                {
                    System.out.println("\nNiet genoeg trucks beschikbaar!");
                    System.exit(0);
                }
            }
            else
            {
                //Als een nieuwe kandidaat gevonden is, add point to route
                int rideTime = timeMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][candidate.getLocation().getId()];
                int serviceTime = candidate.getMachine().getMachineType().getServiceTime();
                int distance = distanceMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][candidate.getLocation().getId()];

                greedyTrucks.get(truckIndex).addPointToRoute(candidate,rideTime + serviceTime, distance);
                candidate.setVisited(true);
                Cost += minCost;
            }
        }

        int rideDistance = distanceMatrix[greedyTrucks.get(truckIndex).getCurrentLocation().getId()][greedyTrucks.get(truckIndex).getStartLocation().getId()];
        Customer depot = new Customer(greedyMachines.get(0), greedyTrucks.get(truckIndex).getStartLocation(), Customer.Type.COLLECT);
        greedyTrucks.get(truckIndex).addPointToRoute(depot,0, 0);
        Cost += rideDistance;

        bestRoute = new Route(greedyTrucks, Cost);
    }

    /** Kijken als alle klanten bezocht zijn **/
    public boolean VisitedAllCustomers(ArrayList<Customer> Costumers) {
        for(Customer costumer : Costumers)
        {
            if (!costumer.isVisited())
                return true;
        }
        return false;
    }

    public void SolutionPrint(String Solution_Label) {
        System.out.println("\n" + Solution_Label + "\n");

        for (Truck truck : bestRoute.getTrucks()) {
            if(truck.getRoute().size() > 1) {
                System.out.print(truck.getId() + " " + truck.getCurrentWorkTime() + " " + truck.getCurrentDistance());

                // Per locatie alle machine veranderingen op de truck
                int currentLocationID = -1;
                for (Customer customer: truck.getRoute()) {
                    if(currentLocationID != customer.getLocation().getId())
                    {
                        currentLocationID = customer.getLocation().getId();
                        System.out.print(" " + customer.getLocation().getId() + ":" + customer.getMachine().getId());
                    }
                    else
                    {
                        System.out.print(":" + customer.getMachine().getId());
                    }
                }
                System.out.print("\n");
            }
        }
        System.out.println("\nTotale afstand: " + bestRoute.getTotalCost()+"\n");
    }

    public void WriteFile() throws IOException {

        // Berekenen hoeveel newTrucks er effectief in dienst zijn
        int numberOfUsedTrucks = 0;
        for (Truck truck : bestRoute.getTrucks()) {
            if(truck.getRoute().size() > 1){
                numberOfUsedTrucks++;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.OUTPUT_FILE));
        writer.write("PROBLEM: " + Main.INPUT_FILE + "\n");
        writer.write("DISTANCE: " + bestRoute.getTotalCost() + "\n");
        writer.write("TRUCKS: " + numberOfUsedTrucks + "\n");

        for (Truck truck : bestRoute.getTrucks()) {
            if(truck.getRoute().size() > 1) {
                writer.write(truck.getId() + " " + truck.getCurrentWorkTime() + " " + truck.getCurrentDistance());

                // Per locatie alle machine veranderingen op de truck
                int currentLocationID = -1;
                for (Customer customer: truck.getRoute()) {
                    if(currentLocationID != customer.getLocation().getId())
                    {
                        currentLocationID = customer.getLocation().getId();
                        writer.write(" " + customer.getLocation().getId() + ":" + customer.getMachine().getId());
                    }
                    else
                    {
                        writer.write(":" + customer.getMachine().getId());
                    }
                }
                writer.write("\n");
            }
        }
        writer.close();
    }

    public Object deepClone(Object orig) {
        Object obj = null;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

    //<editor-fold TABUSEARCH LATER TOEVOEGEN>

    public void TabuSearch(int TABU_Horizon, int[][] costMatrix,int[][] timeMatrix, Route greedyRoute) {

        //We use 1-0 exchange move
        ArrayList<Customer> RouteFrom;
        ArrayList<Customer> RouteTo;

        int MovingNodeDemand = 0;
        int locationFrom = 0;
        int locationTo = 0;

        int VehIndexFrom,VehIndexTo;
        int BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom =-1, SwapRouteTo=-1;

        int MAX_ITERATIONS = 10000;
        int iteration_number= 0;

        int DimensionCustomer = costMatrix[1].length;
        int TABU_Matrix[][] = new int[DimensionCustomer+1][DimensionCustomer+1];

        BestSolutionCost = greedyRoute.getTotalCost(); //Initial solution Cost

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Integer.MAX_VALUE;

            for (VehIndexFrom = 0;  VehIndexFrom <  greedyRoute.getTrucks().size();  VehIndexFrom++) {
                RouteFrom =  greedyRoute.getTrucks().get(VehIndexFrom).getRoute();
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depots!

                    for (VehIndexTo = 0; VehIndexTo <  greedyRoute.getTrucks().size(); VehIndexTo++) {
                        RouteTo =   greedyRoute.getTrucks().get(VehIndexTo).getRoute();
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).getMachine().getMachineType().getVolume();
                            locationFrom = RouteFrom.get(i).getLocation().getId();
                            // locationTo = RouteTo.get(i).locationId;

                            //int timeToCustomer = timeMatrix[locationFrom][locationTo];

                            //|| greedyRoute.v[VehIndexTo].CheckIfTimeFits(timeToCustomer)

                            if ((VehIndexFrom == VehIndexTo) ||  (greedyRoute.getTrucks().get(VehIndexTo).CheckIfLoadFits(MovingNodeDemand) && greedyTrucks.get(VehIndexTo).CheckIfTimeFits(timeMatrix[greedyTrucks.get(VehIndexTo).getCurrentLocation().getId()][locationFrom] + timeMatrix[locationFrom][greedyTrucks.get(VehIndexTo).getStartLocation().getId()])))
                            {
                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                {
                                    int MinusCost1 = costMatrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i).getLocation().getId()];
                                    int MinusCost2 = costMatrix[RouteFrom.get(i).getLocation().getId()][RouteFrom.get(i + 1).getLocation().getId()];
                                    int MinusCost3 = costMatrix[RouteTo.get(j).getLocation().getId()][RouteTo.get(j + 1).getLocation().getId()];

                                    int AddedCost1 = costMatrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i + 1).getLocation().getId()];
                                    int AddedCost2 = costMatrix[RouteTo.get(j).getLocation().getId()][RouteFrom.get(i).getLocation().getId()];
                                    int AddedCost3 = costMatrix[RouteFrom.get(i).getLocation().getId()][RouteTo.get(j + 1).getLocation().getId()];

                                    //Check if the move is a Tabu! - If it is Tabu break
                                    if ((TABU_Matrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i+1).getLocation().getId()] != 0)
                                            || (TABU_Matrix[RouteTo.get(j).getLocation().getId()][RouteFrom.get(i).getLocation().getId()] != 0)
                                            || (TABU_Matrix[RouteFrom.get(i).getLocation().getId()][RouteTo.get(j+1).getLocation().getId()] != 0)) {
                                        break;
                                    }

                                    NeigthboorCost = AddedCost1 + AddedCost2 + AddedCost3
                                            - MinusCost1 - MinusCost2 - MinusCost3;

                                    if (NeigthboorCost < BestNCost) {
                                        BestNCost = NeigthboorCost;
                                        SwapIndexA = i;
                                        SwapIndexB = j;
                                        SwapRouteFrom = VehIndexFrom;
                                        SwapRouteTo = VehIndexTo;
                                    }
                                }
                            }

                        }
                    }
                }
            }

            for (int o = 0; o < TABU_Matrix[0].length;  o++) {
                for (int p = 0; p < TABU_Matrix[0].length ; p++) {
                    if (TABU_Matrix[o][p] > 0)
                    { TABU_Matrix[o][p]--; }
                }
            }

            RouteFrom =  greedyRoute.getTrucks().get(SwapRouteFrom).getRoute();
            RouteTo =  greedyRoute.getTrucks().get(SwapRouteTo).getRoute();
            greedyRoute.getTrucks().get(SwapRouteFrom).setRoute(null);
            greedyRoute.getTrucks().get(SwapRouteTo).setRoute(null);

            Customer SwapNode = RouteFrom.get(SwapIndexA);

            int NodeIDBefore = RouteFrom.get(SwapIndexA-1).getLocation().getId();
            int NodeIDAfter = RouteFrom.get(SwapIndexA+1).getLocation().getId();
            int NodeID_F = RouteTo.get(SwapIndexB).getLocation().getId();
            int NodeID_G = RouteTo.get(SwapIndexB+1).getLocation().getId();

            Random TabuRan = new Random();
            int RendomDelay1 = TabuRan.nextInt(5);
            int RendomDelay2 = TabuRan.nextInt(5);
            int RendomDelay3 = TabuRan.nextInt(5);

            TABU_Matrix[NodeIDBefore][SwapNode.getLocation().getId()] = TABU_Horizon + RendomDelay1;
            TABU_Matrix[SwapNode.getLocation().getId()][NodeIDAfter]  = TABU_Horizon + RendomDelay2 ;
            TABU_Matrix[NodeID_F][NodeID_G] = TABU_Horizon + RendomDelay3;

            RouteFrom.remove(SwapIndexA);

            if (SwapRouteFrom == SwapRouteTo) {
                if (SwapIndexA < SwapIndexB) {
                    RouteTo.add(SwapIndexB, SwapNode);
                } else {
                    RouteTo.add(SwapIndexB + 1, SwapNode);
                }
            }
            else
            {
                RouteTo.add(SwapIndexB+1, SwapNode);
            }


            greedyRoute.getTrucks().get(SwapRouteFrom).setRoute(RouteFrom);
            greedyRoute.getTrucks().get(SwapRouteFrom).lessLoad(MovingNodeDemand);

            greedyRoute.getTrucks().get(SwapRouteTo).setRoute(RouteTo);
            greedyRoute.getTrucks().get(SwapRouteTo).addLoad(MovingNodeDemand);

            PastSolutions.add(greedyRoute.getTotalCost());

            greedyRoute.setTotalCost(greedyRoute.getTotalCost() + BestNCost);

            if (greedyRoute.getTotalCost() < BestSolutionCost)
            {
                SaveBestSolution(greedyRoute);
            }

            if (iteration_number == MAX_ITERATIONS)
            {
                Termination = true;
            }
            measureLoad(greedyRoute);
            measureTime(greedyRoute,timeMatrix);
        }

        greedyRoute.setTrucks(VehiclesForBestSolution);
        greedyRoute.setTotalCost(BestSolutionCost);
    }

    public void SaveBestSolution(Route greedyRoute) {
        BestSolutionCost = greedyRoute.getTotalCost();
        for (int j=0 ; j < greedyRoute.getTrucks().size() ; j++)
        {
            VehiclesForBestSolution.get(j).getRoute().clear();
            if (! greedyRoute.getTrucks().get(j).getRoute().isEmpty())
            {
                int RoutSize = greedyRoute.getTrucks().get(j).getRoute().size();
                for (int k = 0; k < RoutSize ; k++) {
                    Customer n = greedyRoute.getTrucks().get(j).getRoute().get(k);
                    VehiclesForBestSolution.get(j).getRoute().add(n);
                }
            }
        }
    }

    //Updates telkens de nieuwe curLoad van een voertuig
    //TODO moet nog verbeteren is een test!!
    public void measureLoad(Route route) {
        for(Truck truck : route.getTrucks())
        {
            int load = 0;
            for(Customer c : truck.getRoute())
            {
                load += c.getMachine().getMachineType().getVolume();
            }

            truck.setCurrentLoad(load);
        }
    }

    //Updates telkens de nieuwe workTime berekenen van een voertuig
    //TODO moet nog verbeteren is een test!!
    public void measureTime(Route r, int[][] timeMatrix) {
        for(Truck v : r.getTrucks())
        {
            int workTime = 0;
            Customer vorigeKlant = null;
            for(Customer customer : v.getRoute())
            {
                if(vorigeKlant != null)
                {
                    workTime = workTime + timeMatrix[vorigeKlant.getLocation().getId()][customer.getLocation().getId()];
                }

                vorigeKlant = customer;
            }

            v.setCurrentWorkTime(workTime);
        }
    }

    //</editor-fold>
}
