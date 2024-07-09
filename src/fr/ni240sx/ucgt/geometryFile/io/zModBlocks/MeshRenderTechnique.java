package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;

public class MeshRenderTechnique extends ZModBlock {

	public LinkedHashMap<Integer,Integer> techniqueUIDs = new LinkedHashMap<>();
	
	public int settings1 = 0x3B;
	public byte colorR=(byte)0xff, colorG=(byte)0xff, colorB=(byte)0xff, colorA=(byte)0xff;

	public Techniques4Blocks techniques; //only used for saving
	
	public MeshRenderTechnique(int ID) {
		super(ID);
	}

	public MeshRenderTechnique() {
	}

	@Override
	public void readData(ByteBuffer in) {
		int numTechniques =  in.getInt();
		for (int i=0;i<numTechniques;i++) {
			techniqueUIDs.put(in.getInt(), in.getInt()); // technique UID, technique data
		}
		
		settings1 = in.getInt();
		colorR = in.get();
		colorG = in.get();
		colorB = in.get();
		colorA = in.get();
	}

	@Override
	public String getName() {
		return "rend::CStandardRenderIndxTechnique";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * something : 4B
		 * nameLength+5B
		 * blank : 4B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(18);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * numtechniques : 4B
		 * for each tech : 8B
		 * stuff : 8B
		 */
		//precompute length
		final var length = 28 + 8*techniqueUIDs.entrySet().size();
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(33554433); //version
		block.putInt(length-16);

		block.putInt(techniqueUIDs.size());
		for (var t : techniqueUIDs.entrySet()) {
			block.putInt(t.getKey());
			block.putInt(t.getValue());
		}
		block.putInt(settings1);
		block.put(colorR);
		block.put(colorG);
		block.put(colorB);
		block.put(colorA);
		
		fos.write(block.array());
	}

	public void makeTechniques() {
		techniques = new Techniques4Blocks();
		techniqueUIDs.put(techniques.UID, 1);
		techniqueUIDs.put(techniques.UID2, 2);
		techniqueUIDs.put(techniques.UID3, 4);
		techniqueUIDs.put(techniques.UID4, 8);
	}

}
