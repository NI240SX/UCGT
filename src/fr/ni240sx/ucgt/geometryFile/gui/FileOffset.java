package fr.ni240sx.ucgt.geometryFile.gui;

public class FileOffset {

	public String file;
	public String chunk;
	public int offset;
	public int size;
	
	public FileOffset(int offset, int blockSize, String blockname, String filename) {
		this.offset = offset;
		this.file = filename;
		this.chunk = blockname;
		this.size = blockSize;
	}

	@Override
	public String toString() {
		return "Chunk \""+chunk+"\", File \""+file+"\"";
	}
}
