package fr.ni240sx.ucgt.geometryFile.part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import javafx.geometry.Point3D;
import javafx.util.Pair;

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
			mp.key = in.getInt();
			in.getInt(); //0
			in.getInt(); //0
			in.getInt(); //0

			mp.matrix[0][0] = in.getFloat();
			mp.matrix[0][1] = in.getFloat();
			mp.matrix[0][2] = in.getFloat();
			in.getInt(); //0
			
			mp.matrix[1][0] = in.getFloat();
			mp.matrix[1][1] = in.getFloat();
			mp.matrix[1][2] = in.getFloat();
			in.getInt(); //0

			mp.matrix[2][0] = in.getFloat();
			mp.matrix[2][1] = in.getFloat();
			mp.matrix[2][2] = in.getFloat();
			in.getInt(); //0

			mp.positionX = in.getFloat();
			mp.positionY = in.getFloat();
			mp.positionZ = in.getFloat();
			mp.scale = in.getFloat();
		}
		
//		for (var p : texusage) {
//			System.out.println("Texture "+Integer.toHexString(p.getKey())+" has usage "+Usage.get(p.getValue()).getName());
//		}
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 16); //TODO does this fix it
		var out = ByteBuffer.wrap(new byte[mpoints.size()*80 + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(mpoints.size()*80 + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		for (var mp : mpoints) {
			out.putInt(mp.key);
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
			out.putFloat(mp.scale);
		}
		
		return out.array();	
	}
}