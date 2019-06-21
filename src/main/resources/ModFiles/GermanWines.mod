/*********************************************
 * OPL 12.6.0.0 Model
 * German Wines
 *********************************************/

//Make sure you use c[i] to access the i-th cost 
//and do not remove/change the following line
float c[1..3] = [10, 12, 20];

//variables
dvar float+ sw; // Vin sweet en gallon
dvar float+ re; // Vin regular en gallon
dvar float+ ex; // Vin deustchland extra dry en gallon

maximize c[1]*sw + c[2]*re + c[3]*ex;

subject to {
    sw + 2*re <= 150;
    sw + 2*ex <= 150;
    2*sw + re <= 80;
    2*sw + 3*re + ex <= 225;
    sw >= 0;
    re >= 0;
    ex >= 0;
}

/* Affichage de la solution */
execute {
  writeln("Post-traitement: ");
  writeln("La valeur de l'objectif est de "+cplex.getObjValue());
} 