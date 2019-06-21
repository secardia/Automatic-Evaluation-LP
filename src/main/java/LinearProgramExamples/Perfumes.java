package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class Perfumes {

    public LinearProgram lp;

    // Perfumes
    public Perfumes() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(1., 0., 3.)),
                new ArrayList<>(Arrays.asList(0., 2., 2.))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList("<=", "<=", "<="));
        double [] rhs = new double[] {4., 12., 18.};

        lp = new LinearProgram(matrix, constSigns, rhs);
    }


}
