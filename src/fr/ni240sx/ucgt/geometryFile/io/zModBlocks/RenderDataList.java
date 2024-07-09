package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;

import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;

public class RenderDataList extends ZModBlock {

	// the indices in this start after the last data block in a file saved with zmod
	public LinkedHashMap<Integer, String> renderData = new LinkedHashMap<>();

	public Techniques4Blocks techniques; //only used for saving
	
	public RenderDataList(int ID) {
		super(ID);
	}
	
	public RenderDataList() {
		techniques = new Techniques4Blocks();
		renderData.put(techniques.UID, "rend::CSemitransparencyExtension");
		renderData.put(techniques.UID2, "rend::CHighBlendingExtension");
		renderData.put(techniques.UID3, "rend::CNoBlendingExtension");
		renderData.put(techniques.UID4, "rend::CNoCullExtension");
	}

	@Override
	public void readData(ByteBuffer in) {
		in.getInt();
		var numRenderData = in.getInt();
		for (int i=0; i<numRenderData; i++) {
			var data = ZModBlock.readString(in);
			renderData.put(in.getInt(), data);
		}
	}

	public void makeLastIDs() {
		renderData.put(ZModelerZ3D.createUID(), "rend::CSpecularRenderExtension");
		renderData.put(ZModelerZ3D.createUID(), "rend::CNoZBufferRenderExtension");
		renderData.put(ZModelerZ3D.createUID(), "rend::CUserDefinedOptionsMaterialExtension");
	}
	
	@Override
	public String getName() {
		return "services::CRenderManager";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * some length : 4B
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
		
		block.putInt(length-20);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * blank : 4B
		 * num datas : 4B
		 * then per data
		 * string1length+5B
		 * ID : 4B
		 */
		//precompute length
		var length = 24; //header + blank + num datas
		for (var i : renderData.entrySet()) {
			length += i.getValue().length()+9; //5B for string length, 4B for UID
		}
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		//header
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);
		
		block.putInt(0);
		block.putInt(renderData.entrySet().size());
		for (var i : renderData.entrySet()) {
			ZModBlock.putString(block, i.getValue());
			block.putInt(i.getKey());
		}
		
		fos.write(block.array());
	}

}
