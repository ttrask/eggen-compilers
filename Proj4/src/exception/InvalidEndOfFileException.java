package exception;

public class InvalidEndOfFileException extends LocalException {
	public InvalidEndOfFileException() {
		this.ExceptionMessage = "Invalid end of file.";
	}
}
