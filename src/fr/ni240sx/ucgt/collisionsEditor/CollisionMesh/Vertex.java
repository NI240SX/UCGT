package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.util.List;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;

public class Vertex {

	public float x = 0;
	public float y = 0;
	public float z = 0;

//	public ArrayList<LineEquation> intersectedLines = new ArrayList<>();

	public CollisionConvexVertice convexVerticeShape;
	
	/**
//	 * @param l - first line equation
//	 * @param l2 - second line equation
//	 * @param save - save to all lists (true) or use temporarily (false)
//	 */
//	public Vertex(LineEquation l, LineEquation l2, boolean save) {
//		x = (l2.c*l.b - l2.b*l.c) / (l.a*l2.b - l2.a*l.b);
//		y = (-l.a*x - l.c)/l.b;
//		z = (-l.intersectedPlanes.get(0).a*x -l.intersectedPlanes.get(0).b*y -l.intersectedPlanes.get(0).d)/l.intersectedPlanes.get(0).c;
//		if (x != x || y != y || z != z) {
////			System.out.println("prevented a NaN vertex");
//			save = false;
//		}
//		if (save) {
//			intersectedLines.add(l);
//			intersectedLines.add(l2);
//			l.containedVertices.add(this);
//			l2.containedVertices.add(this);		
//			this.convexVerticeShape = l.convexVerticeShape;
//			l.convexVerticeShape.Vertices.add(this);
//		}
//	}
//	
//	public Vertex(LineEquation l, LineEquation l2) { //move this elsewhere to handle duplicates
//		this(l, l2, true);
//	}

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

	public Vertex(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double distanceTo(Vertex v) {
		return Math.sqrt((x+v.x)*(x+v.x) + (y+v.y)*(y+v.y) + (z+v.z)*(z+v.z));
		
	}
	
	public Vertex findOnPlaneBetweenVertices(PlaneEquation plane, Vertex B) { // this = vertex, B = barycenter
	    float dx = B.x - x, dy = B.y - y, dz = B.z - z;
	    float denom = plane.a * dx + plane.b * dy + plane.c * dz;

	    if (Math.abs(denom) < 1e-6) return null; // line parallel to plane which might happen

	    float t = -(plane.a * x + plane.b * y + plane.c * z + plane.d) / denom;
	    
	    if (t < 0 || t > 1) return null;
	    
	    return new Vertex(x + t * dx, y + t * dy, z + t * dz);
	}
	
	static Vertex getCenter(List<Vertex> verts) {
	    float sx = 0, sy = 0, sz = 0;
	    int n = verts.size();
	    for (Vertex v : verts) {
	        sx += v.x;
	        sy += v.y;
	        sz += v.z;
	    }
	    return new Vertex(sx / n, sy / n, sz / n);
	}
	
	@Override
	public String toString() {
		return "Vertex x=" + x + ", y=" + y + ", z=" + z 
//				+ ", " + intersectedLines.size() + " intersected lines"
				;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		return Collisions.similarEnough(this.x, other.x) && Collisions.similarEnough(this.y, other.y) && Collisions.similarEnough(this.z, other.z);
	}
}
