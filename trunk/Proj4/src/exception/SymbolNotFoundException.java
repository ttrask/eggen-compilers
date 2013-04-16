package exception;


public class SymbolNotFoundException extends LocalException {

	static final long serialVersionUID = 1203847938;
	
	public SymbolNotFoundException(){
		this.ExceptionMessage = "Undefined Local Variable";
	}
}
