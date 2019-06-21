package main;

import Model.LinearProgram;
import Model.ModelFileEvaluator;
import Utils.Util;
import ilog.concert.IloLPMatrix;

import java.util.ArrayList;
import java.util.Collections;

public class testOneModFile {
    public static void main(String[] args) {
        String pathToSolution = "/home/secardia/Documents/Projet/Private_Ressources/Dairy_Products/dairy.mod";
        String pathToModfile = "/home/secardia/Documents/Projet/Private_Ressources/Dairy_Products/Student_files/27042/2016-09-28-09-45-13/dairy.mod";

        // On récupère la matrix du modèle du .mod (corrigé) et on génère les tests
        IloLPMatrix iloLPMatrix = Util.getIloLPMatrixFromModFile(pathToSolution);
        LinearProgram lp = new LinearProgram(iloLPMatrix);
        lp.enumerateAllExtremePoints();
        System.out.println("nbEP : " + lp.getExtremePoints().size());
        lp.generateTests();
        System.out.println("nbTests : " + lp.getTests().size());


        ModelFileEvaluator testor = new ModelFileEvaluator(lp);
        // On teste le fichier
        ArrayList<Boolean> testResult = testor.getFileEvaluation(pathToModfile);
        int nbError = Collections.frequency(testResult, false);
        System.out.println("nbError : " + nbError);

        // On affiche les erreurs probables
        ArrayList<Integer> wrongVars = testor.getCorrectionHelp(testResult);
        // On analyse
        for (Integer varIndice : wrongVars) {
            // Si c'est une variable "normale" c'est que la contrainte >= 0 n'est pas respectée
            if (varIndice < lp.getVarNb()) {
                System.out.println("var"+varIndice+" n'a pas de contrainte >= 0");
            } else {
                // Si c'est une variable d'écart on cherche la contrainte à laquelle elle est associée
                // On récupère les coefs de la variable
                ArrayList<Double> coefs = lp.getMatrix().get(varIndice);
                int indexOfConst = Math.max(coefs.indexOf(1.), coefs.indexOf(-1.));
                System.out.println("La contrainte "+lp.getConstNames().get(indexOfConst)+" est probablement fausse");
            }
        }
    }



}