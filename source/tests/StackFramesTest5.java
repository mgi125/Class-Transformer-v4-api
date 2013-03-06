package tests;

public class StackFramesTest5 {

	public StackFramesTest5() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Object x = new byte[100][100];
		Object y = new byte[100][100][100];
		//System.err.println((x() ? new byte[10][10][10][10] : new byte[10][10]).length); // javac works fine while eclipse not?
	}
	
	public static boolean x() {
		return true;
	}
	
	static class x {
		
	}
	
	static class y extends x {
		
	}

}
