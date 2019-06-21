dvar float+ xA;
dvar float+ xB;
dvar float+ xR;
dvar float+ v;

float c[1..4] = [20, 25, 15, 0.25];

maximize c[1]*xA + c[2]*xB + c[3]*xR + c[4]*v;

subject to {
    5*xA  +  5*xB +  2*xR   <= 3000;
    3*xA                    <= 1000;
    2*xA  +  5*xB +  4*xR   <= 4000;
    15*xA + 30*xB + 10*xR   <= 250*60;
    //v == 8000 - 10*xA - 10*xB - 6*xR;
}