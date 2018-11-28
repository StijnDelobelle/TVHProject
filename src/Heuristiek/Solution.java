package Heuristiek;

import Objects.*;
import Main.*;

import java.io.*;
import java.util.*;

import static Heuristiek.Problem.*;


public class Solution   {

    private Route route;
    private ArrayList<Truck> initialTrucks;
    private ArrayList<Request> initialRequests;
    private HashMap<Integer, Request> requests;
    private static final Random random = new Random(0);
    private Route BesteMetaSolution = null;


    public void zetStartStops(ArrayList<Truck> initialTrucks)
    {
        for(Truck truck: initialTrucks)
        {
            Stop stopStart = new Stop(truck.getStartLocation(),null,Request.Type.START);

            truck.addStopToRoute(stopStart);
        }
    }

    public void InitialSolution(ArrayList<Request> argRequests, ArrayList<Truck> argTrucks) {

        /** MACHINE_TYPES: [id volume serviceTime name]  **/
        /** MACHINES:      [id machineTypeId locationId] **/
        /** DROPS:         [id machineTypeId locationId] **/
        /** COLLECTS:      [id machineId]                **/

        initialRequests = (ArrayList<Request>) deepClone(argRequests);
        initialTrucks = (ArrayList<Truck>) deepClone(argTrucks);

        zetStartStops(initialTrucks);

        Random random = new Random(0);
        Collections.shuffle(initialRequests, random);

        requests = new HashMap<>();
        for(Request request : initialRequests){
            requests.put(request.getId(), request);
        }

        // Alle collect requests die nodig zijn, key => machineID
        HashMap<Integer, Request> collects = new HashMap<>();
        for(Request request : requests.values()) {
            if (request.getType() == Request.Type.COLLECT)
                collects.put(request.getMachine().getId(), request);
        }

        for (Request request : requests.values())
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

                //AddToTruck(ride);
                Add(rit,request);
                requests.get(request.getId()).setDone(true);
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
            //ArrayList<Ride> rides = (ArrayList<Ride>) deepClone(truck.getRoute());

            /** Truck heeft nog geen ritten gedaan => Startlocatie van de truck nemen (De code denkt nu dat de vorige rit eindigde in deze locatie) **/
            //if(rides.size() == 0){
            //    rides.add(new Ride(null, truck.getStartLocation(), null, null, null));
            //}

            //for(Ride ride : rides){
            /** Van de eindlocatie van de vorige rit naar de pickuplocatie **/
            //int distance = distanceMatrix[truck.getCurrentLocation().getId()][pickupLocation.getId()] + distanceMatrix[pickupLocation.getId()][currentRide.getToLocation().getId()];

            //Een testruck aanmaken waar we de handeling eerst op uitproberen => de beste handeling uiteindelijk toevoegen aan de effectieve truck!
            Truck testTruck = (Truck) deepClone(truck);

            Stop stop1 = null;
            Stop stop2 = null;

            boolean collect = false;
            boolean drop = false;

            // Een drop request
            //Bij een drop hoort eerst de machine ophalen collect en dan de machine effectief droppen
            if(rit.getType() == Request.Type.DROP)
            {
                //Alle stops overlopen die reeds in de truck zitten indien er reeds één bestaat op die locatie

                //TODO mechanisme dat checkt dat drop zeker na de collect komt zit er nu nog niet in!!
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
                    //TODO truck.getCurrentLocation() parameter veranderen !!
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
                //int rideTime = timeMatrix[truck.getCurrentLocation().getId()][pickupLocation.getId()] + timeMatrix[pickupLocation.getId()][currentRide.getToLocation().getId()];
                int time = measureTimeTruck(testTruck) + testTruck.getTijdLaden();
                if(checkLoadTruck(testTruck) && truck.CheckIfTimeFitsStop(time, serviceTime) ) {
                    minDistance = distance;
                    //startLocation = truck.getCurrentLocation();
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
            //}
        }

        // Geen trucks beschikbaar => dummy truck toevoegen
        if(candidateTruck == null){
            Location start_endLocation = locations.get(0);
            initialTrucks.add(new Truck(initialTrucks.size(), start_endLocation, start_endLocation));
            candidateTruck = initialTrucks.get(initialTrucks.size()-1);
            Stop stopStart = new Stop(candidateTruck.getStartLocation(),null,Request.Type.START);
            candidateTruck.addStopToRoute(stopStart);
        }

        //nu hebben we de id van de truck waar we de request het best aan toevoegen deze effectief toevoegen aan initialtrucks gelijkaardig als hierboven
        if(s1 != null)
        {
            initialTrucks.get(candidateTruck.getId()).addStopToRoute(s1);
        }
        else
        {
            for(Stop stop : initialTrucks.get(candidateTruck.getId()).getStops())
            {
                if(stop.getLocation().getId() == rit.getFromLocation().getId())
                {
                    stop.addCollect(rit.getMachine());
                    initialTrucks.get(candidateTruck.getId()).AddLoadedMachines(rit.getMachine());
                    initialTrucks.get(candidateTruck.getId()).addLoad(rit.getMachine().getMachineType().getVolume());
                    initialTrucks.get(candidateTruck.getId()).addTijdLaden(rit.getMachine().getMachineType().getServiceTime());

                    break;
                }
            }
        }

        if(s2 != null)
        {
            initialTrucks.get(candidateTruck.getId()).addStopToRoute(s2);
        }
        else
        {
            //Bij collect geen to destination, dus deze functie wordt enkel gelopen bij drop
           if(rit.getType() != Request.Type.COLLECT)
           {
               for(Stop stop : initialTrucks.get(candidateTruck.getId()).getStops())
               {
                   if(stop.getLocation().getId() == rit.getToLocation().getId())
                   {
                       if(rit.getFromLocation().getId() != rit.getToLocation().getId())
                       {
                           stop.addDrop(rit.getMachine(),false);
                       }
                       else
                       {
                           //Hier weten we dat er een drop en collect op dezelfde plek zijn dus speciaal geval op true (in en uitladen na elkaar van machine)
                           stop.addDrop(rit.getMachine(),true);
                       }

                       initialTrucks.get(candidateTruck.getId()).getLoadedMachines().remove(rit.getMachine());

                       //Load adden & servicetime to truck!
                       initialTrucks.get(candidateTruck.getId()).addLoad(rit.getMachine().getMachineType().getVolume());
                       initialTrucks.get(candidateTruck.getId()).addTijdLaden(rit.getMachine().getMachineType().getServiceTime());
                   }
               }
           }
        }

        request.setInTruckId(candidateTruck.getId());

        int distance = measureDistanceTruck(initialTrucks.get(candidateTruck.getId()));
        int time = measureTimeTruck(initialTrucks.get(candidateTruck.getId())) + initialTrucks.get(candidateTruck.getId()).getTijdLaden();

        initialTrucks.get(candidateTruck.getId()).setCurrentDistance(distance);
        initialTrucks.get(candidateTruck.getId()).setCurrentWorkTime(time);
    }

    //De totale load berekenen van de truck geeft terug als hij over zijn max load gaat
    public boolean checkLoadTruck(Truck t) {
        int load = 0;
        if(t.getStops().size() >= 1)
        {
            for(int index = 0; index< (t.getStops().size()-1); index++)
            {
                for(Machine m : t.getStops().get(index).getcollect())
                {
                    load = load + m.getMachineType().getVolume();
                }

                if (t.getStops().get(index).getLocation().getId() != t.getEndLocation().getId()) {
                    for (Machine m : t.getStops().get(index).getdrop()) {
                        load = load - m.getMachineType().getVolume();
                    }
                }

                if(load > 100)
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
                    if (truck.getStops().get(index).getLocation().getId() == truck.getEndLocation().getId())
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

            //    if (truck.getStops().get(truck.getStops().size() - 1).getLocation().getId() != truck.getEndLocation().getId()) {
                    //Location location, Machine collectOrDrop, Request.Type type

                    Stop stop = new Stop(truck.getEndLocation(), null, Request.Type.END);
                    truck.addStopToRoute(stop);

                    for(Machine m : truck.getLoadedMachines())
                    {
                        truck.getStops().get(truck.getStops().size()-1).addDrop(m,false);
                    }

                    int distance = measureDistanceTruck(truck);
                    int time = measureTimeTruck(truck) + truck.getTijdLaden();

                    truck.setCurrentDistance(distance);
                    truck.setCurrentWorkTime(time);
               // }
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

    public void MakeFeasible() {
        Route bestRoute = new Route(route);
        measureTotalDistance(bestRoute);

      /*  HashMap<Integer, List<Request>> requestsByTruck = new HashMap<>();
        for(Request request : requests.values()){
            if(requestsByTruck.containsKey(request.getInTruckId())){
                List<Request> temp = requestsByTruck.get(request.getInTruckId());
                temp.add(request);
                //requestsByTruck.get(request.getInTruckId()).
            }
            else {
                List<Request> temp = new ArrayList<>();
                temp.add(request);
                requestsByTruck.put(request.getInTruckId(), temp);
            }
        }*/

        List<Request> requestsNotFeasible = new ArrayList<>();
        for(Request request : requests.values()){
            if(request.getInTruckId() > 39){
                requestsNotFeasible.add(request);
            }
        }

        while (true) {

            // Hier kiezen we welke request we gaan behandelen
            int randomRequest = random.nextInt(requestsNotFeasible.size());
            Request req = requestsNotFeasible.get(randomRequest);

            /*
            int requestId = -1;
            Request req = null;
            int requestIdCheckIfCorrect = 0;
            while(requestId == -1) {
                //int requestIdCheckIfCorrect = random.nextInt(requests.size() - 1);
                if (bestRoute.getTrucks().size() > 40) {
                    if (requests.get(requestIdCheckIfCorrect).getInTruckId() > 39) {
                        requestId = requestIdCheckIfCorrect;
                        req = requests.get(requestId);
                    }
                }
                else {
                    requestId = requestIdCheckIfCorrect;
                    req = requests.get(requestId);
                }
                requestIdCheckIfCorrect++;
                if(requestIdCheckIfCorrect == requests.size())
                    requestIdCheckIfCorrect = 0;
            }*/

            //   Truck truckToDeleteRequest = rou.getTrucks().get(request.getInTruckId());

            //Hier bereken je naar welke truck we de move doen
            // TODO hardcoded 39
            int toTruckId = random.nextInt(39);

            //De move uitvoeren
            Route returnRoute = DoMove(bestRoute, req, toTruckId);

            // Wanneer returnRoute niet gelijk is aan null, dit wil zeggen dat de move toegelaten is dus de tijd en load van de truck is niet overschreden
            if(returnRoute != null)
            {
                bestRoute = new Route(returnRoute);

                requestsNotFeasible.remove(randomRequest);
                req.setInTruckId(toTruckId);
            }

            // stop?
            // TODO hardcoded 40
            if (requestsNotFeasible.size() == 0) {
                break;
            }
        }
    }

    public boolean checkIfTruckIsEmpty(List<Stop> stops){
        for(Stop stop : stops) {
            if(stop.getcollect().size() != 0 || stop.getdrop().size() != 0)
                return false;
        }
        return true;
    }

    public void meta() {
        //Solution solution = null;

        /* meta settings ----------------------------------- */

        int MAX_IDLE = 100; //100000
        int L = 1000;

        /* create initial solution ------------------------- */

        Route bestRoute = new Route(route);
        measureTotalDistance(bestRoute);

        /* [meta] init ------------------------------------- */
        int idle = 0;
        int count = 0;
        double bound = bestRoute.getTotalDistance();

        /* loop -------------------------------------------- */
        while (true) {

            double oldDist = bestRoute.getTotalDistance();
            // hier kiezen we welke request we gaan behandelen
            int requestId = random.nextInt(requests.size() - 1);
            Request req = requests.get(requestId);

            //TODO hier moves maken naar een truck die geen dummy truck is dus bv bij problem 4 binnen de 40!!!
            //Hier bereken je naar welke truck we de move doen
            int toTruckId = random.nextInt(trucks.size()-1);

            //De move uitvoeren
            Route returnRoute = DoMove(bestRoute, req, toTruckId);

            // Wanneer returnRoute niet gelijk is aan null, dit wil zeggen dat de move toegelaten is dus de tijd en load van de truck is niet overschreden
            if(returnRoute != null)
            {
                double newDist = measureTotalDistance(returnRoute);

                // [meta] accept?
                if (newDist < oldDist || newDist < bound) {
                    idle = 0;
                    if(newDist<bestRoute.getTotalDistance()){
                        bestRoute = new Route(returnRoute);

                        //De truck waar de request aan toegekent is updaten
                        req.setInTruckId(toTruckId);
                        System.out.println("Totale afstand: " + newDist);

                       /* for(Stop stop : bestRoute.getTrucks().get(toTruckId).getStops()) {
                            for(Machine machine : stop.getdrop()){
                                if(machine.getId() == req.getMachine().getId()) {
                                    req.getMachine().setLocation(stop.getLocation());
                                    break;
                                }
                            }
                        }*/
                    }
                }
            }
            else{
                idle++;
                //DoMove(route, req, toTruckId);
                measureTotalDistance(bestRoute);
            }

            // [meta] update
            count++;
            if(count==L){
                count = 0;
                bound = bestRoute.getTotalDistance();
            }

            // stop?
            if (idle >= MAX_IDLE) {
                break;
            }
        }

        /* finished ---------------------------------------- */

        BesteMetaSolution = bestRoute;
    }

    private Route DoMove(Route r, Request request, int toTruckId) {

        Route rou = (Route) deepClone(r);

        Truck truckToDeleteRequest = rou.getTrucks().get(request.getInTruckId());
        Truck truckToAddRequest = rou.getTrucks().get(toTruckId);

        //Collect request
        //Een collect bestaat uit een collect & deze machine droppen op de eindlocatie van de truck (dit nog verbeteren niet persé eindlocatie)
        if(request.getType() == Request.Type.COLLECT)
        {
            Location locatieCollectMachine = null;
            Location locatieDropMachine = null;

            //////////////////////////////////
            /* verwijderen uit huidige truck*/
            //////////////////////////////////


            boolean found = false;
            int indexRemoveLocatie =0;
            int indexfor = 0;
            boolean delete = false;

            // TODO beter alle for lussen vervangen door hashmappen (zal sneller werken maar niet dringend beter de rest eerst)
            boolean foundMachine_1 = false;
            boolean foundMachine_2 = false;
            for(Stop stop : truckToDeleteRequest.getStops())
            {
                //Deel 1 van de collect
                for(Machine machine : stop.getcollect())
                {
                    if(machine.getId() == request.getMachine().getId())
                    {
                        //De locatie opslaan waar de machine staat
                        locatieCollectMachine = stop.getLocation();
                        //Collect gaan verwijderen in oude truck
                        stop.removeCollect(machine);

                        /* stop deleten */
                        //Als in de oude truck na het verwijderen van de collect de stop
                        // niet meer gebruikt wordt deze verwijderen gewoon index opslaan (straks verwijderen anders conflicten)
                        if(stop.getcollect().size() == 0 && stop.getdrop().size() == 0)
                        {
                            delete = true;
                            indexRemoveLocatie = indexfor;
                        }
                        foundMachine_1 = true;
                        //found = true;
                        break;
                    }
                }

                //Deel 2 van de collect
                for(Machine machine : stop.getdrop())
                {
                    if(machine.getId() == request.getMachine().getId())
                    {
                        locatieDropMachine = stop.getLocation();
                        stop.removeDrop(machine);
                        //found = true;
                        foundMachine_2 = true;
                        break;
                    }
                }
//                if(found == true)
//                {
//                    break;
//                }
                indexfor++;
            }

            /* stop deleten */
            //Hier de stop verwijderen zie hierboven
            if(delete)
            {
                truckToDeleteRequest.removeStop(indexRemoveLocatie);
            }

            //truckToDeleteRequest.getLoadedMachines().remove(request.getMachine());

            truckToDeleteRequest.lessLoad(request.getMachine().getMachineType().getVolume());
            //2* omdat laden & lossen eruit gehaald wordt
            truckToDeleteRequest.lessTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));

            int distance = measureDistanceTruck(truckToDeleteRequest);
            int time = measureTimeTruck(truckToDeleteRequest) + truckToDeleteRequest.getTijdLaden();

            truckToDeleteRequest.setCurrentDistance(distance);
            truckToDeleteRequest.setCurrentWorkTime(time);

            ///////////////////////////////
            /* Toevoegen aan nieuwe truck*/
            ///////////////////////////////

            boolean huidigeStopBestaatNogNiet = false;
            Stop stop1 = null;

            //kijken als er al een stop bestaat van met de locatie van de collect
            if(locatieCollectMachine != null) {
                for (Stop stop : truckToAddRequest.getStops()) {
                    if (stop.getLocation().getId() == locatieCollectMachine.getId()) {
                        stop.addCollect(request.getMachine());
                        huidigeStopBestaatNogNiet = true;
                    }
                }

                if (huidigeStopBestaatNogNiet == false) {
                    stop1 = new Stop(locatieCollectMachine, request.getMachine(), Request.Type.COLLECT);
                    //Nieuwe stop steken net voor de laatste huidige stop
                    int index = truckToAddRequest.getStops().size()-1;
                    truckToAddRequest.addStopToRoute(index,stop1);
                }

                //De drop toevoegen aan de trucks op de laatste stop (dus altijd op een depo)
                //TODO veranderen dat dit gelijk welke positie kan zijn
                truckToAddRequest.getStops().get(truckToAddRequest.getStops().size()-1).addDrop(request.getMachine(),false);

                //truckToAddRequest.getLoadedMachines().add(request.getMachine());

                //load toevoegen aan truck
                truckToAddRequest.addLoad(request.getMachine().getMachineType().getVolume());
                //2* omdat laden & lossen eruit gehaald wordt
                truckToAddRequest.addTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));


                distance = measureDistanceTruck(truckToAddRequest);
                time = measureTimeTruck(truckToAddRequest) + truckToAddRequest.getTijdLaden();

                truckToAddRequest.setCurrentDistance(distance);
                truckToAddRequest.setCurrentWorkTime(time);

                if(checkLoadTruck(truckToAddRequest) && truckToAddRequest.CheckIfTimeFitsStop() ) {
                    //Als het past binnen de load en tijd van de truck geef instantie terug
                    //anders returned hij nul
                    return rou;
                }
            }
        }
        else if(request.getType() == Request.Type.DROP)
        {
            Location locatieCollectMachine = null;
            Location locatieDropMachine = null;

            //////////////////////////////////
            // verwijderen uit huidige truck
            //////////////////////////////////

            boolean found = false;

            int indexfor = 0;
            boolean deleteCollect = false;
            boolean deleteDrop = false;
            int indexRemoveLocatieCollect = 0;
            int indexRemoveLocatieDrop = 0;

            boolean foundMachine_1 = false;
            boolean foundMachine_2 = false;

            // TODO beter alle for lussen vervangen door hashmappen (zal sneller werken maar niet dringend beter de rest eerst)
            for(Stop stop : truckToDeleteRequest.getStops())
            {
                //Deel 1 van de drop
                for(Machine machine : stop.getcollect())
                {
                    if(machine.getId() == request.getMachine().getId())
                    {
                        //De locatie opslaan waar de machine staat
                        locatieCollectMachine = stop.getLocation();
                        //Collect gaan verwijderen in oude truck
                        stop.removeCollect(machine);

                        // stop deleten //
                        //Als in de oude truck na het verwijderen van de collect de stop
                        // niet meer gebruikt wordt deze verwijderen gewoon index opslaan (straks verwijderen anders conflicten)
                        if(stop.getcollect().size() == 0 && stop.getdrop().size() == 0)
                        {
                            deleteCollect = true;
                            indexRemoveLocatieCollect = indexfor;
                        }
                        //found = true;
                        foundMachine_1 = true;
                        break;
                    }
                }

                //Deel 2 van de drop
                for(Machine machine : stop.getdrop())
                {
                    if(machine.getId() == request.getMachine().getId())
                    {
                        locatieDropMachine = stop.getLocation();
                        stop.removeDrop(machine);

                        // stop deleten //
                        //Als in de oude truck na het verwijderen van de collect de stop
                        // niet meer gebruikt wordt deze verwijderen gewoon index opslaan (straks verwijderen anders conflicten)
                        if(stop.getcollect().size() == 0 && stop.getdrop().size() == 0)
                        {
                            deleteDrop = true;
                            indexRemoveLocatieDrop = indexfor;
                        }
                        //found = true;
                        foundMachine_2 = true;
                        break;
                    }
                }
//                if(found == true)
//                {
//                    break;
//                }
                indexfor++;
            }


            // stop deleten //
            //Hier de stop verwijderen zie hierboven
            if(deleteDrop && indexRemoveLocatieCollect != indexRemoveLocatieDrop)
            {
                if(truckToDeleteRequest.getStops().size()-1 != indexRemoveLocatieDrop && indexRemoveLocatieDrop != 0)
                    truckToDeleteRequest.removeStop(indexRemoveLocatieDrop);
            }

            if(deleteCollect)
            {
                if(truckToDeleteRequest.getStops().size()-1 != indexRemoveLocatieCollect && indexRemoveLocatieCollect != 0)
                    truckToDeleteRequest.removeStop(indexRemoveLocatieCollect);
            }

            //truckToDeleteRequest.getLoadedMachines().remove(request.getMachine());

            truckToDeleteRequest.lessLoad(request.getMachine().getMachineType().getVolume());
            //2* omdat laden & lossen eruit gehaald wordt
            truckToDeleteRequest.lessTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));


            int distance = measureDistanceTruck(truckToDeleteRequest);
            int time = measureTimeTruck(truckToDeleteRequest) + truckToDeleteRequest.getTijdLaden();

            truckToDeleteRequest.setCurrentDistance(distance);
            truckToDeleteRequest.setCurrentWorkTime(time);

            ///////////////////////////////
            // Toevoegen aan nieuwe truck//
            ///////////////////////////////

            boolean huidigeStopBestaatNogNietCollect = false;
            boolean huidigeStopBestaatNogNietDrop = false;
            Stop stop1 = null;
            Stop stop2 = null;

            //kijken als er al een stop bestaat van met de locatie van de collect
            if(locatieCollectMachine != null && locatieDropMachine != null) {
                // TODO ADDCOLLECT moet gebeuren vanwaar de machine komt!
                for (Stop stop : truckToAddRequest.getStops()) {
                    if (stop.getLocation().getId() == locatieCollectMachine.getId()) {
                        stop.addCollect(request.getMachine());
                        huidigeStopBestaatNogNietCollect = true;
                        break;
                    }
                }
                if (huidigeStopBestaatNogNietCollect == false) {
                    stop1 = new Stop(locatieCollectMachine, request.getMachine(), Request.Type.TEMPORARYCOLLECT);
                    //Nieuwe stop steken net voor de laatste huidige stop
                    int index = truckToAddRequest.getStops().size()-1;
                    truckToAddRequest.addStopToRoute(index,stop1);
                }
                for (Stop stop : truckToAddRequest.getStops()) {
                    if (stop.getLocation().getId() == locatieDropMachine.getId() && VolgordeChecken(truckToAddRequest.getStops(), request.getMachine())) {
                        stop.addDrop(request.getMachine(), false);
                        huidigeStopBestaatNogNietDrop = true;
                        break;
                    }
                }
                if (huidigeStopBestaatNogNietDrop == false) {
                    stop2 = new Stop(locatieDropMachine, request.getMachine(), Request.Type.DROP);
                    //Nieuwe stop steken net voor de laatste huidige stop
                    int index = truckToAddRequest.getStops().size()-1;
                    truckToAddRequest.addStopToRoute(index,stop2);
                }

                //De drop toevoegen aan de trucks op de laatste stop (dus altijd op een depo)
                //TODO veranderen dat dit gelijk welke positie kan zijn
                //truckToAddRequest.getStops().get(truckToAddRequest.getStops().size()-1).addDrop(request.getMachine(),false);

                //truckToAddRequest.getLoadedMachines().add(request.getMachine());

                //load toevoegen aan truck
                truckToAddRequest.addLoad(request.getMachine().getMachineType().getVolume());
                //2* omdat laden & lossen eruit gehaald wordt
                truckToAddRequest.addTijdLaden((2*request.getMachine().getMachineType().getServiceTime()));


                distance = measureDistanceTruck(truckToAddRequest);
                time = measureTimeTruck(truckToAddRequest) + truckToAddRequest.getTijdLaden();

                truckToAddRequest.setCurrentDistance(distance);
                truckToAddRequest.setCurrentWorkTime(time);

                if(checkLoadTruck(truckToAddRequest) && truckToAddRequest.CheckIfTimeFitsStop() ) {
                    //Als het past binnen de load en tijd van de truck geef instantie terug
                    //anders returned hij nul
                    return rou;
                }
            }
        }

        //nul retunen => wil zeggen past niet in de truck
        return null;
    }

    // Kijken dat het toestel eerst opgehaald wordt en daarna pas gedropt wordt!
    public boolean VolgordeChecken(List<Stop> stops, Machine dropmachine) {
        for(Stop stop : stops) {
            for(Machine machine : stop.getcollect()){
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
        for (Truck truck : BesteMetaSolution.getTrucks()) {
            if(truck.getStops().size() > 1){
                numberOfUsedTrucks++;
            }
        }

        int totalDistance = measureTotalDistance(BesteMetaSolution);


        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.OUTPUT_FILE));
        writer.write("PROBLEM: " + Main.INPUT_FILE + "\n");
        writer.write("DISTANCE: " + totalDistance + "\n");
        writer.write("TRUCKS: " + numberOfUsedTrucks + "\n");

        for (Truck truck : BesteMetaSolution.getTrucks()) {
            // Enkel trucks die rijden uitprinten
            if(truck.getStops().size() > 2)
            {
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
                writer.write("\n");
                System.out.print("\n");
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