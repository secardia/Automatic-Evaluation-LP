package Utils;

import Model.Basis;
import Model.ExtremePoint;
import Model.LinearProgram;
import ilog.concert.*;
import ilog.opl.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class Util {


    /**
     * Affiche les points extrême spécifiés
     * @param extremesPoints : Liste des points extrêmes à afficher
     */
    static public void printExtremesPoints(ArrayList<ExtremePoint> extremesPoints) {
        for (ExtremePoint ep : extremesPoints) {
            System.out.println("ep : " + ep.getCoord());
        }
    }


    /**
     * Affiche les points extrême spécifiés ainsi que leurs bases
     * @param extremesPoints : Liste des points extrêmes à afficher
     */
    static public void printExtremesPointsAndBases(ArrayList<ExtremePoint> extremesPoints) {
        for (ExtremePoint ep : extremesPoints) {
            System.out.println("\nep : " + ep.getCoord());
            for (Basis basis : ep.getBases()) {
                System.out.println("\tbasis : " + basis.toString());
            }
        }
    }


    /**
     * Génère l'objet Cplex grâce au programme linéaire et aux coefficients de la fonction objectif spécifiés et
     * lance le solveur. Il renvoie les coordonnés du point où se trouve la fonction optimale
     * @param lp : Programme linéaire à résoudre
     * @param coefs : Coefficients de c[...] à remplacer dans le programme linéaire
     * @return : Liste dont la première valeur est la valeur de la solution optimale et toutes les autres sont
     * les coordonnés du point où se trouve la solution optimale
     */
    static public ArrayList<Double> solveWithCplex(LinearProgram lp, ArrayList<Double> coefs) {
        ArrayList<Double> ret = new ArrayList<>();
        try {
            IloCplex cplex = new IloCplex();
            cplex.setOut(null);
            // Variables
            ArrayList<IloNumVar> variables = new ArrayList<>();
            for (int i = 0; i < lp.getVarNb(); i++) {
                variables.add(cplex.numVar(0, Double.MAX_VALUE));
            }
            // Coef de la fonction
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for (int i = 0; i < lp.getVarNb(); i++) {
                objective.addTerm(coefs.get(i), variables.get(i));
            }
            // Objectif de la fonction
            cplex.addMaximize(objective);
            // Contraintes
            for (int i = 0; i < lp.getConstNb(); i++) {
                // On construit l'expression de la contrainte
                IloNumExpr[] constExpr = new IloNumExpr[lp.getVarNb()];
                for (int j = 0; j < lp.getVarNb(); j++) {
                    constExpr[j] = cplex.prod(lp.getMatrix().get(j).get(i), variables.get(j));
                }
                // On l'ajoute suivant le signe
                switch (lp.getConstSigns().get(i)) {
                    case "==":
                        cplex.addEq(cplex.sum(constExpr), lp.getRhs()[i]);
                        break;
                    case ">=":
                        cplex.addGe(cplex.sum(constExpr), lp.getRhs()[i]);
                        break;
                    case "<=":
                        cplex.addLe(cplex.sum(constExpr), lp.getRhs()[i]);
                        break;
                }
            }
            // display option
            cplex.setParam(IloCplex.Param.Simplex.Display, 0);
            // solve
            if (cplex.solve()) {
                ret.add(cplex.getObjValue());
                for (int i = 0; i < lp.getVarNb(); i++) {
                    ret.add(cplex.getValue(variables.get(i)));
                }
            } else {
                for (int i = 0; i < lp.getVarNb(); i++) {
                    ret.add(null);
                }
            }
            cplex.end();

        } catch (IloException exc) {
            throw new RuntimeException("Error during the resolution of the lp with coefficients : "+coefs+".", exc);
        }
        return ret;
    }


    /**
     * Extraie la matrix du fichier.mod spécifié
     * @param pathToModFile : Chemin vers le fichier.mod dont on veut extraire la matrix
     * @return : La matrix extraite du fichier.mod
     */
    static public IloLPMatrix getIloLPMatrixFromModFile(String pathToModFile) {
        try {
            // On ouvre le fichier .mod
            IloOplFactory.setDebugMode(false);
            IloOplFactory oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
            IloOplModelSource modelSource = oplF.createOplModelSource(pathToModFile);
            IloOplSettings settings = oplF.createOplSettings(errHandler);
            settings.setWithWarnings(false);
            IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource,settings);
            ilog.opl.IloCplex cplex = oplF.createCplex();
            cplex.setOut(null);
            // On génère l'IloOplModel
            IloOplModel opl = oplF.createOplModel(def, cplex);
            opl.generate();
            // On export l'IloCplex en .lp
            cplex.exportModel("_tmp.lp");
            IloCplex newCplex = new IloCplex();
            // On import le .lp (Cplex génère automatiquement la IloLPMatrix)
            newCplex.importModel("_tmp.lp");
            // On supprime le fichier temporaire
            //new File("_tmp.lp").delete();
            return (IloLPMatrix) newCplex.LPMatrixIterator().next();
        } catch (Exception e) {
            throw new RuntimeException("Error during the extraction of the model.", e);
        }
    }


    /**
     * Extraie la matrix du fichier.lp spécifié
     * @param pathToLPFile : Chemin vers le fichier.lp dont on veut extraire la matrix
     * @return : La matrix extraite du fichier.lp
     */
    static public IloLPMatrix getIloLPMatrixFromLPFile(String pathToLPFile) {
        try {
            IloCplex newCplex = new IloCplex();
            // On import le .lp (Cplex génère automatiquement la IloLPMatrix)
            newCplex.importModel(pathToLPFile);
            return (IloLPMatrix) newCplex.LPMatrixIterator().next();
        } catch (Exception e) {
            throw new RuntimeException("Error during the extraction of the model.", e);
        }
    }


    /**
     * Liste les fichiers à tester en parcourant l'arborescence pathToDir/Student
     * @param pathToStudentsDirs : Path du dossier où se trouve les dossiers des étudiants
     * @return : Une liste des (Fichier.mod à tester ; Fichier où se trouve la note) à tester
     */
    static public ArrayList<ArrayList<File>> getFilesToTest(String pathToStudentsDirs) {
        ArrayList<ArrayList<File>> filesToTests = new ArrayList<>();

        File dirToStudentsDirs = new File(pathToStudentsDirs);
        // On parcourt les dossier des étudiants
        for (File studentDir : Objects.requireNonNull(dirToStudentsDirs.listFiles())) {
            // On récupère les dossiers .ceg (contenant la note)
            File[] textDirs = studentDir.listFiles((dir12, name) -> name.endsWith(".ceg"));
            // S'il y en a
            if (textDirs != null) {
                // On les parcourt
                for (File textDir : textDirs) {
                    String textDirPath = textDir.getPath();
                    // On regarde si le fichier "gradecomments.txt" existe, si c'est pas le cas le modèle est incorrect
                    if (new File(textDirPath + File.separator + "gradecomments.txt").exists()) {
                        // Path vers le fichier contenant la note
                        String gradeFilePath = textDirPath + File.separator + "grade.txt";
                        // Path vers le dossier contenant le .mod (on cut le ".ceg")
                        String modFileDirPath = textDirPath.substring(0, textDirPath.length() - 4);
                        // Fichier contenant la note
                        File gradeFile = new File(gradeFilePath);
                        // Dossier contenant le .mod
                        File modFileDir = new File(modFileDirPath);
                        // On récupère les .mod
                        File[] modFiles = modFileDir.listFiles((dir14, name) -> name.endsWith(".mod"));
                        // S'il y a un fichier .mod
                        if (modFiles != null && modFiles.length > 0) {
                            // On le récupère
                            File modFile = modFiles[0];
                            // Si le fichier .mod et le fichier contenant la note existent
                            if (modFile.exists() && gradeFile.exists()) {
                                // On les ajoute à la liste de tests à effectuer
                                ArrayList<File> files = new ArrayList<>();
                                files.add(modFile);
                                files.add(gradeFile);
                                filesToTests.add(files);
                            }
                        }
                    }
                }
            }
        }
        return filesToTests;
    }

}
