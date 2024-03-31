package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;

public class LineEquation {
	// ax + by + c = 0 in a plane
	
	public float a = 0;
	public float b = 0;
	public float c = 0;

	public ArrayList<PlaneEquation> intersectedPlanes = new ArrayList<PlaneEquation>();
	public ArrayList<Vertex> containedVertices = new ArrayList<Vertex>();

	public CollisionConvexVertice convexVerticeShape;
	
	public LineEquation(PlaneEquation p, PlaneEquation p2) {
		a = p.a*p2.c - p2.a*p.c;
		b = p.b*p2.c - p2.b*p.c;
		c = p.d*p2.c - p2.d*p.c;
		intersectedPlanes.add(p);
		intersectedPlanes.add(p2);
		p.containedLines.add(this);
		p2.containedLines.add(this);
		
		this.convexVerticeShape = p.convexVerticeShape;
		p.convexVerticeShape.LineEquations.add(this);
	}
	
	public Vertex intersect(LineEquation l) {
		float x = (l.c*this.b - l.b*this.c) / (this.a*l.b - l.a*this.b);
		float y = (-this.a*x - this.c)/this.b;
		float z = (-this.intersectedPlanes.get(0).a*x -this.intersectedPlanes.get(0).b*y -this.intersectedPlanes.get(0).d)/this.intersectedPlanes.get(0).c;

		if (x > convexVerticeShape.HalfExtentsX+0.1 || x < -convexVerticeShape.HalfExtentsX-0.1 ||
				y > convexVerticeShape.HalfExtentsY+0.1 || y < -convexVerticeShape.HalfExtentsY-0.1 ||
				z > convexVerticeShape.HalfExtentsZ+0.1 || z < -convexVerticeShape.HalfExtentsZ-0.1) {
			return null;
		}else {
			for (Vertex v : convexVerticeShape.Vertices) {
				if(Collisions.similarEnough(v.x, x) && Collisions.similarEnough(v.y, y) && Collisions.similarEnough(v.z, z)) {
					// the if statements below should never be entered
					if (!this.containedVertices.contains(v)) this.containedVertices.add(v);
					if (!l.containedVertices.contains(v)) l.containedVertices.add(v);
					if (!v.intersectedLines.contains(this)) v.intersectedLines.add(this);
					if (!v.intersectedLines.contains(l)) v.intersectedLines.add(l);
					return v;
				}
			}
			
			return new Vertex(this, l);
		}
	}

}
