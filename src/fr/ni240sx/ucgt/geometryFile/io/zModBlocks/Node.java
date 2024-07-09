package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Node extends ZModBlock {

	public int int1=1, int2=0, int3=1; //int3=0 if specificName isn't empty
	public boolean bool5=false;
	public String specificName="", name="";
	public int int6=0, int7=0;
	
	public float[][] matrix = {	{1.0f,	0,		0,		0},
								{0,		1.0f,	0,		0},
								{0,		0,		1.0f,	0},
								{0,		0,		0,		1.0f}};
	
	public Node(int ID) {
		super(ID);
	}

	public Node() {
	}

	@Override
	public void readData(ByteBuffer in) {
		int1 = in.getInt();
		int2 = in.getInt();
		int3 = in.getInt();
		specificName = ZModBlock.readString(in); //often empty, ?
		name = ZModBlock.readString(in); //name
		int6 = in.getInt();
		int7 = in.getInt();
		for (int i=0;i<4;i++) for (int j=0;j<4;j++) matrix[i][j] = in.getFloat();
	}

	@Override
	public String getName() {
		return "scene::CNode";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * something : 4B
		 * blank : 4B
		 * nameLength+5B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(18);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		fos.write(writeData());
	}

	public byte[] writeData() {
		/* header : 16B
		 * stuff : 20B
		 * each name + 5B
		 * matrix : 64B
		 */
		//precompute length
		final var length = 110 +specificName.length()+name.length();
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(int1);
		block.putInt(int2);
		block.putInt(int3);
		ZModBlock.putString(block, specificName);
		ZModBlock.putString(block, name);
		block.putInt(int6);
		block.putInt(int7);
		for (int i=0;i<4;i++) for (int j=0;j<4;j++) block.putFloat(matrix[i][j]);
		
		return block.array();
	}
}
