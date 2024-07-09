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
		
		for (int i=0; i< blockLength/12; i++) {
			texusage.add(new Pair<>(in.getInt(), in.getInt()));
			in.getInt(); //0
		}
		
//		for (var p : texusage) {
//			System.out.println("Texture "+Integer.toHexString(p.getKey())+" has usage "+Usage.get(p.getValue()).getName());
//		}
	}

	public TexUsage() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var out = ByteBuffer.wrap(new byte[texusage.size()*12 + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(texusage.size()*12);

		for (var p : texusage) {
			out.putInt(p.getKey());
			out.putInt(p.getValue());
			out.putInt(0);
		}
		
		return out.array();	
	}
}
