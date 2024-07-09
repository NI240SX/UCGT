package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class TexturePaths extends ZModBlock {

	public ArrayList<String> searchPaths = new ArrayList<>();
	public ArrayList<ZTexture> textures = new ArrayList<>();

	public TexturePaths(int ID) {
		super(ID);
	}
	
	public TexturePaths() {
		//TODO add the possibility for a user-defined global textures folder
		searchPaths.add(".");
		searchPaths.add(".\\textures");
		searchPaths.add(".\\tex");
	}
	
	@Override
	public void readData(ByteBuffer in) {
		var numSearchPaths = in.getInt();
		for (int i=0; i<numSearchPaths; i++) {
			searchPaths.add(ZModBlock.readString(in));
		}
		var numTextures = in.getInt();
		for (int i=0; i<numTextures; i++) {
			textures.add(new ZTexture(in));
		}
	}

	@Override
	public String getName() {
		return "services::CTexturesService";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * some length : 4B
		 * nameLength+5B
		 * blank : 4B
		 */
		var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(547); //version
		block.putInt(length-16);

		block.putInt(33);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
		//immediately write data block
		
		/* header : 16B
		 * num paths : 4B
		 * then per path
		 * stringlength+5B
		 * then num textures : 4B
		 * then per texture
		 * then blank : 4B
		 * 
		 */
		//precompute length
		length = 28; //header + num paths + num tex + blank
		for (var p : searchPaths) length += p.length() + 5;
		for (var t : textures) length += t.computeLength();
		
		block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(547); //version
		block.putInt(length-16);
		
		block.putInt(searchPaths.size());
		for (var p : searchPaths) {
			ZModBlock.putString(block, p);
		}
		block.putInt(textures.size());
		for (var t : textures) {
			t.put(block);
		}
		block.putInt(0);
		
		fos.write(block.array());
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {	
		//DATA BLOCK WRITTEN RIGHT AFTER DECLARATION
	}
	
}
