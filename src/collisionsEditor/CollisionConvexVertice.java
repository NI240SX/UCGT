package collisionsEditor;

import java.nio.ByteBuffer;
import java.util.ArrayList;

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

		return load;
	}

}
