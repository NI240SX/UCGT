package fr.ni240sx.ucgt.collisionsEditor;

import java.nio.ByteBuffer;

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
}
