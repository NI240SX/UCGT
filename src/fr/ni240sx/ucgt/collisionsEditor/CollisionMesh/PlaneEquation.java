package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;

public class PlaneEquation {
	// ax + by + cz + d = 0
	
	public float a = 0.0f;
	public float b = 1.0f;
	public float c = 0.0f;
	public float d = 1.0f;
	
	public CollisionConvexVertice convexVerticeShape;

//	public ArrayList<LineEquation> containedLines = new ArrayList<>();
	public ArrayList<Vertex> containedVertices = new ArrayList<>();
	
	public double colorR = 0;
	public double colorG = 0;
	public double colorB = 0;
	
	public PlaneEquation() {
	}
	
	public PlaneEquation(CollisionConvexVertice v, float a, float b, float c, float d) {
		convexVerticeShape = v;
		colorR = Collisions.randomizeColor(v.bound.colorR, 0.4);
		colorG = Collisions.randomizeColor(v.bound.colorG, 0.4);
		colorB = Collisions.randomizeColor(v.bound.colorB, 0.4);
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public PlaneEquation(ByteBuffer bb, CollisionConvexVertice load) {
		this.a = bb.getFloat();
		this.b = bb.getFloat();
		this.c = bb.getFloat();
		this.d = bb.getFloat();
		this.convexVerticeShape = load;
	}
	
	@SuppressWarnings("hiding")
//	public LineEquation intersect(PlaneEquation p) {
//		float a = this.a*p.c - p.a*this.c;
//		float b = this.b*p.c - p.b*this.c;
//		float c = this.d*p.c - p.d*this.c;
//		
//		for (LineEquation li : convexVerticeShape.LineEquations) {
//			if(Collisions.boundCenterCloseEnough(li.a, a) && Collisions.boundCenterCloseEnough(li.b, b) && Collisions.boundCenterCloseEnough(li.c, c)) {
//				// the if statements below should never be entered
//				if (!this.containedLines.contains(li)) this.containedLines.add(li);
//				if (!p.containedLines.contains(li)) p.containedLines.add(li);
//				if (!li.intersectedPlanes.contains(this)) li.intersectedPlanes.add(this);
//				if (!li.intersectedPlanes.contains(p)) li.intersectedPlanes.add(p);
//				return li;
//			}
//		}
//		
//		return new LineEquation(this, p);
//	}

	@Override
	public String toString() {
		return "\n    -PlaneEquation : " + a + "x + " + b + "y + " + c + " + " + d + " = 0";
	}

	public static void intersect3(PlaneEquation p1, PlaneEquation p2, PlaneEquation p3) {
		double[][] A = {
		        {p1.a, p1.b, p1.c},
		        {p2.a, p2.b, p2.c},
		        {p3.a, p3.b, p3.c}
		    };
		    
		    double[] D = {-p1.d, -p2.d, -p3.d};

		    double detA = det3x3(A);
		    if (Math.abs(detA) < 1e-6) return; // planes are parallel

		    double[][] Ax = { {D[0], A[0][1], A[0][2]}, {D[1], A[1][1], A[1][2]}, {D[2], A[2][1], A[2][2]} };
		    double[][] Ay = { {A[0][0], D[0], A[0][2]}, {A[1][0], D[1], A[1][2]}, {A[2][0], D[2], A[2][2]} };
		    double[][] Az = { {A[0][0], A[0][1], D[0]}, {A[1][0], A[1][1], D[1]}, {A[2][0], A[2][1], D[2]} };

		    double x = det3x3(Ax) / detA;
		    double y = det3x3(Ay) / detA;
		    double z = det3x3(Az) / detA;

		    var v = new Vertex((float)x, (float)y, (float)z);
		    p1.containedVertices.add(v);
		    p2.containedVertices.add(v);
		    p3.containedVertices.add(v);
		    
	}

	static double det3x3(double[][] m) {
	    return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
	         - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
	         + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
	}

	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[16]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat(a);
		bb.putFloat(b);
		bb.putFloat(c);
		bb.putFloat(d);
		return bb.array();
	}


	public PlaneEquation deepCopy(CollisionConvexVertice v) {
		var copy = new PlaneEquation();
		copy.a = this.a;
		copy.b = this.b;
		copy.c = this.c;
		copy.d = this.d;
		copy.convexVerticeShape = v;
		copy.colorR = Collisions.randomizeColor(v.bound.colorR, 0.4);
		copy.colorG = Collisions.randomizeColor(v.bound.colorG, 0.4);
		copy.colorB = Collisions.randomizeColor(v.bound.colorB, 0.4);
		return copy;
	}

	public PlaneEquation deepCopyOffset(CollisionConvexVertice v) {
		var copy = new PlaneEquation();
		copy.a = this.a;
		copy.b = this.b;
		copy.c = this.c;
		copy.d = this.d-0.05f;
		copy.convexVerticeShape = v;
		copy.colorR = Collisions.randomizeColor(v.bound.colorR, 0.4);
		copy.colorG = Collisions.randomizeColor(v.bound.colorG, 0.4);
		copy.colorB = Collisions.randomizeColor(v.bound.colorB, 0.4);
		return copy;
	}
}
