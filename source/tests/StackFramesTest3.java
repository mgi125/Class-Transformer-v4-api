package tests;

public class StackFramesTest3 {

	public StackFramesTest3() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		System.err.println(((Object[][])(x() ? new int[10][10][10] : new long[10][10][10]))[0]);
	}
	
	public static boolean x() {
		return true;
	}

}
