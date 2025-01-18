package fr.ni240sx.ucgt.geometryFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import fr.ni240sx.ucgt.binstuff.Block;

public class UCGTData extends Block {

	public HashMap<String,String> datas = new HashMap<>();
	@Override
	public BlockType getBlockID() {return BlockType.UCGT_Data;}

	public UCGTData(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while (in.position() < blockStart+blockLength) {
			datas.put(Block.readStringAligned(in), Block.readStringAligned(in));
		}
		
	}
	
	public UCGTData() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var d : datas.entrySet()) {
			Block.putString(out, d.getKey());
			Block.putString(out, d.getValue());
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
