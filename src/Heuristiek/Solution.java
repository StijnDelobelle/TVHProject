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
    int NoOfVehicles;
    int NoOfCustomers;
    Truck[] trucks;
    int Cost;

    //Tabu Variables
    public Truck[] VehiclesForBestSolution;
    int BestSolutionCost;

    public ArrayList<Integer> PastSolutions;

    Solution(int CustNum, int VechNum , int VechCap, ArrayList<Truck> trucks) {
        this.NoOfVehicles = VechNum;
        this.NoOfCustomers = CustNum;
        this.Cost = 0;
        this.trucks = new Truck[VechNum];
        VehiclesForBestSolution =  new Truck[VechNum];

        //TODO rechtstreeks trucks doorgeven zoals hieronder werkt precies niet, reden weet ik niet nog eens bekijken!!

        //trucks = trucks;
        //VehiclesForBestSolution =  trucks;

        PastSolutions = new ArrayList<>();

        int index = 0;

        for (Truck v : trucks)
        {
            this.trucks[index] = new Truck(index, v.startLocation, v.endLocation ,v.currentLocation);
            VehiclesForBestSolution[index] = new Truck(index, null, null, v.currentLocation);

            Customer depot = new Customer(machines.get(0), v.currentLocation, Customer.Type.COLLECT);
            depot.IsDepot = true;
            this.trucks[index].startLocation = v.currentLocation;
            VehiclesForBestSolution[index].startLocation = v.currentLocation;
            this.trucks[index].AddNode(depot,0, 0);
            VehiclesForBestSolution[index].AddNode(depot,0, 0);
            index++;
        }

        //SHUFFLE array met trucks om soort van random alle startlocaties te bekijken welke de kortste is
        //TODO moet nog verbeteren om alle depots te checken welke de beste start heeft!!
       // trucks = RandomizeArray(trucks);


//        for (int i = 0 ; i < NoOfVehicles; i++)
//        {
//            trucks[i] = new Objects.Truck(i+1,VechCap,0);
//            VehiclesForBestSolution[i] = new Objects.Truck(i+1,VechCap,0);
//        }
    }

    public Truck[]  RandomizeArray(Truck[] array){
        Random rgen = new Random(); // Random number generator
        for (int i=0; i<array.length; i++) {
            int randomPosition = rgen.nextInt(array.length);
            Truck temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
        return array;
    }

    // kijken als alle klanten bezocht zijn
    public boolean UnassignedCustomerExists(ArrayList<Customer> Costumers) {
        for(Customer costumer : Costumers)
        {
            if (!costumer.IsRouted)
                return true;
        }
        return false;
    }

    public Route GreedySolution(ArrayList<Customer> customers , int[][] distanceMatrix, int[][] timeMatrix, int index) {

        int CandCost,EndCost;

        int VehIndex = index;

        //Om nieuwe Objects.Route uit te testen alle parameters terug op nul zetten!!

        this.Cost = 0;

        for(Customer c : customers)
        {
            c.IsRouted = false;
        }

        for(Truck v : trucks)
        {
            v.Route = new ArrayList<>();
            Customer depot = new Customer(machines.get(0), v.currentLocation, Customer.Type.COLLECT);
            // tweede parameter van addNode is hier nul omdat we de startdepo erin stoppen, dus nog niet echt gereden!
            v.AddNode(depot,0, 0);
            v.actualLoad = 0;
        }


        /////////////////////////////////////////////////////////////////////

            //Objects.Customer depots = new Objects.Customer(0,0);
            //depots.IsDepot = true;


            // zolang niet alle klanten bezocht zijn doe verder
            while (UnassignedCustomerExists(customers)) {

                Customer Candidate = null;
                int minCost = Integer.MAX_VALUE;

//            if (trucks[VehIndex].Objects.Route.isEmpty())
//            {
//
//                trucks[VehIndex].AddNode(depots);
//            }


                for (Customer c : customers) {
                    if (c.IsRouted == false)    {
                        if (trucks[VehIndex].CheckIfFits(c.machine.getMachineType().getVolume())) {
                            // TODO: aanpassing enkel voor COLLECT!!!!!!!!
                            if(trucks[VehIndex].CheckIfTimeFits(timeMatrix[trucks[VehIndex].currentLocation.getId()][c.location.getId()] + timeMatrix[c.location.getId()][trucks[VehIndex].startLocation.getId()]))
                            {
                                //kijken als de tijd tussen de customers plus 40 minuten service time nog binnen het tijdsbestek van de chauffeur liggen
                                //TODO 40 automatisch inlezen!!
                                //TODO Nu wordt er niet gecontroleerd als de laatste rit, dus de rit van de laatste klant tot het depo als deze nog binnen de tijd zit.

                                CandCost = distanceMatrix[trucks[VehIndex].currentLocation.getId()][c.location.getId()];
                                if (minCost > CandCost) {
                                    minCost = CandCost;
                                    Candidate = c;
                                }
                            }
                        }
                    }
                }

                if (Candidate  == null)
                {
                    //Geen enkele Objects.Customer past
                    if (VehIndex+1 < trucks.length ) //We hebben nog trucks ter beschikking
                    {
                        //maximum capaciteit voor deze truck is bereikt (geen candidates), dus terugkeren naar depots.
                        // TODO nul is voorlopig de locatie van onze depots, bij meerdere depots moet dit wijzigen!!
                        if (trucks[VehIndex].currentLocation.getId() != 0) {
                            EndCost = distanceMatrix[trucks[VehIndex].currentLocation.getId()][trucks[VehIndex].startLocation.getId()];

                            Customer depot = new Customer(machines.get(0), trucks[VehIndex].startLocation, Customer.Type.COLLECT);
                            depot.IsDepot = true;

                            //get time to go to the depots
                            int time = timeMatrix[trucks[VehIndex].currentLocation.getId()][trucks[VehIndex].startLocation.getId()];
                            int distance = distanceMatrix[trucks[VehIndex].currentLocation.getId()][trucks[VehIndex].startLocation.getId()];
                            trucks[VehIndex].AddNode(depot,time, distance);


                            this.Cost +=  EndCost;
                        }
                        //Ga naar volgende truck
                        VehIndex = VehIndex+1;
                    }
                    //Wanneer we geen voldoende trucks meer hebben is dit probleem niet oplosbaar!
                    else
                    {
                        System.out.println("\nThe rest customers do not fit in any Objects.Truck\n" +
                                "The problem cannot be resolved under these constrains");
                        System.exit(0);
                    }
                }
                else
                {
                    //Als een nieuwe Candidate gevonden is deze toevoegen aan de truck
                    int time = timeMatrix[trucks[VehIndex].currentLocation.getId()][Candidate.location.getId()];
                    int distance = distanceMatrix[trucks[VehIndex].currentLocation.getId()][Candidate.location.getId()];
                    trucks[VehIndex].AddNode(Candidate,time, distance);
                    Candidate.IsRouted = true;
                    this.Cost += minCost;
                }
            }

            // TODO nul is voorlopig de locatie van onze depots, bij meerdere depots moet dit wijzigen!!
            EndCost = distanceMatrix[trucks[VehIndex].currentLocation.getId()][trucks[VehIndex].startLocation.getId()];

            Customer depot = new Customer(machines.get(0), trucks[VehIndex].startLocation, Customer.Type.COLLECT);
            depot.IsDepot = true;
            //depots dus tweede parameter nul!
            trucks[VehIndex].AddNode(depot,0, 0);


            this.Cost +=  EndCost;

            Route r = new Route(trucks, Cost);

        return r;
    }

    /*
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

        BestSolutionCost = greedyRoute.totalCost; //Initial Heuristiek.Solution Cost

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Integer.MAX_VALUE;

            for (VehIndexFrom = 0;  VehIndexFrom <  greedyRoute.v.length;  VehIndexFrom++) {
                RouteFrom =  greedyRoute.v[VehIndexFrom].Route;
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depots!

                    for (VehIndexTo = 0; VehIndexTo <  greedyRoute.v.length; VehIndexTo++) {
                        RouteTo =   greedyRoute.v[VehIndexTo].Route;
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).machine.getMachineType().getVolume();
                            locationFrom = RouteFrom.get(i).location.getId();
                           // locationTo = RouteTo.get(i).locationId;

                            //int timeToCustomer = timeMatrix[locationFrom][locationTo];

                            //|| greedyRoute.v[VehIndexTo].CheckIfTimeFits(timeToCustomer)

                            if ((VehIndexFrom == VehIndexTo) ||  (greedyRoute.v[VehIndexTo].CheckIfFits(MovingNodeDemand) && trucks[VehIndexTo].CheckIfTimeFits(timeMatrix[trucks[VehIndexTo].currentLocation.getId()][locationFrom] + timeMatrix[locationFrom][trucks[VehIndexTo].startLocation.getId()])))
                            {
                                    if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                    {
                                        int MinusCost1 = costMatrix[RouteFrom.get(i - 1).location.getId()][RouteFrom.get(i).location.getId()];
                                        int MinusCost2 = costMatrix[RouteFrom.get(i).location.getId()][RouteFrom.get(i + 1).location.getId()];
                                        int MinusCost3 = costMatrix[RouteTo.get(j).location.getId()][RouteTo.get(j + 1).location.getId()];

                                        int AddedCost1 = costMatrix[RouteFrom.get(i - 1).location.getId()][RouteFrom.get(i + 1).location.getId()];
                                        int AddedCost2 = costMatrix[RouteTo.get(j).location.getId()][RouteFrom.get(i).location.getId()];
                                        int AddedCost3 = costMatrix[RouteFrom.get(i).location.getId()][RouteTo.get(j + 1).location.getId()];

                                        //Check if the move is a Tabu! - If it is Tabu break
                                        if ((TABU_Matrix[RouteFrom.get(i - 1).location.getId()][RouteFrom.get(i+1).location.getId()] != 0)
                                                || (TABU_Matrix[RouteTo.get(j).location.getId()][RouteFrom.get(i).location.getId()] != 0)
                                                || (TABU_Matrix[RouteFrom.get(i).location.getId()][RouteTo.get(j+1).location.getId()] != 0)) {
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

            RouteFrom =  greedyRoute.v[SwapRouteFrom].Route;
            RouteTo =  greedyRoute.v[SwapRouteTo].Route;
            greedyRoute.v[SwapRouteFrom].Route = null;
            greedyRoute.v[SwapRouteTo].Route = null;

            Customer SwapNode = RouteFrom.get(SwapIndexA);

            // todo VERLOPPIGE AANPASSING (NODEIDAFTER +1)
            int NodeIDBefore = RouteFrom.get(SwapIndexA-1).location.getId();
            int NodeIDAfter = RouteFrom.get(SwapIndexA).location.getId();
            int NodeID_F = RouteTo.get(SwapIndexB).location.getId();
            int NodeID_G = RouteTo.get(SwapIndexB+1).location.getId();

            Random TabuRan = new Random();
            int RendomDelay1 = TabuRan.nextInt(5);
            int RendomDelay2 = TabuRan.nextInt(5);
            int RendomDelay3 = TabuRan.nextInt(5);

            TABU_Matrix[NodeIDBefore][SwapNode.location.getId()] = TABU_Horizon + RendomDelay1;
            TABU_Matrix[SwapNode.location.getId()][NodeIDAfter]  = TABU_Horizon + RendomDelay2 ;
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


            greedyRoute.v[SwapRouteFrom].Route = RouteFrom;
            greedyRoute.v[SwapRouteFrom].actualLoad -= MovingNodeDemand;

            greedyRoute.v[SwapRouteTo].Route = RouteTo;
            greedyRoute.v[SwapRouteTo].actualLoad += MovingNodeDemand;

            PastSolutions.add(greedyRoute.totalCost);

            greedyRoute.totalCost += BestNCost;

            if (greedyRoute.totalCost <   BestSolutionCost)
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

        greedyRoute.v = VehiclesForBestSolution;
        greedyRoute.totalCost = BestSolutionCost;
    }
    */

    public void SaveBestSolution(Route greedyRoute) {
        BestSolutionCost = greedyRoute.totalCost;
        for (int j=0 ; j < greedyRoute.v.length ; j++)
        {
            VehiclesForBestSolution[j].Route.clear();
            if (! greedyRoute.v[j].Route.isEmpty())
            {
                int RoutSize = greedyRoute.v[j].Route.size();
                for (int k = 0; k < RoutSize ; k++) {
                    Customer n = greedyRoute.v[j].Route.get(k);
                    VehiclesForBestSolution[j].Route.add(n);
                }
            }
        }
    }

    //Updates telkens de nieuwe curLoad van een voertuig
    //TODO moet nog verbeteren is een test!!
    public void measureLoad(Route r) {
        for(Truck v : r.v)
        {
            int load = 0;
            for(Customer c : v.Route)
            {
                load = load + c.machine.getMachineType().getVolume();
            }

            v.actualLoad = load;
        }
    }

    //Updates telkens de nieuwe workTime berekenen van een voertuig
    //TODO moet nog verbeteren is een test!!
    public void measureTime(Route r, int[][] timeMatrix) {
        for(Truck v : r.v)
        {
            int workTime = 0;
            Customer vorigeKlant = null;
            for(Customer c : v.Route)
            {
                if(vorigeKlant != null)
                {
                    workTime = workTime + timeMatrix[vorigeKlant.location.getId()][c.location.getId()];
                }

                vorigeKlant = c;
            }

            v.curWorkTime = workTime;
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

            for (VehIndexFrom = 0; VehIndexFrom < this.trucks.length; VehIndexFrom++) {
                RouteFrom = this.trucks[VehIndexFrom].Route;
                int RoutFromLength = RouteFrom.size();
                //We starten bij 1 omdat het niet mogelijk is om het depots te verplaatsen
                for (int i = 1; i < RoutFromLength - 1; i++) {
                    for (VehIndexTo = 0; VehIndexTo < this.trucks.length; VehIndexTo++) {
                        RouteTo =  this.trucks[VehIndexTo].Route;
                        int RouteTolength = RouteTo.size();
                        //RouteToLength -1 omdat het niet mogelijk is te de eindbestemming te veranderen (dit is namelijk altijd een depots)
                        for (int j = 0; (j < RouteTolength - 1); j++) {

                            MovingNodeDemand = RouteFrom.get(i).machine.getMachineType().getVolume();
                            if ((VehIndexFrom == VehIndexTo) ||  this.trucks[VehIndexTo].CheckIfFits(MovingNodeDemand))
                            {

                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                {
                                    int MinusCost1 = distanceMatrix[RouteFrom.get(i - 1).location.getId()][RouteFrom.get(i).location.getId()];
                                    int MinusCost2 = distanceMatrix[RouteFrom.get(i).location.getId()][RouteFrom.get(i + 1).location.getId()];
                                    int MinusCost3 = distanceMatrix[RouteTo.get(j).location.getId()][RouteTo.get(j + 1).location.getId()];

                                    int AddedCost1 = distanceMatrix[RouteFrom.get(i - 1).location.getId()][RouteFrom.get(i + 1).location.getId()];
                                    int AddedCost2 = distanceMatrix[RouteTo.get(j).location.getId()][RouteFrom.get(i).location.getId()];
                                    int AddedCost3 = distanceMatrix[RouteFrom.get(i).location.getId()][RouteTo.get(j + 1).location.getId()];

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

                RouteFrom = this.trucks[SwapRouteFrom].Route;
                RouteTo = this.trucks[SwapRouteTo].Route;
                this.trucks[SwapRouteFrom].Route = null;
                this.trucks[SwapRouteTo].Route = null;

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

                this.trucks[SwapRouteFrom].Route = RouteFrom;
                this.trucks[SwapRouteFrom].actualLoad -= MovingNodeDemand;

                this.trucks[SwapRouteTo].Route = RouteTo;
                this.trucks[SwapRouteTo].actualLoad += MovingNodeDemand;

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

            for (int VehIndex = 0; VehIndex < this.trucks.length; VehIndex++) {
                rt = this.trucks[VehIndex].Route;
                int RoutLength = rt.size();

                for (int i = 1; i < RoutLength - 1; i++) { //Not possible to move depots!

                    for (int j =  0 ; (j < RoutLength-1); j++) {//Not possible to move after last Depot!

                        if ( ( j != i ) && (j != i-1) ) { // Not a move that cHanges solution cost

                            int MinusCost1 = CostMatrix[rt.get(i-1).location.getId()][rt.get(i).location.getId()];
                            int MinusCost2 =  CostMatrix[rt.get(i).location.getId()][rt.get(i+1).location.getId()];
                            int MinusCost3 =  CostMatrix[rt.get(j).location.getId()][rt.get(j+1).location.getId()];

                            int AddedCost1 = CostMatrix[rt.get(i-1).location.getId()][rt.get(i+1).location.getId()];
                            int AddedCost2 = CostMatrix[rt.get(j).location.getId()][rt.get(i).location.getId()];
                            int AddedCost3 = CostMatrix[rt.get(i).location.getId()][rt.get(j+1).location.getId()];

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

                rt = this.trucks[SwapRoute].Route;

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

    public void SolutionPrint(String Solution_Label, Route r){
        System.out.println("=========================================================");
        System.out.println(Solution_Label+"\n");

        for (Truck truck : r.v) {
            if(truck.Route.size() > 1) {
                System.out.print(truck.getID() + " " + truck.curWorkTime + " " + truck.curDistance);

                // Per locatie alle machine veranderingen op de truck
                int currentLocationID = -1;
                for (Customer customer: truck.Route) {
                    if(currentLocationID != customer.location.getId())
                    {
                        currentLocationID = customer.location.getId();
                        System.out.print(" " + customer.location.getId() + ":" + customer.machine.getId());
                    }
                    else
                    {
                        System.out.print(":" + customer.machine.getId());
                    }
                }
                System.out.print("\n");
            }
        }

        // CONSOLE WRITE VAN MARTIJN! (vroeger)
        /*for (int j=0 ; j < r.v.length ; j++)
        {
            if (! r.v[j].Route.isEmpty())
            {   System.out.print("Objects.Truck " + j + ":");
                int RoutSize = r.v[j].Route.size();
                for (int k = 0; k < RoutSize ; k++) {
                    if (k == RoutSize-1)
                    { System.out.print(r.v[j].Route.get(k).location.getId() );  }
                    else
                    { System.out.print(r.v[j].Route.get(k).location.getId()+ "->"); }
                }
                System.out.println();
            }
        }*/

        System.out.println("\nSolution Cost "+r.totalCost+"\n");
    }

    public void WriteFile(Route r) throws IOException {

        // Berekenen hoeveel trucks er effectief in dienst zijn
        int numberOfUsedTrucks = 0;
        for (Truck truck : r.v) {
            if(truck.Route.size() > 1){
                numberOfUsedTrucks++;
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.OUTPUT_FILE));
        writer.write("PROBLEM: " + Main.INPUT_FILE + "\n");
        writer.write("DISTANCE: " + r.totalCost + "\n");
        writer.write("TRUCKS: " + numberOfUsedTrucks + "\n");

        for (Truck truck : r.v) {
            if(truck.Route.size() > 1) {
                writer.write(truck.getID() + " " + truck.curWorkTime + " " + truck.curDistance);

                // Per locatie alle machine veranderingen op de truck
                int currentLocationID = -1;
                for (Customer customer: truck.Route) {
                    if(currentLocationID != customer.location.getId())
                    {
                        currentLocationID = customer.location.getId();
                        writer.write(" " + customer.location.getId() + ":" + customer.machine.getId());
                    }
                    else
                    {
                        writer.write(":" + customer.machine.getId());
                    }
                }
                writer.write("\n");
            }
        }
        writer.close();
    }
}
