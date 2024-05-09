package fr.ni240sx.ucgt.binstuff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import fr.ni240sx.ucgt.geometryFile.*;
import fr.ni240sx.ucgt.geometryFile.geometry.*;

public abstract class Block {
	public abstract GeomBlock getBlockID();

	public ArrayList<Block> subBlocks = new ArrayList<Block>();
	public byte[] data;
	
	public abstract byte[] save() throws IOException;
	
	public static Block read(ByteBuffer in) {
		int chunkToInt;
		GeomBlock block = GeomBlock.get(chunkToInt = in.getInt());
//		System.out.println("Block read : "+block.getName());
		int len;
		switch (block) {
		case Padding:
			return new Padding(in);
		case Geometry:
			return new Geometry(in);
		case Geom_Header:
			return new Header(in);
		case Geom_Info:
			return new Info(in);
		case Geom_PartsList:
			return new PartsList(in);
		case Geom_PartsOffsets:
			return new PartsOffsets(in);
		case Geom_UNKNOWN:
			return new Geom_Unknown(in);
		case CompressedData:
			return new CompressedData(in);
		case Part:
		case Part_HashAssign:
		case Part_HashList:
		case Part_Header:
		case Part_MPoints:
		case Part_Mesh:
		case Part_Mesh_Indices:
		case Part_Mesh_Materials:
		case Part_Mesh_Padding:
		case Part_Mesh_Shaders:
		case Part_Mesh_Triangles:
		case Part_Mesh_UNKNOWN:
		case Part_Padding:
		case Part_ShaderList:
		case Part_Strings:
		case Part_TexUsage:
		case INVALID:
		default:
			return new UnknownBlock(in, chunkToInt);
		}
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
	
	/**
	 * @param bb - ByteBuffer to use
	 * @param s - String to add
	 * @param maxSize - Maximum string length
	 */
	public static void putString(ByteBuffer bb, String s, int maxSize) {
		
		for (int i=0; i<s.length(); i++) {
			if (i>maxSize) {
				System.out.println("[WARN] String too long !");
				break;
			}
			bb.put((byte)s.charAt(i));
		}
	}
}
