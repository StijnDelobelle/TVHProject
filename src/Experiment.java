import java.util.*;
public class Experiment {

    public static void main(String[] args)throws Exception{
        String outdir = "output/";
        //String problemdir = "tests/";
        String [] probs = {
                "tvh_problem_3"
        };

        for (String f:probs){
            VRProblem vrp = new VRProblem(f +".txt");
            VRP vrp_solution = new VRP(vrp);

        }
    }
}
