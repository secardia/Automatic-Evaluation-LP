package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class DairyProduct {

    public LinearProgram lp;

    // Exemple big (dairy product)
    public DairyProduct() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList(5., 3., 2., 15., 10.)),
            new ArrayList<>(Arrays.asList(5., 0., 5., 30., 10.)),
            new ArrayList<>(Arrays.asList(2., 0., 4., 10., 6.)),
            new ArrayList<>(Arrays.asList(0., 0., 0., 0., 1.))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList("<=", "<=", "<=", "<=", "=="));
        double [] rhs = new double[] { 3000, 1000, 4000, 250*60, 8000 };

        lp = new LinearProgram(matrix, constSigns, rhs);

    }


}
