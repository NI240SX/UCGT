package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Mesh_VertsHeader extends Block {
	//apparently always empty, still saves data in case it's not

	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_VertsHeader;}
	
	public Mesh_VertsHeader(ByteBuffer in) {
		var length = in.getInt();
//		var blockStart = in.position();
		data = new byte[length];
		in.get(data); //raw data if there's any
		
		if (length > 0) System.out.println("Found VertsHeader block with length="+length+", this shouldn't happen !");
	}
	
	public Mesh_VertsHeader() {
		data = new byte[0];
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {
		
		var arr = new byte [data.length+8];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(data.length); //length for later
		buf.put(data);
		
		return buf.array();
	}

}
