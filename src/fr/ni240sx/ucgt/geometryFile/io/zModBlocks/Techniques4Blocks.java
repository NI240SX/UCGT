package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;

public class Techniques4Blocks extends ZModBlock { //only used for saving
	
	public int UID2, UID3, UID4;
	private static final String name1 = "rend::CSemitransparencyExtension";
	private static final String name2 = "rend::CHighBlendingExtension";
	private static final String name3 = "rend::CNoBlendingExtension";
	private static final String name4 = "rend::CNoCullExtension";

	public Techniques4Blocks() {
		super();
		this.numDecl = 4;
		UID2 = ZModelerZ3D.createUID();
		UID3 = ZModelerZ3D.createUID();
		UID4 = ZModelerZ3D.createUID();
	}
	
	@Override
	public String getName() {
		return null;
	}

	@Override
	public void readData(ByteBuffer in) {
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		
		//writes all four declarations one after the other
		/* header : 16B
		 * something : 4B
		 * nameLength+5B
		 * blank : 4B
		 */
		var length = 29+name1.length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);
		//SemiTransparency
		block.putInt(18);
		ZModBlock.putString(block, name1);
		block.putInt(0);

		fos.write(block.array());
		
		//HighBlending
		length = 29+name2.length();
		block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID2); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(18);
		ZModBlock.putString(block, name2);
		block.putInt(0);

		fos.write(block.array());
		
		//NoBlending
		length = 29+name3.length();
		block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID3); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(18);
		ZModBlock.putString(block, name3);
		block.putInt(0);

		fos.write(block.array());
		
		//NoCull
		length = 29+name4.length();
		block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID4); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(18);
		ZModBlock.putString(block, name4);
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		// NO DATA FOR THESE BLOCKS
	}

}
