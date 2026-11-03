package fr.ni240sx.ucgt.geometryFile.part.mesh.X360;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Materials;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Shader;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;

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
			m.SizeofVertexData = in.getInt(); //SizeofVertexData
//			in.getInt(); //vertex format data, 0x00001880 for cars
			in.get(); //Stream
			in.get(); //LoadStreamIndex
			m.VertexBytesAndFlag = in.get(); //VertexBytesAndFlag (short)
			in.get(); //VertexBytesAndFlag
			m.IndexData = in.getInt(); //IndexData
			m.ReducedIndexData = in.getInt(); //ReducedIndexData

			m.TextureSortKey = in.getInt(); //TextureSortKey
			in.get(m.MeshFlags); //MeshFlags
			m.NumIndices = in.getInt(); //NumIndices
			m.NumReducedIndices = in.getInt(); //NumReducedIndices
			
			m.LightMaterialNumber = in.get(); //LightMaterialNumber
			var NumTextures = in.get(); //NumTextures
			in.getShort(); //MeshRemap

			for (int i=0; i< NumTextures; i++) {
				m.TextureParam.add(in.getInt()); //TextureParam
			}
			in.position(matStart+112); //after all texture priorities

			for (int i=0;i<NumTextures;i++) {
				m.textureUsages.add(TextureUsage.get(in.getInt())); //TextureParamBBMaterial
			}
			in.position(matStart+152); //after all texture usages

			for (int i=0;i<NumTextures;i++) {
				m.textures.add(in.getInt()); //TextureNameBBMaterial
			}
			in.position(matStart+192); //after all texture hashes
			
			for (int i=0; i< NumTextures; i++) {
				m.textureIDs.add(in.get()); //TextureNumber
			}
			in.position(matStart+202); //after all texture IDs
			
			m.mMaterialIndex = in.get(); //mMaterialIndex
			//byte NumBoneHashes
			//byte[16] BoneIndexMap
			//UINT BoneHashMap
			//eVertexSkinInfo* SkinInfo
			//eVertexDamageInfo* DamageInfo
			in.position(matStart+232);
			m.shader = Shader.get(in.getInt()); //mMaterialAttribHashName
			//cEffect* bbEffect
			//escope::cScope* mTextureScope
			//int[1] LockedData
			//eSmartPointer<cAssetHandle<eScopeSchemaAsset>> mTextureScopeSchema
			//byte[4] padding
			
			in.position(matStart+materialLength);
		}
		in.position(blockStart+blockLength);
	}

	public Materials_X360() {
	}
	

	public Materials_X360(Materials materials) {
		super(materials);
	}

	@Override
	public BlockType getBlockID() {
		return BlockType.Part_Mesh_Materials_X360;
	}

	@Override
	public byte[] save(int currentPosition) {
		var alignment = Utils.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[materialLength*materials.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(materialLength*materials.size() + alignment);
		
		Utils.makeAlignment(out, alignment, (byte) 0x11);
		
		
		for (var m : materials) {
			int matStart = out.position();
			
			out.position(out.position()+36);

			out.putInt(m.SizeofVertexData);
			out.put((byte) 0x00);
			out.put((byte) 0x00);
			out.put((byte) m.shader.vertexFormat_X360.getLength());
			out.put((byte) 0x80);
			out.putInt(m.IndexData);
			out.putInt(m.ReducedIndexData);

			out.putInt(m.TextureSortKey);
			out.put(m.MeshFlags);
			out.putInt(m.NumIndices);
			out.putInt(m.NumReducedIndices);

			out.put(m.LightMaterialNumber);
			out.put((byte)m.textureIDs.size());
			out.put((byte) 0);
			out.put((byte) 0);
			
			for (int i=0; i<m.TextureParam.size(); i++) {
				out.putInt(m.TextureParam.get(i));
			}
			Utils.makeAlignment(out, 40 - 4*m.TextureParam.size(), (byte)-1);
			
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
			Utils.makeAlignment(out, 10-m.textureIDs.size(), (byte)-1);
//			out.position(matStart+202);

			out.put(m.mMaterialIndex);

			out.position(matStart+232); 
			out.putInt(m.shader.getKey());
			
			out.position(matStart+materialLength);
		}

		return out.array();	
	}

}
