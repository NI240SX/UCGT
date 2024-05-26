package fr.ni240sx.ucgt.geometryFile.part;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;

public class MPoint {
	
	public int key;
	
	public float[][] matrix = new float[3][3];

	public float positionX = 0;
	public float positionY = 0;
	public float positionZ = 0;
	public float scale = 1; //scale or positionW ?
	
	public Hash nameHash;
	public String part = "";
	
	public void tryGuessName(Geometry geometry, Part p) {
		nameHash = Hash.guess(key, geometry.hashlist, String.format("0x%08X", key), "BIN");
	}
	
	// source learnopencv https://learnopencv.com/rotation-matrix-to-euler-angles/
	// Calculates rotation matrix to euler angles
	// The result is the same as MATLAB except the order
	// of the euler angles ( x and z are swapped ).
	public float[] rotationMatrixToEulerAngles(float[][] R){
	    float sy = (float) Math.sqrt(R[0][0] * R[0][0] +  R[1][0]*R[1][0]);
	 
	    double x, y, z;
	    if (sy > 1e-6) {
	        x = Math.atan2(R[2][1] , R[2][2]);
	        y = Math.atan2(-R[2][0] , sy);
	        z = Math.atan2(R[1][0] , R[0][0]);
	    } else {
	        x = Math.atan2(-R[1][2], R[1][1]);
	        y = Math.atan2(-R[2][0], sy);
	        z = 0;
	    }
	    return new float[] {(float)Math.round(Math.toDegrees(x)*1000)/1000, (float)Math.round(Math.toDegrees(y)*1000)/1000, (float)Math.round(Math.toDegrees(z)*1000)/1000};
//	    return new float[] {(float) Math.toDegrees(x), (float) Math.toDegrees(y), (float) Math.toDegrees(z)};
//	    return new int[] {(int) Math.round(Math.toDegrees(x)), (int) Math.round(Math.toDegrees(y)), (int) Math.round(Math.toDegrees(z))};
	 
	}

	public String toConfig() {
		String s = "";
		var m = rotationMatrixToEulerAngles(matrix);
		s += "MARKER	"
		+nameHash.label +"	"
		+part+"	"
		+positionX+"	"+positionY+"	"+positionZ+"	"
		+ m[0] + "	" + m[1] + "	" + m[2];
		return s;
	}
}
