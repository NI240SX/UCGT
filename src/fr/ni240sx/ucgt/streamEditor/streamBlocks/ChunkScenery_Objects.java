package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class ChunkScenery_Objects implements Block {

	public ArrayList<Object> objects = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunkScenery_Objects;}

	public ChunkScenery_Objects(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		while(in.position() < blockStart+blockLength) {
			objects.add(new Object(in));
		}
		in.position(blockStart+blockLength);
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later
		
		out.write(buf.array());
		Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 16), (byte)0x11);

		for (var z : objects) out.write(z.save());
		
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

	
	public static class Object{
		public String name;
		public int[] lod = new int[4];
		public int v1, v2, v3, v4, v6, v7, v8; 
		public float v5;
		
		Object(ByteBuffer in){
			var nameEnd = in.position()+24;
			name = Block.readString(in);
			in.position(nameEnd); //ignore name
			lod[0] = in.getInt(); //A
			lod[1] = in.getInt(); //B
			lod[2] = in.getInt(); //C
			lod[3] = in.getInt(); //Z

			v1 = in.getInt();
			v2 = in.getInt();
			v3 = in.getInt();
			v4 = in.getInt();
			v5 = in.getFloat();
			v6 = in.getInt();
			v7 = in.getInt();
			v8 = in.getInt();
			//0
			//0
			//0
			//0
			//unk float
			//unk int, 0 or 8
			//0
			//0
			//in.position(in.position()+32); //ignore rest
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(72);
			bb.order(ByteOrder.LITTLE_ENDIAN);

			var nameEnd = bb.position()+24;
			Block.putString(bb, name, 24);
			bb.position(nameEnd);

			bb.putInt(lod[0]);
			bb.putInt(lod[1]);
			bb.putInt(lod[2]);
			bb.putInt(lod[3]);

			bb.putInt(v1);
			bb.putInt(v2);
			bb.putInt(v3);
			bb.putInt(v4);
			bb.putFloat(v5);
			bb.putInt(v6);
			bb.putInt(v7);
			bb.putInt(v8);
			return bb.array();
		}
	}
}
