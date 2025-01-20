package fr.ni240sx.ucgt.geometryFile.part.mesh.PC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Materials;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;

public class Materials_PC extends Materials {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_Materials_PC;}
	
	public static final int materialLength = 256;

//	public ArrayList<Material> materials = new ArrayList<>();
	
	public Materials_PC(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		while (in.position() < blockStart+blockLength) {
			Material m = new Material();
			materials.add(m);
			var matStart = in.position();

			var numtextures = in.get(matStart+58);
			
			m.fromTriVertID = in.getInt();
			m.toTriVertID = in.getInt();
//			in.getInt(); //0 ?				// TEXTURE ORDER OR SOMETHING
//			m.usageSpecific1 = in.getInt(); // TEXTURE ORDER OR SOMETHING - there's room for 10 textures in total
//
//			m.usageSpecific2 = in.getInt();
//			m.usageSpecific3 = in.getInt(); //probably never used

			for (int i=0; i< numtextures; i++) {
				m.texturePriorities.add(in.getInt());
			}
			
			in.position(matStart+48);
			for (int i=0; i< numtextures; i++) {
				m.textureIDs.add(in.get());
			}
			in.position(matStart+59);
			m.shaderID = in.get();
			m.textureHash = in.getInt();
			
			in.get(m.flags);
			m.numTriVertices = in.getInt();
			m.numTriVerticesExtra = in.getInt();
			
			in.position(matStart+96);
			in.get();
			m.shaderUsageID = in.get();
			in.get();
			in.get();
			in.getInt();
			in.getInt();
			m.verticesDataLength = in.getInt();
			//vertex format ? 0x00002080 maybe more like amount of data in the vertex format or smth
			
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

	public Materials_PC() {
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
//			out.putInt(0);
//			out.putInt(m.usageSpecific1);		
//			out.putInt(m.usageSpecific2);
//			out.putInt(m.usageSpecific3);

			for (int i=0; i<m.texturePriorities.size(); i++) {
				out.putInt(m.texturePriorities.get(i));
			}
			Block.makeAlignment(out, 40 - 4*m.texturePriorities.size(), (byte)-1);
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
			out.putInt(m.numTriVerticesExtra);
			
			out.position(matStart+96);
			out.put((byte) 0);
			out.put(m.shaderUsageID);
			out.put((byte) 0);
			out.put((byte) 0);
			out.putInt(0);
			out.putInt(0);
			out.putInt(m.verticesDataLength);
			out.put((byte) 0x00);
			out.put((byte) 0x00);
			out.put((byte) m.shaderUsage.vertexFormat_PC.getLength());
			out.put((byte) 0x80);
			
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