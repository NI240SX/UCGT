package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public abstract class Vertices extends Block {

	@Override
	public abstract BlockType getBlockID();

	public byte[] verticesData;
	public VertexFormat vertexFormat;
	
	public List<Vertex> vertices = new ArrayList<>();

	public Material material;
	
	public Vertices() {}
	
	public Vertices(Vertices v) {
		this.vertices = v.vertices;
		this.material = v.material;
	}
	
	public abstract void readVertices();

	@Override
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException;

}
