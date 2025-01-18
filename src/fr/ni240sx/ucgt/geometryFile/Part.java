package fr.ni240sx.ucgt.geometryFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.geometryFile.part.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Mesh_VertsHeader;

public class Part extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part;}
	
	public int decompressedLength;
	public int compressedLength;
	
	public byte[] compressedData;
	
	
	public PartHeader header;
	public TexUsage texusage;
	public Strings strings;
	public Shaders shaderlist;
	public MPoints mpoints;
	public Mesh mesh;
	public AutosculptLinking asLinking;
	public AutosculptZones asZones;
	
	public String kit;
	public String part;
	public String lod;
	public String name = "";
	
	public Part(ByteBuffer in) throws Exception {
		
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block;
		
		while((in.position() < blockStart+blockLength)) {
			if ((block = Block.read(in)) != null) subBlocks.add(block);
		}
		
		// SUB-BLOCKS PRE-TREATMENT TO REFERENCE THEM ALL
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			case Part_Header:
				header = (PartHeader) b;
				break;
			case Part_TexUsage:
				texusage = (TexUsage) b;
				break;
			case Part_Strings:
				strings = (Strings) b;
				break;
			case Part_ShaderList:
				shaderlist = (Shaders) b;
				break;
			case Part_MPoints:
				mpoints = (MPoints) b;
				break;
			case Part_Mesh:
				mesh = (Mesh) b;
				break;
			case Part_HashAssign:
				break;
			case Part_HashList:
				break;
			case Part_AutosculptLinking:
				asLinking = (AutosculptLinking) b;
				break;
			case Part_AutosculptZones:
				asZones = (AutosculptZones) b;
				break;
			default:
				break;
			}
		}
		
//		if (header != null) {
//			if (new Hash(header.partName).binHash != partKey) System.out.println("WARNING : incorrect part name "+header.partName);
//			
//			findKitLodPart("");
//		}
	}
	
	public void findName(String carname) {
		if (header != null) {
			if (carname.isBlank() || carname.equals("UNDETERMINED")) name = header.partName;
			else name = header.partName.replace(carname+"_", "");
			findKitLodPart(carname);
		}
	}


	@SuppressWarnings("unused")
	public void findKitLodPart(String carname) {
		try {
			kit = "KIT" + header.partName.split("_KIT")[1].split("_")[0];
			lod = header.partName.split("_")[header.partName.split("_").length-1];
	//			String s = header.partName.split("_")[header.partName.split("_").length-2];
	//			if (s.length() == 2 && s.charAt(0) == 'T') autosculptZone = Byte.parseByte(s.substring(1));
			part = header.partName.split(kit+"_")[1].substring(0, header.partName.split(kit+"_")[1].length()-2) /*.replace("_T"+autosculptZone, "")*/;
	//			if (autosculptZone == -1) 
	//				System.out.println("Kit : "+kit+", part : "+part+", lod : "+lod);
	//			else System.out.println("Kit : "+kit+", part : "+part+", autosculpt : T"+autosculptZone+", lod : "+lod);
		} catch (Exception e) {
//			System.out.println("Could not read part kit : "+header.partName);
			kit = "";
			try {
				lod = header.partName.split("_")[header.partName.split("_").length-1];
				part = header.partName.replaceFirst(carname+"_","").substring(0, header.partName.replaceFirst(carname+"_","").length()-lod.length()-1);
			} catch (Exception e2) {
//				System.out.println("Could not read part lod : "+header.partName);
				lod = "";
				part = header.partName.replaceFirst(carname+"_","");
			}
		}
	}
	

	public Part(String carname, String substring) {

		if (carname.isBlank() || carname.equals("UNDETERMINED")) this.header = new PartHeader(substring);
		else this.header = new PartHeader(carname+"_"+substring);
		this.texusage = new TexUsage();
		this.strings = new Strings();
		this.shaderlist = new Shaders();
		this.mesh = new Mesh();
		
		findKitLodPart(carname);
		this.name = substring;
	}


	public Part(Part p, String carname, String substring) {
		// no need to be a deep copy (this is used for generated LODs), only the header changes
		this.header = new PartHeader(p.header, carname+"_"+substring);
		this.texusage = p.texusage;
		this.strings = p.strings;
		this.shaderlist = p.shaderlist;
		this.mpoints = p.mpoints;
		this.mesh = p.mesh;
		this.asLinking = p.asLinking;
		this.asZones = p.asZones;

		findKitLodPart(carname);
		this.name = substring;
		
		this.rebuildSubBlocks();
	}

	/**
	 * Creates a part with the specified name and adds it to the Geometry.
	 * This also assigns markers and autosculpt links with the corresponding tempPartName.
	 * @param geom
	 * @param name
	 */
	public Part(Geometry geom, String name) {
		this(geom.carname, name);
		geom.parts.add(this);

		for (var mp : geom.mpointsAll) for (var n : mp.tempPartNames) if (name.contains(n)) { // binding mpoints read from config, if existing
			if (this.mpoints == null) this.mpoints = new MPoints();
			this.mpoints.mpoints.add(mp);
//			mp.part = curPart;
//			mp.parts.add(curPart);
		}
		
		for (var asl : geom.asLinking) if (asl.tempPartName.equals(name)) { // binding autosculpt linking data read from ini
			this.asLinking = asl;
			break;
		}	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		if (shaderlist != null && shaderlist.shaders.size() == 0) subBlocks.remove(shaderlist);
		
		for (var b : subBlocks) {
			if (b != null) out.write(b.save(currentPosition + out.size()));
		}

		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		
		return arr;
	}
	
	public void precompress(Geometry geom) throws IOException, InterruptedException {
		
		var partBytes = this.save(0);
		decompressedLength = partBytes.length;
		
		compressedData  = Compression.compress(partBytes, geom.defaultCompressionType, geom.defaultCompressionLevel);
		compressedLength = compressedData.length + 24;
	}
	
	public void rebuildSubBlocks() {
		subBlocks.clear();
		subBlocks.add(header);
		subBlocks.add(texusage);
		subBlocks.add(strings);
		subBlocks.add(shaderlist);
		if (mpoints != null) subBlocks.add(mpoints);
		subBlocks.add(mesh);
		if (asLinking != null) subBlocks.add(asLinking);
		if (asZones != null) subBlocks.add(asZones);
		
		mesh.subBlocks.clear();
		mesh.subBlocks.add(mesh.info);
		mesh.subBlocks.add(mesh.shadersUsage);
		mesh.subBlocks.add(mesh.materials);
		for (var v : mesh.verticesBlocks) {
			mesh.subBlocks.add(new Mesh_VertsHeader());
			mesh.subBlocks.add(v);
		}
		mesh.subBlocks.add(mesh.triangles);
	}
	
	public void computeBounds() {
		this.header.boundsXmax = Float.NEGATIVE_INFINITY;
		this.header.boundsYmax = Float.NEGATIVE_INFINITY;
		this.header.boundsZmax = Float.NEGATIVE_INFINITY;
		this.header.boundsXmin = Float.POSITIVE_INFINITY;
		this.header.boundsYmin = Float.POSITIVE_INFINITY;
		this.header.boundsZmin = Float.POSITIVE_INFINITY;
		for (var vb : this.mesh.verticesBlocks) for (var v : vb.vertices) {
			if (v.posX > header.boundsXmax) header.boundsXmax = (float)v.posX;
			if (v.posY > header.boundsYmax) header.boundsYmax = (float)v.posY;
			if (v.posZ > header.boundsZmax) header.boundsZmax = (float)v.posZ;
			if (v.posX < header.boundsXmin) header.boundsXmin = (float)v.posX;
			if (v.posY < header.boundsYmin) header.boundsYmin = (float)v.posY;
			if (v.posZ < header.boundsZmin) header.boundsZmin = (float)v.posZ;
		}
		header.boundsXmax += 0.01;
		header.boundsYmax += 0.01;
		header.boundsZmax += 0.01;
		header.boundsXmin -= 0.01;
		header.boundsYmin -= 0.01;
		header.boundsZmin -= 0.01;
	}
	
	public ArrayList<Integer> generateASZones() {
		ArrayList<Integer> zones = new ArrayList<>();
		for (int i=0;i<11;i++) {
			zones.add(Hash.findBIN(header.partName.substring(0,header.partName.length()-1) +"T"+i+"_"+lod));
		}
		return zones;
	}
}