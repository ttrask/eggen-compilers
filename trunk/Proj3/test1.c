int gcd(int u, int v)
{
	if (v == 0){
		return u;}
	else{
		return gcd(v, u - u / v * v);}
		return 1;
}
int input(void){
return 1;
}

void output(int x){

}
int main(void)
{
	int x;
	int y;
	x = input();
	y = input();
	output(gcd(x, y));

	return 0;
}