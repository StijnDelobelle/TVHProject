package Data;

import Objects.*;
import java.util.*;
import java.io.*;

import static Heuristiek.Problem.*;

public class FileIO {

    public static void readInput(String filename) throws FileNotFoundException {

        //InputStream is = FileIO.class.getResourceAsStream(filename);
        //InputStreamReader isr = new InputStreamReader(is);
        //BufferedReader br = new BufferedReader(isr);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        Scanner s = new Scanner(bufferedReader);

        int requestId = 0;

        while (s.hasNextLine()) {
            String actualRead = s.nextLine();
            if(actualRead.contains("TRUCK_CAPACITY"))
            {
                String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                TRUCK_CAPACITY = Integer.parseInt(splited[1]);
            }
            if(actualRead.contains("TRUCK_WORKING_TIME"))
            {
                String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                TIME_CAPACITY = Integer.parseInt(splited[1]);
            }
            else if(actualRead.contains("LOCATIONS"))
            {
                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    int id = Integer.parseInt(splited[0]);
                    double lat = Double.parseDouble(splited[1]);
                    double lon = Double.parseDouble(splited[2]);
                    String name = splited[3];

                    locations.add(new Location(id, lat, lon, name));

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("DEPOTS"))
            {
                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                    Location location = locations.get(Integer.parseInt(splited[1]));
                    location.setDepot(true);
                    depots.add(location);

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("TRUCKS"))
            {
                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    int id = Integer.parseInt(splited[0]);
                    Location startLocation = locations.get(Integer.parseInt(splited[1]));
                    Location endLocation = locations.get(Integer.parseInt(splited[2]));

                    trucks.add(new Truck(id, startLocation, endLocation));

                    if(s.hasNextLine())
                    {
                        actualRead = s.nextLine();
                    }
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("MACHINE_TYPES"))
            {
                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                    int id = Integer.parseInt(splited[0]);
                    int volume = Integer.parseInt(splited[1]);
                    int serviceTime = Integer.parseInt(splited[2]);
                    String name = splited[3];
                    machineTypes.add(new MachineType(id, volume, serviceTime, name));

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("MACHINES"))
            {
                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    int id = Integer.parseInt(splited[0]);
                    MachineType machineType = machineTypes.get(Integer.parseInt(splited[1]));
                    Location location = locations.get(Integer.parseInt(splited[2]));

                    machines.add(new Machine(id, machineType, location));

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
			else if(actualRead.contains("DROPS"))
			{
				actualRead = s.nextLine();
				while(!actualRead.equals(""))
				{
					String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

					MachineType machineType = machineTypes.get(Integer.parseInt(splited[1]));
					Location location = locations.get(Integer.parseInt(splited[2]));

					requests.add(new Request(requestId++,null, machineType, location, Request.Type.DROP));

					if(s.hasNextLine())
						actualRead = s.nextLine();
					else
						actualRead = "";
				}
			}
            else if(actualRead.contains("COLLECTS"))
            {
                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    Machine machine = machines.get(Integer.parseInt(splited[1]));
                    MachineType machineType = machine.getMachineType();

                    requests.add(new Request(requestId++, machine, machineType, null, Request.Type.COLLECT));

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("TIME_MATRIX"))
            {
                String[] split = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                int length = Integer.parseInt(split[1]);
                timeMatrix = new int[length][length];
                actualRead = s.nextLine();
                int i = 0;
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    int j=0;
                    for (String v: splited)
                    {
                        timeMatrix[i][j] = Integer.parseInt(v);
                        j++;
                    }
                    i++; // Lijn lager

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("DISTANCE_MATRIX"))
            {
                String[] split = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                int length = Integer.parseInt(split[1]);
                distanceMatrix = new int[length][length];
                actualRead = s.nextLine();
                int i = 0;
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    int j=0;
                    for (String v: splited)
                    {
                        distanceMatrix[i][j] = Integer.parseInt(v);
                        j++;
                    }
                    i++; // Lijn lager

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
        }
        s.close();
    }
}
