package fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.Mesh;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangle;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangles;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PC.Triangles_PC;

public class LegacyTriangles extends Triangles_PC {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_LegacyTriangles;}
	
	public LegacyTriangles(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		if (Mesh.currentPlatform == Platform.Prostreet_X360) in.order(ByteOrder.BIG_ENDIAN);
		while(in.getInt() == 0x11111111) {
			if (in.remaining() == 0) break; //end of geometry reached, fix for some random geom in the map stream
		} // skip alignment
		in.position(in.position()-4);
		
		Triangle t;
		while (in.position() <= blockStart+blockLength-triangleLength) {
			triangles.add(t = new Triangle());
			t.vert0 = Short.toUnsignedInt(in.getShort());
			t.vert1 = Short.toUnsignedInt(in.getShort());
			t.vert2 = Short.toUnsignedInt(in.getShort());
		}
		in.position(blockStart+blockLength);
		
		in.order(ByteOrder.LITTLE_ENDIAN);
	}

	public LegacyTriangles() {
		super();
	}

	public LegacyTriangles(Triangles triangles) {
		super(triangles);
	}

	@Override
	public byte[] save(int currentPosition) {
		var alignment = Block.findAlignment(currentPosition+8, 32);
		var dataLength = triangleLength*triangles.size() + Block.findAlignment(triangleLength*triangles.size(), 4);
		var out = ByteBuffer.wrap(new byte[dataLength + 8 + alignment]);

		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(dataLength + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		if (Mesh.currentPlatform == Platform.Prostreet_X360) out.order(ByteOrder.BIG_ENDIAN);
		for (var t : triangles) {
			if (t.vert0 > 65535 || t.vert1 > 65535 || t.vert2 > 65535) System.out.println("[Error] Vertex index overflow; proceeding anyways !");
			out.putShort((short) t.vert0);
			out.putShort((short) t.vert1);
			out.putShort((short) t.vert2);
		}

		return out.array();	
	}
}
