package Model;


import org.apache.commons.math.linear.*;

import java.util.ArrayList;

public class Basis {

    private LinearProgram lp;

    // Valeurs des variables de la base
    private ArrayList<Double> coord;

    // Variables en base, sous la forme d'un tableau d'indices de la matrice décrivant les contraintes
    private ArrayList<Integer> inVarIndices;

    // Variables hors base, sous la forme d'un tableau d'indices de la matrice décrivant les contraintes
    private ArrayList<Integer> outVarIndices;


    /* ------------------ Constructeurs ------------------ */


    /**
     * Construit une base à partir de la liste de variables spécifiée et du programme linéaire auquel elle correspond.
     * Cette liste sera la liste des variables en base.
     * @param inVarIndices liste des variables en base
     */
    public Basis(ArrayList<Integer> inVarIndices, LinearProgram lp) {
        if (inVarIndices.size() == lp.getConstNb()) {
            this.lp = lp;
            // On set les variables en base avec la liste spécifiée
            this.inVarIndices = new ArrayList<>(inVarIndices);
            // On initialise les variables hors base (toutes celles qui ne sont pas en base)
            this.outVarIndices = getInitOutVarIndices();
            // On calcul les coordonnés de la base
            this.coord = computeCoord();
        } else {
            throw new IllegalArgumentException("You try to initialise a basis with "+inVarIndices.size()+" variables in" +
                    "the basis but it requires "+lp.getConstNb()+".");
        }
    }


    /**
     * Construit une copie de la base spécifiée.
     * @param basisToCopy base à copier
     */
    public Basis(Basis basisToCopy) {
        this.lp = basisToCopy.lp;
        this.inVarIndices = new ArrayList<>(basisToCopy.inVarIndices);
        this.outVarIndices = new ArrayList<>(basisToCopy.outVarIndices);
        // On calcul les coordonnés de la base
        this.coord = computeCoord();
    }


    /* ------------------ Fonctions ------------------ */


    /**
     * Compare l'objet spécifié avec cette base pour vérifier s'ils sont égaux. Deux bases sont égales si elles ont
     * les mêmes variables en base.
     * @param obj la base à comparer
     * @return true si l'objet spécifié est une base et s'il a les mêmes variables en bases que
     * cette base, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Basis) {
            // Si this ou obj ne sont pas valides on retourne false
            if (isInvalid() || ((Basis) obj).isInvalid()) {
                return false;
            } else {
                // Si une des var en base diffère on renvoie false
                for (int i = 0; i < inVarIndices.size(); i++) {
                    Integer a = inVarIndices.get(i);
                    Integer b = ((Basis) obj).inVarIndices.get(i);
                    if (!a.equals(b)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Retourne true si la solution de base est une solution réalisable.
     * @return true si la solution de base est une solution réalisable, false sinon
     */
    public boolean isAFeasibleSolution() {
        // Si la base est invalid elle n'est pas réalisable (contraintes parallèles)
        if (isInvalid()) {
            return false;
        }
        // On check le signe des variables
        for (int i = 0; i < lp.getVarNb(); i++) {
            if (coord.get(i) < 0) {
                return false;
            }
        }
        // On check le signe des variables d'écart (elles doivent être toutes positives)
        for (int i = 0; i < lp.getSlackVarNb(); i++) {
            if (coord.get(lp.getVarNb()+i) < 0) {
                return false;
            }
        }
        // On check si les contraintes sont respectées
        int i = 0;
        while (i < lp.getConstNb()) {
            // On calcul la valeur associée à la contraintes
            double sum = 0.;
            for (int j = 0; j < lp.getVarNb(); j++) {
                sum += lp.getMatrix().get(j).get(i) * coord.get(j);
            }
            if (lp.getConstSigns().get(i).equals("<=")) {
                // Si la valeur dépasse la contrainte ou que la var d'écart est négative
                if (sum > lp.getRhs()[i]) {
                    return false;
                }
            } else if (lp.getConstSigns().get(i).equals("==")) {
                if (sum != lp.getRhs()[i]) {
                    return false;
                }
            } else { // sign is ">="
                // Si la valeur dépasse la contrainte ou que la var d'écart est positive
                if (sum < lp.getRhs()[i]) {
                    return false;
                }
            }
            i++;
        }
        return true;
    }


    @Override
    public String toString() {
        return inVarIndices.toString();
    }


    /**
     * Ajoute dans une liste les indices des variables qui ne sont pas en base et la renvoie.
     * @return une liste contenant les indices des variables hors base
     */
    private ArrayList<Integer> getInitOutVarIndices() {
        ArrayList<Integer> outVariables = new ArrayList<>(lp.getVarNb() + lp.getSlackVarNb());
        for (int i = 0; i < lp.getVarNb() + lp.getSlackVarNb(); i++) {
            outVariables.add(i);
        }
        outVariables.removeAll(inVarIndices);
        return outVariables;
    }


    /**
     * Renvoie true si la base est invalide.
     * @return true si la base est invalide (ses coordonnés sont null), false sinon
     */
    private boolean isInvalid() {
        return coord.get(0) == null;
    }


    /**
     * Calcul les coordonnés de la base en résolvant le système formé par les contraintes.
     */
    private ArrayList<Double> computeCoord() {
        ArrayList<Double> coord = new ArrayList<>();
        for (int i = 0; i < lp.getVarNb() + lp.getSlackVarNb(); i++) {
            coord.add(0.);
        }

        double [][] systemMatrix = new double[inVarIndices.size()][inVarIndices.size()];
        // On rempli notre matrice représentant le système
        for (int i = 0; i < inVarIndices.size(); i++) {
            for (int j = 0; j < inVarIndices.size(); j++) {
                systemMatrix[i][j] = lp.getMatrix().get(inVarIndices.get(j)).get(i);
            }
        }
        RealMatrix coefficients =
                new Array2DRowRealMatrix(systemMatrix, false);
        DecompositionSolver solver = new LUDecompositionImpl(coefficients).getSolver();
        RealVector constants = new ArrayRealVector(lp.getRhs(), false);
        try {
            RealVector solution = solver.solve(constants);
            double [] sol = solution.toArray();
            // On set à 0 la valeur des coords correspondantes aux var hors base
            for (Integer i : outVarIndices) {
                coord.set(i, 0.);
            }
            // On set la valeur des coords correspondantes aux var en base avec la valeur obtenue en résolvant le système
            for (int i = 0; i < inVarIndices.size(); i++) {
                Integer indiceToSet = inVarIndices.get(i);
                // On est obligé de faire +0. sinon on a des 0.0 et des -0.0
                coord.set(indiceToSet, sol[i] + 0.);
            }
        } catch (SingularMatrixException e) {
            // Si le système n'est pas résolvable la base est invalide donc on met ses coords à null
            for (int i = 0; i < coord.size(); i++) {
                coord.set(i, null);
            }
        }
        return coord;
    }


    /* ------------------ Getters et Setters ------------------ */


    public ArrayList<Integer> getInVarIndices() {
        return inVarIndices;
    }

    public ArrayList<Integer> getOutVarIndices() {
        return outVarIndices;
    }

    public ArrayList<Double> getCoord() {
        return coord;
    }
}
