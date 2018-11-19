package Main;

import Heuristiek.*;

public class Main {
    /**
     * Valideren:
     java -jar tvh-1.0-student.jar --problem=tvh_problem_3.txt --solution=tvh_solution_3.txt --gui
     java -jar tvh-1.0-student.jar --problem=tvh_problem_4.txt --solution=tvh_solution_4.txt --gui
     */

    public static String INPUT_FILE;
    public static String OUTPUT_FILE;

    public static void main(String[] args) throws Exception{
        INPUT_FILE = args[0];
        OUTPUT_FILE = args[1];

        FileIO.readInput(INPUT_FILE);
        Problem.solve();
    }
}