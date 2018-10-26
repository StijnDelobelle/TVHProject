import java.io.*;
import java.util.Scanner;

public class Input {
    String actualRead;

    public int[][] adjacentMatrix = new int[28][28];

    public void LoadFile() {
        Scanner s = null;
        try {
            s = new Scanner(new File("src/tvh_problem_1.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (s.hasNextLine()) {
            actualRead = s.nextLine();
            if(actualRead.contains("DISTANCE_MATRIX"))
            {
                int i = 0;
                actualRead = s.nextLine();
                while(actualRead != "")
                {
                    String[] splited = actualRead.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");

                    int j=0;
                    for (String v: splited)
                    {
                        adjacentMatrix[i][j] = Integer.parseInt(v);
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
