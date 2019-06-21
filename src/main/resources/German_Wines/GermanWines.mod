float c[1..3] = [1, 1, 1];

dvar float+ x1;
dvar float+ x2;
dvar float+ x3;

maximize c[1]*x1 + c[2]*x2 + c[3]*x3;

subject to {
	1.0*x1 + 2.0*x2 + 0.0*x3 <= 150.0;
	1.0*x1 + 0.0*x2 + 2.0*x3 <= 150.0;
	2.0*x1 + 1.0*x2 + 0.0*x3 <= 80.0;
	2.0*x1 + 3.0*x2 + 1.0*x3 <= 225.0;
}
