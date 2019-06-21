package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class Apples {

    public LinearProgram lp;

    // Apples
    public Apples() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(500., 0.,   500., 0.,   0.)),
                new ArrayList<>(Arrays.asList(0.,   250., 0.,   250., 0.)),
                new ArrayList<>(Arrays.asList(-1.,  0.6, -1.,  0.6, 0.)),
                new ArrayList<>(Arrays.asList(0.,   -1.,  0.,   -1.,  0.4))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList("<=", "<=", ">=", ">=", "<="));
        double[] rhs = new double[]{5000, 2000, 0, 0, 500};

        lp = new LinearProgram(matrix, constSigns, rhs);
    }
}
