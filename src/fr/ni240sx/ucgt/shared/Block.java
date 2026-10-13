package fr.ni240sx.ucgt.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;
import fr.ni240sx.ucgt.geometryFile.*;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.geometryFile.part.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PC.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.X360.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PS3.*;
import fr.ni240sx.ucgt.geometryFile.textures.*;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.*;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.*;

public interface Block {
	
	public static final boolean debug = false;
	public AtomicInteger debugDumpIndex = new AtomicInteger(0);
	
	public abstract BlockType getBlockID();
	
	public static HashMap<BlockType, Boolean> doNotRead = new HashMap<>(); //allows speeding up file loading by blocking unnecessary blocks from being read
	
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException;
	
	public static Block read(ByteBuffer in) throws Exception {
		int chunkToInt;
		BlockType block = BlockType.get(chunkToInt = in.getInt());
//		var pos = in.position();
//		System.out.println("Block read : "+block.getName());
		
		if (debug) {
			//if (block == BlockType.INVALID) 
				System.out.println("0x%08X - %s - %s".formatted(chunkToInt, block, bChunkID.get(chunkToInt)));
		}
		
		if (doNotRead.get(block) == null) //use the doNotRead with caution !
		//try {
		switch (block) {
		case Padding:
			return new Padding(in);
		case Geometry:
//			System.out.println("initializing a geometry block - this shouldn't happen");
			return new Geometry(in);
		case UCGT_Data:
			return new UCGTData(in);
		case Geom_Header:
			return new GeomHeader(in);
		case Geom_Info:
			return new GeomInfo(in);
		case Geom_PartsList:
			return new PartsList(in);
		case Geom_PartsOffsets:
			return new PartsOffsets(in);
		case Geom_UNKNOWN:
			in.position(in.getInt()+in.position()); //skips the block
			return null;
//			return new Geom_Unknown(in);
		case CompressedData:
			return new CompressedData(in);
		case Part:
			System.out.println("initializing a part block without geometry - this shouldn't happen");
			return new Part(in);
		case Part_Header:
			return new PartHeader(in);
		case Part_TexUsage:
			return new TexUsage(in);
		case Part_Strings:
			return new Strings(in);
		case Part_ShaderList:
			return new Shaders(in);
		case Part_MPoints:
			return new MPoints(in);
		case Part_Mesh:
			return new Mesh(in);
		case Part_Mesh_Info_PC:
			return new Mesh_Info_PC(in);
		case Part_Mesh_ShadersUsage:
			return new ShadersUsage(in);
		case Part_Mesh_Materials_PC:
			return new Materials_PC(in);
		case Part_Mesh_VertsHeader:
			return new Mesh_VertsHeader(in);
		case Part_Mesh_Vertices_PC:
			return new Vertices_PC(in);
		case Part_Mesh_Triangles_PC:
			return new Triangles_PC(in);
//		case Part_Padding:
//			in.position(in.getInt()+in.position()); //skips the block
//			return null;
////			return new Part_Padding(in);
//		case Part_HashList:
//			in.position(in.getInt()+in.position()); //skips the block
//			return null;
//		case Part_HashAssign:
//			in.position(in.getInt()+in.position()); //skips the block
//			return null;
		case Part_AutosculptLinking:
			return new AutosculptLinking(in);
		case Part_AutosculptZones:
			return new AutosculptZones(in);

		case Part_Mesh_Info_X360:
			return new Mesh_Info_X360(in);
		case Part_Mesh_Materials_X360:
			return new Materials_X360(in);
		case Part_Mesh_Vertices_X360:
			return new Vertices_X360(in);
		case Part_Mesh_Triangles_X360:
			return new Triangles_X360(in);

		case Part_Mesh_Info_PS3:
			return new Mesh_Info_PS3(in);
		case Part_Mesh_Materials_PS3:
			return new Materials_PS3(in);
		case Part_Mesh_Vertices_PS3:
			return new Vertices_PS3(in);
		case Part_Mesh_Triangles_PS3:
			return new Triangles_PS3(in);

		case Part_Mesh_LegacyMaterials:
			return new LegacyMaterials(in);
		case Part_Mesh_LegacyVertices:
			return new LegacyVertices(in);
		case Part_Mesh_LegacyTriangles:
			return new LegacyTriangles(in);
			
		case TPK:
			return new TPK(in);
		case TPK_Header:
			return new TPKHeader(in, null);
		case TPK_Info:
			return new TPKInfo(in, null);
		case TPK_TexList:
			return new TexList(in);
		case TPK_TexOffsets:
			return new TexOffsets(in);
		case TPK_Names:
			return new TPKNames(in, null);
		case TPK_Formats:
			return new TPKFormats(in, null);
			
		case CarCollisions:
			return new Collisions(in);
			
//		case NIS_Skeleton:
//			System.out.println("NIS Skeleton block");
//			return new UnknownBlock(in, chunkToInt);
			
			
			//region file
		case ChunksTrackPathContainer:
			return new ChunksZonesContainer(in);
		case ChunksTrackPathZones:
			return new ChunksTrackPathZones(in);
		case ChunksPositionMarkers:
			return new TrackPositionMarkers(in);
		case VisibleSections:
			return new ChunksStreamingContainer(in);
		case VisibleSections_Boundaries:
			return new VisibleSectionBoundaries(in);
		case ChunksElevationRules:
			return new ChunksElevPolys(in);
		case VisibleSections_Relations:
			return new DrivableScenerySections(in);
		case ChunksOffsets:
			return new TrackStreamingSections(in);
		case ChunksSceneryHierarchies:
			return new SceneryHierarchies(in);
		case CarpWorldGrid:
			return new CarpWorldGrid(in);
		case BoundsPack:
			return new BoundsPack(in);
		case TrackPathBarriers:
			return new TrackPathBarriers(in);
			
			//stream file
		case ChunkScenery:
			return new ChunkScenery(in);
		case ChunkScenery_Header:
			return new ChunkScenery_Header(in);
		case ChunkScenery_Instances:
			return new ChunkScenery_Instances(in);
		case ChunkScenery_Objects:
			return new ChunkScenery_Objects(in);
		case CarpWGridIslandData:
			return new CarpWGridIslandData(in);
		case ChunkHeliSheet:
			return new ChunkHeliSheet(in);
		case ChunkSmokeSpawners:
			return new ChunkSmokeSpawners(in);
		case ChunkVisCurtains:
			return new ChunkVisCurtains(in);
		case ChunkCollisions:
			return new ChunkCollisions(in);
			
		case INVALID:
		default:
//			System.out.println("Unknown block, ID="+String.format("0x%08X", chunkToInt)+" ("+BlockType.get(chunkToInt)+")");
			if (debug) {
				var b = new UnknownBlock(in, chunkToInt);
				if (b.data.length > 0) {
					new File("DEBUG_DUMPS").mkdirs();
					var fos = new FileOutputStream(new File("DEBUG_DUMPS\\%s_0x%08X_%d".formatted(b.getBlockID().toString(), b.ID, debugDumpIndex.getAndIncrement())));
					fos.write(b.data);
					fos.close();
					debugDumpIndex.getAndIncrement();
				}
				System.out.println("UNSUPPORTED, size %d".formatted(b.data.length));
				return b;
			}
			return new UnknownBlock(in, chunkToInt);
		}
		//} catch (Exception e) {
		//	System.out.println("Unable to read block "+block+" : "+e.getMessage());
		//	e.printStackTrace();
		//	in.position(pos);
		//}
		return new SkippedBlock(in, chunkToInt);
	}

	public static String readStringAligned(ByteBuffer bb) {
		var s = readString(bb);
		while (bb.position()%4 != 0) bb.get();
		return s;
	}
	
	public static String readString(ByteBuffer bb) {
		byte[] stringBytesOversize = new byte[128];
		byte b;
		int i = 0;
		while ((b=bb.get())!=0) {
			stringBytesOversize[i] = b;
			i++;
		}
		return new String(Arrays.copyOf(stringBytesOversize, i));
	}

	public static void putString(ByteBuffer bb, String s) {
		putString(bb, s, Integer.MAX_VALUE);
	}
	public static void putString(ByteArrayOutputStream bb, String s) throws IOException {
		putString(bb, s, Integer.MAX_VALUE);
	}
	/**
	 * @param bb - ByteBuffer to use
	 * @param s - String to add
	 * @param maxSize - Maximum string length
	 */
	public static void putString(ByteBuffer bb, String s, int maxSize) {
		
		for (int i=0; i<s.length(); i++) {
			if (i>maxSize-2) {
				break;
			}
			bb.put((byte)s.charAt(i));
		}
		bb.put((byte)0);
		//4-byte alignment
		if (bb.position()%4 != 0) bb.put(new byte[4 - bb.position()%4]);
	}
	
	public static void putString(ByteArrayOutputStream bb, String s, int maxSize) throws IOException {
		
		for (int i=0; i<s.length(); i++) {
			if (i>maxSize-1) {
				System.out.println("[WARN] String too long !");
				break;
			}
			bb.write((byte)s.charAt(i));
		}
		bb.write((byte)0);
		//4-byte alignment
		if (bb.size()%4 != 0) bb.write(new byte[4 - bb.size()%4]);
	}
	
	public static int stringLengthAligned(String s) {
		if ((s.length()+1)%4 == 0) return s.length()+1;
		return s.length() + 5 - (s.length()+1)%4;
	}
	
	public static void makeAlignment(ByteBuffer bb, int length, byte pattern) {
		for (int i=0; i<length; i++) {
			bb.put(pattern);
		}
	}
	
	public static void skipAlignment(ByteBuffer bb, int alignment) {
		while (bb.position()%alignment != 0) {
			bb.get();
		}
	}
	public static void skipAlignment(ByteArrayOutputStream bos, int modulo, int pattern) {
		while (bos.size()%modulo != 0) {
			bos.write(pattern);
		}
	}
	
	public static int findAlignment(int position, int alignment) {
		if (position%alignment == 0) return 0;
		return alignment-position%alignment;
	}

	public static void makeAlignment(ByteArrayOutputStream out, int length, byte pattern) {
		for (int i=0; i<length; i++) {
		out.write(pattern);
		}
	}
}
