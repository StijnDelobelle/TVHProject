package Main;

import Data.FileIO;
import Data.Param;
import Heuristiek.*;

public class Main {

    /** Validator **/
    // java -jar tvh-3.0-student.jar --problem=tvh_problem_4.txt --solution=tvh_solution_4.txt --gui

    /** Application **/
    // java -jar algorithm.jar --problem=<input_file> --solution=<solution_file> --seed=<random_seed> --time=<time_limit>
    // java -jar algorithm.jar --problem=tvh_problem_3.txt --solution=tvh_solution_3.txt --seed=0 --time=10

    public static String INPUT_FILE;
    public static String SOLUTION_FILE;
    public static int RANDOM_SEED;
    public static int TIME_LIMIT ;

    public static void main(String[] args) throws Exception{

        Param param = new Param(args);

        if (!param.getNamed().containsKey("problem") || !param.getNamed().containsKey("solution") || !param.getNamed().containsKey("seed") || !param.getNamed().containsKey("time")) {
            System.out.println("Usage: java -jar algorithm.jar --problem=<input_file> --solution=<solution_file> --seed=<random_seed> --time=<time_limit>");
        }
        else {

            INPUT_FILE = param.getNamed().get("problem");
            SOLUTION_FILE = param.getNamed().get("solution");
            RANDOM_SEED = Integer.parseInt(param.getNamed().get("seed"));
            TIME_LIMIT = Integer.parseInt(param.getNamed().get("time"));

            FileIO.readInput(INPUT_FILE);
            Problem.solve();
        }
    }
}