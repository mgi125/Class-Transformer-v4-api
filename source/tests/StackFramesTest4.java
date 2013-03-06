package tests;

public class StackFramesTest4 {

	public StackFramesTest4() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		System.err.println((x() ? new x[10][10][10][10][10] : new y[10][10][10])[0][0][0]);
	}
	
	public static boolean x() {
		return true;
	}
	
	static class x {
		
	}
	
	static class y extends x {
		
	}

}
