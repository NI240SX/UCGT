package fr.ni240sx.ucgt.dbmpPlus;

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

import fr.ni240sx.ucgt.shared.Hash;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class DBMP{
	
	public static final int maxPartLength = 500; //200 bytes for each part should be more than enough when exporting
	
	public String carname;
	public int GameInt;
	public ArrayList<DBMPPart> dBMPParts;
	
	public DBMP() {
		super();
		this.carname = "UNKNOWN";
		this.dBMPParts = new ArrayList<>();
	}
	public DBMP(String carname) {
		super();
		this.carname = carname;
		this.dBMPParts = new ArrayList<>();
	}
	public DBMP(String carname, ArrayList<DBMPPart> dBMPParts) {
		super();
		this.carname = carname;
		this.dBMPParts = dBMPParts;
	}
	
	@Override
	public String toString() {
		String s = "== DBModelParts ==\nCar=" + carname;
		for (DBMPPart p : dBMPParts) {
			s += "\n" + p;
		}
		s += "\n== End of DBMP  ==";
		return s;
	}
	
	public String displayName() {
		String s = "== DBModelParts ==\nCar=" + carname;
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
		writeString(carname, bb);
		bb.putInt(dBMPParts.size());
		for (DBMPPart p : dBMPParts) { //loop on parts
			bb.putInt(p.attributes.size());
			for (Attribute a : p.attributes) { //loop on attributes
				a.writeToFile(bb);
			}
		}
		int fileLength = bb.position();
		bb.position(0);
		bb.putInt(Integer.reverseBytes(0x674d7042));	//Nikki header
		bb.putInt(fileLength-8);						//Nikki block length
		bb.putInt(GameInt);								//Game int
		bb.putInt(0x768C36C0);							//DBModelParts manager bin hash
		bb.putInt(fileLength-20);						//buffer length like binary cant read it in the compression header
		bb.putInt(Integer.reverseBytes(0x52415757));	//RAWW compression type
		bb.putInt(Integer.reverseBytes(0x01100000));	//RAWW flags ?
		bb.putInt(fileLength-36);						//RAWW data length
		bb.putInt(fileLength-20);						//RAWW total length
		
		//Integer.reverseBytes(0); //<- so this was there active just chilling in the code ? epic
		
		fos.write(bb.array(), 0, fileLength);			
		fos.close();
		System.out.println("File saved to " + f);
		
	}
	
	public static DBMP loadDBMP(File f) {
		return loadDBMP(f, true);
	}
	
	public static DBMP loadDBMP(File f, boolean useGUI) {

		if (f==null) return null;
		
		DBMP loadDBMP = null;
		var oldGameInt = DBMPPlus.mainDBMP == null ? 0 : DBMPPlus.mainDBMP.GameInt;
		
		//File f = new File("...");
		try {
			var fis = new FileInputStream(f);
			byte [] fileToBytes = new byte[(int)f.length()];
			fis.read(fileToBytes);
			fis.close();

			
			//why have this load here
			//this tool's code is a war crime
			ArrayList<Integer> attributesTwoString = new ArrayList<>();
			ArrayList<Integer> attributesString = new ArrayList<>();
			ArrayList<Integer> attributesInteger = new ArrayList<>();
			ArrayList<Integer> attributesCarPartID = new ArrayList<>();
			ArrayList<Integer> attributesKey = new ArrayList<>();
			ArrayList<Integer> attributesBoolean = new ArrayList<>();
			ArrayList<Integer> attributesColor = new ArrayList<>();
			ArrayList<Integer> attributesFloating = new ArrayList<>();
			ArrayList<Integer> attributesModelTable = new ArrayList<>();
			
			BufferedReader br = new BufferedReader(new FileReader(new File("data\\DBMP_Attributes")));
			String line;
			while((line = br.readLine()) != null) {
				if (!line.isBlank()) {
					switch (line.split("=")[1]) {
					case "TwoString":
						attributesTwoString.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "String":
						attributesString.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "Integer":
						attributesInteger.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "CarPartID":
						attributesCarPartID.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "Key":
						attributesKey.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "Boolean":
						attributesBoolean.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "Color":
						attributesColor.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "Floating":
						attributesFloating.add(Hash.findBIN(line.split("=")[0]));
						break;
					case "ModelTable":
						attributesModelTable.add(Hash.findBIN(line.split("=")[0]));
						break;
					}
				}
			}
			br.close();
			
			ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			if (bb.getInt() != 0x42704D67) throw new UnsupportedOperationException("The provided file is not a Binary serialized DBModelParts!"); //Nikki header
			bb.getInt(); //size
			//OH MY GOD WHY ISNT IT A FUCKING CONSTRUCTOR
			var GameInt = bb.getInt(); //GameInt
			if (bb.getInt() != 0x768C36C0) throw new UnsupportedOperationException("The provided file is not a Binary serialized DBModelParts!"); //DBModelParts manager bin hash
			
			//FIXME continue game int detection, check game specific attribs, add saving option and game specific CarPartID
			
			bb.position(36);
			loadDBMP = new DBMP(readString(bb)); //reads the plain text car name
			DBMPPlus.mainDBMP.GameInt = GameInt; //yeah nah i did this thing very stupidly
			loadDBMP.GameInt = GameInt; 
			int partsAmount = bb.getInt();
			for (int i=0; i<partsAmount; i++) { //loop on parts
				DBMPPart p;
				loadDBMP.dBMPParts.add(p = new DBMPPart(useGUI));
				int partAttribCount = bb.getInt();
				for (int j=0; j<partAttribCount; j++) { //loop on attributes
					int attributeHash = bb.getInt();
					Attribute a = null;
					if (DBMPPlus.debug) System.out.println("0x%08x - %s".formatted(attributeHash, Hash.getBIN(attributeHash)));
					for (var h : attributesTwoString) if (h == attributeHash) {
						p.attributes.add(a = new AttributeTwoString(h, bb, useGUI));
						break;
					}
					for (var h : attributesString) if (h == attributeHash) {
						p.attributes.add(a = new AttributeString(h, bb, useGUI));
						break;
					}
					for (var h : attributesInteger) if (h == attributeHash) {
						p.attributes.add(a = new AttributeInteger(h, bb, useGUI));
						break;
					}
					for (var h : attributesCarPartID) if (h == attributeHash) {
						p.attributes.add(a = new AttributeCarPartID(h, bb, useGUI));
						break;
					}
					for (var h : attributesKey) if (h == attributeHash) {
						p.attributes.add(a = new AttributeKey(h, bb, useGUI));
						break;
					}
					for (var h : attributesBoolean) if (h == attributeHash) {
						p.attributes.add(a = new AttributeBoolean(h, bb, useGUI));
						break;
					}
					for (var h : attributesColor) if (h == attributeHash) {
						p.attributes.add(a = new AttributeColor(h, bb, useGUI));
						break;
					}
					for (var h : attributesFloating) if (h == attributeHash) {
						p.attributes.add(a = new AttributeFloating(h, bb, useGUI));
						break;
					}
					for (var h : attributesModelTable) if (h == attributeHash) {
						p.attributes.add(a = new AttributeModelTable(h, bb, useGUI));
						break;
					}
					assert (a != null);
					if (DBMPPlus.debug) System.out.println("Attribute " + (j+1) + "/" + partAttribCount + " : " + a.toString());
				}
				if (useGUI) p.update();
				if (DBMPPlus.debug)System.out.println("Part imported : "+p.displayName);
			}			
			
			
			//WHAT A HORRDENDOUS CODE
		} catch (FileNotFoundException e) {
			//dbmp to load not found
			// TODO Auto-generated catch block
			DBMPPlus.mainDBMP.GameInt = oldGameInt;
			if (useGUI) new Alert(Alert.AlertType.ERROR, "File not found!", ButtonType.OK).show();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			if (useGUI) new Alert(Alert.AlertType.ERROR, "Error while loading file: %s".formatted(e.getMessage()), ButtonType.OK).show();
			loadDBMP = null;
			DBMPPlus.mainDBMP.GameInt = oldGameInt;
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
