package Heuristiek;

import Objects.*;
import Main.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static Heuristiek.Problem.distanceMatrix;
import static Heuristiek.Problem.machines;
import static Heuristiek.Problem.timeMatrix;

public class Solution  {

    private Route bestRoute;
    private ArrayList<Truck> initialTrucks;
    private ArrayList<Request> initialRequests;

    public void InitialSolution(ArrayList<Request> argRequests, ArrayList<Truck> argTrucks) {

        /** MACHINE_TYPES: [id volume serviceTime name]  **/
        /** MACHINES:      [id machineTypeId locationId] **/
        /** DROPS:         [id machineTypeId locationId] **/
        /** COLLECTS:      [id machineId]                **/

        initialRequests = (ArrayList<Request>) deepClone(argRequests);
        initialTrucks = (ArrayList<Truck>) deepClone(argTrucks);

        Random random = new Random(0);
        Collections.shuffle(initialRequests, random);

        HashMap<Integer, Request> requests = new HashMap<>();
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

                Ride ride = null;

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

                    ride = new Ride(null, request.getLocation(), pickupLocation, pickupMachine, Request.Type.DROP);

                    machines.get(pickupMachine.getId()).setLocked(true);
                }

                /****** COLLECTS ******/
                if (request.getType() == Request.Type.COLLECT) {
                    ride = new Ride(null, request.getMachine().getLocation(), null, request.getMachine(), Request.Type.COLLECT);

                    machines.get(request.getMachine().getId()).setLocked(true);
                }

                AddToTruck(ride);
                requests.get(request.getId()).setDone(true);
            }
        }

        /** Alle truck terug naar depot sturen **/
        SendTrucksToHome();

        /** Initiele Oplossing **/
        bestRoute = new Route(initialTrucks);
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

    public void AddToTruck(Ride ride) {
        if(ride.getType() == Request.Type.DROP)
        {
            SearchClosestTruckDrop(ride);
        }
        else if(ride.getType() == Request.Type.COLLECT)
        {
            SearchClosestTruckCollect(ride);
        }
    }

    public void SearchClosestTruckDrop(Ride currentRide){
        Location pickupLocation = currentRide.getPickupLocation();
        int load = currentRide.getMachine().getMachineType().getVolume();
        int serviceTime = currentRide.getMachine().getMachineType().getServiceTime();

        Location startLocation = null;
        int minDistance = Integer.MAX_VALUE;
        Truck candidateTruck = null;

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
                int distance = distanceMatrix[truck.getCurrentLocation().getId()][pickupLocation.getId()] + distanceMatrix[pickupLocation.getId()][currentRide.getToLocation().getId()];
                if(minDistance > distance)
                {
                    /** Controleren of die er nog bij kan **/
                    int rideTime = timeMatrix[truck.getCurrentLocation().getId()][pickupLocation.getId()] + timeMatrix[pickupLocation.getId()][currentRide.getToLocation().getId()];
                    if(truck.CheckIfLoadFits(load) && truck.CheckIfTimeFits(rideTime, serviceTime, currentRide.getToLocation()) ) {
                        minDistance = distance;
                        startLocation = truck.getCurrentLocation();
                        candidateTruck = truck;
                    }
                }
            //}
        }

        /** Van start locatie naar pickup locatie **/
        Ride start_pickup = new Ride(startLocation, currentRide.getPickupLocation(), null, currentRide.getMachine(), Request.Type.TEMPORARYCOLLECT);
        initialTrucks.get(candidateTruck.getId()).addPointToRoute(start_pickup);
        /** Van pickup locatie naar eindlocatie **/
        Ride pickup_end = new Ride(currentRide.getPickupLocation(), currentRide.getToLocation(), null, currentRide.getMachine(), Request.Type.DROP);
        initialTrucks.get(candidateTruck.getId()).addPointToRoute(pickup_end);

        //machines.get(currentRide.getMachine().getId()).setLocked(true);
    }

    public void SearchClosestTruckCollect(Ride currentRide){
        Truck candidateTruck = null;
        Location startLocation = null;
        int minDistance = Integer.MAX_VALUE;
        int load = currentRide.getMachine().getMachineType().getVolume();
        int serviceTime = currentRide.getMachine().getMachineType().getServiceTime();

        /** Per truck **/
        for(Truck truck : initialTrucks)
        {
            //ArrayList<Ride> rides = (ArrayList<Ride>) deepClone(truck.getRoute());

            /** Truck heeft nog geen ritten gedaan => Startlocatie van de truck nemen **/
            //if(rides.size() == 0){
            //    rides.add(new Ride(null, truck.getStartLocation(), null, null, null));
            //}

            //for(Ride ride : rides){
                /** Van de eindlocatie van de vorige rit naar de collectlocatie **/
                int distance = distanceMatrix[truck.getCurrentLocation().getId()][currentRide.getToLocation().getId()];
                if(minDistance > distance)
                {
                    /** Controleren of die er nog bij kan **/
                    int rideTime = timeMatrix[truck.getCurrentLocation().getId()][currentRide.getToLocation().getId()];
                    if(truck.CheckIfLoadFits(load) && truck.CheckIfTimeFits(rideTime, serviceTime, currentRide.getToLocation()) ) {
                        minDistance = distance;
                        startLocation = truck.getCurrentLocation();
                        candidateTruck = truck;
                    }
                }
            //}
        }

        Ride start_end = new Ride(startLocation, currentRide.getToLocation(), null, currentRide.getMachine(), Request.Type.COLLECT);
        initialTrucks.get(candidateTruck.getId()).addPointToRoute(start_end);

        //machines.get(currentRide.getMachine().getId()).setLocked(true);
    }

    public void SendTrucksToHome(){
        for(Truck truck : initialTrucks)
        {
            if(truck.getCurrentLocation().getId() != truck.getEndLocation().getId())
            {
                truck.addPointToRoute(new Ride(truck.getCurrentLocation(), truck.getEndLocation(), null, null, Request.Type.END));
            }
        }
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
        writer.write("DISTANCE: " + bestRoute.getTotalDistance() + "\n");
        writer.write("TRUCKS: " + numberOfUsedTrucks + "\n");

        for (Truck truck : bestRoute.getTrucks()) {
            // Enkel trucks die rijden uitprinten
            if(truck.getRoute().size() > 1)
            {
                writer.write(truck.getId() + " " + truck.getCurrentDistance() + " " + truck.getCurrentWorkTime());
                System.out.print(truck.getId() + " " + truck.getCurrentDistance() + " " + truck.getCurrentWorkTime());

                // First location print (startlocation)
                int currentLocationID = truck.getStartLocation().getId();
                writer.write(" " + truck.getStartLocation().getId());
                System.out.print(" " + truck.getStartLocation().getId());

                // Route
                for (Ride ride : truck.getRoute()) {
                    if(ride.getType() != Request.Type.END)
                    {
                        if(currentLocationID != ride.getToLocation().getId())
                        {
                            currentLocationID = ride.getToLocation().getId();
                            writer.write(" " + ride.getToLocation().getId() + ":" + ride.getMachine().getId());
                            System.out.print(" " + ride.getToLocation().getId() + ":" + ride.getMachine().getId());
                        }
                        else
                        {
                            writer.write(":" + ride.getMachine().getId());
                            System.out.print(":" + ride.getMachine().getId());
                        }
                    }
                    else
                    {
                        writer.write(" " + ride.getToLocation().getId());
                        System.out.print(" " + ride.getToLocation().getId());

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
        System.out.println("\nTotale afstand: " + bestRoute.getTotalDistance()+"\n");
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