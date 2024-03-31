package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;

public class PlaneEquation {
	// ax + by + cz + d = 0
	
	public float a = 0;
	public float b = 0;
	public float c = 0;
	public float d = 0;
	
	public CollisionConvexVertice convexVerticeShape;
	
	public ArrayList<LineEquation> containedLines = new ArrayList<LineEquation>();
	
	public PlaneEquation() {
		// TODO Auto-generated constructor stub
	}

	public PlaneEquation(ByteBuffer bb, CollisionConvexVertice load) {
		this.a = bb.getFloat();
		this.b = bb.getFloat();
		this.c = bb.getFloat();
		this.d = bb.getFloat();
		this.convexVerticeShape = load;
	}
	
	public LineEquation intersect(PlaneEquation p) {
		float a = this.a*p.c - p.a*this.c;
		float b = this.b*p.c - p.b*this.c;
		float c = this.d*p.c - p.d*this.c;
		
		for (LineEquation li : convexVerticeShape.LineEquations) {
			if(Collisions.similarEnough(li.a, a) && Collisions.similarEnough(li.b, b) && Collisions.similarEnough(li.c, c)) {
				// the if statements below should never be entered
				if (!this.containedLines.contains(li)) this.containedLines.add(li);
				if (!p.containedLines.contains(li)) p.containedLines.add(li);
				if (!li.intersectedPlanes.contains(this)) li.intersectedPlanes.add(this);
				if (!li.intersectedPlanes.contains(p)) li.intersectedPlanes.add(p);
				return li;
			}
		}
		
		return new LineEquation(this, p);
	}

	@Override
	public String toString() {
		return "\n    -PlaneEquation : " + a + "x + " + b + "y + " + c + " + " + d + " = 0";
	}

}
