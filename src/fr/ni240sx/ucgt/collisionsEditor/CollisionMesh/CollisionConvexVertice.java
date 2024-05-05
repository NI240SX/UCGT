package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.CollisionsEditor;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionShape;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

public class CollisionConvexVertice extends CollisionShape {

	public float CenterX = 0;
	public float CenterY = 0;
	public float CenterZ = 0;
	public float CenterW = 0;

	public float HalfExtentsX = 0;
	public float HalfExtentsY = 0;
	public float HalfExtentsZ = 0;
	public float HalfExtentsW = 0;

	public int NumberOfPlaneEquations = 0;
	public int NumberOfRotatedVertices = 0;
	public int NumVertices = 0;
	
	public float unknownFloat = (float) 0.05;

	public ArrayList<PlaneEquation> PlaneEquations = new ArrayList<PlaneEquation>();
	public ArrayList<LineEquation> LineEquations = new ArrayList<LineEquation>();
	public ArrayList<Vertex> Vertices = new ArrayList<Vertex>();
	
	public ArrayList<RotatedVertice> RotatedVertices = new ArrayList<RotatedVertice>();

	public ArrayList<float[]> vertices = new ArrayList<float[]>();
	
	public MeshView shape = new MeshView();

	public static float carHalfWidth = 1f;
	public static float carHalfLength = 2f;
	public static float carHalfHeight = 0.7f;
	
	public CollisionConvexVertice() {
		// TODO Auto-generated constructor stub
	}

	public CollisionConvexVertice(float centerX, float centerY, float centerZ, float centerW, float halfExtentsX,
			float halfExtentsY, float halfExtentsZ, float halfExtentsW, int numberOfPlaneEquations,
			int numberOfRotatedVertices, int numVertices, float unknownFloat, ArrayList<PlaneEquation> planeEquations,
			ArrayList<RotatedVertice> rotatedVertices) {
		CenterX = centerX;
		CenterY = centerY;
		CenterZ = centerZ;
		CenterW = centerW;
		HalfExtentsX = halfExtentsX;
		HalfExtentsY = halfExtentsY;
		HalfExtentsZ = halfExtentsZ;
		HalfExtentsW = halfExtentsW;
		NumberOfPlaneEquations = numberOfPlaneEquations;
		NumberOfRotatedVertices = numberOfRotatedVertices;
		NumVertices = numVertices;
		this.unknownFloat = unknownFloat;
		PlaneEquations = planeEquations;
		RotatedVertices = rotatedVertices;
		calculateVertices();
		updateShape();
	}

	@Override
	public String toString() {
		return "\n -CollisionConvexVertice [CenterX=" + CenterX + ", CenterY=" + CenterY + ", CenterZ=" + CenterZ
				+ ", CenterW=" + CenterW + ", HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY
				+ ", HalfExtentsZ=" + HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW + ", NumberOfPlaneEquations="
				+ NumberOfPlaneEquations + ", NumberOfRotatedVertices=" + NumberOfRotatedVertices + ", NumVertices="
				+ NumVertices + ", unknownFloat=" + unknownFloat + ",\n PlaneEquations=" + PlaneEquations
				+ ",\n RotatedVertices=" + RotatedVertices + "]";
	}

	public void calculateVertices() {
		//I FUCKED UP, 2025 WILL BE MY YEAR
		//maybe look up the closest planes to the center of gravity on the x, y, z axes instead, then find the closest top/bottom/sides intersections
		//it's always the closest plane to the axes that's taken into account
		
		// 1) intersect all planes -> lines and all lines -> vertices
		// 2) intersect all planes with the 3 axes, find the 6 planes closest to the center of the mesh on each axis (x, -x, y, -y, z, -z)
		// 3) build the intersection shape around each intersection point ???
		// 4) ???
		Vertex COG = new Vertex(CenterX, CenterY, CenterZ);
		PlaneEquation closestPosX;
		PlaneEquation closestPosY;
		PlaneEquation closestPosZ;
		PlaneEquation closestNegX;
		PlaneEquation closestNegY;
		PlaneEquation closestNegZ;
		double closestDistPosX = Float.POSITIVE_INFINITY;
		double closestDistPosY = Float.POSITIVE_INFINITY;
		double closestDistPosZ = Float.POSITIVE_INFINITY;
		double closestDistNegX = Float.POSITIVE_INFINITY;
		double closestDistNegY = Float.POSITIVE_INFINITY;
		double closestDistNegZ = Float.POSITIVE_INFINITY;
		
		
		for (PlaneEquation p : this.PlaneEquations) {
			for (PlaneEquation p2 : this.PlaneEquations) if (!p.equals(p2)) {
				p.intersect(p2);
			}
			
			for (LineEquation l : p.containedLines) {
				for (LineEquation l2 : p.containedLines) if (!l.equals(l2)) {
					l.intersect(l2);
				}
			}
			
			//distance to cog
			Vertex intersectX = new Vertex(p, "x");
			Vertex intersectY = new Vertex(p, "y");
			Vertex intersectZ = new Vertex(p, "z");
			
			// find out the closest plane to the origin on the x axis
			if (COG.distanceTo(intersectX) < closestDistPosX && intersectX.x > 0) {
				closestPosX = p;
				closestDistPosX = COG.distanceTo(intersectX);
			} else if (COG.distanceTo(intersectX) < closestDistNegX && intersectX.x < 0) {
				closestNegX = p;
				closestDistNegX = COG.distanceTo(intersectX);
			}
			
			if (COG.distanceTo(intersectY) < closestDistPosY && intersectY.y > 0) {
				closestPosY = p;
				closestDistPosY = COG.distanceTo(intersectY);
			} else if (COG.distanceTo(intersectY) < closestDistNegY && intersectY.y < 0) {
				closestNegY = p;
				closestDistNegY = COG.distanceTo(intersectY);
			}
			
			if (COG.distanceTo(intersectZ) < closestDistPosZ && intersectZ.z > 0) {
				closestPosZ = p;
				closestDistPosZ = COG.distanceTo(intersectZ);
			} else if (COG.distanceTo(intersectZ) < closestDistNegZ && intersectZ.z < 0) {
				closestNegZ = p;
				closestDistNegZ = COG.distanceTo(intersectZ);
			}	
		}
		
		//now all vertices are calculated and we know 6 close planes
		//next up : find out the shortest vertices loop that encloses the intersections
		
		//for each plane (?) : find 4 close lines to the intersection point on the plane's x' and y'
		

		System.out.println("Number of calculated vertices : "+this.Vertices.size()+" vs actual number of vertices : "+this.NumVertices);
	}
	
	public void updateShape() {
		TriangleMesh planeMesh = new TriangleMesh();
		planeMesh.getTexCoords().addAll(0, 0);

		
		for (Vertex v : this.Vertices) {
			Sphere s;
			CollisionsEditor.viewport.viewportGroup.getChildren().addAll(s = new Sphere(0.05));
			s.setTranslateX(v.x);
			s.setTranslateY(v.y - CollisionsEditor.mainCollisions.Z);
			s.setTranslateZ(v.z - CollisionsEditor.mainCollisions.X);
//			System.out.println(v);
		}
		
		
		
		
		// display the planes themselves
		
		for (PlaneEquation p : PlaneEquations) {
	        // Calculate y-coordinate for each vertex based on the plane equation
			// x= +/- 1m; z= +/- 2m
			// ax + by + cz + d = 0 -> y = 

			//approximate method, not that great
			if (Math.abs(p.b)>0.05) {
            planeMesh.getPoints().addAll(
            		CenterX+ -carHalfWidth, 	(-p.a*-carHalfWidth - p.c*-carHalfLength -p.d)/p.b, CenterZ+ 	-carHalfLength,	//0
            		CenterX+ carHalfWidth, 	(-p.a*carHalfWidth -  p.c*-carHalfLength -p.d)/p.b, 	CenterZ+ -carHalfLength,	//1
            		CenterX+ -carHalfWidth, 	(-p.a*-carHalfWidth - p.c*carHalfLength  -p.d)/p.b, CenterZ+ 	carHalfLength,	//2
            		CenterX+ carHalfWidth, 	(-p.a*carHalfWidth -  p.c*carHalfLength  -p.d)/p.b, 	CenterZ+ carHalfLength);	//3
			} else if (Math.abs(p.c)>0.05) {
                planeMesh.getPoints().addAll(
                		CenterX+ -carHalfWidth, CenterY+ 	-carHalfHeight,		(-p.a*-carHalfWidth - p.b*-carHalfHeight -p.d)/p.c,	//0
                		CenterX+ carHalfWidth, 	CenterY+ -carHalfHeight,		(-p.a*carHalfWidth - p.b*-carHalfHeight -p.d)/p.c,	//1
                		CenterX+ -carHalfWidth, CenterY+ 	carHalfHeight,		(-p.a*-carHalfWidth - p.b*carHalfHeight -p.d)/p.c,	//2
                		CenterX+ carHalfWidth, 	CenterY+ carHalfHeight,		(-p.a*carHalfWidth - p.b*carHalfHeight -p.d)/p.c);	//3
			} else {
                planeMesh.getPoints().addAll(
                		(-p.b*-carHalfHeight - p.c*-carHalfLength -p.d)/p.a,	CenterY+ -carHalfHeight, 	CenterZ+ -carHalfLength,	//0
                		(-p.b*carHalfHeight -  p.c*-carHalfLength -p.d)/p.a, 	CenterY+ carHalfHeight,		CenterZ+ -carHalfLength,	//1
                		(-p.b*-carHalfHeight - p.c*carHalfLength  -p.d)/p.a, 	CenterY+ -carHalfHeight,	CenterZ+ carHalfLength,	//2
                		(-p.b*carHalfHeight -  p.c*carHalfLength  -p.d)/p.a, 	CenterY+ carHalfHeight,		CenterZ+ carHalfLength);	//3
			}
			
			
	        // Define the faces of the plane
	        planeMesh.getFaces().addAll(
	                planeMesh.getPoints().size()/3-4, 0,  planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-2, 0,  // Triangle 1 (Vertices 0, 1, 2)
	                planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-2, 0,  planeMesh.getPoints().size()/3-1, 0   // Triangle 2 (Vertices 1, 2, 3) should be 1,3,2
	        );
		}

		
		
		
		
		
//		
//		// the convexverticeshape is the smallest shape formed by all planes
//		// 1) intersect planes -> lines
//		// 2) intersect lines -> vertices
//		// 3) find the smallest area surface per plane ? -> NO, good as a shit first approach maybe
//		vertices = new ArrayList<float[]>();
//		for (PlaneEquation p : PlaneEquations) {
//			ArrayList<float[]> lineEquations = new ArrayList<float[]>();
//			for (PlaneEquation p2 : PlaneEquations) if (!p.equals(p2)) {
//				lineEquations.add(new float[] {p.X*p2.Z - p2.X*p.Z, p.Y*p2.Z - p2.Y*p.Z, p.W*p2.Z - p2.W*p.Z});
//				// ax + by + cz + d = 0							--> z = (-ax -by -d)/c
//				// ex + fy + gz + h = 0  --> a'x + b'y + c = 0
//				// (-ax -by -d)/c = (-ex -fy -h)/g
//				// agx +bgy +dg = ecx + fcy + ch	--> (ag-ec)x + (bg-fc)y + dg-ch = 0
//			}
//
//			ArrayList<float[]> intersectedVertices = new ArrayList<float[]>();
//			for (float[] line : lineEquations) { 
//				for (float[] line2 : lineEquations) if (!line.equals(line2)) {
//					float x = (line2[2]*line[1] - line2[1]*line[2]) / (line[0]*line2[1] - line2[0]*line[1]);
//					float y = (-line[0]*x - line[2])/line[1];
//					float z = (-p.X*x -p.Y*y -p.W)/p.Z;
//					intersectedVertices.add(new float[] {x, y, z});
//					vertices.add(new float[] {x, y, z});
//					// ax + by + c = 0		--> y = (-ax -c)/b
//					// dx + ey + f = 0
//					// (-ax -c)/b = (-dx -f)/e
//					// aex + ec = dbx + fb	--> (ae-db)x + ec-fb = 0	--> x = (fb-ec)/(ae-db)
//				}
//			}
//
//			//shit
//			planeMesh = new TriangleMesh();
//			planeMesh.getTexCoords().addAll(0, 0);
//			int oldSize = planeMesh.getPoints().size();
//			for (int i=0; i<intersectedVertices.size(); i++) {
//				planeMesh.getPoints().addAll(intersectedVertices.get(i)[0], intersectedVertices.get(i)[1], intersectedVertices.get(i)[2]);
//			}
//			for (int i=(oldSize+2); i<oldSize+intersectedVertices.size(); i++) {
//				planeMesh.getFaces().addAll(
//		                i-2, 0,  i-1, 0, i, 0);  // Triangle 1 (Vertices 0, 1, 2)
//			}
//			
//			
//		}
//		
//		System.out.println("Number of calculated vertices : "+vertices.size()+" vs actual number of vertices : "+this.NumVertices);
//		
        // Create a MeshView to render the plane
        this.shape = new MeshView(planeMesh);
	}
	
	public static CollisionConvexVertice load(ByteBuffer bb) {
		CollisionConvexVertice load = new CollisionConvexVertice();
		bb.position(bb.position() + 0x10);
		load.unknownFloat = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		
		load.HalfExtentsX = bb.getFloat();
		load.HalfExtentsY = bb.getFloat();
		load.HalfExtentsZ = bb.getFloat();
		load.HalfExtentsW = bb.getFloat();
		load.CenterX = bb.getFloat();
		load.CenterY = bb.getFloat();
		load.CenterZ = bb.getFloat();
		load.CenterW = bb.getFloat();
		//TODO currently going around the HKArray, might need to do this properly
		//commented is the C code from Binary
//		load.arrRotatedVertices.Read(br); (aka the following)

//		br.BaseStream.Position += 0x4;
		bb.getInt();
		load.NumberOfRotatedVertices = bb.getShort();
		bb.position(bb.position()+6);
//		br.BaseStream.Position += 0x2;
//		this.Capacity = br.ReadInt16();
//		br.BaseStream.Position += 0x1;
//		this.Flags = br.ReadByte();
		
		load.NumVertices = bb.getInt();
//		load.arrPlaneEquations.Read(br);
		bb.getInt();
		load.NumberOfPlaneEquations = bb.getShort();
		bb.position(bb.position()+10);

		// Get Rotated Vertices
		for (int loop = 0; loop < load.NumberOfRotatedVertices; loop++)
		{
			load.RotatedVertices.add(new RotatedVertice(bb));
		}

		// Get Plane Equations
		for (int loop = 0; loop < load.NumberOfPlaneEquations; loop++)
		{
			load.PlaneEquations.add(new PlaneEquation(bb, load));
		}

		load.updateShape();
		load.calculateVertices();
		return load;
	}

}
