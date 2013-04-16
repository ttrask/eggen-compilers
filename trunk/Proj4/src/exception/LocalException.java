package exception;



public class LocalException extends Exception {
	public String ExceptionMessage;
	
	public LocalException(){
		
	}
	
	public LocalException(String m){
		ExceptionMessage = m;
	}
}
