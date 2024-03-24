package collisionsEditor;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class CollisionConvexVertice extends CollisionShape {

	float CenterX = 0;
	float CenterY = 0;
	float CenterZ = 0;
	float CenterW = 0;

	float HalfExtentsX = 0;
	float HalfExtentsY = 0;
	float HalfExtentsZ = 0;
	float HalfExtentsW = 0;

	int NumberOfPlaneEquations = 0;
	int NumberOfRotatedVertices = 0;
	int NumVertices = 0;
	
	float unknownFloat = (float) 0.05;

	ArrayList<PlaneEquation> PlaneEquations = new ArrayList<PlaneEquation>();
	ArrayList<RotatedVertice> RotatedVertices = new ArrayList<RotatedVertice>();

	MeshView shape = new MeshView();

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

	public void updateShape() {
		TriangleMesh planeMesh = new TriangleMesh();
//        planeMesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);

		planeMesh.getTexCoords().addAll(0, 0);
		
		for (PlaneEquation p : PlaneEquations) {
	        // Calculate y-coordinate for each vertex based on the plane equation
			// x= +/- 1m; z= +/- 2m
			// ax + by + cz + d = 0 -> y = 

			//approximate method, not that great
			if (Math.abs(p.Y)>0.05) {
            planeMesh.getPoints().addAll(
            		-carHalfWidth, 	(-p.X*-carHalfWidth - p.Z*-carHalfLength -p.W)/p.Y, 	-carHalfLength,	//0
            		carHalfWidth, 	(-p.X*carHalfWidth -  p.Z*-carHalfLength -p.W)/p.Y, 	-carHalfLength,	//1
            		-carHalfWidth, 	(-p.X*-carHalfWidth - p.Z*carHalfLength  -p.W)/p.Y, 	carHalfLength,	//2
            		carHalfWidth, 	(-p.X*carHalfWidth -  p.Z*carHalfLength  -p.W)/p.Y, 	carHalfLength);	//3
			} else if (Math.abs(p.Z)>0.05) {
                planeMesh.getPoints().addAll(
                		-carHalfWidth, 	-carHalfHeight,		(-p.X*-carHalfWidth - p.Y*-carHalfHeight -p.W)/p.Z,	//0
                		carHalfWidth, 	-carHalfHeight,		(-p.X*carHalfWidth - p.Y*-carHalfHeight -p.W)/p.Z,	//1
                		-carHalfWidth, 	carHalfHeight,		(-p.X*-carHalfWidth - p.Y*carHalfHeight -p.W)/p.Z,	//2
                		carHalfWidth, 	carHalfHeight,		(-p.X*carHalfWidth - p.Y*carHalfHeight -p.W)/p.Z);	//3
			} else {
                planeMesh.getPoints().addAll(
                		(-p.Y*-carHalfHeight - p.Z*-carHalfLength -p.W)/p.X,	-carHalfHeight, -carHalfLength,	//0
                		(-p.Y*carHalfHeight -  p.Z*-carHalfLength -p.W)/p.X, 	carHalfHeight,	-carHalfLength,	//1
                		(-p.Y*-carHalfHeight - p.Z*carHalfLength  -p.W)/p.X, 	-carHalfHeight,	carHalfLength,	//2
                		(-p.Y*carHalfHeight -  p.Z*carHalfLength  -p.W)/p.X, 	carHalfHeight,	carHalfLength);	//3
			}
			
			
	        // Define the faces of the plane
	        planeMesh.getFaces().addAll(
	                planeMesh.getPoints().size()/3-4, 0,  planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-2, 0,  // Triangle 1 (Vertices 0, 1, 2)
	                planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-2, 0,  planeMesh.getPoints().size()/3-1, 0   // Triangle 2 (Vertices 0, 2, 3)
	        );
		}
		
		
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
			load.PlaneEquations.add(new PlaneEquation(bb));
		}

		load.updateShape();
		return load;
	}

}
