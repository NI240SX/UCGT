package dbmpPlus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import binstuff.Hash;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

class DBMP{
	
	public static final int maxPartLength = 500; //200 bytes for each part should be more than enough when exporting
	
	public Hash carname;
	public ArrayList<DBMPPart> dBMPParts;
	public DBMP() {
		super();
		this.carname = new Hash("UNKNOWN");
		this.dBMPParts = new ArrayList<DBMPPart>();
	}
	public DBMP(String carname) {
		super();
		this.carname = new Hash(carname);
		this.dBMPParts = new ArrayList<DBMPPart>();
	}
	public DBMP(String carname, ArrayList<DBMPPart> dBMPParts) {
		super();
		this.carname = new Hash(carname);
		this.dBMPParts = dBMPParts;
	}
	public DBMP(Hash carname, ArrayList<DBMPPart> dBMPParts) {
		super();
		this.carname = carname;
		this.dBMPParts = dBMPParts;
	}
	
	public String toString() {
		String s = "== DBModelParts ==\nCar=" + carname.label;
		for (DBMPPart p : dBMPParts) {
			s += "\n" + p;
		}
		s += "\n== End of DBMP  ==";
		return s;
	}
	
	public String displayName() {
		String s = "== DBModelParts ==\nCar=" + carname.label;
		for (DBMPPart p : dBMPParts) {
			s += "\n" + p.displayName;
		}
		s += "\n== End of DBMP  ==";
		return s;
	}
	
	public void updateAll() {
		for (DBMPPart p : dBMPParts) {
			p.update();
		}
	}
	

	public void saveToFile(File f) throws IOException {
		FileOutputStream fos;
		fos = new FileOutputStream(f);
		
		ByteBuffer bb = ByteBuffer.wrap(new byte[maxPartLength*this.dBMPParts.size()]);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(36);
		writeString(carname.label, bb);
		bb.putInt(dBMPParts.size());
		for (DBMPPart p : dBMPParts) { //loop on parts
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

			ArrayList<Hash> attributesTwoString = new ArrayList<Hash>();
			ArrayList<Hash> attributesString = new ArrayList<Hash>();
			ArrayList<Hash> attributesInteger = new ArrayList<Hash>();
			ArrayList<Hash> attributesCarPartID = new ArrayList<Hash>();
			ArrayList<Hash> attributesKey = new ArrayList<Hash>();
			ArrayList<Hash> attributesBoolean = new ArrayList<Hash>();
			ArrayList<Hash> attributesColor = new ArrayList<Hash>();
			
			
			
			
			BufferedReader br = new BufferedReader(new FileReader(new File("data\\DBMP_Attributes")));
			String line;
			while((line = br.readLine()) != null) {
				if (!line.isBlank()) {
					switch (line.split("=")[1]) {
					case "TwoString":
						attributesTwoString.add(new Hash(line.split("=")[0]));
						break;
					case "String":
						attributesString.add(new Hash(line.split("=")[0]));
						break;
					case "Integer":
						attributesInteger.add(new Hash(line.split("=")[0]));
						break;
					case "CarPartID":
						attributesCarPartID.add(new Hash(line.split("=")[0]));
						break;
					case "Key":
						attributesKey.add(new Hash(line.split("=")[0]));
						break;
					case "Boolean":
						attributesBoolean.add(new Hash(line.split("=")[0]));
						break;
					case "Color":
						attributesColor.add(new Hash(line.split("=")[0]));
					}
				}
			}
			br.close();
			
			ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(36);
			loadDBMP = new DBMP(readString(bb)); //reads the plain text car name
			int partsAmount = bb.getInt();
			for (int i=0; i<partsAmount; i++) { //loop on parts
				DBMPPart p;
				loadDBMP.dBMPParts.add(p = new DBMPPart());
				int partAttribCount = bb.getInt();
				for (int j=0; j<partAttribCount; j++) { //loop on attributes
					int attributeHash = bb.getInt();
					Attribute a = null;
					for (Hash h : attributesTwoString) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeTwoString(h, bb));
					for (Hash h : attributesString) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeString(h, bb));
					for (Hash h : attributesInteger) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeInteger(h, bb));
					for (Hash h : attributesCarPartID) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeCarPartID(h, bb));
					for (Hash h : attributesKey) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeKey(h, bb));
					for (Hash h : attributesBoolean) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeBoolean(h, bb));
					for (Hash h : attributesColor) if (h.binHash == attributeHash) p.attributes.add(a = new AttributeColor(h, bb));
					if (DBMPPlus.debug)System.out.println("Attribute " + (j+1) + "/" + partAttribCount + " : " + a.toString());
				}
				p.update();
				if (DBMPPlus.debug)System.out.println("Part imported : "+p.displayName);
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