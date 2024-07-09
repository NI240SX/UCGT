package fr.ni240sx.ucgt.binstuff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import fr.ni240sx.ucgt.geometryFile.*;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.geometryFile.part.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.*;

public abstract class Block {
	public abstract GeomBlock getBlockID();
	
	public static HashMap<GeomBlock, Boolean> doNotRead = new HashMap<>(); //allows speeding up file loading by blocking unnecessary blocks from being read
	
	public ArrayList<Block> subBlocks = new ArrayList<>();

	public byte[] data;
	
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException; //TODO check whether these throws are still useful
	
	public static Block read(ByteBuffer in) {
		int chunkToInt;
		GeomBlock block = GeomBlock.get(chunkToInt = in.getInt());
//		System.out.println("Block read : "+block.getName());
		if (doNotRead.get(block) == null) //use the doNotRead with caution !
		switch (block) {
		case Padding:
			return new Padding(in);
		case Geometry:
			System.out.println("initializing a geometry block - this shouldn't happen");
			return new Geometry(in);
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
		case Part_Mesh_Info:
			return new Mesh_Info(in);
		case Part_Mesh_ShadersUsage:
			return new ShadersUsage(in);
		case Part_Mesh_Materials:
			return new Materials(in);
		case Part_Mesh_VertsHeader:
			return new Mesh_VertsHeader(in);
		case Part_Mesh_Vertices:
			return new Vertices(in);
		case Part_Mesh_Triangles:
			return new Triangles(in);
		case Part_Padding:
			in.position(in.getInt()+in.position()); //skips the block
			return null;
//			return new Part_Padding(in);
		case Part_HashList:
			in.position(in.getInt()+in.position()); //skips the block
			return null;
		case Part_HashAssign:
			in.position(in.getInt()+in.position()); //skips the block
			return null;
		case Part_AutosculptLinking:
			return new AutosculptLinking(in);
		case Part_AutosculptZones:
			return new AutosculptZones(in);
		case INVALID:
		default:
			System.out.println("Unknown block, ID="+Integer.toHexString(Integer.reverseBytes(chunkToInt)));
			return new UnknownBlock(in, chunkToInt);
		}
		return new UnknownBlock(in, chunkToInt);
	}
	
	public static String readString(ByteBuffer bb) {
		byte[] stringBytesOversize = new byte[64];
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
	/**
	 * @param bb - ByteBuffer to use
	 * @param s - String to add
	 * @param maxSize - Maximum string length
	 */
	public static void putString(ByteBuffer bb, String s, int maxSize) {
		
		for (int i=0; i<s.length(); i++) {
			if (i>maxSize-1) {
				System.out.println("[WARN] String too long !");
				break;
			}
			bb.put((byte)s.charAt(i));
		}
		bb.put((byte)0);
		//4-byte alignment
		if (bb.position()%4 != 0) bb.put(new byte[4 - bb.position()%4]); //TODO changed since the last working version
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
	
	public static int findAlignment(int position, int alignment) {
		if (position%alignment == 0) return 0;
		return alignment-position%alignment;
	}
}
