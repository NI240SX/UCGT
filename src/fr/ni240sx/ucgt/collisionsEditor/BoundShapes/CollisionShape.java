package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import fr.ni240sx.ucgt.collisionsEditor.CollisionBound;

public abstract class CollisionShape {

	public abstract CollisionShape deepCopy(CollisionBound b);
	//parent for other shapes
}
