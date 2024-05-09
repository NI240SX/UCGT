package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Header extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Geom_Header;}
	
	public Info info = null;
	public PartsList partsList = null;
	public PartsOffsets partsOffsets = null;
	
	public Header(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block;
		
		while(in.position() < blockStart+blockLength) {
			if ((block = Block.read(in)) != null) subBlocks.add(block);
		}
		
		// SUB-BLOCKS PRE-TREATMENT TO REFERENCE THEM ALL
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			case Geom_Info:
				info = (Info) b;
				break;
			case Geom_PartsList:
				partsList = (PartsList) b;
				break;
			case Geom_PartsOffsets:
				partsOffsets = (PartsOffsets) b;
				break;
			case Geom_UNKNOWN:
				break;
			default:
				break;
			}
		}
	}

	@Override
	public byte[] save() throws IOException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var b : subBlocks) {
			out.write(b.save());
		}

		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

//		out.write(buf.array(), 4, 4); //write correct size
		
		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		return arr;	
	}

}