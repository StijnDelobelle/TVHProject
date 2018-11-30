package Main;

import Heuristiek.*;

public class Main {

    /** java -jar tvh-3.0-student.jar --problem=tvh_problem_4.txt --solution=tvh_solution_4.txt --gui **/

    public static String INPUT_FILE;
    public static String SOLUTION_FILE;

    public static final int MAX_IDLE = 1000;
    public static final int RANDOM_SEED = 0;
    public static final int TIME_LIMIT = 1800 ; // In minuten 30 min => 1800

    public static void main(String[] args) throws Exception{
        INPUT_FILE = args[0];
        SOLUTION_FILE = args[1];

        FileIO.readInput(INPUT_FILE);
        Problem.solve();
    }
}