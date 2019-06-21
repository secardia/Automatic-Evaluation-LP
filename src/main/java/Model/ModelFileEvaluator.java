package Model;

import ilog.concert.IloException;
import ilog.concert.IloNumMap;
import ilog.opl.*;

import java.util.ArrayList;

public class ModelFileEvaluator {

    private LinearProgram lp;

    /* ------------------ Constructeurs ------------------ */


    /**
     * Construit un evaluateur de fichier.mod contenant un modèle de programme linéaire.
     * @param lp le programme linéaire du corrigé
     */
    public ModelFileEvaluator(LinearProgram lp) {
        this.lp = lp;
    }


    /* ------------------ Fonctions ------------------ */


    /**
     * Permet d'évaluer, avec les tests du programme linéaire spécfié à la construction, toute la liste de
     * fichiers spécifiée.
     * @param filesNamesToEval liste des noms des fichiers.mod à évaluer
     * @return une liste contenant pour chaque fichier une liste, qui contient pour chaque test,
     * true si le test est passé, false sinon
     */
    public ArrayList<ArrayList<Boolean>> getFilesEvaluations(ArrayList<String> filesNamesToEval) {
        ArrayList<ArrayList<Boolean>> evals = new ArrayList<>();
        for (String file : filesNamesToEval) {
            evals.add(getFileEvaluation(file));
        }
        return evals;
    }


    /**
     * Evalue, avec les tests du programme linéaire spécfié à la construiction, le fichier spécifié.
     * @param fileNameToEval le nom du fichier .mod à évaluer
     * @return une liste qui contient pour chaque test, true si le test est passé, false sinon
     */
    public ArrayList<Boolean> getFileEvaluation(String fileNameToEval) {
        ArrayList<Test> tests = lp.getTests();
        if (tests.isEmpty()) {
            throw new RuntimeException("You try to evaluate a file before generate the tests in the linear program.");
        } else {
            ArrayList<Boolean> eval = new ArrayList<>();
            try {
                IloOplFactory.setDebugMode(false);
                IloOplFactory oplF = new IloOplFactory();
                IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
                IloOplModelSource modelSource = oplF.createOplModelSource(fileNameToEval);
                IloOplSettings settings = oplF.createOplSettings(errHandler);
                settings.setWithWarnings(false);
                IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource,settings);
                IloCplex cplex = oplF.createCplex();
                cplex.setOut(null);


                IloOplModel opl = oplF.createOplModel(def, cplex);
                IloOplElement elem = opl.getElement("c");
                IloNumMap c = elem.asNumMap();

                // Si c[...] est plus petit que le nombre de coef, on met directement tous les tests à false
                if (c.getSize() < tests.get(0).getCoefs().size()) {
                    for (int testNb = 0; testNb < tests.size(); testNb++) {
                        eval.add(false);
                    }
                } else {
                    for (Test test : tests) {
                        opl = oplF.createOplModel(def, cplex);
                        elem = opl.getElement("c");
                        c = elem.asNumMap();

                        // On set les valeurs c c[...]
                        for (int i = 0; i < test.getCoefs().size(); i++) {
                            c.set(i + 1, test.getCoefs().get(i));
                        }

                        // display option
                        cplex.setParam(IloCplex.Param.Simplex.Display, 0);
                        // On régénère l'opl avec le nouveau c
                        opl.generate();

                        if (cplex.solve()) {
                            // Si la valeur trouvé est bien la valeur attendue
                            double e = Math.abs(test.getExpectedVal() - cplex.getObjValue());
                            if (e < 0.000001) {
                                // On indique que le test est passé
                                eval.add(true);
                            } else {
                                // Sinon on indique que le test est raté
                                eval.add(false);
                            }
                        } else {
                            eval.add(false);
                        }
                    }
                }
            } catch (IloException exc) {
                throw new RuntimeException("Error during the evaluation of "+fileNameToEval+".", exc);
            }

            // On retourne la liste de Boolean
            return eval;
        }
    }


    /**
     * Essaye de deviner quelles sont les erreurs probables pour chacune des évaluations de la liste spécifiée.
     * @param evaluations une liste contenant la liste de boolean indiquant quels tests sont passés et lesquels ne sont pas passés
     * @return une liste contenant la liste des variables qui sont probablement fausses pour chacune des évaluation.
     * Si c'est une variable cela veut dire que la variable n'est probablement pas >= 0. Si c'est une variable d'écart
     * cela veut dire que la contrainte associée à cette variable d'écart est probablement fausse
     */
    public ArrayList<ArrayList<Integer>> getCorrectionHelps(ArrayList<ArrayList<Boolean>> evaluations) {
        ArrayList<ArrayList<Integer>> helps = new ArrayList<>();
        for (ArrayList<Boolean> eval : evaluations) {
            helps.add(getCorrectionHelp(eval));
        }
        return helps;
    }


    /**
     * Essaye de deviner quelles sont les erreurs probables en fonction de l'évaluation spécifiée.
     * @param evaluation la liste de boolean indiquant quels tests sont passés et lesquels ne sont pas passés
     * @return la liste des variables qui sont probablement fausses. Si c'est une variable cela veut dire que
     * la variable n'est probablement pas >= 0. Si c'est une variable d'écart cela veut dire
     * que la contrainte associée à cette variable d'écart est probablement fausse
     */
    public ArrayList<Integer> getCorrectionHelp(ArrayList<Boolean> evaluation) {
        ArrayList<ExtremePoint> extremePoints = lp.getExtremePoints();
        if (extremePoints.size() != evaluation.size()) {
            throw new RuntimeException("The number of extreme points and evaluations should be the same but there " +
                    "is "+extremePoints.size()+" extreme points and "+evaluation.size()+" evaluation.");
        } else {
            ArrayList<Integer> wrongVars = new ArrayList<>();
            // On récupère les ExtremePoint évalués à faux
            ArrayList<ExtremePoint> wrongEPs = new ArrayList<>();
            for (int i = 0; i < evaluation.size(); i++) {
                if (!evaluation.get(i)) {
                    wrongEPs.add(extremePoints.get(i));
                }
            }
            // On cherche varNb points qui ont au moins 1 variable hors base en commun (il faut donc au moins varNb erreurs)
            if (lp.getVarNb() <= wrongEPs.size()) {
                // On liste toutes les combinaison de varNb points parmis ceux évalués à faux
                ArrayList<ArrayList<Integer>> combinationToCheck = enumAllKChoseN(lp.getVarNb(), wrongEPs.size());
                for (ArrayList<Integer> combination : combinationToCheck) {
                    // On récupère les bases
                    ArrayList<Basis> bases = new ArrayList<>();
                    for (Integer indice : combination) {
                        bases.add(wrongEPs.get(indice).getBases().get(0));
                    }
                    // On récupère toutes les variables en bases (sans doublons)
                    ArrayList<Integer> allInVarIndicies = new ArrayList<>();
                    for (Basis b : bases) {
                        for (Integer indice : b.getInVarIndices()) {
                            if (!allInVarIndicies.contains(indice)) {
                                allInVarIndicies.add(indice);
                            }
                        }
                    }
                    // Si la liste ne contient pas toutes les variables
                    if (allInVarIndicies.size() < lp.getVarNb() + lp.getSlackVarNb()) {
                        // C'est que les varNb ont au moins une variable hors base en commun, on trouve lesquelles
                        ArrayList<Integer> commonIndicies = new ArrayList<>();
                        // On parcourt toutes les valeurs possible
                        for (int i = 0; i < lp.getVarNb() + lp.getSlackVarNb(); i++) {
                            // Si une n'est pas dans la liste
                            if (!allInVarIndicies.contains(i)) {
                                // On l'ajoute dans la liste des indices en commun
                                commonIndicies.add(i);
                            }
                        }
                        // On supprime ceux qu'on avait déjà trouvé
                        commonIndicies.removeAll(wrongVars);
                        // On ajoute les nouveaux
                        wrongVars.addAll(commonIndicies);
                    }
                }
            }
            return wrongVars;
        }
    }


    /**
     * Liste toutes les possibilités en prenant k variables différentes parmis n (pour k=2, n=3 : ((0,1), (0,2), (1,2)).
     * @param k nombre d'éléments à prendre
     * @param n nombre total d'éléments
     * @return la liste de toutes les possibilités
     */
    private ArrayList<ArrayList<Integer>> enumAllKChoseN(int k, int n) {
        if (k > n) {
            throw new RuntimeException("You try to enumerate all "+k+" chose "+n+" possibilities but you can't pick " +
                    k+" differents numbers in a set of "+n+" numbers.");
        } else {
            ArrayList<ArrayList<Integer>> kChoseN = new ArrayList<>();
            ArrayList<Integer> sol = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                sol.add(i);
            }
            kChoseN.add(sol);

            boolean incremented = true;
            while (incremented) {
                incremented = false;
                sol = new ArrayList<>(sol);
                int lastIndice = sol.size()-1;
                int shift = 0;
                int indice;
                do {
                    indice = lastIndice-shift;
                    Integer val = sol.get(indice);
                    if (val < n-shift-1) {
                        sol.set(indice, val+1);
                        incremented = true;
                    } else {
                        shift++;
                    }
                } while (shift <= lastIndice && !incremented);

                indice++;
                if (incremented) {
                    while (indice < sol.size()) {
                        sol.set(indice, sol.get(indice-1)+1);
                        indice++;
                    }
                    kChoseN.add(sol);
                }
            }
            return kChoseN;
        }
    }
}
