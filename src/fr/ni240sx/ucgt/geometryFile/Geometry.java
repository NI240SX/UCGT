package fr.ni240sx.ucgt.geometryFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.geometryFile.part.AutosculptZones;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.testing.GeomDump;

public class Geometry extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Geometry;}
	
	public GeomHeader geomHeader;
	public ArrayList<Part> parts = new ArrayList<Part>();
	public ArrayList<Material> materials = new ArrayList<Material>();
	public ArrayList<MPoint> mpoints = new ArrayList<MPoint>();
	public ArrayList<Hash> hashlist = new ArrayList<Hash>();
	
	public String carname = "CAR";
	
	public static boolean USE_MULTITHREADING = true;
	

	//---------------------------------------------------------------------------------------------------
	//
	//										I/O : LOAD
	//
	//---------------------------------------------------------------------------------------------------
	
	public static Geometry load(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		byte [] arr = new byte[(int)f.length()];
		fis.read(arr);
		fis.close();
		return new Geometry(ByteBuffer.wrap(arr));
	}
	
	public Geometry(ByteBuffer in) {
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		/*var blockLength =*/ in.getInt();
		in.getInt();
		in.getInt(); // skip common stuff
//		var blockStart = in.position();
		
		// read the header, if this goes wrong the file is probably corrupted or smth
		geomHeader = (GeomHeader) Block.read(in);
		
		// decompress and read parts by offsets
		for (var o : geomHeader.partsOffsets.partOffsets) { //.values()
			byte[] partData = new byte[o.sizeDecompressed];
			ByteBuffer dataWriter = ByteBuffer.wrap(partData);
			
			in.position(o.offset);
			//loops on the one or multiple compressed blocks
			while (in.position() < o.offset + o.sizeCompressed) {
				CompressedData d = (CompressedData) Block.read(in);
				dataWriter.put(d.decompressionOffset, Compression.decompress(d.data));
			}
			
			dataWriter.position(0);
			parts.add(new Part(dataWriter, o.partKey));
			
		}

		try {
			carname = parts.get(0).header.partName.split("_KIT")[0];
		} catch (Exception e) {
			System.out.println("Could not determine car name.");
		}
		
		updateHashes();
		
		
		
		// check global materials, markers, update names and hashes, optimize the file
		
		var toRemove = new ArrayList<Part>(); // parts flagged to be removed to optimize the geometry (eg T0 autosculpt with no other actual morphtargets)
		for (var p : parts) { // iterate on parts, TODO multithread it, use var toRemove = Collections.synchronizedList(new ArrayList<Part>());
//			System.out.println(p.kit+"_"+p.part+"_"+p.lod);

			
			// AUTOSCULPT OPTIMIZATION - has to be moved to the saving method
			
//			ArrayList<Integer> tempZones = new ArrayList<Integer>();
			int numZonesFound = 0;
			Part potentialUselessT0 = null;
			//link autosculpt zones
			for (var p2 : parts) if (p2 != p) {
														//T0-9														T10-99
				if ((p2.part.substring(0, p2.part.length()-1).equals(p.part+"_T") || p2.part.substring(0, p2.part.length()-2).equals(p.part+"_T"))
						&& p2.kit.equals(p.kit) && p2.lod.equals(p.lod)) { // as zone found
//					tempZones.add(new Hash(p2.header.partName).binHash); //TODO assuming the header exists and is up to date
					potentialUselessT0 = p2;
					numZonesFound++;
				}
			}
			
			if (numZonesFound == 1) { //if only one zone detected, check whether it is T0, if yes yeet it, it should have no point existing
//				System.out.println("Warning : only one autosculpt zone on "+p.kit+"_"+p.part+"_"+p.lod);
				toRemove.add(potentialUselessT0);
//				tempZones.clear();
			}
			
			if (p.asZones != null) {
				if (numZonesFound < 2) { //preexisting but no as zones detected
					p.subBlocks.remove(p.asZones);
					p.asZones = null;
//					System.out.println("Removed unused autosculpt zones on "+p.kit+"_"+p.part+"_"+p.lod);
				}
			} else if (numZonesFound > 1) { //nothing preexisting but as zones detected
				p.asZones = new AutosculptZones();
				p.asZones.zones = p.generateASZones();
				p.subBlocks.add(p.asZones);
				System.out.println("Added autosculpt zones on "+p.kit+"_"+p.part+"_"+p.lod);
			}
			
			
			// MESH MATERIALS OPTIMIZATION + GLOBAL MATERIALS (only the global materials part should be kept here)
			
			if (p.mesh != null) for (var m : p.mesh.materials.materials) {	
				m.tryGuessHashes(this, p);

				//destructive tests 
//				m.tryGuessUsageSpecific();
//				m.tryGuessFlags(this);
//				m.tryGuessDefaultTex();
				m.removeUnneeded();
				
				if (!materials.contains(m)) {
					materials.add(m); // this gets random vertex and triangle data, these can be ignored and will be computed if a mesh is imported
				}
			}
			
			// GLOBAL MARKERS STORAGE
			if (p.mpoints != null) for (var mp : p.mpoints.mpoints) {
				mpoints.add(mp);
				mp.part = p.header.partName.replace(carname+"_", "");
				mp.tryGuessName(this, p);
			}
		}
		
		
		//remove parts flagged as useless, TODO move to the saving method

		for (var p : toRemove) {
//			System.out.println("Removing part "+p.header.partName);
			parts.remove(p);
		}


		// sort lists
		
		parts.sort(new PartSorterLodKitName()); //not necessary but makes the file look cleaner
		materials.sort(new MaterialsSorterName());
		mpoints.sort(new MPointSorterName());
		
		for (var m : materials) { //TODO move this list to a new class GlobalMaterials
			var name = m.generateName().replace(carname+"_", "").replace("KIT00_", "");
			if (name.length()>38) name = name.substring(0, 38);
			int duplicate = 0;
			for (var m2 : materials) {
				if (m2.uniqueName.contains(name)) {
					duplicate++;
				}
			}
			if (duplicate != 0) {
				name += "-"+duplicate;
			}
			m.uniqueName = name;
		}
		System.out.println(materials.size() + " global materials");
	}
	
	
	public void updateHashes() {
		hashlist.clear();
		hashlist.add(new Hash(carname));
		
		try {
			for (var p : parts) {
				hashlist.add(new Hash(p.header.partName));
			} 
		} catch (Exception e) {}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data/textures")));
			String tex;
			while ((tex = br.readLine())!=null){
				hashlist.add(new Hash(carname + "_" + tex));
			}
			
			br = new BufferedReader(new FileReader(new File("data/gentextures")));
			while ((tex = br.readLine())!=null){
				hashlist.add(new Hash(tex));
			}

			br = new BufferedReader(new FileReader(new File("data/shaders")));
			while ((tex = br.readLine())!=null){
				hashlist.add(new Hash(tex));
			}

			br = new BufferedReader(new FileReader(new File("data/mpoints")));
			while ((tex = br.readLine())!=null){
				hashlist.add(new Hash(tex));
				for (int as=0; as<11; as++) {
					hashlist.add(new Hash(tex + "_T" + as));
				}
			}
			
//			br = new BufferedReader(new FileReader(new File("data/stuff")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex.strip()));
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	//---------------------------------------------------------------------------------------------------
	//
	//										I/O : SAVE
	//
	//---------------------------------------------------------------------------------------------------
	
	/**
	 * MAIN GEOMETRY SAVING METHOD
	 * Saves the loaded geometry object to a byte array ready to be written to a file
	 */
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		
		// first compresses all parts to get their compressed and decompressed size
		// using RFPK compression but no blocks, hopefully the game recognizes it properly
		System.out.println("Compressing parts...");

		long t = System.currentTimeMillis();
	
		

		
		
		//for debugging
//		var toRemove = new ArrayList<Part>();
//		for (var p : parts) {
//			if (p.header.partName.contains("KITW01") && p.header.partName.contains("DOOR_REAR") && p.header.partName.contains("_B")) {
//				toRemove.add(p);
//				System.out.println(p.header.partName);
//			}
//		}
//		
//		for (var p : toRemove) {
//			parts.remove(p);
//		}

		
		// lists exported parts
//		for (var p : parts) System.out.println(p.kit+"_"+p.part+"_"+p.lod);
		
		
		if (USE_MULTITHREADING) {
			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			System.out.println("Using "+Runtime.getRuntime().availableProcessors()+" CPUs");
			
			for (final var p : parts){
			    pool.execute(new Runnable() {
			        @Override
			        public void run() {
			        	try {
							p.precompress();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			    });
			}
			pool.shutdown();
			// wait for them to finish for up to one minute.
			pool.awaitTermination(10, TimeUnit.MINUTES);
		} else { // useful for debugging
			for (var p : parts) {
				p.precompress();
			}
		}
		

		System.out.println("Parts compressed in "+(System.currentTimeMillis()-t)+" ms.");
		t = System.currentTimeMillis();
		
		geomHeader.refresh(parts);

		System.out.println("Part lists and offsets refreshed in "+(System.currentTimeMillis()-t)+" ms.");
		t = System.currentTimeMillis();

		System.out.println("Preparing file...");
		
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[16]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later
		buf.putInt(0);
		buf.putInt(0);

		out.write(buf.array());

		out.write(geomHeader.save(0)); //temporary without offsets

		for (var p : parts) {
			Padding.makePadding(out);
			geomHeader.partsOffsets.setOffset(p, out.size());
			out.write(new CompressedData(p.compressedData, p.decompressedLength, 0, 0).save(0)); //decompOffset and suppChunkOffset both set to 0 because no chunks
		}
		
		buf = ByteBuffer.wrap(out.toByteArray());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.position(4);
		buf.putInt(out.size()-8); //size

		buf.position(16);
		buf.put(geomHeader.save(0)); //save the header again, this time with correct offsets

		System.out.println("File prepared in "+(System.currentTimeMillis()-t)+" ms.");
		return buf.array();
	}
	
	//---------------------------------------------------------------------------------------------------
	//
	//										I/O : EXPORT
	//
	//---------------------------------------------------------------------------------------------------
	
	/**
	 * Writes a configuration file for this geometry
	 * @param f the file to write the config to
	 * @throws IOException
	 */
	public void writeConfig(File f) throws IOException {
		var bw = new BufferedWriter(new FileWriter(f));
		
		bw.write("=== CONFIGURATION FILE FOR CAR "+carname+" ===\n"
				+ "UCGT by NI240SX\n"
				+ "\n--- Settings ---\n");
		bw.write("SETTING	CompressionLevel="+Part.defaultCompressionLevel.getName()+"\n")	;

		bw.write("\n--- Materials ---\n");
		//MATERIAL	MATNAME	MATSHADER=ShaderUsage[...]	defTex=DEFAULTTEXTURE	flags=0XFLAGS000	DIFFUSE=DIFFUSE_TEX	...
		for (var m : materials) {
			bw.write(m.toConfig()+"\n");
		}
		
		bw.write("\n--- Position markers ---\n");
		for (var mp : mpoints) bw.write(mp.toConfig()+"\n");

		bw.write("\n--- Autosculpt links ---\n"); //only links, autosculpt zones can be automated and renaming them really would be pointless
		for (var p : parts) if (p.asLinking != null) bw.write(p.asLinking.toConfig(this, p)+"\n");
		
		bw.close();
		System.out.println("Configuration file written");
	}
	
	public static void main(String[] args) {
		
		try {
			long t = System.currentTimeMillis();
			
//			Block.doNotRead.put(GeomBlock.Geom_PartsList, true);
			
			File f;
			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN"));
			
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-ctk.BIN"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN"));
//			FileInputStream fis = new FileInputStream(f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY.BIN"));
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			
			System.out.println("File loaded in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var geom = new Geometry(ByteBuffer.wrap(arr));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

			var save = geom.save(0);
			
			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-recompiled.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY-recompiled.BIN"));
			
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-ctk recompiled.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY-recompiled.BIN"));
//			var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY-recompiled.BIN"));
			fos.write(save);
			fos.close();
						
			System.out.println("Total saving time "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}


class PartSorterLodKitName implements Comparator<Part> {
	public int compare(Part a, Part b) {
		return (a.lod + a.kit + a.part).compareTo(b.lod + b.kit + b.part);
	}
}
class MaterialsSorterName implements Comparator<Material>{
	public int compare(Material o1, Material o2) {
		return o1.generateName().compareTo(o2.generateName());
	}
}
class MPointSorterName implements Comparator<MPoint>{
	public int compare(MPoint o1, MPoint o2) {
		return (o1.nameHash.label+o1.part).compareTo((o2.nameHash.label+o2.part));
	}
}