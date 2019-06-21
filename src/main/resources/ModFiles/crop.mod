float c[1..2] = [1, 1];

    dvar float x1;
    dvar float x2;

    maximize c[1]*x1 + c[2]*x2;

    subject to {
    //1.0*x1 >= 0.0;
    1.0*x2 >= 0.0;

    2.0*x1 + 1.0*x2 <= 8.0;
    //1.0*x1 + 2.0*x2 <= 7.0;
    0.0*x1 + 1.0*x2 <= 3.0;


}
