package Heuristiek;

import Objects.*;
import Main.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import static Heuristiek.Problem.machines;

public class Solution  {

    private int Cost;
    private Truck[] newTrucks;
    private Truck[] VehiclesForBestSolution;
    private int BestSolutionCost;
    private ArrayList<Integer> PastSolutions;

    public Solution(ArrayList<Truck> oldTrucks) {
        this.Cost = 0;
        this.newTrucks = new Truck[oldTrucks.size()];
        VehiclesForBestSolution =  new Truck[oldTrucks.size()];

        //TODO rechtstreeks oldTrucks doorgeven zoals hieronder werkt precies niet, reden weet ik niet nog eens bekijken!!

        //oldTrucks = oldTrucks;
        //VehiclesForBestSolution =  oldTrucks;

        PastSolutions = new ArrayList<>();

        int index = 0;

        for (Truck v : oldTrucks)
        {
            this.newTrucks[index] = new Truck(index, v.getStartLocation(), v.getEndLocation() ,v.getCurrentLocation());
            VehiclesForBestSolution[index] = new Truck(index, null, null, v.getCurrentLocation());

            Customer depot = new Customer(machines.get(0), v.getCurrentLocation(), Customer.Type.COLLECT);
            //depot.IsDepot = true;
            this.newTrucks[index].setStartLocation(v.getCurrentLocation());
            VehiclesForBestSolution[index].setStartLocation(v.getCurrentLocation());
            this.newTrucks[index].AddNode(depot,0, 0);
            VehiclesForBestSolution[index].AddNode(depot,0, 0);
            index++;
        }

        //SHUFFLE array met oldTrucks om soort van random alle startlocaties te bekijken welke de kortste is
        //TODO moet nog verbeteren om alle depots te checken welke de beste start heeft!!
    }

    public Route GreedySolution(ArrayList<Customer> customers , int[][] distanceMatrix, int[][] timeMatrix, int index) {

        int CandCost, EndCost, VehIndex = index;

        //Om nieuwe route uit te testen alle parameters terug op nul zetten!!

        this.Cost = 0;

        for(Customer customer : customers)
        {
            customer.setVisited(false);
        }

        for(Truck v : newTrucks)
        {
            v.setRoute( new ArrayList<>());
            Customer depot = new Customer(machines.get(0), v.getCurrentLocation(), Customer.Type.COLLECT);
            // tweede parameter van addNode is hier nul omdat we de startdepo erin stoppen, dus nog niet echt gereden!
            v.AddNode(depot,0, 0);
            v.setCurrentLoad(0);
        }


        // True zolang niet alle klanten bezocht zijn
        while (VisitedAllCustomers(customers)) {

            Customer Candidate = null;
            int minCost = Integer.MAX_VALUE;

            for (Customer customer : customers) {
                if (!customer.isVisited())    {
                    if (newTrucks[VehIndex].CheckIfFits(customer.getMachine().getMachineType().getVolume())) {
                        // TODO: aanpassing enkel voor COLLECT!!!!!!!!
                        if(newTrucks[VehIndex].CheckIfTimeFits(timeMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][customer.getLocation().getId()] + timeMatrix[customer.getLocation().getId()][newTrucks[VehIndex].getStartLocation().getId()]))
                        {
                            //kijken als de tijd tussen de customers plus 40 minuten service time nog binnen het tijdsbestek van de chauffeur liggen
                            //TODO 40 automatisch inlezen!!
                            //TODO Nu wordt er niet gecontroleerd als de laatste rit, dus de rit van de laatste klant tot het depo als deze nog binnen de tijd zit.

                            CandCost = distanceMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][customer.getLocation().getId()];
                            if (minCost > CandCost) {
                                minCost = CandCost;
                                Candidate = customer;
                            }
                        }
                    }
                }
            }

            if (Candidate  == null)
            {
                //Geen enkele customer past
                if (VehIndex + 1 < newTrucks.length ) //We hebben nog allTrucks ter beschikking
                {
                    //maximum capaciteit voor deze truck is bereikt (geen candidates), dus terugkeren naar depots.
                    // TODO nul is voorlopig de locatie van onze depots, bij meerdere depots moet dit wijzigen!!
                    if (newTrucks[VehIndex].getCurrentLocation().getId() != 0) {
                        EndCost = distanceMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][newTrucks[VehIndex].getStartLocation().getId()];

                        Customer depot = new Customer(machines.get(0), newTrucks[VehIndex].getStartLocation(), Customer.Type.COLLECT);
                       // depot.IsDepot = true;

                        //get time to go to the depots
                        int time = timeMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][newTrucks[VehIndex].getStartLocation().getId()];
                        int distance = distanceMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][newTrucks[VehIndex].getStartLocation().getId()];
                        newTrucks[VehIndex].AddNode(depot,time, distance);


                        this.Cost +=  EndCost;
                    }
                    //Ga naar volgende truck
                    VehIndex = VehIndex+1;
                }
                //Wanneer we geen voldoende newTrucks meer hebben is dit probleem niet oplosbaar!
                else
                {
                    System.out.println("\nNiet voldoende allTrucks beschikbaar voor het probleem");
                    System.exit(0);
                }
            }
            else
            {
                //Als een nieuwe Candidate gevonden is deze toevoegen aan de truck
                int time = timeMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][Candidate.getLocation().getId()];
                int distance = distanceMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][Candidate.getLocation().getId()];
                newTrucks[VehIndex].AddNode(Candidate,time, distance);
                Candidate.setVisited(true);
                this.Cost += minCost;
            }
        }

        // TODO nul is voorlopig de locatie van onze depots, bij meerdere depots moet dit wijzigen!!
        EndCost = distanceMatrix[newTrucks[VehIndex].getCurrentLocation().getId()][newTrucks[VehIndex].getStartLocation().getId()];

        Customer depot = new Customer(machines.get(0), newTrucks[VehIndex].getStartLocation(), Customer.Type.COLLECT);
        //depot.IsDepot = true;
        //depots dus tweede parameter nul!
        newTrucks[VehIndex].AddNode(depot,0, 0);


        this.Cost +=  EndCost;

        Route r = new Route(newTrucks, Cost);

        return r;
    }

    // Kijken als alle klanten bezocht zijn
    public boolean VisitedAllCustomers(ArrayList<Customer> Costumers) {
        for(Customer costumer : Costumers)
        {
            if (!costumer.isVisited())
                return true;
        }
        return false;
    }

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

        BestSolutionCost = greedyRoute.getTotalCost(); //Initial Heuristiek.Solution Cost

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Integer.MAX_VALUE;

            for (VehIndexFrom = 0;  VehIndexFrom <  greedyRoute.getTrucks().length;  VehIndexFrom++) {
                RouteFrom =  greedyRoute.getTrucks()[VehIndexFrom].getRoute();
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depots!

                    for (VehIndexTo = 0; VehIndexTo <  greedyRoute.getTrucks().length; VehIndexTo++) {
                        RouteTo =   greedyRoute.getTrucks()[VehIndexTo].getRoute();
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).getMachine().getMachineType().getVolume();
                            locationFrom = RouteFrom.get(i).getLocation().getId();
                           // locationTo = RouteTo.get(i).locationId;

                            //int timeToCustomer = timeMatrix[locationFrom][locationTo];

                            //|| greedyRoute.v[VehIndexTo].CheckIfTimeFits(timeToCustomer)

                            if ((VehIndexFrom == VehIndexTo) ||  (greedyRoute.getTrucks()[VehIndexTo].CheckIfFits(MovingNodeDemand) && newTrucks[VehIndexTo].CheckIfTimeFits(timeMatrix[newTrucks[VehIndexTo].getCurrentLocation().getId()][locationFrom] + timeMatrix[locationFrom][newTrucks[VehIndexTo].getStartLocation().getId()])))
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

            RouteFrom =  greedyRoute.getTrucks()[SwapRouteFrom].getRoute();
            RouteTo =  greedyRoute.getTrucks()[SwapRouteTo].getRoute();
            greedyRoute.getTrucks()[SwapRouteFrom].setRoute(null);
            greedyRoute.getTrucks()[SwapRouteTo].setRoute(null);

            Customer SwapNode = RouteFrom.get(SwapIndexA);

            // todo VERLOPPIGE AANPASSING (NODEIDAFTER +1)
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


            greedyRoute.getTrucks()[SwapRouteFrom].setRoute(RouteFrom);
            greedyRoute.getTrucks()[SwapRouteFrom].lessLoad(MovingNodeDemand);

            greedyRoute.getTrucks()[SwapRouteTo].setRoute(RouteTo);
            greedyRoute.getTrucks()[SwapRouteTo].addLoad(MovingNodeDemand);

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
        for (int j=0 ; j < greedyRoute.getTrucks().length ; j++)
        {
            VehiclesForBestSolution[j].getRoute().clear();
            if (! greedyRoute.getTrucks()[j].getRoute().isEmpty())
            {
                int RoutSize = greedyRoute.getTrucks()[j].getRoute().size();
                for (int k = 0; k < RoutSize ; k++) {
                    Customer n = greedyRoute.getTrucks()[j].getRoute().get(k);
                    VehiclesForBestSolution[j].getRoute().add(n);
                }
            }
        }
    }

    //Updates telkens de nieuwe curLoad van een voertuig
    //TODO moet nog verbeteren is een test!!
    public void measureLoad(Route r) {
        for(Truck v : r.getTrucks())
        {
            int load = 0;
            for(Customer c : v.getRoute())
            {
                load = load + c.getMachine().getMachineType().getVolume();
            }

            v.setCurrentLoad(load);
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

    public void InterRouteLocalSearch(int[][] distanceMatrix,int[][] timeMatrix) {
        //We use 1-0 exchange move
        ArrayList<Customer> RouteFrom;
        ArrayList<Customer> RouteTo;

        int MovingNodeDemand = 0;
        int MovingNodeLocation = 0;
        int VehIndexFrom,VehIndexTo;
        int BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom =-1, SwapRouteTo=-1;

        int MAX_ITERATIONS = 200;
        int iteration_number= 0;

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Integer.MAX_VALUE;

            for (VehIndexFrom = 0; VehIndexFrom < this.newTrucks.length; VehIndexFrom++) {
                RouteFrom = this.newTrucks[VehIndexFrom].getRoute();
                int RoutFromLength = RouteFrom.size();
                //We starten bij 1 omdat het niet mogelijk is om het depots te verplaatsen
                for (int i = 1; i < RoutFromLength - 1; i++) {
                    for (VehIndexTo = 0; VehIndexTo < this.newTrucks.length; VehIndexTo++) {
                        RouteTo =  this.newTrucks[VehIndexTo].getRoute();
                        int RouteTolength = RouteTo.size();
                        //RouteToLength -1 omdat het niet mogelijk is te de eindbestemming te veranderen (dit is namelijk altijd een depots)
                        for (int j = 0; (j < RouteTolength - 1); j++) {

                            MovingNodeDemand = RouteFrom.get(i).getMachine().getMachineType().getVolume();
                            if ((VehIndexFrom == VehIndexTo) ||  this.newTrucks[VehIndexTo].CheckIfFits(MovingNodeDemand))
                            {

                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                {
                                    int MinusCost1 = distanceMatrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i).getLocation().getId()];
                                    int MinusCost2 = distanceMatrix[RouteFrom.get(i).getLocation().getId()][RouteFrom.get(i + 1).getLocation().getId()];
                                    int MinusCost3 = distanceMatrix[RouteTo.get(j).getLocation().getId()][RouteTo.get(j + 1).getLocation().getId()];

                                    int AddedCost1 = distanceMatrix[RouteFrom.get(i - 1).getLocation().getId()][RouteFrom.get(i + 1).getLocation().getId()];
                                    int AddedCost2 = distanceMatrix[RouteTo.get(j).getLocation().getId()][RouteFrom.get(i).getLocation().getId()];
                                    int AddedCost3 = distanceMatrix[RouteFrom.get(i).getLocation().getId()][RouteTo.get(j + 1).getLocation().getId()];

                                    NeigthboorCost = AddedCost1 + AddedCost2 + AddedCost3 - MinusCost1 - MinusCost2 - MinusCost3;

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

            // If Best Neightboor Cost is better than the current
            if (BestNCost < 0) {

                RouteFrom = this.newTrucks[SwapRouteFrom].getRoute();
                RouteTo = this.newTrucks[SwapRouteTo].getRoute();
                this.newTrucks[SwapRouteFrom].setRoute(null);
                this.newTrucks[SwapRouteTo].setRoute(null);

                Customer SwapNode = RouteFrom.get(SwapIndexA);

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

                this.newTrucks[SwapRouteFrom].setRoute(RouteFrom);
                this.newTrucks[SwapRouteFrom].lessLoad(MovingNodeDemand);

                this.newTrucks[SwapRouteTo].setRoute(RouteTo);
                this.newTrucks[SwapRouteTo].addLoad(MovingNodeDemand);

                PastSolutions.add(this.Cost);
                this.Cost  += BestNCost;
            }
            else{
                Termination = true;
            }

            if (iteration_number == MAX_ITERATIONS)
            {
                Termination = true;
            }
        }
        PastSolutions.add(this.Cost);

        try{
            PrintWriter writer = new PrintWriter("PastSolutionsInter.txt", "UTF-8");
            for  (int i = 0; i< PastSolutions.size(); i++){
                writer.println(PastSolutions.get(i)+"\t");
            }
            writer.close();
        } catch (Exception e) {}
    }

    public void IntraRouteLocalSearch(Customer[] Nodes, int[][] CostMatrix) {
        //We use 1-0 exchange move
        ArrayList<Customer> rt;
        int BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRoute =-1;

        int MAX_ITERATIONS = 1000000;
        int iteration_number= 0;

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Integer.MAX_VALUE;

            for (int VehIndex = 0; VehIndex < this.newTrucks.length; VehIndex++) {
                rt = this.newTrucks[VehIndex].getRoute();
                int RoutLength = rt.size();

                for (int i = 1; i < RoutLength - 1; i++) { //Not possible to move depots!

                    for (int j =  0 ; (j < RoutLength-1); j++) {//Not possible to move after last Depot!

                        if ( ( j != i ) && (j != i-1) ) { // Not a move that cHanges solution cost

                            int MinusCost1 = CostMatrix[rt.get(i-1).getLocation().getId()][rt.get(i).getLocation().getId()];
                            int MinusCost2 =  CostMatrix[rt.get(i).getLocation().getId()][rt.get(i+1).getLocation().getId()];
                            int MinusCost3 =  CostMatrix[rt.get(j).getLocation().getId()][rt.get(j+1).getLocation().getId()];

                            int AddedCost1 = CostMatrix[rt.get(i-1).getLocation().getId()][rt.get(i+1).getLocation().getId()];
                            int AddedCost2 = CostMatrix[rt.get(j).getLocation().getId()][rt.get(i).getLocation().getId()];
                            int AddedCost3 = CostMatrix[rt.get(i).getLocation().getId()][rt.get(j+1).getLocation().getId()];

                            NeigthboorCost = AddedCost1 + AddedCost2 + AddedCost3
                                    - MinusCost1 - MinusCost2 - MinusCost3;

                            if (NeigthboorCost < BestNCost) {
                                BestNCost = NeigthboorCost;
                                SwapIndexA  = i;
                                SwapIndexB  = j;
                                SwapRoute = VehIndex;

                            }
                        }
                    }
                }
            }

            if (BestNCost < 0) {

                rt = this.newTrucks[SwapRoute].getRoute();

                Customer SwapNode = rt.get(SwapIndexA);

                rt.remove(SwapIndexA);

                if (SwapIndexA < SwapIndexB)
                { rt.add(SwapIndexB, SwapNode); }
                else
                { rt.add(SwapIndexB+1, SwapNode); }

                PastSolutions.add(this.Cost);
                this.Cost  += BestNCost;
            }
            else{
                Termination = true;
            }

            if (iteration_number == MAX_ITERATIONS)
            {
                Termination = true;
            }
        }
        PastSolutions.add(this.Cost);

        try{
            PrintWriter writer = new PrintWriter("PastSolutionsIntra.txt", "UTF-8");
            for  (int i = 0; i< PastSolutions.size(); i++){
                writer.println(PastSolutions.get(i)+"\t");
            }
            writer.close();
        } catch (Exception e) {}
    }

    public void SolutionPrint(String Solution_Label, Route r) {
        System.out.println("\n" + Solution_Label + "\n");

        for (Truck truck : r.getTrucks()) {
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
        System.out.println("\nTotale afstand: "+r.getTotalCost()+"\n");
    }

    public void WriteFile(Route r) throws IOException {

        // Berekenen hoeveel newTrucks er effectief in dienst zijn
        int numberOfUsedTrucks = 0;
        for (Truck truck : r.getTrucks()) {
            if(truck.getRoute().size() > 1){
                numberOfUsedTrucks++;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.OUTPUT_FILE));
        writer.write("PROBLEM: " + Main.INPUT_FILE + "\n");
        writer.write("DISTANCE: " + r.getTotalCost() + "\n");
        writer.write("TRUCKS: " + numberOfUsedTrucks + "\n");

        for (Truck truck : r.getTrucks()) {
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
}
