package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MaterialsList extends ZModBlock {

	// these UIDs are usually immediately defined here
	public ArrayList<Integer> materialUIDs = new ArrayList<>();
	
	public ArrayList<ZMaterial> materials; //used for saving
	
	public MaterialsList(int ID) {
		super(ID);
	}

	public MaterialsList() {
		materials = new ArrayList<>();
	}

	@Override
	public void readData(ByteBuffer in) {
		int numMats = in.getInt();
		for (int i=0; i<numMats; i++) {
			materialUIDs.add(in.getInt());
		}
		in.getInt(); //?=0
	}

	@Override
	public String getName() {
		return "services::CMaterialsService";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * some length : 4B
		 * nameLength+5B
		 * blank : 4B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(33);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * num mats : 4B
		 * then per mat
		 * ID : 4B
		 * then blank : 4B
		 */
		//precompute length
		final var length = 24 + materials.size()*4; //header + blank + num datas
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(545); //version
		block.putInt(length-16);
		
		block.putInt(materials.size());
		for (var m : materials) {
			block.putInt(m.UID);
		}
		block.putInt(0);
		
		fos.write(block.array());
	}

}
