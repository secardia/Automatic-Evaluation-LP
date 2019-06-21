package Model;

import Utils.Util;
import ilog.concert.*;

import java.util.ArrayList;
import java.util.HashMap;

public class LinearProgram {
    // Nombre de contraintes
    private Integer constNb;
    // Nombre de variables
    private Integer varNb;
    // Nombre de variables d'écart
    private Integer slackVarNb;

    // Contient les contraintes sous forme de matrice
    private ArrayList<ArrayList<Double>> matrix;
    // Contient les signes des contraintes ("<=", "==", ou ">="), sert au check des contraintes + coef des vars d'écart
    private ArrayList<String> constSigns;
    // Contient le second membre
    private double [] rhs;

    // Contient les points extrêmes de programme linéaire
    private ArrayList<ExtremePoint> extremePoints;
    // Contient les tests à faire pour vérifier si un programme linéaire est égale à celui là
    private ArrayList<Test> tests;

    // Dans le cas où on charge de programme linéaire avec un fichier.lp, contient le nom des contraintes
    // pour permettre l'analyse de l'aide à la correction
    private ArrayList<String> constNames;


    /* ------------------ Constructeurs ------------------ */


    /**
     * Construit un programme linéaire à partir des coefficients des contraintes, de leurs signes et du
     * second membre, ces trois éléments étant spécifiés.
     * @param matrix coefficients des contraintes sous forme matricielle
     * @param constSigns signes des contraintes
     * @param rhs second membre
     */
    public LinearProgram(ArrayList<ArrayList<Double>> matrix, ArrayList<String> constSigns, double [] rhs) {
        this.matrix = matrix;
        this.constSigns = constSigns;
        this.rhs = rhs;
        this.constNames = new ArrayList<>();

        try {
            // On vérifie que le format est correcte
            checkFormat();
        } catch (Exception e) {
            throw new RuntimeException("Error during initialisation of linear program.", e);
        }
        this.varNb = matrix.size();
        this.constNb = constSigns.size();
        // Ajoute à la matrice les variables d'écarts nécessaire
        this.addSlackVariables();
        this.extremePoints = new ArrayList<>();
        this.tests = new ArrayList<>();
    }


    /**
     * Construit un programme linéaire à partir de la matrice spécifiée (elle contient les coefficients des
     * contraintes, leurs signes et le second membre).
     * @param iloLPMatrix matrice contenant les coefficients des contraintes, leurs signes et le second membre
     */
    public LinearProgram(IloLPMatrix iloLPMatrix) {
        try {
            IloRange[] constraintes = iloLPMatrix.getRanges();

            this.matrix = new ArrayList<>();
            this.constSigns = new ArrayList<>(constraintes.length);
            this.rhs = new double[constraintes.length];
            this.constNames = new ArrayList<>();

            HashMap<String, ArrayList<Double>> varCoefs = new HashMap<>();
            int constNb = 0;
            for (IloRange constr : constraintes) {
                constNames.add(constr.getName());
                Double lb = constr.getLB();
                Double ub = constr.getUB();
                // On rempli constSigns et RHS
                if (lb.equals(ub)) {
                    constSigns.add("==");
                    rhs[constNb] = lb;
                } else if (lb.equals(Double.NEGATIVE_INFINITY)) {
                    constSigns.add("<=");
                    rhs[constNb] = ub;
                } else if (ub.equals(Double.POSITIVE_INFINITY)) {
                    constSigns.add(">=");
                    rhs[constNb] = lb;
                }
                // On rempli matrix
                if (constr.getExpr() instanceof IloLinearNumExpr) {
                    IloLinearNumExpr parseExpr = (IloLinearNumExpr) constr.getExpr();
                    IloLinearNumExprIterator itexpr = parseExpr.linearIterator();
                    while (itexpr.hasNext()) {
                        IloNumVar iloVar = itexpr.nextNumVar();
                        String name = iloVar.getName();
                        Double coef = itexpr.getValue();
                        // Si la Map ne contient pas cette entrée (= la variable est inconnu)
                        if (!varCoefs.containsKey(name)) {
                            // On créé une nouvelle liste
                            ArrayList<Double> list = new ArrayList<>();
                            // On l'initialise avec des 0
                            for (int i = 0; i < constraintes.length; i++) {
                                list.add(0.);
                            }
                            // On set le coef
                            list.set(constNb, coef);
                            // On l'ajoute dans la Map
                            varCoefs.put(name, list);
                        } else {
                            // Si la var est connu, on récupère la liste et set le coef
                            varCoefs.get(name).set(constNb, coef);
                        }
                    }
                }
                constNb++;
            }

            // On ajoute les valeurs de la Map à la matrix
            matrix.addAll(varCoefs.values());
            // On vérifie que le format est correcte
            checkFormat();
        } catch (Exception e) {
            throw new RuntimeException("Error during initialisation of linear program.", e);
        }

        this.varNb = matrix.size();
        this.constNb = constSigns.size();
        // Ajoute à la matrice les variables d'écarts nécessaire
        this.addSlackVariables();
        this.extremePoints = new ArrayList<>();
        this.tests = new ArrayList<>();
    }


    /* ------------------ Fonctions ------------------ */


    /**
     * Liste tous les points extrêmes en faisant une énumération complète des bases. Ne renvoie pas
     * la liste des points extrême mais la stock en attribut de la classe.
     */
    public void enumerateAllExtremePoints() {
        ExtremePoint pe;
        int indexOfPe;
        ArrayList<ExtremePoint> extremesPoints = new ArrayList<>();

        // Initialise inVarIndices à [0, 1, 2, ...]
        ArrayList<Integer> inVarIndices = new ArrayList<>(constNb);
        for (int i = 0; i < constNb; i++) {
            inVarIndices.add(i);
        }
        // On créer la base correspondante
        Basis b = new Basis(inVarIndices, this);
        while (b != null) {
            // Si la solution de base est réalisable
            if (b.isAFeasibleSolution()) {
                // C'est qu'elle correspond à un ExtremePoint
                pe = new ExtremePoint(b, this);
                // On regarde si cet ExtremePoint est déjà dans la liste
                indexOfPe = extremesPoints.indexOf(pe);
                // S'il n'est pas dans la liste
                if (indexOfPe == -1) {
                    // On ajoute le pe
                    extremesPoints.add(pe);
                } else {
                    // On ajoute la base au pe
                    extremesPoints.get(indexOfPe).addBasis(b);
                }
            }
            b = getNextBasis(b.getInVarIndices());
        }
        this.extremePoints = extremesPoints;
    }


    /**
     * Génère les tests à faire pour comparer un programme linéaire à celui là. Ne renvoie pas
     * la liste des tests mais la stock en attribut de la classe.
     */
    public void generateTests() {
        if (extremePoints.isEmpty()) {
            throw new RuntimeException("You try to generate tests while there is no extreme points to test in the " +
                    "linear program.");
        } else {
            ArrayList<Test> tests = new ArrayList<>();

            for (ExtremePoint ep : extremePoints) {
                ArrayList<Double> coefs = new ArrayList<>(varNb);
                double expectedVal = 0;
                for (int i = 0; i < varNb; i++) {
                    coefs.add(0.);
                }
                for (Integer outVarIndice : ep.getBases().get(0).getOutVarIndices()) {
                    // Si la var n'est pas une variable d'écart
                    if (outVarIndice < varNb) {
                        // on ajoute juste un coef de -1
                        coefs.set(outVarIndice, coefs.get(outVarIndice) - 1);
                    } else {
                        // Sinon on parcourt les var de la contrainte correspondant à la var d'écart
                        if (constSigns.get(outVarIndice - varNb).equals("<=")) {
                            for (int i = 0; i < varNb; i++) {
                                coefs.set(i, coefs.get(i) + matrix.get(i).get(outVarIndice - varNb));
                            }
                            expectedVal += rhs[outVarIndice - varNb];
                        } else if (constSigns.get(outVarIndice - varNb).equals(">=")) {
                            for (int i = 0; i < varNb; i++) {
                                coefs.set(i, coefs.get(i) - matrix.get(i).get(outVarIndice - varNb));
                            }
                            expectedVal -= rhs[outVarIndice - varNb];
                        }
                    }
                }
                tests.add(new Test(coefs, expectedVal));
            }
            this.tests = tests;
        }
    }


    /**
     * Evalue ce programme linéaire avec les tests calculés permettant de le tester. Permet de vérifier que les
     * tests générés permettent bien d'évaluer correctement ce programme linéaire.
     * @return true si les tests passés en paramètre sont valides, false sinon
     */
    public boolean validTests() {
        if (tests.isEmpty()) {
            throw new RuntimeException("You try to validate tests but there is no tests to validate.");
        } else {
            for (Test test : tests) {
                double expectedVal = test.getExpectedVal();
                double foundedVal = Util.solveWithCplex(this, test.getCoefs()).get(0);
                if (expectedVal != foundedVal) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Compare l'objet spécifié avec ce programme linéaire pour vérifier s'ils sont égaux. Deux programme linéaires sont
     * égaux si la matrice, le signe des contraintes et le second membre sont identiques.
     * @param o le programme linéaire à comparer
     * @return true si l'objet spécifié est un programme linéaire et si la matrice, le signe des contraintes et
     * le second membre sont identiques, false sinon
     */
    @Override
    public boolean equals(Object o) {
        boolean eq = true;
        if (o instanceof LinearProgram) {
            LinearProgram lp = (LinearProgram) o;

            // Test l'égalité de matrix
            if (this.matrix.size() == lp.matrix.size()) {
                for (int i = 0; i < this.matrix.size(); i++) {
                    if (!this.matrix.get(i).equals(lp.matrix.get(i))) {
                        eq = false;
                    }
                }
            } else {
                eq = false;
            }
            // Test l'égalité de constSigns
            if (!this.constSigns.equals(lp.constSigns)) {
                eq = false;
            }
            // Test l'égalité de rhs
            if (this.rhs.length == lp.rhs.length) {
                for (int i = 0; i < this.rhs.length; i++) {
                    if (this.rhs[i] != lp.rhs[i]) {
                        eq = false;
                    }
                }
            } else {
                eq = false;
            }
        } else {
            eq = false;
        }
        return eq;
    }


    /**
     * Vérifie le format de la matrice de coefficients, de la liste des signes ainsi que du second membre.
     * @throws Exception : Throw une exception en cas de mauvais format d'un élément
     */
    private void checkFormat() throws Exception {
        // Toutes les contraintes doivent avoir le même nombre de coefficients
        int constSize = matrix.get(0).size();
        for (int i = 1; i < matrix.size(); i++) {
            if (matrix.get(i).size() != constSize) {
                throw new Exception("All constraints should have the same number of coefficients but the " +
                        "constraint 0 have "+constSize+" coefficients and the "+i+" have "+matrix.get(i).size()+".");
            }
        }
        // Il doit y avoir 1 signe par contrainte, pas plus, pas moins
        if (constSize != constSigns.size()) {
            throw new Exception("The number of constraints must be the same as the number of signs but there " +
                    "is "+constSize+" constraints and "+constSigns.size()+" signs.");
        }
        // Les signes de contraintes sont soit "<=", soit "==", soit ">="
        for (int i = 0; i < constSigns.size(); i++) {
            String sign = constSigns.get(i);
            if (!sign.equals("<=") && !sign.equals("==") && !sign.equals(">=")) {
                throw new Exception("The sign of the constraintes must be \"<=\", \"==\" or \">=\" but the sign of " +
                        "the constraint "+i+" is \""+sign+"\".");
            }
        }
        // Il doit y avoir autant de valeur dans le second membre que de contraintes
        if (constSigns.size() != rhs.length) {
            throw new Exception("The number of constraints must be the same as the number of values in the rhs " +
                    "but there is "+constSize+" constraints and "+constSigns.size()+" signs.");
        }
    }


    /**
     * A partir des variables en base spécifiées, calcul quelle est la prochaine base à analyser.
     * @param oldInVarIndices variables en base de la dernière base analysée
     * @return la prochaine base à analyser
     */
    private Basis getNextBasis(ArrayList<Integer> oldInVarIndices) {
        ArrayList<Integer> nextInVarIndicies = new ArrayList<>(oldInVarIndices);
        int lastIndice = nextInVarIndicies.size()-1;
        int shift = 0;
        int indice;
        boolean incremented = false;

        do {
            indice = lastIndice-shift;
            Integer val = nextInVarIndicies.get(indice);
            if (val < this.varNb+this.slackVarNb-shift-1) {
                nextInVarIndicies.set(indice, val+1);
                incremented = true;
            } else {
                shift++;
            }
        } while (shift <= lastIndice && !incremented);

        indice++;
        if (incremented) {
            while (indice < nextInVarIndicies.size()) {
                nextInVarIndicies.set(indice, nextInVarIndicies.get(indice-1)+1);
                indice++;
            }
            return new Basis(nextInVarIndicies, this);
        } else {
            return null;
        }
    }


    /**
     * Ajoute au programme linéaire les variables d'écart nécessaires pour qu'il soit ensuite possible de calculer
     * les solutions de base.
     */
    private void addSlackVariables() {
        for (int i = 0; i < constNb; i++) {
            if (constSigns.get(i).equals("<=")) {
                ArrayList<Double> slackVar = new ArrayList<>(constNb);
                for (int j = 0; j < constNb; j++) {
                    if (i == j) {
                        slackVar.add(1.);
                    } else {
                        slackVar.add(0.);
                    }
                }
                matrix.add(slackVar);
            } else if (constSigns.get(i).equals(">=")) {
                ArrayList<Double> slackVar = new ArrayList<>(constNb);
                for (int j = 0; j < constNb; j++) {
                    if (i == j) {
                        slackVar.add(-1.);
                    } else {
                        slackVar.add(0.);
                    }
                }
                matrix.add(slackVar);
            }
        }
        slackVarNb = matrix.size() - varNb;
    }


    /* ------------------ Getters et Setters ------------------ */


    public Integer getConstNb() {
        return constNb;
    }

    public Integer getVarNb() {
        return varNb;
    }

    public Integer getSlackVarNb() {
        return slackVarNb;
    }

    public ArrayList<ArrayList<Double>> getMatrix() {
        return matrix;
    }

    public ArrayList<String> getConstSigns() {
        return constSigns;
    }

    public double[] getRhs() {
        return rhs;
    }

    public ArrayList<ExtremePoint> getExtremePoints() {
        return extremePoints;
    }

    public ArrayList<Test> getTests() {
        return tests;
    }

    public ArrayList<String> getConstNames() {
        return constNames;
    }
}
