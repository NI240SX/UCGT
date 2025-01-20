package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public abstract class Materials extends Block {

	@Override
	public abstract BlockType getBlockID();

	public ArrayList<Material> materials = new ArrayList<>();
	
	@Override
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException;

}
