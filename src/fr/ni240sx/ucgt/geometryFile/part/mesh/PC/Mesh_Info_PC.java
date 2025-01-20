package fr.ni240sx.ucgt.geometryFile.part.mesh.PC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Mesh_Info;

public class Mesh_Info_PC extends Mesh_Info {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_Info_PC;}

	public static final int usualLength = 72;
	
	public int const01 = 0;
	public int const02 = 0;
//	public int version = 48;

	//flags, may be linked to how simplified the part is : 0x8041A302 = full part, 80410000 = less detail (brake, exhaust_tip)
//	public byte[] flags = {(byte) 0x80, 0x41, (byte) 0xA3, 0x02};
			
//	public int numMaterials = 0;
	public int const12 = 0; //these constants are most likely always 0
	public int const13 = 0;
	public int const14 = 0;
	
	public int const21 = 0;
	public int const22 = 0;
	public int const23 = 0;
	public int const24 = 0;

//	public int numTriangles = 0;
	public int const32 = 0;
//	public int numTrianglesExtra = 0;
	public int const34 = 0;
	
//	public int numVertices = 0;
	public int const42 = 0;
	
	public Mesh_Info_PC(ByteBuffer in) {
		//var blockLength = 
		in.getInt();
		//var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		const01 = in.getInt();
		const02 = in.getInt();
		version = in.getInt();
		
		in.get(flags);
		
		numMaterials = in.getInt();
		const12 = in.getInt();
		const13 = in.getInt();
		const14 = in.getInt();

		const21 = in.getInt();
		const22 = in.getInt();
		if (version > 47) const23 = in.getInt(); //PS compatibility
		if (version > 47) const24 = in.getInt();

		numTriangles = in.getInt();
		const32 = in.getInt();
		if (version > 47) numTrianglesExtra = in.getInt(); //triangles2
		if (version > 47) const34 = in.getInt();

		numVertices = in.getInt();
		const42 = in.getInt();
	}

	public Mesh_Info_PC() {
		version = 48;
		flags = new byte[]{(byte) 0x80, 0x41, (byte) 0xA3, 0x02};
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 16);
		if (version < 48) alignment -= 16; //PS model compatibility
		var out = ByteBuffer.wrap(new byte[usualLength + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(usualLength + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		out.putInt(const01);
		out.putInt(const02);
		out.putInt(version);
		
		out.put(flags);
		
		out.putInt(numMaterials);
		out.putInt(const12);
		out.putInt(const13);
		out.putInt(const14);

		out.putInt(const21);
		out.putInt(const22);
		if (version > 47) out.putInt(const23);
		if (version > 47) out.putInt(const24);

		out.putInt(numTriangles);
		out.putInt(const32);
		if (version > 47) out.putInt(numTrianglesExtra);
		if (version > 47) out.putInt(const34);

		out.putInt(numVertices);
		out.putInt(const42);

		return out.array();	
	}
}