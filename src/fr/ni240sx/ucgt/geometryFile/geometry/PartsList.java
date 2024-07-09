package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import fr.ni240sx.ucgt.geometryFile.Part;

public class PartsList extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Geom_PartsList;}
	
	public ArrayList<Integer> partKeys = new ArrayList<>();
	
	public PartsList(ByteBuffer in) {
		var blockLength = in.getInt();
//		var blockStart = in.position();
		
		for(int i=0; i<blockLength/8; i++) {
			partKeys.add(in.getInt());
			in.getInt(); //0
		}
	}

	public PartsList() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {
		var buf = ByteBuffer.wrap(new byte[partKeys.size()*8 + 8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(partKeys.size()*8); //length
		for (var p : partKeys) {
			buf.putInt(p);
			buf.putInt(0);
		}
		return buf.array();
	}

	public void refresh(List<Part> parts) {
		partKeys.clear();
		for (var p : parts) {
			partKeys.add(p.header.binKey);
		}
		partKeys.sort(new PartKeysSorter());
	}
}

class PartKeysSorter implements Comparator<Integer>{

	@Override
	public int compare(Integer o1, Integer o2) {
		return Integer.compareUnsigned(o1, o2);
	}

}