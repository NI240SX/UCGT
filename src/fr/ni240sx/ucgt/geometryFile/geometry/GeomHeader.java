package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.UCGTData;

public class GeomHeader extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Geom_Header;}
	
	public GeomInfo geomInfo = null;
	public PartsList partsList = null;
	public PartsOffsets partsOffsets = null;
	public UCGTData geomData = null;
	
	public GeomHeader(ByteBuffer in) throws Exception {
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
				geomInfo = (GeomInfo) b;
				break;
			case Geom_PartsList:
				partsList = (PartsList) b;
				break;
			case Geom_PartsOffsets:
				partsOffsets = (PartsOffsets) b;
				break;
			case Geom_UNKNOWN:
				break;
			case UCGT_Data:
				geomData = (UCGTData) b;
				break;
			default:
				break;
			}
		}
	}

	public GeomHeader() {
		this.geomInfo = new GeomInfo();
		this.partsList = new PartsList();
		this.partsOffsets = new PartsOffsets();
		this.subBlocks.add(geomInfo);
		this.subBlocks.add(partsList);
		this.subBlocks.add(partsOffsets);
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var b : subBlocks) {
			out.write(b.save(out.size()));
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

	public void refresh(Geometry g) {
		if (g.SAVE_useOffsetsTable) partsOffsets.refresh(g);
		partsList.refresh(g.parts);
		geomInfo.partsCount = g.parts.size();
	}
}
