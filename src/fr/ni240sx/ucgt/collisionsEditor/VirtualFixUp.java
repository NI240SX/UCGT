package fr.ni240sx.ucgt.collisionsEditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VirtualFixUp {

	public int fromOffset;
	public int ClassID;
	
	public VirtualFixUp(ByteBuffer bb) {
		this.fromOffset = bb.getInt();
		this.ClassID = bb.getInt();	
	}

	@Override
	public String toString() {
		return "VirtualFixUp [fromOffset=" + fromOffset + ", ClassID=" + ClassID + "]";
	}
	
	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[8]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(fromOffset);
		bb.putInt(ClassID);
		return bb.array();
	}

}
