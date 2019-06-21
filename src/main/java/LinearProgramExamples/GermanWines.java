package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class GermanWines {

    public LinearProgram lp;

    // German wines
    public GermanWines() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(1., 1., 2., 2.)),
                new ArrayList<>(Arrays.asList(2., 0., 1., 3.)),
                new ArrayList<>(Arrays.asList(0., 2., 0., 1.))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList("<=", "<=", "<=", "<="));
        double [] rhs = new double[] {150., 150., 80., 225.};

        lp = new LinearProgram(matrix, constSigns, rhs);
    }


}
