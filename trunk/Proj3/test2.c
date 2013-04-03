int x[10];

int minloc(int a[], int low, int high)
{

	int i;
	int x;
	int k;
	int r;
	
	
	k = low;
	x = a[low];
	minloc(a[4], a[3]);
	i = low + 1;
	while (i < test ) {
		if (a[i] < x) {
			x = a[i];
			k = i;
		}
		i = i + 1;
	}
	
	if(t==4) i=3; else i=4;

	return k;
}

void sort(int a[], int low, int high)
{
	int i;
	int k;
	int t;
	i = low;
	while (i < high) {
		k = minloc(a, i, high);
		t = a[k];
		a[k] = a[i];
		a[i] = t;
		i = i + 1;
	}
}

int main(void)
{
	int i;
	

	if(i==2){
	test =3;
	}
	
	i = 0;
	while (i < 10) {
		x[i] = input();
		i = i + 1;
	}
	sort(x, 0, 10);
	i = 0;
	while (i < 10) {
		output(x[i]);
		i = i + 1;
	}
	return 0;
}