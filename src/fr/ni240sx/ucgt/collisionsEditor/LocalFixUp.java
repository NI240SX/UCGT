package fr.ni240sx.ucgt.collisionsEditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LocalFixUp {

	public int fromOffset;
	public int toOffset;
	
	public LocalFixUp(ByteBuffer bb) {
		this.fromOffset = bb.getInt();
		this.toOffset = bb.getInt();
	}

	@Override
	public String toString() {
		return "LocalFixUp [fromOffset=" + fromOffset + ", toOffset=" + toOffset + "]";
	}

	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[8]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(fromOffset);
		bb.putInt(toOffset);
		return bb.array();
	}
}
