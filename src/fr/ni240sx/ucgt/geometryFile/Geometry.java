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
import fr.ni240sx.ucgt.geometryFile.io.VertexData3;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
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
import fr.ni240sx.ucgt.geometryFile.sorters.MaterialsSorterGlobalList;
import fr.ni240sx.ucgt.geometryFile.sorters.MaterialsSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.PartSorterLodKitName;
import javafx.util.Pair;
import fr.ni240sx.ucgt.geometryFile.settings.*;

public class Geometry extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Geometry;}
	
	public GeomHeader geomHeader;
	public List<Part> parts = new ArrayList<>();//Collections.synchronizedList(new ArrayList<Part>());
	public List<Material> materials = new ArrayList<>();//Collections.synchronizedList(new ArrayList<Material>());
	public List<MPoint> mpointsAll = new ArrayList<>();//Collections.synchronizedList(new ArrayList<MPoint>());
	public List<MPointPositionCube> mpointsPositions = new ArrayList<>();//Collections.synchronizedList(new ArrayList<MPoint>());
	public List<Hash> hashlist = new ArrayList<>();//Collections.synchronizedList(new ArrayList<Hash>());
	public List<AutosculptLinking> asLinking = new ArrayList<>();//Collections.synchronizedList(new ArrayList<AutosculptLinking>());
	
	public ArrayList<String> forceAsFixOnParts = new ArrayList<>();
	public ArrayList<Pair<String,String>> renameParts = new ArrayList<>();
	public ArrayList<String> deleteParts = new ArrayList<>();
	public ArrayList<RenderPriority> priorities = new ArrayList<>();
	
	
	public String carname = "UNDETERMINED";
	
	public static boolean USE_MULTITHREADING = true;

	public static CompressionType defaultCompressionType = CompressionType.RefPack;
	public static CompressionLevel defaultCompressionLevel = CompressionLevel.Low;
	
	public static boolean LOAD_removeUselessAutosculptParts = false;
	
	public static boolean SAVE_removeUselessAutosculptParts = true;
	public static boolean SAVE_optimizeMaterials = true;
	public static boolean SAVE_sortEverythingByName = true;
	public static boolean SAVE_fixAutosculptNormals = true;
	public static boolean SAVE_removeInvalid = true;
	public static boolean SAVE_copyMissingLODs = false;
	public static boolean SAVE_copyLOD_D = false;

	public static boolean IMPORT_importVertexColors = true;
	public static boolean IMPORT_calculateVertexColors = false;
	public static SettingsImport_Tangents IMPORT_Tangents = SettingsImport_Tangents.HIGH;
	public static boolean IMPORT_flipV = false;
	
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
				try {
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
						parts.add(new Part(dataWriter));	
					} else {
						in.position(o.offset);
						parts.add(new Part(in));	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//		}

		// determines the compressiontype to put the right one for re-exporting
		// disabled for testing purposes
		if (geomHeader.partsOffsets.partOffsets.get(0).isCompressed == 512) defaultCompressionType = CompressionType.RefPack;
		else if (geomHeader.partsOffsets.partOffsets.get(0).isCompressed == 0) defaultCompressionType = CompressionType.RawDecompressed;
		
		for (var p : parts) {
			try {
				if (p.header.partName.contains("_KIT")) carname = p.header.partName.split("_KIT")[0];
				break;
			} catch (@SuppressWarnings("unused") Exception e) {
				//header is null
			}
		}
		if (carname.equals("UNDETERMINED")) System.out.println("Could not determine car name.");
			
		updateHashes();
		
		
		
		materials.clear();
		mpointsAll.clear();
		mpointsPositions.clear();

		for (var p : parts) {
			p.findName(carname);
			computeMatsList(p);
			globalizePartMarkers(p);
			
//				optimizeAutosculpt(toRemove, p); 
		}
		computeMarkersList();
//			if (LOAD_removeUselessAutosculptParts) for (var p : toRemove) {
//				System.out.println("Removing part "+p.header.partName);
//				parts.remove(p);
//			}

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
		//process stuff on parts (TODO make a part invalidation list for that and compressing)
		processParts();
		
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
							System.out.print("\rProgress " + Math.round(100*((float)(parts.indexOf(p))/parts.size()))+ " %" );
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
					System.out.print("\rProgress " + Math.round(100*((float)(parts.indexOf(p))/parts.size()))+ " %" );
				}
			}
		}
			

		System.out.println("\nParts compressed in "+(System.currentTimeMillis()-t)+" ms.");
		t = System.currentTimeMillis();
		
		geomHeader.refresh(parts);
		
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
		bw.write("SETTING	CarName="+this.carname+"\n"
				+ "SETTING	UseMultithreading="+USE_MULTITHREADING+"\n"
				+ "SETTING	CompressionType="+defaultCompressionType+"\n"
				+ "SETTING	CompressionLevel="+defaultCompressionLevel.getName()+"\n"
				+ "SETTING	VertexColors="
				)	;
		if (IMPORT_importVertexColors) bw.write("Import\n");
		else if (IMPORT_calculateVertexColors) bw.write("Calculate\n");
		else bw.write("Off\n");
		bw.write("SETTING	Tangents="+IMPORT_Tangents.getName()+"\n"
				+ "SETTING	RemoveUselessAutosculpt="+SAVE_removeUselessAutosculptParts+"\n"
				+ "SETTING	OptimizeMaterials="+SAVE_optimizeMaterials+"\n"
				+ "SETTING	FixAutosculptNormals="+SAVE_fixAutosculptNormals+"\n"
				+ "SETTING	RemoveInvalid="+SAVE_removeInvalid+"\n"
				+ "SETTING	CopyMissingLODs="+SAVE_copyMissingLODs+"\n"
				+ "SETTING	MakeLodD="+SAVE_copyLOD_D+"\n"
				+ "");

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
		if (!modelFile.getName().endsWith(".obj") && !modelFile.getName().endsWith(".z3d")) {
			throw new Exception("Wrong file format ! Only Wavefront OBJ and ZModeler 2 Z3D are supported.");
		}
		long time = System.currentTimeMillis();
		System.out.println("Loading config from "+configFile.getName());
		Geometry geom = new Geometry();
		geom.readConfig(configFile);
		System.out.println("Config read in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
		System.out.println("Importing meshes from "+modelFile.getName());
		if (modelFile.getName().endsWith(".obj")) WavefrontOBJ.load(geom, modelFile);
		if (modelFile.getName().endsWith(".z3d")) ZModelerZ3D.load(geom, modelFile);
		System.out.println("3D model converted in "+(System.currentTimeMillis()-time)+" ms.");			
		return geom;			
	}
	
	public void readConfig(File f) throws IOException {
		var br = new BufferedReader(new FileReader(f));
		String l;
		while ((l=br.readLine())!=null) {
			int iterator;
			switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
			case "SETTING": {
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
						carname = s2.split("=")[1].toUpperCase();
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
						if (IMPORT_flipV) System.out.println("Flipping V texture coordinates.");
						break;
						
						//EXPERIMENTAL SETTINGS
					case "ForceAsFix":
						forceAsFixOnParts.add(s2.split("=")[1]);
						System.out.println("Forcing Autosculpt fix : "+s2.split("=")[1]);
						break;
					case "FixAutosculptNormals":
						SAVE_fixAutosculptNormals = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_fixAutosculptNormals) System.out.println("Attempting to fix normals on Autosculpt zero area triangles.");
						break;
						
					case "SortAllByName":
						SAVE_sortEverythingByName = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_sortEverythingByName) System.out.println("Sorting everything by name.");
						break;
						
					case "CopyMissingLODs":
						SAVE_copyMissingLODs = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_copyMissingLODs) System.out.println("Copying missing LODs.");
						break;
					case "MakeLodD":
						SAVE_copyLOD_D = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_copyLOD_D) System.out.println("Copying lod D from lod C.");
						break;
						
					case "RemoveInvalid":
						SAVE_removeInvalid = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_removeInvalid) System.out.println("Removing invalid parts.");
						break;

					default:
						System.out.println("Setting not supported : "+s2);
					}
				}
				break;}
				
			case "RENAME":
				iterator = 0;
				String toren = null;
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) {
					switch(iterator) {
					case 1:
						toren = s2.toUpperCase();
						break;
					case 2:
						renameParts.add(new Pair<>(toren, s2.toUpperCase()));
						break;
					}
					iterator++;
				}
				break;
				
			case "DELETE":
				iterator = 0;
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) {
					switch(iterator) {
					case 1:
						deleteParts.add(s2.toUpperCase());
						break;
					}
					iterator++;
				}
				break;
				
			case "MATERIAL": // IF THE CAR NAME ISN'T SET BEFORE THIS, CAR-SPECIFIC TEXTURES WILL BREAK
				var m = new Material();
				materials.add(m);
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MATERIAL")) {
					if (!s2.contains("=")) { //material name
						m.uniqueName = s2;
					} else { //shader or texture usage
						if (s2.split("=")[0].equals("UseTangents")) {
							//material tangents setting
							m.useTangents = Boolean.getBoolean(s2.split("=")[1]);
						} else if (s2.split("=")[0].equals("FERenderingOrder")) {
							m.renderingOrder = Integer.parseInt(s2.split("=")[1]);
						} else if (s2.split("=")[0].equals("RenderingOrder")) { // i don't know
							m.usageSpecific1 = Integer.parseInt(s2.split("=")[1]);
						} else if (TextureUsage.get(s2.split("=")[1]) != TextureUsage.INVALID) {
							// texture usage
							m.TextureHashes.add(new Hash(s2.split("=")[0].replace("%", carname).toUpperCase()));
							m.textureUsages.add(TextureUsage.get(s2.split("=")[1]));
						} else if (ShaderUsage.get(s2.split("=")[1]) != ShaderUsage.INVALID) {
							//shader usage
							m.ShaderHash = new Hash(s2.split("=")[0].toUpperCase());
							m.shaderUsage = ShaderUsage.get(s2.split("=")[1]);
						}
					}
				}
//				System.out.println("Material : "+m.toConfig(carname));
				break;
				
			case "MARKER":
				var mp = new MPoint();
				iterator = 0;
				float u=0, v=0, w=0; //euler angles
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MARKER")) {
					switch (iterator) {
					case 0:
						mp.uniqueName = s2.toUpperCase();
						break;
					case 1:
						mp.nameHash = new Hash(s2.toUpperCase());
						break;
					case 2:
						mp.tempPartName = s2.toUpperCase();
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
						asl.tempPartName = s2.toUpperCase();
					} else {
						asl.links.add(new AutosculptLink(new Hash(carname+"_"+s2.split(",")[0].toUpperCase()).binHash, 
								Short.parseShort(s2.split(",")[1]), 
								Short.parseShort(s2.split(",")[2]), 
								Short.parseShort(s2.split(",")[3]), 
								Short.parseShort(s2.split(",")[4])  ));
					}
				}
//				System.out.println("Autosculpt link : "+l);
				break;
				
			case "PRIORITY":
				var prio = new RenderPriority();
				priorities.add(prio);
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("PRIORITY")) {
					if (!s2.contains("=")) { //part name
						prio.partName = s2.toUpperCase();
					} else {
						Material material = null;
						for (var mat : materials) if (mat.uniqueName.equals(s2.split("=")[0])) {
							material = mat;
							break;
						}
						if (material != null) prio.values.add(new Pair<>(material, Integer.parseInt(s2.split("=")[1])));
					}
				}
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
		bw.write("SETTING	CarName=MISSING\n"
				+ "SETTING	UseMultithreading="+Geometry.USE_MULTITHREADING+"\n"
				+ "SETTING	CompressionType="+Geometry.defaultCompressionType+"\n"
				+ "SETTING	CompressionLevel="+Geometry.defaultCompressionLevel.getName()+"\n"
				+ "SETTING	VertexColors=Calculate\n"
				+ "SETTING	Tangents="+Geometry.IMPORT_Tangents.getName()+"\n"
				+ "SETTING	FlipV=true\n"
				+ "SETTING	RemoveUselessAutosculpt="+Geometry.SAVE_removeUselessAutosculptParts+"\n"
				+ "SETTING	OptimizeMaterials="+Geometry.SAVE_optimizeMaterials+"\n"
				+ "SETTING	FixAutosculptNormals="+SAVE_fixAutosculptNormals+"\n"
				+ "SETTING	RemoveInvalid="+SAVE_removeInvalid+"\n"
				+ "SETTING	CopyMissingLODs="+SAVE_copyMissingLODs+"\n"
				+ "\n--- Converted data from CTK config ---\n" );
		
		String l;
		int iterator;
		while ((l=br.readLine())!=null) {
			switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
				
			case "PART":
				iterator = 0;
				String meshName = "";
				String wantedPart = "";
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
					switch(iterator) {
					case 2:
						//mesh in the model
						meshName = s2;
						break;
					case 3:
						//wanted part name
						wantedPart = s2;
						break;
					}
					iterator++;
				}
				if (!meshName.equals(wantedPart)) {
					bw.write("RENAME	"+meshName+"	"+wantedPart+"\n");
				}
				break;
			
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
						if ("METAL_SWATCH".equals(texture)) swatch = "METAL_SWATCH"; else swatch = "%_SKIN1";
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
					if (shader.equals("DOORLINE") && normal != null) usage = ShaderUsage.DiffuseNormalAlpha;
					if (shader.equals("HEADLIGHTREFLECTOR")) {
						usage = ShaderUsage.DiffuseGlow;
						glow = texture;
					}
//					if (texture.equals("TIRE_STYLE01")) usage = ShaderUsage.DiffuseNormalAlpha;
					if ("%_BADGING".equals(texture) && normal != null) usage = ShaderUsage.DiffuseNormalAlpha;
					if ("DAMAGE_N".equals(normal)) usage = ShaderUsage.DiffuseNormalSwatch;
					//vanilla plus
//					if (texture.equals("GRILL_02")) usage = ShaderUsage.DiffuseAlpha; 
//					if (texture.equals("%_ENGINE")) {
//						usage = ShaderUsage.DiffuseNormal;
//						normal = "%_ENGINE_N";
//					}
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
				//	TODO try to fix rotations
				//	CTK				UCGT
				//	0 0 0			0	0	0			-> ok
				
				//	0 -90 0			0	-90	-180		-> specific ok
				
				//	0 0 -180		0	0	180			-> should be ok
				
				//	-70 180 90		180	-70.052	90		-> +/-90 ok
				//	70 180 -90		180	-70.052	-90
				//	10 0 90			0	-10.02	90
				//	-10 0 -90		0	-10.02	-90
				//	0 0 -90			0	0	-90
				//	0 0 90			0	0	90
				
				//	-25.26 90 180	0	-90	-25.283		-> 90 +/-180 ok
				//	25 90 180		0	-90	25.027
				//	0 90 -180		0	-90	0
				//	0.1 90 -180		0	-90	0.115
				//	-89.19 90 -180	0	-90	-90
				//	-89.19 90 -180	0	-90	-90

				//	89.19 -90 0		0	-90	90
				//	70 -90 0		0	-90	110		was broken in 1.1.2 (output 70 -270 0), the vS == +/- 90 case seemed to be completely wrong
				
				// 	89.19 180 180	90	0	0


				if (uS.equals("0") && vS.equals("-90") && wS.equals("0")) {uS = "0"; vS="-90"; wS="-180";}
				else if (vS.equals("90") && (wS.equals("180") || wS.equals("-180"))) {wS = uS; uS = "0"; vS = "-90";}
				else if ((vS.equals("180") || vS.equals("-180")) && (wS.equals("180") || wS.equals("-180"))) {vS = "0"; wS = "0";}
				else if ((wS.equals("90") || wS.equals("-90"))) {var tp = vS; vS = "-"+uS.replace("-", ""); uS = tp;}
				else if (vS.equals("90") || vS.equals("-90")) {
					double u = Double.parseDouble(uS);
					u = 180*Math.abs(u)/u - u;					
					uS = "0";
					vS = "-90";
					wS = ((int) u == u) ? Integer.toString((int)u) : Double.toString(u);
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

	public void processParts() {
		var toRemove = Collections.synchronizedList(new ArrayList<Part>()); // parts flagged to be removed to optimize the geometry (eg T0 autosculpt with no other actual morphtargets)
		var toAdd = Collections.synchronizedList(new ArrayList<Part>()); // parts flagged to be added (eg missing LODs)
		
		if (!renameParts.isEmpty()) {
//			System.out.println("parts to rename present :");
//			for (var k : renameParts.keySet()) {
//				System.out.println(k+" : "+renameParts.get(k));
//			}
			for (var p : parts) for (var r : renameParts) if (p.name.contains(r.getKey())) {
				System.out.print("Renaming part "+p.name);
				p.header.partName = p.header.partName.replace(r.getKey(), r.getValue());
				p.header.binKey = new Hash(p.header.partName).binHash;
				p.name = p.name.replace(r.getKey(), r.getValue());
				p.findKitLodPart(carname);
				System.out.println(" to "+p.name);
			}
		}
		
		if (!deleteParts.isEmpty()) {
//			System.out.println("parts to rename present :");
//			for (var k : renameParts.keySet()) {
//				System.out.println(k+" : "+renameParts.get(k));
//			}
			for (var p : parts) for (var r : deleteParts) if (p.name.contains(r)) {
				System.out.println("Deleting part "+p.name);
				toRemove.add(p);
			}
		}
		
		
		for (var p : parts) {		
			HashMap<Integer, Integer> FERenderData; //first int is either shader usage or shader hash
			FERenderData = new HashMap<>();
			
			if (SAVE_removeInvalid) {if (!checkValid(toRemove, p)) continue;}
			optimizeAutosculpt(toRemove, p); 
			computeMatsList(p); // IMPORTANT TO KEEP HERE
			checkVertexBounds(p);
//			globalizePartMarkers(p);
			if (SAVE_optimizeMaterials && p.strings != null) p.subBlocks.remove(p.strings);
			if (p.mesh != null) {				
				for (var m : p.mesh.materials.materials) if (m.renderingOrder!=0) FERenderData.put(m.ShaderHash.binHash , m.renderingOrder*256);
				for (var m : p.mesh.materials.materials) {
					m.tryGuessFEData(FERenderData);
					if (SAVE_optimizeMaterials) {	
		    			m.removeUnneeded();
		    		} else {
						m.tryGuessUsageSpecific();
//						m.tryGuessFlags(this);
//		    			m.tryGuessDefaultTex();
		    		}
				}
				for (var prio : priorities) if (p.name.contains(prio.partName)) {
//					System.out.println("Changing render priorities for part "+p.name);
					for (var m : p.mesh.materials.materials) {
						for (var pair : prio.values) {
							if (m.equals(pair.getKey())) { //for some reason materials are NOT equal (materials are fucked up for some reason and have texture hashes repeated twice)
								//HOW IS IT NOT FINDING THE FUCKING MATERIALS
								m.usageSpecific1 = pair.getValue();
							}
						}
					}
				}
			}
			fixAutosculptMeshes(p);
			
			if (SAVE_copyMissingLODs) checkAndCopyMissingLODs(toAdd, p);
		}

		//remove/add parts
		parts.removeAll(toRemove);
		parts.addAll(toAdd);
	}

	@SuppressWarnings("unlikely-arg-type")
	public void globalizePartMarkers(Part p) {
		// GLOBAL MARKERS STORAGE
		if (p.mpoints != null) for (var mp : p.mpoints.mpoints) {
			mp.part = p;
			mp.tryGuessName(this);
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
		
		// MESH MATERIALS OPTIMIZATION + GLOBAL MATERIALS (only the global materials part should be kept here)
		
		
		if (p.mesh != null) if (p.mesh.materials != null) for (var m : p.mesh.materials.materials) {	
			m.tryGuessHashes(this, p);

			//destructive tests 
//				m.tryGuessUsageSpecific();
//				m.tryGuessFlags(this);
//				m.tryGuessDefaultTex();

			
			
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

	public static boolean checkValid(List<Part> toRemove, Part p) {
		boolean valid = true;
		
		if (p.kit.equals("")) valid = false;
		if (!p.lod.equals("A") && !p.lod.equals("B") && !p.lod.equals("C") && !p.lod.equals("D")) valid = false;
		
		if (!valid) {
			toRemove.add(p);
			System.out.println("Invalid part : "+p.name);
		}
		return valid;
	}
	
	public void checkAndCopyMissingLODs(List<Part> toAdd, Part p) {
		
		for (var part : toAdd) if (part.kit.equals(p.kit) && part.part.equals(p.part)) return;
		
		Part A=null;
		Part B=null;
		Part C=null;
		Part D=null;
		for (var p2 : parts) if (p2.kit.equals(p.kit) && p2.part.equals(p.part)) {
			if (p2.lod.equals("A")) A = p2;
			if (p2.lod.equals("B")) B = p2;
			if (p2.lod.equals("C")) C = p2;
			if (p2.lod.equals("D")) D = p2;
		}
//		System.out.println(p.name+" has A : "+(A!=null)+", has B : "+(B!=null)+", has C : "+(C!=null));

		if (A == null && B != null) toAdd.add(A = new Part(B, carname, B.kit+"_"+B.part+"_A"));
		if (A == null && C != null) toAdd.add(A = new Part(C, carname, C.kit+"_"+C.part+"_A"));
		if (B == null && A != null) toAdd.add(B = new Part(A, carname, A.kit+"_"+A.part+"_B"));
		assert B!=null; //B cannot be null since there's at least one part that isn't (the one from which the method got called)
		if (C == null) toAdd.add(C = new Part(B, carname, B.kit+"_"+B.part+"_C"));
		
		if (SAVE_copyLOD_D && D == null && p.kit.equals("KIT00")) {
			toAdd.add(D = new Part(C, carname, C.kit+"_"+C.part+"_D"));
		}
		
	}
	
	public static void checkVertexBounds(Part p) {
		boolean posOOB = false;
		boolean UVOOB = false;
		for (var m : p.mesh.materials.materials) {
			for (var v : m.verticesBlock.vertices) {
				if (v.posX > Vertex.posMax || v.posY > Vertex.posMax || v.posZ > Vertex.posMax || 
						v.posX < Vertex.posMin || v.posX < Vertex.posMin || v.posX < Vertex.posMin) posOOB = true;
				if (v.texU > Vertex.UVMax || v.texV > Vertex.UVMax || v.texU < Vertex.UVMin || v.texV < Vertex.UVMin) UVOOB = true;
			}
		}
		if (posOOB) System.out.println("Warning : one or multiple vertices are too far away from the origin on part "+p.name+" ! Please keep X, Y and Z between -10 and +10.");
		if (UVOOB) System.out.println("Warning : one or multiple UV summits are too far away from the origin on part "+p.name+" ! Please keep U and V between -32 and +32.");
	}
	
	public void optimizeAutosculpt(List<Part> toRemove, Part p) {
		// AUTOSCULPT OPTIMIZATION
		
		int numZonesFound = 0;
		Part potentialUselessT0 = null;
		//link autosculpt zones
		for (var p2 : parts) if (p2 != p) {
			if ((p2.part.substring(0, p2.part.length()-1).equals(p.part+"_T") || p2.part.substring(0, p2.part.length()-2).equals(p.part+"_T"))
					&& p2.kit.equals(p.kit) && p2.lod.equals(p.lod)) { // as zone found
				potentialUselessT0 = p2;
				numZonesFound++;
			}
		}
		
		if (SAVE_removeUselessAutosculptParts) if (numZonesFound == 1 && toRemove != null) { 
			//if only one zone detected, check whether it is T0, if yes yeet it, it should have no point existing
			toRemove.add(potentialUselessT0);
		}
		
		if (p.asZones != null) {
			if (numZonesFound < 2) { //preexisting but no as zones detected
				p.subBlocks.remove(p.asZones);
				p.asZones = null;
//					System.out.println("Removed unused autosculpt zones on "+p.name);
			}
		} else if (numZonesFound > 1) { //nothing preexisting but as zones detected
			p.asZones = new AutosculptZones();
			p.asZones.zones = p.generateASZones();
			p.subBlocks.add(p.asZones);
//			System.out.println("Added autosculpt zones on "+p.name);
		}
	}
	
	public void updateHashes() {
		hashlist.clear();
		hashlist.add(new Hash(carname));
		
		try {
			for (var p : parts) {
				hashlist.add(new Hash(p.header.partName));
			} 
			
		} catch (@SuppressWarnings("unused") Exception e) {
			//header is null or smth
		}
		
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

	public void rebuild() {
		for (var p : parts) {

        	//sort materials by name and add the materials data back to the mesh
//        	if (Geometry.SAVE_sortEverythingByName) p.mesh.materials.materials.sort(new MaterialsSorterName());
        	//same order as in config file
//        	else 
        	p.mesh.materials.materials.sort(new MaterialsSorterGlobalList(this));
    		
        	p.mesh.verticesBlocks.clear();
    		p.mesh.triangles.triangles.clear();
        	
        	//make hashmaps for textures and shaders
        	
        	//    ID		TEX/SHAD        USAGE
        	HashMap<Integer, Pair<Integer, Integer>> texturesAndUsage = new HashMap<>();
        	HashMap<Pair<Integer, Integer>, Integer> texturesIDs = new HashMap<>();
//        	HashMap<Integer, Pair<Integer, Integer>> shadersAndUsage = new HashMap<Integer, Pair<Integer, Integer>>();
        	HashMap<Integer , Integer> shadersOnly = new HashMap<>();
        	HashMap<Integer , Integer> shadersIDs = new HashMap<>();

        	List<TextureUsage> allTextureUsages = new ArrayList<>();
        	List<ShaderUsage> allShaderUsages = new ArrayList<>();
        	
        	int texi = 0;
        	int shai = 0;
        	for (var m : p.mesh.materials.materials) {
        		p.mesh.verticesBlocks.add(m.verticesBlock);
        		p.mesh.triangles.triangles.addAll(m.triangles);
        		
        		
        		if (	(Geometry.IMPORT_Tangents == SettingsImport_Tangents.LOW && m.needsTangentsLow()) || 
    					(Geometry.IMPORT_Tangents == SettingsImport_Tangents.HIGH && m.needsTangentsHigh()) || 
    					(Geometry.IMPORT_Tangents == SettingsImport_Tangents.MANUAL && m.useTangents == true) ||
    					Geometry.IMPORT_Tangents == SettingsImport_Tangents.ON ) {
        			
        			addToTangents(m);
        			normalizeTangents(m.verticesBlock.vertices);
        		}
        		
        		if (Geometry.IMPORT_calculateVertexColors) {
    				for (var v : m.verticesBlock.vertices) {
    					// -1 to 1 -> 20 to 255
        				int color;
        				if (!p.part.contains("WHEEL") && !p.part.contains("BRAKE"))
        					color = Math.max(0, Math.min(255, (int)((v.normZ+0.8)*150)));
        				else color = Math.max(20, Math.min(255, (int)((-v.normY+0.8)*150)));
        				v.colorR = (byte) color;
        				v.colorG = (byte) color;
        				v.colorB = (byte) color;
    				}
    			}
        		
//        		if (!shadersAndUsage.containsValue(new Pair<Integer,Integer>(m.ShaderHash.binHash, m.shaderUsage.getKey()))) {
//       			shadersAndUsage.put(shadersAndUsage.size(), new Pair<Integer,Integer>(m.ShaderHash.binHash, m.shaderUsage.getKey()));
//        		}
        		if (!shadersOnly.containsValue(m.ShaderHash.binHash)) {
        			shadersOnly.put(shai, m.ShaderHash.binHash);
        			shadersIDs.put(m.ShaderHash.binHash, shai);
        			shai++;
        		}
        		if (!allShaderUsages.contains(m.shaderUsage)) allShaderUsages.add(m.shaderUsage);

        		for (int i=0; i<m.TextureHashes.size(); i++) {
        			if (!texturesAndUsage.containsValue(new Pair<>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()))) {
	        				texturesAndUsage.put(texi, new Pair<>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()));
	        				texturesIDs.put(new Pair<>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()), texi);
	        				texi++;
            			}
        			if (!allTextureUsages.contains(m.textureUsages.get(i))) allTextureUsages.add(m.textureUsages.get(i));
        		}
        	}
        	
//        	System.out.println(texturesAndUsage);
        	
        	//post-treatment
        	//--- header ---
        	//calculate triangle count
        	//calculate textures count
        	//calculate shaders count
        	//calculate bounds
        	p.header.trianglesCount = p.mesh.triangles.triangles.size();
        	p.header.texturesCount = (short) texturesAndUsage.size();
        	p.header.shadersCount = (short) shadersOnly.size();
        	p.computeBounds();
        	//--- texusage ---
        	//fill in the texusage pairs
        	for (int i=0; i<texturesAndUsage.size(); i++) {
        		p.texusage.texusage.add(texturesAndUsage.get(i));
        	}
        	//--- strings ---
        	//fill in the strings based on mesh shader usages
        	if (allTextureUsages.contains(TextureUsage.DIFFUSE)) p.strings.strings.add("DIFFUSE");
        	if (allTextureUsages.contains(TextureUsage.NORMAL)) p.strings.strings.add("NORMAL");
        	if (allTextureUsages.contains(TextureUsage.SWATCH)) p.strings.strings.add("AMBIENT");
        	if (allTextureUsages.contains(TextureUsage.ALPHA) || allTextureUsages.contains(TextureUsage.OPACITY)) p.strings.strings.add("OPACITY");
        	if (allTextureUsages.contains(TextureUsage.SELFILLUMINATION)) p.strings.strings.add("SELFILLUMINATION");
        	//--- shaders ---
        	//fill in the shaders binhashes
        	for (int i=0; i<shadersOnly.size(); i++) p.shaderlist.shaders.add(shadersOnly.get(i));

        	//=== mesh ===
        	//--- info ---
        	//numMaterials, numTriangles, numVertices
        	p.mesh.info.numMaterials = p.mesh.materials.materials.size();
        	p.mesh.info.numTriangles = p.mesh.triangles.triangles.size();

        	for (var vb : p.mesh.verticesBlocks) p.mesh.info.numVertices += vb.vertices.size();
        	
        	int triVertI = 0;
        	//--- materials ---
        	//for each material :
        	//numVertices, toVertID
        	//shaderID, textureIDs
        	//verticesDataLength
        	for (var m : p.mesh.materials.materials) {
        		m.fromTriVertID = triVertI; //actually concerns triangles
        		m.toTriVertID = m.fromTriVertID + m.triangles.size()*3;
        		m.numTriVertices = m.toTriVertID - m.fromTriVertID;
        		triVertI = m.toTriVertID;
        		int shaderid = shadersIDs.get(m.ShaderHash.binHash);
        		m.shaderID = (byte) shaderid;
        		for (int i=0; i<m.TextureHashes.size(); i++) {
        			int texid = texturesIDs.get(new Pair<>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()));
        			m.textureIDs.add((byte) texid);
        		}
        		m.verticesDataLength = m.verticesBlock.vertices.size()*Vertices.vertexLength;
            }
        	
        	//--- shadersusage ---
        	//fill in the shaders usage
        	for (var s : allShaderUsages) p.mesh.shadersUsage.shadersUsage.add(s.getKey());
        	//--- vertices (several blocks) ---
        	//normally already filled in
        	//--- triangles (one block) ---
        	//normally already filled in
        	//--- autosculpt linking ---
        	//already filled in
        	//--- autosculpt zones ---
        	//recalculated at export time
        	
        	p.rebuildSubBlocks();
        }
        
        for (var mp : mpointsAll) {

        	for (var mp2 : mpointsAll) if (mp2 != mp && mp2.uniqueName.equals(mp.uniqueName) && mp2.verts.size()>0) {
        		mp.verts = mp2.verts;
        	}
        	
        	//calculate mpoint coords
        	VertexData3 avgPos = new VertexData3(0,0,0);
        	for (var v : mp.verts) {
        		avgPos.x += v.x;
        		avgPos.y += v.y;
        		avgPos.z += v.z;
        	}
        	mp.positionX = (float) (avgPos.x / mp.verts.size());
        	mp.positionY = (float) (avgPos.y / mp.verts.size());
        	mp.positionZ = (float) (avgPos.z / mp.verts.size());
        	
        	
        	if (Float.isNaN(mp.positionX)) System.out.println("NaN position for "+mp.uniqueName);
        }
//        geom.mpointsAll.clear();
//        for (var p : geom.parts) geom.globalizePartMarkers(p);
//        geom.computeMarkersList();
        Geometry.IMPORT_flipV = false; //reset
        
	}
	
	private static void addToTangents(Material m) {
		for (var t : m.triangles) {
		    m.verticesBlock.vertices.get(t.vert0).tanX += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posX-m.verticesBlock.vertices.get(t.vert0).posX) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posX-m.verticesBlock.vertices.get(t.vert0).posX)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert0).tanY += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posY-m.verticesBlock.vertices.get(t.vert0).posY) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posY-m.verticesBlock.vertices.get(t.vert0).posY)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert0).tanZ += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posZ-m.verticesBlock.vertices.get(t.vert0).posZ) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posZ-m.verticesBlock.vertices.get(t.vert0).posZ)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert1).tanX += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posX-m.verticesBlock.vertices.get(t.vert0).posX) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posX-m.verticesBlock.vertices.get(t.vert0).posX)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert1).tanY += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posY-m.verticesBlock.vertices.get(t.vert0).posY) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posY-m.verticesBlock.vertices.get(t.vert0).posY)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert1).tanZ += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posZ-m.verticesBlock.vertices.get(t.vert0).posZ) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posZ-m.verticesBlock.vertices.get(t.vert0).posZ)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert2).tanX += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posX-m.verticesBlock.vertices.get(t.vert0).posX) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posX-m.verticesBlock.vertices.get(t.vert0).posX)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert2).tanY += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posY-m.verticesBlock.vertices.get(t.vert0).posY) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posY-m.verticesBlock.vertices.get(t.vert0).posY)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		    m.verticesBlock.vertices.get(t.vert2).tanZ += 
		    		((m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posZ-m.verticesBlock.vertices.get(t.vert0).posZ) - 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posZ-m.verticesBlock.vertices.get(t.vert0).posZ)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert2).texV-m.verticesBlock.vertices.get(t.vert0).texV) - 
		    				(m.verticesBlock.vertices.get(t.vert2).texU-m.verticesBlock.vertices.get(t.vert0).texU) * 
		    				(m.verticesBlock.vertices.get(t.vert1).texV-m.verticesBlock.vertices.get(t.vert0).texV));
		}
	}
	
	private static void normalizeTangents(ArrayList<Vertex> vertices) {
		// Normalize tangents
	    for (var v : vertices) {
	        double nx = v.normX;
	        double ny = v.normY;
	        double nz = v.normZ;

	        double tx = v.tanX;
	        double ty = v.tanY;
	        double tz = v.tanZ;

	        // Gram-Schmidt orthogonalize
	        double dot = nx * tx + ny * ty + nz * tz;
	        tx -= nx * dot;
	        ty -= ny * dot;
	        tz -= nz * dot;

	        // Normalize the tangent
	        double length = Math.sqrt(tx * tx + ty * ty + tz * tz);
	        v.tanX = tx / length;
	        v.tanY = ty / length;
	        v.tanZ = tz / length;
	        v.tanW = 0x7FFF/32768.0;
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
			matsloop: for (int mat=0; mat<p.mesh.materials.materials.size(); mat++) {
				boolean invalidateVertsMat = false;
				if (forceAsFixOnParts.contains(p.name)) {
					invalidateVertsGlobal = true;
	    			invalidateVertsMat = true;
				} else {
					for (var p2 : asParts) {
			    		if (p2.mesh.materials.materials.get(mat).triangles.size() != p.mesh.materials.materials.get(mat).triangles.size()) {
			    			System.out.println("Warning : Different triangle counts across Autosculpt zones on part "+p.header.partName+", Autosculpt may break, please check your model !");
			    			break matsloop; //avoids further issues for the current part
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
							
							if (	partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[1])	){
								//vertex 1 is identical to vertex 0 on at least one part
								move0 = true;
							} else
							if (	partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[2])	){
								//vertex 2 is identical to vertex 0 on at least one part
								move1 = true;
							} else
							if (	partsTris.get(p2).get(i)[1].positionEquals(partsTris.get(p2).get(i)[2])	){
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
								if (	!partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[1])	){
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
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID0), v0));
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID1), v1));
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID2), v2));
								}
							}
						} else

						if (move1) { // vert0 == vert2
							// 1. figure out the correct id
							short ID0=0, ID1=0, ID2=0;
							Vertex v0 = null, v1 = null, v2 = null;
							for (int p2=0; p2<asParts.size(); p2++) {
								if (	!partsTris.get(p2).get(i)[0].positionEquals(partsTris.get(p2).get(i)[2])	){
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
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID0), v0));
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID1), v1));
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID2), v2));
								}
							}
						} else

						if (move2) { // vert1 == vert2
							// 1. figure out the correct id
							short ID0=0, ID1=0, ID2=0;
							Vertex v0 = null, v1 = null, v2 = null;
							for (int p2=0; p2<asParts.size(); p2++) {
								if (	!partsTris.get(p2).get(i)[1].positionEquals(partsTris.get(p2).get(i)[2])	){
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
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID0), v0));
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID1), v1));
									swapNormals.add(new Pair<>(asParts.get(p2).mesh.materials.materials.get(mat).verticesBlock.vertices.get(ID2), v2));
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

class RenderPriority{
	String partName = "";
	ArrayList<Pair<Material,Integer>> values = new ArrayList<>();
}