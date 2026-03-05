package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public class TPKInfo extends Block {
	
	public int version = 9,unknownHash=0;
	public String filename;
	public String blockname;
	
	static final int DataLength = 124;
	
	@Override
	public BlockType getBlockID() {
		return BlockType.TPK_Info;
	}

	public TPKInfo(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		version = in.getInt();
		blockname = Block.readString(in);
		in.position(blockStart+32);
		filename = Block.readString(in);
		in.position(blockStart+96);
		unknownHash = in.getInt();
		in.position(blockStart+blockLength);		
	}
	
	public TPKInfo(ByteBuffer in, TPK tpk) {
		this(in);
		tpk.version = this.version;
	}


	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var save = ByteBuffer.wrap(new byte[DataLength+8]);
		save.order(ByteOrder.LITTLE_ENDIAN);
		save.putInt(getBlockID().getKey());
		save.putInt(DataLength);
		
		save.putInt(version);
		Block.putString(save, blockname, 28);
		save.position(8+32);
		Block.putString(save, filename, 64);
		save.position(8+96);
		save.putInt(unknownHash);
		
		
		return save.array();
	}
	
	public TPKInfo(int version, String filename, String blockname, int unknownHash) {
		this.version = version;
		this.filename = filename;
		this.blockname = blockname;
		this.unknownHash = unknownHash;
	}

}
