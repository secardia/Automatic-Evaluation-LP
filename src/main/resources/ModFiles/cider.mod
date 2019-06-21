/*********************************************
* OPL 12.6.0.0 Model
* Cider
*********************************************/

//Data declarations.
//Make sure you use c[i] to access the i-th cost 
//and do not remove/change the following line
float c[1..4] = [-1500,4, 8, 10];

//Decision variables.
dvar float+ xj;
dvar float+ xc;
dvar float+ xjc;
dvar float+ xcc;

//Objective function.
maximize c[1]*(xj+xc) + c[2]*(500*xj-xjc) + c[3]*(250*xc + 0.6*xjc - xcc) + c[4]*0.4*xcc;

//Constraints
subject to {
    500*xj         - xjc                <= 5000;
            250*xc + 0.6*xjc    - xcc   <= 2000;
    500*xj         - xjc                >= 0;
            250*xc + 0.6*xjc    - xcc   >= 0;
                                0.4*xcc <=500;
}

// Display
execute {
 writeln("Post treatment: ");
 writeln("The objectif's value is  "+cplex.getObjValue());
}