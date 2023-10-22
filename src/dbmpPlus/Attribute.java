package dbmpPlus;

import java.nio.ByteBuffer;

import binstuff.Hash;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

abstract class Attribute {
	
//	public String AttributeIdentifier;
	public Hash Key = null;
	public String displayName = "MISSING";
	public HBox dataHBox = new HBox(10);

	public Attribute(String key) {
		Key = new Hash(key);
		displayName = Key.label;
		dataHBox.getChildren().addAll(new Label(displayName));
	}
	
	public Attribute(Hash key) {
		Key = key;
		displayName = Key.label;
		dataHBox.getChildren().addAll(new Label(displayName));
	}

	public Attribute(Attribute copyFrom) {
		Key = copyFrom.Key;
		displayName = copyFrom.displayName;
		dataHBox.getChildren().addAll(new Label(displayName));
	}
	
	public void writeToFile(ByteBuffer bb) {
		bb.putInt(Key.binHash);
	}
	
	public void revertFrom(Attribute a) {
		
	}
	
	public void update() {
		
	}
	
	public String getAttribType() {
		return null;
	}
	
	public String toString() {
		return "Generic " + super.toString();
	}
}

class AttributeString extends Attribute{
//	public static final String AttributeIdentifier = "String";
	public String value1 = "";
	public byte value1Exists = 0;
	TextField value1gui = new TextField();
	public AttributeString(String key) {
		super(key);
		dataHBox.getChildren().addAll(value1gui);
	}
	public AttributeString(String key, String value1) {
		super(key);
		this.value1 = value1.strip();
		if(!value1.isBlank()) value1Exists = 1;
		initGUI();
	}
	public AttributeString(Hash key, ByteBuffer bb) {
		super(key);
		value1Exists = bb.get();
		value1 = DBMP.readString(bb);
		initGUI();
	}
	public AttributeString(AttributeString copyFrom) {
		super(copyFrom);
		this.value1Exists = copyFrom.value1Exists;
		this.value1 = copyFrom.value1;
		initGUI();
	}
	public void initGUI() {
		value1gui.setText(value1);
		dataHBox.getChildren().addAll(value1gui);
		value1gui.setOnAction(e -> {
			new UndoAttributeChange(this);
			value1 = value1gui.getText().strip();
			value1gui.setText(value1gui.getText().strip());
			if (value1.isEmpty()) value1Exists = 0; else value1Exists = 1;
			e.consume();
		});
	}
	public void update() {
		value1gui.setText(value1);
	}
	public void revertFrom(Attribute a) {
		this.value1 = ((AttributeString)a).value1;
		this.value1Exists = ((AttributeString)a).value1Exists;
		value1gui.setText(value1);
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(value1Exists);
		DBMP.writeString(value1, bb);
	}
	public String getAttribType() {
		return "String";
	}
	public String toString() {
		return "String" + Key + ": " + value1 + "," + (value1Exists==1);
	}
}

class AttributeTwoString extends Attribute{
//	public static final String AttributeIdentifier = "TwoString";
	public String value1 = "";
	public byte value1Exists = 0;
	public String value2 = "";
	public byte value2Exists = 0;
	TextField value1gui = new TextField();
	TextField value2gui = new TextField();
	public AttributeTwoString(String key) {
		super(key);
		initGUI();
	}
	public AttributeTwoString(String key, String string1, String string2) {
		super(key);
		value1 = string1.strip();
		value2 = string2.strip();
		if(!value1.isBlank()) value1Exists = 1;
		if(!value2.isBlank()) value2Exists = 1;
		initGUI();
	}
	public AttributeTwoString(Hash key, ByteBuffer bb) {
		super(key);
		value1Exists = bb.get();
		value2Exists = bb.get();
		value1 = DBMP.readString(bb);
		value2 = DBMP.readString(bb);
		initGUI();
	}
	public AttributeTwoString(AttributeTwoString copyFrom) {
		super(copyFrom);
		this.value1Exists = copyFrom.value1Exists;
		this.value1 = copyFrom.value1;
		this.value2Exists = copyFrom.value2Exists;
		this.value2 = copyFrom.value2;
		initGUI();
	}
	public void initGUI() {
		value1gui.setText(value1);
		value2gui.setText(value2);
		value1gui.setPrefWidth(60);
		dataHBox.getChildren().addAll(value1gui,value2gui);

		value1gui.setOnAction(e -> {
			new UndoAttributeChange(this);
			value1 = value1gui.getText().strip();
			value1gui.setText(value1gui.getText().strip());
			if (value1.isEmpty()) value1Exists = 0; else value1Exists = 1;
			DBMPPlus.updateAllPartsDisplay();
			e.consume();
		});
		value2gui.setOnAction(e -> {
			new UndoAttributeChange(this);
			value2 = value2gui.getText().strip();
			value2gui.setText(value2gui.getText().strip());
			if (value2.isEmpty()) value2Exists = 0; else value2Exists = 1;
			e.consume();
			DBMPPlus.updateAllPartsDisplay();
		});
	}
	public void update() {
		value1gui.setText(value1);
		value2gui.setText(value2);
	}
	public void revertFrom(Attribute a) {
		this.value1 = ((AttributeTwoString)a).value1;
		this.value1Exists = ((AttributeTwoString)a).value1Exists;
		this.value2 = ((AttributeTwoString)a).value2;
		this.value2Exists = ((AttributeTwoString)a).value2Exists;
		value1gui.setText(value1);
		value2gui.setText(value2);
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(value1Exists);
		bb.put(value2Exists);
		DBMP.writeString(value1, bb);
		DBMP.writeString(value2, bb);
	}
	public String getAttribType() {
		return "TwoString";
	}
	public String toString() {
		return "TwoString" + Key + ": " + value1 + "," + (value1Exists==1) + "," + value2 + "," + (value2Exists==1);
	}
}

class AttributeInteger extends Attribute{
//	public static String AttributeIdentifier = "Integer";
	public int value = 0;
	TextField valuegui = new TextField();
	public AttributeInteger(String key) {
		super(key);
		initGUI();
	}
	public AttributeInteger(String key, int value) {
		super(key);
		this.value = value;
		initGUI();
	}
	public AttributeInteger(Hash key, ByteBuffer bb) {
		super(key);
		value = bb.getInt();
		initGUI();
	}
	public AttributeInteger(AttributeInteger copyFrom) {
		super(copyFrom);
		this.value = copyFrom.value;
		initGUI();
	}
	public void initGUI() {
		valuegui.setText(Integer.toString(value));
		dataHBox.getChildren().addAll(valuegui);
		valuegui.setOnAction(e -> {
			new UndoAttributeChange(this);
			try {
				value = Integer.parseInt(valuegui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid integer", ButtonType.OK).show();
			}
			valuegui.setText(Integer.toString(value));
			e.consume();
		});
	}
	public void update() {
		valuegui.setText(Integer.toString(value));
	}
	public void revertFrom(Attribute a) {
		this.value = ((AttributeInteger)a).value;
		valuegui.setText(Integer.toString(value));
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.putInt(value);
	}
	public String getAttribType() {
		return "Integer";
	}
	public String toString() {
		return "Integer" + Key + ": " + value;
	}
}

class AttributeCarPartID extends Attribute{
//	public static String AttributeIdentifier = "CarPartID";
	public PartUndercover ID = PartUndercover.INVALID;
	public byte level = 0;
	TextField IDgui = new TextField();
	TextField levelgui = new TextField();
	public AttributeCarPartID(String key) {
		super(key);
		initGUI();
	}
	public AttributeCarPartID(String key, PartUndercover ID, int level) {
		super(key);
		this.ID = ID;
		this.level = (byte)level;
		initGUI();
	}
	public AttributeCarPartID(Hash key, ByteBuffer bb) {
		super(key);
		level = bb.get();
		ID = PartUndercover.get(bb.get());
		initGUI();
	}
	public AttributeCarPartID(AttributeCarPartID copyFrom) {
		super(copyFrom);
		this.ID = copyFrom.ID;
		this.level = copyFrom.level;
		initGUI();
	}
	public void initGUI() {
		IDgui.setText(ID.getText());
		levelgui.setPrefWidth(40);
		levelgui.setText(Integer.toString(level));
		dataHBox.getChildren().addAll(levelgui, IDgui);

		IDgui.setOnAction(e -> {
			new UndoAttributeChange(this);
			if(PartUndercover.get(IDgui.getText().strip())!=null) {
				ID = PartUndercover.get(IDgui.getText().strip());
			} else new Alert(Alert.AlertType.ERROR, "Invalid slot", ButtonType.OK).show();
			IDgui.setText(ID.getText());
			e.consume();
		});
		levelgui.setOnAction(e -> {
			new UndoAttributeChange(this);
			try {
				level = (byte) Integer.parseInt(levelgui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid integer", ButtonType.OK).show();
			}
			levelgui.setText(Integer.toString(level));
			e.consume();
		});
	}
	public void update() {
		IDgui.setText(ID.getText());
		levelgui.setText(Integer.toString(level));
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(level);
		bb.put(ID.getValue());
	}
	public void revertFrom(Attribute a) {
		this.ID = ((AttributeCarPartID)a).ID;
		this.level = ((AttributeCarPartID)a).level;
		IDgui.setText(ID.getText());
		levelgui.setText(Integer.toString(level));
	}
	public String getAttribType() {
		return "CarPartID";
	}
	public String toString() {
		return "CarPartID" + Key + ": " + ID + "/" + ID.getValue() + ", level " + level;
	}
}

class AttributeKey extends Attribute{
//	public static String AttributeIdentifier = "Key";
	public Hash value = new Hash("");
	TextField valuegui = new TextField();
	public AttributeKey(String key) {
		super(key);
		initGUI();
	}
	public AttributeKey(String key, String value) {
		super(key);
		this.value = new Hash(value);
		initGUI();
	}
	public AttributeKey(Hash key, ByteBuffer bb) {
		super(key);
		value = new Hash(DBMP.readString(bb));
		initGUI();
	}
	public AttributeKey(AttributeKey copyFrom) {
		super(copyFrom);
		this.value = copyFrom.value;
		initGUI();
	}
	public void initGUI() {
		valuegui.setText(this.value.label);
		dataHBox.getChildren().addAll(valuegui);

		valuegui.setOnAction(e -> {
			new UndoAttributeChange(this);
			value = new Hash(valuegui.getText().strip());
			valuegui.setText(value.label);
			e.consume();
		});
	}
	public void update() {
		valuegui.setText(this.value.label);
	}
	public void revertFrom(Attribute a) {
		this.value = ((AttributeKey)a).value;
		valuegui.setText(value.label);
	}
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		DBMP.writeString(value.label, bb);
	}
	public String getAttribType() {
		return "Key";
	}
	public String toString() {
		return "Key" + Key + ": " + value;
	}
}

enum PartUndercover {
    BADGING_BUMPER_SET_FRONT(0x00, "BADGING_BUMPER_SET_FRONT"),
    BADGING_BUMPER_SET_REAR(0x01, "BADGING_BUMPER_SET_REAR"),
    BADGING_FENDER_FRONT_LEFT(0x02, "BADGING_FENDER_FRONT_LEFT"),
    BADGING_FENDER_FRONT_RIGHT(0x03, "BADGING_FENDER_FRONT_RIGHT"),
    BADGING_FENDER_REAR_LEFT(0x04, "BADGING_FENDER_REAR_LEFT"),
    BADGING_FENDER_REAR_RIGHT(0x05, "BADGING_FENDER_REAR_RIGHT"),
    BADGING_TRUNK(0x06, "BADGING_TRUNK"),
    BASE(0x07, "BASE"),
    BODY(0x08, "BODY"),
    BODY_DOORLINE(0x09, "BODY_DOORLINE"),
    BRAKE(0x0A, "BRAKE"),
    BRAKELIGHT_GLASS_LEFT(0x0B, "BRAKELIGHT_GLASS_LEFT"),
    BRAKELIGHT_GLASS_RIGHT(0x0C, "BRAKELIGHT_GLASS_RIGHT"),
    BRAKELIGHT_LEFT(0x0D, "BRAKELIGHT_LEFT"),
    BRAKELIGHT_RIGHT(0x0E, "BRAKELIGHT_RIGHT"),
    BRAKEROTOR(0x0F, "BRAKEROTOR"),
    BUMPER_FRONT(0x10, "BUMPER_FRONT"),
    BUMPER_FRONT_EXTRA(0x11, "BUMPER_FRONT_EXTRA"),
    BUMPER_REAR(0x12, "BUMPER_REAR"),
    BUMPER_REAR_EXTRA(0x13, "BUMPER_REAR_EXTRA"),
    CHASSIS(0x14, "CHASSIS"),
    DAMAGE_BODY(0x15, "DAMAGE_BODY"),
    DAMAGE_BRAKELIGHT_LEFT(0x16, "DAMAGE_BRAKELIGHT_LEFT"),
    DAMAGE_BRAKELIGHT_RIGHT(0x17, "DAMAGE_BRAKELIGHT_RIGHT"),
    DAMAGE_BUMPER_FRONT(0x18, "DAMAGE_BUMPER_FRONT"),
    DAMAGE_BUMPER_REAR(0x19, "DAMAGE_BUMPER_REAR"),
    DAMAGE_DOOR_LEFT(0x1A, "DAMAGE_DOOR_LEFT"),
    DAMAGE_DOOR_REAR_LEFT(0x1B, "DAMAGE_DOOR_REAR_LEFT"),
    DAMAGE_DOOR_REAR_RIGHT(0x1C, "DAMAGE_DOOR_REAR_RIGHT"),
    DAMAGE_DOOR_RIGHT(0x1D, "DAMAGE_DOOR_RIGHT"),
    DAMAGE_FENDER_FRONT_LEFT(0x1E, "DAMAGE_FENDER_FRONT_LEFT"),
    DAMAGE_FENDER_FRONT_RIGHT(0x1F, "DAMAGE_FENDER_FRONT_RIGHT"),
    DAMAGE_HEADLIGHT_LEFT(0x20, "DAMAGE_HEADLIGHT_LEFT"),
    DAMAGE_HEADLIGHT_RIGHT(0x21, "DAMAGE_HEADLIGHT_RIGHT"),
    DAMAGE_HOOD(0x22, "DAMAGE_HOOD"),
    DAMAGE_SIDEMIRROR_LEFT(0x23, "DAMAGE_SIDEMIRROR_LEFT"),
    DAMAGE_SIDEMIRROR_RIGHT(0x24, "DAMAGE_SIDEMIRROR_RIGHT"),
    DAMAGE_TRUNK(0x25, "DAMAGE_TRUNK"),
    DAMAGE_WINDOW_FRONT(0x26, "DAMAGE_WINDOW_FRONT"),
    DAMAGE_WINDOW_FRONT_LEFT(0x27, "DAMAGE_WINDOW_FRONT_LEFT"),
    DAMAGE_WINDOW_FRONT_RIGHT(0x28, "DAMAGE_WINDOW_FRONT_RIGHT"),
    DAMAGE_WINDOW_REAR(0x29, "DAMAGE_WINDOW_REAR"),
    DAMAGE_WINDOW_REAR_LEFT(0x2A, "DAMAGE_WINDOW_REAR_LEFT"),
    DAMAGE_WINDOW_REAR_RIGHT(0x2B, "DAMAGE_WINDOW_REAR_RIGHT"),
    DECAL_FRONT_WINDOW(0x2C, "DECAL_FRONT_WINDOW"),
    DECAL_REAR_WINDOW(0x2D, "DECAL_REAR_WINDOW"),
    DOOR_LEFT(0x2E, "DOOR_LEFT"),
    DOOR_REAR_LEFT(0x2F, "DOOR_REAR_LEFT"),
    DOOR_REAR_RIGHT(0x30, "DOOR_REAR_RIGHT"),
    DOOR_RIGHT(0x31, "DOOR_RIGHT"),
    DOORHANDLE_FRONT_LEFT(0x32, "DOORHANDLE_FRONT_LEFT"),
    DOORHANDLE_FRONT_RIGHT(0x33, "DOORHANDLE_FRONT_RIGHT"),
    DOORHANDLE_REAR_LEFT(0x34, "DOORHANDLE_REAR_LEFT"),
    DOORHANDLE_REAR_RIGHT(0x35, "DOORHANDLE_REAR_RIGHT"),
    DRIVER(0x36, "DRIVER"),
    ENGINE(0x37, "ENGINE"),
    EXHAUST(0x38, "EXHAUST"),
    EXHAUST_TIPS_CENTER(0x39, "EXHAUST_TIPS_CENTER"),
    EXHAUST_TIPS_LEFT(0x3A, "EXHAUST_TIPS_LEFT"),
    EXHAUST_TIPS_RIGHT(0x3B, "EXHAUST_TIPS_RIGHT"),
    FENDER_FRONT_LEFT(0x3C, "FENDER_FRONT_LEFT"),
    FENDER_FRONT_RIGHT(0x3D, "FENDER_FRONT_RIGHT"),
    HEADLIGHT_GLASS_LEFT(0x3E, "HEADLIGHT_GLASS_LEFT"),
    HEADLIGHT_GLASS_RIGHT(0x3F, "HEADLIGHT_GLASS_RIGHT"),
    HEADLIGHT_LEFT(0x40, "HEADLIGHT_LEFT"),
    HEADLIGHT_RIGHT(0x41, "HEADLIGHT_RIGHT"),
    HOOD(0x42, "HOOD"),
    LIGHTBAR(0x43, "LIGHTBAR"),
    PLATES(0x44, "PLATES"),
    ROLL_CAGE(0x45, "ROLL_CAGE"),
    ROOF(0x46, "ROOF"),
    ROOF_INSIDE(0x47, "ROOF_INSIDE"),
    ROOFSCOOP(0x48, "ROOFSCOOP"),
    SEAT(0x49, "SEAT"),
    SIDEMIRROR_LEFT(0x4A, "SIDEMIRROR_LEFT"),
    SIDEMIRROR_RIGHT(0x4B, "SIDEMIRROR_RIGHT"),
    SKIRT_LEFT(0x4C, "SKIRT_LEFT"),
    SKIRT_RIGHT(0x4D, "SKIRT_RIGHT"),
    SPOILER(0x4E, "SPOILER"),
    STEERINGWHEEL(0x4F, "STEERINGWHEEL"),
    TRUNK(0x50, "TRUNK"),
    UNIVERSAL_SPOILER_BASE(0x51, "UNIVERSAL_SPOILER_BASE"),
    WHEEL(0x52, "WHEEL"),
    WHEEL_REAR(0x53, "WHEEL_REAR"),
    WINDOW_FRONT(0x54, "WINDOW_FRONT"),
    WINDOW_FRONT_LEFT(0x55, "WINDOW_FRONT_LEFT"),
    WINDOW_FRONT_RIGHT(0x56, "WINDOW_FRONT_RIGHT"),
    WINDOW_REAR(0x57, "WINDOW_REAR"),
    WINDOW_REAR_LEFT(0x58, "WINDOW_REAR_LEFT"),
    WINDOW_REAR_RIGHT(0x59, "WINDOW_REAR_RIGHT"),
    BRAKE_PACKAGE(0x5A, "BRAKE_PACKAGE"),
    DRIVETRAIN_PACKAGE(0x5B, "DRIVETRAIN_PACKAGE"),
    ENGINE_PACKAGE(0x5C, "ENGINE_PACKAGE"),
    FORCED_INDUCTION_PACKAGE(0x5D, "FORCED_INDUCTION_PACKAGE"),
    INTERIOR(0x5E, "INTERIOR"),
    LIVERY(0x5F, "LIVERY"),
    NITROUS_PACKAGE(0x60, "NITROUS_PACKAGE"),
    SUSPENSION_PACKAGE(0x61, "SUSPENSION_PACKAGE"),
    TIRE(0x62, "TIRE"),
    TIRE_PACKAGE(0x63, "TIRE_PACKAGE"),
    VECTORVINYL(0x64, "VECTORVINYL"),
    INVALID(0xFF, "INVALID");

    private final byte value;
    private final String name;

    private PartUndercover(int value, String text) {
        this.value = (byte) value;
        this.name = text;
    }

    public byte getValue() {
        return value;
    }

    public String getText() {
        return name;
    }

    public static PartUndercover get(int value) {
        for (PartUndercover s : values()) {
            if (s.value == value) return s;
        }
        return PartUndercover.INVALID;
    }

    public static PartUndercover get(String name) {
        for (PartUndercover s : values()) {
            if (s.name.equals(name)) return s;
        }
        return PartUndercover.INVALID;
    }
    
}
