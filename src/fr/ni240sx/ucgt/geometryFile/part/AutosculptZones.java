package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class AutosculptZones extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_AutosculptZones;}

	// texture binhash, usage type
	public ArrayList<Integer> zones = new ArrayList<Integer>();

	public static final int zoneLength = 12;
	
	public AutosculptZones(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while(in.position() < blockStart+blockLength) {
			zones.add(in.getInt());
			in.getInt(); //0
			in.getInt(); //0
		}
	}

	public AutosculptZones() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var out = ByteBuffer.wrap(new byte[zones.size()*zoneLength + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(zones.size()*zoneLength);
		
		for (var z : zones) {
			out.putInt(z);
			out.putInt(0);
			out.putInt(0);
		}
		
		return out.array();	
	}
}