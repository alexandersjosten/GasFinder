package sjosten.android.gasfinder.parser;

public class PanicException extends Exception {
	private static final long serialVersionUID = 7459050125299311623L;

	public PanicException() {
        super("Panic, the impossible happened!");
    }
    
    public PanicException(String msg) {
        super("Panic, the impossible happened! " + msg);
    }
}
