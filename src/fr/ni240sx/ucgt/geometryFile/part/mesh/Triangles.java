package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public abstract class Triangles extends Block {

	public static final int triangleLength = 6;
	public ArrayList<Triangle> triangles = new ArrayList<>();
	
	public Triangles() {
	}
	
	public Triangles(Triangles t) {
		this.triangles = t.triangles;
	}

	@Override
	public abstract BlockType getBlockID();

	@Override
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException;

}
