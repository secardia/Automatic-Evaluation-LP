package Model;

import java.util.ArrayList;


public class ExtremePoint {

    private LinearProgram lp;

    // Coordonnés du point
    private ArrayList<Double> coord;

    // Bases correspondant à ce point extrême
    private ArrayList<Basis> bases;


    /* ------------------ Constructeurs ------------------ */


    /**
     * Construit un point extrême à partir de base la base spécifiée.
     * @param basis base le décrivant
     */
    public ExtremePoint(Basis basis, LinearProgram lp) {
        this.lp = lp;
        // Ses coordonnées sont les varNb premières coord de la base le décrivant
        this.coord = new ArrayList<>(basis.getCoord().subList(0, lp.getVarNb()));
        this.bases = new ArrayList<>();
        this.addBasis(basis);
    }


    /**
     * Construit une copie du point extrême spécifié
     * @param extremePointToCopy point extrême à copier
     */
    public ExtremePoint(ExtremePoint extremePointToCopy) {
        this.lp = extremePointToCopy.lp;
        this.coord = new ArrayList<>(extremePointToCopy.coord);
        this.bases = new ArrayList<>(extremePointToCopy.bases);
    }


    /* ------------------ Fonctions ------------------ */


    /**
     * Compare l'objet spécifié avec ce point extrême pour vérifier s'ils sont égaux. Deux points extrêmes sont égaux
     * s'ils ont les mêmes coordonnés.
     * @param obj l'objet à comparer
     * @return true si l'objet est un point extrême et si ses coordonnés sont égales à celles de ce point extrême, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExtremePoint)) {
            return false;
        } else {
            // Si une des valeurs de coord diffère on renvoie false
            for (int i = 0; i < coord.size(); i++) {
                Double a = coord.get(i);
                Double b = ((ExtremePoint) obj).coord.get(i);
                if (!a.equals(b)) {
                    return false;
                }
            }
            // Si aucune différence n'est relevée on return true
            return true;
        }
    }


    @Override
    public String toString() {
        return coord.toString();
    }


    /* ------------------ Getters et Setters ------------------ */


    /**
     * Ajoute la base spécifiée à la liste des bases décrivant ce point extrême si elle n'est pas déjà dedans et
     * qu'elle décrit bien ce point extrême.
     * @param basis la base à ajouter à la liste
     */
    public void addBasis(Basis basis) {
        // Si les coordonnées de la base sont bien égales à celle de this et que la base n'est pas déjà connue
        if (this.coord.equals(basis.getCoord().subList(0, lp.getVarNb())) && !bases.contains(basis)) {
            // On ajoute la base
            bases.add(basis);
        }
    }

    public ArrayList<Basis> getBases() {
        return bases;
    }

    public ArrayList<Double> getCoord() {
        return coord;
    }
}
