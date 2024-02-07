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
	
	public ArrayList<ArrayList<Boolean>> scan(byte [] fileToBytes, ArrayList<ArrayList<Hash>> toScan){
		// for all lists check if the hash can be found in the 512 first bytes
		ArrayList<ArrayList<Boolean>> ret = new ArrayList<ArrayList<Boolean>>();
		for (ArrayList<Hash> a : toScan) {
			ArrayList<Boolean> bool;
			ret.add(bool = new ArrayList<Boolean>());
			for(Hash h : a) {
				bool.add(false);
			}
		}
		
		return null;
	}
}
