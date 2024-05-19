package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import javafx.util.Pair;

public class Materials extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_Materials;}
	
	public static final int materialLength = 256;

	public ArrayList<Material> materials = new ArrayList<Material>();
	
	public Materials(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.get() == (byte) 0x11) {} // skip alignment
		in.position(in.position()-1);

		while (in.position() < blockStart+blockLength) {
			Material m = new Material();
			materials.add(m);
			var matStart = in.position();
			
			m.fromVertID = in.getInt();
			m.toVertID = in.getInt();
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
			m.numVertices = in.getInt();
			
			in.position(matStart+96);
			m.materialsListOffset = in.getInt();
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

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[materialLength*materials.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(materialLength*materials.size() + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		//randomize for the lolz
		for (int i=0; i<0; i++) {
			int randmat = (int)(Math.random()*(materials.size()-1));
			int randmat2 = (int)(Math.random()*(materials.size()-1));
	
			var tp0 = materials.get(randmat).flags;
			var tp1 = materials.get(randmat).shaderID;
			var tp2 = materials.get(randmat).shaderUsage;
			var tp3 = materials.get(randmat).textureHash;
			var tp4 = materials.get(randmat).textureIDs;
			var tp5 = materials.get(randmat).textures;
			var tp6 = materials.get(randmat).textureUsages;
			var tp7 = materials.get(randmat).usageSpecific1;
			var tp8 = materials.get(randmat).usageSpecific2;
			var tp9 = materials.get(randmat).usageSpecific3;
	
			materials.get(randmat).flags = materials.get(randmat2).flags;
			materials.get(randmat).shaderID = materials.get(randmat2).shaderID;
			materials.get(randmat).shaderUsage = materials.get(randmat2).shaderUsage;
			materials.get(randmat).textureHash = materials.get(randmat2).textureHash;
			materials.get(randmat).textureIDs = materials.get(randmat2).textureIDs;
			materials.get(randmat).textures= materials.get(randmat2).textures;
			materials.get(randmat).textureUsages= materials.get(randmat2).textureUsages;
			materials.get(randmat).usageSpecific1= materials.get(randmat2).usageSpecific1;
			materials.get(randmat).usageSpecific2 = materials.get(randmat2).usageSpecific2;
			materials.get(randmat).usageSpecific3 = materials.get(randmat2).usageSpecific3;
			
	
			materials.get(randmat2).flags = tp0;
			materials.get(randmat2).shaderID = tp1;
			materials.get(randmat2).shaderUsage = tp2;
			materials.get(randmat2).textureHash = tp3;
			materials.get(randmat2).textureIDs = tp4;
			materials.get(randmat2).textures= tp5;
			materials.get(randmat2).textureUsages= tp6;
			materials.get(randmat2).usageSpecific1= tp7;
			materials.get(randmat2).usageSpecific2 = tp8;
			materials.get(randmat2).usageSpecific3 = tp9;
		}
		
		for (var m : materials) {
			int matStart = out.position();
			out.putInt(m.fromVertID);
			out.putInt(m.toVertID);
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
			
			out.put((byte)m.textures.size());
			out.put(m.shaderID);
			out.putInt(m.textureHash);
			
			out.put(m.flags);
			out.putInt(m.numVertices);
			
			out.position(matStart+96);
			out.putInt(m.materialsListOffset);
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