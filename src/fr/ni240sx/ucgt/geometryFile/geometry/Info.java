package fr.ni240sx.ucgt.geometryFile.geometry;

import java.nio.ByteBuffer;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomChunk;

public class Info extends Block {

	public static final GeomChunk BlockID = GeomChunk.Geom_Info;
	
	public int const01, const02, const03, partsCount;
	public String filename, blockname;
	public int const1, const21, const22, const23, const24;
	
	public Info(ByteBuffer in) {
		var blockLength = in.getInt();
		if (blockLength != 144) System.out.println("[WARN] Unexpected info block length : "+blockLength+" instead of 144.");
		var blockStart = in.position();
		const01 = in.getInt();
		const02 = in.getInt();
		const03 = in.getInt();
		partsCount = in.getInt();
		filename = Block.readString(in);
		in.position(blockStart+72);
		blockname = Block.readString(in);
		in.position(blockStart+blockLength-32);
		const1 = in.getInt();
		in.getInt();
		in.getInt();
		in.getInt();
		const21 = in.getInt();
		const22 = in.getInt();
		const23 = in.getInt();
		const24 = in.getInt();
	}

	@Override
	public byte[] save() {
		// TODO Auto-generated method stub
		return null;
	}

}
