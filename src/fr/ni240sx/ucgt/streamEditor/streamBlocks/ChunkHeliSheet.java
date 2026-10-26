package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;

public class ChunkHeliSheet implements Block {

	public int type1=11, type2=11, id;
	public short sheet[][];
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunkHeliSheet;}

	public ChunkHeliSheet(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		type1 = in.getInt();
		type2 = in.getInt();
		id = in.getInt();
		var count = in.getInt();
//		System.out.println("HELI SHEET id=%d, count=%d".formatted(id, count));
		in.getInt();
		in.getInt();
		sheet = new short[count][9];
		for (int i=0; i<count; i++) for (int j=0; j<9; j++){
			sheet[i][j] = in.getShort();
		}
		if (count %2 != 0) in.getShort();
		
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
		Utils.makeAlignment(out, Utils.findAlignment(currentPosition+out.size(), 16), (byte)0x11);

		buf = ByteBuffer.wrap(new byte[24 + sheet.length*18 + (sheet.length %2 != 0 ? 2 : 0)]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(type1);
		buf.putInt(type2);
		buf.putInt(id);
		buf.putInt(sheet.length);
		buf.putInt(0);
		buf.putInt(0);
		for (int i=0; i<sheet.length; i++) for (int j=0; j<9; j++){
			buf.putShort(sheet[i][j]);
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
