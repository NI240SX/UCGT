package dbmpPlus;

import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

class Part {//extends HBox {
	public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	public String displayName = "MISSING ATTRIBUTES";
	
	public int kitnumber = 99;
	public boolean isWidebody = false;
	
//	CheckBox uiCheckBox;
//	Label uiLabel;
	
	CarPartListCell listCell = new CarPartListCell();
	
/*	public Part() {
		uiCheckBox = new CheckBox();
		uiLabel = new Label(displayName);

        this.getChildren().addAll(uiCheckBox, uiLabel);
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent arg0) {
				DBMPPlus.attributesDisplay.getChildren().clear();
				for (Attribute a : attributes) {
					DBMPPlus.attributesDisplay.getChildren().addAll(a);
				}
				if (!arg0.isControlDown()) {
					for (Part p : DBMPPlus.mainDBMP.parts) {
						p.uiCheckBox.setSelected(false);
					}
				}
				if (!uiCheckBox.isSelected()) uiCheckBox.setSelected(true); else uiCheckBox.setSelected(false);
				arg0.consume();
			}
        });
        
	}*/
	
	public Part() {
		
	}
	
	public Part(Part copyFrom) {
		for(Attribute a : copyFrom.attributes) {
			if (a.getClass() == AttributeInteger.class) {
				this.attributes.add(new AttributeInteger((AttributeInteger)a));
			} else if (a.getClass() == AttributeString.class) {
				this.attributes.add(new AttributeString((AttributeString)a));
			} else if (a.getClass() == AttributeTwoString.class) {
				this.attributes.add(new AttributeTwoString((AttributeTwoString)a));
			} else if (a.getClass() == AttributeKey.class) {
				this.attributes.add(new AttributeKey((AttributeKey)a));
			} else if (a.getClass() == AttributeCarPartID.class) {
				this.attributes.add(new AttributeCarPartID((AttributeCarPartID)a));
			}
		}
		this.displayName = copyFrom.displayName;
		this.isWidebody = copyFrom.isWidebody;
		this.kitnumber = copyFrom.kitnumber;
	}
	
	public void update() {
		String name1 = "MISSING ATTRIBUTES";
		String name2 = "MISSING ATTRIBUTES";
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals("PART_NAME_OFFSETS")) {
				AttributeTwoString att2S = (AttributeTwoString)attribute;
				name1 = att2S.value1 + "_" + att2S.value2;
				kitnumber = Integer.valueOf(att2S.value1.substring(att2S.value1.length()-2));
				if (att2S.value1.charAt(3) == 'W') isWidebody = true; else isWidebody = false;
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
//		uiLabel.setText(displayName);
		listCell.update();
	}
	
	public void addAttribute(Attribute a) {
		attributes.add(a);
		update();
	}
	public Attribute getAttribute(String a) {
		Attribute att = null;
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				att = attribute;
			}
		}
		return att;
	}
	public AttributeInteger getAttributeInteger(String a) {
		AttributeInteger att = null;
		for (Attribute attribute : attributes) {
			if (attribute.Key.label.equals(a)) {
				att = (AttributeInteger)attribute;
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