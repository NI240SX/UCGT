package fr.ni240sx.ucgt.geometryFile.geometry;

public class PartOffset {
	public int partKey;
	public int offset;
	public int sizeCompressed;
	public int sizeDecompressed;
	public int unknown = 512;
	
	/**
	 * @param partKey
	 * @param offset
	 * @param sizeCompressed
	 * @param sizeDecompressed
	 */
	public PartOffset(int partKey, int offset, int sizeCompressed, int sizeDecompressed) {
		this.partKey = partKey;
		this.offset = offset;
		this.sizeCompressed = sizeCompressed;
		this.sizeDecompressed = sizeDecompressed;
	}
}