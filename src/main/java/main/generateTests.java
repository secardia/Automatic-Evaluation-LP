package main;

import LinearProgramExamples.CuncumbersAndOnions;
import Model.LinearProgram;
import Utils.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class generateTests {

    public static void main(String[] args) {
        LinearProgram lp = new CuncumbersAndOnions().lp;
        lp.enumerateAllExtremePoints();
        System.out.println("nbEP : " + lp.getExtremePoints().size());
        lp.generateTests();
        System.out.println("nbTests : " + lp.getTests().size());

        Util.printExtremesPointsAndBases(lp.getExtremePoints());

        // On regarde si les tests sont valides en testant le modèle lui-même
        if (lp.validTests()) {
            // On affiche le résultat
            System.out.println("Les tests sont validés.");
            for (int i = 0; i < lp.getExtremePoints().size(); i++) {
                System.out.println("-----------------");
                System.out.println("Test du point : "+lp.getExtremePoints().get(i).getCoord());
                System.out.println("Avec les coefs : "+lp.getTests().get(i).getCoefs());
                System.out.println("Valeur attendue : "+lp.getTests().get(i).getExpectedVal()+"\n");
            }
            try {
                String fileName = "_tests.cases";
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                for (int i = 0; i < lp.getExtremePoints().size(); i++) {
                    writer.write("Test du point : "+lp.getExtremePoints().get(i).getCoord().toString()+"\n");
                    writer.write("Avec les coefs : "+lp.getTests().get(i).getCoefs().toString()+"\n");
                    writer.write("Valeur attendue : "+lp.getTests().get(i).getExpectedVal()+"\n\n");
                }
                writer.close();
                System.out.println("Les tests ont été écrit dans le fichier \""+fileName+"\"");

            } catch (IOException e) {
                throw new RuntimeException("Error while writing tests to the file.");
            }
        }






    }
}
