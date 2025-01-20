package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public abstract class Vertices extends Block {

	@Override
	public abstract BlockType getBlockID();

	public byte[] verticesData;
	public VertexFormat vertexFormat;
	
	public ArrayList<Vertex> vertices = new ArrayList<>();

	public Material material;
	
	public abstract void readVertices();

	@Override
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException;

}
