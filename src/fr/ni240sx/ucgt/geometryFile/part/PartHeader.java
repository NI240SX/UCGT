package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Platform;

public class PartHeader extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Header;}
	
	public static final int usualLengthWithoutName = 200;
	
	public int const01 = 4194329;
	public int binKey;
	public int trianglesCount;
	public short texturesCount;
	public short shadersCount;
	
	public float boundsXmin, boundsXmax, boundsYmin, boundsYmax, boundsZmin, boundsZmax;
	
	public float[][] matrix = {	{1.0f,	0,		0,		0},
								{0,		1.0f,	0,		0},
								{0,		0,		1.0f,	0},
								{0,		0,		0,		1.0f}};
	
	public int game = 947208; //game not platform
	
	public static final int UNDERCOVER = 947208;
	public static final int PROSTREET = 947640;
	public static final int CARBON = 963280;
	public static final int WORLD = 0;
	public static final int MOSTWANTED = 959824;
	public static final int UNDERGROUND2 = 976256;
	public static final int UNDERGROUND = 1243136;

	public float const11 = 0;
	public float const12 = 0;
	
	public String partName = "CAR_PART";
	
	public PartHeader(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		while (in.getInt() == 0x11111111) { //skip possible padding ?
		}
		in.position(in.position()-4);
		
		in.getInt(); //0
		in.getInt(); //0
		in.getInt(); //0
		const01 = in.getInt(); //0x19004000, flags mask ? 0x19000000 for several map models
//		if (const01 != 0x00400019) System.out.println("WARNING : const01="+String.format("0x%08X", Integer.reverseBytes(const01)));
		
		binKey = in.getInt();
		trianglesCount = in.getInt();
		in.order(ByteOrder.BIG_ENDIAN);
		texturesCount = in.getShort();
		in.order(ByteOrder.LITTLE_ENDIAN);
		shadersCount = in.getShort();
		in.getInt(); //0

		boundsXmin = in.getFloat();
		boundsYmin = in.getFloat();
		boundsZmin = in.getFloat();
		in.getInt(); //0
		
		boundsXmax = in.getFloat();
		boundsYmax = in.getFloat();
		boundsZmax = in.getFloat();
		in.getInt(); //0
		
		for (int i=0; i<4; i++) for (int j=0; j<4; j++) { //i = rows, j = columns
			matrix[i][j] = in.getFloat();
		}
		in.getInt(); //0
		in.getInt(); //0
		game = in.getInt(); //0x08740E00
		game = in.getInt(); //0x08740E00

		in.getInt(); //0
		in.getInt(); //0
		const11 = in.getFloat(); //might be a float
		const12 = in.getFloat(); //might be a float
		
		if (game == UNDERCOVER) in.position(in.position()+40); //UC model
		else if (game == PROSTREET) in.position(in.position()+24); //PS model
		else if (game == CARBON) {} //carbon model, do nothing
		else if (game == WORLD) {}
		else if (game == MOSTWANTED) {}
		else if (game == UNDERGROUND2) in.position(in.position()+4);
		else if (game == UNDERGROUND) in.position(in.position()-12);
		else System.out.println("Unknown part header magic! ("+game+")");
//		System.out.println("Part header magic "+const02);
		
		partName = Block.readString(in);
//		if (partName.isEmpty()) throw new Exception("Part name cannot be empty !");
		
//		System.out.println("part header data for "+partName+": const01="+String.format("0x%08X", Integer.reverseBytes(const01)) + ", const11="+const11+", const12="+const12);
		
//		System.out.println(partName);
//		if (in.position()%4 != 0) in.position(in.position()+4-in.position()%4); //unnecessary complication
		in.position(blockStart+blockLength); //anyways it's that
	}

	public PartHeader(String name, Platform plat) {
		this.partName = name;
		this.binKey = Hash.findBIN(name);
		switch(plat) {
		case PC:
		case X360:
			game = UNDERCOVER;
			break;
		case Prostreet_PC:
		case Prostreet_X360:
			game = PROSTREET;
			break;
		case Carbon_PC:
			game = CARBON;
			break;
		default:
			break;
		}
	}

	public PartHeader(PartHeader header, String name) {
		this.partName = name;
		this.binKey = Hash.findBIN(name);
		this.boundsXmax = header.boundsXmax;
		this.boundsXmin = header.boundsXmin;
		this.boundsYmax = header.boundsYmax;
		this.boundsYmin = header.boundsYmin;
		this.boundsZmax = header.boundsZmax;
		this.boundsZmin = header.boundsZmin;
		this.matrix = header.matrix;
		this.shadersCount = header.shadersCount;
		this.texturesCount = header.texturesCount;
		this.trianglesCount = header.trianglesCount;
		this.game = header.game;
	}
	
	public void setPlatform(Platform plat) {
		game = switch(plat) {
		case PC:
		case X360:
			yield UNDERCOVER;
		case Carbon_PC:
			yield CARBON;
		case Prostreet_PC:
		case Prostreet_X360:
			yield PROSTREET;
		default:
			throw new IllegalArgumentException("Unexpected value: " + plat);
		};
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		
		//process size
		var alignment = Block.findAlignment(currentPosition+8, 16);
		var blockLength = usualLengthWithoutName + partName.length() + 1 + alignment;
		if (game == PROSTREET) blockLength -=16;
		if (game == CARBON) blockLength -=40;
		if ((partName.length()+1) % 4 != 0) blockLength += 4 - (partName.length()+1)%4;

		var out = ByteBuffer.wrap(new byte[blockLength + 8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(blockLength);

		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		out.putInt(0);
		out.putInt(0);
		out.putInt(0);
		out.putInt(const01); //flags mask, check it's fine with map models
		
		out.putInt(binKey);
		out.putInt(trianglesCount);
		out.order(ByteOrder.BIG_ENDIAN);
		out.putShort(texturesCount);
		out.order(ByteOrder.LITTLE_ENDIAN);
		out.putShort(shadersCount);
		out.putInt(0);

		out.putFloat(boundsXmin);
		out.putFloat(boundsYmin);
		out.putFloat(boundsZmin);
		out.putFloat(0);
		
		out.putFloat(boundsXmax);
		out.putFloat(boundsYmax);
		out.putFloat(boundsZmax);
		out.putFloat(0);
		
		
		for (int i=0; i<4; i++) for (int j=0; j<4; j++) { //i = rows, j = columns
			out.putFloat(matrix[i][j]);
		}
		out.putInt(0);
		out.putInt(0);
		out.putInt(game);
		out.putInt(game);
		
		out.putInt(0);
		out.putInt(0);
		out.putFloat(const11); //const11
		out.putFloat(const12); //const12
		
		if (game == UNDERCOVER) out.position(out.position()+40); //UC
		else if (game == PROSTREET) out.position(out.position()+24); //PS
		Block.putString(out, partName);

		return out.array();	
	}

}
