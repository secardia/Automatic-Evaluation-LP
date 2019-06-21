package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class OliveOil {

    public LinearProgram lp;

    // Huiles d'olives
    public OliveOil() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(1., 0., 0., 1., 0.)),
                new ArrayList<>(Arrays.asList(0., 1., 0., 0., 1.)),
                new ArrayList<>(Arrays.asList(-1., 0.6, 0.3, -1., 0.6)),
                new ArrayList<>(Arrays.asList(0., -1., 0.8, 0., -1.))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList("<=", "<=", "<=", ">=", ">="));
        double[] rhs = new double[]{3000, 3000, 2000, 0, 0};

        lp = new LinearProgram(matrix, constSigns, rhs);
    }
}
