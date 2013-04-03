package exception;


public class SymbolNotFoundException extends LocalException {

	public SymbolNotFoundException(){
		this.ExceptionMessage = "Undefined Local Variable";
	}
}
