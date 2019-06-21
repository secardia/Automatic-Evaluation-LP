package main;

import Model.LinearProgram;
import Model.ModelFileEvaluator;
import Utils.Util;
import ilog.concert.IloLPMatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

import static Utils.Util.getFilesToTest;

public class testStudentsFiles {

    /*
    Hypothèses :
        Variables >= 0; (sinon pour <= 0 : [-oo ; 0] ==> +1 coef à mettre pour tendre vers 0 / dur de repérer avec Cplex)
        Toutes les variables sont dans la fonction objectif (sinon il faudrait savoir quelles var sont dans la fct)
        Fonction obectif de la forme : c[1]*x + c[2]*y + c[3]*z (car Cplex fait du prétraitement = pas possible d'avoir la forme de la fct)
        Contraintes de la forme : +/- ax +/- by <=/==/>= 5000 (pas de "double contrainte": 100 <= ax + by <= 8000)
     */

    public static void main(String[] args) {
        int error = 0;
        int nbHelp = 0;

        String pathToDir = "src/main/resources/German_Wines";
        String pathToStudentsDirs = pathToDir+ File.separator+"Student_files";

        ArrayList<ArrayList<File>> filesToTest = getFilesToTest(pathToStudentsDirs);

        File dir = new File(pathToDir);
        // On récupère les .mod du dossier pathToDir
        String [] solutions = dir.list((dir1, name) -> name.endsWith(".mod"));
        // Normalement il n'y a qu'un .mod (le corrigé)
        assert (solutions != null && solutions.length > 0);
        // Si c'est le cas on le récupère
        String pathToSolution = pathToDir+File.separator+solutions[0];

        // On récupère la matrix du modèle du .mod (corrigé) et on génère le programme linéaire
        IloLPMatrix iloLPMatrix = Util.getIloLPMatrixFromModFile(pathToSolution);
        LinearProgram lp = new LinearProgram(iloLPMatrix);
        // On énumère les points extrêmes
        lp.enumerateAllExtremePoints();
        // On génère les tests
        lp.generateTests();

        ModelFileEvaluator evaluator = new ModelFileEvaluator(lp);

        for (ArrayList<File> fileToTest : filesToTest) {
            File modFile = fileToTest.get(0);
            File gradeFile = fileToTest.get(1);
            double caseineGrade;
            // On récupère la note de Caseine
            try {
                FileReader gradeFileReader = new FileReader(gradeFile);
                BufferedReader bf = new BufferedReader(gradeFileReader);
                String line = bf.readLine();
                caseineGrade = Double.valueOf(line);
            } catch (Exception e) {
                throw new RuntimeException("Error while trying to read the caseine grade.", e);
            }

            // On teste le fichier
            ArrayList<Boolean> eval = evaluator.getFileEvaluation(modFile.getPath());
            ArrayList<Integer> helps = evaluator.getCorrectionHelp(eval);
            // On compte le nombre de succès (pour calculer la note)
            int nbSuccess = Collections.frequency(eval, true);
            double newGrade = ((double) nbSuccess/eval.size())*100;

            boolean cond1 = (caseineGrade == 0 || caseineGrade == 100 || newGrade == 0 || newGrade == 100);
            if (cond1 && caseineGrade != newGrade) {
                System.out.println("-----------------------------");
                System.out.println("----- Test de : "+modFile.getPath());
                System.out.println("Caseine grade : "+caseineGrade);
                System.out.println("New grade :     "+newGrade);
                System.out.println(eval);
                error++;
            }

            if (helps.size() > 0) {
                System.out.println("-----------------------------");
                System.out.println("----- Test de : "+modFile.getPath());
                System.out.println("Caseine grade : "+caseineGrade);
                System.out.println("New grade :     "+newGrade);
                // On analyse
                for (Integer varIndice : helps) {
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
                nbHelp++;
            }
        }

        System.out.println();
        System.out.println("Nombre de tests générés : "+lp.getTests().size());
        System.out.println("Nombre de fichiers analysés : "+filesToTest.size());
        System.out.println("Nombre de d'erreurs de notation : "+error);
        System.out.println("Nombre de proposition de correction : "+nbHelp);

    }

}
