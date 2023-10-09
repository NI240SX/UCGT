package dbmpPlus;

import java.nio.ByteBuffer;

import binstuff.Hash;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

abstract class Attribute extends HBox{
	
	public static String AttributeIdentifier = null;
	public Hash Key = null;

	Label uiLabel = new Label("MISSING");
	
	public Attribute(String key) {
		Key = new Hash(key);
        this.getChildren().addAll(uiLabel);
	}
	
	public Attribute(Hash key) {
		Key = key;
        this.getChildren().addAll(uiLabel);
	}

	public void writeToFile(ByteBuffer bb) {
		bb.putInt(Key.binHash);
	}
	
	public String toString() {
		return "Generic " + super.toString();
	}
}

class AttributeString extends Attribute{
	public static String AttributeIdentifier = "String";
	public String value1 = "";
	public byte value1Exists = 0;
	public AttributeString(String key) {
		super(key);
	}
	public AttributeString(Hash key, ByteBuffer bb) {
		super(key);
		value1Exists = bb.get();
		value1 = DBMP.readString(bb);
		uiLabel.setText(this.toString());
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(value1Exists);
		DBMP.writeString(value1, bb);
	}
	public String toString() {
		return "String" + Key + ": " + value1 + "," + (value1Exists==1);
	}
}

class AttributeTwoString extends Attribute{
	public static String AttributeIdentifier = "TwoString";
	public String value1 = "";
	public byte value1Exists = 0;
	public String value2 = "";
	public byte value2Exists = 0;
	public AttributeTwoString(String key) {
		super(key);
	}
	public AttributeTwoString(String key, String string1, String string2) {
		super(key);
		value1 = string1;
		value2 = string2;
		value1Exists = 1;
		value2Exists = 1;
		uiLabel.setText(this.toString());
	}
	public AttributeTwoString(Hash key, ByteBuffer bb) {
		super(key);
		value1Exists = bb.get();
		value2Exists = bb.get();
		value1 = DBMP.readString(bb);
		value2 = DBMP.readString(bb);
		uiLabel.setText(this.toString());
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(value1Exists);
		bb.put(value2Exists);
		DBMP.writeString(value1, bb);
		DBMP.writeString(value2, bb);
	}
	public String toString() {
		return "TwoString" + Key + ": " + value1 + "," + (value1Exists==1) + "," + value2 + "," + (value2Exists==1);
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
		uiLabel.setText(this.toString());
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.putInt(value);
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
		uiLabel.setText(this.toString());
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(level);
		bb.put(ID.getValue());
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
		value = new Hash(DBMP.readString(bb));
		uiLabel.setText(this.toString());
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		DBMP.writeString(value.label, bb);
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

    public byte getValue() {
        return value;
    }




    public static SlotUndercover get(int value) { 
        for(SlotUndercover s : values()) {
            if(s.value == value) return s;
        }
        return null;
    }
    
    
}