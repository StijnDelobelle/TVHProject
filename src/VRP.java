/*
 * Created by Nikolaos Michail
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class  VRP{

    public VRP(VRProblem problem) throws Exception{

        //Problem Parameters
        int NoOfCustomers = problem.size();
        int NoOfVehicles = 40;
        int VehicleCap = 100;

        //Tabu Parameter
        int TABU_Horizon = 10;

        //Initialise
        ArrayList<Customer> customers = problem.customers;
        ArrayList<Customer> depot = problem.depot;
        Vehicle[] vehicles = problem.trucks;


        for(Customer c : depot)
        {
            c.IsDepot = true;
        }

        //Customer depot = problem.depot;
        int[][] distanceMatrix = problem.distanceMatrix;
        int[][] timeMatrix = problem.timeMatrix;
        //Compute the greedy Solution
        System.out.println("Attempting to resolve Vehicle Routing Problem (VRP) for "+NoOfCustomers+
                " Customers and "+NoOfVehicles+" Vehicles"+" with "+VehicleCap + " units of capacity\n");

        Solution s = new Solution(NoOfCustomers,NoOfVehicles,VehicleCap,vehicles);

        ArrayList<Route> ar = new ArrayList<>();
        double minCost = Integer.MAX_VALUE;
        Route bestRoute = null;


        for(int i = 0;i<1;i++)
        {
            Route r = s.GreedySolution(customers,distanceMatrix,timeMatrix,i);

            if(r.totalCost<minCost)
            {
                minCost = r.totalCost;
                bestRoute = r;
            }
        }


        s.SolutionPrint("Greedy Solution",bestRoute);

//        s.InterRouteLocalSearch(distanceMatrix,timeMatrix);
//
//        s.SolutionPrint("Solution after Inter-Route Heuristic Neighborhood Search");
//
//
//        s.GreedySolution(customers,distanceMatrix,timeMatrix);

        s.TabuSearch(TABU_Horizon,distanceMatrix,timeMatrix,bestRoute);

        s.SolutionPrint("Solution after TabuSearch Heuristic Neighborhood Search", bestRoute);
    }
}

//class draw
//{
//    public static void  drawRoutes(Solution s, String fileName) {
//
//        int VRP_Y = 800;
//        int VRP_INFO = 200;
//        int X_GAP = 600;
//        int margin = 30;
//        int marginNode = 1;
//
//
//        int XXX = VRP_INFO + X_GAP;
//        int YYY = VRP_Y;
//
//
//        BufferedImage output = new BufferedImage(XXX, YYY, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = output.createGraphics();
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, XXX, YYY);
//        g.setColor(Color.BLACK);
//
//
//        double minX = Double.MAX_VALUE;
//        double maxX = Double.MIN_VALUE;
//        double minY = Double.MAX_VALUE;
//        double maxY = Double.MIN_VALUE;
//
//        for (int k = 0; k < s.Vehicles.length ; k++)
//        {
//            for (int i = 0; i < s.Vehicles[k].Route.size(); i++)
//            {
//                Node n = s.Vehicles[k].Route.get(i);
//                if (n.Node_X > maxX) maxX = n.Node_X;
//                if (n.Node_X < minX) minX = n.Node_X;
//                if (n.Node_Y > maxY) maxY = n.Node_Y;
//                if (n.Node_Y < minY) minY = n.Node_Y;
//
//            }
//        }
//
//        int mX = XXX - 2 * margin;
//        int mY = VRP_Y - 2 * margin;
//
//        int A, B;
//        if ((maxX - minX) > (maxY - minY))
//        {
//            A = mX;
//            B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
//            if (B > mY)
//            {
//                B = mY;
//                A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
//            }
//        }
//        else
//        {
//            B = mY;
//            A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
//            if (A > mX)
//            {
//                A = mX;
//                B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
//            }
//        }
//
//        // Draw Route
//        for (int i = 0; i < s.Vehicles.length ; i++)
//        {
//            for (int j = 1; j < s.Vehicles[i].Route.size() ; j++) {
//                Node n;
//                n = s.Vehicles[i].Route.get(j-1);
//
//                int ii1 = (int) ((double) (A) * ((n.Node_X - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
//                int jj1 = (int) ((double) (B) * (0.5 - (n.Node_Y - minY) / (maxY - minY)) + (double) mY / 2) + margin;
//
//                n = s.Vehicles[i].Route.get(j);
//                int ii2 = (int) ((double) (A) * ((n.Node_X - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
//                int jj2 = (int) ((double) (B) * (0.5 - (n.Node_Y - minY) / (maxY - minY)) + (double) mY / 2) + margin;
//
//
//                g.drawLine(ii1, jj1, ii2, jj2);
//            }
//        }
//
//        for (int i = 0; i < s.Vehicles.length ; i++)
//        {
//            for (int j = 0; j < s.Vehicles[i].Route.size() ; j++) {
//
//                Node n = s.Vehicles[i].Route.get(j);
//
//                int ii = (int) ((double) (A) * ((n.Node_X  - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
//                int jj = (int) ((double) (B) * (0.5 - (n.Node_Y - minY) / (maxY - minY)) + (double) mY / 2) + margin;
//                if (i != 0) {
//                    g.fillOval(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode); //2244
//                    String id = Integer.toString(n.NodeId);
//                    g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
//                } else {
//                    g.fillRect(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode);  //4488
//                    String id = Integer.toString(n.NodeId);
//                    g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
//                }
//            }
//
//        }
//
//        String cst = "VRP solution for "+s.NoOfCustomers+ " customers with Cost: " + s.Cost;
//        g.drawString(cst, 10, 10);
//
//        fileName = fileName + ".png";
//        File f = new File(fileName);
//        try
//        {
//            ImageIO.write(output, "PNG", f);
//        } catch (IOException ex) {
//            //  Logger.getLogger(s.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
//}