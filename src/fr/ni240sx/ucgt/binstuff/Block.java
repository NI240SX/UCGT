package fr.ni240sx.ucgt.binstuff;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import fr.ni240sx.ucgt.geometryFile.*;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.geometryFile.part.*;

public abstract class Block {
	public static final GeomChunk BlockID = GeomChunk.INVALID;

	public ArrayList<Block> subBlocks = new ArrayList<Block>();
	public byte[] data;
	
	public abstract byte[] save();
	
	public static Block read(ByteBuffer in) {
		switch (GeomChunk.get(in.getInt())) {
		case Geometry:
			return new Geometry(in);
		case Geom_Header:
			return new Header(in);
		case Geom_Info:
			return new Info(in);
		case Geom_PartsList:
		case Geom_PartsOffsets:
		case Geom_UNKNOWN:
		case CompressedData:
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
		case EMPTY:
		case INVALID:
		default:
			return null;
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
}
