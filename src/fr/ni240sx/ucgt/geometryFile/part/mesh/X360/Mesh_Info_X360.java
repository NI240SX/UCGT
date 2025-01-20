package fr.ni240sx.ucgt.geometryFile.part.mesh.X360;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Mesh_Info;

public class Mesh_Info_X360 extends Mesh_Info {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_Info_X360;}

	public static final int usualLength = 92; // 72 for PC
	
	public Mesh_Info_X360(ByteBuffer in) {
		//var blockLength = 
		in.getInt();
		//var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		in.getInt();
		in.getInt();
		version = in.getInt();
		
		in.get(flags);
		
		numMaterials = in.getInt();
		
		in.position(in.position()+52);
		
		numTriangles = in.getInt();
		in.getInt();
		in.getInt();
		numVertices = in.getInt();
		in.getInt();
	}

	public Mesh_Info_X360() {
		version = 19;
		flags = new byte[]{(byte) 0x80, 0x41, (byte) 0xA3, 0x02};
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var alignment = Block.findAlignment(currentPosition+8, 16);
		var out = ByteBuffer.wrap(new byte[usualLength + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(usualLength + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		out.putInt(0);
		out.putInt(0);
		out.putInt(version);
		out.put(flags);
		out.putInt(numMaterials);
		out.position(out.position()+52);
		out.putInt(numTriangles);
		out.putInt(0);
		out.putInt(0);
		out.putInt(numVertices);
		out.putInt(0);

		return out.array();	
	}

}
