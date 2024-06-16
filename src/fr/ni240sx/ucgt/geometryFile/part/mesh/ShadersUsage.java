package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class ShadersUsage extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_ShadersUsage;}

	// texture binhash, usage type
	public ArrayList<Integer> shadersUsage = new ArrayList<Integer>();
	
	public ShadersUsage(ByteBuffer in) {
//		var blockLength = 
				in.getInt();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		var numUsages = in.getInt();
		for (int i=0; i< numUsages; i++) {
			shadersUsage.add(in.getInt());
		}
		
//		System.out.print("Shaders usage");
//		for (var s : shadersUsage) {
//			System.out.print(", "+ShaderUsage.get(s).getName());
//		}
//		System.out.println();
		
//		for (var p : texusage) {
//			System.out.println("Texture "+Integer.toHexString(p.getKey())+" has usage "+Usage.get(p.getValue()).getName());
//		}
	}

	public ShadersUsage() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[shadersUsage.size()*4 + 12 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(shadersUsage.size()*4 + 4 + alignment);
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		out.putInt(shadersUsage.size());
		for (var s : shadersUsage) {
			out.putInt(s);
		}
		
		return out.array();	
	}
}