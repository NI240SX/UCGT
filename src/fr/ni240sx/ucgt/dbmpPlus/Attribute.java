package fr.ni240sx.ucgt.dbmpPlus;

import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.shared.Hash;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

abstract class Attribute {
	
//	public String AttributeIdentifier;
	public int Key = 0;
	public String displayName = "MISSING";
	public HBox dataHBox = new HBox(10);

	public Attribute(String key) {
		Key = Hash.findBIN(key);
		displayName = Hash.getBIN(Key);
		dataHBox.getChildren().addAll(new Label(displayName));
		setupDeletion();
	}
	
	public Attribute(int key, boolean useGUI) {
		Key = key;
		displayName = Hash.getBIN(key);
		if (useGUI) dataHBox.getChildren().addAll(new Label(displayName));
		setupDeletion();
	}

	public Attribute(Attribute copyFrom) {
		Key = copyFrom.Key;
		displayName = copyFrom.displayName;
		dataHBox.getChildren().addAll(new Label(displayName));
		setupDeletion();
	}

	private void setupDeletion() {
		dataHBox.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.SECONDARY) {
		        ButtonType sure = null;
				if (!DBMPPlus.disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Delete attribute ?", ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure) || DBMPPlus.disableWarnings) {
					DBMPPlus.partsDisplay.getSelectionModel().getSelectedItem().attributes.remove(this);
					DBMPPlus.updateAttributesDisplay();
				}
				e.consume();
			}
		});
	}
	
	public void writeToFile(ByteBuffer bb) {
		bb.putInt(Key);
	}
	
	public abstract void revertFrom(Attribute a);
	
	public void update() {
		
	}
	
	public abstract String getAttribType();
	
	@Override
	public String toString() {
		return "Generic " + super.toString();
	}
	
	
}

class AttributeTwoString extends Attribute{
//	public static final String AttributeIdentifier = "TwoString";
	public String value1 = "";
	public byte value1Exists = 0;
	public String value2 = "";
	public byte value2Exists = 0;
	TextField value1gui;
	TextField value2gui;
	public AttributeTwoString(String key) {
		super(key);
		value1gui = new TextField();
		value2gui = new TextField();
		initGUI();
	}
	public AttributeTwoString(String key, String string1, String string2) {
		super(key);
		value1 = string1.strip();
		value2 = string2.strip();
		if(!value1.isBlank()) value1Exists = 1;
		if(!value2.isBlank()) value2Exists = 1;
		value1gui = new TextField();
		value2gui = new TextField();
		initGUI();
	}
	public AttributeTwoString(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		value1Exists = bb.get();
		value2Exists = bb.get();
		if (value1Exists == 1) value1 = DBMP.readString(bb);
		if (value2Exists == 1) value2 = DBMP.readString(bb);
		if (useGUI) {
			value1gui = new TextField();
			value2gui = new TextField();
			initGUI();
		}
	}
	public AttributeTwoString(AttributeTwoString copyFrom) {
		super(copyFrom);
		this.value1Exists = copyFrom.value1Exists;
		this.value1 = copyFrom.value1;
		this.value2Exists = copyFrom.value2Exists;
		this.value2 = copyFrom.value2;
		value1gui = new TextField();
		value2gui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		value1gui.setText(value1);
		value2gui.setText(value2);
		value1gui.setPrefWidth(60);
		dataHBox.getChildren().addAll(value1gui,value2gui);

		value1gui.setOnKeyTyped(e -> {
			int caretB4Save = value1gui.getCaretPosition();
			new UndoAttributeChange(this);
			value1 = value1gui.getText().strip();
			value1gui.setText(value1gui.getText().strip());
			if (value1.isEmpty()) value1Exists = 0; else value1Exists = 1;
			DBMPPlus.updateAllPartsDisplay();
			value1gui.positionCaret(caretB4Save);
			e.consume();
		});
		value2gui.setOnKeyTyped(e -> {
			int caretB4Save = value2gui.getCaretPosition();
			new UndoAttributeChange(this);
			value2 = value2gui.getText().strip();
			value2gui.setText(value2gui.getText().strip());
			if (value2.isEmpty()) value2Exists = 0; else value2Exists = 1;
			e.consume();
			DBMPPlus.updateAllPartsDisplay();
			value2gui.positionCaret(caretB4Save);
		});
	}
	@Override
	public void update() {
		value1gui.setText(value1);
		value2gui.setText(value2);
	}
	@Override
	public void revertFrom(Attribute a) {
		this.value1 = ((AttributeTwoString)a).value1;
		this.value1Exists = ((AttributeTwoString)a).value1Exists;
		this.value2 = ((AttributeTwoString)a).value2;
		this.value2Exists = ((AttributeTwoString)a).value2Exists;
		value1gui.setText(value1);
		value2gui.setText(value2);
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(value1Exists);
		bb.put(value2Exists);
		if (value1Exists == 1) DBMP.writeString(value1, bb);
		if (value2Exists == 1) DBMP.writeString(value2, bb);
	}
	@Override
	public String getAttribType() {
		return "TwoString";
	}
	@Override
	public String toString() {
		return "TwoString" + Key + ": " + value1 + "," + (value1Exists==1) + "," + value2 + "," + (value2Exists==1);
	}
}

class AttributeInteger extends Attribute{
//	public static String AttributeIdentifier = "Integer";
	public int value = 0;
	TextField valuegui;
	public AttributeInteger(String key) {
		super(key);
		valuegui = new TextField();
		initGUI();
	}
	public AttributeInteger(String key, int value) {
		super(key);
		this.value = value;
		valuegui = new TextField();
		initGUI();
	}
	public AttributeInteger(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		value = bb.getInt();
		if (useGUI) {
			valuegui = new TextField();
			initGUI();
		}
	}
	public AttributeInteger(AttributeInteger copyFrom) {
		super(copyFrom);
		this.value = copyFrom.value;
		valuegui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		valuegui.setText(Integer.toString(value));
		dataHBox.getChildren().addAll(valuegui);
		valuegui.setOnKeyTyped(e -> {
			int caretB4Save = valuegui.getCaretPosition();
			if (valuegui.getText().isBlank()) valuegui.setText("0");
			new UndoAttributeChange(this);
			try {
				value = Integer.parseInt(valuegui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid integer", ButtonType.OK).show();
			}
			valuegui.setText(Integer.toString(value));
			valuegui.positionCaret(caretB4Save);
			e.consume();
		});
	}
	@Override
	public void update() {
		valuegui.setText(Integer.toString(value));
	}
	@Override
	public void revertFrom(Attribute a) {
		this.value = ((AttributeInteger)a).value;
		valuegui.setText(Integer.toString(value));
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.putInt(value);
	}
	@Override
	public String getAttribType() {
		return "Integer";
	}
	@Override
	public String toString() {
		return "Integer" + Key + ": " + value;
	}
}




class AttributeFloating extends Attribute{
//	public static String AttributeIdentifier = "Integer";
	public float value = 0;
	TextField valuegui;
	public AttributeFloating(String key) {
		super(key);
		valuegui = new TextField();
		initGUI();
	}
	public AttributeFloating(String key, float value) {
		super(key);
		this.value = value;
		valuegui = new TextField();
		initGUI();
	}
	public AttributeFloating(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		value = bb.getFloat();
		if (useGUI) {
			valuegui = new TextField();
			initGUI();
		}
	}
	public AttributeFloating(AttributeFloating copyFrom) {
		super(copyFrom);
		this.value = copyFrom.value;
		valuegui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		valuegui.setText(Float.toString(value));
		dataHBox.getChildren().addAll(valuegui);
		valuegui.setOnKeyTyped(e -> {
			int caretB4Save = valuegui.getCaretPosition();
			if (valuegui.getText().isBlank()) valuegui.setText("0");
			new UndoAttributeChange(this);
			try {
				value = Float.parseFloat(valuegui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid float", ButtonType.OK).show();
			}
			valuegui.setText(Float.toString(value));
			valuegui.positionCaret(caretB4Save);
			e.consume();
		});
	}
	@Override
	public void update() {
		valuegui.setText(Float.toString(value));
	}
	@Override
	public void revertFrom(Attribute a) {
		this.value = ((AttributeFloating)a).value;
		valuegui.setText(Float.toString(value));
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.putFloat(value);
	}
	@Override
	public String getAttribType() {
		return "Floating";
	}
	@Override
	public String toString() {
		return "Floating" + Key + ": " + value;
	}
}





class AttributeKey extends Attribute{
//	public static String AttributeIdentifier = "Key";
	public String value = "";
	TextField valuegui;
	public AttributeKey(String key) {
		super(key);
		valuegui = new TextField();
		initGUI();
	}
	public AttributeKey(String key, String value) {
		super(key);
		this.value = value;
		valuegui = new TextField();
		initGUI();
	}
	public AttributeKey(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		value = DBMP.readString(bb);
		if (useGUI) {
			valuegui = new TextField();
			initGUI();
		}
	}
	public AttributeKey(AttributeKey copyFrom) {
		super(copyFrom);
		this.value = copyFrom.value;
		valuegui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		valuegui.setText(this.value);
		dataHBox.getChildren().addAll(valuegui);

		valuegui.setOnKeyTyped(e -> {
			int caretB4Save = valuegui.getCaretPosition();
			new UndoAttributeChange(this);
			value = valuegui.getText().strip();
			valuegui.setText(value);
			valuegui.positionCaret(caretB4Save);
			e.consume();
		});
	}
	@Override
	public void update() {
		valuegui.setText(this.value);
	}
	@Override
	public void revertFrom(Attribute a) {
		this.value = ((AttributeKey)a).value;
		valuegui.setText(value);
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		DBMP.writeString(value, bb);
	}
	@Override
	public String getAttribType() {
		return "Key";
	}
	@Override
	public String toString() {
		return "Key" + Key + ": " + value;
	}
}






class AttributeBoolean extends Attribute{
//	public static String AttributeIdentifier = "Integer";
	public boolean value = false;
	ComboBox<Boolean> valuegui;
	public AttributeBoolean(String key) {
		super(key);
		valuegui = new ComboBox<>();
		initGUI();
	}
	public AttributeBoolean(String key, boolean value) {
		super(key);
		this.value = value;
		valuegui = new ComboBox<>();
		initGUI();
	}
	public AttributeBoolean(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		value = bb.get()==1;
		if (useGUI) {
			valuegui = new ComboBox<>();
			initGUI();
		}
	}
	public AttributeBoolean(AttributeBoolean copyFrom) {
		super(copyFrom);
		this.value = copyFrom.value;
		valuegui = new ComboBox<>();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		valuegui.getItems().add(false);
		valuegui.getItems().add(true);
		valuegui.getSelectionModel().select(this.value);
		dataHBox.getChildren().addAll(valuegui);
		valuegui.setOnAction(e -> {
			new UndoAttributeChange(this);
			value = valuegui.getValue();
			e.consume();
		});
	}
	@Override
	public void update() {
		valuegui.getSelectionModel().select(this.value);
	}
	@Override
	public void revertFrom(Attribute a) {
		this.value = ((AttributeBoolean)a).value;
		valuegui.getSelectionModel().select(this.value);
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		if (value) bb.put((byte)1); else bb.put((byte)0);
	}
	@Override
	public String getAttribType() {
		return "Boolean";
	}
	@Override
	public String toString() {
		return "Boolean" + Key + ": " + value;
	}
}





class AttributeColor extends Attribute{
//	public static final String AttributeIdentifier = "TwoString";
	public byte red=0;
	public byte green=0;
	public byte blue=0;
	public byte alpha=0;
	TextField redgui;
	TextField greengui;
	TextField bluegui;
	TextField alphagui;
	public AttributeColor(String key) {
		super(key);
		redgui = new TextField();
		greengui = new TextField();
		bluegui = new TextField();
		alphagui = new TextField();
		initGUI();
	}
	public AttributeColor(String key, byte red, byte blue, byte green, byte alpha) {
		super(key);
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		redgui = new TextField();
		greengui = new TextField();
		bluegui = new TextField();
		alphagui = new TextField();
		initGUI();
	}
	public AttributeColor(int key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		red = bb.get();
		green = bb.get();
		blue = bb.get();
		alpha = bb.get();
		if (useGUI) {
			redgui = new TextField();
			greengui = new TextField();
			bluegui = new TextField();
			alphagui = new TextField();
			initGUI();
		}
	}
	public AttributeColor(AttributeColor copyFrom) {
		super(copyFrom);
		this.red = copyFrom.red;
		this.green = copyFrom.green;
		this.blue = copyFrom.blue;
		this.alpha = copyFrom.alpha;
		redgui = new TextField();
		greengui = new TextField();
		bluegui = new TextField();
		alphagui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		redgui.setText(Integer.toString(red & 0xFF));
		greengui.setText(Integer.toString(green & 0xFF));
		bluegui.setText(Integer.toString(blue & 0xFF));
		alphagui.setText(Integer.toString(alpha & 0xFF));
		redgui.setPrefWidth(60);
		greengui.setPrefWidth(60);
		bluegui.setPrefWidth(60);
		alphagui.setPrefWidth(60);
		dataHBox.getChildren().addAll(redgui, greengui, bluegui, alphagui);

		redgui.setOnKeyTyped(e -> {
			int caretB4Save = redgui.getCaretPosition();
			if (redgui.getText().isBlank()) redgui.setText("0");
			new UndoAttributeChange(this);
			try {
				red = (byte) Integer.parseInt(redgui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid byte", ButtonType.OK).show();
			}
			redgui.setText(Integer.toString(red & 0xFF));
			DBMPPlus.updateAllPartsDisplay();
			redgui.positionCaret(caretB4Save);
			e.consume();
		});
		greengui.setOnKeyTyped(e -> {
			int caretB4Save = greengui.getCaretPosition();
			if (greengui.getText().isBlank()) greengui.setText("0");
			new UndoAttributeChange(this);
			try {
				green = (byte) Integer.parseInt(greengui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid byte", ButtonType.OK).show();
			}
			greengui.setText(Integer.toString(green & 0xFF));
			DBMPPlus.updateAllPartsDisplay();
			greengui.positionCaret(caretB4Save);
			e.consume();
		});
		bluegui.setOnKeyTyped(e -> {
			int caretB4Save = bluegui.getCaretPosition();
			if (bluegui.getText().isBlank()) bluegui.setText("0");
			new UndoAttributeChange(this);
			try {
				blue = (byte) Integer.parseInt(bluegui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid byte", ButtonType.OK).show();
			}
			bluegui.setText(Integer.toString(blue & 0xFF));
			DBMPPlus.updateAllPartsDisplay();
			bluegui.positionCaret(caretB4Save);
			e.consume();
		});
		alphagui.setOnKeyTyped(e -> {
			int caretB4Save = alphagui.getCaretPosition();
			if (alphagui.getText().isBlank()) alphagui.setText("0");
			new UndoAttributeChange(this);
			try {
				alpha = (byte) Integer.parseInt(alphagui.getText().strip());
			}catch(NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter a valid byte", ButtonType.OK).show();
			}
			alphagui.setText(Integer.toString(alpha & 0xFF));
			DBMPPlus.updateAllPartsDisplay();
			alphagui.positionCaret(caretB4Save);
			e.consume();
		});
		}
	@Override
	public void update() {
		redgui.setText(Integer.toString(red & 0xFF));
		greengui.setText(Integer.toString(green & 0xFF));
		bluegui.setText(Integer.toString(blue & 0xFF));
		alphagui.setText(Integer.toString(alpha & 0xFF));
	}
	@Override
	public void revertFrom(Attribute a) {
		this.red = ((AttributeColor)a).red;
		this.green = ((AttributeColor)a).green;
		this.blue = ((AttributeColor)a).blue;
		this.alpha = ((AttributeColor)a).alpha;
		redgui.setText(Integer.toString(red & 0xFF));
		greengui.setText(Integer.toString(green & 0xFF));
		bluegui.setText(Integer.toString(blue & 0xFF));
		alphagui.setText(Integer.toString(alpha & 0xFF));
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(red);
		bb.put(green);
		bb.put(blue);
		bb.put(alpha);
	}
	@Override
	public String getAttribType() {
		return "Color";
	}
	@Override
	public String toString() {
		return "Color" + Key + ": R=" + (red & 0xFF) + ", G=" + (green & 0xFF) + ", B=" + (blue & 0xFF) + ", A=" + (alpha & 0xFF);
	}
}






class StringData{
	public String value;
	public boolean exists;
	public CheckBox existsgui;
	public TextField valuegui;
	public HBox hbox;
	public StringData() {
		exists = false;
		initGUI();
	}
	public StringData(ByteBuffer in) {
		exists = in.get() != 0;
		if (exists) value = DBMP.readString(in);
	}
	public StringData(StringData other) {
		this.exists = other.exists;
		if (other.value!=null) this.value = new String(other.value);
		initGUI();
	}
	public void initGUI() {
		hbox = new HBox();
		existsgui = new CheckBox();
		valuegui = new TextField(value);
		valuegui.setMinWidth(200);
		existsgui.setOnAction(e -> exists = existsgui.isSelected());
		valuegui.setOnKeyTyped(e -> value = valuegui.getText());
		hbox.getChildren().addAll(existsgui, valuegui);
		update();
	}
	public void update() {
		existsgui.setSelected(exists);
		valuegui.setText(value);
	}
	public void save(ByteBuffer bb) {
		bb.put((byte) (exists ? 1 : 0));
		if (exists) DBMP.writeString(value, bb);
	}
}


class AttributeModelTable extends Attribute{
//	public static final String AttributeIdentifier = "TwoString";
	public boolean templated=false;
	public StringData concatenator;
	public StringData[][] models;

	public CheckBox templatedgui;
	
	public AttributeModelTable(String key) {
		super(key);
		templated = false;
		concatenator = new StringData();
		models = new StringData[5][12];
		for (int lod=0; lod<5; lod++) for (var model=0; model<12; model++){
			models[lod][model] = new StringData();
		}
		templatedgui = new CheckBox("Templated");
		initGUI();
	}
	public AttributeModelTable(int key, ByteBuffer in, boolean useGUI) {
		super(key, useGUI);
		templated = in.get() != 0;
		concatenator = new StringData(in);
		models = new StringData[5][12];
		for (int lod=0; lod<5; lod++) for (var model=0; model<12; model++){
			models[lod][model] = new StringData(in);
		}
		if (useGUI) {
			templatedgui = new CheckBox("Templated");
			initGUI();
		}
	}
	public AttributeModelTable(AttributeModelTable copyFrom) {
		super(copyFrom);
		this.templated = copyFrom.templated;
		this.concatenator = new StringData(copyFrom.concatenator);
		for (int lod=0; lod<5; lod++) for (var model=0; model<12; model++){
			models[lod][model] = new StringData(copyFrom.models[lod][model]);
		}
		initGUI();
	}
	public void initGUI() {
		var vbox = new VBox();
		templatedgui.setSelected(templated);
		templatedgui.setOnAction(e -> templated = templatedgui.isSelected());
		concatenator.initGUI();
		concatenator.hbox.getChildren().add(0, new Label("Concatenator"));
		vbox.getChildren().addAll(templatedgui, concatenator.hbox);
		for (var model=0; model<12; model++) for (var lod=0; lod<5; lod++){
			models[lod][model].initGUI();
			models[lod][model].hbox.getChildren().add(0, new Label("Solid"+model+" LOD"+(char)('A'+(char)lod)));
			vbox.getChildren().add(models[lod][model].hbox);
		}
		dataHBox.getChildren().add(vbox);
	}
	@Override
	public void update() {
		templatedgui.setSelected(templated);
		concatenator.update();
		for (var ms : models) for (var m : ms) m.update();
	}
	@Override
	public void revertFrom(Attribute a) {
		templated = ((AttributeModelTable)a).templated;
		concatenator = ((AttributeModelTable)a).concatenator;
		models = ((AttributeModelTable)a).models;		
		update();
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put((byte) (templated ? 1 : 0));
		concatenator.save(bb);
		for (int lod=0; lod<5; lod++) for (var model=0; model<12; model++){
			models[lod][model].save(bb);
		}
	}
	@Override
	public String getAttribType() {
		return "ModelTable";
	}
	@Override
	public String toString() {
		return "ModelTable";
	}
}



//class AttributeTextFieldInt extends TextField{
//
//	public AttributeTextFieldInt(Integer linkedint, Attribute linkedattrib) {
//		super();
//		setOnKeyTyped(e -> {
//			int caretB4Save = getCaretPosition();
//			if (getText().isBlank()) setText("0");
//			new UndoAttributeChange(linkedattrib);
//			try {
//				linkedint = Integer.parseInt(getText().strip());
//			}catch(NumberFormatException ex) {
//				new Alert(Alert.AlertType.ERROR, "Please enter a valid byte", ButtonType.OK).show();
//			}
//			setText(Integer.toString(linkedint & 0xFF));
//			DBMPPlus.updateAllPartsDisplay();
//			positionCaret(caretB4Save);
//			e.consume();
//		});
//	}
//
//	public AttributeTextFieldInt(String arg0) {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}
//	
//}