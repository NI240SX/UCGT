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
		DBMPPlus.updateAllPartsDisplay();
	}
}