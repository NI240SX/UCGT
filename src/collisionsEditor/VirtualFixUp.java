package collisionsEditor;

import java.nio.ByteBuffer;

public class VirtualFixUp {

	public int fromOffset;
	public int ClassID;
	
	public VirtualFixUp(ByteBuffer bb) {
		this.fromOffset = bb.getInt();
		this.ClassID = bb.getInt();	
	}

}
