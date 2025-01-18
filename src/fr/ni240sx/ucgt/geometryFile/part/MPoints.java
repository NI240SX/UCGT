package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public class MPoints extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part_MPoints;}

	// texture binhash, usage type
	public ArrayList<MPoint> mpoints = new ArrayList<>();
	
	public MPoints(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);
		
		while(in.position() < blockStart+blockLength) {
			var mp = new MPoint();
			mpoints.add(mp);
			int key = in.getInt();
			mp.nameHash = key;
			in.getInt(); //0
			in.getInt(); //0
			in.getInt(); //0

			mp.matrix[0][0] = MPoint.round(in.getFloat());
			mp.matrix[0][1] = MPoint.round(in.getFloat());
			mp.matrix[0][2] = MPoint.round(in.getFloat());
			in.getInt(); //0
			
			mp.matrix[1][0] = MPoint.round(in.getFloat());
			mp.matrix[1][1] = MPoint.round(in.getFloat());
			mp.matrix[1][2] = MPoint.round(in.getFloat());
			in.getInt(); //0

			mp.matrix[2][0] = MPoint.round(in.getFloat());
			mp.matrix[2][1] = MPoint.round(in.getFloat());
			mp.matrix[2][2] = MPoint.round(in.getFloat());
			in.getInt(); //0

			mp.scaleX = 0;
			mp.scaleY = 0;
			mp.scaleZ = 0;
		
			
			
			mp.scaleX = MPoint.round((float) Math.sqrt(mp.matrix[0][0] * mp.matrix[0][0] + mp.matrix[0][1] * mp.matrix[0][1] + mp.matrix[0][2] * mp.matrix[0][2]));
			mp.scaleY = MPoint.round((float) Math.sqrt(mp.matrix[1][0] * mp.matrix[1][0] + mp.matrix[1][1] * mp.matrix[1][1] + mp.matrix[1][2] * mp.matrix[1][2]));
			mp.scaleZ = MPoint.round((float) Math.sqrt(mp.matrix[2][0] * mp.matrix[2][0] + mp.matrix[2][1] * mp.matrix[2][1] + mp.matrix[2][2] * mp.matrix[2][2]));
	       

			mp.matrix[0][0] = mp.matrix[0][0]/mp.scaleX;
			mp.matrix[0][1] = mp.matrix[0][1]/mp.scaleX;
			mp.matrix[0][2] = mp.matrix[0][2]/mp.scaleX;

			mp.matrix[1][0] = mp.matrix[1][0]/mp.scaleY;
			mp.matrix[1][1] = mp.matrix[1][1]/mp.scaleY;
			mp.matrix[1][2] = mp.matrix[1][2]/mp.scaleY;

			mp.matrix[2][0] = mp.matrix[2][0]/mp.scaleZ;
			mp.matrix[2][1] = mp.matrix[2][1]/mp.scaleZ;
			mp.matrix[2][2] = mp.matrix[2][2]/mp.scaleZ;
			
			mp.positionX = in.getFloat();
			mp.positionY = in.getFloat();
			mp.positionZ = in.getFloat();
			mp.positionW = in.getFloat();
		}
	}

	public MPoints() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 16);
		var out = ByteBuffer.wrap(new byte[mpoints.size()*80 + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(mpoints.size()*80 + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		for (var mp : mpoints) {
			out.putInt(mp.nameHash);
			out.putInt(0);
			out.putInt(0);
			out.putInt(0);

			out.putFloat(mp.matrix[0][0]);
			out.putFloat(mp.matrix[0][1]);
			out.putFloat(mp.matrix[0][2]);
			out.putInt(0);
			
			out.putFloat(mp.matrix[1][0]);
			out.putFloat(mp.matrix[1][1]);
			out.putFloat(mp.matrix[1][2]);
			out.putInt(0);

			out.putFloat(mp.matrix[2][0]);
			out.putFloat(mp.matrix[2][1]);
			out.putFloat(mp.matrix[2][2]);
			out.putInt(0);

			out.putFloat(mp.positionX);
			out.putFloat(mp.positionY);
			out.putFloat(mp.positionZ);
			out.putFloat(mp.positionW);
		}
		
		return out.array();	
	}
}