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
		posX = (double)(in.getShort())/3276.8; //half4
		posY = (double)(in.getShort())/3276.8;
		posZ = (double)(in.getShort())/3276.8;
		posW = (double)(in.getShort())/3276.8;
		
		texU = (double)(in.getShort())/1024.0;	//half2
		texV = (double)(1-(in.getShort())/1024.0);

		colorR = in.get();	//color
		colorG = in.get();
		colorB = in.get();
		colorA = in.get();
		
		normX = (double)(in.getShort())/32768.0; //dec4n
		normY = (double)(in.getShort())/32768.0;
		normZ = (double)(in.getShort())/32768.0;
		normW = (double)(in.getShort())/32768.0;

		tanX = (double)(in.getShort())/32768.0; //dec4n
		tanY = (double)(in.getShort())/32768.0;
		tanZ = (double)(in.getShort())/32768.0;
		tanW = (double)(in.getShort())/32768.0;
		
//		System.out.println(this);
	}

	public Vertex() {
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
		return Objects.hash(colorA, colorB, colorG, colorR, normW, normX, normY, normZ, posW, posX, posY, posZ, tanW,
				tanX, tanY, tanZ, texU, texV);
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
				&& Double.doubleToLongBits(normW) == Double.doubleToLongBits(other.normW)
				&& Double.doubleToLongBits(normX) == Double.doubleToLongBits(other.normX)
				&& Double.doubleToLongBits(normY) == Double.doubleToLongBits(other.normY)
				&& Double.doubleToLongBits(normZ) == Double.doubleToLongBits(other.normZ)
				&& Double.doubleToLongBits(posW) == Double.doubleToLongBits(other.posW)
				&& Double.doubleToLongBits(posX) == Double.doubleToLongBits(other.posX)
				&& Double.doubleToLongBits(posY) == Double.doubleToLongBits(other.posY)
				&& Double.doubleToLongBits(posZ) == Double.doubleToLongBits(other.posZ)
				&& Double.doubleToLongBits(tanW) == Double.doubleToLongBits(other.tanW)
				&& Double.doubleToLongBits(tanX) == Double.doubleToLongBits(other.tanX)
				&& Double.doubleToLongBits(tanY) == Double.doubleToLongBits(other.tanY)
				&& Double.doubleToLongBits(tanZ) == Double.doubleToLongBits(other.tanZ)
				&& Double.doubleToLongBits(texU) == Double.doubleToLongBits(other.texU)
				&& Double.doubleToLongBits(texV) == Double.doubleToLongBits(other.texV);
	}

	@Override
	public String toString() {
		return "Vertex [posX=" + posX + ", posY=" + posY + ", posZ=" + posZ + ", posW=" + posW + ", texU=" + texU
				+ ", texV=" + texV + ", colorR=" + colorR + ", colorG=" + colorG + ", colorB=" + colorB + ", colorA="
				+ colorA + ", normX=" + normX + ", normY=" + normY + ", normZ=" + normZ + ", normW=" + normW + ", tanX="
				+ tanX + ", tanY=" + tanY + ", tanZ=" + tanZ + ", tanW=" + tanW + "]";
	}
}
