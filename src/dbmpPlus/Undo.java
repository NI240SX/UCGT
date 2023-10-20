package dbmpPlus;

import java.util.ArrayList;

public abstract class Undo {

	public static ArrayList<Undo> pastEvents = new ArrayList<Undo>();
	public static final int maxUndoCount = 1000;

	public Undo() {
		pastEvents.add(this);
		if(pastEvents.size()>maxUndoCount) pastEvents.remove(0);
	}
	
	public void undoThis() {
	}
	
	public static void undo() {
		if(!pastEvents.isEmpty()) {
			pastEvents.get(pastEvents.size()-1).undoThis();
			pastEvents.remove(pastEvents.size()-1);
		}
	}
}

class UndoPartDelete extends Undo{
	Part p;
	int index;
	public UndoPartDelete(Part p, int index) {
		super();
		this.p = p;
		this.index = index;
	}
	
	public void undoThis() {
		DBMPPlus.mainDBMP.parts.add(index, p);
		DBMPPlus.partsDisplay.getItems().add(index, p);
//		DBMPPlus.updateAllPartsDisplay();
	}
}

class UndoAttributeChange extends Undo{
	Attribute backup;
	Attribute altered;
	public UndoAttributeChange(Attribute a) {
		super();
		this.altered = a;
		if (a.getAttribType().equals("String")) {
			this.backup = new AttributeString((AttributeString)a);
		}
		if (a.getAttribType().equals("TwoString")) {
			this.backup = new AttributeTwoString((AttributeTwoString)a);
		}
		if (a.getAttribType().equals("Integer")) {
			this.backup = new AttributeInteger((AttributeInteger)a);
		}
		if (a.getAttribType().equals("CarPartID")) {
			this.backup = new AttributeCarPartID((AttributeCarPartID)a);
		}
		if (a.getAttribType().equals("Key")) {
			this.backup = new AttributeKey((AttributeKey)a);
		}
	}
	
	public void undoThis() {
		this.altered.revertFrom(this.backup);
	}
}