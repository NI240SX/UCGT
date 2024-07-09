package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Shaders extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Part_ShaderList;}

	// texture binhash, usage type
	public ArrayList<Integer> shaders = new ArrayList<>();
	
	public Shaders(ByteBuffer in) {
		var blockLength = in.getInt();
		
		for (int i=0; i< blockLength/8; i++) {
			shaders.add(in.getInt());
			in.getInt(); //0
		}
		
//		for (var p : texusage) {
//			System.out.println("Texture "+Integer.toHexString(p.getKey())+" has usage "+Usage.get(p.getValue()).getName());
//		}
	}

	public Shaders() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var out = ByteBuffer.wrap(new byte[shaders.size()*8 + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(shaders.size()*8);

		for (var s : shaders) {
			out.putInt(s);
			out.putInt(0);
		}
		
		return out.array();	
	}
}