package Heuristiek;

import Data.FileIO;
import Data.Log;
import Objects.*;
import Main.*;

import java.io.*;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import static Heuristiek.Problem.*;
import static Main.Main.RANDOM_SEED;
import static Main.Main.TIME_LIMIT;

public class Solution   {

    private Route route;
    private ArrayList<Location> depots;
    private ArrayList<Truck> initialTrucks;
    private static final Logger logger = Logger.getLogger(Solution.class.getName());

    ConsoleHandler handler = new ConsoleHandler();

    private static final Random random = new Random(RANDOM_SEED);

    public void zetStartStops(ArrayList<Truck> initialTrucks) {
        for(Truck truck: initialTrucks)
        {
            Stop stopStart = new Stop(truck.getStartLocation(),null,Request.Type.START);

            truck.addStopToRoute(stopStart);
        }
    }

    public void InitialSolution(ArrayList<Request> argRequests, ArrayList<Truck> argTrucks, ArrayList<Location> depots) {

        this.depots = depots;
        initialTrucks = (ArrayList<Truck>) deepClone(argTrucks);

        zetStartStops(initialTrucks);

        // Alle collect requests die nodig zijn, key => machineID
        HashMap<Integer, Request> collects = new HashMap<>();
        for(Request request : requests) {
            if (request.getType() == Request.Type.COLLECT)
                collects.put(request.getMachine().getId(), request);
        }

        for (Request request : requests)
        {
            // Is de request al gedaan doordat een drop request al de machine heeft meegepakt onderweg?
            if(!request.isDone()) {

                Rit rit = null;

                /****** DROPS ******/
                if (request.getType() == Request.Type.DROP) {

                    // Alle locaties zoeken waar dat type machine staat
                    ArrayList<Location> potentialPickupLocations = new ArrayList<>();
                    for (Machine machine : machines) {
                        if (machine.getMachineType().getId() == request.getMachineType().getId() && !machine.isLocked()) {
                            potentialPickupLocations.add(machine.getLocation());
                        }
                    }

                    // Kortste afstand tussen pickuplocatie en droplocatie zoeken
                    Location pickupLocation = SearchClosestPickupLocation(potentialPickupLocations, request.getLocation());
                    Machine pickupMachine = machines.stream()
                            .filter(s -> s.getLocation().getId() == pickupLocation.getId() && s.getMachineType().getId() == request.getMachineType().getId() && s.isLocked() == false)
                            .findFirst().get();

                    // Indien een collect al gebeurt is voor een drop, verwijder deze uit de collects die nog gedaan moeten worden!
                    if (collects.containsKey(pickupMachine.getId())) {
                        requests.get(collects.get(pickupMachine.getId()).getId()).setDone(true);
                    }

                    rit = new Rit(pickupLocation,request.getLocation(),pickupMachine,Request.Type.DROP);

                    // Lock de machine!
                    machines.get(pickupMachine.getId()).setLocked(true);
                    request.setMachine(pickupMachine);
                }

                /****** COLLECTS ******/
                if (request.getType() == Request.Type.COLLECT) {
                    rit = new Rit(request.getMachine().getLocation(), null, request.getMachine(), Request.Type.COLLECT);
                    machines.get(request.getMachine().getId()).setLocked(true);
                }

                Add(rit,request);
            }
        }

        /** Alle truck terug naar depot sturen **/
        SendTrucksToHome();

        /** Initiele Oplossing **/
        route = new Route(initialTrucks);
    }

    // Kortste afstand tussen pickuplocatie en droplocatie zoeken
    public Location SearchClosestPickupLocation(ArrayList<Location> potentialPickupLocations, Location dropLocation){
        Location pickupLocation = null;
        int minDistance = Integer.MAX_VALUE;
        for(Location potentialPickupLocation : potentialPickupLocations){
            int distance = distanceMatrix[potentialPickupLocation.getId()][dropLocation.getId()];
            if(minDistance > distance){
                minDistance = distance;
                pickupLocation = potentialPickupLocation;
            }
        }

        return pickupLocation;
    }

    public void Add(Rit rit,Request request) {

        int serviceTime = rit.getMachine().getMachineType().getServiceTime();

        int minDistance = Integer.MAX_VALUE;
        Truck candidateTruck = null;
        Stop s1 = null;
        Stop s2 = null;

        /** Per truck & tussen elke tussenlocatie **/
        for(Truck truck : initialTrucks)
        {
            //Een testruck aanmaken waar we de handeling eerst op uitproberen => de beste handeling uiteindelijk toevoegen aan de effectieve truck!
            Truck testTruck = (Truck) deepClone(truck);

            Stop stop1 = null;
            Stop stop2 = null;

            boolean collect = false;
            boolean drop = false;

            // Bij een drop hoort eerst de machine ophalen collect en dan de machine effectief droppen
            if(rit.getType() == Request.Type.DROP)
            {
                //Alle stops overlopen die reeds in de truck zitten indien er reeds één bestaat op die locatie

                for(Stop stop : testTruck.getStops()) {
                    //de collect(deel 1 van de drop) gewoon toevoegen aan de stop, bestaat de locatie van de collect nog niet in de truck blijft de collect boolean false
                    if (stop.getLocation().getId() == rit.getFromLocation().getId()) {
                        stop.addCollect(rit.getMachine());
                        collect = true;
                    }

                    //de drop(deel 1 van de drop) toevoegen aan de stop, bestaat de locatie van de drop nog niet in de truck blijft de drop boolean false
                    if (stop.getLocation().getId() == rit.getToLocation().getId()) {
                        stop.addDrop(rit.getMachine(), false);
                        drop = true;
                    }
                }

                //Wanneer er dus nog geen stop bestaat van de locatie van de collect(deel 1 van de drop) wordt er een nieuwe gemaakt en de collect toegevoegd
                if(collect == false)
                {
                    stop1 = new Stop(rit.getFromLocation(),rit.getMachine(),Request.Type.TEMPORARYCOLLECT);
                }

                //Wanneer er dus nog geen stop bestaat van de locatie van de drop(deel 2 van de drop) wordt er een nieuwe gemaakt en de drop toegevoegd
                if(drop == false)
                {
                    stop2 = new Stop(rit.getToLocation(),rit.getMachine(),Request.Type.DROP);
                }
            }

            //Een collect bestaat uit de machine collecten op de locatie en deze droppen op de eindlocatie van de truck (voorlopig)
            else if(rit.getType() == Request.Type.COLLECT)
            {
                //kijken als er al een stop bestaat van met de locatie van de collect
                for(Stop stop : testTruck.getStops()) {
                    if (stop.getLocation().getId() == rit.getFromLocation().getId()) {
                        stop.addCollect(rit.getMachine());
                        collect = true;
                    }
                }

                if(collect == false)
                {
                    stop1 = new Stop(rit.getFromLocation(),rit.getMachine(),Request.Type.COLLECT);
                }

                //De drop op de eindlocatie van de truck zetten (niet echt goed)
                if(drop == false)
                {
                    stop2 = new Stop(truck.getEndLocation(),rit.getMachine(),Request.Type.DROP);
                    stop2.depo = true;
                }
            }

            //De stops effectief toevoegen aan de truck (als ze er niet inzatten dan is de stop null)
            if(stop1 != null)
            {
                testTruck.addStopToRoute(stop1);
            }

            //Zelfde als hierboven
            if(stop2 != null)
            {
                testTruck.addStopToRoute(stop2);
            }

            //De tijd bereken van de truck
            int distance = measureDistanceTruck(testTruck);

            if(minDistance > distance)
            {
                /** Controleren of die er nog bij kan **/

                Location l = null;
                Stop laatsteStop = null;
                Stop depoVoorLaatsteStop = null;

                /* Begin nieuw deel 3/12 (stoppen op andere locaties dan depo         */
                if(truck.getEndLocation().isDepot() == false) {
                    l = SearchClosestPickupLocation(depots, testTruck.getEndLocation());
                    depoVoorLaatsteStop = new Stop(l, null, Request.Type.END);
                    laatsteStop = new Stop(testTruck.getEndLocation(), null, Request.Type.END);

                    testTruck.addStopToRoute(depoVoorLaatsteStop);
                    testTruck.addStopToRoute(laatsteStop);

                }
                else
                {
                    laatsteStop = new Stop(testTruck.getEndLocation(), null, Request.Type.END);
                    testTruck.addStopToRoute(laatsteStop);
                }

                /* Einde nieuw deel 3/12 (stoppen op andere locaties dan depo         */


                int time = measureTimeTruck(testTruck) + testTruck.getTijdLaden();
                if(checkLoadTruck(testTruck, true) && testTruck.CheckIfTimeFitsStop(time, serviceTime)) {
                    minDistance = distance;
                    candidateTruck = truck;

                    //Als collect niet true is s1 null zetten omdat anders nog de vorige stop 1(van de vorige iteratie) erin zit
                    if(collect != true)
                    {
                        s1 = stop1;
                    }
                    else
                    {
                        s1=null;
                    }

                    //Als drop niet true is s2 null zetten
                    if(drop != true)
                    {
                        s2 = stop2;
                    }
                    else
                    {
                        s2=null;
                    }
                }
            }
        }

        if(candidateTruck != null) {
            //nu hebben we de id van de truck waar we de request het best aan toevoegen deze effectief toevoegen aan initialtrucks gelijkaardig als hierboven
            if (s1 != null) {
                initialTrucks.get(candidateTruck.getId()).addStopToRoute(s1);
            } else {
                for (Stop stop : initialTrucks.get(candidateTruck.getId()).getStops()) {
                    if (stop.getLocation().getId() == rit.getFromLocation().getId()) {
                        stop.addCollect(rit.getMachine());
                        initialTrucks.get(candidateTruck.getId()).AddLoadedMachines(rit.getMachine());
                        initialTrucks.get(candidateTruck.getId()).addLoad(rit.getMachine().getMachineType().getVolume());
                        initialTrucks.get(candidateTruck.getId()).addTijdLaden(rit.getMachine().getMachineType().getServiceTime());

                        break;
                    }
                }
            }

            if (s2 != null) {
                initialTrucks.get(candidateTruck.getId()).addStopToRoute(s2);
            } else {
                //Bij collect geen to destination, dus deze functie wordt enkel gelopen bij drop
                if (rit.getType() != Request.Type.COLLECT) {
                    if(VolgordeChecken(initialTrucks.get(candidateTruck.getId()).getStops(), rit.getMachine(), 0)) {
                        for (Stop stop : initialTrucks.get(candidateTruck.getId()).getStops()) {

                            if (stop.getLocation().getId() == rit.getToLocation().getId()) {
                                if (rit.getFromLocation().getId() != rit.getToLocation().getId()) {
                                    stop.addDrop(rit.getMachine(), false);
                                } else {
                                    //Hier weten we dat er een drop en collect op dezelfde plek zijn dus speciaal geval op true (in en uitladen na elkaar van machine)
                                    stop.addDrop(rit.getMachine(), true);
                                }

                                initialTrucks.get(candidateTruck.getId()).getLoadedMachines().remove(rit.getMachine());

                                //Load adden & servicetime to truck!
                                initialTrucks.get(candidateTruck.getId()).addLoad(rit.getMachine().getMachineType().getVolume());
                                initialTrucks.get(candidateTruck.getId()).addTijdLaden(rit.getMachine().getMachineType().getServiceTime());
                            }
                        }
                    }
                    else {
                        Stop s3 = new Stop(rit.getToLocation(), rit.getMachine(), Request.Type.DROP);
                        initialTrucks.get(candidateTruck.getId()).addStopToRoute(s3);
                    }
                }
            }

            request.setInTruckId(candidateTruck.getId());

            int distance = measureDistanceTruck(initialTrucks.get(candidateTruck.getId()));
            int time = measureTimeTruck(initialTrucks.get(candidateTruck.getId())) + initialTrucks.get(candidateTruck.getId()).getTijdLaden();

            initialTrucks.get(candidateTruck.getId()).setCurrentDistance(distance);
            initialTrucks.get(candidateTruck.getId()).setCurrentWorkTime(time);
        }

        // Geen trucks beschikbaar => dummy truck toevoegen
        else {
            int randomLocation = random.nextInt(4);
            Location start_endLocation = locations.get(randomLocation);
            initialTrucks.add(new Truck(initialTrucks.size(), start_endLocation, start_endLocation));
            candidateTruck = initialTrucks.get(initialTrucks.size()-1);
            Stop stopStart = new Stop(candidateTruck.getStartLocation(),null,Request.Type.START);
            candidateTruck.addStopToRoute(stopStart);
            Add(rit, request);
        }
    }

    //De totale load berekenen van de truck geeft terug als hij over zijn max load gaat
    public boolean checkLoadTruck(Truck t, boolean initieel) {
        int load = 0;
        if(t.getStops().size() > 1)
        {
            for(int index = 0 ; index <= t.getStops().size()-1; index++)
            {
                for(Machine m : t.getStops().get(index).getcollect())
                {
                    load = load + m.getMachineType().getVolume();
                }

                if(initieel) {
                    if (t.getStops().get(index).getLocation().getId() != t.getEndLocation().getId()) {
                        for (Machine m : t.getStops().get(index).getdrop()) {
                            load = load - m.getMachineType().getVolume();
                        }
                    }
                }
                else {
                    for (Machine m : t.getStops().get(index).getdrop()) {
                        load = load - m.getMachineType().getVolume();
                    }
                }

                if(load > TRUCK_CAPACITY)
                {
                    return false;
                }
            }
        }
        return true;
    }

    //De afstand berekenen per truck
    public int measureDistanceTruck(Truck t) {
        int distance = 0;

        if(t.getStops().size() >= 2)
        {
            for(int index = 0; index< (t.getStops().size()-1); index++)
            {
                distance = distance + distanceMatrix[t.getStops().get(index+1).getLocation().getId()][t.getStops().get(index).getLocation().getId()];
            }
        }
        return distance;
    }

    //De tijd berekenen per truck
    public int measureTimeTruck(Truck t) {
        int time = 0;
        for(int index = 0; index< t.getStops().size()-1; index++)
        {
            time = time + timeMatrix[t.getStops().get(index+1).getLocation().getId()][t.getStops().get(index).getLocation().getId()];
        }

        return time;
    }

    //De laatste stop toevoegen aan alle trucks
    public void SendTrucksToHome(){

        for(Truck truck : initialTrucks) {
            if (truck.getStops().size() != 0) {

                ArrayList<Integer> teVerwijderenStops = new ArrayList<Integer>();
                //TODO slechte manier hier verwijder ik alle tussenstoppen die tussen begin en einde staan in de totaal aantal stops
                //de eerste skippen want deze mag niet weg (daarom start index op 1)
                for(int index = 1; index < truck.getStops().size();index++)
                {
                    if (truck.getStops().get(index).getLocation().getId() == truck.getEndLocation().getId() && truck.getStops().get(index).getcollect().size() == 0)
                    {
                        //De index van de te verwijderen stops opslaan
                        teVerwijderenStops.add(index);
                    }
                }

                int teller = 0;

                //De stops effectief verwijderen
                for(int index : teVerwijderenStops)
                {
                    truck.DeleteStop(index-teller);
                    teller++;
                }


                if(truck.getEndLocation().isDepot() == false)
                {
                    Location l = SearchClosestPickupLocation(depots,truck.getEndLocation());
                    Stop stop = new Stop(l, null, Request.Type.END);
                    truck.addStopToRoute(stop);

                    for(Machine m : truck.getLoadedMachines())
                    {
                        truck.getStops().get(truck.getStops().size()-1).addDrop(m,false);
                    }

                    Stop stop2 = new Stop(truck.getEndLocation(), null, Request.Type.END);

                    truck.addStopToRoute(stop2);
                }
                else
                {
                    Stop stop = new Stop(truck.getEndLocation(), null, Request.Type.END);
                    truck.addStopToRoute(stop);

                    for(Machine m : truck.getLoadedMachines())
                    {
                        truck.getStops().get(truck.getStops().size()-1).addDrop(m,false);
                    }
                }




                int distance = measureDistanceTruck(truck);
                int time = measureTimeTruck(truck) + truck.getTijdLaden();

                truck.setCurrentDistance(distance);
                truck.setCurrentWorkTime(time);
            }
        }
    }

    //De afstand berekenen van alle trucks
    public int measureTotalDistance(Route r) {
        int distance = 0;
        for(Truck truck : r.getTrucks())
        {
            distance = distance + measureDistanceTruck(truck);
        }

        r.setTotalDistance(distance);
        return distance;
    }

    // Dummy trucks in de gewenste hoeveelheid krijgen
    public void MakeFeasible() {

        // Instellingen logger
        logger.setUseParentHandlers(false);
        Log formatter = new Log("MakeFeasible");
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.info("Starting make feasible...");


        Route bestRoute = new Route(route);
        measureTotalDistance(bestRoute);

        List<Request> requestsNotFeasible = new ArrayList<>();
        for(Request request : requests){
            if(request.getInTruckId() > trucks.size()-1){
                requestsNotFeasible.add(request);
            }
        }

        int totalRequest = requestsNotFeasible.size();
        int counterCurrentRequest = 1;

        while (true) {
            if (requestsNotFeasible.size() == 0) {
                break;
            }

            // Hier kiezen we welke request we gaan behandelen
            int randomRequest = random.nextInt(requestsNotFeasible.size());
            Request req = requestsNotFeasible.get(randomRequest);

            // Hier bereken je naar welke truck we de move doen
            int toTruckId = random.nextInt(trucks.size());

            Route returnRoute = DoMove(bestRoute, req, toTruckId);

            // Wanneer returnRoute niet gelijk is aan null, dit wil zeggen dat de move toegelaten is dus de tijd en load van de truck is niet overschreden
            if(returnRoute != null)
            {
                bestRoute = new Route(returnRoute);

                requestsNotFeasible.remove(randomRequest);
                req.setInTruckId(toTruckId);

                //System.out.println("Request " + counterCurrentRequest + " of " + totalRequest + " done");
                logger.info("Request " + counterCurrentRequest + " of " + totalRequest + " done");
                counterCurrentRequest++;
            }

            // stop?
            if (requestsNotFeasible.size() == 0) {
                break;
            }
        }

        while (bestRoute.getTrucks().size() > trucks.size()){
            bestRoute.getTrucks().remove(bestRoute.getTrucks().size()-1);
        }

        route = bestRoute;
        logger.removeHandler(handler);
    }

    public void LocalSearch(int MAX_IDLE) {

        // Instellingen logger
        logger.setUseParentHandlers(false);
        Log formatter = new Log("LocalSearch");
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.info("Starting LocalSearch...");

        /* LocalSearch settings ----------------------------------- */

        int L = 1000;

        /* create initial solution ------------------------- */
        Route bestRoute = new Route(route);
        measureTotalDistance(bestRoute);

        /* [LocalSearch] init ------------------------------------- */
        int idle = 0;
        int count = 0;
        double bound = bestRoute.getTotalDistance();

        /* loop -------------------------------------------- */
        while (true) {

            int oldDist = bestRoute.getTotalDistance();

            int toTruckId1 = -1, toTruckId2 = -1, toTruckId3 = -1, toTruckId4 = -1;
            int requestId1 = -1, requestId2 = -1, requestId3 = -1, requestId4 = -1;

            Request req1 = null, req2 = null, req3 = null, req4 = null;
            Route returnRoute1 = null, returnRoute2 = null, returnRoute3 = null, returnRoute4 = null;

            while(returnRoute1 == null) {
                toTruckId1 = random.nextInt(trucks.size());
                requestId1 = random.nextInt(requests.size());

                req1 = requests.get(requestId1);
                returnRoute1 = DoMove(bestRoute, req1, toTruckId1);
            }

            while(returnRoute2 == null) {
                toTruckId2 = random.nextInt(trucks.size());
                requestId2 = random.nextInt(requests.size());

                while(requestId1 == requestId2){
                    requestId2 = random.nextInt(requests.size());
                }

                req2 = requests.get(requestId2);
                returnRoute2 = DoMove(returnRoute1, req2, toTruckId2);
            }

            while(returnRoute3 == null) {
                toTruckId3 = random.nextInt(trucks.size());
                requestId3 = random.nextInt(requests.size());

                while (requestId1 == requestId3 || requestId2 == requestId3) {
                    requestId3 = random.nextInt(requests.size());
                }

                req3 = requests.get(requestId3);
                returnRoute3 = DoMove(returnRoute2, req3, toTruckId3);
            }

            /*while(returnRoute4 == null) {
                toTruckId4 = random.nextInt(trucks.size());
                requestId4 = random.nextInt(requests.size());

                while (requestId1 == requestId4 || requestId2 == requestId4|| requestId3 == requestId4) {
                    requestId4 = random.nextInt(requests.size());
                }

                req4 = requests.get(requestId4);
                returnRoute4 = DoMove(returnRoute3, req4, toTruckId4);
            }*/

            int newDist = measureTotalDistance(returnRoute3);

            // [LocalSearch] accept?
            if (newDist < oldDist || newDist < bound) {
                idle = 0;
                if(newDist < bestRoute.getTotalDistance()){
                    bestRoute = new Route(returnRoute3);
                    measureTotalDistance(bestRoute);

                    //De truck waar de request aan toegekent is updaten
                    req1.setInTruckId(toTruckId1);
                    req2.setInTruckId(toTruckId2);
                    req3.setInTruckId(toTruckId3);
                    //req4.setInTruckId(toTruckId4);

                    logger.info("Totale afstand: " + newDist + " na " + (System.currentTimeMillis()-startTime)/1000 + " seconden");
                }
            }
            else{
                idle++;
                measureTotalDistance(bestRoute);
            }

            // [LocalSearch] update
            count++;
            if(count==L){
                count = 0;
                bound = measureTotalDistance(bestRoute);
            }

             // stop?
             if (idle >= MAX_IDLE || (System.currentTimeMillis() - startTime) / 1000 > TIME_LIMIT) {
                 break;
             }
        }

        /* finished ---------------------------------------- */
        route = bestRoute;
        logger.removeHandler(handler);
    }

    public void SimulatedAnnealing() {

        // Instellingen logger
        logger.setUseParentHandlers(false);
        Log formatter = new Log("Simulated Annealing");
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.info("Starting SimulatedAnnealing...");

        // Set initial temp
        double temp = 10;

        // Cooling rate
        double coolingRate = 0.003;

        // Initialize intial solution
        Route bestSolution = new Route(route);
        List<Request> bestRequests = new ArrayList<Request>(requests);

        // Set as current best
        Route currentSolution = new Route(bestSolution);
        List<Request> currentRequests = new ArrayList<Request>(bestRequests);


        while((System.currentTimeMillis() - startTime) / 1000 < TIME_LIMIT) {
            // Loop until system has cooled
            while (temp > 0.1 && (System.currentTimeMillis() - startTime) / 1000 < TIME_LIMIT) {

                // Create new neighbour tour
                Route newSolution = new Route(currentSolution);
                List<Request> newRequests = new ArrayList<Request>(currentRequests);

                int toTruckId1 = -1;
                int toTruckId2 = -1;
                int toTruckId3 = -1;

                int requestId1 = -1;
                int requestId2 = -1;
                int requestId3 = -1;

                Request req1 = null;
                Request req2 = null;
                Request req3 = null;

                Route returnRoute1 = null;
                Route returnRoute2 = null;
                Route returnRoute3 = null;

                while(returnRoute1 == null) {
                    toTruckId1 = random.nextInt(trucks.size());
                    requestId1 = random.nextInt(requests.size());

                    req1 = newRequests.get(requestId1);
                    returnRoute1 = DoMove(newSolution, req1, toTruckId1);
                }

                while(returnRoute2 == null) {
                    toTruckId2 = random.nextInt(trucks.size());
                    requestId2 = random.nextInt(requests.size());

                    while(requestId1 == requestId2) {
                        requestId2 = random.nextInt(requests.size());
                    }

                    req2 = newRequests.get(requestId2);
                    returnRoute2 = DoMove(returnRoute1, req2, toTruckId2);
                }

                while(returnRoute3 == null) {
                    toTruckId3 = random.nextInt(trucks.size());
                    requestId3 = random.nextInt(requests.size());

                    while(requestId1 == requestId2 || requestId2 == requestId3) {
                        requestId3 = random.nextInt(requests.size());
                    }

                    req3 = newRequests.get(requestId3);
                    returnRoute3 = DoMove(returnRoute2, req3, toTruckId3);
                }

                // Get energy of solutions
                int currentEnergy = measureTotalDistance(currentSolution);
                int neighbourEnergy = measureTotalDistance(returnRoute3);

                // Decide if we should accept the neighbour
                if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
                    req1.setInTruckId(toTruckId1);
                    req2.setInTruckId(toTruckId2);
                    req3.setInTruckId(toTruckId3);

                    currentSolution = new Route(returnRoute3);
                    currentRequests = new ArrayList<Request>(newRequests);
                }

                // Keep track of the best solution found
                int bestTotalDistance = measureTotalDistance(bestSolution);
                int currentSolutionDistance = measureTotalDistance(currentSolution);

                if (currentSolutionDistance < bestTotalDistance) {
                    bestSolution = new Route(currentSolution);
                    bestRequests = new ArrayList<Request>(currentRequests);
                    logger.info("Totale afstand: " + currentSolutionDistance + " na " + (System.currentTimeMillis()-startTime)/1000 + " seconden");
                }

                // Cool system
                temp *= 1 - coolingRate;
            }
            temp = 10;
        }

        // UPDATE BEST ROUTE
        route = bestSolution;
        logger.removeHandler(handler);
    }

    // Calculate the acceptance probability
    public static double acceptanceProbability(int energy, int newEnergy, double temperature) {
        // If the new solution is better, accept it
        if (newEnergy < energy) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        return Math.exp((energy - newEnergy) / temperature);
    }

    private Route DoMove(Route r, Request request, int toTruckId) {
        // IsDone == true wil zeggen dat deze al verwerkt is in een drop, dus deze collect request wordt nooit meer uitgevoerd dit wordt gecllect en direct naar de klant gebracht (initele oplossing)
        if(request.getType() == Request.Type.COLLECT && !request.isDone()) {
            return SwapCollectRequest(r, request, toTruckId);
        }
        else if(request.getType() == Request.Type.DROP) {
            return SwapDropRequest(r, request, toTruckId);
        }

        return null;
    }

    private Route SwapCollectRequest(Route r, Request request, int toTruckId) {

        Route rou = new Route(r);

        Truck truckToDeleteRequest = rou.getTrucks().get(request.getInTruckId());
        Truck truckToAddRequest = rou.getTrucks().get(toTruckId);

        /** Verwijderen uit oude truck **/

        int indexfor = 0;

        int indexRemoveLocatieCollect = 0;
        int indexRemoveLocatieDrop = 0;

        boolean deleteCollect = false;
        boolean deleteDrop = false;

        Location locatieCollectMachine = null;

        // Deel 1
        outerloop:
        for(Stop stop : truckToDeleteRequest.getStops()) {
            for (Machine machine : stop.getcollect()) {
                if (machine.getId() == request.getMachine().getId()) {

                    locatieCollectMachine = stop.getLocation();
                    stop.removeCollect(machine);

                    //Als in de oude truck na het verwijderen van de collect de stop niet meer gebruikt wordt deze verwijderen
                    if (stop.getcollect().size() == 0 && stop.getdrop().size() == 0) {
                        deleteCollect = true;
                        indexRemoveLocatieCollect = indexfor;
                    }
                    break outerloop;
                }
            }
            indexfor++;
        }

        if(locatieCollectMachine == null)
            return null;

        if(deleteCollect)
        {
            if(truckToDeleteRequest.getStops().size()-1 != indexRemoveLocatieCollect && indexRemoveLocatieCollect != 0)
                truckToDeleteRequest.removeStop(indexRemoveLocatieCollect);
        }

        indexfor = 0;

        //Deel 2
        outerloop:
        for(Stop stop : truckToDeleteRequest.getStops()) {
            for (Machine machine : stop.getdrop()) {
                if (machine.getId() == request.getMachine().getId()) {
                    stop.removeDrop(machine);

                    if (stop.getcollect().size() == 0 && stop.getdrop().size() == 0) {
                        deleteDrop = true;
                        indexRemoveLocatieDrop = indexfor;
                    }
                    break outerloop;
                }
            }
            indexfor++;
        }

        if(deleteDrop)
        {
            if(truckToDeleteRequest.getStops().size()-1 != indexRemoveLocatieDrop && indexRemoveLocatieDrop != 0)
                truckToDeleteRequest.removeStop(indexRemoveLocatieDrop);
        }

        truckToDeleteRequest.lessLoad(request.getMachine().getMachineType().getVolume());
        truckToDeleteRequest.lessTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));

        int time = measureTimeTruck(truckToDeleteRequest) + truckToDeleteRequest.getTijdLaden();
        truckToDeleteRequest.setCurrentWorkTime(time);

        /** Toevoegen aan nieuwe truck **/

        // Dichtste depo zoeken om te droppen
        Location locatieDropMachine  = SearchClosestPickupLocation(depots, locatieCollectMachine);

        boolean stopCollectModified = false;
        boolean stopDropModified = false;

        // Kijken als er al een stop bestaat van met de locatie van de collect
        if(locatieCollectMachine != null)
        {
            // Deel 1: Collect machine
            // Kan de machine opgehaald worden vanuit een bestaande locatie?
            for(int index = 0; index < truckToAddRequest.getStops().size() - 1; index++){
                if (truckToAddRequest.getStops().get(index).getLocation().getId() == locatieCollectMachine.getId()) {
                    truckToAddRequest.getStops().get(index).addCollect(request.getMachine());
                    stopCollectModified = true;
                    break;
                }
            }
            // Geen bestaande stop gevonden, nieuwe stop toevoegen
            if (!stopCollectModified) {
                // Index - 1 anders komt hij na de eindlocatie van de truck, we plaatsen hem net ervoor
                int index = SearchClosestCollectPointFromHere(truckToAddRequest.getStops(), locatieCollectMachine);
                index = index == truckToAddRequest.getStops().size()? index - 1 : index;

                Stop newStop = new Stop(locatieCollectMachine, request.getMachine(), Request.Type.COLLECT);
                truckToAddRequest.addStopToRoute(index, newStop);

            }

            // Deel 2: Drop machine
            // Wanneer de eindlocatie geen depo is, droppen we maar tot de voorlaatste stop wat een depo is
            int aantalStops = 0;
            if(truckToAddRequest.getEndLocation().isDepot() == true)
                aantalStops = truckToAddRequest.getStops().size();
            else
                aantalStops = truckToAddRequest.getStops().size() - 1;

            // Kan de machine gedropt worden in een bestaande locatie?
            for(int index = 0; index < aantalStops; index++) {
                if (truckToAddRequest.getStops().get(index).getLocation().getId() == locatieDropMachine.getId() && VolgordeChecken(truckToAddRequest.getStops(), request.getMachine(), index)) {
                    truckToAddRequest.getStops().get(index).addDrop(request.getMachine(), false);
                    stopDropModified = true;
                    break;
                }
            }
            // Geen bestaande stop gevonden, nieuwe stop toevoegen
            if (!stopDropModified) {
                int index = SearchClosestDropPointFromHere(truckToAddRequest.getStops(), locatieDropMachine, request.getMachine(), truckToAddRequest.getEndLocation().isDepot());
                Stop newStop = new Stop(locatieDropMachine, request.getMachine(), Request.Type.DROP);
                truckToAddRequest.addStopToRoute(index, newStop);
            }

            truckToAddRequest.addLoad(request.getMachine().getMachineType().getVolume());
            truckToAddRequest.addTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));

            time = measureTimeTruck(truckToAddRequest) + truckToAddRequest.getTijdLaden();
            truckToAddRequest.setCurrentWorkTime(time);

            if(checkLoadTruck(truckToAddRequest, false) && truckToAddRequest.CheckIfTimeFitsStop() ) {

                return rou;
            }
        }

        return null;
    }

    private Route SwapDropRequest(Route r, Request request, int toTruckId) {

        Route rou = new Route(r);

        Truck truckToDeleteRequest = rou.getTrucks().get(request.getInTruckId());
        Truck truckToAddRequest = rou.getTrucks().get(toTruckId);

        /** Verwijderen uit oude truck **/

        int indexfor = 0;

        int indexRemoveLocatieCollect = 0;
        int indexRemoveLocatieDrop = 0;

        boolean deleteCollect = false;
        boolean deleteDrop = false;

        Location locatieCollectMachine = null;
        Location locatieDropMachine = null;

        // Deel 1
        outerloop:
        for(Stop stop : truckToDeleteRequest.getStops()) {
            for (Machine machine : stop.getcollect()) {
                if (machine.getId() == request.getMachine().getId()) {

                    locatieCollectMachine = stop.getLocation();
                    stop.removeCollect(machine);

                    if (stop.getcollect().size() == 0 && stop.getdrop().size() == 0) {
                        deleteCollect = true;
                        indexRemoveLocatieCollect = indexfor;
                    }
                    break outerloop;
                }
            }
            indexfor++;
        }

        if(locatieCollectMachine == null)
            return null;

        if(deleteCollect)
        {
            if(truckToDeleteRequest.getStops().size()-1 != indexRemoveLocatieCollect && indexRemoveLocatieCollect != 0)
                truckToDeleteRequest.removeStop(indexRemoveLocatieCollect);
        }

        indexfor = 0;

        // Deel 2
        outerloop:
        for(Stop stop : truckToDeleteRequest.getStops()) {
            for(Machine machine : stop.getdrop()) {
                if(machine.getId() == request.getMachine().getId()) {

                    locatieDropMachine = stop.getLocation();
                    stop.removeDrop(machine);

                    if(stop.getcollect().size() == 0 && stop.getdrop().size() == 0)
                    {
                        deleteDrop = true;
                        indexRemoveLocatieDrop = indexfor;
                    }
                    break outerloop;
                }
            }
            indexfor++;
        }

        if(locatieDropMachine == null)
            return null;

        if(deleteDrop)
        {
            if(truckToDeleteRequest.getStops().size()-1 != indexRemoveLocatieDrop && indexRemoveLocatieDrop != 0)
                truckToDeleteRequest.removeStop(indexRemoveLocatieDrop);
        }


        truckToDeleteRequest.lessLoad(request.getMachine().getMachineType().getVolume());
        truckToDeleteRequest.lessTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));

        int time = measureTimeTruck(truckToDeleteRequest) + truckToDeleteRequest.getTijdLaden();
        truckToDeleteRequest.setCurrentWorkTime(time);

        /** Toevoegen aan nieuwe truck **/

        boolean stopCollectModified = false;
        boolean stopDropModified = false;

        if(locatieCollectMachine != null && locatieDropMachine != null) {

            // Deel 1
            for(int index = 0; index < truckToAddRequest.getStops().size() - 1; index++){
                if (truckToAddRequest.getStops().get(index).getLocation().getId() == locatieCollectMachine.getId()) {
                    truckToAddRequest.getStops().get(index).addCollect(request.getMachine());
                    stopCollectModified = true;
                    break;
                }
            }
            if (!stopCollectModified) {
                int index = SearchClosestCollectPointFromHere(truckToAddRequest.getStops(), locatieCollectMachine);
                index = index == truckToAddRequest.getStops().size()? index - 1 : index;

                Stop newStop = new Stop(locatieCollectMachine, request.getMachine(), Request.Type.TEMPORARYCOLLECT);
                truckToAddRequest.addStopToRoute(index, newStop);
            }

            // Deel 2
            int aantalStops = 0;
            if(truckToAddRequest.getEndLocation().isDepot() == true)
                aantalStops = truckToAddRequest.getStops().size();
            else
                aantalStops = truckToAddRequest.getStops().size() - 1;

            for(int index = 0; index < aantalStops; index++) {
                if (truckToAddRequest.getStops().get(index).getLocation().getId() == locatieDropMachine.getId() && VolgordeChecken(truckToAddRequest.getStops(), request.getMachine(), index)) {
                    truckToAddRequest.getStops().get(index).addDrop(request.getMachine(), false);
                    stopDropModified = true;
                    break;
                }
            }
            if (!stopDropModified) {
                int index = SearchClosestDropPointFromHere(truckToAddRequest.getStops(), locatieDropMachine, request.getMachine(), truckToAddRequest.getEndLocation().isDepot());
                Stop newStop = new Stop(locatieDropMachine, request.getMachine(), Request.Type.DROP);
                truckToAddRequest.addStopToRoute(index, newStop);
            }

            truckToAddRequest.addLoad(request.getMachine().getMachineType().getVolume());
            truckToAddRequest.addTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));

            time = measureTimeTruck(truckToAddRequest) + truckToAddRequest.getTijdLaden();
            truckToAddRequest.setCurrentWorkTime(time);

            if(checkLoadTruck(truckToAddRequest, false) && truckToAddRequest.CheckIfTimeFitsStop() ) {

                return rou;
            }
        }

        return null;
    }

    // Dichtste bestaande stop zoeken, 1 index verder gaan
    public int SearchClosestCollectPointFromHere(ArrayList<Stop> stops, Location collectLocation) {
        int indexNewLocation = 0;
        int minDistance = Integer.MAX_VALUE;
        for(int index = 0; index < stops.size(); index++) {
            int distance = distanceMatrix[stops.get(index).getLocation().getId()][collectLocation.getId()];
            if (minDistance > distance) {
                minDistance = distance;
                indexNewLocation = index;
            }
        }
        return indexNewLocation + 1;
    }

    public int SearchClosestDropPointFromHere(ArrayList<Stop> stops, Location dropLocation, Machine machine, boolean isDepo) {
        int indexNewLocation = 0;
        int minDistance = Integer.MAX_VALUE;
        for(int index = 0; index < stops.size(); index++) {
            if(VolgordeChecken(stops, machine, index)) {
                int distance = distanceMatrix[stops.get(index).getLocation().getId()][dropLocation.getId()];
                if (minDistance > distance) {
                    minDistance = distance;
                    indexNewLocation = index;
                }
            }
        }
        return indexNewLocation;
    }

    // Kijken dat het toestel eerst opgehaald wordt en daarna pas gedropt wordt!
    public boolean VolgordeChecken(List<Stop> stops, Machine dropmachine, int startIndex) {
        for(int currentIndex = startIndex; currentIndex < stops.size(); currentIndex++){
            for(Machine machine : stops.get(currentIndex).getcollect()){
                if(machine.getId() == dropmachine.getId())
                    return false;
            }
        }
        return true;
    }

    //output file schrijven
    public void WriteFileNieuw() throws IOException {
        System.out.println("");

        // Berekenen hoeveel newTrucks er effectief in dienst zijn
        int numberOfUsedTrucks = 0;
        for (Truck truck : route.getTrucks()) {
            if(truck.getStops().size() > 2){
                numberOfUsedTrucks++;
            }
        }

        int totalDistance = measureTotalDistance(route);

        /** Inteliji **/
        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.SOLUTION_FILE));

        /** Jar execution **/
        //BufferedWriter writer = new BufferedWriter(new FileWriter(new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile() + "/" + Main.SOLUTION_FILE));

        writer.write("PROBLEM: " + Main.INPUT_FILE + "\n");
        writer.write("DISTANCE: " + totalDistance + "\n");
        writer.write("TRUCKS: " + numberOfUsedTrucks + "\n");

        boolean firstLine = true;

        for (Truck truck : route.getTrucks()) {
            // Enkel trucks die rijden uitprinten
            if(truck.getStops().size() > 2)
            {
                if(!firstLine) {
                    writer.write("\n");
                    System.out.print("\n");
                }

                firstLine = false;

                int distance = measureDistanceTruck(truck);
                int time = measureTimeTruck(truck) + truck.getTijdLaden();

                writer.write(truck.getId() + " " + distance + " " + time);
                System.out.print(truck.getId() + " " + distance + " " + time);

                // First location print (startlocation)
                int currentLocationID = truck.getStartLocation().getId();
                writer.write(" " + truck.getStartLocation().getId());
                System.out.print(" " + truck.getStartLocation().getId());

                int index = 0;
                // Route
                for (Stop stop : truck.getStops()) {

                    if(currentLocationID != stop.getLocation().getId())
                    {
                        currentLocationID = stop.getLocation().getId();
                        String wr = "";
                        for(Machine m : stop.getcollect())
                        {
                            wr = wr + ":" + m.getId();
                        }
                        for(Machine m : stop.getdrop())
                        {
                            wr = wr + ":" + m.getId();
                        }
                        writer.write(" " + stop.getLocation().getId() + wr);
                        System.out.print(" " + stop.getLocation().getId() + wr);
                    }
                    else
                    {
                        String wr = "";
                        for(Machine m : stop.getcollect())
                        {
                            wr = wr + ":" + m.getId();
                        }
                        for(Machine m : stop.getdrop())
                        {
                            wr = wr + ":" + m.getId();
                        }

                        writer.write(wr);
                        System.out.print(wr);
                    }
                    index++;
                }
            }
        }
        writer.close();

        System.out.println("\nTotale afstand: " + totalDistance +"\n");
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
}