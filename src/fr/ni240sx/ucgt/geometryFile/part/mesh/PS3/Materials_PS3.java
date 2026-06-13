package fr.ni240sx.ucgt.geometryFile.part.mesh.PS3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Materials;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;
import fr.ni240sx.ucgt.shared.BlockType;

public class Materials_PS3 extends Materials {

	public static final int materialLength = 380; //what the shit
	
	public Materials_PS3(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);
		
//		int curTriID=0;
		
		while (in.position() < blockStart+blockLength) {
			Material m = new Material();
			materials.add(m);
			var matStart = in.position();

			in.position(matStart+24); //skip material bounds
			var numtextures = in.getInt();
			for (int i=0; i< numtextures; i++) {
				m.texturePriorities.add(in.getInt());
			}
			
			in.position(matStart+68);
			for (int i=0; i< numtextures; i++) {
				m.textureIDs.add(in.get());
			}
			in.position(matStart+78);
			m.shaderID = in.get();
			in.get();
			
			var rflags = new byte[4];
			in.get(rflags);
			m.flags[0] = rflags[3];
			m.flags[1] = rflags[2];
			m.flags[2] = rflags[1];
			m.flags[3] = rflags[0];

			in.order(ByteOrder.BIG_ENDIAN);
			m.numVertices = in.getInt();
			in.getInt(); //0
			m.verticesDataLength = in.getInt();
			in.getInt(); //0
			in.getInt(); //5
			in.getInt(); //24 -> vertex format?
			in.getInt(); //other flags ???


			in.position(matStart+232);
			m.numTriIndices = in.getInt(); //ok
			m.fromTriIndex = in.getInt();
//			System.out.println(m.fromTriIndex);
//			System.out.println(curTriID);
//			m.fromTriIndex = curTriID;
			in.getInt(); //0
			in.order(ByteOrder.LITTLE_ENDIAN);
			m.toTriIndex = in.getInt(); // H U H
//			m.toTriIndex = m.fromTriIndex + m.numTriIndices;
//			curTriID = m.toTriIndex;
			
			in.position(matStart+272);
			for (int i=0;i<numtextures;i++) {
				m.textureUsages.add(TextureUsage.get(in.getInt()));
			}

			in.position(matStart+312);
			for (int i=0;i<numtextures;i++) {
				m.textures.add(in.getInt());
			}

			in.position(matStart+372);
			m.shaderUsage = ShaderUsage.get(in.getInt());
			m.shaderUsageID = (byte) in.getInt();
			
			
			in.position(matStart+materialLength);
		}
		in.position(blockStart+blockLength);
		in.order(ByteOrder.LITTLE_ENDIAN);
	}

	public Materials_PS3() {
	}
	

	public Materials_PS3(Materials materials) {
		super(materials);
	}

	@Override
	public BlockType getBlockID() {
		return BlockType.Part_Mesh_Materials_PS3;
	}

	@Override
	public byte[] save(int currentPosition) {
		throw new RuntimeException("PS3 saving not implemented.");
//		var alignment = Block.findAlignment(currentPosition+8, 128);
//		var out = ByteBuffer.wrap(new byte[materialLength*materials.size() + 8 + alignment]);
//		out.order(ByteOrder.LITTLE_ENDIAN);
//
//		out.putInt(getBlockID().getKey());
//		out.putInt(materialLength*materials.size() + alignment);
//		
//		Block.makeAlignment(out, alignment, (byte) 0x11);
//		
//		
//		for (var m : materials) {
//			int matStart = out.position();
//			
//			out.position(out.position()+36);
//
//			out.putInt(m.verticesDataLength);
//			out.put((byte) 0x00);
//			out.put((byte) 0x00);
//			out.put((byte) m.shaderUsage.vertexFormat_X360.getLength());
//			out.put((byte) 0x80);
//			out.putInt(m.fromTriIndex);
//			out.putInt(m.toTriIndex);
//
//			out.putInt(m.textureHash);
//			out.put(m.flags);
//			out.putInt(m.numTriIndices);
//			out.putInt(m.numTriIndicesExtra);
//
//			out.put(m.shaderID);
//			out.put((byte)m.textureIDs.size());
//			out.put((byte) 0);
//			out.put((byte) 0);
//			
//			for (int i=0; i<m.texturePriorities.size(); i++) {
//				out.putInt(m.texturePriorities.get(i));
//			}
//			Block.makeAlignment(out, 40 - 4*m.texturePriorities.size(), (byte)-1);
//			
//			for (var u : m.textureUsages) {
//				out.putInt(u.getKey());
//			}
////			Block.makeAlignment(out, 40 - 4*m.textureUsages.size(), (byte)0);
//			out.position(matStart+152); //after all texture usages
//
//			for (var t : m.textures) {
//				out.putInt(t);
//			}
////			Block.makeAlignment(out, 40 - 4*m.textures.size(), (byte)0);
//			out.position(matStart+192); //after all texture hashes
//			
//			for (int i=0; i<m.textureIDs.size(); i++) {
//				out.put(m.textureIDs.get(i));
//			}
//			Block.makeAlignment(out, 10-m.textureIDs.size(), (byte)-1);
////			out.position(matStart+202);
//
//			out.put(m.shaderUsageID);
//
//			out.position(matStart+232); 
//			out.putInt(m.shaderUsage.getKey());
//			
//			out.position(matStart+materialLength);
//		}
//
//		return out.array();	
	}

}
