package mgi.tools.jtransformer.utilities;

public class ByteBuffer {

	private int position;

	private byte[] buffer;

	private static char[] unicodeTable = {'\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\0', '\u017e', '\u0178'};

	private static final int xteaDelta = 0x9E3779B9;

	private static final int xteaRounds = 32;

	public ByteBuffer() {
		this(new byte[5000], 0);
	}

	public ByteBuffer(byte[] data) {
		this(data, 0);
	}

	public ByteBuffer(int capacity) {
		this(new byte[capacity], 0);
	}

	public ByteBuffer(byte[] data, int offset) {
		this.buffer = data;
		this.position = offset;
	}

	public void setPosition(int position) {
		if (position < 0 || position >= this.buffer.length)
			throw new IllegalArgumentException();
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public byte[] toArray(int offset, int length) {
		byte[] bf = new byte[length - offset];
		for (int i = 0; i < length; i++) {
			bf[i] = this.buffer[offset + i];
		}
		return bf;
	}

	public byte[] getBuffer() {
		return this.buffer;
	}

	public final byte readSignedByte() {
		return this.buffer[this.position++];
	}

	public final int readUByte() {
		return this.buffer[this.position++] & 0xff;
	}

	public final void writeByte(int v) {
		this.buffer[this.position++] = (byte) v;
	}

	public final void readBytes(byte[] buffer, int offset, int length) {
		for (int pos = offset; pos < length + offset; pos++)
			buffer[pos] = this.buffer[this.position++];
	}

	public final void writeBytes(byte[] buffer, int offset, int length) {
		for (int pos = offset; pos < offset + length; pos++)
			this.buffer[this.position++] = buffer[pos];
	}

	public final int readUnsignedSmart() {
		int v = this.buffer[this.position] & 0xff;
		if (v >= 0x80)
			return readUnsignedShort() - 0x8000;
		return readUByte();
	}

	public final int readSignedSmart() {
		int v = this.buffer[this.position] & 0xff;
		if (v < 0x80)
			return readUByte() - 0x40;
		return readUnsignedShort() - 0xC000;
	}

	public final void writeSmart(int v) {
		if (v >= 0 && v < 0x80)
			this.writeByte(v);
		else if (v >= 0 && v < 0x8000)
			this.writeShort(v + 0x8000);
		else
			throw new IllegalArgumentException();
	}

	public final int readSignedShort() {
		this.position += 2;
		int i = ((this.buffer[this.position - 1] & 0xff) + (this.buffer[this.position - 2] << 8 & 0xff00));
		if (i > 0x7FFF)
			i -= 0x10000;
		return i;
	}
	public final int readUnsignedShort() {
		this.position += 2;
		return ((this.buffer[this.position - 2] << 8 & 0xff00) + (this.buffer[this.position - 1] & 0xff));
	}

	public final void writeShort(int s) {
		this.buffer[this.position++] = (byte) (s >> 8);
		this.buffer[this.position++] = (byte) s;
	}

	public final int readUnsignedMedInt() {
		this.position += 3;
		return ((this.buffer[this.position - 2] << 8 & 0xff00) + ((this.buffer[this.position - 3] & 0xff) << 16) + (this.buffer[this.position - 1] & 0xff));
	}

	public final int readSignedMedInt() {
		this.position += 3;
		int v = ((this.buffer[this.position - 1] & 0xff) + (this.buffer[this.position - 2] << 8 & 0xff00) + (this.buffer[this.position - 3] << 16 & 0xff0000));
		if (v > 0x7FFFFF)
			v -= 0x1000000;
		return v;
	}

	public final void writeMedInt(int v) {
		this.buffer[this.position++] = (byte) (v >> 16);
		this.buffer[this.position++] = (byte) (v >> 8);
		this.buffer[this.position++] = (byte) v;
	}

	public final int readInt() {
		this.position += 4;
		return ((this.buffer[this.position - 1] & 0xff) + ((this.buffer[this.position - 3] & 0xff) << 16) + ((this.buffer[this.position - 4] << 24 & ~0xffffff) + (this.buffer[this.position - 2] << 8 & 0xff00)));
	}

	public final void writeInt(int value) {
		this.buffer[this.position++] = (byte) (value >> 24);
		this.buffer[this.position++] = (byte) (value >> 16);
		this.buffer[this.position++] = (byte) (value >> 8);
		this.buffer[this.position++] = (byte) value;
	}

	public final long read5Byte() {
		long v0 = (long) readUByte() & 0xffffffffL;
		long v1 = (long) readInt() & 0xffffffffL;
		return v1 + (v0 << 32);
	}

	public final long readLong() {
		long v0 = (long) readInt() & 0xffffffffL;
		long v1 = (long) readInt() & 0xffffffffL;
		return (v0 << 32) + v1;
	}

	public final void writeLong(long v) {
		this.buffer[this.position++] = (byte) (int) (v >> 56);
		this.buffer[this.position++] = (byte) (int) (v >> 48);
		this.buffer[this.position++] = (byte) (int) (v >> 40);
		this.buffer[this.position++] = (byte) (int) (v >> 32);
		this.buffer[this.position++] = (byte) (int) (v >> 24);
		this.buffer[this.position++] = (byte) (int) (v >> 16);
		this.buffer[this.position++] = (byte) (int) (v >> 8);
		this.buffer[this.position++] = (byte) (int) v;
	}

	public final String readNullString() {
		if (this.buffer[this.position] == 0) {
			this.position++;
			return null;
		}
		return readString();
	}

	public final String readVersionedString() {
		return this.readVersionedString((byte) 0);
	}

	public final String readVersionedString(byte versionNumber) {
		byte vNumber = this.buffer[this.position++];
		if (vNumber != versionNumber)
			throw new IllegalStateException("Bad string version number!");
		int pos = this.position;
		while (this.buffer[this.position++] != 0) {
			/* empty */
		}
		int strLen = this.position - pos - 1;
		if (strLen == 0)
			return "";
		return decodeString(buffer, pos, strLen);
	}

	public final void writeVersionedString(String str) {
		this.writeVersionedString(str, (byte) 0);
	}

	public final void writeVersionedString(String str, byte version) {
		int nullIdx = str.indexOf('\0');
		if (nullIdx >= 0)
			throw new IllegalArgumentException("NUL character at " + nullIdx + "!");
		this.buffer[this.position++] = (byte) version;
		this.position += encodeString(buffer, this.position, str, 0, str.length());
		this.buffer[this.position++] = (byte) 0;
	}

	public final String readString() {
		int pos = this.position;
		while (this.buffer[this.position++] != 0) {
			/* empty */
		}
		int strlen = this.position - pos - 1;
		if (strlen == 0)
			return "";
		return decodeString(buffer, pos, strlen);
	}

	public final void writeString(String string) {
		int n = string.indexOf('\0');
		if (n >= 0)
			throw new IllegalArgumentException("NUL character at " + n + "!");
		this.position += encodeString(buffer, this.position, string, 0, string.length());
		this.buffer[this.position++] = (byte) 0;
	}

	public final void writeStringUTF(String string) {
		writeShort(string.length());
		for (int i = 0; i < string.length(); i++)
			writeShort(string.charAt(i));
	}

	public final String readStringUTF() {
		int strLen = readUnsignedShort();
		char[] buffer = new char[strLen];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (char) readUnsignedShort();
		return new String(buffer);
	}

	public final int readSum() {
		int sum = 0;
		int incr = readUnsignedSmart();
		while (incr == 32767) {
			incr = readUnsignedSmart();
			sum += 32767;
		}
		sum += incr;
		return sum;
	}

	public final int readVarSeized() {
		int f = this.buffer[this.position++];
		int sum = 0;
		for (; f < 0; f = this.buffer[this.position++])
			sum = (sum | f & 0x7f) << 7;
		return sum | f;
	}

	public final void writeVarSeized(int val) {
		if ((val & ~0x7f) != 0) {
			if ((val & ~0x3fff) != 0) {
				if ((val & ~0x1fffff) != 0) {
					if ((val & ~0xfffffff) != 0)
						this.writeByte(val >>> 28 | 0x80);
					this.writeByte((val | 0x100a2c1c) >>> 21);
				}
				this.writeByte(val >>> 14 | 0x80);
			}
			this.writeByte((val | 0x4021) >>> 7);
		}
		this.writeByte(val & 0x7f);
	}

	public final long readDynamic(int numBytes) throws IllegalArgumentException {
		if (--numBytes < 0 || numBytes > 7)
			throw new IllegalArgumentException();
		long value = 0L;
		for (int bitsLeft = numBytes * 8; bitsLeft >= 0; bitsLeft -= 8)
			value |= ((long) this.buffer[this.position++] & 0xffL) << bitsLeft;
		return value;
	}

	public final void writeDynamic(int numBytes, long value) {
		if (--numBytes < 0 || numBytes > 7)
			throw new IllegalArgumentException();
		for (int bitsLeft = numBytes * 8; bitsLeft >= 0; bitsLeft -= 8)
			this.buffer[this.position++] = (byte) (int) (value >> bitsLeft);
	}

	public final void encryptXTEA(int[] keys, int offset, int length) {
		int originalPosition = this.position;
		this.position = offset;
		int numCycles = (length - offset) / 8;
		for (int cycle = 0; cycle < numCycles; cycle++) {
			int v0 = readInt();
			int v1 = readInt();
			int sum = 0;
			int numRounds = xteaRounds;
			while (numRounds-- > 0) {
				v0 += (sum + keys[sum & 0x3] ^ (v1 << 4 ^ v1 >>> 5) + v1);
				sum += xteaDelta;
				v1 += (sum + keys[(sum & 0x1d2f) >>> 11] ^ v0 + (v0 << 4 ^ v0 >>> 5));
			}
			this.position -= 8;
			writeInt(v0);
			writeInt(v1);
		}
		this.position = originalPosition;
	}

	public final void xteaDecrypt(int[] keys, int offset, int length) {
		int originalPosition = this.position;
		this.position = offset;
		int numCycles = (length - offset) / 8;
		for (int cycle = 0; cycle < numCycles; cycle++) {
			int v0 = readInt();
			int v1 = readInt();
			int numRounds = xteaRounds;
			int sum = xteaDelta * numRounds;
			while (numRounds-- > 0) {
				v1 -= (sum + keys[(sum & 0x196e) >>> 11] ^ (v0 << 4 ^ v0 >>> 5) + v0);
				sum -= xteaDelta;
				v0 -= keys[sum & 0x3] + sum ^ v1 + (v1 >>> 5 ^ v1 << 4);
			}
			this.position -= 8;
			this.writeInt(v0);
			this.writeInt(v1);
		}
		this.position = originalPosition;
	}

	static final String decodeString(byte[] buffer, int offset, int strLen) {
		char[] strBuffer = new char[strLen];
		int write = 0;
		for (int dc = 0; dc < strLen; dc++) {
			int data = buffer[dc + offset] & 0xff;
			if (data == 0)
				continue;
			if (data >= 128 && data < 160) {
				char uni = unicodeTable[data - 128];
				if (uni == 0)
					uni = '?';
				strBuffer[write++] = (char) uni;
				continue;
			}
			strBuffer[write++] = (char) data;
		}
		return new String(strBuffer, 0, write);
	}

	public static final int encodeString(byte[] buffer, int bufferOffset, String str, int strOffset, int strLen) {
		int charsToEncode = strLen - strOffset;
		for (int cc = 0; cc < charsToEncode; cc++) {
			char c = str.charAt(cc + strOffset);

			if ((c > 0 && c < 128) || (c >= 160 && c <= 255)) {
				buffer[bufferOffset + cc] = (byte) c;
				continue;
			}

			switch (c) {
				case '\u20ac' :
					buffer[bufferOffset + cc] = -128;
					break;
				case '\u201a' :
					buffer[bufferOffset + cc] = -126;
					break;
				case '\u0192' :
					buffer[bufferOffset + cc] = -125;
					break;
				case '\u201e' :
					buffer[bufferOffset + cc] = -124;
					break;
				case '\u2026' :
					buffer[bufferOffset + cc] = -123;
					break;
				case '\u2020' :
					buffer[bufferOffset + cc] = -122;
					break;
				case '\u2021' :
					buffer[bufferOffset + cc] = -121;
					break;
				case '\u02c6' :
					buffer[bufferOffset + cc] = -120;
					break;
				case '\u2030' :
					buffer[bufferOffset + cc] = -119;
					break;
				case '\u0160' :
					buffer[bufferOffset + cc] = -118;
					break;
				case '\u2039' :
					buffer[bufferOffset + cc] = -117;
					break;
				case '\u0152' :
					buffer[bufferOffset + cc] = -116;
					break;
				case '\u017d' :
					buffer[bufferOffset + cc] = -114;
					break;
				case '\u2018' :
					buffer[bufferOffset + cc] = -111;
					break;
				case '\u2019' :
					buffer[bufferOffset + cc] = -110;
					break;
				case '\u201c' :
					buffer[bufferOffset + cc] = -109;
					break;
				case '\u201d' :
					buffer[bufferOffset + cc] = -108;
					break;
				case '\u2022' :
					buffer[bufferOffset + cc] = -107;
					break;
				case '\u2013' :
					buffer[bufferOffset + cc] = -106;
					break;
				case '\u2014' :
					buffer[bufferOffset + cc] = -105;
					break;
				case '\u02dc' :
					buffer[bufferOffset + cc] = -104;
					break;
				case '\u2122' :
					buffer[bufferOffset + cc] = -103;
					break;
				case '\u0161' :
					buffer[bufferOffset + cc] = -102;
					break;
				case '\u203a' :
					buffer[bufferOffset + cc] = -101;
					break;
				case '\u0153' :
					buffer[bufferOffset + cc] = -100;
					break;
				case '\u017e' :
					buffer[bufferOffset + cc] = -98;
					break;
				case '\u0178' :
					buffer[bufferOffset + cc] = -97;
					break;
				default :
					buffer[bufferOffset + cc] = (byte) '?';
					break;
			}
		}
		return charsToEncode;
	}

}
