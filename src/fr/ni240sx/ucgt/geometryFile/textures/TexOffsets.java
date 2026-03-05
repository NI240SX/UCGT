package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.geometry.PartOffset;

public class TexOffsets extends Block{
	
	@Override
	public BlockType getBlockID() {return BlockType.TPK_TexOffsets;}
	
	public ArrayList<PartOffset> offsets = new ArrayList<>();
//	public HashMap<Integer, PartOffset> partOffsets = new HashMap<Integer, PartOffset>(); //this caused the part offsets to be mismatched
	
	public TexOffsets(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		if (blockLength > 32 && in.getInt(in.position()+32) == 0) {
			//nfs world is cursed
			while (in.position() < blockStart+blockLength){				
				var po = new PartOffset(in.getInt(), in.getInt(), in.getInt(), in.getInt(), in.getInt());
				in.getInt();
				in.getInt();
				in.getInt();
				in.getInt();
				offsets.add(po);
			}
			in.position(blockStart+blockLength);
			return;
		}
		
		for(int i=0; i<blockLength/24; i++) {
			var po = new PartOffset(in.getInt(), in.getInt(), in.getInt(), in.getInt(), in.getInt());
			offsets.add(po);
//			partOffsets.put(po.partKey, po);
			in.getInt(); //0
		}
		in.position(blockStart+blockLength);
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {
		var buf = ByteBuffer.wrap(new byte[offsets.size()*24 + 8]); 
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(offsets.size()*24); //length
		for (var p : offsets) { //.values()
			buf.putInt(p.partKey);
			buf.putInt(p.offset);
			buf.putInt(p.sizeCompressed);
			buf.putInt(p.sizeDecompressed);
			buf.putInt(p.isCompressed);
			buf.putInt(0);
		}
		return buf.array();
	}
	
	public void setOffset(NFSTexture t, int offset) {
//		partOffsets.get(p.partKey).offset = offset;
		
		for (var o : offsets) { //.values()
			if (o.partKey == t.binKey) {
				o.offset = offset;
				break;
			}
		}
	}
	
	public void setLengths(NFSTexture t, int length) { //used for raw data only
		for (var o : offsets) { //.values()
			if (o.partKey == t.binKey) {
				o.sizeCompressed = length;
				o.sizeDecompressed = length;
				break;
			}
		}
	}
	
	public TexOffsets(TPK tpk) {
		tpk.textures.forEach(t -> offsets.add(new PartOffset(
				t.binKey, 
				0, 
				0,
				0,
				tpk.defaultCompressionType.equals(CompressionType.RawDecompressed) ? 0 : 512)));
	}

	public void refresh(TPK tpk) {
		offsets.clear();
		for (var t : tpk.textures) {
			offsets.add(new PartOffset(t.binKey, 0, t.compressedLength, t.decompressedLength, tpk.defaultCompressionType.equals(CompressionType.RawDecompressed) ? 0 : 512));
		}
//		offsets.sort(new TexOffsetsSorter());
	}

}

class TexOffsetsSorter implements Comparator<PartOffset>{
	@Override
	public int compare(PartOffset o1, PartOffset o2) {
		return Integer.compareUnsigned(o1.partKey, o2.partKey);
	}
}