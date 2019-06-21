/*********************************************
 * OPL 12.6.0.0 Model
 * German Wines
 *********************************************/

//Make sure you use c[i] to access the i-th cost 
//and do not remove/change the following line
float c[1..3] = [10, 12, 20];

/// NB 22/01 : Vous pouvez utiliser float+ pour indiquer que vos variables sont positives
dvar float Heidelberg_sweet;
dvar float Heidelberg_regular;
dvar float Deutschl_extra_dry;

maximize (c[1]*Heidelberg_sweet + c[2]*Heidelberg_regular + c[3]*Deutschl_extra_dry);

subject to {
    Heidelberg_sweet + 2*Heidelberg_regular <= 150 ;
    Heidelberg_sweet + 2*Deutschl_extra_dry <= 150;
    2*Heidelberg_sweet + Heidelberg_regular <= 80;
    2*Heidelberg_sweet + 3*Heidelberg_regular + Deutschl_extra_dry <= 255; /// 225 ou 255
    Heidelberg_sweet >= 0;
    Heidelberg_regular >= 0;
    Deutschl_extra_dry >= 0;
}

/* Affichage de la solution */
execute {
  writeln("Post-traitement: ");
  writeln("La valeur de l'objectif est de "+cplex.getObjValue());
} 