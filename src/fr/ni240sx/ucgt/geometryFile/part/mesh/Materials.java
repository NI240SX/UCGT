package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;

public class Materials extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_Materials;}
	
	public static final int materialLength = 256;

	public ArrayList<Material> materials = new ArrayList<>();
	
	public Materials(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		while (in.position() < blockStart+blockLength) {
			Material m = new Material();
			materials.add(m);
			var matStart = in.position();
			
			m.fromTriVertID = in.getInt();
			m.toTriVertID = in.getInt();
			in.getInt(); //0 ?
			m.usageSpecific1 = in.getInt(); 

			m.usageSpecific2 = in.getInt();
			m.usageSpecific3 = in.getInt(); //probably never used

			var numtextures = in.get(matStart+58);
			in.position(matStart+48);
			for (int i=0; i< numtextures; i++) {
				m.textureIDs.add(in.get());
			}
			in.position(matStart+59);
			m.shaderID = in.get();
			m.textureHash = in.getInt();
			
			in.get(m.flags);
			m.numTriVertices = in.getInt();
			
			in.position(matStart+96);
			m.frontendRenderingData = in.getInt();
			in.getInt();
			in.getInt();
			m.verticesDataLength = in.getInt();
			//0x00002080, it's always the same, prolly will never change
			
			in.position(matStart+132);
			m.shaderUsage = ShaderUsage.get(in.getInt());
			in.getInt();
			for (int i=0;i<numtextures;i++) {
				m.textureUsages.add(TextureUsage.get(in.getInt()));
			}
			
			in.position(matStart+180);
			for (int i=0;i<numtextures;i++) {
				m.textures.add(in.getInt());
			}
			in.position(matStart+materialLength);
		}
		in.position(blockStart+blockLength);
	}

	public Materials() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[materialLength*materials.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(materialLength*materials.size() + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		
		for (var m : materials) {
			int matStart = out.position();
			out.putInt(m.fromTriVertID);
			out.putInt(m.toTriVertID);
			out.putInt(0);
			out.putInt(m.usageSpecific1);
			
			out.putInt(m.usageSpecific2);
			out.putInt(m.usageSpecific3);
			
			Block.makeAlignment(out, 24, (byte)-1);
			//normally at 48
			
			for (int i=0; i<m.textureIDs.size(); i++) {
				out.put(m.textureIDs.get(i));
			}
			Block.makeAlignment(out, 10-m.textureIDs.size(), (byte)-1);
			//normally at pos 58
			
			out.put((byte)m.textureIDs.size());
			out.put(m.shaderID);
			out.putInt(m.textureHash);
			
			out.put(m.flags);
			out.putInt(m.numTriVertices);
			
			out.position(matStart+96);
			out.putInt(m.frontendRenderingData);
			out.putInt(0);
			out.putInt(0);
			out.putInt(m.verticesDataLength);
			out.putInt(-2145386496); //0x00002080
			
			out.position(matStart+132);
			out.putInt(m.shaderUsage.getKey());
			out.getInt();
			for (var u : m.textureUsages) {
				out.putInt(u.getKey());
			}
			
			out.position(matStart+180);
			for (var t : m.textures) {
				out.putInt(t);
			}
			out.position(matStart+materialLength);
		}

		return out.array();	
	}
}