package apps;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import binstuff.Hash;

public class DBMPPlus {

	public static void main(String[] args) {
		/*
		System.out.println(new AttributeTwoString("PART_NAME_OFFSETS"));
		System.out.println(new AttributeCarPartID("PARTID_UPGRADE_GROUP"));
		System.out.println(new AttributeKey("CV"));
		
		Part p = new Part();
		p.attributes.add(new AttributeTwoString("PART_NAME_OFFSETS","KIT00","BUMPER_FRONT"));
		p.attributes.add(new AttributeTwoString("LOD_BASE_NAME","KIT00","BUMPER_FRONT"));
		System.out.println(p);
		p.update();
		System.out.println(p);
		((AttributeTwoString)p.editAttribute("PART_NAME_OFFSETS")).value2 = "BUMP";
		System.out.println(p);
		p.update();
		System.out.println(p);

		Part p2 = new Part();
		System.out.println(p2);
		p2.addAttribute(new AttributeTwoString("PART_NAME_OFFSETS","KIT00","BODY"));
		System.out.println(p2);
		p2.addAttribute(new AttributeTwoString("LOD_BASE_NAME","KIT00","BODY"));
		System.out.println(p2);
		((AttributeTwoString)p2.editAttribute("PART_NAME_OFFSETS")).value1 = "KIT11";
		p2.update();
		System.out.println(p2);
		
		DBMP testDBMP = new DBMP("NIS_240_SX_89");
		testDBMP.parts.add(p);
		testDBMP.parts.add(p2);
		System.out.println(testDBMP); */

		//DBMP loadTest = loadDBMP(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\dbmp step 8.bin"));
		DBMP loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"));
		System.out.println(loadTest.displayName());
		
	}

	public static DBMP loadDBMP(File f) {

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

					// recheck par rapport au fichier
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
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
}

class DBMP{
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
	
	public void updatePartNames() {
		for (Part p : parts) {
			p.update();
		}
	}
}

class Part {
	public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	public String displayName = "MISSING ATTRIBUTES";
	public void update() {
		String name1 = "MISSING ATTRIBUTES";
		String name2 = "MISSING ATTRIBUTES";
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals("PART_NAME_OFFSETS")) {
				AttributeTwoString att2S = (AttributeTwoString)attribute;
				name1 = att2S.value1 + "_" + att2S.value2;
			}
			if (attribute.Key.label.equals("LOD_BASE_NAME")) {
				AttributeTwoString att2S = (AttributeTwoString)attribute;
				name2 = att2S.value1 + "_" + att2S.value2;
			}
		}
		if (name2.equals(name1)) {
			displayName = name1;
		} else {
			displayName = name1 + " (reference to " + name2 + ")";
		}
	}
	
	public void addAttribute(Attribute a) {
		attributes.add(a);
		update();
	}
	public Attribute editAttribute(String a) {
		Attribute att = null;
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				att = attribute;
			}
		}
		return att;
	}
	public void removeAttribute(String a) {
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				attributes.remove(attribute);
			}
		}
		update();
	}
	
	public String toString() {
		String s = "Part " + displayName;
		for (Attribute a : attributes) {
			s += "; " + a;
		}
		return s;
	}
}




abstract class Attribute{
	public static String AttributeIdentifier = null;
	public Hash Key = null;
	public Attribute(String key) {
		Key = new Hash(key);
	}
	
	public Attribute(Hash key) {
		Key = key;
	}

	public void writeToFile() {
	}
	
	public String toString() {
		return "Generic " + super.toString();
	}
}

class AttributeString extends Attribute{
	public static String AttributeIdentifier = "String";
	public String value1 = "";
	public boolean value1Exists = false;
	public AttributeString(String key) {
		super(key);
	}
	public AttributeString(Hash key, ByteBuffer bb) {
		super(key);
		value1Exists = bb.get()==1;
		value1 = DBMPPlus.readString(bb);
	}
	public String toString() {
		return "String" + Key + ": " + value1 + "," + value1Exists;
	}
}

class AttributeTwoString extends Attribute{
	public static String AttributeIdentifier = "TwoString";
	public String value1 = "";
	public boolean value1Exists = false;
	public String value2 = "";
	public boolean value2Exists = false;
	public AttributeTwoString(String key) {
		super(key);
	}
	public AttributeTwoString(String key, String string1, String string2) {
		super(key);
		value1 = string1;
		value2 = string2;
		value1Exists = true;
		value2Exists = true;
	}
	public AttributeTwoString(Hash key, ByteBuffer bb) {
		super(key);
		value1Exists = bb.get()==1;
		value2Exists = bb.get()==1;
		value1 = DBMPPlus.readString(bb);
		value2 = DBMPPlus.readString(bb);
	}
	public String toString() {
		return "TwoString" + Key + ": " + value1 + "," + value1Exists + "," + value2 + "," + value2Exists;
	}
}

class AttributeInteger extends Attribute{
	public static String AttributeIdentifier = "Integer";
	public int value = 0;
	public AttributeInteger(String key) {
		super(key);
	}
	public AttributeInteger(Hash key, ByteBuffer bb) {
		super(key);
		value = bb.getInt();
	}
	public String toString() {
		return "Integer" + Key + ": " + value;
	}
}

class AttributeCarPartID extends Attribute{
	public static String AttributeIdentifier = "CarPartID";
	public SlotUndercover ID = SlotUndercover.INVALID;
	public byte level = 0;
	public AttributeCarPartID(String key) {
		super(key);
	}
	public AttributeCarPartID(Hash key, ByteBuffer bb) {
		super(key);
		level = bb.get();
		ID = SlotUndercover.get(bb.get());
	}
	public String toString() {
		return "CarPartID" + Key + ": " + ID + ", level " + level;
	}
}

class AttributeKey extends Attribute{
	public static String AttributeIdentifier = "Key";
	public Hash value = new Hash("");
	public AttributeKey(String key) {
		super(key);
	}
	public AttributeKey(Hash key, ByteBuffer bb) {
		super(key);
		value = new Hash(DBMPPlus.readString(bb));
	}
	public String toString() {
		return "CarPartID" + Key + ": " + value;
	}
}














enum SlotUndercover {
    BADGING_BUMPER_SET_FRONT(0x00),
    BADGING_BUMPER_SET_REAR(0x01),
    BADGING_FENDER_FRONT_LEFT(0x02),
    BADGING_FENDER_FRONT_RIGHT(0x03),
    BADGING_FENDER_REAR_LEFT(0x04),
    BADGING_FENDER_REAR_RIGHT(0x05),
    BADGING_TRUNK(0x06),
    BASE(0x07),
    BODY(0x08),
    BODY_DOORLINE(0x09),
    BRAKE_CALIPER_FRONT(0x0A),
    BRAKE_CALIPER_REAR(0x0B),
    BRAKE_ROTOR_FRONT(0x0C),
    BRAKE_ROTOR_REAR(0x0D),
    BRAKELIGHT_GLASS_LEFT(0x0E),
    BRAKELIGHT_GLASS_RIGHT(0x0F),
    BRAKELIGHT_LEFT(0x10),
    BRAKELIGHT_RIGHT(0x11),
    BUMPER_FRONT(0x12),
    BUMPER_FRONT_EXTRA(0x13),
    BUMPER_REAR(0x14),
    BUMPER_REAR_EXTRA(0x15),
    CHASSIS(0x16),
    DAMAGE_BODY(0x17),
    DAMAGE_BRAKELIGHT_LEFT(0x18),
    DAMAGE_BRAKELIGHT_RIGHT(0x19),
    DAMAGE_BUMPER_FRONT(0x1A),
    DAMAGE_BUMPER_REAR(0x1B),
    DAMAGE_DOOR_LEFT(0x1C),
    DAMAGE_DOOR_REAR_LEFT(0x1D),
    DAMAGE_DOOR_REAR_RIGHT(0x1E),
    DAMAGE_DOOR_RIGHT(0x1F),
    DAMAGE_FENDER_FRONT_LEFT(0x20),
    DAMAGE_FENDER_FRONT_RIGHT(0x21),
    DAMAGE_HEADLIGHT_LEFT(0x22),
    DAMAGE_HEADLIGHT_RIGHT(0x23),
    DAMAGE_HOOD(0x24),
    DAMAGE_SIDEMIRROR_LEFT(0x25),
    DAMAGE_SIDEMIRROR_RIGHT(0x26),
    DAMAGE_TRUNK(0x27),
    DAMAGE_TRUNK911(0x28),
    DAMAGE_WIDEBODY(0x29),
    DAMAGE_WIDEBODY_BUMPER_FRONT(0x2A),
    DAMAGE_WIDEBODY_BUMPER_REAR(0x2B),
    DAMAGE_WIDEBODY_DOOR_LEFT(0x2C),
    DAMAGE_WIDEBODY_DOOR_REAR_LEFT(0x2D),
    DAMAGE_WIDEBODY_DOOR_REAR_RIGHT(0x2E),
    DAMAGE_WIDEBODY_DOOR_RIGHT(0x2F),
    DAMAGE_WIDEBODY_FENDER_FRONT_LEFT(0x30),
    DAMAGE_WIDEBODY_FENDER_FRONT_RIGHT(0x31),
    DAMAGE_WINDOW_FRONT(0x32),
    DAMAGE_WINDOW_FRONT_LEFT(0x33),
    DAMAGE_WINDOW_FRONT_RIGHT(0x34),
    DAMAGE_WINDOW_REAR(0x35),
    DAMAGE_WINDOW_REAR_LEFT(0x36),
    DAMAGE_WINDOW_REAR_RIGHT(0x37),
    DECAL_FRONT_WINDOW(0x38),
    DECAL_REAR_WINDOW(0x39),
    DOOR_LEFT(0x3A),
    DOOR_REAR_LEFT(0x3B),
    DOOR_REAR_RIGHT(0x3C),
    DOOR_RIGHT(0x3D),
    DOORHANDLE_FRONT_LEFT(0x3E),
    DOORHANDLE_FRONT_RIGHT(0x3F),
    DOORHANDLE_REAR_LEFT(0x40),
    DOORHANDLE_REAR_RIGHT(0x41),
    DRIVER(0x42),
    ENGINE(0x43),
    EXHAUST(0x44),
    EXHAUST_TIPS_CENTER(0x45),
    EXHAUST_TIPS_LEFT(0x46),
    EXHAUST_TIPS_RIGHT(0x47),
    FENDER_FRONT_LEFT(0x48),
    FENDER_FRONT_RIGHT(0x49),
    HEADLIGHT_GLASS_LEFT(0x4A),
    HEADLIGHT_GLASS_RIGHT(0x4B),
    HEADLIGHT_LEFT(0x4C),
    HEADLIGHT_RIGHT(0x4D),
    HOOD(0x4E),
    LICENSE_PLATE(0x4F),
    LIGHTBAR(0x50),
    ROLL_CAGE(0x51),
    ROOF(0x52),
    ROOF_INSIDE(0x53),
    ROOFSCOOP(0x54),
    SEAT_LEFT(0x55),
    SEAT_RIGHT(0x56),
    SIDEMIRROR_LEFT(0x57),
    SIDEMIRROR_RIGHT(0x58),
    SKIRT_LEFT(0x59),
    SKIRT_RIGHT(0x5A),
    SPOILER(0x5B),
    STEERINGWHEEL(0x5C),
    TRUNK(0x5D),
    UNIVERSAL_SPOILER_BASE(0x5E),
    WHEEL_FRONT(0x5F),
    WHEEL_REAR(0x60),
    WINDOW_FRONT(0x61),
    WINDOW_FRONT_LEFT(0x62),
    WINDOW_FRONT_RIGHT(0x63),
    WINDOW_REAR(0x64),
    WINDOW_REAR_LEFT(0x65),
    WINDOW_REAR_RIGHT(0x66),
    BRAKE_PACKAGE(0x67),
    DRIVETRAIN_PACKAGE(0x68),
    ENGINE_PACKAGE(0x69),
    FORCED_INDUCTION_PACKAGE(0x6A),
    INTERIOR(0x6B),
    LIVERY(0x6C),
    NITROUS_PACKAGE(0x6D),
    SUSPENSION_PACKAGE(0x6E),
    TIRE_FRONT(0x6F),
    TIRE_PACKAGE(0x70),
    TIRE_REAR(0x71),
    VECTORVINYL(0x72),
    INVALID(0xFF);

    private final byte value;

    private SlotUndercover(int value) {
        this.value = (byte)value;
    }

    public int getValue() {
        return value;
    }




    public static SlotUndercover get(int value) { 
        for(SlotUndercover s : values()) {
            if(s.value == value) return s;
        }
        return null;
    }
    
    
}