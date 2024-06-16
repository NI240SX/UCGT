package fr.ni240sx.ucgt.geometryFile.part;

public class AutosculptLink {
	public AutosculptLink() {
		
	}
	
	public AutosculptLink(int partKey, short passZone1, short passZone2, short passZone3, short passZone4) {
		super();
		this.partKey = partKey;
		this.passZone1 = passZone1;
		this.passZone2 = passZone2;
		this.passZone3 = passZone3;
		this.passZone4 = passZone4;
	}
	public int partKey = 0;

	public short passZone1 = 1;
	public short passZone2 = 1;
	public short passZone3 = 1;
	public short passZone4 = 1;
}
