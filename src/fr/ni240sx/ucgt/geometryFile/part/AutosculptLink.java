package fr.ni240sx.ucgt.geometryFile.part;

public class AutosculptLink {
	public AutosculptLink() {
		
	}
	
	public AutosculptLink(int partKey, short passZone1, short passZone2, short passZone3, short passZone4) {
		super();
		this.mNameHash = partKey;
		this.mParentZoneRangeStart = passZone1;
		this.mParentZoneRangeEnd = passZone2;
		this.mChildZoneRangeStart = passZone3;
		this.mChildZoneRangeEnd = passZone4;
	}
	public int mNameHash = 0;

	public short mParentZoneRangeStart = 1;
	public short mParentZoneRangeEnd = 1;
	public short mChildZoneRangeStart = 1;
	public short mChildZoneRangeEnd = 1;
}
