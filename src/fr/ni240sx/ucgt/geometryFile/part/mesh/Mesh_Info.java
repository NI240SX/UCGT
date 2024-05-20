package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;
import javafx.util.Pair;

public class Mesh_Info extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_Info;}

	public static final int usualLength = 72;
	
	public int const01 = 0;
	public int const02 = 0;
	public int const03 = 48;
	public short const04_1 = 0x4180; //flags, may be linked to how simplified the part is : 0x8041A302 = full part, 80410000 = less detail (brake, exhaust_tip)
	public short const04_2 = 0x02A3;
	
	public int numMaterials = 0;
	public int const12 = 0; //these constants are most likely always 0
	public int const13 = 0;
	public int const14 = 0;
	
	public int const21 = 0;
	public int const22 = 0;
	public int const23 = 0;
	public int const24 = 0;

	public int numTriangles = 0;
	public int const32 = 0;
	public int const33 = 0;
	public int const34 = 0;
	
	public int numVertices = 0;
	public int const42 = 0;
	
	public Mesh_Info(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		const01 = in.getInt();
		const02 = in.getInt();
		const03 = in.getInt();
		const04_1 = in.getShort();
		const04_2 = in.getShort();
		
		numMaterials = in.getInt();
		const12 = in.getInt();
		const13 = in.getInt();
		const14 = in.getInt();

		const21 = in.getInt();
		const22 = in.getInt();
		const23 = in.getInt();
		const24 = in.getInt();

		numTriangles = in.getInt();
		const32 = in.getInt();
		const33 = in.getInt();
		const34 = in.getInt();

		numVertices = in.getInt();
		const42 = in.getInt();
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 16);
		var out = ByteBuffer.wrap(new byte[usualLength + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(usualLength + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);

		out.putInt(const01);
		out.putInt(const02);
		out.putInt(const03);
		out.putShort(const04_1);
		out.putShort(const04_2);
		
		out.putInt(numMaterials);
		out.putInt(const12);
		out.putInt(const13);
		out.putInt(const14);

		out.putInt(const21);
		out.putInt(const22);
		out.putInt(const23);
		out.putInt(const24);

		out.putInt(numTriangles);
		out.putInt(const32);
		out.putInt(const33);
		out.putInt(const34);

		out.putInt(numVertices);
		out.putInt(const42);

		return out.array();	
	}
}