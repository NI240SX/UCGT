package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import javafx.util.Pair;

public class TexUsage extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Part_TexUsage;}

	// texture binhash, usage type
	public ArrayList<Pair<Integer,Integer>> texusage = new ArrayList<>();
	
	public TexUsage(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while (in.position() < blockStart+blockLength) {
			var tex = in.getInt();
			var us = in.getInt();
			texusage.add(new Pair<>(tex, us));
			if (us != 0) { //PS compatibility (it has no usage)
				in.getInt(); //0
			}
		}

		in.position(blockStart+blockLength);
//		for (var p : texusage) {
//			System.out.println("Texture "+Integer.toHexString(p.getKey())+" has usage "+Usage.get(p.getValue()).getName());
//		}
	}

	public TexUsage() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		int length = texusage.size()*12;
		if (texusage.size() > 0) if (texusage.get(0).getValue()==0) length = texusage.size()*8; //PS compatibility
		
		var out = ByteBuffer.wrap(new byte[length + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(length);

		for (var p : texusage) {
			out.putInt(p.getKey());
			out.putInt(p.getValue());
			if (p.getValue()!=0) out.putInt(0);
		}
		
		return out.array();	
	}
}
