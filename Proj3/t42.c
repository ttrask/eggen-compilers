
/* should parse void function parms       */
void main(void)
{
  void i;
  void j;
  car (i, j);
}


int car(void x, void  y)
{
   int q;
   
   q=x+4;

  return q;
}