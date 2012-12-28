package tests;

@SuppressWarnings("unused")
public class CopyOftestclass {

	public float a,b,c,d,e,f,g,h;
	public static float aa,ab,ac,ad,ae,af,ag,ah;
	public static long  ba,bb,bc,bd,be,bf,bg,bh;
	public static int   ca,cb,cc,cd,ce,cf,cg,ch;
	public static byte  xa,xb,xc,xd,xe,xf,xg,xh;
	public byte x;
	public int y;
	public long l;
	public long[] ax,bx,cx;
	
	
	public void itest() {
		int x = 5;
		int a = x += 50;
		int b = a++;
	}
	
	public void ltest() {
		long l = 0;
		long a = l++;
	}
	
	public void bytetest() {
		byte x = 0;
		x += 50;
	}
	
	public void fulltest() {
		int var = 0;
		long var2 = var += 10;
		var += 50;
		var2 = var++;
		var2 = ca += 20;
		ca += 20;
		var2 = ca++;
		var2 = y += 20;
		y += 20;
		var2 = y++;
		var2 = ax[14] += 20;
		ax[14] += 20;
		var2 = ax[14]++;
	}
	
	public void arraytest() {
		boolean l = new Integer(ax.length) instanceof Integer;
		long a = ax[47 + ca + 666] += 500;	
		ax[47 + ca * cb * cc * ch + (int)((long)a + (long)((double)b * aa)) + 666]++;
	}
	
	public void trycatchtest() {
		/*
		try {
			throw null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			throw new RuntimeException("oh god");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}*/
	}
	
	
	public void stest() {
		long a = a(Long.parseLong(o()), this.l++, l += (l += 5888), ba + bb + bc + bd + a(be,bf,bg,bh));
		xtest();
		ytest();
		x_stest();
		y_stest();
		mergeTest(a, a);
		System.err.println("asdfkak" + ba + "asdjfkjk" + bb + "asdfkj" + l + "akdfj" + a + "djkjkfj" + h);
		boolean b = "fdkj" + l instanceof String;
	}
	
	public void xtest() {
		long a = this.l++;
	}
	
	public void ytest() {
		long a = ++this.l;
	}
	
	public void x_stest() {
		long a = ba++;
	}
	
	public void y_stest() {
		long a = ++ba;
	}
	
	public void mergeTest(long a, long c) {
		long b = this.l += 560;
	}
	
	public void storeTest(float x, float y) {
		a = x;
		b = y;
		aa = ab = ac = 1F;
		a = b = 0F;
	}
	
	public <A,K,E> long a(long a,long b,long c,long d) throws RuntimeException {
		a = b = c = d = 0;
		int x = (char)c;
		int cc = (int)(a * (b + c));
		return a + b * 2 * 3 ^ 50 ^ c ^ d | a | d;
	}
	
	public void ff() {
		float x = a + d + f + h + g;
		long a = 0;
		long b = a++;
	}
	
	public String o() {
		return "stringtest";
	}
	
	public void ternaryTest() {
		boolean b = ca > cb;
		int x = (ca > cb || ca < cb || ca == cb) ? 454547 : 454545;
	}
}
