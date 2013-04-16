package exception;


public class UnexpectedTokenException extends LocalException {
	static final long serialVersionUID = 1203847937;
	
	public UnexpectedTokenException(){
		this.ExceptionMessage = "Unexpected Token.";
	}
}
