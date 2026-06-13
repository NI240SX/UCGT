package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Hash;

public class AutosculptLinking extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part_AutosculptLinking;}

	// texture binhash, usage type
	public ArrayList<AutosculptLink> links = new ArrayList<>();
	
	public static final int linkLength = 16;

	public String tempPartName; //used only when loading from file
	
	public AutosculptLinking(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while(in.position() < blockStart+blockLength) {
			var l = new AutosculptLink();
			links.add(l);
			l.partKey = in.getInt();
			l.fromZone1 = in.getShort(); //usually the same as zone 4 times, not sure what this means exactly
			l.fromZone2 = in.getShort(); //perhaps one is origin zone and the other is destination zone
			l.toZone3 = in.getShort();
			l.toZone4 = in.getShort();
			in.getInt(); //0
		}
	}

	public AutosculptLinking() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var out = ByteBuffer.wrap(new byte[links.size()*linkLength + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(links.size()*linkLength);
		
		for (var l : links) {
			out.putInt(l.partKey);
			out.putShort(l.fromZone1);
			out.putShort(l.fromZone2);
			out.putShort(l.toZone3);
			out.putShort(l.toZone4);
			out.putInt(0);
		}
		
		return out.array();	
	}

	public String toConfig(Geometry g, Part p) {
		//Hash.guess(partKey, g.hashlist, String.format("0x%08X", partKey), "BIN");
		String s = "ASLINK	"+p.header.partName.replace(g.carname+"_", "");
		for (var l : links) s += "	" + Hash.getBIN(l.partKey).replace(g.carname+"_", "")
				+","+l.fromZone1+","+l.fromZone2+","+l.toZone3+","+l.toZone4;
		return s;
	}
}