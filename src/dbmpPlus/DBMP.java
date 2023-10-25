package dbmpPlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import binstuff.Hash;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

class DBMP{
	
	public static final int maxPartLength = 200; //200 bytes for each part should be more than enough when exporting
	
	public Hash carname;
	public ArrayList<Part> parts;
	public DBMP() {
		super();
		this.carname = new Hash("UNKNOWN");
		this.parts = new ArrayList<Part>();
	}
	public DBMP(String carname) {
		super();
		this.carname = new Hash(carname);
		this.parts = new ArrayList<Part>();
	}
	public DBMP(String carname, ArrayList<Part> parts) {
		super();
		this.carname = new Hash(carname);
		this.parts = parts;
	}
	public DBMP(Hash carname, ArrayList<Part> parts) {
		super();
		this.carname = carname;
		this.parts = parts;
	}
	
	public String toString() {
		String s = "== DBModelParts ==\nCar=" + carname.label;
		for (Part p : parts) {
			s += "\n" + p;
		}
		s += "\n== End of DBMP  ==";
		return s;
	}
	
	public String displayName() {
		String s = "== DBModelParts ==\nCar=" + carname.label;
		for (Part p : parts) {
			s += "\n" + p.displayName;
		}
		s += "\n== End of DBMP  ==";
		return s;
	}
	
	public void updateAll() {
		for (Part p : parts) {
			p.update();
		}
	}
	

	public void saveToFile(File f) throws IOException {
		FileOutputStream fos;
		fos = new FileOutputStream(f);
		
		ByteBuffer bb = ByteBuffer.wrap(new byte[maxPartLength*this.parts.size()]);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(36);
		writeString(carname.label, bb);
		bb.putInt(parts.size());
		for (Part p : parts) { //loop on parts
			bb.putInt(p.attributes.size());
			for (Attribute a : p.attributes) { //loop on attributes
				a.writeToFile(bb);
			}
		}
		int fileLength = bb.position();
		bb.position(0);
		bb.putInt(Integer.reverseBytes(0x674d7042));
		bb.putInt(fileLength-8);
		bb.putInt(Integer.reverseBytes(0x06000000));
		bb.putInt(Integer.reverseBytes(0xc0368c76));
		bb.putInt(fileLength-20);
		bb.putInt(Integer.reverseBytes(0x52415757));
		bb.putInt(Integer.reverseBytes(0x01100000));
		bb.putInt(fileLength-36);
		bb.putInt(fileLength-20);
		
		Integer.reverseBytes(0);
		
		fos.write(Arrays.copyOfRange(bb.array(), 0, fileLength));			
		fos.close();
		System.out.println("File saved to " + f);
		
	}
	
	
	public static DBMP loadDBMP(File f) {

		if (f==null) return null;
		
		DBMP loadDBMP = null;
		
		//File f = new File("...");
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			byte [] fileToBytes = new byte[(int)f.length()];
			fis.read(fileToBytes);
			fis.close();
			
			//common attribute hashes
			Hash PART_NAME_OFFSETS = new Hash("PART_NAME_OFFSETS");
			Hash LOD_BASE_NAME = new Hash("LOD_BASE_NAME");

			Hash PARTID_UPGRADE_GROUP = new Hash("PARTID_UPGRADE_GROUP");

			Hash PART_NAME_SELECTOR = new Hash("PART_NAME_SELECTOR");
			Hash LOD_NAME_PREFIX_SELECTOR = new Hash("LOD_NAME_PREFIX_SELECTOR");
			Hash MAX_LOD = new Hash("MAX_LOD");
			Hash MORPHTARGET_NUM = new Hash("MORPHTARGET_NUM");

			Hash LOD_CHARACTERS_OFFSET = new Hash("LOD_CHARACTERS_OFFSET");
			Hash NAME_OFFSET = new Hash("NAME_OFFSET");
			
			Hash CV = new Hash("CV");
			
			ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(36);
			loadDBMP = new DBMP(readString(bb)); //reads the plain text car name
			int partsAmount = bb.getInt();
			for (int i=0; i<partsAmount; i++) { //loop on parts
				Part p;
				loadDBMP.parts.add(p = new Part());
				int partAttribCount = bb.getInt();
				for (int j=0; j<partAttribCount; j++) { //loop on attributes
					int attributeHash = bb.getInt();
					if (attributeHash == PART_NAME_OFFSETS.binHash) p.attributes.add(new AttributeTwoString(PART_NAME_OFFSETS, bb));
					if (attributeHash == LOD_BASE_NAME.binHash) p.attributes.add(new AttributeTwoString(LOD_BASE_NAME, bb));

					if (attributeHash == PARTID_UPGRADE_GROUP.binHash) p.attributes.add(new AttributeCarPartID(PARTID_UPGRADE_GROUP, bb));

					if (attributeHash == PART_NAME_SELECTOR.binHash) p.attributes.add(new AttributeInteger(PART_NAME_SELECTOR, bb));
					if (attributeHash == LOD_NAME_PREFIX_SELECTOR.binHash) p.attributes.add(new AttributeInteger(LOD_NAME_PREFIX_SELECTOR, bb));
					if (attributeHash == MAX_LOD.binHash) p.attributes.add(new AttributeInteger(MAX_LOD, bb));
					if (attributeHash == MORPHTARGET_NUM.binHash) p.attributes.add(new AttributeInteger(MORPHTARGET_NUM, bb));
					
					if (attributeHash == LOD_CHARACTERS_OFFSET.binHash) p.attributes.add(new AttributeString(LOD_CHARACTERS_OFFSET, bb));
					if (attributeHash == NAME_OFFSET.binHash) p.attributes.add(new AttributeString(NAME_OFFSET, bb));

					if (attributeHash == CV.binHash) p.attributes.add(new AttributeKey(CV, bb));
					
					//cheap ass code moment
				}
				p.update();
			}			
		} catch (FileNotFoundException e) {
			//dbmp to load not found
			// TODO Auto-generated catch block
			new Alert(Alert.AlertType.ERROR, "File not found", ButtonType.OK).show();
			e.printStackTrace();
			loadDBMP = null;
		} catch (Exception e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Error while loading file", ButtonType.OK).show();
			loadDBMP = null;
		}
		return loadDBMP;
		
	}
		
	public static String readString(ByteBuffer bb) {
		byte[] stringBytesOversize = new byte[64];
		byte b;
		int i = 0;
		while ((b=bb.get())!=0) {
			stringBytesOversize[i] = b;
			i++;
		}
		return new String(Arrays.copyOf(stringBytesOversize, i));
	}
	
	public static void writeString(String s, ByteBuffer bb) {
		bb.put(s.getBytes());
		bb.put((byte)0);
	}
}