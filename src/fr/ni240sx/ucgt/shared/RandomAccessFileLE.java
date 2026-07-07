package fr.ni240sx.ucgt.shared;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileLE extends RandomAccessFile {

	public RandomAccessFileLE(String name, String mode) throws FileNotFoundException {
		super(name, mode);
	}

	public RandomAccessFileLE(File file, String mode) throws FileNotFoundException {
		super(file, mode);
	}
	
	public short getShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 0) + (ch2 << 8));
    }
	
	public final int getUnsignedShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 0) + (ch2 << 8);
    }

	public final char getChar() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 0) + (ch2 << 8));
    }
	
	public final int getInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }
	
	public final long getLong() throws IOException {
        return (getInt() & 0xFFFFFFFFL) + ((long)(getInt()) << 32);
    }
	
	public final float getFloat() throws IOException {
        return Float.intBitsToFloat(getInt());
    }
	
	public final double getDouble() throws IOException {
        return Double.longBitsToDouble(getLong());
    }
	
	public final void putShort(int v) throws IOException {
        write((v >>> 0) & 0xFF);
        write((v >>> 8) & 0xFF);
        //written += 2;
    }
	
	public final void putChar(int v) throws IOException {
        write((v >>> 0) & 0xFF);
        write((v >>> 8) & 0xFF);
        //written += 2;
    }
	
	public final void putInt(int v) throws IOException {
        write((v >>>  0) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 24) & 0xFF);
        //written += 4;
    }
	
	public final void putLong(long v) throws IOException {
        write((int)(v >>>  0) & 0xFF);
        write((int)(v >>>  8) & 0xFF);
        write((int)(v >>> 16) & 0xFF);
        write((int)(v >>> 24) & 0xFF);
        write((int)(v >>> 32) & 0xFF);
        write((int)(v >>> 40) & 0xFF);
        write((int)(v >>> 48) & 0xFF);
        write((int)(v >>> 56) & 0xFF);
        //written += 8;
    }
	
	public final void putFloat(float v) throws IOException {
        putInt(Float.floatToIntBits(v));
    }

    public final void putDouble(double v) throws IOException {
        putLong(Double.doubleToLongBits(v));
    }
	
	
	
}
