package tests;

public class StackFramesTest6 {

	public StackFramesTest6() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		//byte b = (x() ? new y[10][10] : new x[10][10]);
		//x[][] x = (x() ? new y[10][10] : new x[10][10]);
	}
	
	public static boolean x() {
		return true;
	}
	
	static class x {
		
	}
	
	static class y extends x {
		
	}

}
