package tests;

public class FinallyTest {

	public static void main(String[] args) {
		try {
			System.err.println("Try code");
		}
		catch (RuntimeException ex) {
			System.err.println("Catch 1 code");
		}
		catch (Throwable t) { 
			System.err.println("Catch 2 code");
		}
		finally {
			System.err.println("Here comes the finally handler");
		}
	}

}
