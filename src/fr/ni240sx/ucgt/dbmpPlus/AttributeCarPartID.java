package fr.ni240sx.ucgt.dbmpPlus;

import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.dbmpPlus.DBMPPlus.Game;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AttributeCarPartID extends Attribute{
//	public static String AttributeIdentifier = "CarPartID";
//	public PartUndercover ID = PartUndercover.INVALID;
	public byte ID = -1;
	public byte level = 0;
	ComboBox<PartNFS> IDgui; //THIS SHOULDNT BE HERE...
	TextField levelgui;
	public AttributeCarPartID(String key) {
		super(key);
		IDgui = new ComboBox<>();
		levelgui = new TextField();
		initGUI();
	}
	public AttributeCarPartID(String key, byte ID, int level) {
		super(key);
		this.ID = ID;
		this.level = (byte)level;
		IDgui = new ComboBox<>();
		levelgui = new TextField();
		initGUI();
	}
	public AttributeCarPartID(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		level = bb.get();
		ID = bb.get();
		if (useGUI) {
			IDgui = new ComboBox<>();
			levelgui = new TextField();
			initGUI();
		}
	}
	public AttributeCarPartID(AttributeCarPartID copyFrom) {
		super(copyFrom);
		this.ID = copyFrom.ID;
		this.level = copyFrom.level;
		IDgui = new ComboBox<>();
		levelgui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		IDgui.getItems().addAll(PartNFS.values());
		IDgui.getSelectionModel().select(PartNFS.get(ID));
		levelgui.setPrefWidth(40);
		levelgui.setText(Integer.toString(level));
		dataHBox.getChildren().addAll(levelgui, IDgui);

		IDgui.setOnAction(e -> {
			ID = IDgui.getValue().getValue();
//			int caretB4Save = IDgui.getCaretPosition();
			new UndoAttributeChange(this);
//			if(PartUndercover.get(IDgui.getText().strip())!=null) {
//				ID = PartUndercover.get(IDgui.getText().strip());
//			} else new Alert(Alert.AlertType.ERROR, "Invalid slot", ButtonType.OK).show();
//			IDgui.setText(ID.getText());
//			IDgui.positionCaret(caretB4Save);
			e.consume();
		});
		levelgui.setOnKeyTyped(e -> {
			int caretB4Save = levelgui.getCaretPosition();
			if (levelgui.getText().isBlank()) levelgui.setText("0");
			new UndoAttributeChange(this);
			try {
				level = (byte) Integer.parseInt(levelgui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid integer", ButtonType.OK).show();
			}
			levelgui.setText(Integer.toString(level));
			levelgui.positionCaret(caretB4Save);
			e.consume();
		});
	}
	@Override
	public void update() {
//		IDgui.setText(ID.getText());
		IDgui.getSelectionModel().select(PartNFS.get(ID));
		levelgui.setText(Integer.toString(level));
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(level);
		bb.put(ID);
	}
	@Override
	public void revertFrom(Attribute a) {
		this.ID = ((AttributeCarPartID)a).ID;
		this.level = ((AttributeCarPartID)a).level;
//		IDgui.setText(ID.getText());
		IDgui.getSelectionModel().select(PartNFS.get(ID));
		levelgui.setText(Integer.toString(level));
	}
	@Override
	public String getAttribType() {
		return "CarPartID";
	}
	@Override
	public String toString() {
		return "CarPartID" + Key + ": " + PartNFS.get(ID) + "/" + ID + ", level " + level;
	}
}


interface PartNFS{
	public byte getValue();
	public String name();

    public static PartNFS[] values() {
    	switch(Game.get(DBMPPlus.mainDBMP.GameInt)) { //TODO not ideal but will have to do
		case Undercover:
			return PartUndercover.values();
		case Prostreet:
			return PartProstreet.values();
		case Carbon:
			return PartCarbon.values();
		case None:
		case Underground:
		case Underground2:
		case MostWanted:
    	}
		return new PartUndercover[0];
	}

	public static PartNFS get (int value) {return get(value, DBMPPlus.mainDBMP.GameInt);}
	public static PartNFS get(int value, int game) {
    	switch(Game.get(game)) { //TODO not ideal but will have to do
		case Undercover:
			return PartUndercover.get(value);
		case Prostreet:
			return PartProstreet.get(value);
		case Carbon:
			return PartCarbon.get(value);
		case None:
		case Underground:
		case Underground2:
		case MostWanted:
    	}
		return null;
    }
	public static PartNFS get (String value) {return get(value, DBMPPlus.mainDBMP.GameInt);}
    public static PartNFS get(String value, int game) {
    	switch(Game.get(game)) { //TODO not ideal but will have to do
		case Undercover:
			return PartUndercover.get(value);
		case Prostreet:
			return PartProstreet.get(value);
		case Carbon:
			return PartCarbon.get(value);
		case None:
		case Underground:
		case Underground2:
		case MostWanted:
    	}
		return null;
    }
}


enum PartUndercover implements PartNFS{
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
    BRAKE(0x0A),
    BRAKELIGHT_GLASS_LEFT(0x0B),
    BRAKELIGHT_GLASS_RIGHT(0x0C),
    BRAKELIGHT_LEFT(0x0D),
    BRAKELIGHT_RIGHT(0x0E),
    BRAKEROTOR(0x0F),
    BUMPER_FRONT(0x10),
    BUMPER_FRONT_EXTRA(0x11),
    BUMPER_REAR(0x12),
    BUMPER_REAR_EXTRA(0x13),
    CHASSIS(0x14),
    DAMAGE_BODY(0x15),
    DAMAGE_BRAKELIGHT_LEFT(0x16),
    DAMAGE_BRAKELIGHT_RIGHT(0x17),
    DAMAGE_BUMPER_FRONT(0x18),
    DAMAGE_BUMPER_REAR(0x19),
    DAMAGE_DOOR_LEFT(0x1A),
    DAMAGE_DOOR_REAR_LEFT(0x1B),
    DAMAGE_DOOR_REAR_RIGHT(0x1C),
    DAMAGE_DOOR_RIGHT(0x1D),
    DAMAGE_FENDER_FRONT_LEFT(0x1E),
    DAMAGE_FENDER_FRONT_RIGHT(0x1F),
    DAMAGE_HEADLIGHT_LEFT(0x20),
    DAMAGE_HEADLIGHT_RIGHT(0x21),
    DAMAGE_HOOD(0x22),
    DAMAGE_SIDEMIRROR_LEFT(0x23),
    DAMAGE_SIDEMIRROR_RIGHT(0x24),
    DAMAGE_TRUNK(0x25),
    DAMAGE_WINDOW_FRONT(0x26),
    DAMAGE_WINDOW_FRONT_LEFT(0x27),
    DAMAGE_WINDOW_FRONT_RIGHT(0x28),
    DAMAGE_WINDOW_REAR(0x29),
    DAMAGE_WINDOW_REAR_LEFT(0x2A),
    DAMAGE_WINDOW_REAR_RIGHT(0x2B),
    DECAL_FRONT_WINDOW(0x2C),
    DECAL_REAR_WINDOW(0x2D),
    DOOR_LEFT(0x2E),
    DOOR_REAR_LEFT(0x2F),
    DOOR_REAR_RIGHT(0x30),
    DOOR_RIGHT(0x31),
    DOORHANDLE_FRONT_LEFT(0x32),
    DOORHANDLE_FRONT_RIGHT(0x33),
    DOORHANDLE_REAR_LEFT(0x34),
    DOORHANDLE_REAR_RIGHT(0x35),
    DRIVER(0x36),
    ENGINE(0x37),
    EXHAUST(0x38),
    EXHAUST_TIPS_CENTER(0x39),
    EXHAUST_TIPS_LEFT(0x3A),
    EXHAUST_TIPS_RIGHT(0x3B),
    FENDER_FRONT_LEFT(0x3C),
    FENDER_FRONT_RIGHT(0x3D),
    HEADLIGHT_GLASS_LEFT(0x3E),
    HEADLIGHT_GLASS_RIGHT(0x3F),
    HEADLIGHT_LEFT(0x40),
    HEADLIGHT_RIGHT(0x41),
    HOOD(0x42),
    LIGHTBAR(0x43),
    PLATES(0x44),
    ROLL_CAGE(0x45),
    ROOF(0x46),
    ROOF_INSIDE(0x47),
    ROOFSCOOP(0x48),
    SEAT(0x49),
    SIDEMIRROR_LEFT(0x4A),
    SIDEMIRROR_RIGHT(0x4B),
    SKIRT_LEFT(0x4C),
    SKIRT_RIGHT(0x4D),
    SPOILER(0x4E),
    STEERINGWHEEL(0x4F),
    TRUNK(0x50),
    UNIVERSAL_SPOILER_BASE(0x51),
    WHEEL(0x52),
    WHEEL_REAR(0x53),
    WINDOW_FRONT(0x54),
    WINDOW_FRONT_LEFT(0x55),
    WINDOW_FRONT_RIGHT(0x56),
    WINDOW_REAR(0x57),
    WINDOW_REAR_LEFT(0x58),
    WINDOW_REAR_RIGHT(0x59),
    BRAKE_PACKAGE(0x5A),
    DRIVETRAIN_PACKAGE(0x5B),
    ENGINE_PACKAGE(0x5C),
    FORCED_INDUCTION_PACKAGE(0x5D),
    INTERIOR(0x5E),
    LIVERY(0x5F),
    NITROUS_PACKAGE(0x60),
    SUSPENSION_PACKAGE(0x61),
    TIRE(0x62),
    TIRE_PACKAGE(0x63),
    VECTORVINYL(0x64),
    INVALID(0xFF);

    private final byte value;
    private PartUndercover(int value) {this.value = (byte) value;}
	@Override
	public byte getValue() {return value;}

    public static PartUndercover get(int value) {
        for (var s : values()) if (s.value == value) return s;
        return INVALID;
    }
    public static PartUndercover get(String name) {
        for (var s : values()) if (s.name().equals(name)) return s;
        return INVALID;
    }   
}


enum PartProstreet implements PartNFS{
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
	BRAKE(0x0A),
	BRAKELIGHT_GLASS_LEFT(0x0B),
	BRAKELIGHT_GLASS_RIGHT(0x0C),
	BRAKELIGHT_LEFT(0x0D),
	BRAKELIGHT_RIGHT(0x0E),
	BRAKEROTOR(0x0F),
	BUMPER_FRONT(0x10),
	BUMPER_FRONT_EXTRA(0x11),
	BUMPER_REAR(0x12),
	BUMPER_REAR_EXTRA(0x13),
	DAMAGE_BODY(0x14),
	DAMAGE_BRAKELIGHT_LEFT(0x15),
	DAMAGE_BRAKELIGHT_RIGHT(0x16),
	DAMAGE_BUMPER_FRONT(0x17),
	DAMAGE_BUMPER_REAR(0x18),
	DAMAGE_DOOR_LEFT(0x19),
	DAMAGE_DOOR_REAR_LEFT(0x1A),
	DAMAGE_DOOR_REAR_RIGHT(0x1B),
	DAMAGE_DOOR_RIGHT(0x1C),
	DAMAGE_FENDER_FRONT_LEFT(0x1D),
	DAMAGE_FENDER_FRONT_RIGHT(0x1E),
	DAMAGE_HEADLIGHT_LEFT(0x1F),
	DAMAGE_HEADLIGHT_RIGHT(0x20),
	DAMAGE_HOOD(0x21),
	DAMAGE_SIDEMIRROR_LEFT(0x22),
	DAMAGE_SIDEMIRROR_RIGHT(0x23),
	DAMAGE_TRUNK(0x24),
	DAMAGE_WINDOW_FRONT(0x25),
	DAMAGE_WINDOW_FRONT_LEFT(0x26),
	DAMAGE_WINDOW_FRONT_RIGHT(0x27),
	DAMAGE_WINDOW_REAR(0x28),
	DAMAGE_WINDOW_REAR_LEFT(0x29),
	DAMAGE_WINDOW_REAR_RIGHT(0x2A),
	DAMAGE0_FRONT(0x2B),
	DAMAGE0_FRONTLEFT(0x2C),
	DAMAGE0_FRONTRIGHT(0x2D),
	DAMAGE0_REAR(0x2E),
	DAMAGE0_REARLEFT(0x2F),
	DAMAGE0_REARRIGHT(0x30),
	DECAL_FRONT_WINDOW(0x31),
	DECAL_REAR_WINDOW(0x32),
	DOOR_LEFT(0x33),
	DOOR_REAR_LEFT(0x34),
	DOOR_REAR_RIGHT(0x35),
	DOOR_RIGHT(0x36),
	DOORHANDLE_FRONT_LEFT(0x37),
	DOORHANDLE_FRONT_RIGHT(0x38),
	DOORHANDLE_REAR_LEFT(0x39),
	DOORHANDLE_REAR_RIGHT(0x3A),
	DRIVER(0x3B),
	ENGINE(0x3C),
	EXHAUST(0x3D),
	FENDER_FRONT_LEFT(0x3E),
	FENDER_FRONT_RIGHT(0x3F),
	HEADLIGHT_GLASS_LEFT(0x40),
	HEADLIGHT_GLASS_RIGHT(0x41),
	HEADLIGHT_LEFT(0x42),
	HEADLIGHT_RIGHT(0x43),
	HOOD(0x44),
	PLATES(0x45),
	ROLL_CAGE(0x46),
	ROOF(0x47),
	ROOF_INSIDE(0x48),
	ROOFSCOOP(0x49),
	SEAT(0x4A),
	SIDEMIRROR_LEFT(0x4B),
	SIDEMIRROR_RIGHT(0x4C),
	SKIRT_LEFT(0x4D),
	SKIRT_RIGHT(0x4E),
	SPOILER(0x4F),
	STEERINGWHEEL(0x50),
	TRUNK(0x51),
	UNIVERSAL_SPOILER_BASE(0x52),
	WHEEL(0x53),
	WHEEL_REAR(0x54),
	WINDOW_FRONT(0x55),
	WINDOW_FRONT_LEFT(0x56),
	WINDOW_FRONT_RIGHT(0x57),
	WINDOW_REAR(0x58),
	WINDOW_REAR_LEFT(0x59),
	WINDOW_REAR_RIGHT(0x5A),
	WINDTUNNEL(0x5B),
	BODY_PAINT_GROUP(0x5C),
	CHASSIS(0x5D),
	DRIVETRAIN_PACKAGE(0x5E),
	ENGINE_PACKAGE(0x5F),
	FORCED_INDUCTION_PACKAGE(0x60),
	INTERIOR(0x61),
	LIVERY(0x62),
	NITROUS_PACKAGE(0x63),
	PAINT(0x64),
	PAINT_SWATCH(0x65),
	RIM_PAINT(0x66),
	ROLLCAGE_PAINT(0x67),
	ROLLCAGE_PAINT_GROUP(0x68),
	SUSPENSION_PACKAGE(0x69),
	TIRE(0x6A),
	TIRE_PACKAGE(0x6B),
	VECTORVINYL(0x6C),
	VINYL_PAINT(0x6D),
	WHEEL_PAINT_GROUP(0x6E),
	WINDOW_TINT(0x6F),
	WINDOW_TINT_GROUP(0x70),
	INVALID(0xFF);
	
    private final byte value;
    private PartProstreet(int value) {this.value = (byte) value;}
	@Override
	public byte getValue() {return value;}

    public static PartProstreet get(int value) {
        for (var s : values()) if (s.value == value) return s;
        return INVALID;
    }
    public static PartProstreet get(String name) {
        for (var s : values()) if (s.name().equals(name)) return s;
        return INVALID;
    }
}

enum PartCarbon implements PartNFS{
	BASE(0x00),
	DAMAGE_0_FRONT_WINDOW(0x01),
	DAMAGE_0_BODY(0x02),
	DAMAGE_0_COP_LIGHTS(0x03),
	DAMAGE_0_COP_SPOILER(0x04),
	DAMAGE_0_FRONT_WHEEL(0x05),
	DAMAGE_0_LEFT_BRAKELIGHT(0x06),
	DAMAGE_0_RIGHT_BRAKELIGHT(0x07),
	DAMAGE_0_LEFT_HEADLIGHT(0x08),
	DAMAGE_0_RIGHT_HEADLIGHT(0x09),
	DAMAGE_0_HOOD(0x0A),
	DAMAGE_0_BUSHGUARD(0x0B),
	DAMAGE_0_FRONT_BUMPER(0x0C),
	DAMAGE_0_RIGHT_DOOR(0x0D),
	DAMAGE_0_RIGHT_REAR_DOOR(0x0E),
	DAMAGE_0_TRUNK(0x0F),
	DAMAGE_0_REAR_BUMPER(0x10),
	DAMAGE_0_REAR_LEFT_WINDOW(0x11),
	DAMAGE_0_FRONT_LEFT_WINDOW(0x12),
	DAMAGE_0_FRONT_RIGHT_WINDOW(0x13),
	DAMAGE_0_REAR_RIGHT_WINDOW(0x14),
	DAMAGE_0_LEFT_DOOR(0x15),
	DAMAGE_0_LEFT_REAR_DOOR(0x16),
	BODY(0x17),
	FRONT_BRAKE(0x18),
	FRONT_ROTOR(0x19),
	FRONT_LEFT_WINDOW(0x1A),
	FRONT_RIGHT_WINDOW(0x1B),
	FRONT_WINDOW(0x1C),
	INTERIOR(0x1D),
	LEFT_BRAKELIGHT(0x1E),
	LEFT_BRAKELIGHT_GLASS(0x1F),
	LEFT_HEADLIGHT(0x20),
	LEFT_HEADLIGHT_GLASS(0x21),
	LEFT_SIDE_MIRROR(0x22),
	REAR_BRAKE(0x23),
	REAR_ROTOR(0x24),
	REAR_LEFT_WINDOW(0x25),
	REAR_RIGHT_WINDOW(0x26),
	REAR_WINDOW(0x27),
	RIGHT_BRAKELIGHT(0x28),
	RIGHT_BRAKELIGHT_GLASS(0x29),
	RIGHT_HEADLIGHT(0x2A),
	RIGHT_HEADLIGHT_GLASS(0x2B),
	RIGHT_SIDE_MIRROR(0x2C),
	DRIVER(0x2D),
	STEERINGWHEEL(0x2E),
	EXHAUST(0x2F),
	SPOILER(0x30),
	UNIVERSAL_SPOILER_BASE(0x31),
	DAMAGE_FRONT(0x32),
	DAMAGE_FRONT_LEFT(0x33),
	DAMAGE_FRONT_RIGHT(0x34),
	DAMAGE_REAR(0x35),
	DAMAGE_REAR_LEFT(0x36),
	DAMAGE_REAR_RIGHT(0x37),
	ATTACHMENT0(0x38),
	ATTACHMENT1(0x39),
	ATTACHMENT2(0x3A),
	ATTACHMENT3(0x3B),
	ATTACHMENT4(0x3C),
	ATTACHMENT5(0x3D),
	ATTACHMENT6(0x3E),
	ATTACHMENT7(0x3F),
	ATTACHMENT8(0x40),
	ATTACHMENT9(0x41),
	ATTACHMENT10(0x42),
	ATTACHMENT11(0x43),
	ATTACHMENT12(0x44),
	ATTACHMENT13(0x45),
	ATTACHMENT14(0x46),
	ATTACHMENT15(0x47),
	FRONT_BUMPER(0x48),
	FRONT_BUMPER_BADGING_SET(0x49),
	REAR_BUMPER(0x4A),
	REAR_BUMPER_BADGING_SET(0x4B),
	ROOF(0x4C),
	ROOF_SCOOP(0x4D),
	HOOD(0x4E),
	SKIRT(0x4F),
	HEADLIGHT(0x50),
	BRAKELIGHT(0x51),
	DOOR_LEFT(0x52),
	DOOR_RIGHT(0x53),
	BRAKE(0x54),
	WHEEL(0x55),
	LICENSE_PLATE(0x56),
	BODY_DOORLINE(0x57),
	DECAL_FRONT_WINDOW(0x58),
	DECAL_REAR_WINDOW(0x59),     
	PAINT(0x5A),
	PAINT_GROUP(0x5B),
	PAINT_SWATCH(0x5C),
	VINYL_PAINT(0x5D),
	RIM_PAINT(0x5E),
	WHEEL_PAINT_GROUP(0x5F),
	DECAL_TEXTURE(0x60),
	VINYL_SPECIFIC(0x61),
	VINYL_GENERIC(0x62),
	VECTORVINYL(0x63),
	WINDOW_TINT(0x64),
	CUSTOM_HUD(0x65),
	CV(0x66),
	MISC(0x67),
	INVALID(0xFF);

    private final byte value;
    private PartCarbon(int value) {this.value = (byte) value;}
	@Override
	public byte getValue() {return value;}

    public static PartCarbon get(int value) {
        for (var s : values()) if (s.value == value) return s;
        return INVALID;
    }
    public static PartCarbon get(String name) {
        for (var s : values()) if (s.name().equals(name)) return s;
        return INVALID;
    }
}