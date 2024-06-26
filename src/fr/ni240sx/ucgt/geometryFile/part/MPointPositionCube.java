package fr.ni240sx.ucgt.geometryFile.part;

import java.util.ArrayList;
import java.util.Objects;

public class MPointPositionCube {
	public float x;
	public float y;
	public float z;
	public ArrayList<MPoint> mpoints = new ArrayList<>();
	
	public MPointPositionCube(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public MPointPositionCube(MPoint mp) {
		super();
		this.x = mp.positionX;
		this.y = mp.positionY;
		this.z = mp.positionZ;
		mpoints.add(mp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
//		System.out.println("Comparing");
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() == MPoint.class) {
			MPoint other = (MPoint) obj;
			return MPoint.floatEquals(x, other.positionX) && MPoint.floatEquals(y, other.positionY) && MPoint.floatEquals(z, other.positionZ);
		}
		if (getClass() != obj.getClass())
			return false;
		MPointPositionCube other = (MPointPositionCube) obj;
		return MPoint.floatEquals(x, other.x)
				&& MPoint.floatEquals(y, other.y)
				&& MPoint.floatEquals(z, other.z);
	}
}
