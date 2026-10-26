package fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.Mesh;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Materials;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Shader;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;

public class LegacyMaterials extends Materials {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_LegacyMaterials;}
	
	public static final int materialLength = 128;
	public static final int materialLengthCarbon = 144;
	
	public LegacyMaterials(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);
		
		switch(Mesh.currentPlatform) {
		case Prostreet_PC:
			while (in.position() < blockStart+blockLength) {
				Material m = new Material();
				materials.add(m);
				var matStart = in.position();

				var numtextures = in.get(matStart+33); //only up to 5 textures in PS
				
				m.fromTriIndex = 
						in.getInt(); //not sure its still this
				//m.toTriVertID = 
				m.shader = Shader.get(Platform.indexOf(Mesh.currentPlatform) << 24 | in.getInt());
				
				for (int i=0; i< numtextures; i++) {
					m.texturePriorities.add(in.getInt());
				}
				
				in.position(matStart+28);
				for (int i=0; i< numtextures; i++) {
					m.textureIDs.add(in.get());
					if (((Shader.Legacy)m.shader).texusages.size() > i) m.textureUsages.add(((Shader.Legacy)m.shader).texusages.get(i));
				}
				in.position(matStart+34);
				//m.shaderID = 
						in.get();
				in.get(); //additional padding
				m.textureHash = in.getInt();
				
				in.get(m.flags);
				m.numTriIndices = in.getInt();
				//m.numTriVerticesExtra = 
				m.shaderID = (byte)
						in.getInt(); //no, it's an index of some sort
				
				in.position(matStart+76);
				m.verticesDataLength = in.getInt();
				//vertex format ? 0x00002080 maybe more like amount of data in the vertex format or smth
				
				in.position(matStart+materialLength);
				m.toTriIndex = m.fromTriIndex+m.numTriIndices;
			}
			break;
			
		case Prostreet_X360:
			while (in.position() < blockStart+blockLength) {
				Material m = new Material();
				materials.add(m);
				var matStart = in.position();
				
				in.position(matStart+36);
				m.verticesDataLength = in.getInt();
				// 0x00001880 vertex format check

				in.position(matStart+44);
				m.fromTriIndex = 
						in.getInt(); //not sure its still this
				//m.toTriVertID = 

				m.shader = Shader.get(Platform.indexOf(Mesh.currentPlatform) << 24 | in.getInt());

				m.textureHash = in.getInt();
				in.get(m.flags);
				m.numTriIndices = in.getInt();
				m.shaderID = in.get();

				var numtextures = in.get(); //only up to 5 textures in PS
				in.getShort();
								
				for (int i=0; i< numtextures; i++) {
					m.texturePriorities.add(in.getInt());
				}
				
				in.position(matStart+88);
				for (int i=0; i< numtextures; i++) {
					m.textureIDs.add(in.get());
					if (((Shader.Legacy)m.shader).texusages.size() > i) m.textureUsages.add(((Shader.Legacy)m.shader).texusages.get(i));
				}
								
				in.position(matStart+materialLength);
				m.toTriIndex = m.fromTriIndex+m.numTriIndices;
			}
			break;
			
		case Carbon_PC:
			if ((blockLength - (in.position() - blockStart)) % 116 == 0) { 
				//nfs world bullshit
				while (in.position() < blockStart+blockLength) {
					Material m = new Material();
					materials.add(m);
					var matStart = in.position();

					
					in.get(m.flags);
					m.textureHash = in.getInt();
					m.shader = Shader.get(Platform.indexOf(Mesh.currentPlatform) << 24 | in.getInt());
					in.getInt();
					for (int i=0; i<6; i++) {
						in.getFloat(); //the fuck are floats doing here
					}
					for (int i=0; i<5; i++) {
						m.textureIDs.add(in.get());
					}
					m.shaderID = in.get();
					
					in.position(matStart+64);
					
					for (int i=0; i<5; i++) {
						if (((Shader.Legacy)m.shader).texusages.size() > i) m.textureUsages.add(((Shader.Legacy)m.shader).texusages.get(i));
					}
					
					m.numVertices = in.getInt(); //num vertices maybe
					m.numTriIndices = in.getInt();
					in.getInt(); //numTriangles
					m.fromTriIndex = in.getInt();
					
					in.position(matStart+116);
					m.toTriIndex = m.fromTriIndex+m.numTriIndices;
				}
				
				
			} else
			
			while (in.position() < blockStart+blockLength) {
				Material m = new Material();
				materials.add(m);
				var matStart = in.position();

				for (int i=0; i<6; i++) {
					in.getFloat(); //whatever the fuck, could be material bounds
				}
				for (int i=0; i<5; i++) {
					m.textureIDs.add(in.get());
				}
				m.shaderID = in.get();
				
				in.position(matStart+48);
				
				m.shader = Shader.get(Platform.indexOf(Mesh.currentPlatform) << 24 | in.getInt());

				for (int i=0; i<5; i++) {
					if (((Shader.Legacy)m.shader).texusages.size() > i) m.textureUsages.add(((Shader.Legacy)m.shader).texusages.get(i));
				}
				
				in.getInt();
				in.get(m.flags);
				m.textureHash = in.getInt();
				
				m.numVertices = in.getInt(); //numVertices
				
				in.position(matStart+96);
				in.getInt(); //numTriangles
				m.fromTriIndex = in.getInt();
				
				in.position(matStart+124);
				m.numTriIndices = in.getInt();
				
				in.position(matStart+materialLengthCarbon);
				m.toTriIndex = m.fromTriIndex+m.numTriIndices;
			}
			
			break;

		default:
			System.out.println("LegacyMaterial: Unsupported platform for loading: "+Mesh.currentPlatform.getName());
			break;
		}
		
		in.position(blockStart+blockLength);
	}

	public LegacyMaterials() {
	}

	public LegacyMaterials(Materials materials) {
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
			
			m.flags = new byte[]{(byte) 0x80, 0x41, 0x00, 0x00};
			
			switch(Mesh.currentPlatform) {
			case Prostreet_PC:

				out.putInt(m.fromTriIndex);
				out.putInt(Shader.findLegacyIndex(m.shader.getName(), Mesh.currentPlatform));
				
				for (int i=0; i<m.texturePriorities.size(); i++) {
					out.putInt(m.texturePriorities.get(i));
				}
				Utils.makeAlignment(out, 20 - 4*m.texturePriorities.size(), (byte)-1);
				
				for (int i=0; i<m.textureIDs.size(); i++) {
					out.put(m.textureIDs.get(i));
				}
				Utils.makeAlignment(out, 5-m.textureIDs.size(), (byte)-1);
				//normally at pos 58
				
				out.put((byte)m.textureIDs.size());
				out.put((byte)0);
				out.put((byte)0);
				out.putInt(m.textureHash);
				
				out.put(m.flags);
				out.putInt(m.numTriIndices);
//				out.putInt(m.numTriVerticesExtra);
				out.putInt(m.shaderID);
				
				out.position(matStart+76);
				out.putInt(m.verticesDataLength);
				out.put((byte) 0x00);
				out.put((byte) 0x00);
				out.put((byte) m.shader.vertexFormat_PC.getLength());
				out.put((byte) 0x80);
				break;
				
			case Prostreet_X360:				
				out.position(matStart+36);
				out.putInt(m.verticesDataLength);
				out.put((byte) 0x00);
				out.put((byte) 0x00);
				out.put((byte) m.shader.vertexFormat_PC.getLength());
				out.put((byte) 0x80);

				out.position(matStart+44);
				out.putInt(m.fromTriIndex);
				out.putInt(Shader.findLegacyIndex(m.shader.getName(), Mesh.currentPlatform));

				out.putInt(m.textureHash);
				out.put(m.flags);
				out.putInt(m.numTriIndices);
				out.put(m.shaderID);

				out.put((byte)m.textureIDs.size());
				out.put((byte)0);
				out.put((byte)0);
								
				for (int i=0; i<m.texturePriorities.size(); i++) {
					out.putInt(m.texturePriorities.get(i));
				}
				Utils.makeAlignment(out, 20 - 4*m.texturePriorities.size(), (byte)-1);
				
				out.position(matStart+88);
				for (int i=0; i<m.textureIDs.size(); i++) {
					out.put(m.textureIDs.get(i));
				}
				Utils.makeAlignment(out, 5-m.textureIDs.size(), (byte)-1);
				break;
				
			case Carbon_PC:
				//TODO this is very wrong!
				for (int i=0; i<6; i++) {
					out.putInt(0); //whatever the fuck
				}
				for (int i=0; i<m.textureIDs.size(); i++) {
					out.put(m.textureIDs.get(i));
				}
				Utils.makeAlignment(out, 5-m.textureIDs.size(), (byte)-1);
				out.putInt(m.shaderID);
				
				out.position(matStart+48);

				out.putInt(Shader.findLegacyIndex(m.shader.getName(), Mesh.currentPlatform));
				
				out.putInt(0);
				out.put(m.flags);
				out.putInt(m.textureHash);

				out.putInt(m.numVertices);
				
				out.position(matStart+96);
				out.putInt(0); //numTriangles
				out.putInt(m.fromTriIndex);
				
				out.position(matStart+124);
				out.putInt(m.numTriIndices);
				break;

			default:
				System.out.println("LegacyMaterial: Unsupported platform for saving: "+Mesh.currentPlatform.getName());
				break;
			}

			
			
			out.position(matStart+materialLength);
		}

		return out.array();	
	}
}