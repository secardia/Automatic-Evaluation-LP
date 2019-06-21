package Model;

import java.util.ArrayList;

public class Test {

    private ArrayList<Double> coefs;
    private double expectedVal;


    /* ------------------ Constructeurs ------------------ */


    /**
     * Construit un test constitué des coefficients (c[...]) et de la valeur attendue.
     * @param coefs liste des coefficients (c[...])
     * @param expectedVal valeur attendue
     */
    public Test(ArrayList<Double> coefs, double expectedVal) {
        this.coefs = coefs;
        this.expectedVal = expectedVal;
    }


    /* ------------------ Getters et Setters ------------------ */


    public ArrayList<Double> getCoefs() {
        return coefs;
    }

    public double getExpectedVal() {
        return expectedVal;
    }
}
