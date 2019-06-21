/*********************************************
 * OPL 12.6.0.0 Model
 * German Wines
 *********************************************/

//Make sure you use c[i] to access the i-th cost 
//and do not remove/change the following line
float c[1..3] = [10, 12, 20];

dvar float Heidelberg_sweet;
dvar float Heidelberg_regular;
dvar float Deutschl_extra_dry; 

maximize c[1]*Heidelberg_sweet + c[2]*Heidelberg_regular + c[3]*Deutschl_extra_dry;

subject to {

}

/* Affichage de la solution */
execute {
  writeln("Post-traitement: ");
  writeln("La valeur de l'objectif est de "+cplex.getObjValue());
} 