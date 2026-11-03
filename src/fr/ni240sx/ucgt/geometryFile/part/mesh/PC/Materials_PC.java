package fr.ni240sx.ucgt.geometryFile.part.mesh.PC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Materials;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Shader;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;

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
			
			m.IndexData = in.getInt();
			m.ReducedIndexData = in.getInt();
//			in.getInt(); //0 ?				// TEXTURE ORDER OR SOMETHING
//			m.usageSpecific1 = in.getInt(); // TEXTURE ORDER OR SOMETHING - there's room for 10 textures in total
//
//			m.usageSpecific2 = in.getInt();
//			m.usageSpecific3 = in.getInt(); //probably never used

			for (int i=0; i< numtextures; i++) {
				m.TextureParam.add(in.getInt());
			}
			
			in.position(matStart+48);
			for (int i=0; i< numtextures; i++) {
				m.textureIDs.add(in.get());
			}
			in.position(matStart+59);
			m.LightMaterialNumber = in.get();
			m.TextureSortKey = in.getInt();
			
			in.get(m.MeshFlags);
			m.NumIndices = in.getInt();
			m.NumReducedIndices = in.getInt();
			
			in.position(matStart+96);
			in.get();
			m.mMaterialIndex = in.get();
			in.get();
			in.get();
			in.getInt();
			in.getInt();
			m.SizeofVertexData = in.getInt();
			in.get();
			in.get();
			m.VertexBytesAndFlag = in.get();
			in.get();
			//vertex format ? 0x00002080 maybe more like amount of data in the vertex format or smth
			
			in.position(matStart+132);
			m.shader = Shader.get(in.getInt());
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

	public Materials_PC(Materials materials) {
		super(materials);
	}
	

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Utils.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[materialLength*materials.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(materialLength*materials.size() + alignment);
		
		Utils.makeAlignment(out, alignment, (byte) 0x11);
		
		
		for (var m : materials) {
			int matStart = out.position();
			out.putInt(m.IndexData);
			out.putInt(m.ReducedIndexData);
//			out.putInt(0);
//			out.putInt(m.usageSpecific1);		
//			out.putInt(m.usageSpecific2);
//			out.putInt(m.usageSpecific3);

			for (int i=0; i<m.TextureParam.size(); i++) {
				out.putInt(m.TextureParam.get(i));
			}
			Utils.makeAlignment(out, 40 - 4*m.TextureParam.size(), (byte)-1);
			//normally at 48
			
			for (int i=0; i<m.textureIDs.size(); i++) {
				out.put(m.textureIDs.get(i));
			}
			Utils.makeAlignment(out, 10-m.textureIDs.size(), (byte)-1);
			//normally at pos 58
			
			out.put((byte)m.textureIDs.size());
			out.put(m.LightMaterialNumber);
			out.putInt(m.TextureSortKey);
			
			out.put(m.MeshFlags);
			out.putInt(m.NumIndices);
			out.putInt(m.NumReducedIndices);
			
			out.position(matStart+96);
			out.put((byte) 0);
			out.put(m.mMaterialIndex);
			out.put((byte) 0);
			out.put((byte) 0);
			out.putInt(0);
			out.putInt(0);
			out.putInt(m.SizeofVertexData);
			out.put((byte) 0x00);
			out.put((byte) 0x00);
			out.put((byte) m.shader.vertexFormat_PC.getLength());
			out.put((byte) 0x80);
			
			out.position(matStart+132);
			out.putInt(m.shader.getKey());
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