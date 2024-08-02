package fr.ni240sx.ucgt.dbmpPlus;

import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.binstuff.Hash;
import javafx.scene.control.TextField;

public class AttributeString extends Attribute{
//	public static final String AttributeIdentifier = "String";
	public String value1 = "";
	public byte value1Exists = 0;
	TextField value1gui;
	public AttributeString(String key) {
		super(key);
		value1gui = new TextField();
		dataHBox.getChildren().addAll(value1gui);
	}
	public AttributeString(String key, String value1) {
		super(key);
		value1gui = new TextField();
		this.value1 = value1.strip();
		if(!value1.isBlank()) value1Exists = 1;
		initGUI();
	}
	public AttributeString(Hash key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		value1Exists = bb.get();
		if (value1Exists == 1) value1 = DBMP.readString(bb);
		if (useGUI) {
			value1gui = new TextField();
			initGUI();
		}
	}
	public AttributeString(AttributeString copyFrom) {
		super(copyFrom);
		this.value1Exists = copyFrom.value1Exists;
		this.value1 = copyFrom.value1;
		value1gui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		value1gui.setText(value1);
		dataHBox.getChildren().addAll(value1gui);
		value1gui.setOnKeyTyped(e -> {
			int caretB4Save = value1gui.getCaretPosition();
			new UndoAttributeChange(this);
			value1 = value1gui.getText().strip();
			value1gui.setText(value1gui.getText().strip());
			if (value1.isEmpty()) value1Exists = 0; else value1Exists = 1;
			value1gui.positionCaret(caretB4Save);
			e.consume();
		});
	}
	@Override
	public void update() {
		value1gui.setText(value1);
	}
	@Override
	public void revertFrom(Attribute a) {
		this.value1 = ((AttributeString)a).value1;
		this.value1Exists = ((AttributeString)a).value1Exists;
		value1gui.setText(value1);
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(value1Exists);
		if (value1Exists == 1) DBMP.writeString(value1, bb);
	}
	@Override
	public String getAttribType() {
		return "String";
	}
	@Override
	public String toString() {
		return "String" + Key + ": " + value1 + "," + (value1Exists==1);
	}
}