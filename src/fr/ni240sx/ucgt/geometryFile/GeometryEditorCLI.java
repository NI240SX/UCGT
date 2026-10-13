package fr.ni240sx.ucgt.geometryFile;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.geometry.CompressedData;
import fr.ni240sx.ucgt.geometryFile.geometry.Padding;
import fr.ni240sx.ucgt.geometryFile.io.GLTF;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
import fr.ni240sx.ucgt.geometryFile.textures.TPK;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.TrackStreamingSections;
import javafx.scene.shape.Path;
import javafx.util.Pair;

@SuppressWarnings("unused")
public class GeometryEditorCLI {

	public static Geometry geom = null;
//	public static ArrayList<String> commandsHistory = new ArrayList<>();
	public static File commandLocation = null;
	
	public static void main(String[] args) {
		
		if (args.length >0) {
			//launching with arguments runs a special mode without user input
			String concatenatedArgs = "";
			for (var s : args) {
				if (s.contains(" ")) concatenatedArgs += "\"" +s + "\" ";
				else concatenatedArgs += s + " ";
			}
//			System.out.println("Arguments provided : "+concatenatedArgs);
			parseCommand(concatenatedArgs);
			
		} else {
			//launching without arguments runs the command-line utility
			
			showHelp();
			if (!new File(Utils.geomDefaultConfig).isFile()) {
				System.out.print("""

						No default configuration found. Would you like to generate one and open it for editing? (Y/Yes/N/No)""");
				System.out.print("\n> ");
				
		        var in = new BufferedReader(new InputStreamReader(System.in));
		        String l;
		        try {
		        	l = in.readLine();
		        	if (l.toLowerCase(Locale.ROOT).equals("yes") || l.toLowerCase(Locale.ROOT).equals("y")) {
		        		Geometry.writeDefaultConfig();
		        		Desktop.getDesktop().open(new File(Utils.geomDefaultConfig));
		        	}
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
			}
			commandLocation = new File("").getAbsoluteFile();
			
			System.out.print("\n%s> ".formatted(commandLocation == null ? "" : commandLocation.getAbsolutePath()));
	
	        var in = new BufferedReader(new InputStreamReader(System.in));
	        String l;
	        
	        try {
				while (!(l = in.readLine()).equals("exit")) {
					parseCommand(l);
			    	System.gc();
					System.out.print("\n%s> ".formatted(commandLocation == null ? "" : commandLocation.getAbsolutePath()));
				}
			} catch (Exception e) {
				System.out.println("An unexpected error happened while parsing user input, the program has to close.");
				e.printStackTrace();
			}
	        
		}

	}

	public static void parseCommand(String l) {
		try{
			String src, dst;
			switch (l.split(" ")[0]) {
	
			case "help":
			case "?":
				showHelp();
				break;
				
			case "compile":
				compile(l);
				break;
				
			case "compileall":
				compileAll(l);
				break;
				
			case "decompile":
				decompile(l);
				break;
				
			case "convert":
				convert(l);
				break;
				
			case "script":
				script(l);
				break;

			case "dump":
				dump(l);
				break;
				
			case "replace":
				replace(l);
				break;

			case "compress":
				l = l.substring(9);
				changeComp(l,CompressionType.RefPack);
				break;

			case "decompress":
				l = l.substring(11);
				changeComp(l,CompressionType.RawDecompressed);
				break;

			case "compress_stream":
				l = l.substring(9+7);
				changeStreamComp(l,CompressionType.JDLZ); //JDLZ because RefPack is slow as hell
				break;

			case "decompress_stream":
				l = l.substring(11+7);
				changeStreamComp(l,CompressionType.RawDecompressed);
				break;
				
			case "platform":
				platform(l);
				break;
				
			case "trol":
				System.out.println("The Grand Syndicate of Trolling agrees with your decision. Therefore I shall NOT nuke your coputer !");
				break;
				
			case "config":
				if (!new File(Utils.geomDefaultConfig).isFile()) Geometry.writeDefaultConfig();
        		Desktop.getDesktop().open(new File(Utils.geomDefaultConfig));
				break;
				
			case "cd":
				try {
					l = l.substring(3);					
					//read first path
					if (l.startsWith("\"")) {
						l = l.substring(1);
						src = l.split("\"")[0];
						l = l.substring(src.length()+1);
					} else {
						src = l.split(" ")[0];
						l = l.substring(src.length());
					}
					src = resolveRelativePath(src);

					if (new File(src).isDirectory()) {
						commandLocation = new File(src).getCanonicalFile();
					}
				} catch (Exception e) {
					System.out.println("Invalid command syntax : cd <new directory>");
				}
				break;
				
			default:
				System.out.println("Unknown command : \""+l+"\". Type \"help\" for a list of commands.");
			}
			
			
		} catch (Exception e) {
			System.out.println("An unexpected error happened while parsing the command : "+e.getMessage());
			e.printStackTrace();
		}
	}

	private static void changeStreamComp(String l, CompressionType ct) {
		String stream = null;
		String region = null;
		try {
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				stream = l.split("\"")[0];
				l = l.substring(stream.length()+1);
			} else {
				stream = l.split(" ")[0];
				l = l.substring(stream.length());
			}
			stream = resolveRelativePath(stream);

			if (l.length()>1) {
				l = l.substring(1);
				//read comp type			
				ct = CompressionType.get(l.split(" ")[0]);
				l = l.substring(l.split(" ")[0].length());
			}
			

			//read type if existing
			if (l.length() > 1) {
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					region = l.split("\"")[0];
				} else {
					region = l.split(" ")[0];
				}
				region = resolveRelativePath(region);
			}

			
			try {
				if (stream.contains("STREAM") && region == null) region = stream.replace("STREAM", "");
				else throw new RuntimeException("Couldn't find the region file !");
				
//				if (new File(stream.replace(".BUN", "").replace(".BIN", "")+".tmp").isFile()) new File(stream.replace(".BUN", "").replace(".BIN", "")+".tmp").delete();
//				if (new File(region.replace(".BUN", "").replace(".BIN", "")+".tmp").isFile()) new File(region.replace(".BUN", "").replace(".BIN", "")+".tmp").delete();

				new File(stream).renameTo(new File(stream.replace(".BUN", "").replace(".BIN", "")+".tmp")); //i'll read this file and transfer the data to a new file as it is read, to lower RAM usage which already gets high just with cars
				new File(region).renameTo(new File(region.replace(".BUN", "").replace(".BIN", "")+".tmp"));
	
				File streamFileIn = new File(stream.replace(".BUN", "").replace(".BIN", "")+".tmp");
				File streamFileOut = new File(stream);
				File regionFileOut = null;
				ArrayList<Block> regionBlocks = null;
				TrackStreamingSections offsets = null;
				long t = System.currentTimeMillis();
				System.out.println("Changing stream compression...");
				
				{
					Block.doNotRead.put(BlockType.ChunksEmpty, true);
					Block.doNotRead.put(BlockType.ChunksTrackPathZones, true);
					Block.doNotRead.put(BlockType.ChunksPositionMarkers, true);
					Block.doNotRead.put(BlockType.VisibleSections, true);
					Block.doNotRead.put(BlockType.VisibleSections_Info, true);
					Block.doNotRead.put(BlockType.VisibleSections_Boundaries, true);
					Block.doNotRead.put(BlockType.ChunksElevationRules, true);
					Block.doNotRead.put(BlockType.ChunksLoadingSections, true);
					Block.doNotRead.put(BlockType.VisibleSections_Relations, true);
					Block.doNotRead.put(BlockType.ChunksSceneryOverrideInfos, true);
					Block.doNotRead.put(BlockType.ChunksSceneryGroups, true);
					Block.doNotRead.put(BlockType.ChunksSceneryHierarchies, true);
					Block.doNotRead.put(BlockType.SceneryHierarchy, true);
					Block.doNotRead.put(BlockType.CarpWorldGrid, true);
					Block.doNotRead.put(BlockType.BoundsPack, true);
					Block.doNotRead.put(BlockType.CarCollisions, true);
					Block.doNotRead.put(BlockType.TrackPathBarriers, true);
					File regionFileIn = new File(region.replace(".BUN", "").replace(".BIN", "")+".tmp");
					regionFileOut = new File(region);
					FileInputStream fisL8r = new FileInputStream(regionFileIn);
					byte[] L8rToBytes = new byte[(int) regionFileIn.length()];
					fisL8r.read(L8rToBytes);
					fisL8r.close();
					
					regionBlocks = new ArrayList<>();
					var l8rBB = ByteBuffer.wrap(L8rToBytes);
					l8rBB.order(ByteOrder.LITTLE_ENDIAN);
					while (l8rBB.remaining()>8) {
						var b = Block.read(l8rBB);
						regionBlocks.add(b);
						if (b.getBlockID() == BlockType.ChunksOffsets) {
							offsets = (TrackStreamingSections) b;
						}
					}
					l8rBB = null;
					L8rToBytes = null;
					if (offsets == null) {
						throw new Exception("Couldn't find block offsets in the region file !");
					}
				}
				
				
				
				FileInputStream fisStream = new FileInputStream(streamFileIn);
				FileOutputStream fosStream = new FileOutputStream(streamFileOut);
				
				long currentPositionIn = 0;
				long currentPositionOut = 0;

				System.out.println();
				for (var chunk : offsets) {
					if (currentPositionIn < chunk.FileOffset) { //if there was preexisting padding
						currentPositionIn += fisStream.skip(chunk.FileOffset - currentPositionIn);

						byte[] outBlock = Padding.makePadding(currentPositionOut, 2048);
						fosStream.write(outBlock);
						currentPositionOut += outBlock.length;
					}
					chunk.FileOffset = (int) currentPositionOut;
										
					
					System.out.print("\rChanging compression of stream... "+(offsets.indexOf(chunk)*100/offsets.size())+"% - "+chunk.SectionName);
					byte[] chunkBytes = new byte[chunk.CompressedSize];
					fisStream.read(chunkBytes);
					currentPositionIn += chunk.CompressedSize;
					int posInChunk = 0;
					
					var bb = ByteBuffer.wrap(chunkBytes);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					if (chunk.Size != chunk.CompressedSize) {
						byte[] partData = new byte[chunk.Size];
						var dataWriter = ByteBuffer.wrap(partData);
						dataWriter.order(ByteOrder.LITTLE_ENDIAN);

						//loops on the one or multiple compressed blocks (usually RefPack, 32768 bytes each)
						while (bb.hasRemaining()) {
							CompressedData d = (CompressedData) Block.read(bb);
							var decomp = Compression.decompress(d.data);
//							if (decomp==null) continue offsets;
							dataWriter.put(d.decompressionOffset, decomp);
						}
						
						dataWriter.position(0);
						bb = dataWriter;
					}
					
					
					
					
					
					var outBytes = bb.array();
					//vanilla X360 has parts 1, 2, 3, 4 ... and only then 0 ???
					if (ct != CompressionType.RawDecompressed) {
						int off = 0;
						int compPos = 0;
						while (off < outBytes.length) {

							var decompressedLength = off + Geometry.COMPRESSION_BLOCK_SIZE > outBytes.length ? outBytes.length - off : Geometry.COMPRESSION_BLOCK_SIZE;
							var compressedData = Compression.compress(outBytes, off, 
									decompressedLength, 
									decompressedLength > 16 ? ct : CompressionType.RawDecompressed, 
									CompressionLevel.Minimum);
							
							var compblock = new CompressedData(compressedData, decompressedLength, off, compPos).save((int) currentPositionOut);
							fosStream.write(compblock);
							currentPositionOut += compblock.length;
							
							off += decompressedLength;
							compPos += compblock.length;
						}
//						System.out.println("Compressing stream chunk %s!".formatted(chunk.name));
					} else {
						fosStream.write(outBytes);
						currentPositionOut += outBytes.length;
					}
					
					chunk.Size = outBytes.length;
					chunk.CompressedSize = (int) (currentPositionOut - chunk.FileOffset);
					fosStream.flush();
				}
				fisStream.close();
				fosStream.close();
				
				new File(stream.replace(".BUN", "").replace(".BIN", "")+".tmp").delete();
				new File(region.replace(".BUN", "").replace(".BIN", "")+".tmp").delete();

				{
					FileOutputStream fosL8r = new FileOutputStream(regionFileOut);
					for (var b : regionBlocks) {
						fosL8r.write(b.save(0));
					}
					fosL8r.close();
				}
				
				System.out.println("Operation successful !");
				Block.doNotRead.clear();				
			} catch (Exception e) {
				System.out.printf("Error changing stream compression: %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : compress_stream/decompress_stream <stream file> [region file] [compression type]");}
	}
	
	private static void changeComp(String l, CompressionType ct) {
		String src;
		try {
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+1);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length());
			}
			src = resolveRelativePath(src);

			if (l.length()>1) {
				l = l.substring(1);
				//read comp type			
				ct = CompressionType.get(l.split(" ")[0]);
			}
			
			
			boolean useBackup = false;
			if (!new File(src.replace(".BIN", "-BACKUP.BIN").replace(".bin", "-BACKUP.bin")).isFile() && new File(src).isFile()) {
				//create vanilla backup if not existing
				new File(src).renameTo(new File(src.replace(".BIN", "-BACKUP.BIN").replace(".bin", "-BACKUP.bin")));
				useBackup = true;
			}
			try {
				Block.doNotRead.put(BlockType.Part_MPoints, true);
				Block.doNotRead.put(BlockType.Part_Mesh, true);
				Block.doNotRead.put(BlockType.Part_HashList, true);
				Block.doNotRead.put(BlockType.Part_HashAssign, true);
				Block.doNotRead.put(BlockType.Part_Padding, true);
				
				long t = System.currentTimeMillis();
				System.out.println("Loading geometry...");
				if (useBackup) geom = Geometry.load(new File(src.replace(".BIN", "-BACKUP.BIN").replace(".bin", "-BACKUP.bin")));
				else geom = Geometry.load(new File(src));
				System.out.println("Geometry read in "+(System.currentTimeMillis()-t)+" ms.");
				
				geom.defaultCompressionType = ct;
				geom.defaultCompressionLevel = CompressionLevel.Maximum;
				geom.SAVE_useOffsetsTable = true;
				geom.SAVE_removeUselessAutosculptParts = false;
				geom.SAVE_optimizeMaterials = false;
				geom.SAVE_sortEverythingByName = true;
				geom.SAVE_fixAutosculptNormals = false;
				geom.SAVE_removeInvalid = false;
				geom.SAVE_copyMissingLODs = false;
				geom.SAVE_copyLOD_D = false;
				geom.SAVE_protectModel = false;
				geom.SAVE_processParts = false;
				
				t = System.currentTimeMillis();
				try {
					geom.save(new File(src));
					System.out.println("Operation successful !");
					
				} catch (Exception e) {
					System.out.printf("Error saving Geometry : %s\n", e.getMessage());
					e.printStackTrace();
				}
				
				Block.doNotRead.clear();
				
			} catch (Exception e) {
				System.out.printf("Error loading Geometry : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : compress/decompress <BIN source> [Compression type]");}
	}

	private static void decompile(String l) {
		String src;
		String dst;
		try {
			l = l.substring(10);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+1);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length());
			}
			
			if (l.length()>1) {
				l = l.substring(1);
				
				//read second path
				if (l.startsWith("\"")) {
					l = l.substring(1);
					dst = l.split("\"")[0];
	//					l = l.substring(dst.length()+2);
				} else {
					dst = l.split(" ")[0];
	//					l = l.substring(dst.length()+1);
				}
			} else {
				dst = src.replace(".BIN", ".both").replace(".bin", ".both");
			}
			
			src = resolveRelativePath(src);
			dst = resolveRelativePath(dst);

			new File(dst).getParentFile().mkdirs();
			
			try {
				long t = System.currentTimeMillis();
				System.out.println("Loading geometry...");
				geom = Geometry.load(new File(src));
				System.out.println("Geometry read in "+(System.currentTimeMillis()-t)+" ms.");
				
				TPK tpk = null;
				var tpkfile = new File(new File(src).getAbsolutePath().replace(new File(src).getName(), "TEXTURES.BIN"));
				if (tpkfile.isFile() && tpkfile.length()>0) {
					t = System.currentTimeMillis();
					System.out.println("Loading textures...");
					FileInputStream fis = new FileInputStream(tpkfile);
					byte [] arr = new byte[(int)tpkfile.length()-4];
					fis.skipNBytes(4); //if this method is called on a file, we assume that the file is a geometry, therefore the first blockid can be skipped
					fis.read(arr);
					fis.close();
					tpk = new TPK(ByteBuffer.wrap(arr));
					System.out.println("Textures read in "+(System.currentTimeMillis()-t)+" ms.");
				}
				
				
				System.out.println("Saving 3D model...");
				t = System.currentTimeMillis();
				try {
					if (dst.toLowerCase(Locale.ROOT).endsWith(".z3d")) {
						ZModelerZ3D.save(geom, dst);
						System.out.println("ZModeler scene saved in "+(System.currentTimeMillis()-t)+" ms.");
				        geom.writeConfig(new File(dst.replace(".z3d", "").replace(".Z3D", "")+".ini"));
					} else if (dst.toLowerCase(Locale.ROOT).endsWith(".obj")){
						WavefrontOBJ.save(geom, dst);
						System.out.println("Wavefront saved in "+(System.currentTimeMillis()-t)+" ms.");
				        geom.writeConfig(new File(dst.replace(".obj", "").replace(".OBJ", "")+".ini"));
					} else if (dst.toLowerCase(Locale.ROOT).endsWith(".gltf")){
						GLTF.save(geom, dst,false);
						System.out.println("glTF saved in "+(System.currentTimeMillis()-t)+" ms.");
				        geom.writeConfig(new File(dst.replace(".obj", "").replace(".OBJ", "")+".ini"));
					} else {
						dst = dst.replace("."+dst.split("\\.")[dst.split("\\.").length-1], "");
						WavefrontOBJ.save(geom, dst+".obj");
						System.out.println("Wavefront saved in "+(System.currentTimeMillis()-t)+" ms.");
						ZModelerZ3D.save(geom, dst+".z3d");
						System.out.println("ZModeler scene saved in "+(System.currentTimeMillis()-t)+" ms.");
				        geom.writeConfig(new File(dst+".ini"));
					}
					System.out.println("Configuration file written.");
					if (tpk != null) {
						System.out.println("Saving textures...");
						t = System.currentTimeMillis();
						tpk.exportToFolder(new File(dst).getParent(), geom.carname);
						System.out.println("Textures saved in "+(System.currentTimeMillis()-t)+" ms.");
					}
					System.out.println("Operation successful !");
					
				} catch (Exception e) {
					System.out.printf("Error saving 3D model : %s\n", e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.printf("Error loading Geometry : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : decompile <BIN source> [glTF/OBJ/Z3D output]");}
	}

	private static void script(String l) {
		String src;
		try {
			l = l.substring(7);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
			} else {
				src = l.split(" ")[0];
			}
			src = resolveRelativePath(src);
			
			try {
				System.out.println("Reading script...");
				long t = System.currentTimeMillis();
				BufferedReader script = new BufferedReader(new FileReader(new File(src)));
				String scriptCommand;
				var previousCommandLocation = commandLocation;
				commandLocation = new File(src).getParentFile().getCanonicalFile();
				while ((scriptCommand = script.readLine()) != null) if (!scriptCommand.isBlank() && !scriptCommand.startsWith("#")) {
					System.out.println("\n%s> %s".formatted(commandLocation == null ? "" : commandLocation.getAbsolutePath(), scriptCommand));
					parseCommand(scriptCommand);
				}
				commandLocation = previousCommandLocation;
				script.close();
//					Geometry.ctkConfigToUCGTConfig(new File(src), new File(dst));
				System.out.println("Script fully executed ! Took "+(System.currentTimeMillis()-t)+" ms.");

				
			} catch (Exception e) {
				System.out.printf("Error reading script : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : script <file>");}
	}

	/**
	 * replace <source folder> <destination file> [file with blocks definitions]
	 */
	private static void replace(String l) {
		String src;
		String dst;
		String definitions = null;
		try {
			l = l.substring(8);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			sanitizeFolderEnd(src);
			src += File.separator;
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
				l = l.substring(dst.length()+1);
			} else {
				dst = l.split(" ")[0];
				l = l.substring(dst.length());
			}

			//read type if existing
			if (l.length() > 1) {
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					definitions = l.split("\"")[0];
				} else {
					definitions = l.split(" ")[0];
				}
			}
			
			src = resolveRelativePath(src);
			dst = resolveRelativePath(dst);
			definitions = resolveRelativePath(definitions);

			try {
				long t = System.currentTimeMillis();
				System.out.println("Starting to replace data...");
				Geometry.replaceInStream(src, dst, definitions);
				System.out.println("Data replaced successfully ! Took "+(System.currentTimeMillis()-t)+" ms.");
			} catch (Exception e) {
				System.out.printf("Error replacing geometries in file : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : replace <source folder> <destination file> [file with blocks definitions]");}
	}
	
	/**
	 * dump <source> <destination folder> [OBJ/Z3D/ALL] [filter]
	 */
	private static void dump(String l) {
		String src;
		String dst;
		String type = "ALL";
		List<String> filters = new ArrayList<>();
		try {
			l = l.substring(5);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
				l = l.substring(dst.length()+1);
			} else {
				dst = l.split(" ")[0];
				l = l.substring(dst.length());
			}
			sanitizeFolderEnd(src);
			dst += File.separator;

			src = resolveRelativePath(src);
			dst = resolveRelativePath(dst);

			//read type if existing
			if (l.length() > 1) {
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					type = l.split("\"")[0];
					l = l.substring(type.length()+1);
				} else {
					type = l.split(" ")[0];
					l = l.substring(type.length());
				}
			}
			
			//read filter if existing
			while (l.length() > 1) {
				String filter;
				l = l.substring(1);
				if (l.startsWith("\"")) {
					l = l.substring(1);
					filter = l.split("\"")[0];
					l = l.substring(filter.length()+1);
				} else {
					filter = l.split(" ")[0];
					l = l.substring(filter.length());
				}
				filters.add(filter);
			}
			
			try {
				long t = System.currentTimeMillis();
				System.out.println("Starting to dump data...");
				Geometry.dumpStream(src, dst, type, filters);
				System.out.println("\nDumping successful ! Took "+(System.currentTimeMillis()-t)+" ms.");
			} catch (Exception e) {
				System.out.printf("Error dumping geometries from file : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : dump <source> <destination folder> [file type] [filter]");}
	}

	private static void convert(String l) {
		String src;
		String dst;
		try {
			l = l.substring(8);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				if (l.length()>src.length()+2) l = l.substring(src.length()+2);
				else l="";
			} else {
				src = l.split(" ")[0];
				if (l.length()>src.length()+1) l = l.substring(src.length()+1);
				else l="";
			}
			
				//read second path
			if (!l.isBlank()) {
				if (l.startsWith("\"")) {
					l = l.substring(1);
					dst = l.split("\"")[0];
//					l = l.substring(dst.length()+2);
				} else {
					dst = l.split(" ")[0];
//					l = l.substring(dst.length()+1);
				}
			} else dst = src.replace(".txt", ".ini");
			
			src = resolveRelativePath(src);
			dst = resolveRelativePath(dst);

			try {
				System.out.println("Reading CTK config...");
				Geometry.ctkConfigToUCGTConfig(new File(src), new File(dst));
				System.out.println("UCGT config saved.");
				System.out.println("Operation successful !");
				
			} catch (Exception e) {
				System.out.printf("Error converting config : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : convert <TXT CTK config> [INI UCGT config]");}
	}

	private static void compile(String l) {
		String src;
		String dst;
		try {
			l = l.substring(8);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
//					l = l.substring(dst.length()+2);
			} else {
				dst = l.split(" ")[0];
//					l = l.substring(dst.length()+1);
			}
			src = resolveRelativePath(src);
			dst = resolveRelativePath(dst);
			
			backUpFile(new File(dst));
			new File(dst).getParentFile().mkdirs();
			
			try {
				if ( !new File(src).isDirectory()) {
					//regular geometry
					geom = Geometry.importFromFile(new File(src));
					
					TPK tpk = null;
					try {
						long t = System.currentTimeMillis();
						System.out.println("Saving textures...");
						tpk = new TPK(new File(src).getParentFile(), geom);
						if (tpk.textures.size() == 0) tpk = null;
						else {
							String texdst = new File(dst).getParent()+File.separator+"TEXTURES.BIN";
							backUpFile(new File(texdst));
							tpk.save(new File(texdst));
							System.out.println("\nTextures saved in "+(System.currentTimeMillis()-t)+" ms.");
						}
					} catch (Exception e) {
						System.out.println("Error saving the texture pack: "+e.getMessage());
					}
					
					try {
						geom.save(new File(dst));
						System.out.println("Operation successful !");
						
					} catch (Exception e) {
						System.out.printf("Error saving Geometry : %s\n", e.getMessage());
						e.printStackTrace();
					}
				} else {
					try {
						sanitizeFolderEnd(src);
						src += File.separator;
						
						//create a geometry bundle
						var filesInDir = new File (src).list();
						var fos = new FileOutputStream(dst);
						
						for (var s : filesInDir) {
							if (new File(src + s).isFile()) {
								// decompiled 3D models
								if (s.toLowerCase(Locale.ROOT).endsWith(".ini")) {
									for (var ext : Geometry.ALL_SUPPORTED_IO_EXTENSIONS) {
										if (new File(src + s.toLowerCase(Locale.ROOT).replace(".ini", ext)).exists()) {
											var g = Geometry.importFromFile(new File(src + s.toLowerCase(Locale.ROOT).replace(".ini", ext)));
											fos.write(g.save(0));
										}	
									}
								}
								//compiled models not taken into account
							}
							
							//TPKs as folders (using UC support)
							if (s.contains("-") && new File(src + s).isDirectory()) {
								var tpk = new TPK(new File(src + s), 9);
								if (tpk.textures.size() > 0) fos.write(tpk.save(0));
							}
						}
						fos.close();
	
						System.out.println("Operation successful !");
					} catch (Exception e) {
						System.out.printf("Error loading the provided files and converting them into a Geometry bundle : %s\n", e.getMessage());
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				System.out.printf("Error loading the provided file and converting it into a Geometry : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : compile <OBJ/Z3D source> <BIN output>");}
	}

	private static String resolveRelativePath(String src) {
		if (src == null) return null;
		if (new File(src).isAbsolute()) return src;
		return commandLocation.getAbsolutePath() + File.separator + src;
	}

	private static void compileAll(String l) {
		String src;
		String dst;
		String conf = null;
		try {
			l = l.substring(11);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+2);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length()+1);
			}
			
			//read second path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				dst = l.split("\"")[0];
				l = l.substring(dst.length()+1);
			} else {
				dst = l.split(" ")[0];
			}
			
			if (l.length() > 0) {
				l = l.substring(1);
				//read config path
				if (l.startsWith("\"")) {
					l = l.substring(1);
					conf = l.split("\"")[0];
				} else {
					conf = l.split(" ")[0];
				}
			}
			src = resolveRelativePath(src);
			dst = resolveRelativePath(dst);
			conf = resolveRelativePath(conf);
			
			sanitizeFolderEnd(src);
			sanitizeFolderEnd(dst);
			
			if (conf == null) conf = src + File.separator + "settings.ini";
			
			if (!new File(conf).isFile()) {
				System.out.print("""
						No configuration found. Would you like to generate one and open it for editing? (Y/Yes/N/No)""");
				System.out.print("\n> ");
				
		        var in = new BufferedReader(new InputStreamReader(System.in));
		        try {
		        	l = in.readLine();
		        	if (l.toLowerCase(Locale.ROOT).equals("yes") || l.toLowerCase(Locale.ROOT).equals("y")) {
		        		Geometry.writeDefaultConfig(new File(conf));
		        		Desktop.getDesktop().open(new File(conf));
			        	System.out.println("Press any key to continue with compiling...");
						System.out.print("> ");
						in.readLine();
		        	}
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
			}
						
			var dir = new File(src);
			try {
				int recursionLevel = 0;
				compileAllRecursive(dst, dir, conf, recursionLevel);
				System.out.println("Operation successful !");
			} catch (Exception e) {
				System.out.printf("An error occured : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
			
		} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : compileall <source folder> <output folder> [configuration]");}
	}

	private static void sanitizeFolderEnd(String s) {
		if (s.endsWith("\\") || s.endsWith("/")) s = s.substring(0, s.length()-1);
	}

	public static void compileAllRecursive(String dst, File dir, String conf, int recursionLevel) {
		if (!dir.isDirectory()) return;
		for (var f : dir.listFiles()) {
			
			compileAllRecursive(dst, f, conf, recursionLevel+1);

			var ini = new File(f.getPath().replace(f.getName().split("\\.")[f.getName().split("\\.").length-1], "ini"));

			if (ini.isFile()) {
				try {
					geom = Geometry.importFromFile(f, ini, conf == null ? null : new File(conf));
					
					var out = dst+File.separator+"CARS"+File.separator+geom.carname;
					new File(out).mkdirs();

					if (recursionLevel > 0) {
						TPK tpk = null;
						try {
							long t = System.currentTimeMillis();
							System.out.println("Saving textures...");
							tpk = new TPK(dir, geom);
							if (tpk.textures.size() == 0) tpk = null;
							else {
								var tpkFile = new File(out+File.separator+"TEXTURES.BIN");
								backUpFile(tpkFile);
								tpk.save(tpkFile);
								System.out.println("\nTextures saved in "+(System.currentTimeMillis()-t)+" ms.");
							}
						} catch (Exception e) {
							System.out.println("Error saving the texture pack: "+e.getMessage());
						}
					}
					
					try {
						var geomFile = new File(out+File.separator+"GEOMETRY.BIN");
						backUpFile(geomFile);
						geom.save(geomFile);
						
					} catch (Exception e) {
						System.out.printf("Error saving Geometry : %s\n", e.getMessage());
						e.printStackTrace();
					}						
				} catch (Exception e) {} //probably errors out trying to load a geom
			}
		}
	}

	public static void backUpFile(File f) {
		if (f.isFile()) {
			File f_old = new File(f.getAbsolutePath());
			f_old.renameTo(new File(f_old.getAbsolutePath() + ".bak_" + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date(f_old.lastModified()))));
		}
	}

	private static void platform(String l) {
		String src;
		Platform plat;
		try {
			l = l.substring(9);
			
			//read first path
			if (l.startsWith("\"")) {
				l = l.substring(1);
				src = l.split("\"")[0];
				l = l.substring(src.length()+1);
			} else {
				src = l.split(" ")[0];
				l = l.substring(src.length());
			}
			
			l = l.substring(1);
			
			//read platform
			plat = Platform.get(l);
			
			try {
				long t = System.currentTimeMillis();
				System.out.println("Loading geometry...");
				geom = Geometry.load(new File(src));
				System.out.println("Geometry read in "+(System.currentTimeMillis()-t)+" ms.");				

				if (!new File(src.replace(".BIN", "-BACKUP.BIN").replace(".bin", "-BACKUP.bin")).isFile() && new File(src).isFile()) {
					//create vanilla backup if not existing
					new File(src).renameTo(new File(src.replace(".BIN", "-BACKUP.BIN").replace(".bin", "-BACKUP.bin")));
				}
				
				System.out.println("Changing geometry platform...");
				t = System.currentTimeMillis();
				try {
					geom.changePlatform(plat);
					System.out.println("Platform changed in "+(System.currentTimeMillis()-t)+" ms.");				
					t = System.currentTimeMillis();

					geom.save(new File(src));
					System.out.println("Geometry converted and saved in "+(System.currentTimeMillis()-t)+" ms.");				

					System.out.println("Operation successful !");
					
				} catch (Exception e) {
					System.out.printf("Error changing platfrom and saving Geometry : %s\n", e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.printf("Error loading Geometry : %s\n", e.getMessage());
				e.printStackTrace();
			}
			
		}catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {System.out.println("Invalid command syntax : platform <BIN source> <platform>");}
	}

	public static void showHelp() {
		System.out.printf("""
				 ===   UCGT by needeka   === 
				Geometry Editor Command-Line
				Version %s - %s
				
				Supported files:
					NFS Undercover .BIN and .BUN files
					Wavefront .OBJ 3D models
					ZModeler 2 .Z3D 3D scenes
					UCGT .INI configuration files
					CTK .TXT configuration files
				
				Command syntax:
					<something> means MANDATORY
					[something] means OPTIONAL
					write commands without brackets
					if a file path contains spaces, wrap the path in quotes ""
					eg for compress <BIN source> -> compress "D:\\Games\\NFS UC\\CARS\\SOME_CAR\\GEOMETRY.BIN"
				
				Commands:
					compile <glTF/OBJ/Z3D source> <BIN output>
					    - compile a 3D model and its corresponding config to a BIN file containing a single geometry
					      also compiles a corresponding TEXTURES.BIN if DDS textures are found in the source folder

					compileall <source folder> <output folder> [configuration]
					    - compile all suitable models in the source folder and its subfolders to CARS\\XNAME in the output folder
					      also compiles textures with each model when DDS textures are found next to a model that's not in the root source folder
					      uses the given configuration file (if provided) for default and override settings
					    
					decompile <BIN source> [glTF/OBJ/Z3D output]
					    - extract a BIN file containing a single geometry to a 3D model and a configuration file
					      also extracts textures if a corresponding TEXTURES.BIN is found
					    
					compress <BIN source> [compression type]
					    - compress an existing geometry
					
					decompress <BIN source>
					    - decompress an existing geometry
					    
					platform <BIN source> <platform>
						- change platform of an existing geometry
						  supports X360 to PC or vice versa + experimental Prostreet_X360 and Prostreet_PC support
					    
					dump <source> <destination folder> [file type] [filter]
					    - extract all geometries and textures contained into the source BIN/BUN file to the output folder
					      file type is whether to export glTF, OBJ or Z3D
					      filter will allow to export only matching blocks and names (X0, Road, Chop, XBu, etc)
					    
					replace <source folder> <destination> [blocks definitions]
					    - compile and replace geometries and textures in the destination with 3D models from the source folder
					      if needed, it can also update blocks definitions offsets, this will be automatically recognized
					      if you're using this tool in a very custom environment, fill in the [blocks definitions]
					    
					compress_stream <stream file> [region file] [compression type]
						- compress a map stream
					    
					decompress_stream <stream file> [region file] [compression type]
						- decompress a map stream
						
					convert <TXT CTK config> [INI UCGT config]
					    - convert a CTK .txt config to an UCGT .ini config
					    
					script <file>
					    - load a script containing multiple of these commands

					help
					    - show this info
					    
					config
					    - opens the default configuration file
					    
					exit
					    - exits the program
				""", Utils.geom_version, Utils.geom_build);
	}
}
