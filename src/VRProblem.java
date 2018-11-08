import java.util.*;
import java.io.*;


public class VRProblem {
    public String id;
    ArrayList<Customer> depot;
    ArrayList<Customer> customers;
    HashMap<Integer, Integer> machine_Types = new HashMap<Integer, Integer>();
    //als output een collection nog keer opzoeken
    ArrayList<Machine> machines = new ArrayList<Machine>();
    Vehicle[] trucks = new Vehicle[40];

    public int[][] timeMatrix = new int[28][28];
    public int[][] distanceMatrix = new int[28][28];


    public VRProblem(String filename) throws Exception{
        this.id = filename;
        customers = new ArrayList<>();
        depot = new ArrayList<>();

        Scanner s = null;

        try {
            s = new Scanner(new File("src/" + id));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (s.hasNextLine()) {
            String actualRead = s.nextLine();

            if(actualRead.contains("DEPOTS")) {

                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                    depot.add(new Customer(Integer.parseInt(splited[1]),	0));


                    if(s.hasNextLine())
                        actualRead = s.nextLine();

                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("TRUCKS"))
            {
                actualRead = s.nextLine();
                int index = 0;
                while(!actualRead.equals(""))
                {


                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                    //Customer c = new Customer(Integer.parseInt(splited[1]),0);
                    trucks[index] = new Vehicle(Integer.parseInt(splited[0])+1,100,600,Integer.parseInt(splited[1]));

                    if(s.hasNextLine())
                    {
                        actualRead = s.nextLine();
                        index++;
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
                    machine_Types.put(Integer.parseInt(splited[0]), Integer.parseInt(splited[1]));

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
                    machines.add(new Machine(Integer.parseInt(splited[0]),Integer.parseInt(splited[1]),Integer.parseInt(splited[2])));

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
//			else if(actualRead.contains("DROPS"))
//			{
//				actualRead = s.nextLine();
//				while(!actualRead.equals(""))
//				{
//					String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
//					customers.add(new Customer(Integer.parseInt(splited[1]),	0));
//
//					if(s.hasNextLine())
//						actualRead = s.nextLine();
//					else
//						actualRead = "";
//				}
//			}
            else if(actualRead.contains("COLLECTS"))
            {

                actualRead = s.nextLine();
                while(!actualRead.equals(""))
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                    customers.add(new Customer(-1,Integer.parseInt(splited[1])));

                    if(s.hasNextLine())
                        actualRead = s.nextLine();
                    else
                        actualRead = "";
                }
            }
            else if(actualRead.contains("TIME_MATRIX"))
            {
                int i = 0;
                actualRead = s.nextLine();
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
                int i = 0;
                actualRead = s.nextLine();
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

        getWeightCostumer();
        s.close();
    }

    public void getWeightCostumer() {
        for (Customer c : customers) {
            if(c != null)
            {
                int typeId = machines.get(c.machineId).machineTypeId;
                int weightMachine = machine_Types.get(typeId);
                int locationId = machines.get(c.machineId).locationId;
                c.c = weightMachine;
                c.locationId = locationId;
            }
        }
    }
    public int size(){
        return this.customers.size();
    }
}
