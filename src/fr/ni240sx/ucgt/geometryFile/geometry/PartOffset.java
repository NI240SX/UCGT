package fr.ni240sx.ucgt.geometryFile.geometry;

public class PartOffset {

	public static final int compressed = 512;
	public static final int rawData = 0;
	
	public int partKey;
	public int offset;
	public int sizeCompressed;
	public int sizeDecompressed;
	public int isCompressed = 512;
	
	/**
	 * @param partKey
	 * @param offset
	 * @param sizeCompressed
	 * @param sizeDecompressed
	 */
	public PartOffset(int partKey, int offset, int sizeCompressed, int sizeDecompressed, int isCompressed) {
		this.partKey = partKey;
		this.offset = offset;
		this.sizeCompressed = sizeCompressed;
		this.sizeDecompressed = sizeDecompressed;
		this.isCompressed = isCompressed;
	}
}