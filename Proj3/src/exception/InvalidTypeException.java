package exception;

public class InvalidTypeException extends LocalException {

	public InvalidTypeException() {

	}

	public InvalidTypeException(String s) {
		this.ExceptionMessage = s;
	}
}
