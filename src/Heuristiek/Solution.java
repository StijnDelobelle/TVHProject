package Heuristiek;

import Objects.*;
import Main.*;

import javax.crypto.Mac;
import javax.sound.sampled.LineEvent;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static Heuristiek.Problem.distanceMatrix;
import static Heuristiek.Problem.locations;
import static Heuristiek.Problem.timeMatrix;

public class Solution  {
    private Route bestRoute;
    private int BestSolutionCost;
    private ArrayList<Truck> initialTrucks;
    private ArrayList<Machine> initialMachines;
    private ArrayList<Customer> initialCustomers;
    public ArrayList<Ride> rides = new ArrayList<>();
    Location currentStop = null;
    Customer currenCustomer = null;

    private ArrayList<Truck> VehiclesForBestSolution = new ArrayList<>();
    private ArrayList<Integer> PastSolutions = new ArrayList<>();

    public void InitialSolution(ArrayList<Customer> argCustomers, ArrayList<Truck> argTrucks,  ArrayList<Machine> argMachines) {

        /** MACHINE_TYPES: [id volume serviceTime name]  **/
        /** MACHINES:      [id machineTypeId locationId] **/
        /** DROPS:         [id machineTypeId locationId] **/
        /** COLLECTS:      [id machineId]                **/

        int totalCost = 0;

        initialCustomers = (ArrayList<Customer>) deepClone(argCustomers);
        initialTrucks = (ArrayList<Truck>) deepClone(argTrucks);
        initialMachines = (ArrayList<Machine>) deepClone(argMachines);

        currentStop = initialTrucks.get(0).getStartLocation();

        ArrayList<Integer> collects = new ArrayList<>();
        for (Customer customer : initialCustomers){
            if(customer.getType() == Customer.Type.COLLECT)
                collects.add(customer.getMachine().getId());
        }


        // Ritten van alle kortste afstanden tussen collects/drops berekenen en toevoegen in rides
        while(AlleCustomersVisited())
        {
            /** Variabelen worden reset bij elke test **/
            Machine candidateMachine = null;
            Customer candidateCustomer = null;
            int minDistance = Integer.MAX_VALUE;
            Customer.Type type = null;

            // Alle requests doorlopen
            for (Customer customer : initialCustomers)
            {
                if(!customer.isVisited())
                {
                    /****** DROPS ******/
                    if (customer.getType() == Customer.Type.DROP) {
                        // Alle machines zoeken van hetzelfde type dat gevraagd is
                        ArrayList<Machine> identicalMachines = new ArrayList<>();
                        for(Machine machine : initialMachines) {
                            if(machine.getMachineType().getId() == customer.getMachineType().getId() && !collects.contains(machine.getId())){
                                identicalMachines.add(machine);
                            }
                        }

                        // Kortste afstand van alle identieke machines naar de drop locatie berekenen
                        for(Machine machine : identicalMachines)
                        {
                                // Afstand huidige locatie naar locatie machine + afstand van machine naar effectieve dropplaats
                                int distance = distanceMatrix[currentStop.getId()][machine.getLocation().getId()] + distanceMatrix[machine.getLocation().getId()][customer.getLocation().getId()];
                                if( minDistance > distance) {
                                    minDistance = distance;
                                    candidateMachine = machine;
                                    candidateCustomer = customer;
                                    type = Customer.Type.DROP;

                                }
                        }
                    }

                    /****** COLLECTS ******/
                    if (customer.getType() == Customer.Type.COLLECT)
                    {
                            int distance = distanceMatrix[currentStop.getId()][customer.getMachine().getLocation().getId()];
                            if (minDistance > distance) {
                                minDistance = distance;
                                candidateMachine = customer.getMachine();
                                candidateCustomer = customer;
                                type = Customer.Type.COLLECT;

                            }
                    }
                }
            }

            if (candidateCustomer.getType() == Customer.Type.DROP) {
                candidateCustomer.setMachine(candidateMachine);
                candidateCustomer.setMachineType(candidateMachine.getMachineType());
                rides.add(new Ride(currentStop, candidateCustomer.getLocation(), candidateMachine.getLocation(), candidateMachine));
                currentStop = candidateCustomer.getLocation();

            } else if (candidateCustomer.getType() == Customer.Type.COLLECT) {
                candidateCustomer.setLocation(candidateMachine.getLocation());
                rides.add(new Ride(currentStop, candidateMachine.getLocation(), null, candidateCustomer.getMachine()));
                currentStop = candidateMachine.getLocation();
            }

            candidateCustomer.setVisited(true);
        }


        // Begin bij truck 1
        int truckIndex = 0;
        Location candidateNextStop = initialTrucks.get(0).getStartLocation();

        for(int rideIndex = 0; rideIndex < rides.size(); rideIndex++)
        {
            int load = rides.get(rideIndex).getMachine().getMachineType().getVolume();
            int service = rides.get(rideIndex).getMachine().getMachineType().getServiceTime();
            int timeToEndLocation = 0;

            /** DROPS **/
            if(rides.get(rideIndex).getPickupLocation() != null)
            {
                timeToEndLocation = timeMatrix[candidateNextStop.getId()][rides.get(rideIndex).getPickupLocation().getId()] + timeMatrix[rides.get(rideIndex).getPickupLocation().getId()][rides.get(rideIndex).getToLocation().getId()] + timeMatrix[rides.get(rideIndex).getToLocation().getId()][initialTrucks.get(truckIndex).getEndLocation().getId()];
            }
            /** COLLECT **/
            else
            {
                timeToEndLocation = timeMatrix[candidateNextStop.getId()][rides.get(rideIndex).getToLocation().getId()] + timeMatrix[rides.get(rideIndex).getToLocation().getId()][initialTrucks.get(truckIndex).getEndLocation().getId()];
            }

            if (initialTrucks.get(truckIndex).CheckIfLoadFits(load) && initialTrucks.get(truckIndex).CheckIfTimeFits(timeToEndLocation)) // TODO: Van hier al rekenening houden met serivce time
            {
                // Indien een pickuplocation moet de machine nog opgehaald worden voor de drop
                if(rides.get(rideIndex).getPickupLocation() != null)
                {
                    /** DROPS **/
                    // Rit naar machine
                    int time1 = timeMatrix[candidateNextStop.getId()][rides.get(rideIndex).getPickupLocation().getId()];
                    int distance1 = distanceMatrix[candidateNextStop.getId()][rides.get(rideIndex).getPickupLocation().getId()];
                    Customer temporaryCustomer = new Customer(rides.get(rideIndex).getMachine(), rides.get(rideIndex).getMachine().getMachineType(), rides.get(rideIndex).getPickupLocation(), Customer.Type.TEMPORARY );
                    initialTrucks.get(truckIndex).addPointToRoute(temporaryCustomer, time1 + service, distance1, load, rides.get(rideIndex).getMachine());

                    totalCost += distance1;

                    // Rit naar dropplaats
                    int time2 = timeMatrix[rides.get(rideIndex).getPickupLocation().getId()][rides.get(rideIndex).getToLocation().getId()];
                    int distance2 = distanceMatrix[rides.get(rideIndex).getPickupLocation().getId()][rides.get(rideIndex).getToLocation().getId()];
                    Customer endCustomer = new Customer(rides.get(rideIndex).getMachine(), rides.get(rideIndex).getMachine().getMachineType(), rides.get(rideIndex).getToLocation(), Customer.Type.DROP );
                    initialTrucks.get(truckIndex).addPointToRoute(endCustomer, time2 + service, distance2, -load, rides.get(rideIndex).getMachine());

                    totalCost += distance2;
                    candidateNextStop = rides.get(rideIndex).getToLocation();
                }
                else
                {
                    /** COLLECT **/
                    int time = timeMatrix[candidateNextStop.getId()][rides.get(rideIndex).getToLocation().getId()];
                    int distance = distanceMatrix[candidateNextStop.getId()][rides.get(rideIndex).getToLocation().getId()];
                    Customer endCustomer = new Customer(rides.get(rideIndex).getMachine(), rides.get(rideIndex).getMachine().getMachineType(), rides.get(rideIndex).getToLocation(), Customer.Type.COLLECT );
                    initialTrucks.get(truckIndex).addPointToRoute(endCustomer, time + service, distance, load, rides.get(rideIndex).getMachine());

                    totalCost += distance;
                    candidateNextStop = rides.get(rideIndex).getToLocation();
                }
            }
            else
            {
                // Truck vol -> Naar depot terug sturen
                Customer endCustomer = new Customer(null, null, initialTrucks.get(truckIndex).getEndLocation(), Customer.Type.END );
                int time = timeMatrix[rides.get(rideIndex-1).getToLocation().getId()][initialTrucks.get(truckIndex).getEndLocation().getId()];
                int distance = distanceMatrix[rides.get(rideIndex-1).getToLocation().getId()][initialTrucks.get(truckIndex).getEndLocation().getId()];

                int tijdAfladen = 0;
                // Tijd om alles nog af te laden
                for(Machine machine : initialTrucks.get(truckIndex).getLoadedMachines()){
                    tijdAfladen += machine.getMachineType().getServiceTime();
                }

                initialTrucks.get(truckIndex).addPointToRoute(endCustomer, time + tijdAfladen, distance, 0, null );

                totalCost += distance;
                truckIndex++;


                // Start punt toevoegen
                Customer startCustomer = new Customer(null, null, initialTrucks.get(truckIndex).getStartLocation(), Customer.Type.START );
                candidateNextStop = initialTrucks.get(truckIndex).getStartLocation();
                initialTrucks.get(truckIndex).addPointToRoute(startCustomer, 0, 0,0,null);
                rideIndex--;
            }
        }

        // TODO laatste rit nog naar depot terug sturen!
        int load = rides.get(rides.size()-1).getMachine().getMachineType().getVolume();
        int time = timeMatrix[candidateNextStop.getId()][initialTrucks.get(truckIndex).getEndLocation().getId()];
        int distance = distanceMatrix[candidateNextStop.getId()][initialTrucks.get(truckIndex).getEndLocation().getId()];
        Customer endCustomer = new Customer(null,null, initialTrucks.get(truckIndex).getEndLocation(), Customer.Type.END );
        initialTrucks.get(truckIndex).addPointToRoute(endCustomer, time, distance, load, rides.get(rides.size()-1).getMachine());
        totalCost += distance;

        /** Initiele Oplossing **/
        bestRoute = new Route(initialTrucks, totalCost);
    }

    public boolean AlleCustomersVisited() {
        for(Customer customer : initialCustomers)
        {
            if (!customer.isVisited())
                return true;
        }
        return false;
    }

    public void WriteFile() throws IOException {
        System.out.println("");

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
            // Enkel trucks die rijden uitprinten
            if(truck.getRoute().size() > 1)
            {
                writer.write(truck.getId() + " " + truck.getCurrentDistance() + " " + truck.getCurrentWorkTime());
                System.out.print(truck.getId() + " " + truck.getCurrentDistance() + " " + truck.getCurrentWorkTime());

                // Per locatie alle machine veranderingen op de truck
                int currentLocationID = -1;
                // Eerste null overslaan
                boolean startAdded = false;
                for (Customer customer: truck.getRoute()) {
                    if(customer.getType() == Customer.Type.START){
                        if(!startAdded){
                            writer.write(" " + customer.getLocation().getId());
                            System.out.print(" " + customer.getLocation().getId());
                            startAdded = true;
                        }
                    }
                    else if(customer.getMachine() != null)
                    {
                        if(currentLocationID != customer.getLocation().getId())
                        {
                            currentLocationID = customer.getLocation().getId();
                            writer.write(" " + customer.getLocation().getId() + ":" + customer.getMachine().getId());
                            System.out.print(" " + customer.getLocation().getId() + ":" + customer.getMachine().getId());
                        }
                        else
                        {
                            writer.write(":" + customer.getMachine().getId());
                            System.out.print(":" + customer.getMachine().getId());
                        }
                    }
                    else
                    {
                        writer.write(" " + customer.getLocation().getId());
                        System.out.print(" " + customer.getLocation().getId());

                        if(truck.getLoadedMachines().size() > 0)
                        {
                            for(Machine machine : truck.getLoadedMachines())
                            {
                                // Indien de truck terug naar depot rijd veranderd er niets aan de lading
                                if(machine != null){
                                    writer.write(":" + machine.getId());
                                    System.out.print(":" + machine.getId());
                                }
                            }
                        }
                    }
                }
                writer.write("\n");
                System.out.print("\n");
            }
        }
        writer.close();
        System.out.println("\nTotale afstand: " + bestRoute.getTotalCost()+"\n");
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

    public void TabuSearch(int TABU_Horizon) {

        for (int j=0 ; j < bestRoute.getTrucks().size() ; j++) {
            VehiclesForBestSolution.add(new Truck(0, null,null));
        }

        //We use 1-0 exchange move
        ArrayList<Customer> RouteFrom;
        ArrayList<Customer> RouteTo;

        int MovingNodeDemand = 0;
        int locationFrom = 0;
        //int locationTo = 0;

        int VehIndexFrom,VehIndexTo;
        int BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom =-1, SwapRouteTo=-1;

        int MAX_ITERATIONS = 100000;
        int iteration_number= 0;

        int DimensionCustomer = distanceMatrix[1].length;
        int TABU_Matrix[][] = new int[DimensionCustomer+1][DimensionCustomer+1];

        BestSolutionCost = bestRoute.getTotalCost(); //Initial solution Cost

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Integer.MAX_VALUE;

            for (VehIndexFrom = 0;  VehIndexFrom <  bestRoute.getTrucks().size();  VehIndexFrom++) {
                RouteFrom =  bestRoute.getTrucks().get(VehIndexFrom).getRoute();
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depots!

                    for (VehIndexTo = 0; VehIndexTo <  bestRoute.getTrucks().size(); VehIndexTo++) {
                        RouteTo =   bestRoute.getTrucks().get(VehIndexTo).getRoute();
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).getMachine().getMachineType().getVolume();
                            locationFrom = RouteFrom.get(i).getLocation().getId();
                            // locationTo = RouteTo.get(i).locationId;

                            //int timeToCustomer = timeMatrix[locationFrom][locationTo];

                            //|| greedyRoute.v[VehIndexTo].CheckIfTimeFits(timeToCustomer)

                            if ((VehIndexFrom == VehIndexTo) ||  (bestRoute.getTrucks().get(VehIndexTo).CheckIfLoadFits(MovingNodeDemand) && initialTrucks.get(VehIndexTo).CheckIfTimeFits(timeMatrix[initialTrucks.get(VehIndexTo).getCurrentLocation().getId()][locationFrom] + timeMatrix[locationFrom][initialTrucks.get(VehIndexTo).getStartLocation().getId()])))
                            {
                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                {
                                    int MinusCost1 = distanceMatrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i).getLocation().getId()];
                                    int MinusCost2 = distanceMatrix[RouteFrom.get(i).getLocation().getId()][RouteFrom.get(i + 1).getLocation().getId()];
                                    int MinusCost3 = distanceMatrix[RouteTo.get(j).getLocation().getId()][RouteTo.get(j + 1).getLocation().getId()];

                                    int AddedCost1 = distanceMatrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i + 1).getLocation().getId()];
                                    int AddedCost2 = distanceMatrix[RouteTo.get(j).getLocation().getId()][RouteFrom.get(i).getLocation().getId()];
                                    int AddedCost3 = distanceMatrix[RouteFrom.get(i).getLocation().getId()][RouteTo.get(j + 1).getLocation().getId()];

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

            RouteFrom =  bestRoute.getTrucks().get(SwapRouteFrom).getRoute();
            RouteTo =  bestRoute.getTrucks().get(SwapRouteTo).getRoute();
            bestRoute.getTrucks().get(SwapRouteFrom).setRoute(null);
            bestRoute.getTrucks().get(SwapRouteTo).setRoute(null);

            Customer SwapNode = RouteFrom.get(SwapIndexA);

            int NodeIDBefore = RouteFrom.get(SwapIndexA-1).getLocation().getId();
            int NodeIDAfter = RouteFrom.get(SwapIndexA).getLocation().getId();
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


            bestRoute.getTrucks().get(SwapRouteFrom).setRoute(RouteFrom);
            bestRoute.getTrucks().get(SwapRouteFrom).lessLoad(MovingNodeDemand);

            bestRoute.getTrucks().get(SwapRouteTo).setRoute(RouteTo);
            bestRoute.getTrucks().get(SwapRouteTo).addLoad(MovingNodeDemand);

            PastSolutions.add(bestRoute.getTotalCost());

            bestRoute.setTotalCost(bestRoute.getTotalCost() + BestNCost);

            if (bestRoute.getTotalCost() < BestSolutionCost)
            {
                SaveBestSolution();
            }

            if (iteration_number == MAX_ITERATIONS)
            {
                Termination = true;
            }
            measureLoad(bestRoute);
            measureTime(bestRoute,timeMatrix);
        }

        bestRoute.setTrucks(VehiclesForBestSolution);
        bestRoute.setTotalCost(BestSolutionCost);
    }

    public void SaveBestSolution() {
        BestSolutionCost = bestRoute.getTotalCost();
        for (int j=0 ; j < bestRoute.getTrucks().size() ; j++)
        {
            VehiclesForBestSolution.get(j).getRoute().clear();
            if (! bestRoute.getTrucks().get(j).getRoute().isEmpty())
            {
                int RoutSize = bestRoute.getTrucks().get(j).getRoute().size();
                for (int k = 0; k < RoutSize ; k++) {
                    Customer n = bestRoute.getTrucks().get(j).getRoute().get(k);
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
                if(c.getMachine()!= null)
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
}