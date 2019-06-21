package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class CuncumbersAndOnions {

    public LinearProgram lp;

    // Concombres et onions
    public CuncumbersAndOnions() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(2., 1., 0.)),
                new ArrayList<>(Arrays.asList(1., 2., 1.))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList("<=", "<=", "<="));
        double [] rhs = new double[] {8., 7., 3.};

        lp = new LinearProgram(matrix, constSigns, rhs);
    }


}
