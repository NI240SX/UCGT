package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;

public class LineEquation {
	// ax + by + c = 0 in a plane
	
	public float a = 0;
	public float b = 0;
	public float c = 0;

	public ArrayList<PlaneEquation> intersectedPlanes = new ArrayList<>();
	public ArrayList<Vertex> containedVertices = new ArrayList<>();

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
	
	/**
	 * Create a line passing by a vertex, on a plane
	 * @param v - the vertex
	 * @param p - the plane
	 * @param axis - use one axis or the other
	 */
	public LineEquation(Vertex v, PlaneEquation p, boolean axis) {
		// v : X, Y, Z  (e L & p)
		// p : ax + by + cz + d = 0
		// v e P -> aX + bY + cZ + d = 0
		// 
		// L : ?x + ?y + ? = 0
		// choose the best among these depending on max(a,b,c)
		// L : x + b/a*y + c/a*z + (d +aX +bY +cZ +d)/a = 0, with y=0 or z=0
		// ax + by + cz + d + aX + bY + cZ + d = 0
		// z = (-ax -by -d -aX -bY -cZ -d)/c
		//
		// ??????????????????????????????????????
		
		// L : y + a/b*x + c/b*z + (d +aX +bY +cZ +d)/b = 0, with x=0 or z=0
		// L : z + b/c*y + a/c*x + (d +aX +bY +cZ +d)/c = 0, with y=0 or x=0
		if (Math.abs(a) > Math.abs(b) && Math.abs(a) > Math.abs(c)) {
			if (axis) {
				a = 1;
				b = p.b/p.a;
				
			}else {
				//
			}
		} else if (Math.abs(b) > Math.abs(a) && Math.abs(b) > Math.abs(c)) {
			//
		} else { //c >= a & b
			
		}
		
	}
	
	public Vertex intersect(LineEquation l) {
		float x = (l.c*this.b - l.b*this.c) / (this.a*l.b - l.a*this.b);
		float y = (-this.a*x - this.c)/this.b;
		float z = (-this.intersectedPlanes.get(0).a*x -this.intersectedPlanes.get(0).b*y -this.intersectedPlanes.get(0).d)/this.intersectedPlanes.get(0).c;

		if (x > convexVerticeShape.HalfExtentsX+0.1+convexVerticeShape.CenterX || x < -convexVerticeShape.HalfExtentsX-0.1+convexVerticeShape.CenterX ||
				y > convexVerticeShape.HalfExtentsY+0.1+convexVerticeShape.CenterY || y < -convexVerticeShape.HalfExtentsY-0.1+convexVerticeShape.CenterY ||
				z > convexVerticeShape.HalfExtentsZ+0.1+convexVerticeShape.CenterZ || z < -convexVerticeShape.HalfExtentsZ-0.1+convexVerticeShape.CenterZ || 
				x != x || y != y || z != z) {
			return null;
		}
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
		return  new Vertex(this, l);
//			System.out.println(ret);
	}

}
