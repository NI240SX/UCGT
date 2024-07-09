package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Vertex {
	public double posX=0x0CCD/3276.8; //position
	public double posY=0x0CCD/3276.8;
	public double posZ=0x0CCD/3276.8;
	public double posW=0x0CCD/3276.8;
	
	public double texU=0.0; //texcoord
	public double texV=0.0;
	
	public byte colorR=(byte) 255; //color
	public byte colorG=(byte) 255;
	public byte colorB=(byte) 255;
	public byte colorA=(byte) 255;

	public double normX=0x7FFF/32768.0; //normals
	public double normY=0.0;
	public double normZ=0.0;
	public double normW=0x7FFF/32768.0;

	public double tanX=0.0; //tangents, usually no data included
	public double tanY=0.0;
	public double tanZ=0.0;
	public double tanW=0.0;
	
	public Vertex(ByteBuffer in) {
		posX = (in.getShort())/3276.8; //half4
		posY = (in.getShort())/3276.8;
		posZ = (in.getShort())/3276.8;
		posW = (in.getShort())/3276.8;
		
		texU = (in.getShort())/1024.0;	//half2
		texV = 1-(in.getShort())/1024.0;

		colorR = in.get();	//color
		colorG = in.get();
		colorB = in.get();
		colorA = in.get();
		
		normX = (in.getShort())/32768.0; //dec4n
		normY = (in.getShort())/32768.0;
		normZ = (in.getShort())/32768.0;
		normW = (in.getShort())/32768.0;

		tanX = (in.getShort())/32768.0; //dec4n
		tanY = (in.getShort())/32768.0;
		tanZ = (in.getShort())/32768.0;
		tanW = (in.getShort())/32768.0;
		
//		System.out.println(this);
	}

	public Vertex() {
	}

	public Vertex(Vertex v) {
		posX = v.posX;
		posY = v.posY;
		posZ = v.posZ;
		posW = v.posW;
		texU = v.texU;
		texV = v.texV;
		colorR = v.colorR;
		colorG = v.colorG;
		colorB = v.colorB;
		colorA = v.colorA;
		normX = v.normX;
		normY = v.normY;
		normZ = v.normZ;
		normW = v.normW;
		tanX = v.tanX;
		tanY = v.tanY;
		tanZ = v.tanZ;
		tanW = v.tanW;
	}

	public void save(ByteBuffer out) {
		out.putShort((short) (posX*3276.8));
		out.putShort((short) (posY*3276.8));
		out.putShort((short) (posZ*3276.8));
		out.putShort((short) (posW*3276.8));

		out.putShort((short) (texU*1024));
		out.putShort((short) ((1-texV)*1024));

		out.put(colorR);
		out.put(colorG);
		out.put(colorB);
		out.put(colorA);
		
		out.putShort((short) (normX*32768));
		out.putShort((short) (normY*32768));
		out.putShort((short) (normZ*32768));
		out.putShort((short) (normW*32768));

		out.putShort((short) (tanX*32768));
		out.putShort((short) (tanY*32768));
		out.putShort((short) (tanZ*32768));
		out.putShort((short) (tanW*32768));
	}

	@Override
	public int hashCode() {
		return Objects.hash(colorA, colorB, colorG, colorR, normX, normY, normZ, posX, posY, posZ, texU, texV);
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
		return colorA == other.colorA && colorB == other.colorB && colorG == other.colorG && colorR == other.colorR
//				&& Double.doubleToLongBits(normW) == Double.doubleToLongBits(other.normW)
				&& doubleEquals(posX, other.posX, 1000)
				&& doubleEquals(posY, other.posY, 1000)
				&& doubleEquals(posZ, other.posZ, 1000)
				&& doubleEquals(normX, other.normX, 1000)
				&& doubleEquals(normY, other.normY, 1000)
				&& doubleEquals(normZ, other.normZ, 1000)
				&& doubleEquals(texU, other.texU, 1000)
				&& doubleEquals(texV, other.texV, 1000)
//				&& Double.doubleToLongBits(posW) == Double.doubleToLongBits(other.posW)
				;
	}

	public boolean positionEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		return doubleEquals(posX, other.posX, 1000)
				&& doubleEquals(posY, other.posY, 1000)
				&& doubleEquals(posZ, other.posZ, 1000)
				&& doubleEquals(texU, other.texU, 1000)
				&& doubleEquals(texV, other.texV, 1000);
	}
	
	public static boolean doubleEquals(double a, double b, int precision) {
		//takes in account floating point errors, we don't need more than millimeter precision
		return (int)(a*precision) == (int)(b*precision);
	}

	@Override
	public String toString() {
		return "Vertex [posX=" + posX + ", posY=" + posY + ", posZ=" + posZ + ", posW=" + posW + ", texU=" + texU
				+ ", texV=" + texV + ", colorR=" + colorR + ", colorG=" + colorG + ", colorB=" + colorB + ", colorA="
				+ colorA + ", normX=" + normX + ", normY=" + normY + ", normZ=" + normZ + ", normW=" + normW + ", tanX="
				+ tanX + ", tanY=" + tanY + ", tanZ=" + tanZ + ", tanW=" + tanW + "]";
	}
}
