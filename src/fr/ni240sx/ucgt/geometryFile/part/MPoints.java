package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class MPoints extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_MPoints;}

	// texture binhash, usage type
	public ArrayList<MPoint> mpoints = new ArrayList<MPoint>();
	
	public MPoints(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);
		
		while(in.position() < blockStart+blockLength) {
			var mp = new MPoint();
			mpoints.add(mp);
			int key = in.getInt();
			mp.nameHash = new Hash(String.format("0x%08X", key), key);
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
			
			//scaling, fixes an issue with some rotations
			for (var v : mp.matrix[0]) if (Math.abs(v) > mp.scaleX) mp.scaleX = Math.abs(MPoint.round(v));
			for (var v : mp.matrix[1]) if (Math.abs(v) > mp.scaleY) mp.scaleY = Math.abs(MPoint.round(v));
			for (var v : mp.matrix[2]) if (Math.abs(v) > mp.scaleZ) mp.scaleZ = Math.abs(MPoint.round(v));

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
			out.putInt(mp.nameHash.binHash);
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