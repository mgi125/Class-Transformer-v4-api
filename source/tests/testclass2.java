package tests;

public class testclass2 {
	
	public float a,b,c,d,e,f,g,h;
	public static float aa,ab,ac,ad,ae,af,ag,ah;
	public static long  ba,bb,bc,bd,be,bf,bg,bh;
	public static int   ca,cb,cc,cd,ce,cf,cg,ch;
	public static byte  xa,xb,xc,xd,xe,xf,xg,xh;
	public int ya,yb,yc,yd,ye,yf,yg,yh;
	public byte x;
	public int y;
	public long l;
	public long[] ax,bx,cx;

	public testclass2() {
		// TODO Auto-generated constructor stub
	}
	
	public void test() {
		byte[] b = new byte[100];
		byte bytevar = byteMethod();
		b[0] = bytevar;
		
	}
	
	public byte byteMethod() {
		return 0;
	}

}
