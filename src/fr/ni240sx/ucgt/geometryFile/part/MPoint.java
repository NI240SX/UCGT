package fr.ni240sx.ucgt.geometryFile.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.io.BasicVertex;

public class MPoint {
	
//	public int key;
	
	public float[][] matrix = new float[3][3];

	public float positionX = 0;
	public float positionY = 0;
	public float positionZ = 0;
	public float scale = 1; //scale or positionW ?
	
	public Hash nameHash;
	public ArrayList<Part> parts = new ArrayList<Part>();
	
	public String uniqueName = "_";
	
	public String tempPartName; //used only when loading from file
	public ArrayList<BasicVertex> verts = new ArrayList<BasicVertex>();
	
	public MPoint() {
	}
	
	public MPoint(MPoint mp) {
//		this.key = mp.key;
		this.nameHash = mp.nameHash;
		this.matrix = mp.matrix;
		this.positionX = mp.positionX;
		this.positionY = mp.positionY;
		this.positionZ = mp.positionZ;
		this.scale = mp.scale;
	}

	public void tryGuessName(Geometry geometry, Part p) {
		nameHash = Hash.guess(nameHash.binHash, geometry.hashlist, String.format("0x%08X", nameHash.binHash), "BIN");
	}
	
	public static float[] rotationMatrixToEulerAngles(float[][] R){
		//based on code from danceswithcode.net
		double x, y, z;
		y = -Math.asin(R[2][0]);
		
		//Gymbal lock: pitch = -90
        if( R[2][0] == 1 ){    
            x = 0.0;             //yaw = 0
            z = Math.atan2( -R[0][1], -R[0][2] );    //Roll
//            System.out.println(this.uniqueName+" Gimbal lock: pitch = -90");
        }

        //Gymbal lock: pitch = 90
        else if( R[2][0] == -1 ){    
            x = 0.0;             //yaw = 0
            z = Math.atan2( R[0][1], R[0][2]);    //Roll
//            System.out.println(this.uniqueName+" Gimbal lock: pitch = 90");
        }
        //General solution
        else{
            x = Math.atan2(  R[1][0], R[0][0] );
            z = Math.atan2(  R[2][1], R[2][2] );
//            System.out.println(this.uniqueName+" No gimbal lock");
        }
	    return new float[] {(float)Math.round(-Math.toDegrees(z)*1000)/1000, (float)Math.round(-Math.toDegrees(y)*1000)/1000, (float)Math.round(-Math.toDegrees(x)*1000)/1000};
//	    return new float[] {(float) Math.toDegrees(x), (float) Math.toDegrees(y), (float) Math.toDegrees(z)};
//	    return new int[] {(int) Math.round(Math.toDegrees(x)), (int) Math.round(Math.toDegrees(y)), (int) Math.round(Math.toDegrees(z))};
	 
	}
	
	
	public static float[][] eulerAnglesToMatrix(double u, double v, double w ) {
		//based on code from danceswithcode.net
		u = Math.toRadians(-u);
		v = Math.toRadians(-v);
		w = Math.toRadians(-w);
		
        //Precompute sines and cosines of Euler angles
        double su = Math.sin(w);
        double cu = Math.cos(w);
        double sv = Math.sin(v);
        double cv = Math.cos(v);
        double sw = Math.sin(u);
        double cw = Math.cos(u);
        
        float[][] R = new float[3][3];
        R[0][0] = (float) (cv*cw);
        R[0][1] = (float) (su*sv*cw - cu*sw);
        R[0][2] = (float) (su*sw + cu*sv*cw);
        R[1][0] = (float) (cv*sw);
        R[1][1] = (float) (cu*cw + su*sv*sw);
        R[1][2] = (float) (cu*sv*sw - su*cw);
        R[2][0] = (float) -sv;
        R[2][1] = (float) (su*cv);
        R[2][2] = (float) (cu*cv);         
        return R;
	}

	public String toConfig() {
		String s = "";
		var m = rotationMatrixToEulerAngles(matrix);
		for (var p : parts) {
			s += "MARKER	"
			+uniqueName+"	"
			+nameHash.label +"	"
			+p.kit+"_"+p.part+"_"+p.lod+"	";
//			+positionX+"	"+positionY+"	"+positionZ+"	"
//			s += m[0] + "	" + m[1] + "	" + m[2] + "\n";
			// inverted axes and using - to have the same angles as ctk
			if ((int)m[0] == m[0]) s+=(int)m[0]; else s+=m[0];
			s += "	";
			if ((int)m[1] == m[1]) s+=(int)m[1]; else s+=m[1];
			s += "	";
			if ((int)m[2] == m[2]) s+=(int)m[2]; else s+=m[2];
			s += "\n";
		}
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(matrix);
		result = prime * result + Objects.hash(nameHash.binHash, nameHash, positionX, positionY, positionZ, scale);
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
		MPoint other = (MPoint) obj;
		return nameHash.binHash == other.nameHash.binHash && Arrays.deepEquals(matrix, other.matrix)
				&& Float.floatToIntBits(positionX) == Float.floatToIntBits(other.positionX)
				&& Float.floatToIntBits(positionY) == Float.floatToIntBits(other.positionY)
				&& Float.floatToIntBits(positionZ) == Float.floatToIntBits(other.positionZ)
				&& Float.floatToIntBits(scale) == Float.floatToIntBits(other.scale);
	}
}
