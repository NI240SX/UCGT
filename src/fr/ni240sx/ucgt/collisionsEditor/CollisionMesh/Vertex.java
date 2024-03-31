package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.util.ArrayList;

public class Vertex {

	public float x = 0;
	public float y = 0;
	public float z = 0;

	public ArrayList<LineEquation> intersectedLines = new ArrayList<LineEquation>();

	public CollisionConvexVertice convexVerticeShape;
	
	/**
	 * @param l - first line equation
	 * @param l2 - second line equation
	 * @param save - save to all lists (true) or use temporarily (false)
	 */
	public Vertex(LineEquation l, LineEquation l2, boolean save) {
		x = (l2.c*l.b - l2.b*l.c) / (l.a*l2.b - l2.a*l.b);
		y = (-l.a*x - l.c)/l.b;
		z = (-l.intersectedPlanes.get(0).a*x -l.intersectedPlanes.get(0).b*y -l.intersectedPlanes.get(0).d)/l.intersectedPlanes.get(0).c;
		if (save) {
			intersectedLines.add(l);
			intersectedLines.add(l2);
			l.containedVertices.add(this);
			l2.containedVertices.add(this);		
			this.convexVerticeShape = l.convexVerticeShape;
			l.convexVerticeShape.Vertices.add(this);
		}
	}
	
	public Vertex(LineEquation l, LineEquation l2) { //move this elsewhere to handle duplicates
		this(l, l2, true);
	}

	public Vertex(PlaneEquation p, String axis) {
		
		// ax + by + cz + d = 0
		// axe x : y et z fixés (ici égaux au centre de gravité)
		
		switch (axis) {
		case "x":
			y = p.convexVerticeShape.CenterY;
			z = p.convexVerticeShape.CenterZ;
			x = (-p.b*y - p.c*z -p.d)/p.a;
			break;
		case "y":
			x = p.convexVerticeShape.CenterX;
			z = p.convexVerticeShape.CenterZ;
			y = (-p.a*x - p.c*z -p.d)/p.b;
			break;
		case "z":
			x = p.convexVerticeShape.CenterX;
			y = p.convexVerticeShape.CenterY;
			z = (-p.a*x - p.b*y -p.d)/p.c;
			break;
		}
//		this.convexVerticeShape = p.convexVerticeShape;
	}
}
