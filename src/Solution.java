import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

class Solution
{
    int NoOfVehicles;
    int NoOfCustomers;
    Vehicle[] Vehicles;
    double Cost;

    //Tabu Variables
    public Vehicle[] VehiclesForBestSolution;
    double BestSolutionCost;

    public ArrayList<Double> PastSolutions;

    Solution(int CustNum, int VechNum , int VechCap, Vehicle[] vehicles)
    {
        this.NoOfVehicles = VechNum;
        this.NoOfCustomers = CustNum;
        this.Cost = 0;
        Vehicles = new Vehicle[VechNum];
        VehiclesForBestSolution =  new Vehicle[VechNum];

        //TODO rechtstreeks vehicles doorgeven zoals hieronder werkt precies niet, reden weet ik niet nog eens bekijken!!

        //Vehicles = vehicles;
        //VehiclesForBestSolution =  vehicles;

        PastSolutions = new ArrayList<>();

        int index = 0;

        for (Vehicle v : vehicles)
        {
            Vehicles[index] = new Vehicle(index+1,200,300,v.curLoc);
            VehiclesForBestSolution[index] = new Vehicle(index+1,100,300,v.curLoc);

            Customer depot = new Customer(v.curLoc,0);
            depot.IsDepot = true;
            Vehicles[index].startLoc = v.curLoc;
            VehiclesForBestSolution[index].startLoc = v.curLoc;
            Vehicles[index].AddNode(depot,0);
            VehiclesForBestSolution[index].AddNode(depot,0);

            index++;
        }

        //SHUFFLE array met trucks om soort van random alle startlocaties te bekijken welke de kortste is
        //TODO moet nog verbeteren om alle depots te checken welke de beste start heeft!!
       // Vehicles = RandomizeArray(Vehicles);


//        for (int i = 0 ; i < NoOfVehicles; i++)
//        {
//            Vehicles[i] = new Vehicle(i+1,VechCap,0);
//            VehiclesForBestSolution[i] = new Vehicle(i+1,VechCap,0);
//        }
    }

    public Vehicle[]  RandomizeArray(Vehicle[] array){
        Random rgen = new Random(); // Random number generator
        for (int i=0; i<array.length; i++) {
            int randomPosition = rgen.nextInt(array.length);
            Vehicle temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
        return array;
    }

    // kijken als alle klanten bezocht zijn
    public boolean UnassignedCustomerExists(ArrayList<Customer> Costumers)
    {
        for(Customer costumer : Costumers)
        {
            if (!costumer.IsRouted)
                return true;
        }
        return false;
    }

    public Route GreedySolution( ArrayList<Customer> customers , int[][] distanceMatrix, int[][] timeMatrix,int index) {

        double CandCost,EndCost;

        int VehIndex = index;

        //Om nieuwe Route uit te testen alle parameters terug op nul zetten!!

        this.Cost = 0;

        for(Customer c : customers)
        {
            c.IsRouted = false;
        }

        for(Vehicle v : Vehicles)
        {
            v.Route = new ArrayList<>();
            Customer depot = new Customer(v.curLoc,0);
            // tweede parameter van addNode is hier nul omdat we de startdepo erin stoppen, dus nog niet echt gereden!
            v.AddNode(depot,0);
            v.load = 0;
        }


        /////////////////////////////////////////////////////////////////////

            //Customer depot = new Customer(0,0);
            //depot.IsDepot = true;


            // zolang niet alle klanten bezocht zijn doe verder
            while (UnassignedCustomerExists(customers)) {

                Customer Candidate = null;
                double minCost = (float) Double.MAX_VALUE;

//            if (Vehicles[VehIndex].Route.isEmpty())
//            {
//
//                Vehicles[VehIndex].AddNode(depot);
//            }

                for (Customer c : customers) {
                    if (c.IsRouted == false)    {
                        if (Vehicles[VehIndex].CheckIfFits(c.c)) {
                            if(Vehicles[VehIndex].CheckIfTimeFits(timeMatrix[Vehicles[VehIndex].curLoc][c.locationId] + timeMatrix[c.locationId][Vehicles[VehIndex].startLoc]))
                            {
                                //kijken als de tijd tussen de customers plus 40 minuten service time nog binnen het tijdsbestek van de chauffeur liggen
                                //TODO 40 automatisch inlezen!!
                                //TODO Nu wordt er niet gecontroleerd als de laatste rit, dus de rit van de laatste klant tot het depo als deze nog binnen de tijd zit.


                                CandCost = distanceMatrix[Vehicles[VehIndex].curLoc][c.locationId];
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
                    //Geen enkele Customer past
                    if (VehIndex+1 < Vehicles.length ) //We hebben nog trucks ter beschikking
                    {
                        //maximum capaciteit voor deze truck is bereikt (geen candidates), dus terugkeren naar depot.
                        // TODO nul is voorlopig de locatie van onze depot, bij meerdere depots moet dit wijzigen!!
                        if (Vehicles[VehIndex].curLoc != 0) {
                            EndCost = distanceMatrix[Vehicles[VehIndex].curLoc][Vehicles[VehIndex].startLoc];

                            Customer depot = new Customer(Vehicles[VehIndex].startLoc,0);
                            depot.IsDepot = true;

                            //get time to go to the depot
                            int t = timeMatrix[Vehicles[VehIndex].curLoc][Vehicles[VehIndex].startLoc];
                            Vehicles[VehIndex].AddNode(depot,t);


                            this.Cost +=  EndCost;
                        }
                        //Ga naar volgende truck
                        VehIndex = VehIndex+1;
                    }
                    //Wanneer we geen voldoende trucks meer hebben is dit probleem niet oplosbaar!
                    else
                    {
                        System.out.println("\nThe rest customers do not fit in any Vehicle\n" +
                                "The problem cannot be resolved under these constrains");
                        System.exit(0);
                    }
                }
                else
                {
                    //Als een nieuwe Candidate gevonden is deze toevoegen aan de truck
                    int t = timeMatrix[Vehicles[VehIndex].curLoc][Candidate.locationId];
                    Vehicles[VehIndex].AddNode(Candidate,t);
                    Candidate.IsRouted = true;
                    this.Cost += minCost;
                }
            }

            // TODO nul is voorlopig de locatie van onze depot, bij meerdere depots moet dit wijzigen!!
            EndCost = distanceMatrix[Vehicles[VehIndex].curLoc][Vehicles[VehIndex].startLoc];

            Customer depot = new Customer(Vehicles[VehIndex].startLoc,0);
            depot.IsDepot = true;
            //depot dus tweede parameter nul!
            Vehicles[VehIndex].AddNode(depot,0);


            this.Cost +=  EndCost;

            Route r = new Route(Vehicles, Cost);

        return r;
    }
    public void TabuSearch(int TABU_Horizon, int[][] costMatrix,int[][] timeMatrix, Route greedyRoute) {

        //We use 1-0 exchange move
        ArrayList<Customer> RouteFrom;
        ArrayList<Customer> RouteTo;

        int MovingNodeDemand = 0;
        int locationFrom = 0;
        int locationTo = 0;

        int VehIndexFrom,VehIndexTo;
        double BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom =-1, SwapRouteTo=-1;

        int MAX_ITERATIONS = 10000;
        int iteration_number= 0;

        int DimensionCustomer = costMatrix[1].length;
        int TABU_Matrix[][] = new int[DimensionCustomer+1][DimensionCustomer+1];

        BestSolutionCost = greedyRoute.totalCost; //Initial Solution Cost

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Double.MAX_VALUE;

            for (VehIndexFrom = 0;  VehIndexFrom <  greedyRoute.v.length;  VehIndexFrom++) {
                RouteFrom =  greedyRoute.v[VehIndexFrom].Route;
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depot!

                    for (VehIndexTo = 0; VehIndexTo <  greedyRoute.v.length; VehIndexTo++) {
                        RouteTo =   greedyRoute.v[VehIndexTo].Route;
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).c;
                            locationFrom = RouteFrom.get(i).locationId;
                           // locationTo = RouteTo.get(i).locationId;

                            //int timeToCustomer = timeMatrix[locationFrom][locationTo];

                            //|| greedyRoute.v[VehIndexTo].CheckIfTimeFits(timeToCustomer)

                            if ((VehIndexFrom == VehIndexTo) ||  (greedyRoute.v[VehIndexTo].CheckIfFits(MovingNodeDemand) && Vehicles[VehIndexTo].CheckIfTimeFits(timeMatrix[Vehicles[VehIndexTo].curLoc][locationFrom] + timeMatrix[locationFrom][Vehicles[VehIndexTo].startLoc])))
                            {
                                    if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                    {
                                        double MinusCost1 = costMatrix[RouteFrom.get(i - 1).locationId][RouteFrom.get(i).locationId];
                                        double MinusCost2 = costMatrix[RouteFrom.get(i).locationId][RouteFrom.get(i + 1).locationId];
                                        double MinusCost3 = costMatrix[RouteTo.get(j).locationId][RouteTo.get(j + 1).locationId];

                                        double AddedCost1 = costMatrix[RouteFrom.get(i - 1).locationId][RouteFrom.get(i + 1).locationId];
                                        double AddedCost2 = costMatrix[RouteTo.get(j).locationId][RouteFrom.get(i).locationId];
                                        double AddedCost3 = costMatrix[RouteFrom.get(i).locationId][RouteTo.get(j + 1).locationId];

                                        //Check if the move is a Tabu! - If it is Tabu break
                                        if ((TABU_Matrix[RouteFrom.get(i - 1).locationId][RouteFrom.get(i+1).locationId] != 0)
                                                || (TABU_Matrix[RouteTo.get(j).locationId][RouteFrom.get(i).locationId] != 0)
                                                || (TABU_Matrix[RouteFrom.get(i).locationId][RouteTo.get(j+1).locationId] != 0)) {
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

            int NodeIDBefore = RouteFrom.get(SwapIndexA-1).locationId;
            int NodeIDAfter = RouteFrom.get(SwapIndexA+1).locationId;
            int NodeID_F = RouteTo.get(SwapIndexB).locationId;
            int NodeID_G = RouteTo.get(SwapIndexB+1).locationId;

            Random TabuRan = new Random();
            int RendomDelay1 = TabuRan.nextInt(5);
            int RendomDelay2 = TabuRan.nextInt(5);
            int RendomDelay3 = TabuRan.nextInt(5);

            TABU_Matrix[NodeIDBefore][SwapNode.locationId] = TABU_Horizon + RendomDelay1;
            TABU_Matrix[SwapNode.locationId][NodeIDAfter]  = TABU_Horizon + RendomDelay2 ;
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
            greedyRoute.v[SwapRouteFrom].load -= MovingNodeDemand;

            greedyRoute.v[SwapRouteTo].Route = RouteTo;
            greedyRoute.v[SwapRouteTo].load += MovingNodeDemand;

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

        try{
            PrintWriter writer = new PrintWriter("PastSolutionsTabu.txt", "UTF-8");
            writer.println("Solutions"+"\t");
            for  (int i = 0; i< PastSolutions.size(); i++){
                writer.println(PastSolutions.get(i)+"\t");
            }
            writer.close();
        } catch (Exception e) {}
    }

    public void SaveBestSolution(Route greedyRoute)
    {
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
    public void measureLoad(Route r)
    {
        for(Vehicle v : r.v)
        {
            int load = 0;
            for(Customer c : v.Route)
            {
                load = load + c.c;
            }

            v.load = load;
        }
    }

    //Updates telkens de nieuwe workTime berekenen van een voertuig
    //TODO moet nog verbeteren is een test!!
    public void measureTime(Route r, int[][] timeMatrix)
    {
        for(Vehicle v : r.v)
        {
            int workTime = 0;
            Customer vorigeKlant = null;
            for(Customer c : v.Route)
            {
                if(vorigeKlant != null)
                {
                    workTime = workTime + timeMatrix[vorigeKlant.locationId][c.locationId];
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
        double BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom =-1, SwapRouteTo=-1;

        int MAX_ITERATIONS = 200;
        int iteration_number= 0;

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Double.MAX_VALUE;

            for (VehIndexFrom = 0;  VehIndexFrom < this.Vehicles.length;  VehIndexFrom++) {
                RouteFrom = this.Vehicles[VehIndexFrom].Route;
                int RoutFromLength = RouteFrom.size();
                //We starten bij 1 omdat het niet mogelijk is om het depot te verplaatsen
                for (int i = 1; i < RoutFromLength - 1; i++) {
                    for (VehIndexTo = 0; VehIndexTo < this.Vehicles.length; VehIndexTo++) {
                        RouteTo =  this.Vehicles[VehIndexTo].Route;
                        int RouteTolength = RouteTo.size();
                        //RouteToLength -1 omdat het niet mogelijk is te de eindbestemming te veranderen (dit is namelijk altijd een depot)
                        for (int j = 0; (j < RouteTolength - 1); j++) {

                            MovingNodeDemand = RouteFrom.get(i).c;
                            if ((VehIndexFrom == VehIndexTo) ||  this.Vehicles[VehIndexTo].CheckIfFits(MovingNodeDemand))
                            {

                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false)  // Not a move that Changes solution cost
                                {
                                    double MinusCost1 = distanceMatrix[RouteFrom.get(i - 1).locationId][RouteFrom.get(i).locationId];
                                    double MinusCost2 = distanceMatrix[RouteFrom.get(i).locationId][RouteFrom.get(i + 1).locationId];
                                    double MinusCost3 = distanceMatrix[RouteTo.get(j).locationId][RouteTo.get(j + 1).locationId];

                                    double AddedCost1 = distanceMatrix[RouteFrom.get(i - 1).locationId][RouteFrom.get(i + 1).locationId];
                                    double AddedCost2 = distanceMatrix[RouteTo.get(j).locationId][RouteFrom.get(i).locationId];
                                    double AddedCost3 = distanceMatrix[RouteFrom.get(i).locationId][RouteTo.get(j + 1).locationId];

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

                RouteFrom = this.Vehicles[SwapRouteFrom].Route;
                RouteTo = this.Vehicles[SwapRouteTo].Route;
                this.Vehicles[SwapRouteFrom].Route = null;
                this.Vehicles[SwapRouteTo].Route = null;

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

                this.Vehicles[SwapRouteFrom].Route = RouteFrom;
                this.Vehicles[SwapRouteFrom].load -= MovingNodeDemand;

                this.Vehicles[SwapRouteTo].Route = RouteTo;
                this.Vehicles[SwapRouteTo].load += MovingNodeDemand;

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




    public void IntraRouteLocalSearch(Customer[] Nodes,  double[][] CostMatrix) {

        //We use 1-0 exchange move
        ArrayList<Customer> rt;
        double BestNCost,NeigthboorCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRoute =-1;

        int MAX_ITERATIONS = 1000000;
        int iteration_number= 0;

        boolean Termination = false;

        while (!Termination)
        {
            iteration_number++;
            BestNCost = Double.MAX_VALUE;

            for (int VehIndex = 0; VehIndex < this.Vehicles.length; VehIndex++) {
                rt = this.Vehicles[VehIndex].Route;
                int RoutLength = rt.size();

                for (int i = 1; i < RoutLength - 1; i++) { //Not possible to move depot!

                    for (int j =  0 ; (j < RoutLength-1); j++) {//Not possible to move after last Depot!

                        if ( ( j != i ) && (j != i-1) ) { // Not a move that cHanges solution cost

                            double MinusCost1 = CostMatrix[rt.get(i-1).locationId][rt.get(i).locationId];
                            double MinusCost2 =  CostMatrix[rt.get(i).locationId][rt.get(i+1).locationId];
                            double MinusCost3 =  CostMatrix[rt.get(j).locationId][rt.get(j+1).locationId];

                            double AddedCost1 = CostMatrix[rt.get(i-1).locationId][rt.get(i+1).locationId];
                            double AddedCost2 = CostMatrix[rt.get(j).locationId][rt.get(i).locationId];
                            double AddedCost3 = CostMatrix[rt.get(i).locationId][rt.get(j+1).locationId];

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

                rt = this.Vehicles[SwapRoute].Route;

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

    public void SolutionPrint(String Solution_Label,Route r)//Print Solution In console
    {
        System.out.println("=========================================================");
        System.out.println(Solution_Label+"\n");

        for (int j=0 ; j < r.v.length ; j++)
        {
            if (! r.v[j].Route.isEmpty())
            {   System.out.print("Vehicle " + j + ":");
                int RoutSize = r.v[j].Route.size();
                for (int k = 0; k < RoutSize ; k++) {
                    if (k == RoutSize-1)
                    { System.out.print(r.v[j].Route.get(k).locationId );  }
                    else
                    { System.out.print(r.v[j].Route.get(k).locationId+ "->"); }
                }
                System.out.println();
            }
        }

        System.out.println("\nSolution Cost "+r.totalCost+"\n");
    }
}
