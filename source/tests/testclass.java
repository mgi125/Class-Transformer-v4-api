package tests;

public class testclass {

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
	
	public double testx() {
		byte[] somearray = new byte[1];
		somearray[0] = bm();
		byte varx = -1;
		return varx += l * 1D;
		//System.err.println(varx);
	}
	
	public byte bm() {
		return -1;
	}
	
	
	public static int[] anIntArray1 = new int[64];
	public static int anInt1 = -30241709;
	
	public static void test(int i) {
		anIntArray1[(anInt1 += -30241709) * 1450153947 - 1] = 5;
		if(anInt1 * 1450153947 == i) {
			System.out.println("yay");
		} else {
			System.out.println("noo");
		}
	}
	
	public testclass(int a, int b, int c, int d, int e, int f, int g, int h) {
		
	}
	
	public void exprtest2() {
		byte var1 = (byte)ca;
		int var2 = cb;
		int var3 = cc;
		
		float x = 1L;
		

		byte[] array = new byte[1];
		for (int i = 0; i < ch; i++) {
			//System.err.println(var1++);
			//array[0]++;
			//++get().ya;
			//++ya;
			System.err.println(++get().ya);
			//System.err.println(++array[0]);
			//System.err.println(++var1);
		}
		
		int[] test = new int[] { get().hashCode(), get().hashCode() };
		cd = var1;
		

	}
	
	public void exprtest() {
		
		int var1 = 10;
		int var2 = 20;
		int var3 = 30;
		
		//ya = yb = (10 + var1);
		
		//var1 = var2 = var3 = 40;
		
		ya = yb = 2;
		
		ax[ya = yb = var1 = var2 = (40 * 20 + (int)ax[ca * cd])] = bx[yb] = var3 =  2;
	
		System.err.println(var1 + "," + var2 + "," + var3);
	}
	
	public testclass get() {
		return null;
	}
	
	public void arraytest() {
		
		int var = -12;
		int[] primitive = new int[10];
		Object[] objs = new Object[10];
		Object[][][][] multi = new Object[10][10][][];
		
		primitive[1] = (Integer)objs[0] + (Integer)multi[0][1][3][2];
		objs[0] = new testclass(primitive[0], primitive[1], primitive[2], primitive[3], primitive[4], primitive[5], primitive[6], primitive[7]);
	
		if (primitive[1] == 2) {
			objs[-var] = null;
		}
		
		objs[1] = null;
		
		synchronized (objs) {
			objs[1] = objs;
		}
		
		switch (primitive[1 + 2]) {
			case 0:
				System.err.println("lol");
				break;
			default:
				System.err.println("Wtf");
				break;
		}
		
		if (((Long)objs[1]).longValue() == ((Long)objs[2]).longValue()) {
			System.err.println("Blah");
		}
	
	}
	
	public void blahTest() {
		long var2 = ax[11] = 2L;
	}
	
	
	private static int[] p;
	private static char[] m;
	static {
		label0: {
			p = new int[128];
			int i1 = 0;
			while (~i1 > ~p.length) {
				p[i1] = -1;
				i1++;
			}
			break label0;
		}
		label1: {
			int j1 = 65;
			while (-91 <= ~j1) {
				p[j1] = -65 + j1;
				j1++;
			}
			break label1;
		}
		label2: {
			int k1 = 97;
			while (122 >= k1) {
				p[k1] = k1 + -71;
				k1++;
			}
			break label2;
		}
		label3: {
			int l1 = 48;
			while (-58 <= ~l1) {
				p[l1] = (-48 + l1) - -52;
				l1++;
			}
			break label3;
		}
		label4: {
			p[43] = 62;
			int ai[] = p;
			ai[42] = 62;
			p[47] = 63;
			int ai1[] = p;
			ai1[45] = 63;
			m = new char[64];
			int i2 = 0;
			while (26 > i2) {
				m[i2] = (char) (65 + i2);
				i2++;
			}
			break label4;
		}
		label5: {
			int j2 = 26;
			while (~j2 > -53) {
				m[j2] = (char) (-26 + (97 - -j2));
				j2++;
			}
			break label5;
		}
		label6: {
			int k2 = 52;
			while (k2 < 62) {
				m[k2] = (char) (-52 + k2 + 48);
				k2++;
			}
			break label6;
		}
		m[63] = '/';
		m[62] = '+';
	}
}
