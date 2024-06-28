package fr.ni240sx.ucgt.geometryFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.part.AutosculptLink;
import fr.ni240sx.ucgt.geometryFile.part.AutosculptLinking;
import fr.ni240sx.ucgt.geometryFile.part.AutosculptZones;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.MPointPositionCube;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangle;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertices;
import fr.ni240sx.ucgt.geometryFile.sorters.MPointPosSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.MPointSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.MaterialsSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.PartSorterLodKitName;
import javafx.util.Pair;
import fr.ni240sx.ucgt.geometryFile.settings.*;

public class Geometry extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Geometry;}
	
	public GeomHeader geomHeader;
	public List<Part> parts = new ArrayList<Part>();//Collections.synchronizedList(new ArrayList<Part>());
	public List<Material> materials = new ArrayList<Material>();//Collections.synchronizedList(new ArrayList<Material>());
	public List<MPoint> mpointsAll = new ArrayList<MPoint>();//Collections.synchronizedList(new ArrayList<MPoint>());
	public List<MPointPositionCube> mpointsPositions = new ArrayList<>();//Collections.synchronizedList(new ArrayList<MPoint>());
	public List<Hash> hashlist = new ArrayList<Hash>();//Collections.synchronizedList(new ArrayList<Hash>());
	public List<AutosculptLinking> asLinking = new ArrayList<AutosculptLinking>();//Collections.synchronizedList(new ArrayList<AutosculptLinking>());
	
	public ArrayList<String> forceAsFixOnParts = new ArrayList<>();
	
	public String carname = "CAR";
	
	public static boolean USE_MULTITHREADING = true;

	public static CompressionType defaultCompressionType = CompressionType.RefPack;
	public static CompressionLevel defaultCompressionLevel = CompressionLevel.Low;
	
	public static boolean LOAD_removeUselessAutosculptParts = true;
	
	public static boolean SAVE_removeUselessAutosculptParts = true;
	public static boolean SAVE_optimizeMaterials = true;
	public static boolean SAVE_sortEverythingByName = true;
	public static boolean SAVE_fixAutosculptNormals = true;
	
	public static boolean IMPORT_importVertexColors = true;
	public static boolean IMPORT_calculateVertexColors = false;
	public static SettingsImport_Tangents IMPORT_Tangents = SettingsImport_Tangents.HIGH;
	public static boolean IMPORT_flipV = false;
	
	//normalmap the engine and make GRILL_02 transparent - use with caution, grills are sometimes inverted on kits from PS
	//not valid in config as you can just change that
	public static boolean EXPORT_vanillaPlusMaterials = false; 
	
	//---------------------------------------------------------------------------------------------------
	//
	//										CONSTRUCTORS
	//
	//---------------------------------------------------------------------------------------------------
	
	public Geometry(ByteBuffer in) {
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		/*var blockLength =*/ in.getInt();
		in.getInt();
		in.getInt(); // skip common stuff
//		var blockStart = in.position();
		
		// read the header, if this goes wrong the file is probably corrupted or smth
		geomHeader = (GeomHeader) Block.read(in);
		
		//load from offsets
//		if (USE_MULTITHREADING) {
//			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//			for (final var o : geomHeader.partsOffsets.partOffsets){
//			    pool.execute(new Runnable() {
//			        @Override
//			        public void run() { //for each offset load the corresponding data into memory
//			        	byte[] partData = new byte[o.sizeDecompressed];
//						ByteBuffer dataWriter = ByteBuffer.wrap(partData);
//						
//						ByteBuffer threadedIn = ByteBuffer.wrap(in.array());
//						threadedIn.order(ByteOrder.LITTLE_ENDIAN);
//						
//						threadedIn.position(o.offset);
//						//loops on the one or multiple compressed blocks
//						while (threadedIn.position() < o.offset + o.sizeCompressed) {
//							CompressedData d = (CompressedData) Block.read(threadedIn);
//							dataWriter.put(d.decompressionOffset, Compression.decompress(d.data));
//						}
//						
//						dataWriter.position(0);
//						parts.add(new Part(dataWriter, o.partKey));
//			        }
//			    });
//			}
//			pool.shutdown();
//			// wait for them to finish for up to one minute.
//			try {
//				pool.awaitTermination(10, TimeUnit.MINUTES);
//			} catch (InterruptedException e) {
//				System.out.println("Critical failure ! Please disable multi-threading.");
//				e.printStackTrace();
//			}
//		} else { // useful for debugging
			for (var o : geomHeader.partsOffsets.partOffsets) { //.values()
				if (o.isCompressed == 512) {
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
				} else {
					in.position(o.offset);
					parts.add(new Part(in, o.partKey));	
				}
			}
//		}

		// determines the compressiontype to put the right one for re-exporting
		// disabled for testing purposes
		if (geomHeader.partsOffsets.partOffsets.get(0).isCompressed == 512) defaultCompressionType = CompressionType.RefPack;
		else if (geomHeader.partsOffsets.partOffsets.get(0).isCompressed == 0) defaultCompressionType = CompressionType.RawDecompressed;
		

		try {
			carname = parts.get(0).header.partName.split("_KIT")[0];
		} catch (Exception e) {
			System.out.println("Could not determine car name.");
		}
		
		updateHashes();
		
		
		
		// check global materials, markers, update names and hashes, optimize the file
		
//		for (var p : parts) { // iterate on parts, TODO multithread it, use var toRemove = Collections.synchronizedList(new ArrayList<Part>());
////			System.out.println(p.kit+"_"+p.part+"_"+p.lod);
//			computeMatsList(p);
//			computeMarkersList(p);
//		}

//		if (USE_MULTITHREADING) {
//			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//			for (final var p : parts){
//			    pool.execute(new Runnable() {
//			        @Override
//			        public void run() {
//			        	computeMatsList(p);
//			        	computeMarkersList(p);
//			        }
//			    });
//			}
//			pool.shutdown();
//			// wait for them to finish for up to one minute.
//			try {
//				pool.awaitTermination(10, TimeUnit.MINUTES);
//			} catch (InterruptedException e) {
//				System.out.println("Critical failure ! Please disable multi-threading.");
//				e.printStackTrace();
//			}
//		} else { // useful for debugging
			materials.clear();
			mpointsAll.clear();
			mpointsPositions.clear();

			var toRemove = new ArrayList<Part>();
			for (var p : parts) {
				computeMatsList(p);
				globalizePartMarkers(p);
				
				optimizeAutosculpt(toRemove, p); 
			}
			computeMarkersList();
//		}
			if (LOAD_removeUselessAutosculptParts) for (var p : toRemove) {
//				System.out.println("Removing part "+p.header.partName);
				parts.remove(p);
			}

			
			
			
			
			
			

		// sort lists
		if (SAVE_sortEverythingByName) {
			parts.sort(new PartSorterLodKitName()); //not necessary but makes the file look cleaner
			materials.sort(new MaterialsSorterName());
			for (var mpc : mpointsPositions) mpc.mpoints.sort(new MPointSorterName());
			mpointsPositions.sort(new MPointPosSorterName());
		}
	}

	public Geometry() {
		this.geomHeader = new GeomHeader();
	}

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

		long t = System.currentTimeMillis();
		var toRemove = Collections.synchronizedList(new ArrayList<Part>()); // parts flagged to be removed to optimize the geometry (eg T0 autosculpt with no other actual morphtargets)
		for (var p : parts) { // iterate on parts, TODO multithread it, use var toRemove = Collections.synchronizedList(new ArrayList<Part>());			
			optimizeAutosculpt(toRemove, p); 
			
			computeMatsList(p); // IMPORTANT TO KEEP HERE
//			globalizePartMarkers(p);
			if (SAVE_optimizeMaterials) if (p.mesh != null) for (var m : p.mesh.materials.materials) {	
    			//destructive tests 
//				m.tryGuessUsageSpecific();
//				m.tryGuessFlags(this);
//    			m.tryGuessDefaultTex();
    			m.removeUnneeded();
    		}
			
			fixAutosculptMeshes(p);
		}

        
		
//		for (var p : parts) {
//			if (!p.header.partName.contains("KIT00_BASE_A")) {
//				toRemove.add(p);
//			}
//		}
		
//		if (USE_MULTITHREADING) {
//			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//			for (final var p : parts){
//			    pool.execute(new Runnable() {
//			        @Override
//			        public void run() {
//			        	optimizeAutosculpt(toRemove, p);
//			        	
//			        	computeMatsList(p);
//			        	computeMarkersList(p);
//			        	if (p.mesh != null) for (var m : p.mesh.materials.materials) {	
//			    			//destructive tests 
////			    				m.tryGuessUsageSpecific();
////			    				m.tryGuessFlags(this);
////			    				m.tryGuessDefaultTex();
//
//			    			m.removeUnneeded();
//			    		}
//			        }
//			    });
//			}
//			pool.shutdown();
//			// wait for them to finish for up to one minute.
//			pool.awaitTermination(10, TimeUnit.MINUTES);
//		} else { // useful for debugging
//			for (var p : parts) {
//				optimizeAutosculpt(toRemove, p);
//				
//				computeMatsList(p);
//				computeMarkersList(p);
//	        	if (p.mesh != null) for (var m : p.mesh.materials.materials) {	
//	    			//destructive tests 
////	    				m.tryGuessUsageSpecific();
////	    				m.tryGuessFlags(this);
////	    				m.tryGuessDefaultTex();
//
//	    			m.removeUnneeded();
//	    		}
//			}
//		}
		
		//remove parts flagged as useless

		if (SAVE_removeUselessAutosculptParts) for (var p : toRemove) {
//			System.out.println("Removing part "+p.header.partName);
			parts.remove(p);
		}
		
		//sort things once again just in case
		if (SAVE_sortEverythingByName) {
			parts.sort(new PartSorterLodKitName());
			materials.sort(new MaterialsSorterName());
			for (var mpc : mpointsPositions) mpc.mpoints.sort(new MPointSorterName());
			mpointsPositions.sort(new MPointPosSorterName());
		}

		System.out.println("Geometry checked and prepared in "+(System.currentTimeMillis()-t)+" ms.");
		t = System.currentTimeMillis();
		
		// first compresses all parts to get their compressed and decompressed size
		// using RFPK compression but no blocks
		System.out.println("Compressing parts...");

		if (defaultCompressionType != CompressionType.RawDecompressed) { //compress parts (eg traffic is not compressed)
			if (USE_MULTITHREADING) {
				ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				System.out.println("Using "+Runtime.getRuntime().availableProcessors()+" CPUs");
				
				for (final var p : parts){
				    pool.execute(() -> {
						try {
							p.precompress();
						} catch (Exception e) {
							e.printStackTrace();
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
		}
			

		System.out.println("Parts compressed in "+(System.currentTimeMillis()-t)+" ms.");
		t = System.currentTimeMillis();
		
		geomHeader.refresh(parts);

//		System.out.println("Part lists and offsets refreshed in "+(System.currentTimeMillis()-t)+" ms.");
//		t = System.currentTimeMillis();

//		System.out.println("Preparing file...");
		
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
			if (Geometry.defaultCompressionType != CompressionType.RawDecompressed)	{
				out.write(new CompressedData(p.compressedData, p.decompressedLength, 0, 0).save(0)); //decompOffset and suppChunkOffset both set to 0 because no chunks
			} else {
				var length = out.size();
				out.write(p.save(0));
				length = out.size()-length;
				geomHeader.partsOffsets.setLengths(p, length);
			}
		}
		
		buf = ByteBuffer.wrap(out.toByteArray());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.position(4);
		buf.putInt(out.size()-8); //size

		buf.position(16);
		buf.put(geomHeader.save(0)); //save the header again, this time with correct offsets

//		System.out.println("File prepared in "+(System.currentTimeMillis()-t)+" ms.");
		return buf.array();
	}

	public void save(File f) throws IOException, InterruptedException {
		long t = System.currentTimeMillis();
		System.out.println("Saving geometry...");
		var save = save(0);
		var fos = new FileOutputStream(f);
		fos.write(save);
		fos.close();		
		System.out.println("Geom saved in "+(System.currentTimeMillis()-t)+" ms.");
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
		bw.write("SETTING	UseMultithreading="+USE_MULTITHREADING+"\n"
				+ "SETTING	CompressionType="+defaultCompressionType+"\n"
				+ "SETTING	CompressionLevel="+defaultCompressionLevel.getName()+"\n"
				+ "SETTING	CarName="+this.carname+"\n"
				+ "SETTING	VertexColors="
				)	;
		if (IMPORT_importVertexColors) bw.write("Import\n");
		else if (IMPORT_calculateVertexColors) bw.write("Calculate\n");
		else bw.write("Off\n");
		bw.write("SETTING	Tangents="+IMPORT_Tangents.getName()+"\n"
				+ "SETTING	RemoveUselessAutosculpt="+SAVE_removeUselessAutosculptParts+"\n"
				+ "SETTING	OptimizeMaterials="+SAVE_optimizeMaterials+"\n"
				+ "SETTING	FixAutosculptNormals="+SAVE_fixAutosculptNormals+"\n");

		bw.write("\n--- Materials ---\n");
		//MATERIAL	MATNAME	MATSHADER=ShaderUsage[...]	defTex=DEFAULTTEXTURE	flags=0XFLAGS000	DIFFUSE=DIFFUSE_TEX	...
		for (var m : materials) {
			bw.write(m.toConfig(carname)+"\n");
		}
		
		bw.write("\n--- Position markers ---\n");
		for (var mpc : mpointsPositions) for (var mp : mpc.mpoints) bw.write(mp.toConfig());

		bw.write("\n--- Autosculpt links ---\n"); //only links, autosculpt zones can be automated and renaming them really would be pointless
		for (var p : parts) if (p.asLinking != null) bw.write(p.asLinking.toConfig(this, p)+"\n");
		
		bw.close();
		System.out.println("Configuration file written");
	}
	
	//---------------------------------------------------------------------------------------------------
	//
	//										I/O : IMPORT
	//
	//---------------------------------------------------------------------------------------------------

	public static Geometry importFromFile(File modelFile) throws IOException, Exception {
		return importFromFile(modelFile, new File(modelFile.getPath().replace(modelFile.getName().split("\\.")[modelFile.getName().split("\\.").length-1], "ini")));
	}
	public static Geometry importFromFile(File modelFile, File configFile) throws IOException, Exception {
		if (!modelFile.getName().endsWith(".obj")) {
			throw new Exception("Wrong file format ! Only Wavefront OBJ is supported.");
		}
		long time = System.currentTimeMillis();
		System.out.println("Loading config from "+configFile.getName());
		Geometry geom = new Geometry();
		geom.readConfig(configFile);
		System.out.println("Config read in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
		System.out.println("Importing meshes from "+modelFile.getName());
		WavefrontOBJ.load(geom, modelFile);
		System.out.println("3D model converted in "+(System.currentTimeMillis()-time)+" ms.");			
		return geom;			
	}
	
	public void readConfig(File f) throws IOException {
		var br = new BufferedReader(new FileReader(f));
		String l;
		while ((l=br.readLine())!=null) {
			switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
			case "SETTING":
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (s2.contains("=")) {
					switch (s2.split("=")[0]) {
					case "UseMultithreading":
						USE_MULTITHREADING = Boolean.parseBoolean(s2.split("=")[1]);
						if (USE_MULTITHREADING) System.out.println("Now using multi-threading");
						else System.out.println("Now using single-threading");
						break;
					case "CompressionType":
						defaultCompressionType = CompressionType.get(s2.split("=")[1]);
						System.out.println("Compression type set : "+defaultCompressionType.getName());
						break;
					case "CompressionLevel":
						defaultCompressionLevel = CompressionLevel.fromName(s2.split("=")[1]);
						System.out.println("Compression level set : "+defaultCompressionLevel.getName());
						break;
					case "CarName":
						carname = s2.split("=")[1];
						System.out.println("Car name set : "+carname);
						break;
						
						//SAVING SETTINGS
					case "RemoveUselessAutosculpt":
						SAVE_removeUselessAutosculptParts = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_removeUselessAutosculptParts) System.out.println("Removing useless T0 Autosculpt parts (lighter file)");
						break;
					case "OptimizeMaterials":
						SAVE_optimizeMaterials = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_optimizeMaterials) System.out.println("Optimizing materials to (slightly) reduce file size");
						break;
						
						//IMPORTING SETTINGS
					case "VertexColors":
						IMPORT_importVertexColors = false;
						IMPORT_calculateVertexColors = false;
						if (s2.split("=")[1].equals("Import")) {
							IMPORT_importVertexColors = true;
							System.out.println("Importing vertex colors");
						}
						else if (s2.split("=")[1].equals("Calculate") || s2.split("=")[1].equals("Generate")) {
							IMPORT_calculateVertexColors = true;
							System.out.println("Generating vertex colors");
						} else System.out.println("Not using vertex colors (lighter file, poor shadows)");
						break;
					case "Tangents":
						IMPORT_Tangents = SettingsImport_Tangents.get(s2.split("=")[1]);
						System.out.println("Tangents behavior : "+IMPORT_Tangents.getName() +" ("+ IMPORT_Tangents.getDesc()+")");
						break;
					case "FlipV":
						IMPORT_flipV = Boolean.parseBoolean(s2.split("=")[1]);
						if (IMPORT_flipV) System.out.println("Flipping V texture coordinates");
						break;
						
					case "ForceAsFix":
						forceAsFixOnParts.add(s2.split("=")[1]);
						System.out.println("Forcing Autosculpt fix : "+s2.split("=")[1]);
						break;
					case "FixAutosculptNormals":
						SAVE_fixAutosculptNormals = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_fixAutosculptNormals) System.out.println("Attempting to fix normals on Autosculpt zero area triangles");
						break;

					default:
						System.out.println("Setting not supported : "+s2);
					}
				}
				break;
				
				
			case "MATERIAL": // IF THE CAR NAME ISN'T SET BEFORE THIS, CAR-SPECIFIC TEXTURES WILL BREAK
				var m = new Material();
				materials.add(m);
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MATERIAL")) {
					if (!s2.contains("=")) { //material name
						m.uniqueName = s2;
					} else { //shader or texture usage
						if (TextureUsage.get(s2.split("=")[1]) != TextureUsage.INVALID) {
							// texture usage
							m.TextureHashes.add(new Hash(s2.split("=")[0].replace("%", carname)));
							m.textureUsages.add(TextureUsage.get(s2.split("=")[1]));
						} else if (ShaderUsage.get(s2.split("=")[1]) != ShaderUsage.INVALID) {
							//shader usage
							m.ShaderHash = new Hash(s2.split("=")[0]);
							m.shaderUsage = ShaderUsage.get(s2.split("=")[1]);
						} else if (s2.split("=")[0].equals("UseTangents")) {
							//material tangents setting
							m.useTangents = Boolean.getBoolean(s2.split("=")[1]);
						}
					}
				}
//				System.out.println("Material : "+m.toConfig(carname));
				break;
				
			case "MARKER":
				var mp = new MPoint();
				int iterator = 0;
				float u=0, v=0, w=0; //euler angles
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MARKER")) {
					switch (iterator) {
					case 0:
						mp.uniqueName = s2;
						break;
					case 1:
						mp.nameHash = new Hash(s2);
						break;
					case 2:
						mp.tempPartName = s2;
						break;
					case 3:
						u = Float.parseFloat(s2);
						break;
					case 4:
						v = Float.parseFloat(s2);
						break;
					case 5:
						w = Float.parseFloat(s2);
						break;
					case 6:
						mp.scaleX = Float.parseFloat(s2);
						break;
					case 7:
						mp.scaleY = Float.parseFloat(s2);
						break;
					case 8:
						mp.scaleZ = Float.parseFloat(s2);
						break;
					}
					iterator++;
				}
				if (iterator ==7) {
					mp.scaleY = mp.scaleX;
					mp.scaleZ = mp.scaleX;
				}
				mp.matrix = mp.eulerAnglesToMatrix(u, v, w);

				if (!mpointsAll.contains(mp)) {
					mpointsAll.add(mp);
					mp.tempPartNames.add(mp.tempPartName);
				} else {
					mpointsAll.get(mpointsAll.indexOf(mp)).tempPartNames.add(mp.tempPartName);
				}
//				if (!mpointsAll.contains(mp)) {
//					mpointsPositions.add(new MPointPositionCube(mp));
//
////					mp.tempPartNames.add(mp.tempPartName);
//				}
//				else {
////					mpoints.get(mpoints.indexOf(mp)).tempPartNames.add(mp.tempPartName);
//					mpointsPositions.get(mpointsPositions.indexOf(mp)).mpoints.add(mp);
////					System.out.println("should be working "+mp.uniqueName+" partname "+mp.tempPartName);
//				}
				
				
//				System.out.println("Marker : "+l);
				break;
			case "ASLINK":
				var asl = new AutosculptLinking();
				asLinking.add(asl);
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("ASLINK")) {
					if (!s2.contains(",")) { //part name
						asl.tempPartName = s2;
					} else {
						asl.links.add(new AutosculptLink(new Hash(carname+"_"+s2.split(",")[0]).binHash, 
								Short.parseShort(s2.split(",")[1]), 
								Short.parseShort(s2.split(",")[2]), 
								Short.parseShort(s2.split(",")[3]), 
								Short.parseShort(s2.split(",")[4])  ));
					}
				}
//				System.out.println("Autosculpt link : "+l);
				break;
			}
		}//loop on config file lines
		br.close();
	}

	public static void ctkConfigToUCGTConfig(File ctkConfig, File config) throws IOException {
		var br = new BufferedReader(new FileReader(ctkConfig));
		
		var bw = new BufferedWriter(new FileWriter(config));
		
		bw.write("=== CONFIGURATION FILE ===\n"
				+ "UCGT by NI240SX\n"
				+ "Converted from a CTK model - please fill in what's missing and check what's already filled in !\n"
				+ "\n--- Settings ---\n");
		bw.write("SETTING	UseMultithreading="+Geometry.USE_MULTITHREADING+"\n"
				+ "SETTING	CompressionType="+Geometry.defaultCompressionType+"\n"
				+ "SETTING	CompressionLevel="+Geometry.defaultCompressionLevel.getName()+"\n"
				+ "SETTING	CarName=MISSING\n"
				+ "SETTING	VertexColors=Calculate\n"
				+ "SETTING	Tangents="+Geometry.IMPORT_Tangents.getName()+"\n"
				+ "SETTING	FlipV=true\n"
				+ "SETTING	RemoveUselessAutosculpt="+Geometry.SAVE_removeUselessAutosculptParts+"\n"
				+ "SETTING	OptimizeMaterials="+Geometry.SAVE_optimizeMaterials+"\n"
				+ "\n--- Converted data from CTK config ---\n" );
		
		String l;
		int iterator;
		while ((l=br.readLine())!=null) {
			switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
				
			case "MATERIAL":
				iterator = 0;
				ShaderUsage usage = ShaderUsage.Diffuse;
				String mat = "MISSING";
				String shader = "UC_PAINT";
				String texture = null;
				String normal = null;

				String glow = null;
				String swatch = null;
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
					switch(iterator) {
					case 2:
						//material name
						mat = s2;
						break;
					case 3:
						//material shader
						shader = s2;
						break;
					case 4:
						//material texture
						texture = s2;
						break;
					case 5:
						//material normalmap
						normal = s2;
						break;
					}
					iterator++;
				}
				
				if (shader.startsWith("UC_")) {
					//hardcoded CTK fake shaders
					switch(shader) {
					case "UC_BADGING_UNIVERSAL":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "DECAL";
						texture = "BADGING_UNIVERSAL";
						break;
					case "UC_BRAKELIGHT":
						usage = ShaderUsage.DiffuseGlow;
						shader = "BRAKELIGHT";
						texture = "%_KIT00_BRAKELIGHT_OFF";
						glow = "%_KIT00_BRAKELIGHT_ON";
						break;
					case "UC_BRAKELIGHTGLASS":
						usage = ShaderUsage.DiffuseGlowAlpha;
						shader = "BRAKELIGHTGLASS";
						texture = "%_KIT00_BRAKELIGHT_GLASS_OFF";
						glow = "%_KIT00_BRAKELIGHT_GLASS_ON";
						break;
					case "UC_BRAKELIGHTGLASSRED":
						usage = ShaderUsage.DiffuseGlowAlpha;
						shader = "BRAKELIGHTGLASSRED";
						texture = "%_KIT00_BRAKELIGHT_GLASS_OFF";
						glow = "%_KIT00_BRAKELIGHT_GLASS_ON";
						break;
					case "UC_DECAL":
						usage = ShaderUsage.DiffuseNormalAlpha;
						shader = "DECAL";
						texture = "%_BADGING";
						normal = "%_BADGING_N";
						break;
					case "UC_DEFROSTER":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "DEFROSTER";
						texture = "REAR_DEFROSTER";
						break;			
					case "UC_HEADLIGHTGLASS":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "HEADLIGHTGLASS";
						texture = "%_KIT00_HEADLIGHT_GLASS_ON";
						break;		
					case "UC_PAINT":
						usage = ShaderUsage.DiffuseNormalSwatch;
						shader = "CARSKIN";
						if (texture != null) {if (texture.equals("METAL_SWATCH")) swatch = "METAL_SWATCH";} else swatch = "%_SKIN1";
						texture = "CARBONFIBRE_PLACEHOLDER";
						normal = "DAMAGE_N";
						break;
					case "UC_WHEEL":
						usage = ShaderUsage.Diffuse;
						shader = "MAGSILVER";
						texture = "%_WHEEL";
						break;
					case "UC_WHEEL_RUBBER":
						usage = ShaderUsage.DiffuseNormalAlpha;
						shader = "RUBBER";
						texture = "TIRE_STYLE01";
						normal = "TIRE_STYLE01_N";
						break;
					case "UC_WINDOW_FRONT":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "WINDSHIELD";
						texture = "WINDOW_FRONT";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_LEFT_FRONT":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "WINDSHIELD";
						texture = "WINDOW_LEFT_FRONT";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_RIGHT_FRONT":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "WINDSHIELD";
						texture = "WINDOW_RIGHT_FRONT";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_REAR":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "WINDSHIELD";
						texture = "WINDOW_REAR";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_LEFT_REAR":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "WINDSHIELD";
						texture = "WINDOW_LEFT_REAR";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_RIGHT_REAR":
						usage = ShaderUsage.DiffuseAlpha;
						shader = "WINDSHIELD";
						texture = "WINDOW_RIGHT_REAR";
						swatch = "%_SKIN1";
						break;
					}
				} else {
					//real shader compiled with Diffuse
					if (normal != null) usage = ShaderUsage.DiffuseNormal;
					if (shader.equals("BRAKEDISC")) usage = ShaderUsage.DiffuseAlpha;
					if (shader.equals("DOORLINE")) usage = ShaderUsage.DiffuseNormalAlpha;
					if (shader.equals("HEADLIGHTREFLECTOR")) {
						usage = ShaderUsage.DiffuseGlow;
						glow = texture;
					}
					if (texture.equals("TIRE_STYLE01")) usage = ShaderUsage.DiffuseNormalAlpha;
					if (texture.equals("%_BADGING")) usage = ShaderUsage.DiffuseNormalAlpha;
					if (normal != null) if (normal.equals("DAMAGE_N")) usage = ShaderUsage.DiffuseNormalSwatch;
					//vanilla plus
					if (texture.equals("GRILL_02")) usage = ShaderUsage.DiffuseAlpha; 
					if (texture.equals("%_ENGINE")) {
						usage = ShaderUsage.DiffuseNormalAlpha;
						normal = "%_ENGINE_N";
					}
				}
				
				bw.write("MATERIAL	"+mat+"	"+shader+"="+usage.toString()+"	"+texture+"=DIFFUSE");
				//normal, glow, swatch
				if (normal != null) bw.write("	"+normal+"=NORMAL");
				if (glow != null) bw.write("	"+glow+"=SELFILLUMINATION");
				if (swatch != null) bw.write("	"+swatch+"=SWATCH");
				bw.write("\n");
				break;
				
			case "MARKER":
				iterator = 0;
				String uS="0";
				String vS="0";
				String wS="0";
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
					switch (iterator) {
					case 2:
						//positioner mesh name
						bw.write("MARKER	"+s2);
						break;
					case 3:
						//marker name
						bw.write("	"+s2);
						break;
					case 4:
						//marker part
						bw.write("	"+s2);
						break;
					case 5:
						//euler angle 1
//						bw.write("	"+s2);
						uS=s2;
						break;
					case 6:
						//euler angle 2
//						bw.write("	"+s2);
						vS=s2;
						break;
					case 7:
						//euler angle 3
//						bw.write("	"+s2);
						wS=s2;
						break;
					}
					iterator++;
				}
				if (vS.equals("90") && (wS.equals("180") || wS.equals("-180"))) {
					double u = Double.parseDouble(uS);
					double v;
					double w;
					//try to fix rotation based on how ucgt reads it
					// 0	90	180 	=>	180		90		0
					// 0.1	90	180 	=>	89.9	89.9	90.1
					// -25	90	180		=>	-90		65		-90
					// 25	90	-180	=>	90		65		90
					if (u == 0) {u=180; v=90; w=0;}
					else {
						w = 90 * u / Math.abs(u);
						v = 90 - u * u / Math.abs(u);
						u = w;
					}
					uS = ((int) u == u) ? Integer.toString((int)u) : Double.toString(u);
//					uS = Double.toString(u);
					vS = ((int) v == v) ? Integer.toString((int)v) : Double.toString(v);
					wS = ((int) w == w) ? Integer.toString((int)w) : Double.toString(w);
				}
				bw.write("	"+uS+"	"+vS+"	"+wS+"\n");
				break;
			}
		}//loop on config file lines
		br.close();
		bw.write("\nPlease indicate necessary Autosculpt links - these cannot be deducted from a CTK config or model. Sample (remove the # to enable) :\n"
				+ "#ASLINK	KITW01_BUMPER_FRONT_A	KITW01_FENDER_FRONT_LEFT_A,1,1,1,1	KITW01_FENDER_FRONT_RIGHT_A,1,1,1,1	KITW01_BUMPER_REAR_A,1,1,1,1	KITW01_SKIRT_LEFT_A,1,1,1,1	KITW01_SKIRT_RIGHT_A,1,1,1,1\r\n");
		bw.close();
	}

	//---------------------------------------------------------------------------------------------------
	//
	//									 HELPER FUNCTIONS
	//
	//---------------------------------------------------------------------------------------------------
	
	@SuppressWarnings("unlikely-arg-type")
	public void globalizePartMarkers(Part p) {
		// GLOBAL MARKERS STORAGE
		if (p.mpoints != null) for (var mp : p.mpoints.mpoints) {
			mp.part = p;
			mp.tryGuessName(this, p);
			if (!this.mpointsPositions.contains(mp)) { //this was problematic, not anymore
				this.mpointsPositions.add(new MPointPositionCube(mp));
			}else {
				// if there's already the exact same marker, affect it to more parts
//				mpoints.get(mpoints.indexOf(mp)).parts.add(p);
				mpointsPositions.get(mpointsPositions.indexOf(mp)).mpoints.add(mp);
			}
			mpointsAll.add(mp);
		}
	}
	
	public void computeMarkersList() {
		for (var mpc : mpointsPositions) {
			String name = "_"+ mpc.mpoints.get(0).nameHash.label + "_" + mpc.mpoints.get(0).part.kit;
			int duplicate = 0;
			for (var m2 : mpointsPositions) if (m2 != mpc) {
				if (m2.mpoints.get(0).uniqueName.contains(name)) {
					duplicate++;
				}
			}
			if (duplicate != 0) {
				name += "-"+duplicate;
			}
			for (var mp : mpc.mpoints) mp.uniqueName = name;
		}
	}

	private void computeMatsList(Part p) {
		HashMap<ShaderUsage, Integer> FERenderData;
		// MESH MATERIALS OPTIMIZATION + GLOBAL MATERIALS (only the global materials part should be kept here)
		
		FERenderData = new HashMap<ShaderUsage, Integer>();
		if (p.mesh != null) for (var m : p.mesh.materials.materials) {	
			m.tryGuessHashes(this, p);

			//destructive tests 
//				m.tryGuessUsageSpecific();
//				m.tryGuessFlags(this);
//				m.tryGuessDefaultTex();

			m.tryGuessFEData(FERenderData);
			
			if (!materials.contains(m)) { 
				// if the global materials list doesn't contain a material matching this one, add it
				// matching here means it has the same texture hashes, shader usage, etc but not the same mesh data
				// this gets random vertex and triangle data, these are ignored for that list and will be computed if a mesh is imported
				materials.add(m); 
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

			} else {
				// if the material already exists, we'll need the name for model exporting
				m.uniqueName = materials.get(materials.indexOf(m)).uniqueName;
			}
		}
	}

	public void optimizeAutosculpt(List<Part> toRemove, Part p) {
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
		
		if (numZonesFound == 1 && toRemove != null) { //if only one zone detected, check whether it is T0, if yes yeet it, it should have no point existing
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
//			System.out.println("Added autosculpt zones on "+p.kit+"_"+p.part+"_"+p.lod);
		}
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
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fixAutosculptMeshes(Part p) {
		if (p.asZones != null) {
			List<Part> asParts = new ArrayList<>();
			asParts.add(0, p);
			for (var p2 : parts) if (p.asZones.zones.contains(p2.header.binKey)) asParts.add(p2);
			
			boolean invalidateVertsGlobal = false;
			
			ArrayList<Pair<Vertex,Vertex>> swapNormals = new ArrayList<>();
			
			//loop on each material for all autosculpt parts
			for (int mat=0; mat<p.mesh.materials.materials.size(); mat++) {
				boolean invalidateVertsMat = false;
				if (forceAsFixOnParts.contains(p.kit+"_"+p.part+"_"+p.lod)) {
					invalidateVertsGlobal = true;
	    			invalidateVertsMat = true;
				} else {
					for (var p2 : asParts) {
			    		if (p2.mesh.materials.materials.get(mat).triangles.size() != p.mesh.materials.materials.get(mat).triangles.size()) {
			    			System.out.println("[Save] Warning : Different triangle counts across autosculpt zones on part "+p.header.partName+", will not be fixed !");
			    		}
			    		if (p2.mesh.materials.materials.get(mat).verticesBlock.vertices.size() != p.mesh.materials.materials.get(mat).verticesBlock.vertices.size()) {
	//		        			System.out.println("Different vertices counts across autosculpt zones on part "+p.header.partName);
			    			invalidateVertsGlobal = true;
			    			invalidateVertsMat = true;
			    		}
					}
				}
				if (invalidateVertsMat) {
					//rebuild a temporary triangle list for the affected material of each part, directly referencing vertices
					ArrayList<ArrayList<Vertex[]>> partsTris = new ArrayList<>();

					for (var p2 : asParts) {
						ArrayList<Vertex[]> tris = new ArrayList<>();
						partsTris.add(tris);
						for (var t : p2.mesh.materials.materials.get(mat).triangles) {
							tris.add(new Vertex[] {p2.mesh.materials.materials.get(mat).verticesBlock.vertices.get(t.vert0),
									p2.mesh.materials.materials.get(mat).verticesBlock.vertices.get(t.vert1),
									p2.mesh.materials.materials.get(mat).verticesBlock.vertices.get(t.vert2)});
						}
						p2.mesh.materials.materials.get(mat).triangles.clear();
						p2.mesh.materials.materials.get(mat).verticesBlock.vertices.clear();
					}
					
					// UNTIL THAT POINT EVERYTHING IS WELDED

					//now make up vertices lists back from the triangles checking that all parts find an already existing vert or not
					//assuming all parts have the same amount of triangles and the same material sorting and triangles offsets
					for (int i=0; i<partsTris.get(0).size(); i++) {
						//loop on triangles
						for (int v=0; v<3; v++) {
							//loop on all 3 vertices of each triangle
							
							boolean alreadyExisting = true;
							for (int p2=0; p2<asParts.size(); p2++) {
								if (!asParts.get(p2).mesh.materials.materials.get(mat) .verticesBlock.vertices.contains(partsTris.get(p2).get(i)[v])) {
									//if one of the parts doesn't have the corresponding vertex in its vertices
									alreadyExisting = false;
									break;
								}
							}
							
							if (!alreadyExisting) {
								// add a vertex to all parts if it's missing in at least one
								for (int p2=0; p2<asParts.size(); p2++) {
									asParts.get(p2).mesh.materials.materials.get(mat) .verticesBlock.vertices.add(partsTris.get(p2).get(i)[v]);
								}
							}
						}

						boolean move0 = false;
						boolean move1 = false;
						boolean move2 = false;
						
						for (int p2=0; p2<asParts.size(); p2++) {
							//temporarily build the triangle
							asParts.get(p2).mesh.materials.materials.get(mat).triangles.add( new Triangle(
								//INDICES SHOULD BE MOSTLY THE SAME FOR ALL PARTS HERE
								(short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[0]),
								(short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1]),
								(short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2])
							) );
							
							if (
									//using positionEquals because the equals takes normals in account and sometimes they can be different, don't ask me
									//this can result in broken normals but fixes autosculpt on some desperate cases (BMW M6 KIT06_BUMPER_FRONT_T5)
									
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[0]) == 
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1])
									
									partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[1])
									){
								//vertex 1 is identical to vertex 0 on at least one part
								move0 = true;
							} else
							if (
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[0]) == 
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2])
									
									partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[2])
									){
								//vertex 2 is identical to vertex 0 on at least one part
								move1 = true;
							} else
							if (
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1]) == 
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2])

									partsTris.get(p2).get(i)[1].positionEquals(partsTris.get(p2).get(i)[2])							
									){
								//vertex 2 is identical to vertex 1 on at least one part
								move2 = true;
							}
							
						}

						// why this exists and how the issue has been fixed :
						// triangle 102 (152) has move0, ends up with 127 210 209 with 210 and 209 in the same position
						// then triangle 103 gets fucked up, ends w/ 210 211 227 when it should have 209 211 227
						// gotta guess the index between 209 and 210 on a part where the triangle doesn't have two equal vertices

						if (move0) { // vert0 == vert1
							// 1. figure out the correct id
							short ID0=0, ID1=0, ID2=0;
							Vertex v0 = null, v1 = null, v2 = null;
							for (int p2=0; p2<asParts.size(); p2++) {
								if (
										//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf((VertexPosition)partsTris.get(p2).get(i)[0]) != 
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf((VertexPosition)partsTris.get(p2).get(i)[1])
										
										!partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[1])
										){
								// find a part where vertex 1 is different from vertex 0 and store its index
									ID0 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[0]);
									ID1 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1]);
									ID2 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2]);
//									System.out.println("move0 : triangle "+i+", taking ID from part "+asParts.get(p2).header.partName);

									// get a vertex in a triangle with non-zero surface to attempt to fix normals
									v0 = partsTris.get(p2).get(i)[0];
									v1 = partsTris.get(p2).get(i)[1];
									v2 = partsTris.get(p2).get(i)[2];
									break;
								}
							}
							
							// 2. change the id for vertex 0 on all parts
							for (int p2=0; p2<asParts.size(); p2++) {
								// assign the new vert id to the tri for each part
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert0 = ID0;
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert1 = ID1;
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert2 = ID2;

								//add the vertex							
								asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.add(new Vertex(partsTris.get(p2).get(i)[1]));

								//attempt to fix normals
								if (v0 != null && SAVE_fixAutosculptNormals) {
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID0), v0));
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID1), v1));
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID2), v2));
								}
							}
						} else

						if (move1) { // vert0 == vert2
							// 1. figure out the correct id
							short ID0=0, ID1=0, ID2=0;
							Vertex v0 = null, v1 = null, v2 = null;
							for (int p2=0; p2<asParts.size(); p2++) {
								if (
										//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf((VertexPosition)partsTris.get(p2).get(i)[0]) != 
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf((VertexPosition)partsTris.get(p2).get(i)[2])
									
									!partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[2])
										){
								// find a part where vertex 2 is different from vertex 0 and store its index
									ID0 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[0]);
									ID1 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1]);
									ID2 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2]);
//									System.out.println("move1 : triangle "+i+", taking ID from part "+asParts.get(p2).header.partName);

									// get a vertex in a triangle with non-zero surface to attempt to fix normals
									v0 = partsTris.get(p2).get(i)[0];
									v1 = partsTris.get(p2).get(i)[1];
									v2 = partsTris.get(p2).get(i)[2];
									break;
								}
							}
							
							// 2. change the id for vertex 0 on all parts
							for (int p2=0; p2<asParts.size(); p2++) {
								// assign the new vert id to the tri for each part
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert0 = ID0;
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert1 = ID1;
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert2 = ID2;
								
								//add the vertex							
								asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.add(new Vertex(partsTris.get(p2).get(i)[2]));

								//attempt to fix normals
								if (v0 != null && SAVE_fixAutosculptNormals) {
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID0), v0));
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID1), v1));
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID2), v2));
								}
							}
						} else

						if (move2) { // vert1 == vert2
							// 1. figure out the correct id
							short ID0=0, ID1=0, ID2=0;
							Vertex v0 = null, v1 = null, v2 = null;
							for (int p2=0; p2<asParts.size(); p2++) {
								if (
										//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1]) != 
									//asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2])
									
										!partsTris.get(p2).get(i)[1].positionEquals(partsTris.get(p2).get(i)[2])
										){
								// find a part where vertex 2 is different from vertex 1 and store its index
									ID0 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[0]);
									ID1 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[1]);
									ID2 = (short)asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.lastIndexOf(partsTris.get(p2).get(i)[2]);
//									System.out.println("move2 : triangle "+i+", taking ID from part "+asParts.get(p2).header.partName);

									// get a vertex in a triangle with non-zero surface to attempt to fix normals
									v0 = partsTris.get(p2).get(i)[0];
									v1 = partsTris.get(p2).get(i)[1];
									v2 = partsTris.get(p2).get(i)[2];
									break;
								}
							}
							
							// 2. change the id for vertex 0 on all parts
							for (int p2=0; p2<asParts.size(); p2++) {
								// assign the new vert id to the tri for each part
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert0 = ID0;
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert1 = ID1;
								asParts.get(p2).mesh.materials.materials.get(mat).triangles.get( i ).vert2 = ID2;
								
								//add the vertex							
								asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.add(new Vertex(partsTris.get(p2).get(i)[2]));

								//attempt to fix normals
								if (v0 != null && SAVE_fixAutosculptNormals) {
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID0), v0));
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID1), v1));
									swapNormals.add(new Pair<Vertex, Vertex>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID2), v2));
								}
							}
						} 
						
//						else if (p.header.partName.contains("KIT06_BUMPER_FRONT_A")) System.out.println("triangle "+i+", nothing detected (verts : "
//								+ "\n"+partsTris.get(0).get(i)[0].toString()
//								+ "\n"+partsTris.get(0).get(i)[1].toString()
//								+ "\n"+partsTris.get(0).get(i)[2].toString());
						
					}
				}
			}//loop on materials
			
			for (var pair : swapNormals) {
				pair.getKey().colorR = pair.getValue().colorR;
				pair.getKey().colorG = pair.getValue().colorG;
				pair.getKey().colorB = pair.getValue().colorB;
				pair.getKey().colorA = pair.getValue().colorA;
				pair.getKey().normX = pair.getValue().normX;
				pair.getKey().normY = pair.getValue().normY;
				pair.getKey().normZ = pair.getValue().normZ;
				pair.getKey().normW = pair.getValue().normW;
				pair.getKey().tanX = pair.getValue().tanX;
				pair.getKey().tanY = pair.getValue().tanY;
				pair.getKey().tanZ = pair.getValue().tanZ;
				pair.getKey().tanW = pair.getValue().tanW;
			}
			
			if (invalidateVertsGlobal) {
				//compute necessary stuff again
				for (var p2 : asParts) {
		    		p2.mesh.verticesBlocks.clear();
		    		p2.mesh.triangles.triangles.clear();
		    		for (var m :p2.mesh.materials.materials) {
		    			p2.mesh.verticesBlocks.add(m.verticesBlock);
		    			p2.mesh.triangles.triangles.addAll(m.triangles);
					}
		        	p2.header.trianglesCount = p2.mesh.triangles.triangles.size();
		        	p2.mesh.info.numTriangles = p2.mesh.triangles.triangles.size();
		        	int triVertI = 0;
		        	p2.mesh.info.numVertices = 0;
		        	for (var vb : p2.mesh.verticesBlocks) p2.mesh.info.numVertices += vb.vertices.size();
		        	for (var m : p2.mesh.materials.materials) {
		        		m.fromTriVertID = triVertI; //actually concerns triangles
		        		m.toTriVertID = m.fromTriVertID + m.triangles.size()*3;
		        		m.numTriVertices = m.toTriVertID - m.fromTriVertID;
		        		triVertI = m.toTriVertID;
		        		m.verticesDataLength = m.verticesBlock.vertices.size()*Vertices.vertexLength;
		            }
		        	p2.rebuildSubBlocks();
//    		        	System.out.println(p2.header.partName+" fixed : "+p2.mesh.info.numTriangles+" tris, "+p2.mesh.info.numVertices+" verts.");
				}
			}
		}
	}
	
}