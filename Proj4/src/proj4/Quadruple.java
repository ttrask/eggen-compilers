package proj4;

public class Quadruple {
	public int LineNumber;
	public String CallSymbol;
	public String Param1;
	public String Param2;
	public String Output;
	
	public Quadruple(){
		
	}
	
	public Quadruple(String symbol, String param1, String param2, String output){
		CallSymbol = symbol;
		Param1 = param1;
		Param2 = param2;
		Output = output;
	}
	
	public Quadruple(String symbol, String param1, String param2, String output, int linenumber){
		CallSymbol = symbol;
		Param1 = param1;
		Param2 = param2;
		Output = output;
		LineNumber = linenumber;
	}
}
