package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;

public class PartsOffsets extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Geom_PartsOffsets;}
	
	public ArrayList<PartOffset> partOffsets = new ArrayList<>();
//	public HashMap<Integer, PartOffset> partOffsets = new HashMap<Integer, PartOffset>(); //this caused the part offsets to be mismatched
	
	public PartsOffsets(ByteBuffer in) {
		var blockLength = in.getInt();
//		var blockStart = in.position();
		
		for(int i=0; i<blockLength/24; i++) {
			var po = new PartOffset(in.getInt(), in.getInt(), in.getInt(), in.getInt(), in.getInt());
			partOffsets.add(po);
//			partOffsets.put(po.partKey, po);
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
			buf.putInt(p.isCompressed);
			buf.putInt(0);
		}
		return buf.array();
	}

	public void refresh(List<Part> parts) {
		partOffsets.clear();
		for (var p : parts) {
			partOffsets.add(new PartOffset(p.header.binKey, 0, p.compressedLength, p.decompressedLength, 
					Geometry.defaultCompressionType == CompressionType.RawDecompressed ? PartOffset.rawData : PartOffset.compressed));
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
	
	public void setLengths(Part p, int length) { //used for raw data only
		for (var o : partOffsets) { //.values()
			if (o.partKey == p.header.binKey) {
				o.sizeCompressed = length;
				o.sizeDecompressed = length;
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