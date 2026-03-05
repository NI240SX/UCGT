package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public class TPKNames extends Block{
	
	TPK tpk;
	
	@Override
	public BlockType getBlockID() {return BlockType.TPK_Names;}
		
	public TPKNames(ByteBuffer in, TPK tpk) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		this.tpk = tpk;
		while(in.position()<blockStart+blockLength) {
			var t = new NFSTexture();
			tpk.textures.add(t);
			t.readHeaderPart1(in);
		}
		in.position(blockStart+blockLength);
	}
	
	public TPKNames(TPK tpk) {
		this.tpk = tpk;
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var t : tpk.textures) {
			out.write(t.writeHeaderPart1());
		}
		
		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);
		
		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		return arr;	
	}

}