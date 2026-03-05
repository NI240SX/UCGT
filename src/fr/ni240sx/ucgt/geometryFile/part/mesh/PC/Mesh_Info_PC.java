package fr.ni240sx.ucgt.geometryFile.part.mesh.PC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Mesh_Info;

public class Mesh_Info_PC extends Mesh_Info {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_Info_PC;}

	public static final int usualLength = 72;
	
	public int const01 = 0;
	public int const02 = 0;
//	public int version = 48;

	//flags, may be linked to how simplified the part is : 0x8041A302 = full part, 80410000 = less detail (brake, exhaust_tip)
//	public byte[] flags = {(byte) 0x80, 0x41, (byte) 0xA3, 0x02};
			
//	public int numMaterials = 0;
	public int const12 = 0; //these constants are most likely always 0
	public int const13 = 0;
	public int const14 = 0;
	
	public int const21 = 0;
	public int const22 = 0;
	public int const23 = 0;
	public int const24 = 0;

//	public int numTriangles = 0;
	public int const32 = 0;
//	public int numTrianglesExtra = 0;
	public int const34 = 0;
	
//	public int numVertices = 0;
	public int const42 = 0;
	
	public Platform platform = null;
	
	public Mesh_Info_PC(ByteBuffer in) {
		var blockLength = 
		in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);
		
		var alignedLength = blockLength - (in.position() - blockStart);

		const01 = in.getInt();
		const02 = in.getInt();
		version = in.getInt();

		/*
		 * UC PC	version	48	length	72
		 * UC X360	N/A (different block ID)
		 * PS PC	version 47	length	56
		 * PS X360	version 18	length	80
		 * C PC		version 18	length	52
		 */

		if (version == 48 && alignedLength == 72) platform = Platform.PC;
		if (version == 47 && alignedLength == 56) platform = Platform.Prostreet_PC;
		if (version == 18 && alignedLength == 80) platform = Platform.Prostreet_X360;
		if (version == 18 && alignedLength == 52) platform = Platform.Carbon_PC;
//		if (version == 18 && alignedLength == 48) platform = Platform.MostWanted_PC;
		
		in.get(flags);
		
		numMaterials = in.getInt();
		const12 = in.getInt();
		const13 = in.getInt();
		const14 = in.getInt();

		const21 = in.getInt();
		const22 = in.getInt();

		if (platform != null) switch(platform) {
		case PC:
			const23 = in.getInt();
			const24 = in.getInt();
	
			numTriangles = in.getInt();
			const32 = in.getInt();
			numTrianglesExtra = in.getInt(); //triangles2
			const34 = in.getInt();
	
			numVertices = in.getInt();
			const42 = in.getInt();
			break;
			
		case Prostreet_PC:
			numTriangles = in.getInt();
			const32 = in.getInt();
			numVertices = in.getInt();
			const42 = in.getInt();
			break;
			
		case Prostreet_X360:
			in.position(in.position()+24);

			numTriangles = in.getInt();
			const32 = in.getInt();
			numVertices = in.getInt();
			const42 = in.getInt();
			break;

		case Carbon_PC:
			in.getInt();
			in.getInt(); //numIndices
			const32 = in.getInt();
			break;
			
		case X360:
		default:
			break;
		} else {
			System.out.println("Mesh: Unsupported platform!");
		}
		
		in.position(blockStart+blockLength); //safety
	}

	public Mesh_Info_PC(Platform platform) {
		updatePlatform(platform);
		flags = new byte[]{(byte) 0x80, 0x41, (byte) 0xA3, 0x02};
	}

	public void updatePlatform(Platform plat) {
		this.platform = plat;
		this.version = switch(plat) {
		case PC:
			yield 48;
		case Prostreet_PC:
			yield 47;
		case Prostreet_X360:
			yield 18;
		case Carbon_PC:
			yield 18;
		case X360:
			yield -1; //not applicable
		};
	}

	public Mesh_Info_PC(Mesh_Info info) {
		super(info);
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 16);
		
		ByteBuffer out = null;
		
		switch(platform) {
		case PC:
			out = ByteBuffer.wrap(new byte[usualLength + 8 + alignment]);
			break;
		case Prostreet_PC:
			out = ByteBuffer.wrap(new byte[56 + 8 + alignment]);
			break;
		case Prostreet_X360:
			out = ByteBuffer.wrap(new byte[80 + 8 + alignment]);
			break;
		case Carbon_PC:
			out = ByteBuffer.wrap(new byte[52 + 8 + alignment]);
			break;
		case X360:
		default:
			break;
		}

		assert (out != null);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		
		switch(platform) {
		case PC:
			out.putInt(usualLength + alignment);
			break;
		case Prostreet_PC:
			out.putInt(56 + alignment);
			break;
		case Prostreet_X360:
			out.putInt(80 + alignment);
			break;
		case Carbon_PC:
			out.putInt(52 + alignment);
			break;
		case X360:
		default:
			break;
		}
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		out.putInt(const01);
		out.putInt(const02);
		out.putInt(version);
		
		out.put(flags);
		
		out.putInt(numMaterials);
		out.putInt(const12);
		out.putInt(const13);
		out.putInt(const14);

		out.putInt(const21);
		out.putInt(const22);

		
		switch(platform) {			
		case PC:
			out.putInt(const23);
			out.putInt(const24);
	
			out.putInt(numTriangles);
			out.putInt(const32);
			out.putInt(numTrianglesExtra);
			out.putInt(const34);

			out.putInt(numVertices);
			out.putInt(const42);
			break;
			
		case Prostreet_PC:
			out.putInt(numTriangles);
			out.putInt(const32);
			out.putInt(numVertices);
			out.putInt(const42);
			break;
			
		case Prostreet_X360:
			out.position(out.position()+24);

			out.putInt(numTriangles);
			out.putInt(const32);
			out.putInt(numVertices);
			out.putInt(const42);
			break;

		case Carbon_PC:
			out.putInt(0);
			out.putInt(numTriangles);
			out.putInt(const32);
			break;

		case X360:
		default:
			break;
		}
		
		return out.array();	
	}
}