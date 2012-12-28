package mgi.tools.jtransformer.utilities;

public class BITBuffer extends ByteBuffer {

	private int bitPosition;

	private static int[] bitMasks = {0, 0x1, 0x3, 0x7, 0xf, 0x1f, 0x3f, 0x7f, 0xff, 0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff, 0x7fff, 0xffff, 0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, -1};

	public BITBuffer() {
		super();
	}

	public BITBuffer(byte[] data) {
		super(data);
	}

	public BITBuffer(int capacity) {
		super(capacity);
	}

	public BITBuffer(byte[] data, int offset) {
		super(data, offset);
	}

	public final int bitOffset(int numBits) {
		return this.bitPosition - (numBits * 8);
	}

	public final void startBIT() {
		this.bitPosition = this.getPosition() * 8;
	}

	public final void endBIT() {
		this.setPosition((this.bitPosition + 7) / 8);
	}

	public final void writeBits(int count, int value) {
		int bytePos = this.bitPosition >> 3;
		int bitOffset = 8 - (this.bitPosition & 7);
		this.bitPosition += count;
		this.setPosition((this.bitPosition + 7) / 8);
		for (; count > bitOffset; bitOffset = 8) {
			this.getBuffer()[bytePos] &= (byte) ~bitMasks[bitOffset];
			this.getBuffer()[bytePos++] |= (byte) ((value >> (count - bitOffset)) & bitMasks[bitOffset]);
			count -= bitOffset;
		}
		if (count == bitOffset) {
			this.getBuffer()[bytePos] &= (byte) ~bitMasks[bitOffset];
			this.getBuffer()[bytePos] |= (byte) (value & bitMasks[bitOffset]);
		} else {
			this.getBuffer()[bytePos] &= (byte) ~(bitMasks[count] << (bitOffset - count));
			this.getBuffer()[bytePos] |= (byte) ((value & bitMasks[count]) << (bitOffset - count));
		}
	}

	public final int readBITS(int count) {
		int bufferPosition = this.bitPosition >> 3;
		int bitOffset = 8 - (this.bitPosition & 0x7);
		this.bitPosition += count;
		int value = 0;
		for (; count > bitOffset; bitOffset = 8) {
			value += ((this.getBuffer()[bufferPosition++] & bitMasks[bitOffset]) << count - bitOffset);
			count -= bitOffset;
		}
		if (count != bitOffset)
			value += (this.getBuffer()[bufferPosition] >> bitOffset - count & bitMasks[count]);
		else
			value += (bitMasks[bitOffset] & this.getBuffer()[bufferPosition]);
		return value;
	}

}
