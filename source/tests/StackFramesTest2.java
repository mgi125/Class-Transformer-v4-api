package tests;

public class StackFramesTest2 {

	public StackFramesTest2() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		test1();
		test2();
	}
	
	public static void test1() {
		Object o = getSomething() > getSomething() ? new c1() : new c2();
		System.err.println(((i1)o).doSomething());
	}
	
	public static void test2() {
		Object o = getSomething() > getSomething() ? new c3() : new c4();
		System.err.println(((i2)o).doSomething());
	}
	
	public static int getSomething() {
		return getSomething() - getSomething();
	}
	
	
	static class c1 implements i1 {
		public int doSomething() {
			return hashCode() - doSomething();
		}
	}
	
	static class c2 implements i1 {
		public int doSomething() { 
			return hashCode() + doSomething();
		}
	}
	
	static class c3 implements i2 {
		public int doSomething() {
			return hashCode() - doSomething();
		}
	}
	
	static class c4 implements i2 {
		public int doSomething() { 
			return hashCode() + doSomething();
		}
	}
	
	static interface i1 {
		int doSomething();
	}
	
	static interface i2 {
		int doSomething();
	}

}
