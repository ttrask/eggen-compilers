

public class InvalidTypeException extends LocalException {

	public InvalidTypeException() {
		this.ExceptionMessage = "Invalid Type Conversion.";
	}

	public InvalidTypeException(String s) {
		this.ExceptionMessage = s;
	}
}
