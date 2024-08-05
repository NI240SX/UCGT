package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;

public class ZMaterial extends ZModBlock {

	public String name;
	
	public float diffuseR=0.8f, diffuseG=0.8f, diffuseB=0.8f, diffuseA=1;
	public float ambientR=0.2f, ambientG=0.2f, ambientB=0.2f, ambientA=1;
	public float specularR=1, specularG=1, specularB=1, specularA=1;
	public float emissiveR=0, emissiveG=0, emissiveB=0, emissiveA=1;
	public float unknownFloat=50f;
	public byte alpha=(byte) 255;
	public boolean bool1=false, useAlpha=false, bool3=false, bool4=false, bool5=false, bool6=false;
	public ArrayList<TexLayer> textures = new ArrayList<>();
	public ArrayList<Integer> renderExtensionUIDs = new ArrayList<>();
	public int parentUID=0;
	public MaterialSubData materialSubData = new MaterialSubData();
	
	public Material binMat; //only used at export
	
	public ZMaterial(int ID) {
		super(ID);
	}
	
	public ZMaterial(String name) {
		super();
		this.name = name;
	}

	public ZMaterial(Material m) {
		super();
		this.binMat = m;
		this.name = m.uniqueName;

		if (m.shaderUsage.equals(ShaderUsage.get("DiffuseAlpha")) || 
				m.shaderUsage.equals(ShaderUsage.get("DiffuseGlowAlpha")) || 
				m.shaderUsage.equals(ShaderUsage.get("DiffuseNormalAlpha")) || 
				m.shaderUsage.equals(ShaderUsage.get("DiffuseNormalSwatchAlpha")) || 
				m.shaderUsage.equals(ShaderUsage.get("TrafficDiffuseAlpha"))) {
			this.useAlpha = true;
		}
//		if (m.textureUsages.contains(TextureUsage.ALPHA)) this.useAlpha = true;
		
	}

	@Override
	public void readData(ByteBuffer in) {
		name = ZModBlock.readString(in);
		
		diffuseR = in.getFloat();
		diffuseG = in.getFloat();
		diffuseB = in.getFloat();
		diffuseA = in.getFloat();

		ambientR = in.getFloat();
		ambientG = in.getFloat();
		ambientB = in.getFloat();
		ambientA = in.getFloat();

		specularR = in.getFloat();
		specularG = in.getFloat();
		specularB = in.getFloat();
		specularA = in.getFloat();

		emissiveR = in.getFloat();
		emissiveG = in.getFloat();
		emissiveB = in.getFloat();
		emissiveA = in.getFloat();
		
		unknownFloat = in.getFloat();
		
		in.getInt(); //5
		in.getInt(); //5
		in.getInt(); //6
		in.getInt(); //6
		in.getInt(); //1
		in.getInt(); //1
		
		in.getInt(); //4 (7 if normalmapped)
		in.getInt(); //128 (4 if normalmapped)
		
		alpha = in.get();

		bool1 = in.get() == 1;
		useAlpha = in.get() == 1;
		bool3 = in.get() == 1;
		bool4 = in.get() == 1;
		bool5 = in.get() == 1;
		bool6 = in.get() == 1;
		
		int numtextures = in.getInt();
		for (int i=0; i<numtextures; i++) {
			textures.add(new TexLayer(in));
		}
		
		int numdata = in.getInt();
		for (int i=0; i<numdata; i++) {
			renderExtensionUIDs.add(in.getInt());
		}
		
		parentUID = in.getInt();

//		var blockType = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
			in.getInt(); //DATA
//		var blockUID = 
			in.getInt(); //0
//		var blockVersion = 
			in.getInt(); //545
		var blockLength = in.getInt();
		var blockStart = in.position();
		materialSubData = new MaterialSubData();
		materialSubData.readData(in);
		in.position(blockStart+blockLength);
		
	}
	
	@Override
	public String getName() {
		return "rend::CMaterial";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * something : 4B
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

		block.putInt(18);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * name : length+5B
		 * diffuse, ambient, specular, emissive RGBA : 64B
		 * some float : 4B
		 * stuff : 32B
		 * alpha : 1B
		 * booleans : 6B
		 * num texture layers : 4B
		 * then per texture layer
		 * 74B
		 * then num render extensions : 4B
		 * then per render extension
		 * UID : 4B
		 * then parent UID : 4B
		 * SUB DATA BLOCK
		 * header : 16B
		 * MaterialSubData length 
		 * num subdatas : 4B
		 * then per subdata (0 for now) : ?
		 */
		//precompute length
		final var length = 16 + name.length() + 124 + 74*textures.size() + 4*renderExtensionUIDs.size() + 20;
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(545); //version
		block.putInt(length-16);

		ZModBlock.putString(block, name);
		block.putFloat(diffuseR);		block.putFloat(diffuseG);		block.putFloat(diffuseB);		block.putFloat(diffuseA);
		block.putFloat(ambientR);		block.putFloat(ambientG);		block.putFloat(ambientB);		block.putFloat(ambientA);
		block.putFloat(specularR);		block.putFloat(specularG);		block.putFloat(specularB);		block.putFloat(specularA);
		block.putFloat(emissiveR);		block.putFloat(emissiveG);		block.putFloat(emissiveB);		block.putFloat(emissiveA);
		block.putFloat(unknownFloat);
		block.putInt(5);		block.putInt(5);
		block.putInt(6);		block.putInt(6);
		block.putInt(1);		block.putInt(1);
		block.putInt(4);		block.putInt(128);
		block.put(alpha);
		block.put((byte) (bool1 ? 1 : 0));
		block.put((byte) (useAlpha ? 1 : 0));
		block.put((byte) (bool3 ? 1 : 0));
		block.put((byte) (bool4 ? 1 : 0));
		block.put((byte) (bool5 ? 1 : 0));
		block.put((byte) (bool6 ? 1 : 0));		
		block.putInt(textures.size());
		for (var t : textures) t.write(block);
		block.putInt(renderExtensionUIDs.size());
		for (var e : renderExtensionUIDs) block.putInt(e);
		block.putInt(parentUID);

		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(0); //UID
		block.putInt(545); //version
		block.putInt(4);
		block.putInt(0);
		
		fos.write(block.array());
	}

	public class TexLayer{
		public boolean enabled=true;
		public int int01=2, int02=0;
		public int texUID;
		public int int11=1, int12=2, int13=1, int14=4;
		public int int21=1, int22=2, int23=1, int24=4;
		public boolean bool30=false;
		public int int31=1, int32=1, int33=1;
		public int int41=2, int42=2, int43=2;
		public byte colorR=(byte)255, colorG=(byte)255, colorB=(byte)255, colorA=(byte)255;
		
		public TexLayer(ByteBuffer in) {
			enabled = in.get() == 1;
			int01 = in.getInt();			int02 = in.getInt();
			texUID = in.getInt();
			int11 = in.getInt();			int12 = in.getInt();			int13 = in.getInt();			int14 = in.getInt();
			int21 = in.getInt();			int22 = in.getInt();			int23 = in.getInt();			int24 = in.getInt();
			bool30 = in.get()==1;
			int31 = in.getInt();			int32 = in.getInt();			int33 = in.getInt();
			int41 = in.getInt();			int42 = in.getInt();			int43 = in.getInt();
			colorR = in.get();			colorG = in.get();			colorB = in.get();			colorA = in.get();
		}

		public void write(ByteBuffer block) {
			block.put((byte) (enabled ? 1 : 0));
			block.putInt(int01);			block.putInt(int02);
			block.putInt(texUID);
			block.putInt(int11);			block.putInt(int12);			block.putInt(int13);			block.putInt(int14);
			block.putInt(int21);			block.putInt(int22);			block.putInt(int23);			block.putInt(int24);
			block.put((byte) (bool30 ? 1 : 0));
			block.putInt(int31);			block.putInt(int32);			block.putInt(int33);
			block.putInt(int41);			block.putInt(int42);			block.putInt(int43);
			block.put(colorR);			block.put(colorG);			block.put(colorB);			block.put(colorA);
		}

		public TexLayer(int UID) {
			texUID = UID;
		}
	}

	public class MaterialSubData {

		public ArrayList<Data> datas = new ArrayList<>();

		public void readData(ByteBuffer in) {
			int numData = in.getInt();
			// Default Material has nothing in this list
			for (int i=0; i<numData; i++) datas.add(new Data(in)); //TODO erroneous data (not read entirely)
		}
		
		public class Data {
			String dataName;
			
			public Data(ByteBuffer in) {
				dataName = ZModBlock.readString(in);
				//idk at this point TODO check later
			}
		}

	}

}

