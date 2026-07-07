package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.textures.TPK;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public abstract class Summary {
	public abstract BlockType getBlockID();
	public abstract String getBlockName();
	public abstract String getBlockChunk();
	public abstract List<String> getBlockContents();

	public static class Default extends Summary{

		String name;
		BlockType type;
		
		public Default(Block b) {
			name = b.getBlockID().toString();
			type = b.getBlockID();
		}
		
		@Override
		public BlockType getBlockID() {
			return type;
		}

		@Override
		public String getBlockName() {
			return null;
		}

		@Override
		public String getBlockChunk() {
			return null;
		}

		@Override
		public List<String> getBlockContents() {
			return null;
		}
		
	}
	
	public static class Geom extends Summary{
		String name;
		String chunk;
		ArrayList<String> parts = new ArrayList<>();
		
		public Geom(Geometry g) {
			name = g.geomHeader.geomInfo.filename;
			chunk = g.geomHeader.geomInfo.blockname;
			for (var p : g.parts) {
				parts.add(p.header.partName);
			}
		}
		
		@Override
		public BlockType getBlockID() {
			return BlockType.Geometry;
		}

		@Override
		public String getBlockName() {
			return name;
		}

		@Override
		public String getBlockChunk() {
			return chunk;
		}

		@Override
		public List<String> getBlockContents() {
			return parts;
		}
	}
	
	public static class TexPack extends Summary{
		String name;
		String chunk;
		ArrayList<String> textures = new ArrayList<>();
		
		public TexPack(TPK t) {
			name = t.header.info.filename;
			chunk = t.header.info.blockname;
			for (var tex : t.textures) {
				textures.add(tex.name);
			}
		}
		
		@Override
		public BlockType getBlockID() {
			return BlockType.TPK;
		}

		@Override
		public String getBlockName() {
			return name;
		}

		@Override
		public String getBlockChunk() {
			return chunk;
		}

		@Override
		public List<String> getBlockContents() {
			return textures;
		}
	}
}
