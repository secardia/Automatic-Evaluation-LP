dvar float x1;
dvar float x2;

float c[1..2] = [300, 500];

maximize c[1]*x1 + c[2]*x2;

subject to {
	//x1 >= 0;
	x2 >= 0;

 	x1 <= 4;
 	2*x2 <= 12;
 	//3*x1 + 2*x2 <= 18;
}
