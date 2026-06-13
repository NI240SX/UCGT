package fr.ni240sx.ucgt.geometryFile.part;

public class AutosculptLink {
	public AutosculptLink() {
		
	}
	
	public AutosculptLink(int partKey, short passZone1, short passZone2, short passZone3, short passZone4) {
		super();
		this.partKey = partKey;
		this.fromZone1 = passZone1;
		this.fromZone2 = passZone2;
		this.toZone3 = passZone3;
		this.toZone4 = passZone4;
	}
	public int partKey = 0;

	public short fromZone1 = 1;
	public short fromZone2 = 1;
	public short toZone3 = 1;
	public short toZone4 = 1;
}
