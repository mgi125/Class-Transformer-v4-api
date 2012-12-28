package tests;

public class MulTest {
	
	public int var;
	public byte[] array;
	
	public byte stdINC() { 
		return array[var++];
	}
	
	public byte mulINC() {
		return array[(var -= 1414836779) * -890535043 - 1];
	}
	
	public byte mulExpr(int i) {
		return array[var = i * 50];
	}
	
}
