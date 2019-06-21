package LinearProgramExamples;

import Model.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

public class Octohedre {

    public LinearProgram lp;

    // Exemple custom type octohèdre (3 var, 8 contraintes, 6 points, 4 voisins chacun)
    public Octohedre() {
        // Contraintes
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(-1., 1.,  1.,  1., -1., 1.,  -1., 1.)),
                new ArrayList<>(Arrays.asList(-1., -1., -1., 1., -1., -1., 1.,  1.)),
                new ArrayList<>(Arrays.asList(1.,  1.,  -1., 1., 1.,  1.,  1.,  1.))
        ));
        ArrayList<String> constSigns = new ArrayList<>(Arrays.asList(">=", ">=", "<=", ">=", "<=", "<=", "<=", "<="));
        double[] rhs = new double[]{-2, 0, 0, 2, 0, 2, 2, 4};
        lp = new LinearProgram(matrix, constSigns, rhs);
    }
}
