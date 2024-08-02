package fr.ni240sx.ucgt.dbmpPlus;

import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.binstuff.Hash;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

public class AttributeCarPartID extends Attribute{
//	public static String AttributeIdentifier = "CarPartID";
	public PartUndercover ID = PartUndercover.INVALID;
	public byte level = 0;
	TextField IDgui;
	TextField levelgui;
	public AttributeCarPartID(String key) {
		super(key);
		IDgui = new TextField();
		levelgui = new TextField();
		initGUI();
	}
	public AttributeCarPartID(String key, PartUndercover ID, int level) {
		super(key);
		this.ID = ID;
		this.level = (byte)level;
		IDgui = new TextField();
		levelgui = new TextField();
		initGUI();
	}
	public AttributeCarPartID(Hash key, ByteBuffer bb, boolean useGUI) {
		super(key, useGUI);
		level = bb.get();
		ID = PartUndercover.get(bb.get());
		if (useGUI) {
			IDgui = new TextField();
			levelgui = new TextField();
			initGUI();
		}
	}
	public AttributeCarPartID(AttributeCarPartID copyFrom) {
		super(copyFrom);
		this.ID = copyFrom.ID;
		this.level = copyFrom.level;
		IDgui = new TextField();
		levelgui = new TextField();
		initGUI();
	}
	@SuppressWarnings("unused")
	public void initGUI() {
		IDgui.setText(ID.getText());
		levelgui.setPrefWidth(40);
		levelgui.setText(Integer.toString(level));
		dataHBox.getChildren().addAll(levelgui, IDgui);

		IDgui.setOnAction(e -> {
			int caretB4Save = IDgui.getCaretPosition();
			new UndoAttributeChange(this);
			if(PartUndercover.get(IDgui.getText().strip())!=null) {
				ID = PartUndercover.get(IDgui.getText().strip());
			} else new Alert(Alert.AlertType.ERROR, "Invalid slot", ButtonType.OK).show();
			IDgui.setText(ID.getText());
			IDgui.positionCaret(caretB4Save);
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
		IDgui.setText(ID.getText());
		levelgui.setText(Integer.toString(level));
	}
	@Override
	public void writeToFile(ByteBuffer bb) {
		super.writeToFile(bb);
		bb.put(level);
		bb.put(ID.getValue());
	}
	@Override
	public void revertFrom(Attribute a) {
		this.ID = ((AttributeCarPartID)a).ID;
		this.level = ((AttributeCarPartID)a).level;
		IDgui.setText(ID.getText());
		levelgui.setText(Integer.toString(level));
	}
	@Override
	public String getAttribType() {
		return "CarPartID";
	}
	@Override
	public String toString() {
		return "CarPartID" + Key + ": " + ID + "/" + ID.getValue() + ", level " + level;
	}
}