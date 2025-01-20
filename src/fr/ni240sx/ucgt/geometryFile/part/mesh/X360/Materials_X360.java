package fr.ni240sx.ucgt.geometryFile.part.mesh.X360;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Materials;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;

public class Materials_X360 extends Materials {

	public static final int materialLength = 256;
	
	public Materials_X360(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		while (in.position() < blockStart+blockLength) {
			Material m = new Material();
			materials.add(m);
			var matStart = in.position();
			
			in.position(matStart+36);
			m.verticesDataLength = in.getInt();
			in.getInt(); //vertex format data, 0x00001880 for cars
			m.fromTriVertID = in.getInt();
			m.toTriVertID = in.getInt();

			m.textureHash = in.getInt();
			in.get(m.flags);
			m.numTriVertices = in.getInt();
			m.numTriVerticesExtra = in.getInt();
			
			m.shaderID = in.get();
			var numtextures = in.get();
			in.get();
			in.get();

			for (int i=0; i< numtextures; i++) {
				m.texturePriorities.add(in.getInt());
			}
			in.position(matStart+112); //after all texture priorities

			for (int i=0;i<numtextures;i++) {
				m.textureUsages.add(TextureUsage.get(in.getInt()));
			}
			in.position(matStart+152); //after all texture usages

			for (int i=0;i<numtextures;i++) {
				m.textures.add(in.getInt());
			}
			in.position(matStart+192); //after all texture hashes
			
			for (int i=0; i< numtextures; i++) {
				m.textureIDs.add(in.get());
			}
			in.position(matStart+202); //after all texture IDs
			
			m.shaderUsageID = in.get(); //shaderUsageID
			
			in.position(matStart+232);
			m.shaderUsage = ShaderUsage.get(in.getInt());
			
			in.position(matStart+materialLength);
		}
		in.position(blockStart+blockLength);
	}

	public Materials_X360() {
	}
	

	@Override
	public BlockType getBlockID() {
		return BlockType.Part_Mesh_Materials_X360;
	}

	@Override
	public byte[] save(int currentPosition) {
		var alignment = Block.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[materialLength*materials.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(materialLength*materials.size() + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		
		for (var m : materials) {
			int matStart = out.position();
			
			out.position(out.position()+36);

			out.putInt(m.verticesDataLength);
			out.put((byte) 0x00);
			out.put((byte) 0x00);
			out.put((byte) m.shaderUsage.vertexFormat_X360.getLength());
			out.put((byte) 0x80);
			out.putInt(m.fromTriVertID);
			out.putInt(m.toTriVertID);

			out.putInt(m.textureHash);
			out.put(m.flags);
			out.putInt(m.numTriVertices);
			out.putInt(m.numTriVerticesExtra);

			out.put(m.shaderID);
			out.put((byte)m.textureIDs.size());
			out.put((byte) 0);
			out.put((byte) 0);
			
			for (int i=0; i<m.texturePriorities.size(); i++) {
				out.putInt(m.texturePriorities.get(i));
			}
			Block.makeAlignment(out, 40 - 4*m.texturePriorities.size(), (byte)-1);
			
			for (var u : m.textureUsages) {
				out.putInt(u.getKey());
			}
//			Block.makeAlignment(out, 40 - 4*m.textureUsages.size(), (byte)0);
			out.position(matStart+152); //after all texture usages

			for (var t : m.textures) {
				out.putInt(t);
			}
//			Block.makeAlignment(out, 40 - 4*m.textures.size(), (byte)0);
			out.position(matStart+192); //after all texture hashes
			
			for (int i=0; i<m.textureIDs.size(); i++) {
				out.put(m.textureIDs.get(i));
			}
			Block.makeAlignment(out, 10-m.textureIDs.size(), (byte)-1);
//			out.position(matStart+202);

			out.put(m.shaderUsageID);

			out.position(matStart+232); 
			out.putInt(m.shaderUsage.getKey());
			
			out.position(matStart+materialLength);
		}

		return out.array();	
	}

}
