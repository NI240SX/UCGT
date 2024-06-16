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

public class PartsOffsets extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Geom_PartsOffsets;}
	
	public ArrayList<PartOffset> partOffsets = new ArrayList<PartOffset>();
//	public HashMap<Integer, PartOffset> partOffsets = new HashMap<Integer, PartOffset>(); //this caused the part offsets to be mismatched
	
	public PartsOffsets(ByteBuffer in) {
		var blockLength = in.getInt();
//		var blockStart = in.position();
		
		for(int i=0; i<blockLength/24; i++) {
			var po = new PartOffset(in.getInt(), in.getInt(), in.getInt(), in.getInt());
			partOffsets.add(po);
//			partOffsets.put(po.partKey, po);
			in.getInt(); //512
			in.getInt(); //0
		}
	}

	public PartsOffsets() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {
		var buf = ByteBuffer.wrap(new byte[partOffsets.size()*24 + 8]); 
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(partOffsets.size()*24); //length
		for (var p : partOffsets) { //.values()
			buf.putInt(p.partKey);
			buf.putInt(p.offset);
			buf.putInt(p.sizeCompressed);
			buf.putInt(p.sizeDecompressed);
			buf.putInt(p.unknown);
			buf.putInt(0);
		}
		return buf.array();
	}

	public void refresh(List<Part> parts) {
		partOffsets.clear();
		for (var p : parts) {
			partOffsets.add(new PartOffset(p.header.binKey, 0, p.compressedLength, p.decompressedLength));
//			partOffsets.put(po.partKey, po);
		}
		partOffsets.sort(new PartOffsetsSorter());
	}

	public void setOffset(Part p, int offset) {
//		partOffsets.get(p.partKey).offset = offset;
		
		for (var o : partOffsets) { //.values()
			if (o.partKey == p.header.binKey) {
				o.offset = offset;
				break;
			}
		}
	}
}

class PartOffsetsSorter implements Comparator<PartOffset>{

	@Override
	public int compare(PartOffset o1, PartOffset o2) {
		return Integer.compareUnsigned(o1.partKey, o2.partKey);
	}

}