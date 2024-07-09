package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IUVNodeSwitcher extends ZModBlock {

	public IUVNodeSwitcher(int ID) {
		super(ID);
	}

	public IUVNodeSwitcher() {
	}

	@Override
	public void readData(ByteBuffer in) {
		in.getInt();
		in.getInt();
		in.getInt();
		in.getInt();
	}

	@Override
	public String getName() {
		return "shared::IUVNodeSwitcher";
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

		block.putInt(66);
		block.putInt(0);
		ZModBlock.putString(block, getName());

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * blank : 16B
		 */
		//precompute length
		final var length = 32;

		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(0);
		block.putInt(0);
		block.putInt(0);
		block.putInt(0);
		
		fos.write(block.array());
	}
}
