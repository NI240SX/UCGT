package geomCheck;

import java.util.ArrayList;

import binstuff.Hash;

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
	Hash lodAHash = null;
	Hash lodBHash = null;
	Hash lodCHash = null;
	Hash lodDHash = null;

	public Part(String kit, String name){
		this.kit = kit;
		this.name = name;
		this.lodAHash = new Hash(GeomCheck.carname + "_" + kit + "_" + name + "_A");
		this.lodBHash = new Hash(GeomCheck.carname + "_" + kit + "_" + name + "_B");
		this.lodCHash = new Hash(GeomCheck.carname + "_" + kit + "_" + name + "_C");
		this.lodDHash = new Hash(GeomCheck.carname + "_" + kit + "_" + name + "_D");
		allParts.add(this);
	}

	public String toString() {
		return kit + " " + name + " | lod A :" + lodAExists + ", lod B :" + lodBExists + " lod C :" + lodCExists + " lod D :" + lodDExists + " lod E :" + lodEExists;
	}
	
	public ArrayList<ArrayList<Boolean>> scan(byte [] fileToBytes, ArrayList<Hash> toScan){
		// for all lists check if the hash can be found in the 512 first bytes
		ArrayList<ArrayList<Boolean>> ret = new ArrayList<ArrayList<Boolean>>();
		for (int i = 0; i<4; i++) { //pre-fill with one arraylist per lod
			ret.add(null);
		}
		
		if (lodAExists) ret.set(0, searchForHashes(fileToBytes, toScan, lodAPosition));
		if (lodBExists) ret.set(1, searchForHashes(fileToBytes, toScan, lodBPosition));
		if (lodCExists) ret.set(2, searchForHashes(fileToBytes, toScan, lodCPosition));
		if (lodDExists) ret.set(3, searchForHashes(fileToBytes, toScan, lodDPosition));
		
		return ret;
	}
	
	public ArrayList<Boolean> scanLOD(byte [] fileToBytes, ArrayList<Hash> toScan, int lod){
		// for all lists check if the hash can be found in the 512 first bytes
		ArrayList<Boolean> ret = null;
		
		if (lod == 0 && lodAExists) ret = searchForHashes(fileToBytes, toScan, lodAPosition);
		if (lod == 1 && lodBExists) ret = searchForHashes(fileToBytes, toScan, lodBPosition);
		if (lod == 2 && lodCExists) ret = searchForHashes(fileToBytes, toScan, lodCPosition);
		if (lod == 3 && lodDExists) ret = searchForHashes(fileToBytes, toScan, lodDPosition);
		
		return ret;
	}

	ArrayList<Boolean> searchForHashes(byte[] fileToBytes, ArrayList<Hash> toScan, int position){

		int off = position + 32;
		ArrayList<Boolean> ret = new ArrayList<Boolean>();
		for (int i=0; i<toScan.size(); i++) {
			ret.add(false);
		}

		Hash potentialpart = null;
		int potentialpartoff = 0;
		byte step = 0;
		byte cut = 0;
		while (off<fileToBytes.length-4 && off<position+512
				&& (1514947658 != (((fileToBytes[off+4] & 0xFF) << 24) | ((fileToBytes[off+3] & 0xFF) << 16)
		        | ((fileToBytes[off+2] & 0xFF) << 8) | (fileToBytes[off+1] & 0xFF)))) {	//stop searching when 4a444c5a (JDLZ) is found OR WHEN EOF REACHED
			
			for(int i=0; i<toScan.size(); i++) {
				
				if (fileToBytes[off] == toScan.get(i).reversedBinHashBytes[0]) {
					//potential beginning of a hash found
					potentialpart = toScan.get(i);
					potentialpartoff = off;
					step = 0;
					cut = 0;
//					System.out.println("1st byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off);
				}
				if (potentialpart != null) {
					if (step == 0 && fileToBytes[off] == toScan.get(i).reversedBinHashBytes[1] && potentialpart == toScan.get(i)) {
						//potential second byte of a part found
						if (potentialpartoff == off-2) cut = 1;
						if (potentialpartoff <= off-3) {
							potentialpart = null;
							step = 0;
						}
						else {
							step = 1;
//							System.out.println("2nd byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off + " right after " + potentialpartoff);
						}
					}else if (step == 1 && fileToBytes[off] == toScan.get(i).reversedBinHashBytes[2] && potentialpart == toScan.get(i)) {
						//potential 3rd byte of a part found
						if (cut == 0 && potentialpartoff == off-3) cut = 2;
						if (potentialpartoff <= off-4) {
							potentialpart = null;
							step = 0;
						}
						else {
							step = 2;
//							System.out.println("3rd byte loop triggered for shader/texture/normalmap " + r.toReplace[i].label +" | "+off);
						}
					}else if (step == 2 && fileToBytes[off] == toScan.get(i).reversedBinHashBytes[3] && potentialpart == toScan.get(i) /*misses one more condition ?*/) {
						//potential last byte of a part found
						if (cut == 0 && potentialpartoff == off-4) cut = 3;
						if (potentialpartoff <= off-5) {
							potentialpart = null;
							step = 0;
						}
						if (potentialpart !=null) {
							potentialpart = null;
							ret.set(i, true);
						}
					}
				}
				
			}
			off++;
		}
		
		return ret;
	}
 

}
