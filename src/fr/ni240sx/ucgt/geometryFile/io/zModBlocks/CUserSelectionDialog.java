package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CUserSelectionDialog extends ZModBlock {

	public CUserSelectionDialog(int ID) {
		super(ID);
	}

	public CUserSelectionDialog() {
	}

	@Override
	public void readData(ByteBuffer in) {
		in.getInt();
	}

	@Override
	public String getName() {
		return "core::tools::CUserSelectionDialog";
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

		block.putInt(64);
		block.putInt(0);
		ZModBlock.putString(block, getName());

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * blank : 4B
		 */
		//precompute length
		final var length = 20;

		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(0);
		
		fos.write(block.array());
	}

}
