package geomCheck;

import java.util.ArrayList;

class Part {

	static ArrayList<Part> allParts = new ArrayList<Part>();
	
	String kit;
	String name;
	boolean lodAExists = false;
	boolean lodBExists = false;
	boolean lodCExists = false;
	boolean lodDExists = false;
	boolean lodEExists = false;
	int lodAPosition = 0;
	int lodBPosition = 0;
	int lodCPosition = 0;
	int lodDPosition = 0;

	public Part(String kit, String name){
		this.kit = kit;
		this.name = name;
		allParts.add(this);
	}

	public String toString() {
		return kit + " " + name + " | lod A :" + lodAExists + ", lod B :" + lodBExists + " lod C :" + lodCExists + " lod D :" + lodDExists + " lod E :" + lodEExists;
	}
	
}
