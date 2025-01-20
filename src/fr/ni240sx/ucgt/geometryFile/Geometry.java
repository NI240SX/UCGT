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
import java.nio.file.Files;
import java.nio.file.Paths;
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
import fr.ni240sx.ucgt.geometryFile.sorters.MPointPosSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.MPointSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.MaterialsSorterGlobalList;
import fr.ni240sx.ucgt.geometryFile.sorters.MaterialsSorterName;
import fr.ni240sx.ucgt.geometryFile.sorters.PartSorterBinKey;
import fr.ni240sx.ucgt.geometryFile.sorters.PartSorterLodKitName;
import javafx.util.Pair;
import fr.ni240sx.ucgt.geometryFile.settings.*;

public class Geometry extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Geometry;}
	
	public GeomHeader geomHeader;
	public List<Part> parts = new ArrayList<>();//Collections.synchronizedList(new ArrayList<Part>());
	public List<Material> materials = new ArrayList<>();//Collections.synchronizedList(new ArrayList<Material>());
	public List<MPoint> mpointsAll = new ArrayList<>();//Collections.synchronizedList(new ArrayList<MPoint>());
	public List<MPointPositionCube> mpointsPositions = new ArrayList<>();//Collections.synchronizedList(new ArrayList<MPoint>());
	public List<AutosculptLinking> asLinking = new ArrayList<>();//Collections.synchronizedList(new ArrayList<AutosculptLinking>());
	
	public ArrayList<String> forceAsFixOnParts = new ArrayList<>();
	public ArrayList<Pair<String,String>> renameParts = new ArrayList<>();
	public ArrayList<ArrayList<String>> copyParts = new ArrayList<>();
	public ArrayList<String> deleteParts = new ArrayList<>();
	public ArrayList<RenderPriority> priorities = new ArrayList<>();
	
	
	public String carname = "UNDETERMINED";
	
	public static boolean USE_MULTITHREADING = true;

	public CompressionType defaultCompressionType = CompressionType.RawDecompressed;
	public CompressionLevel defaultCompressionLevel = CompressionLevel.Low;
	
	public boolean LOAD_removeUselessAutosculptParts = false;

	public boolean SAVE_useOffsetsTable = true;
	public boolean SAVE_removeUselessAutosculptParts = true;
	public boolean SAVE_optimizeMaterials = true;
	public boolean SAVE_sortEverythingByName = true;
	public boolean SAVE_fixAutosculptNormals = true;
	public boolean SAVE_removeInvalid = true;
	public boolean SAVE_copyMissingLODs = false;
	public boolean SAVE_copyLOD_D = false;
	public boolean SAVE_protectModel = false;
	public boolean SAVE_processParts = true;
	public boolean SAVE_checkModel = false;
	public boolean SAVE_autoReplaceWorldLODs = true;
	public boolean SAVE_makeDataBlock = false;

	public boolean IMPORT_importVertexColors = true;
	public boolean IMPORT_calculateVertexColors = false;
	public SettingsImport_Tangents IMPORT_Tangents = SettingsImport_Tangents.HIGH;
	public boolean IMPORT_flipV = false;
	
	public Platform platform = Platform.PC;
	
	//---------------------------------------------------------------------------------------------------
	//
	//										CONSTRUCTORS
	//
	//---------------------------------------------------------------------------------------------------
	
	public Geometry(ByteBuffer in) throws Exception {
		in.order(ByteOrder.LITTLE_ENDIAN);
//		in.getInt(); //ID // with this it cannot be read as a normal block
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block.read(in); //skip padding block
		
		// read the header, if this goes wrong the file is probably corrupted or smth
		geomHeader = (GeomHeader) Block.read(in);
		
		if (geomHeader.geomData != null) {
			for (var d : geomHeader.geomData.datas.entrySet()) {
				readConfigLine(d.getKey()+" "+d.getValue(), 0);
			}
		}
		
		if (geomHeader.partsOffsets == null) {
			//decompressed parts in the Geometry without offsets
			SAVE_useOffsetsTable = false;
			SAVE_removeInvalid = false; //this is meant for car geometries
			SAVE_copyMissingLODs = false;
			SAVE_copyLOD_D = false;
			while (in.position() < blockStart+blockLength) {
				parts.add(new Part(in));				
			}
		} else {		
			//load from offsets
			SAVE_useOffsetsTable = true;
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
							
							in.position(o.offset -4); //-4 because we don't have the full file header here
							//loops on the one or multiple compressed blocks
							while (in.position() < o.offset -4 + o.sizeCompressed) {
								CompressedData d = (CompressedData) Block.read(in);
								dataWriter.put(d.decompressionOffset, Compression.decompress(d.data));
							}
							
							dataWriter.position(0);
							parts.add(new Part(dataWriter));	
						} else {
							in.position(o.offset -4);
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

			carname = null;
			for (var p : parts) {
				try {
					if (carname == null) {
						if (p.header.partName.contains("_KIT")) carname = p.header.partName.split("_KIT")[0];
					} else {
						//check that each part has the same car name
						if (p.header.partName.contains("_KIT")) {
							if (!carname.equals(p.header.partName.split("_KIT")[0])) {
								carname = null;
								break;
							}
						}
						
					}
				} catch (@SuppressWarnings("unused") Exception e) {
					//header is null
				}
			}
			if (carname == null) {
//				System.out.println("Could not determine car name.");
				carname = "UNDETERMINED";
			}
		}
			
		updateHashes();
		
		
		if (parts.size()>0) if (parts.get(0).mesh != null) platform = parts.get(0).mesh.platform;
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
			if (geomHeader.partsOffsets != null) parts.sort(new PartSorterLodKitName()); //not necessary but makes the file look cleaner
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
	
	public static Geometry load(File f) throws Exception {
		FileInputStream fis = new FileInputStream(f);
		byte [] arr = new byte[(int)f.length()-4];
		fis.skipNBytes(4); //if this method is called on a file, we assume that the file is a geometry, therefore the first blockid can be skipped
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
		//process stuff on parts
		if (SAVE_processParts) processParts();
		if(SAVE_checkModel) checkModel();
		if (!SAVE_makeDataBlock) {
			geomHeader.subBlocks.remove(geomHeader.geomData);
			geomHeader.geomData = null;
		}
		//sort things once again just in case
		if (SAVE_sortEverythingByName) {
			if (SAVE_useOffsetsTable) parts.sort(new PartSorterLodKitName());
			materials.sort(new MaterialsSorterName());
			for (var mpc : mpointsPositions) mpc.mpoints.sort(new MPointSorterName());
			mpointsPositions.sort(new MPointPosSorterName());
		}
		if (!SAVE_useOffsetsTable) parts.sort(new PartSorterBinKey());

		System.out.println("Geometry checked and prepared in "+(System.currentTimeMillis()-t)+" ms.");
		t = System.currentTimeMillis();
		
		// first compresses all parts to get their compressed and decompressed size
		// using RFPK compression but no blocks

		if (SAVE_useOffsetsTable && defaultCompressionType != CompressionType.RawDecompressed) { //compress parts (eg traffic is not compressed)
			System.out.println("Compressing parts...");
			if (USE_MULTITHREADING) {
				ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				System.out.println("Using "+Runtime.getRuntime().availableProcessors()+" CPUs");
				
				for (final var p : parts){
				    pool.execute(() -> {
						try {
							p.precompress(this);
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
					p.precompress(this);
					System.out.print("\rProgress " + Math.round(100*((float)(parts.indexOf(p))/parts.size()))+ " %" );
				}
			}
			System.out.println("\nParts compressed in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
		}
			
		geomHeader.refresh(this);
		
		if (!SAVE_useOffsetsTable) {
			geomHeader.subBlocks.remove(geomHeader.partsOffsets);
			geomHeader.geomInfo.const02 = 0;
			geomHeader.geomInfo.const21 = 0;
			geomHeader.geomInfo.const22 = 0;
			geomHeader.geomInfo.const23 = 0;
			geomHeader.geomInfo.const24 = 0;
			geomHeader.subBlocks.add(new Geom_Unknown());
		}
		
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]); //16
		buf.order(ByteOrder.LITTLE_ENDIAN);
		if (SAVE_protectModel) buf.putInt(BlockType.Geom_Header.getKey());
		else buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later
		out.write(buf.array());

		out.write(Padding.makePadding(currentPosition+out.size(), 16));
		
		var headerPos = out.size();
		
		out.write(geomHeader.save(0)); //temporary without offsets

		
		for (var p : parts) {
			if (SAVE_useOffsetsTable) Padding.makePadding(out);
			if (SAVE_useOffsetsTable) geomHeader.partsOffsets.setOffset(p, out.size());
			if (SAVE_useOffsetsTable && this.defaultCompressionType != CompressionType.RawDecompressed)	{
				out.write(new CompressedData(p.compressedData, p.decompressedLength, 0, 0).save(0)); //decompOffset and suppChunkOffset both set to 0 because no chunks
			} else {
				var length = out.size();
				out.write(p.save(currentPosition+out.size()));
				if (SAVE_useOffsetsTable) geomHeader.partsOffsets.setLengths(p, out.size()-length);
			}
		}
	
		buf = ByteBuffer.wrap(out.toByteArray());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.position(4);
		buf.putInt(out.size()-8); //size

		buf.position(headerPos);
		if (SAVE_useOffsetsTable) buf.put(geomHeader.save(0)); //save the header again, this time with correct offsets

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
		
		if (!carname.equals("UNDETERMINED")) bw.write("=== CONFIGURATION FILE FOR CAR "+carname+" ===\n");
		else bw.write("=== CONFIGURATION FILE  ===\n");
		bw.write("UCGT v"+GeometryEditorCLI.programVersion+" by needeka\n"
				+ "\n--- Settings ---\n");
		if (!carname.equals("UNDETERMINED")) bw.write("SETTING	CarName="+this.carname+"\n");
		switch (platform) {
		case PC:
			break;
		case X360:
			bw.write("SETTING	Platform=X360\n");
			break;
		default:
			break;
		}
		if (SAVE_useOffsetsTable) bw.write("SETTING	UseMultithreading="+USE_MULTITHREADING+"\n"
				+ "SETTING	CompressionType="+defaultCompressionType+"\n"
				+ "SETTING	CompressionLevel="+defaultCompressionLevel.getName()+"\n");
		else bw.write("SETTING	UseOffsetsTable=false\n");
		bw.write("SETTING	VertexColors=");
		if (IMPORT_importVertexColors) bw.write("Import\n");
		else if (IMPORT_calculateVertexColors) bw.write("Calculate\n");
		else bw.write("Off\n");
		bw.write("SETTING	Tangents="+IMPORT_Tangents.getName()+"\n");
		if (SAVE_useOffsetsTable) bw.write("SETTING	RemoveUselessAutosculpt="+SAVE_removeUselessAutosculptParts+"\n");
		bw.write("SETTING	OptimizeMaterials="+SAVE_optimizeMaterials+"\n");
		if (SAVE_useOffsetsTable) bw.write("SETTING	FixAutosculptNormals="+SAVE_fixAutosculptNormals+"\n");
		bw.write("SETTING	RemoveInvalid="+SAVE_removeInvalid+"\n");
		if (SAVE_useOffsetsTable) bw.write("SETTING	CopyMissingLODs="+SAVE_copyMissingLODs+"\n");
		if (SAVE_useOffsetsTable) bw.write("SETTING	MakeLodD="+SAVE_copyLOD_D+"\n");
		if (SAVE_useOffsetsTable) bw.write("SETTING	CheckModel=true\n");
		if (!geomHeader.geomInfo.filename.equals("GEOMETRY.BIN") && !geomHeader.geomInfo.filename.contains("NFS-CarToolkit") && !geomHeader.geomInfo.filename.contains("Compiled with UCGT")) bw.write("SETTING	FileName="+geomHeader.geomInfo.filename+"\n");
		if (!geomHeader.geomInfo.blockname.equals("DEFAULT") && !geomHeader.geomInfo.blockname.equals("TOOLKIT")) bw.write("SETTING	BlockName="+geomHeader.geomInfo.blockname+"\n");
		
		
		bw.write("\n--- Materials ---\n");
		//MATERIAL	MATNAME	MATSHADER=ShaderUsage[...]	defTex=DEFAULTTEXTURE	flags=0XFLAGS000	DIFFUSE=DIFFUSE_TEX	...
		for (var m : materials) {
			bw.write(m.toConfig(carname)+"\n");
		}
		
		if (mpointsPositions.size() != 0) {
			bw.write("\n--- Position markers ---\n");
			for (var mpc : mpointsPositions) for (var mp : mpc.mpoints) bw.write(mp.toConfig());
		}
		
		var hasAsLinking = false;
		for (var p : parts) if (p.asLinking != null) {
			hasAsLinking = true;
			break;
		}
		if (hasAsLinking) {
			bw.write("\n--- Autosculpt links ---\n"); //only links, autosculpt zones can be automated and renaming them really would be pointless
			for (var p : parts) if (p.asLinking != null) bw.write(p.asLinking.toConfig(this, p)+"\n");
		}
		
		bw.close();
		System.out.println("Configuration file written");
	}
	
	public static void dumpStream(String stream, String dumpfolder, String format, List<String> filters) throws FileNotFoundException, IOException {
		// DUMP STREAM FUNCTION \/
		
		Files.createDirectories(Paths.get(dumpfolder));
		
		File f = new File(stream);
		FileInputStream fis = new FileInputStream(f);
		int blockIndex=0;
		long inPos=0;
		
		//work on large files, don't put everything in memory
		while (fis.available() >= 8) {	//using blockIndex < 20 to read only a few, correct to read the whole file is fis.available() >= 8
			//read next block ID and length
			byte[] ID = new byte[4];
			byte[] length = new byte[4];
			fis.read(ID);
			fis.read(length);
			int blockID = Byte.toUnsignedInt(ID[0]) | 
					(Byte.toUnsignedInt(ID[1]) << 8) |
					(Byte.toUnsignedInt(ID[2]) << 16) |
					(Byte.toUnsignedInt(ID[3]) << 24);
			int blockLength = Byte.toUnsignedInt(length[0]) | 
					(Byte.toUnsignedInt(length[1]) << 8) |
					(Byte.toUnsignedInt(length[2]) << 16) |
					(Byte.toUnsignedInt(length[3]) << 24);
			byte[] block = new byte[blockLength+8];
			block[0] = ID[0];
			block[1] = ID[1];
			block[2] = ID[2];
			block[3] = ID[3];
			block[4] = length[0];
			block[5] = length[1];
			block[6] = length[2];
			block[7] = length[3];
			fis.read(block, 8, blockLength);

			if (blockID == BlockType.Geometry.getKey()) {
				var bb = ByteBuffer.wrap(block);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				try {
					var geom = (Geometry) Block.read(bb);
//					System.out.print("\rBlock #"+blockIndex+" : Geometry, length="+blockLength+", position="+inPos+", name="+geom.geomHeader.geomInfo.filename+", block name="+geom.geomHeader.geomInfo.blockname);

					boolean filterRead = true;
					// no filter set, anything is read; filters set, if we don't find one of them in either the file name or the blck name, then we skip
					for (var fil : filters) if (!geom.geomHeader.geomInfo.filename.contains(fil) && !geom.geomHeader.geomInfo.blockname.contains(fil)) {
						filterRead = false;
						break;
					}
					
//					if (filter == null || geom.geomHeader.geomInfo.filename.contains(filter) || geom.geomHeader.geomInfo.blockname.contains(filter)) {
					if (filterRead) {
						geom.writeConfig(new File(dumpfolder+geom.geomHeader.geomInfo.blockname+"-"+geom.geomHeader.geomInfo.filename+".ini"));
						switch (format.toUpperCase()) {
						case "Z3D":
						case "ZMODELER2":
						case "ZM2":
							ZModelerZ3D.save(geom, dumpfolder+geom.geomHeader.geomInfo.blockname+"-"+geom.geomHeader.geomInfo.filename);
							break;
						case "OBJ":
						case "WAVEFRONT":
							WavefrontOBJ.save(geom, dumpfolder+geom.geomHeader.geomInfo.blockname+"-"+geom.geomHeader.geomInfo.filename);
							break;
						default:
							ZModelerZ3D.save(geom, dumpfolder+geom.geomHeader.geomInfo.blockname+"-"+geom.geomHeader.geomInfo.filename);
							WavefrontOBJ.save(geom, dumpfolder+geom.geomHeader.geomInfo.blockname+"-"+geom.geomHeader.geomInfo.filename);
						}
					}
					
				} catch (Exception e) {
					System.out.println("Error loading Geometry in block #"+blockIndex+" at "+inPos+" !");
					e.printStackTrace();
				}
			} 
//			else System.out.print("\rBlock #"+blockIndex+" : "+BlockType.get(blockID)+", ID="+String.format("0x%08X", blockID)+", length="+blockLength+", position="+inPos);
			
			inPos += blockLength + 8;
			blockIndex++;
		}
		
		fis.close();
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
	
	public static void replaceInStream(String modelsDirectory, String streamFile, String l8rFile) throws Exception {
		// REPLACE IN STREAM \/
		
//		if (!modelsDirectory.endsWith("\\")) modelsDirectory += "\\";
		
		new File(streamFile).renameTo(new File(streamFile.replace(".BUN", "").replace(".BIN", "")+".OLD")); //i'll read this file and transfer the data to a new file as it is read, to lower RAM usage which already gets high just with cars
		if (l8rFile != null) new File(l8rFile).renameTo(new File(l8rFile.replace(".BUN", "").replace(".BIN", "")+".OLD"));
		
		//eg X0 , eLabScenery_XOs_Sawhorse_1.bin -> "C:\Users\NI240SX\Documents\NFS\a MUCP\UCGT\STREAML8R_MW2 recompiled\X0-eLabScenery_XOs_Sawhorse_1.bin.z3d"

		/*
		 * AUTO REPLACE ALL LODS
		 * objects in A-F : "DEINST", only max lod 1A_00
		 * A-F is mostly chops and scenery without LODs
		 * A = L8_A
		 * B = L8_B
		 * C = L8_C
		 * D = L8_D
		 * E = L8_Interstate
		 * F = L8_FE
		 * 
		 * objects : letter = detail, some objects may not be found in lower lods ! eg SawHorse only in Y and X
		 * U = use 1B_00 (fallback to 1A_00 if it doesn't exist) and 1Z_00 (no fallback), except XOs (keep everything)
		 * W = use 1B_00 (fallback to 1A_00 if it doesn't exist) and 1Z_00 (no fallback), except XOs (keep everything)
		 * X = use 1B_00 (fallback to 1A_00 if it doesn't exist) and 1Z_00 (no fallback), except XOs (keep everything)
		 * Y = all LODs
		 * 
		 * Z = all LODs, only Z0
		 * 
		 */
		
		//new approach
		//actually load the geometries to save in memory
		//make a list of chunks to visit in the order they're saved in the file
		//skip directly to each of these blocks and save it back with proper padding and shit
		//for LODs, backup references to parts besides the geom and delete some from the list in the geom if needed, save, then add them back for the next round
		

//		HashMap<Pair<String,String>,ReplaceInStream> geometriesToReplace = new HashMap<>();
		HashMap<Pair<String,String>,Geometry> binBlocksToReplace = new HashMap<>();
		
		if (new File (modelsDirectory).isDirectory()) {
			var filesInDir = new File (modelsDirectory).list();
			for (var s : filesInDir) {
				s = s.toLowerCase();
				try {
					// decompiled 3D models
					if (s.endsWith(".ini")) {
						Geometry g;
						if (new File(modelsDirectory + s.replace(".ini", ".z3d")).exists()) {
							//check the INI for chunk and file name
							System.out.println("INI and Z3D found : "+s);
							g = Geometry.importFromFile(new File(modelsDirectory + s.replace(".ini", ".z3d")));
							binBlocksToReplace.put(
									new Pair<>(g.geomHeader.geomInfo.blockname, g.geomHeader.geomInfo.filename),
									g
									);
						} else if (new File(modelsDirectory + s.replace(".ini", ".obj")).exists()) {
							System.out.println("INI and OBJ found : "+s);
							g = Geometry.importFromFile(new File(modelsDirectory + s.replace(".ini", ".obj")));
							binBlocksToReplace.put(
									new Pair<>(g.geomHeader.geomInfo.blockname, g.geomHeader.geomInfo.filename),
									g
									);
						}
					}
					
					// compiled Geometries and TPKs
					if (s.endsWith(".bin") || s.toLowerCase().endsWith(".tpk")) {
						var binFile = new File(modelsDirectory + s);
						var fis = new FileInputStream(binFile);
						var binFileToBytes = new byte[(int)binFile.length()];
						fis.read(binFileToBytes);
						fis.close();
						var bb = ByteBuffer.wrap(binFileToBytes);
						bb.order(ByteOrder.LITTLE_ENDIAN);

						Block b;
						while (bb.hasRemaining()) {
							b = Block.read(bb);
							if (b.getClass() == Geometry.class) {
								Geometry g = (Geometry) b;
								binBlocksToReplace.put(
										new Pair<>(g.geomHeader.geomInfo.blockname, g.geomHeader.geomInfo.filename),
										g
										);
							} // TODO support replacing other shit
							
						}	
					}
				} catch (Exception e) {
					System.out.println("Error reading file "+s+" : "+e.getMessage());
					e.printStackTrace();
				}
			}
			
		} else {
			throw new Exception("The given models location isn't a directory !");
		}
		

		HashMap<Pair<String,String>,Geometry> merge = new HashMap<>();
		for (var p : binBlocksToReplace.entrySet()) {
			if(p.getValue().SAVE_autoReplaceWorldLODs && (p.getKey().getKey().startsWith("U") || p.getKey().getKey().startsWith("W") || p.getKey().getKey().startsWith("X") || p.getKey().getKey().startsWith("Y"))) {
				merge.put(new Pair<>("U"+p.getKey().getKey().substring(1), p.getKey().getValue()), p.getValue());
				merge.put(new Pair<>("W"+p.getKey().getKey().substring(1), p.getKey().getValue()), p.getValue());
				merge.put(new Pair<>("X"+p.getKey().getKey().substring(1), p.getKey().getValue()), p.getValue());
				merge.put(new Pair<>("Y"+p.getKey().getKey().substring(1), p.getKey().getValue()), p.getValue());
			}
		}
		for (var p : merge.entrySet()) {
			binBlocksToReplace.put(p.getKey(), p.getValue());
		}
		merge = null;
		ArrayList<String> blocksToEdit = new ArrayList<>();
		for (var p : binBlocksToReplace.keySet()) {
			blocksToEdit.add(p.getKey());
		}
		
		

		File inputFile = new File(streamFile.replace(".BUN", "").replace(".BIN", "")+".OLD");
		File outputFile = new File(streamFile);
		
		File outputL8rFile = null;
		ArrayList<Block> l8rBlocks = null;
		StreamBlocksOffsets offsets = null;
		if (l8rFile != null) {
			File inputL8rFile = new File(l8rFile.replace(".BUN", "").replace(".BIN", "")+".OLD");
			outputL8rFile = new File(l8rFile);
			FileInputStream fisL8r = new FileInputStream(inputL8rFile);
			byte[] L8rToBytes = new byte[(int) inputL8rFile.length()];
			fisL8r.read(L8rToBytes);
			fisL8r.close();
			
			l8rBlocks = new ArrayList<>();
			offsets = null;
			var l8rBB = ByteBuffer.wrap(L8rToBytes);
			l8rBB.order(ByteOrder.LITTLE_ENDIAN);
			while (l8rBB.remaining()>8) {
				var b = Block.read(l8rBB);
				l8rBlocks.add(b);
				if (b.getBlockID() == BlockType.StreamBlocksOffsets) {
					offsets = (StreamBlocksOffsets) b;
				}
			}
			l8rBB = null;
			L8rToBytes = null;
			if (offsets == null) {
				throw new Exception("Couldn't find block offsets in the provided L8R file !");
			}
		}
		
		
		
		//we have the names of the models to replace, now it's time to iterate over the stream blocks and find the ones to edit
		
		FileInputStream fisStream = new FileInputStream(inputFile);
		FileOutputStream fosStream = new FileOutputStream(outputFile);
		
//		int totalAddedOffset = 0;
		long currentPositionIn = 0;
		long currentPositionOut = 0;
		
		if (offsets != null) for (var chunk : offsets.chunkInfos) {
			
			if (currentPositionIn < chunk.offset) { //if there was preexisting padding
				currentPositionIn += fisStream.skip(chunk.offset - currentPositionIn);

				byte[] outBlock = Padding.makePadding(currentPositionOut, 2048);
				fosStream.write(outBlock);
				currentPositionOut += outBlock.length;
			}
			chunk.offset = (int) currentPositionOut;

			if (blocksToEdit.contains(chunk.name) 
//					|| "C132".equals(chunk.name)
					) { //need to edit this chunk
//			if (true) {
				
//				if (chunk.name.equals("Y2")) chunk = null;
				
				System.out.println("Editing chunk "+chunk.name
//						+" at "+currentPositionIn+" in, length "+chunk.length1
						);
				byte[] chunkBytes = new byte[chunk.length1];
				fisStream.read(chunkBytes);
				currentPositionIn += chunk.length1;
				int posInChunk = 0;
				
				var bb = ByteBuffer.wrap(chunkBytes);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				while (bb.hasRemaining()) {
//					var blockStart = bb.position();
//					System.out.print("reading block at "+blockStart);

					//tried to keep old geoms intact but for some reason they need to be recompiled or the game crashes... sad, this fucks with LODs for some reason
//					if (binBlocksToReplace.get(Geometry.findBlockAndNameInGeomOrTPK(bb)) == null) {
//						Block.doNotRead.put(BlockType.Geometry, false);
//					} else {
//						Block.doNotRead.remove(BlockType.Geometry);
//					}
//					bb.position(blockStart);
					Block b = Block.read(bb);
					
//					System.out.println(", ID="+(b.getBlockID() == BlockType.INVALID ? String.format("0x%08X", ((UnknownBlock)b).ID) : b.getBlockID())+", end at "+bb.position()+", length "+(bb.position()-blockStart));
					if (b.getClass() == Geometry.class) { // check the need to replace the geometry
						Geometry g = (Geometry) b;
						byte[] outGeom;
						Geometry newGeom = binBlocksToReplace.get(new Pair<>(g.geomHeader.geomInfo.blockname, g.geomHeader.geomInfo.filename));
						if (newGeom != null) {
							//use 1B_00 (fallback to 1A_00 if it doesn't exist) and 1Z_00 (no fallback), except XOs (keep everything)
							Part lodA = null, lodB = null;
							
							newGeom.geomHeader.geomInfo.blockname = chunk.name;
							if (newGeom.SAVE_autoReplaceWorldLODs && (chunk.name.startsWith("X") || chunk.name.startsWith("W") || chunk.name.startsWith("U")) && !newGeom.geomHeader.geomInfo.filename.contains("XOs")) {
								for (var p : newGeom.parts) {
									if (p.name.endsWith("1A_00")) lodA = p;
									if (p.name.endsWith("1B_00")) lodB = p;
								}
								if (lodB != null && lodA != null) {
//									System.out.println("removing stuff");
									newGeom.parts.remove(lodA);
//									for (var p : newGeom.parts) System.out.println(p.name);
								}
							}
							outGeom = newGeom.save(posInChunk);
							if (newGeom.SAVE_autoReplaceWorldLODs && (chunk.name.startsWith("X") || chunk.name.startsWith("W") || chunk.name.startsWith("U")) && !newGeom.geomHeader.geomInfo.filename.contains("XOs")) {
								if (lodB != null && lodA != null) newGeom.parts.add(0,lodA);
							}
						} else {
							outGeom = g.save(posInChunk);
						}
						fosStream.write(outGeom);
						currentPositionOut += outGeom.length;
						posInChunk += outGeom.length;
//					} else if (b.getClass() == Padding.class){
						// do nothing, TODO padding is still not it (or is it geometry ???)
					} else {
						byte[] outBlock = b.save(posInChunk);
						fosStream.write(outBlock);
						currentPositionOut += outBlock.length;
						posInChunk += outBlock.length;
					}

					// make padding after each block read
//					byte[] outBlock = Padding.makePadding(posInChunk, 128);
//					fosStream.write(outBlock);
//					currentPositionOut += outBlock.length;
//					posInChunk += outBlock.length;

					chunk.length1 = (int) (currentPositionOut - chunk.offset);
					chunk.length2 = chunk.length1;
				}
				
			} else { //no need to edit this block, just pump and update offsets
				
//				System.out.println("Chunk "+chunk.name+" at "+currentPositionIn+ " length "+chunk.length1);
				var arr = new byte[chunk.length1];
				if (fisStream.read(arr) < chunk.length1) {
					System.out.println("something went wrong at "+currentPositionIn+" in chunk "+chunk.name);
					chunk = null;
				}
				currentPositionIn += chunk.length1;
				fosStream.write(arr); 
				fosStream.flush();
				currentPositionOut += chunk.length1;
				arr = null;
				
			}
			fosStream.flush();
			
		} else { //working on a single file without chunk offsets
			
			
			byte[] chunkBytes = new byte[fisStream.available()];
			fisStream.read(chunkBytes);
			int posInChunk = 0;
			
			var bb = ByteBuffer.wrap(chunkBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			while (bb.hasRemaining()) {
//				var blockStart = bb.position();
//				System.out.print("reading block at "+blockStart);

//				Block.doNotRead.put(BlockType.Padding, false);
				Block b = Block.read(bb);
				
//				System.out.println(", ID="+(b.getBlockID() == BlockType.INVALID ? String.format("0x%08X", ((UnknownBlock)b).ID) : b.getBlockID())+", end at "+bb.position()+", length "+(bb.position()-blockStart));
				if (b.getClass() == Geometry.class) { // check the need to replace the geometry
					Geometry g = (Geometry) b;
					byte[] outGeom;
					if (binBlocksToReplace.get(new Pair<>(g.geomHeader.geomInfo.blockname, g.geomHeader.geomInfo.filename)) != null) {
						outGeom = binBlocksToReplace.get(new Pair<>(g.geomHeader.geomInfo.blockname, g.geomHeader.geomInfo.filename)).save(posInChunk);
					} else {
						outGeom = g.save(posInChunk);
					}
					fosStream.write(outGeom);
					currentPositionOut += outGeom.length;
					posInChunk += outGeom.length;
				} else if (b.getClass() == Padding.class){
					// do nothing, TODO padding is still not it (or is it geometry ???)
				} else {
					byte[] outBlock = b.save(posInChunk);
					fosStream.write(outBlock);
					currentPositionOut += outBlock.length;
					posInChunk += outBlock.length;
				}

				// make padding after each block read
				byte[] outBlock = Padding.makePadding(posInChunk, 128);
				fosStream.write(outBlock);
				currentPositionOut += outBlock.length;
				posInChunk += outBlock.length;
			}
		} //working on a single file without chunk offsets
		
		fisStream.close();
		fosStream.close();
		
		if (l8rFile != null) {
			FileOutputStream fosL8r = new FileOutputStream(outputL8rFile);
			for (var b : l8rBlocks) {
				fosL8r.write(b.save(0));
			}
			fosL8r.close();
		}
//		System.out.println();
	}
	
	public void readConfig(File f) throws IOException {
		var br = new BufferedReader(new FileReader(f));
		String l;
		int lineNbr=0;
		while ((l=br.readLine())!=null) {
			lineNbr++;
			readConfigLine(l, lineNbr);
		}//loop on config file lines
		br.close();
	}

	public void readConfigLine(String l, int lineNbr) {
		try {
			int iterator;
			switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
			case "SETTING": {
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (s2.contains("=")) {
					if (SAVE_makeDataBlock) this.geomHeader.geomData.datas.put("SETTING",s2);
					switch (s2.split("=")[0]) {
					case "UseMultithreading":
						USE_MULTITHREADING = Boolean.parseBoolean(s2.split("=")[1]);
						if (USE_MULTITHREADING) System.out.println("Now using multi-threading");
						else System.out.println("Now using single-threading");
						break;
						
					case "UseOffsetsTable":
						SAVE_useOffsetsTable = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_useOffsetsTable) System.out.println("Using an offsets table");
						else System.out.println("Not using any offsets table");
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
						
					case "FileName":
						String fileName = s2.split("=")[1];
						if (fileName.length() > 52) fileName = fileName.substring(0, 51);
						System.out.println("File name set : "+fileName);
						geomHeader.geomInfo.filename = fileName;
						break;
					case "BlockName":
						String blockName = s2.split("=")[1];
						if (blockName.length() > 36) blockName = blockName.substring(0, 35);
						System.out.println("Block name set : "+blockName);
						geomHeader.geomInfo.blockname = blockName;
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

					case "AutoReplaceWorldLODs":
						SAVE_autoReplaceWorldLODs = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_autoReplaceWorldLODs) System.out.println("Automatically replacing other world model LODs.");
						break;
						
					case "RemoveInvalid":
						SAVE_removeInvalid = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_removeInvalid) System.out.println("Removing invalid parts.");
						break;
					case "ProtectModel":
						SAVE_protectModel = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_protectModel) System.out.println("Protecting model to prevent easy editing.");
						break;
					case "CheckModel":
						SAVE_checkModel = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_checkModel) System.out.println("Checking model and displaying potential issues.");
						break;
						
					case "MakeDataBlock":
						SAVE_makeDataBlock = Boolean.parseBoolean(s2.split("=")[1]);
						if (SAVE_makeDataBlock) {
							System.out.println("Adding an UCGT metadata block.");
							geomHeader.geomData = new UCGTData();
							geomHeader.subBlocks.add(geomHeader.geomData);
						}
						break;
						
					case "Platform":
						if (s2.split("=")[1].equals("PC")) {
							platform = Platform.PC;
							System.out.println("Using PC platform");
						}
						else if (s2.split("=")[1].equals("X360") || s2.split("=")[1].equals("XBOX360") || s2.split("=")[1].equals("XBOX") || s2.split("=")[1].equals("XENON")) {
							platform = Platform.X360;
							System.out.println("Using X360 platform");
						} else {
							platform = Platform.PC;
							System.out.println("Invalid platform, defaulting to PC. Valid platforms: PC, X360");
						}
						break;
						
					default:
						System.out.println("Setting not supported : "+s2);
					}
				}
				break;}

			case "RENAME":
				iterator = 0;
				String toren = null;
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
					switch(iterator) {
					case 1:
						toren = s2;
						break;
					case 2:
						renameParts.add(new Pair<>(toren, s2));
						break;
					}
					iterator++;
				}
				break;

			case "COPY":
				ArrayList<String> toCopy = new ArrayList<>();
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("COPY")) {
					toCopy.add(s2);
				}
				if (toCopy.size()>1) copyParts.add(toCopy);
				break;
				
			case "DELETE":
				iterator = 0;
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
					switch(iterator) {
					case 1:
						deleteParts.add(s2);
						break;
					}
					iterator++;
				}
				break;
				
			case "MATERIAL": // IF THE CAR NAME ISN'T SET BEFORE THIS, CAR-SPECIFIC TEXTURES WILL BREAK
				var m = new Material(this, l);
				materials.add(m);
				break;
				
			case "MARKER":
				var mp = new MPoint();
				iterator = 0;
				float u=0, v=0, w=0; //euler angles
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MARKER")) {
					switch (iterator) {
					case 0:
						mp.uniqueName = s2;
						break;
					case 1:
						mp.nameHash = Hash.findBIN(s2);
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
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("ASLINK")) {
					if (!s2.contains(",")) { //part name
						asl.tempPartName = s2;
					} else {
						asl.links.add(new AutosculptLink(Hash.findBIN(carname+"_"+s2.split(",")[0]), 
								Short.parseShort(s2.split(",")[1]), 
								Short.parseShort(s2.split(",")[2]), 
								Short.parseShort(s2.split(",")[3]), 
								Short.parseShort(s2.split(",")[4])  ));
					}
				}
				AutosculptLinking existingLink = null;
				for (var link : asLinking) {
					if (link.tempPartName.equals(asl.tempPartName)) {
						existingLink = link;
						break;
					}
				}
				if (existingLink == null) asLinking.add(asl);
				else {
					for (var lk : asl.links) existingLink.links.add(lk); //merge duplicates
				}
//				System.out.println("Autosculpt link : "+l);
				break;
				
			case "PRIORITY":
				System.out.println("WARNING: PRIORITY is a deprecated feature, please set texture priority directly in materials.");
				var prio = new RenderPriority();
				priorities.add(prio);
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("PRIORITY")) {
					if (!s2.contains("=")) { //part name
						prio.partName = s2;
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
				
			case "SHADERUSAGE": //define a custom shader usage in the config
				if (SAVE_makeDataBlock) this.geomHeader.geomData.datas.put("SHADERUSAGE",l.substring(12));
				if (!ShaderUsage.isLoaded) ShaderUsage.updateUsages();
				ShaderUsage.parseUsage(l.substring(12));
				break;
			}
		} catch (Exception e) {
			System.out.println("WARNING : Error reading configuration file, line "+lineNbr+". Trying to compile nonetheless.");
			System.out.println("> "+l);
			e.printStackTrace();
		}
	}

	public static void ctkConfigToUCGTConfig(File ctkConfig, File config) throws IOException {
		var br = new BufferedReader(new FileReader(ctkConfig));
		
		var bw = new BufferedWriter(new FileWriter(config));
		
		bw.write("=== CONFIGURATION FILE ===\n"
				+ "UCGT v"+GeometryEditorCLI.programVersion+" by needeka\n"
				+ "Converted from a CTK model - please fill in what's missing and check what's already filled in !\n"
				+ "\n--- Settings ---\n");
		bw.write("SETTING	CarName=MISSING\n"
				+ "SETTING	UseMultithreading=true\n"
				+ "SETTING	CompressionType=RawDecompressed\n"
				+ "SETTING	CompressionLevel=Maximum\n"
				+ "SETTING	VertexColors=Calculate\n"
				+ "SETTING	Tangents=High\n"
				+ "SETTING	FlipV=true\n"
				+ "SETTING	RemoveUselessAutosculpt=true\n"
				+ "SETTING	OptimizeMaterials=true\n"
				+ "SETTING	FixAutosculptNormals=true\n"
				+ "SETTING	RemoveInvalid=true\n"
				+ "SETTING	CopyMissingLODs=true\n"
				+ "SETTING	CheckModel=true\n"
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
				ShaderUsage usage = ShaderUsage.get("Diffuse");
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
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "DECAL";
						texture = "BADGING_UNIVERSAL";
						break;
					case "UC_BRAKELIGHT":
						usage = ShaderUsage.get("DiffuseGlow");
						shader = "BRAKELIGHT";
						texture = "%_KIT00_BRAKELIGHT_OFF";
						glow = "%_KIT00_BRAKELIGHT_ON";
						break;
					case "UC_BRAKELIGHTGLASS":
						usage = ShaderUsage.get("DiffuseGlowAlpha");
						shader = "BRAKELIGHTGLASS";
						texture = "%_KIT00_BRAKELIGHT_GLASS_OFF";
						glow = "%_KIT00_BRAKELIGHT_GLASS_ON";
						break;
					case "UC_BRAKELIGHTGLASSRED":
						usage = ShaderUsage.get("DiffuseGlowAlpha");
						shader = "BRAKELIGHTGLASSRED";
						texture = "%_KIT00_BRAKELIGHT_GLASS_OFF";
						glow = "%_KIT00_BRAKELIGHT_GLASS_ON";
						break;
					case "UC_DECAL":
						usage = ShaderUsage.get("DiffuseNormalAlpha");
						shader = "DECAL";
						texture = "%_BADGING";
						normal = "%_BADGING_N";
						break;
					case "UC_DEFROSTER":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "DEFROSTER";
						texture = "REAR_DEFROSTER";
						break;			
					case "UC_HEADLIGHTGLASS":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "HEADLIGHTGLASS";
						texture = "%_KIT00_HEADLIGHT_GLASS_ON";
						break;		
					case "UC_PAINT":
						usage = ShaderUsage.get("DiffuseNormalSwatch");
						shader = "CARSKIN";
						if ("METAL_SWATCH".equals(texture)) swatch = "METAL_SWATCH"; else swatch = "%_SKIN1";
						texture = "CARBONFIBRE_PLACEHOLDER";
						normal = "DAMAGE_N";
						break;
					case "UC_WHEEL":
						usage = ShaderUsage.get("Diffuse");
						shader = "MAGSILVER";
						texture = "%_WHEEL";
						break;
					case "UC_WHEEL_RUBBER":
						usage = ShaderUsage.get("DiffuseNormalAlpha");
						shader = "RUBBER";
						texture = "TIRE_STYLE01";
						normal = "TIRE_STYLE01_N";
						break;
					case "UC_WINDOW_FRONT":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "WINDSHIELD";
						texture = "WINDOW_FRONT";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_LEFT_FRONT":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "WINDSHIELD";
						texture = "WINDOW_LEFT_FRONT";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_RIGHT_FRONT":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "WINDSHIELD";
						texture = "WINDOW_RIGHT_FRONT";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_REAR":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "WINDSHIELD";
						texture = "WINDOW_REAR";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_LEFT_REAR":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "WINDSHIELD";
						texture = "WINDOW_LEFT_REAR";
						swatch = "%_SKIN1";
						break;
					case "UC_WINDOW_RIGHT_REAR":
						usage = ShaderUsage.get("DiffuseAlpha");
						shader = "WINDSHIELD";
						texture = "WINDOW_RIGHT_REAR";
						swatch = "%_SKIN1";
						break;
					}
				} else {
					//real shader compiled with Diffuse
					if (normal != null) usage = ShaderUsage.get("DiffuseNormal");
					if (shader.equals("BRAKEDISC")) usage = ShaderUsage.get("DiffuseAlpha");
					if (shader.equals("DOORLINE") && normal != null) usage = ShaderUsage.get("DiffuseNormalAlpha");
					if (shader.equals("HEADLIGHTREFLECTOR")) {
						usage = ShaderUsage.get("DiffuseGlow");
						glow = texture;
					}
//					if (texture.equals("TIRE_STYLE01")) usage = ShaderUsage.DiffuseNormalAlpha;
					if ("%_BADGING".equals(texture) && normal != null) usage = ShaderUsage.get("DiffuseNormalAlpha");
					if ("DAMAGE_N".equals(normal)) usage = ShaderUsage.get("DiffuseNormalSwatch");
					//vanilla plus
//					if (texture.equals("GRILL_02")) usage = ShaderUsage.DiffuseAlpha; 
//					if (texture.equals("%_ENGINE")) {
//						usage = ShaderUsage.DiffuseNormal;
//						normal = "%_ENGINE_N";
//					}
				}
				
				bw.write("MATERIAL	"+mat+"	"+shader+"="+usage.getName()+"	"+texture+"=DIFFUSE");
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
				//	rotations fix
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

	public static Pair<String,String> findBlockAndNameInConfig(File f) throws IOException {
		var br = new BufferedReader(new FileReader(f));
		String l;
		String blockname = null;
		String filename = null;
		while ((l=br.readLine())!=null) {
			switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
			case "SETTING": {
				for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (s2.contains("=")) {
					switch (s2.split("=")[0]) {
					case "FileName":
						String fileName = s2.split("=")[1];
						if (fileName.length() > 52) fileName = fileName.substring(0, 51);
						System.out.println("File name : "+fileName);
						filename = fileName;
						break;
					case "BlockName":
						String blockName = s2.split("=")[1];
						if (blockName.length() > 36) blockName = blockName.substring(0, 35);
						System.out.println("Block name : "+blockName);
						blockname = blockName;
						break;
					}
				}
				break;}

			}
		}//loop on config file lines
		br.close();
		return new Pair<>(blockname, filename);
	}
	
	//---------------------------------------------------------------------------------------------------
	//
	//									 HELPER FUNCTIONS
	//
	//---------------------------------------------------------------------------------------------------

	public void processParts() {
		
		var toRemove = Collections.synchronizedList(new ArrayList<Part>()); // parts flagged to be removed to optimize the geometry (eg T0 autosculpt with no other actual morphtargets)
		
		// RENAME moved to part constructor
		
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
			HashMap<Integer, Byte> shaderUsageIDs; //first int is either shader usage or shader hash
			shaderUsageIDs = new HashMap<>();
			
			if (SAVE_removeInvalid) {if (!checkValid(toRemove, p)) continue;}
			optimizeAutosculpt(toRemove, p); 
			computeMatsList(p); // IMPORTANT TO KEEP HERE
//			globalizePartMarkers(p);
			if (SAVE_optimizeMaterials && p.strings != null) {
				p.subBlocks.remove(p.strings);
				p.strings = null;
			}
			if (p.mesh != null) {				
				for (var m : p.mesh.materials.materials) if (m.renderingOrder!=0) shaderUsageIDs.put(m.ShaderHash , m.renderingOrder);
				for (var m : p.mesh.materials.materials) {
					m.tryGuessFEData(shaderUsageIDs);
					if (SAVE_optimizeMaterials) {	
		    			m.removeUnneeded();
		    		} else {
//						m.tryGuessTexturePriority();
//						m.tryGuessFlags(this);
		    			m.tryGuessDefaultTex();
		    		}
				}
				for (var prio : priorities) if (p.name.contains(prio.partName)) {
//					System.out.println("Changing render priorities for part "+p.name);
					for (var m : p.mesh.materials.materials) {
						for (var pair : prio.values) {
							if (m.equals(pair.getKey())) { //for some reason materials are NOT equal (materials are fucked up for some reason and have texture hashes repeated twice)
								//HOW IS IT NOT FINDING THE FUCKING MATERIALS
								m.texturePriorities.clear();
								m.texturePriorities.add(0);
								m.texturePriorities.add(pair.getValue());
//								m.usageSpecific1 = pair.getValue();
							}
						}
					}
				}
			}
			fixAutosculptMeshes(p);
			
		}

		parts.removeAll(toRemove);
		
		var toAdd = Collections.synchronizedList(new ArrayList<Part>()); // parts flagged to be added (eg missing LODs)

		if (SAVE_copyMissingLODs) for (var p : parts) checkAndCopyMissingLODs(toAdd, p);
		// will fuck up LODs for some copied parts if not done here
		parts.addAll(toAdd);
		toAdd.clear();

		if (!copyParts.isEmpty()) {
			for (var p : parts) for (var c : copyParts) if (p.name.contains(c.get(0))) {
				System.out.println("Copying part "+p.name);
				for (int i=1; i< c.size(); i++) {
					toAdd.add(new Part(p, carname, p.name.replace(c.get(0), c.get(i))));
				}
			}
		}
		parts.addAll(toAdd);
		
		for (var p : parts) 
			if (SAVE_protectModel) {
				if (p.header != null) p.header.partName = "";
			}
	}

	@SuppressWarnings("unlikely-arg-type")
	public void globalizePartMarkers(Part p) {
		// GLOBAL MARKERS STORAGE
		if (p.mpoints != null) for (var mp : p.mpoints.mpoints) {
			mp.part = p;
//			mp.tryGuessName(this);
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
			String name = "_"+ Hash.getBIN(mpc.mpoints.get(0).nameHash) + "_" + mpc.mpoints.get(0).part.kit;
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
		if (D != null && A == null && B == null && C == null) return; //ignore copying lod D to A, B and C
		
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
		Hash.addBinHash(carname);
		
		try {
			for (var p : parts) {
				Hash.addBinHash(p.header.partName);
			} 
			
		} catch (@SuppressWarnings("unused") Exception e) {
			//header is null or smth
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data/textures")));
			String tex;
			while ((tex = br.readLine())!=null) if (!tex.isBlank() && !tex.startsWith("#") && !tex.startsWith("//")){
				Hash.addBinHash(carname + "_" + tex);
			}
			
			
			br = new BufferedReader(new FileReader(new File("data/gentextures")));
			while ((tex = br.readLine())!=null) if (!tex.isBlank() && !tex.startsWith("#") && !tex.startsWith("//")){
				Hash.addBinHash(tex);
			}

			br = new BufferedReader(new FileReader(new File("data/shaders")));
			while ((tex = br.readLine())!=null) if (!tex.isBlank() && !tex.startsWith("#") && !tex.startsWith("//")){
				Hash.addBinHash(tex);
			}

			br = new BufferedReader(new FileReader(new File("data/mpoints")));
			while ((tex = br.readLine())!=null) if (!tex.isBlank() && !tex.startsWith("#") && !tex.startsWith("//")){
				Hash.addBinHash(tex);
				for (int as=0; as<11; as++) {
					Hash.addBinHash(tex + "_T" + as);
				}
			}
			
			br = new BufferedReader(new FileReader(new File("data/worldtextures")));
			while ((tex = br.readLine())!=null) if (!tex.isBlank() && !tex.startsWith("#") && !tex.startsWith("//")){
				Hash.addBinHash(tex);
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

        	int numTriangles = 0;
        	int numTrianglesEXTRA = 0;
        	for (var m : p.mesh.materials.materials) {
        		switch (p.mesh.platform) {
        		case PC:
            		m.verticesBlock.vertexFormat = m.shaderUsage.vertexFormat_PC;
        			break;
        		case X360:
            		m.verticesBlock.vertexFormat = m.shaderUsage.vertexFormat_X360;
        			break;
        		}
        		
        		p.mesh.verticesBlocks.add(m.verticesBlock);
        		p.mesh.triangles.triangles.addAll(m.triangles);
        		numTriangles += m.triangles.size();
        		p.mesh.triangles.triangles.addAll(m.trianglesExtra);
        		numTrianglesEXTRA += m.trianglesExtra.size();
        		
        		
        		if (	(IMPORT_Tangents == SettingsImport_Tangents.LOW && m.needsTangentsLow()) || 
    					(IMPORT_Tangents == SettingsImport_Tangents.HIGH && m.needsTangentsHigh()) || 
    					(IMPORT_Tangents == SettingsImport_Tangents.MANUAL && m.useTangents == true) ||
    					IMPORT_Tangents == SettingsImport_Tangents.ON ) {
        			
        			addToTangents(m);
        			normalizeTangents(m.verticesBlock.vertices);
        		}
        		
        		if (IMPORT_calculateVertexColors) {
    				for (var v : m.verticesBlock.vertices) {
    					// -1 to 1 -> 20 to 255
        				int color;
        				if (!p.header.partName.contains("WHEEL") && !p.header.partName.contains("BRAKE_") && !p.header.partName.contains("BRAKEROTOR"))
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
        		if (m.ShaderHash != 0) if (!shadersOnly.containsValue(m.ShaderHash)) {
        			shadersOnly.put(shai, m.ShaderHash);
        			shadersIDs.put(m.ShaderHash, shai);
        			shai++;
        		}
        		if (!allShaderUsages.contains(m.shaderUsage)) allShaderUsages.add(m.shaderUsage);

        		for (int i=0; i<m.TextureHashes.size(); i++) {
        			if (!texturesAndUsage.containsValue(new Pair<>(m.TextureHashes.get(i), m.textureUsages.get(i).getKey()))) {
	        				texturesAndUsage.put(texi, new Pair<>(m.TextureHashes.get(i), m.textureUsages.get(i).getKey()));
	        				texturesIDs.put(new Pair<>(m.TextureHashes.get(i), m.textureUsages.get(i).getKey()), texi);
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
        	p.header.trianglesCount = p.mesh.triangles.triangles.size(); //total with EXTRA ??? or numTriangles
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
        	p.mesh.info.numTriangles = numTriangles;
        	p.mesh.info.numTrianglesExtra = numTrianglesEXTRA;

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
        		triVertI = m.toTriVertID + m.trianglesExtra.size()*3;
    			m.numTriVerticesExtra = m.trianglesExtra.size()*3;
        		
        		
        		int shaderid;
        		if (m.ShaderHash != 0) shaderid = shadersIDs.get(m.ShaderHash);
        		else shaderid = -1;
        		m.shaderID = (byte) shaderid;
        		for (int i=0; i<m.TextureHashes.size(); i++) {
        			int texid = texturesIDs.get(new Pair<>(m.TextureHashes.get(i), m.textureUsages.get(i).getKey()));
        			m.textureIDs.add((byte) texid);
        		}
        		m.verticesDataLength = m.verticesBlock.vertices.size()*m.shaderUsage.vertexFormat_PC.getLength();
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
        IMPORT_flipV = false; //reset
        
	}
	
	private static void addToTangents(Material m) {
		for (var t : m.triangles) {
		    m.verticesBlock.vertices.get(t.vert0).tanX += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posX-m.verticesBlock.vertices.get(t.vert0).posX) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posX-m.verticesBlock.vertices.get(t.vert0).posX)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert0).tanY += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posY-m.verticesBlock.vertices.get(t.vert0).posY) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posY-m.verticesBlock.vertices.get(t.vert0).posY)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert0).tanZ += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posZ-m.verticesBlock.vertices.get(t.vert0).posZ) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posZ-m.verticesBlock.vertices.get(t.vert0).posZ)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert1).tanX += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posX-m.verticesBlock.vertices.get(t.vert0).posX) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posX-m.verticesBlock.vertices.get(t.vert0).posX)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert1).tanY += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posY-m.verticesBlock.vertices.get(t.vert0).posY) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posY-m.verticesBlock.vertices.get(t.vert0).posY)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert1).tanZ += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posZ-m.verticesBlock.vertices.get(t.vert0).posZ) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posZ-m.verticesBlock.vertices.get(t.vert0).posZ)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert2).tanX += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posX-m.verticesBlock.vertices.get(t.vert0).posX) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posX-m.verticesBlock.vertices.get(t.vert0).posX)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert2).tanY += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posY-m.verticesBlock.vertices.get(t.vert0).posY) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posY-m.verticesBlock.vertices.get(t.vert0).posY)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
		    m.verticesBlock.vertices.get(t.vert2).tanZ += 
		    		((m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert1).posZ-m.verticesBlock.vertices.get(t.vert0).posZ) - 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) * 
		    				(m.verticesBlock.vertices.get(t.vert2).posZ-m.verticesBlock.vertices.get(t.vert0).posZ)) / 
		    		((m.verticesBlock.vertices.get(t.vert1).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V) - 
		    				(m.verticesBlock.vertices.get(t.vert2).tex0U-m.verticesBlock.vertices.get(t.vert0).tex0U) * 
		    				(m.verticesBlock.vertices.get(t.vert1).tex0V-m.verticesBlock.vertices.get(t.vert0).tex0V));
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
	        v.tanX = (float) (tx / length);
	        v.tanY = (float) (ty / length);
	        v.tanZ = (float) (tz / length);
	        v.tanW = 0x7FFF/32768.0f;
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
//				pair.getKey().normW = pair.getValue().normW;
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
		        		m.verticesDataLength = m.verticesBlock.vertices.size()*m.shaderUsage.vertexFormat_PC.getLength();
		            }
		        	p2.rebuildSubBlocks();
//    		        	System.out.println(p2.header.partName+" fixed : "+p2.mesh.info.numTriangles+" tris, "+p2.mesh.info.numVertices+" verts.");
				}
			}
		}
	}
	
	private void checkModel() {
		/*
		 * CHECKS TO BE PERFORMED
		 * 
		   vertex bounds
		 * oob carskin uv ?
		   lod D existence only for kit00
		   no lod E and lod D other than kit00
		   lod D materials
		   lod D polycount
		 - lod A polycount (for 54k crash without wheels and model quality below 16k without wheels)
		   window textures (F/R/L/R/DEFROSTER)
		   wrong carskin (the one looking red) CARSKIN & DiffuseSwatch
		   exhaust fx mpoints for each muffler found either on the muffler or on tips
		   license plate mpoint on each bumper
		   common mpoints on base (incl spoiler, engine fx, others)
		   brakes existence and brake discs transparency
		   driver existence
		   brake front marker existence
		   nothing trying to be transparent on wheels
		 * 
		 */
		System.out.println("Checking model...");
		int lodDPartsFound = 0;
		int lodDTris = 0;
		boolean hasFrontBrakes = false;
		boolean hasRearBrakes = false;
		boolean hasFrontDiscs = false;
		boolean hasRearDiscs = false;
		boolean hasDriver = false;
		boolean brakeMarker = false;
		HashMap<String,Integer> lodATris = new HashMap<>();
		for (var p : parts) {
			char char0 = p.part.charAt(p.part.length()-2);
			char char1 = p.part.charAt(p.part.length()-1);
			checkVertexBounds(p);
			if (char0 != 'T' && "E".equals(p.lod)) System.out.println("Useless lod E part: "+p.name+"; lod E is unused in Undercover!");
			if (char0 != 'T' && "D".equals(p.lod)) {
				if (!"KIT00".equals(p.kit)) System.out.println("Useless non-KIT00 lod D part: "+p.name+"; UC only loads KIT00 lod D.");
				else {
					if (((char0 != '0' && char0 != '1' && char0 != '2' && char0 != '3' && char0 != '4' && //numbered parts such as exhausts except _00
						char0 != '5' && char0 != '6' && char0 != '7' && char0 != '8' && char0 != '9') ||
						(char0 == '0' && char1 == '0')) && 
						!p.part.equals("SPOILER_LIP") && !p.part.equals("SPOILER_DRAG") && !p.part.equals("SPOILER_EVO") ){
						lodDPartsFound++;
						lodDTris += p.header.trianglesCount;
					}
				}
			}
			if ("A".equals(p.lod)) {
				if ( char0 != 'T' && //AS morph target
					((char0 != '0' && char0 != '1' && char0 != '2' && char0 != '3' && char0 != '4' && //numbered parts such as exhausts except _00
					char0 != '5' && char0 != '6' && char0 != '7' && char0 != '8' && char0 != '9') ||
					(char0 == '0' && char1 == '0')) && 
					!p.part.equals("SPOILER_LIP") && !p.part.equals("SPOILER_DRAG") && !p.part.equals("SPOILER_EVO") && !p.part.contains("WHEEL")) {
					if (lodATris.get(p.kit) != null) {
						lodATris.put(p.kit, lodATris.get(p.kit)+p.header.trianglesCount);
					} else {
						lodATris.put(p.kit, p.header.trianglesCount);
					}
				}
			}
			if ("BUMPER_REAR".equals(p.part) && char0 != 'T') {
				boolean plate = false;
				if (p.mpoints != null) for (var mp : p.mpoints.mpoints) {
					if (mp.nameHash == Hash.findBIN("LICENSE_PLATE_REAR")) plate = true;
				}
				if (!plate) System.out.println("Missing license plate marker on "+p.name+".");
			}
			if (("MUFFLER".equals(p.part) || "EXHAUST".equals(p.part)) && char0 != 'T') {
				Part ETL = null, ETR = null, ETC = null;
				for (var p2 : parts) {
					if ((p.kit+"_EXHAUST_TIPS_LEFT_"+p.lod).equals(p2.name)) ETL = p2;
					if ((p.kit+"_EXHAUST_TIPS_RIGHT_"+p.lod).equals(p2.name)) ETR = p2;
					if ((p.kit+"_EXHAUST_TIPS_CENTER_"+p.lod).equals(p2.name)) ETC = p2;
				}
				if (p.mpoints == null ||
					(ETL != null && ETR != null && (ETR.mpoints == null || ETL.mpoints == null)) ||
					(ETC != null && ETC.mpoints == null) ||
					(ETL != null && ETL.mpoints == null) ||
					(ETR != null && ETR.mpoints == null) ) 
					System.out.println("Missing EXHAUST_FX marker(s) on "+p.name+" or associated tips.");
			}
			if ("BRAKE_FRONT".equals(p.part)) hasFrontBrakes = true;
			if ("BRAKE_REAR".equals(p.part)) hasRearBrakes = true;
			if ("BRAKEROTOR_FRONT".equals(p.part)) hasFrontDiscs = true;
			if ("BRAKEROTOR_REAR".equals(p.part)) hasRearDiscs = true;
			if ("DRIVER".equals(p.part)) hasDriver = true;
			if (p.part.contains("WHEEL") && p.mpoints != null) brakeMarker = true;
			if ("BASE".equals(p.part)) {
				boolean hasDriverMP = false;
				boolean hasSpoiler = false;
				boolean hasEngineFX = false;
				if (p.mpoints != null) for (var mp : p.mpoints.mpoints) {
					if (mp.nameHash == Hash.findBIN("DRIVER_POSITION")) hasDriverMP = true;
					if (mp.nameHash == Hash.findBIN("SPOILER")) hasSpoiler = true;
					if (mp.nameHash == Hash.findBIN("ENGINE_FX")) hasEngineFX = true;
				}
				if (!hasDriverMP) System.out.println("No DRIVER_POSITION marker found on "+p.name+".");
				if (!hasSpoiler) System.out.println("No SPOILER marker found on "+p.name+".");
				if (!hasEngineFX) System.out.println("No ENGINE_FX marker found on "+p.name+".");
			}
			
			boolean hasWindowF = false;
			boolean hasWindowFL = false;
			boolean hasWindowFR = false;
			boolean hasWindowR = false;
			boolean hasWindowRL = false;
			boolean hasWindowRR = false;
			boolean hasDefroster = false;
			if (char0 != 'T') for (var m : p.mesh.materials.materials) {
				if (m.TextureHashes.contains(Hash.findBIN("WINDOW_FRONT"))) hasWindowF = true;
				if (m.TextureHashes.contains(Hash.findBIN("WINDOW_LEFT_FRONT"))) hasWindowFL = true;
				if (m.TextureHashes.contains(Hash.findBIN("WINDOW_RIGHT_FRONT"))) hasWindowFR = true;
				if (m.TextureHashes.contains(Hash.findBIN("WINDOW_REAR"))) hasWindowR = true;
				if (m.TextureHashes.contains(Hash.findBIN("WINDOW_LEFT_REAR"))) hasWindowRL = true;
				if (m.TextureHashes.contains(Hash.findBIN("WINDOW_RIGHT_REAR"))) hasWindowRR = true;
				if (m.TextureHashes.contains(Hash.findBIN("REAR_DEFROSTER"))) hasDefroster = true;
				
				if (m.ShaderHash == Hash.findBIN("CARSKIN") && m.shaderUsage.equals(ShaderUsage.get("DiffuseSwatch"))) 
					System.out.printf("Wrong carskin material found on %s! Please use DiffuseNormalSwatch (car_nm_v_s) instead of DiffuseSwatch (car_v).\n",p.name);

				if (m.ShaderHash == Hash.findBIN("BRAKEDISC") && m.shaderUsage.equals(ShaderUsage.get("Diffuse")))
					System.out.println("Opaque brake disc "+p.name+"; use DiffuseAlpha to fix.");
				
				if ("D".equals(p.lod) && (m.ShaderHash == Hash.findBIN("MAGSILVER") ||
						m.ShaderHash == Hash.findBIN("BRAKELIGHTGLASS") ||
						m.ShaderHash == Hash.findBIN("BRAKELIGHTGLASSRED") ||
						m.ShaderHash == Hash.findBIN("DECAL") ||
						m.ShaderHash == Hash.findBIN("DOORLINE") ||
						m.ShaderHash == Hash.findBIN("MAGCHROME") ||
						m.ShaderHash == Hash.findBIN("MAGLIP") ||
						m.ShaderHash == Hash.findBIN("REGPAINTBLACK"))) {
						System.out.printf("%s found on %s; please use 'simple' materials for LOD D parts.\n", Hash.getBIN(m.ShaderHash), p.name);
				}
				
				if (p.part.contains("WHEEL") && (m.shaderUsage.equals(ShaderUsage.get("DiffuseAlpha")) || 
						m.shaderUsage.equals(ShaderUsage.get("DiffuseNormalAlpha")))) {
					System.out.printf("Transparent shader usage found on %s; wheels do not support transparency!\n", p.name);
				}
			}
			if (char0 != 'T' && !"D".equals(p.lod) && p.name.contains("WINDOW")) {
				switch (p.part) {
				case "WINDOW_FRONT":
					if (!hasWindowF) System.out.println("No/wrong window texture on "+p.name+"; should be WINDOW_FRONT.");
					break;
				case "WINDOW_FRONT_LEFT":
					if (!hasWindowFL) System.out.println("No/wrong window texture on "+p.name+"; should be WINDOW_LEFT_FRONT.");
					break;
				case "WINDOW_FRONT_RIGHT":
					if (!hasWindowFR) System.out.println("No/wrong window texture on "+p.name+"; should be WINDOW_RIGHT_FRONT.");
					break;
				case "WINDOW_REAR":
					if (!hasWindowR) System.out.println("No/wrong window texture on "+p.name+"; should be WINDOW_REAR.");
					if (!hasDefroster) System.out.println("Missing defroster on "+p.name+".");
					break;
				case "WINDOW_REAR_LEFT":
					if (!hasWindowRL) System.out.println("No/wrong window texture on "+p.name+"; should be WINDOW_LEFT_REAR.");
					break;
				case "WINDOW_REAR_RIGHT":
					if (!hasWindowRR) System.out.println("No/wrong window texture on "+p.name+"; should be WINDOW_RIGHT_REAR.");
					break;
				}
			}

		}

		if (!brakeMarker) System.out.println("Your car doesn't have the BRAKE_FRONT marker to position brakes relatively to wheels!");
		if (!hasFrontBrakes) System.out.println("Your car doesn't have front brakes!");
		if (!hasFrontDiscs) System.out.println("Your car doesn't have front brake rotors!");
		if (!hasRearBrakes) System.out.println("Your car doesn't have rear brakes!");
		if (!hasRearDiscs) System.out.println("Your car doesn't have rear brake rotors!");
		if (!hasDriver) System.out.println("Your car doesn't have a driver!");
		if (lodDPartsFound == 0) System.out.println("No lod D found!");
		if (lodDPartsFound > 1) System.out.println("Several lod D parts found, please check how your model's LOD looks ingame!");
		if (lodDTris > 3000) System.out.println("High lod D polycount ("+lodDTris+" triangles), please reduce it! Vanilla models are around 1500 triangles.");
		for (var e : lodATris.entrySet()) {
			if (e.getKey().equals("KIT00") && e.getValue() < 16000) System.out.println("Your model is low-poly for the game's standards.");
			if (e.getValue() > 50000) System.out.printf("Your model is high-poly (%i triangles on %s), make sure no customization combination crashes the game.\n",e.getValue(), e.getKey());
			// quite inaccurate as it doesn't take into account the parts loaded by default from other kits, this would need a cross check from dbmodelparts.
		}
	}
	

	public static void checkVertexBounds(Part p) {
		boolean posOOB = false;
		boolean UVOOB = false;
		for (var m : p.mesh.materials.materials) {
			for (var v : m.verticesBlock.vertices) {
				if (m.verticesBlock.vertexFormat.has_short4n_10x_position())
					if (v.posX > Vertex.short4n_10x_max || v.posY > Vertex.short4n_10x_max || v.posZ > Vertex.short4n_10x_max || 
						v.posX < Vertex.short4n_10x_min || v.posX < Vertex.short4n_10x_min || v.posX < Vertex.short4n_10x_min) posOOB = true;
				if (m.verticesBlock.vertexFormat.has_short2n_32x_texcoord())
					if (v.tex0U > Vertex.short2n_32x_max || v.tex0V > Vertex.short2n_32x_max || v.tex0U < Vertex.short2n_32x_min || v.tex0V < Vertex.short2n_32x_min) UVOOB = true;
			}
		}
		if (posOOB) System.out.println("Warning : one or multiple vertices are too far away from the origin on part "+p.name+" ! Please keep X, Y and Z between -10 and +10.");
		if (UVOOB) System.out.println("Warning : one or multiple UV summits are too far away from the origin on part "+p.name+" ! Please keep U and V between -32 and +32.");
	}

	public static Pair<String,String> findBlockAndNameInGeomOrTPK(ByteBuffer bb) {
//		var beginning = bb.position()-8; //beginning of the parent bin block, header included
		var id = bb.getInt();
//		var blockEnd = bb.getInt() + bb.position();
		if (id == BlockType.Geometry.getKey()) {
			int length;
			while (bb.getInt() != BlockType.Geom_Header.getKey()) {
				length = bb.getInt();
				bb.position(length + bb.position());
			} //skip to the header
			bb.getInt();
			while (bb.getInt() != BlockType.Geom_Info.getKey()) {
				length = bb.getInt();
				bb.position(length + bb.position());
			} //then to the info block
			
			var infoStart = bb.position() - 4; //before header
			bb.position(infoStart+24); //skip block size and data
			var filename = Block.readString(bb);
			bb.position(infoStart+80);
			var blockname = Block.readString(bb);
//			bb.position(beginning);
//			System.out.println(blockname + " " +filename);
			return new Pair<>(blockname, filename);
			
		} else if (id == BlockType.TPK.getKey()) {
			int length;
			while (bb.getInt() != BlockType.TPK_Header.getKey()) {
				length = bb.getInt();
				bb.position(length + bb.position());
			} //skip to the header
			bb.getInt();
			while (bb.getInt() != BlockType.TPK_Info.getKey()) {
				length = bb.getInt();
				bb.position(length + bb.position());
			} //then to the info block

			var infoStart = bb.position() - 4; //before header
			bb.position(infoStart+12); //skip block size and data
			var blockname = Block.readString(bb);
			bb.position(infoStart+40);
			var filename = Block.readString(bb);
//			bb.position(beginning);
//			System.out.println(blockname + " " +filename);
			return new Pair<>(blockname, filename);
		}
		return null;
	}
}

class RenderPriority{
	String partName = "";
	ArrayList<Pair<Material,Integer>> values = new ArrayList<>();
}

class ReplaceInStream{
	String file;
	boolean needsCompiling;
	int binBlockIndex;
	byte[] blockData;
	
	/**
	 * @param file
	 * @param needsCompiling
	 * @param binBlockIndex
	 */
	public ReplaceInStream(String file) {
		super();
		this.file = file;
		this.needsCompiling = true;
	}
	public ReplaceInStream(byte[] arr) {
		super();
		this.blockData = arr;
		this.needsCompiling = false;
	}
}