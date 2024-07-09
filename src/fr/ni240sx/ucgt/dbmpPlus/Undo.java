package fr.ni240sx.ucgt.dbmpPlus;

import java.util.ArrayList;

public abstract class Undo {

	public static ArrayList<Undo> pastEvents = new ArrayList<>();
	public static ArrayList<Undo> futureEvents = new ArrayList<>();
	public static final int maxUndoCount = 1000;
	public static final int maxRedoCount = 1000;

	public Undo() {
		pastEvents.add(this);
		if(pastEvents.size()>maxUndoCount) pastEvents.remove(0);
	}
	
	public void undoThis() {
		futureEvents.add(this);
		if(futureEvents.size()>maxRedoCount) futureEvents.remove(0);
	}

	public void redoThis() {
		pastEvents.add(this);
		if(pastEvents.size()>maxUndoCount) pastEvents.remove(0);
	}
	
	public static void undo() {
		if(!pastEvents.isEmpty()) {
			pastEvents.get(pastEvents.size()-1).undoThis();
			pastEvents.remove(pastEvents.size()-1);
		}
	}

	public static void redo() {
		if(!futureEvents.isEmpty()) {
			futureEvents.get(futureEvents.size()-1).redoThis();
			futureEvents.remove(futureEvents.size()-1);
		}
	}
}

class UndoPartDelete extends Undo{
	DBMPPart p;
	int index;
	public UndoPartDelete(DBMPPart p, int index) {
		super();
		this.p = p;
		this.index = index;
	}
	
	@Override
	public void undoThis() {
		super.undoThis();
		DBMPPlus.mainDBMP.dBMPParts.add(index, p);
		DBMPPlus.partsDisplay.getItems().add(index, p);
//		DBMPPlus.updateAllPartsDisplay();
	}
	
	@Override
	public void redoThis() {
		super.redoThis();
		index = DBMPPlus.mainDBMP.dBMPParts.indexOf(p);
		DBMPPlus.mainDBMP.dBMPParts.remove(p);
		DBMPPlus.updateAllPartsDisplay();
	}
}

class UndoAttributeChange extends Undo{
	Attribute backup;
	Attribute edited;

	Attribute reference;
	public UndoAttributeChange(Attribute a) {
		super();
		this.reference = a;
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
	
	@Override
	public void undoThis() {
		super.undoThis();
		if (reference.getAttribType().equals("String")) {
			this.edited = new AttributeString((AttributeString)reference);
		}
		if (reference.getAttribType().equals("TwoString")) {
			this.edited = new AttributeTwoString((AttributeTwoString)reference);
		}
		if (reference.getAttribType().equals("Integer")) {
			this.edited = new AttributeInteger((AttributeInteger)reference);
		}
		if (reference.getAttribType().equals("CarPartID")) {
			this.edited = new AttributeCarPartID((AttributeCarPartID)reference);
		}
		if (reference.getAttribType().equals("Key")) {
			this.edited = new AttributeKey((AttributeKey)reference);
		}
		this.reference.revertFrom(this.backup);
	}

	@Override
	public void redoThis() {
		super.redoThis();
		if (reference.getAttribType().equals("String")) {
			this.backup = new AttributeString((AttributeString)reference);
		}
		if (reference.getAttribType().equals("TwoString")) {
			this.backup = new AttributeTwoString((AttributeTwoString)reference);
		}
		if (reference.getAttribType().equals("Integer")) {
			this.backup = new AttributeInteger((AttributeInteger)reference);
		}
		if (reference.getAttribType().equals("CarPartID")) {
			this.backup = new AttributeCarPartID((AttributeCarPartID)reference);
		}
		if (reference.getAttribType().equals("Key")) {
			this.backup = new AttributeKey((AttributeKey)reference);
		}
		this.reference.revertFrom(this.edited);
	}
}