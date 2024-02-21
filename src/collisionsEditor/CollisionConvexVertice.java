package collisionsEditor;

import java.util.ArrayList;

public class CollisionConvexVertice {

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
		return "CollisionConvexVertice [CenterX=" + CenterX + ", CenterY=" + CenterY + ", CenterZ=" + CenterZ
				+ ", CenterW=" + CenterW + ", HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY
				+ ", HalfExtentsZ=" + HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW + ", NumberOfPlaneEquations="
				+ NumberOfPlaneEquations + ", NumberOfRotatedVertices=" + NumberOfRotatedVertices + ", NumVertices="
				+ NumVertices + ", unknownFloat=" + unknownFloat + ", PlaneEquations=" + PlaneEquations
				+ ", RotatedVertices=" + RotatedVertices + "]";
	}

}
