package fr.ni240sx.ucgt.geometryFile.part.mesh.PS3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.geometryFile.part.mesh.Mesh_Info;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class Mesh_Info_PS3 extends Mesh_Info {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_Info_PS3;}

	public static final int usualLength = 96; // 72 for PC
	
	public Mesh_Info_PS3(ByteBuffer in) {
		var blockLength = 
		in.getInt();
		var blockStart = in.position();
		in.order(ByteOrder.BIG_ENDIAN);

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		in.getInt();
		in.getInt();
		version = in.getInt();

		var rflags = new byte[4];
		in.get(rflags);
		flags[0] = rflags[3];
		flags[1] = rflags[2];
		flags[2] = rflags[1];
		flags[3] = rflags[0];
		
		numMaterials = in.getInt();
		in.position(in.position()+16);
		
		in.getInt(); //unknown count
		in.position(in.position()+16);
		
		numTriangles = in.getInt(); //not quite but uhhhh
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.position(blockStart+blockLength);
	}

	public Mesh_Info_PS3() {
		version = 19;
		flags = new byte[]{(byte) 0x80, 0x41, (byte) 0xA3, 0x02};
	}

	public Mesh_Info_PS3(Mesh_Info info) {
		super(info);
		version = 19;
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var alignment = Block.findAlignment(currentPosition+8, 16);
		var out = ByteBuffer.wrap(new byte[usualLength + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(usualLength + alignment);
		
		out.order(ByteOrder.BIG_ENDIAN);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		out.putInt(0);
		out.putInt(0);
		out.putInt(version);
		out.put(flags[3]);
		out.put(flags[2]);
		out.put(flags[1]);
		out.put(flags[0]);
		out.putInt(numMaterials);
		out.position(out.position()+16);
		out.putInt(0); //TODO
		out.position(out.position()+16);
		out.putInt(0); //TODO

		return out.array();	
	}

}
