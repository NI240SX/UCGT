package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.geometry.UnknownBlock;

public class TPKHeader extends Block {
	
	public TPKInfo info;
	public TexList texlist;
	public TexOffsets offsets;
	
	@Override
	public BlockType getBlockID() {
		return BlockType.TPK_Header;
	}

	public TPKHeader(ByteBuffer in) throws Exception {
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
			case TPK_Info:
				info = (TPKInfo)b;
				break;
			case TPK_TexList:
				texlist = (TexList)b;
				break;
			case TPK_TexOffsets:
				offsets = (TexOffsets)b;
				break;
			default:
				break;
			}
		}
	}
	
	public TPKHeader(ByteBuffer in, TPK tpk) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while(in.position() < blockStart+blockLength) {
			int chunkToInt;
			var type = BlockType.get(chunkToInt = in.getInt());
			switch (type) {
			case TPK_Info:
				subBlocks.add(new TPKInfo(in, tpk));
				break;
			case TPK_TexList:
				subBlocks.add(new TexList(in));
				break;
			case TPK_TexOffsets:
				subBlocks.add(new TexOffsets(in));
				break;
			case TPK_Names:
				subBlocks.add(new TPKNames(in, tpk));
				break;
			case TPK_Formats:
				subBlocks.add(new TPKFormats(in, tpk));
				break;
			default: //animation, etc
				subBlocks.add(new UnknownBlock(in, chunkToInt));

			}
		}
		
		// SUB-BLOCKS PRE-TREATMENT TO REFERENCE THEM ALL
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			case TPK_Info:
				info = (TPKInfo)b;
				break;
			case TPK_TexList:
				texlist = (TexList)b;
				break;
			case TPK_TexOffsets:
				offsets = (TexOffsets)b;
				break;
			default:
				break;
			}
		}
	}


	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		/*
		 * info
		 * list
		 * offsets
		 */
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
	
	public TPKHeader(TPK tpk, String filename, String blockname, int unknownHash) {
		subBlocks.add(info = new TPKInfo(tpk.version, filename, blockname, unknownHash));
		subBlocks.add(texlist = new TexList(tpk.textures));
		if (tpk.SAVE_useOffsetsTable) {
			subBlocks.add(offsets = new TexOffsets(tpk));
		} else {
			subBlocks.add(new TPKNames(tpk));
			subBlocks.add(new TPKFormats(tpk));
		}
	}
	
	public void refresh(TPK t) {
		if (t.SAVE_useOffsetsTable) offsets.refresh(t);
		else {
			offsets = null;
			this.subBlocks.remove(offsets);
		}
		texlist.refresh(t.textures);
	}

}
