package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import fr.ni240sx.ucgt.geometryFile.part.mesh.X360.HalfPrecisionFloat;

public class Vertex {
	public float posX=0x0CCD/posFactor; //position
	public float posY=0x0CCD/posFactor;
	public float posZ=0x0CCD/posFactor;
//	public float posW=0x0CCD/posFactor;
	
	public float tex0U=0.0f; //texcoord
	public float tex0V=0.0f;

	public float tex1U=0.0f;
	public float tex1V=0.0f;
	public float tex2U=0.0f;
	public float tex2V=0.0f;
	
	public byte colorR=(byte) 255; //color
	public byte colorG=(byte) 255;
	public byte colorB=(byte) 255;
	public byte colorA=(byte) 255;

	public float normX=1.0f; //normals
	public float normY=0.0f;
	public float normZ=0.0f;
	public float normW=1.0f;

	public float tanX=0.0f; //tangents, usually no data included
	public float tanY=0.0f;
	public float tanZ=0.0f;
	public float tanW=0.0f;

	public static final float posFactor = 32767.0f/10f;
	public static final float UVFactor = 1024.0f; //32767.0f/32f;
	public static final float vecFactor = 32767.0f;

	public static final float short4n_10x_min = Short.MIN_VALUE/posFactor;
	public static final float short4n_10x_max = Short.MAX_VALUE/posFactor;
	public static final float short2n_32x_min = Short.MIN_VALUE/UVFactor;
	public static final float short2n_32x_max = Short.MAX_VALUE/UVFactor;
	
	public Vertex(ByteBuffer in, VertexFormat vf) {
		switch(vf) {
		case Pos0_Col0_Tex0s32:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			break;
		case Pos0_Col0_Tex0s32_Norm0s:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			norm0s_in(in);
			break;
		case Pos0_Col0_Tex0s32_Norm0s_Tan0s:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			norm0s_in(in);
			tan0s_in(in);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			tex1s32_in(in);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			tex1s32_in(in);
			norm0s_in(in);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			tex1s32_in(in);
			norm0s_in(in);
			tan0s_in(in);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s:
			pos0_in(in);
			col0_in(in);
			tex0s32_in(in);
			tex1s32_in(in);
			tex2s32_in(in);
			norm0s_in(in);
			tan0s_in(in);
			break;
		case Pos0_Col0f4_Tex0:
			pos0_in(in);
			col0f4_in(in);
			tex0_in(in);
			break;
		case Pos0_Col0f4_Tex0_Norm0s:
			pos0_in(in);
			col0f4_in(in);
			tex0_in(in);
			norm0s_in(in);
			break;
		case Pos0_Tex0:
			pos0_in(in);
			tex0_in(in);
			break;
		case Pos0_Tex0_Norm0_Tan0:
			pos0_in(in);
			tex0_in(in);
			norm0_in(in);
			tan0_in(in);
			break;
		case Pos0_Tex0_Tex1:
			pos0_in(in);
			tex0_in(in);
			tex1_in(in);
			break;
		case Pos0_Tex0s32:
			pos0_in(in);
			tex0s32_in(in);
			break;
		case Pos0_Tex0s32_Norm0s:
			pos0_in(in);
			tex0s32_in(in);
			norm0s_in(in);
			break;
		case Pos0f3_Tex0s32_Norm0s:
			pos0f3_in(in);
			tex0s32_in(in);
			norm0s_in(in);
			break;
		case Pos0f3_Norm0s:
			pos0f3_in(in);
			norm0s_in(in);
			break;
		case Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s:
			pos0_in(in);
			tex0s32_in(in);
			col0_in(in);
			in.getInt(); //skips blendindices
			norm0s_in(in);
			tan0s_in(in);
			break;
		case Pos0s10_Tex0s32_Col0_Norm0s_Tan0s: //car
			pos0s10_in(in);
			tex0s32_in(in);
			col0_in(in);
			norm0s_in(in);
			tan0s_in(in);
			break;
			
		case Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2: //X360 car
			pos0h4_in(in);
			norm0d4n_in(in);
			tan0d4n_in(in);
			col0ARGB_in(in);
			tex0h2_in(in);
			break;
		}	
	}
	
	private void pos0_in(ByteBuffer in) {
		posX = in.getFloat();
		posY = in.getFloat();
		posZ = in.getFloat();
		in.getFloat();
	}
	private void pos0f3_in(ByteBuffer in) {
		posX = in.getFloat();
		posY = in.getFloat();
		posZ = in.getFloat();
	}
	private void pos0s10_in(ByteBuffer in) {
		posX = (in.getShort())/posFactor; //short4n_10x
		posY = (in.getShort())/posFactor;
		posZ = (in.getShort())/posFactor;
		in.getShort();
	}
	private void pos0h4_in(ByteBuffer in) {
		in.order(ByteOrder.BIG_ENDIAN);
		posX = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		posY = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		posZ = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		in.getShort();
		in.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void col0_in(ByteBuffer in) {
		colorR = in.get();	//color
		colorG = in.get();
		colorB = in.get();
		colorA = in.get();
	}
	private void col0ARGB_in(ByteBuffer in) {
		colorA = in.get();
		colorR = in.get();	//color
		colorG = in.get();
		colorB = in.get();
	}
	private void col0f4_in(ByteBuffer in) {
		colorR = (byte) (in.getFloat()*255);
		colorG = (byte) (in.getFloat()*255);
		colorB = (byte) (in.getFloat()*255);
		colorA = (byte) (in.getFloat()*255);
	}

	private void tex0_in(ByteBuffer in) {
		tex0U = in.getFloat();
		tex0V = 1-in.getFloat();
	}
	private void tex1_in(ByteBuffer in) {
		tex1U = in.getFloat();
		tex1V = 1-in.getFloat();
	}
	private void tex0s32_in(ByteBuffer in) {
		tex0U = (in.getShort())/UVFactor;	//short2n_32x
		tex0V = 1-(in.getShort())/UVFactor;
	}
	private void tex1s32_in(ByteBuffer in) {
		tex1U = (in.getShort())/UVFactor;	//short2n_32x
		tex1V = 1-(in.getShort())/UVFactor;
	}
	private void tex2s32_in(ByteBuffer in) {
		tex2U = (in.getShort())/UVFactor;	//short2n_32x
		tex2V = 1-(in.getShort())/UVFactor;
	}
	private void tex0h2_in(ByteBuffer in) {
		in.order(ByteOrder.BIG_ENDIAN);
		tex0U = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		tex0V = 1 - new HalfPrecisionFloat(in.getShort()).getFullFloat();
		in.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private void norm0_in(ByteBuffer in) {
		normX = in.getFloat();
		normY = in.getFloat();
		normZ = in.getFloat();
		normW = in.getFloat();
	}
	private void norm0s_in(ByteBuffer in) {
		normX = (in.getShort())/vecFactor; //short4n
		normY = (in.getShort())/vecFactor;
		normZ = (in.getShort())/vecFactor;
		normW = (in.getShort())/vecFactor;
	}
	private void norm0d4n_in(ByteBuffer in) {
		var bits = Integer.reverseBytes(in.getInt());
		
//		int w = (bits >> 30) & 0b11;
		int z = (bits >> 20) & 0b1111111111; // Bits 30-21
        int y = (bits >> 10) & 0b1111111111; // Bits 20-11
        int x = bits & 0b1111111111;         // Bits 10-1

        normX = x < 512 ? x/511.0f : (x-1024)/511.0f;
        normY = y < 512 ? y/511.0f : (y-1024)/511.0f;
        normZ = z < 512 ? z/511.0f : (z-1024)/511.0f;
        
		/*
		 * SAMPLE VALUES FOR IDENTIFICATION
		 * 
		 * 0.0 -1.0 0.0		40080400	64 8 4 0		01000000 00001000 00000100 00000000
		 * 0.0 1.0 0.0		4007fc00	64 7 -4 0		01000000 00000111 11111100 00000000
		 * ~-0.01 0.0 0.99	5fe003f9	95 -32 3 -7		01011111 11100000 00000011 11111001
		 * 												W	Z	Y	X
		 * -1.0 = 513
		 * -0.01 = 1017
		 * 0.99 = 510
		 * 1.0 = 511
		 * 
		 */
	}

	private void tan0_in(ByteBuffer in) {
		tanX = in.getFloat();
		tanY = in.getFloat();
		tanZ = in.getFloat();
		in.getFloat();
	}
	private void tan0s_in(ByteBuffer in) {
		tanX = (in.getShort())/vecFactor; //short4n
		tanY = (in.getShort())/vecFactor;
		tanZ = (in.getShort())/vecFactor;
		tanW = (in.getShort())/vecFactor;
	}
	private void tan0d4n_in(ByteBuffer in) {
		var bits = Integer.reverseBytes(in.getInt());
		
		int w = (bits >> 30) & 0b11;
		int z = (bits >> 20) & 0b1111111111; // Bits 30-21
        int y = (bits >> 10) & 0b1111111111; // Bits 20-11
        int x = bits & 0b1111111111;         // Bits 10-1

        tanX = x < 512 ? x/511.0f : (x-1024)/511.0f;
        tanY = y < 512 ? y/511.0f : (y-1024)/511.0f;
        tanZ = z < 512 ? z/511.0f : (z-1024)/511.0f;
        tanW = w;
	}
	
	public Vertex() {
	}

	public Vertex(Vertex v) {
		posX = v.posX;
		posY = v.posY;
		posZ = v.posZ;
//		posW = v.posW;
		tex0U = v.tex0U;
		tex0V = v.tex0V;
		colorR = v.colorR;
		colorG = v.colorG;
		colorB = v.colorB;
		colorA = v.colorA;
		normX = v.normX;
		normY = v.normY;
		normZ = v.normZ;
//		normW = v.normW;
		tanX = v.tanX;
		tanY = v.tanY;
		tanZ = v.tanZ;
		tanW = v.tanW;
	}

	public void save(ByteBuffer out, VertexFormat vf) {
		
		switch(vf) {
		case Pos0_Col0_Tex0s32:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			break;
		case Pos0_Col0_Tex0s32_Norm0s:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			norm0s_out(out);
			break;
		case Pos0_Col0_Tex0s32_Norm0s_Tan0s:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			norm0s_out(out);
			tan0s_out(out);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			tex1s32_out(out);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			tex1s32_out(out);
			norm0s_out(out);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			tex1s32_out(out);
			norm0s_out(out);
			tan0s_out(out);
			break;
		case Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s:
			pos0_out(out);
			col0_out(out);
			tex0s32_out(out);
			tex1s32_out(out);
			tex2s32_out(out);
			norm0s_out(out);
			tan0s_out(out);
			break;
		case Pos0_Col0f4_Tex0:
			pos0_out(out);
			col0f4_out(out);
			tex0_out(out);
			break;
		case Pos0_Col0f4_Tex0_Norm0s:
			pos0_out(out);
			col0f4_out(out);
			tex0_out(out);
			norm0s_out(out);
			break;
		case Pos0_Tex0:
			pos0_out(out);
			tex0_out(out);
			break;
		case Pos0_Tex0_Norm0_Tan0:
			pos0_out(out);
			tex0_out(out);
			norm0_out(out);
			tan0_out(out);
			break;
		case Pos0_Tex0_Tex1:
			pos0_out(out);
			tex0_out(out);
			tex1_out(out);
			break;
		case Pos0_Tex0s32:
			pos0_out(out);
			tex0s32_out(out);
			break;
		case Pos0_Tex0s32_Norm0s:
			pos0_out(out);
			tex0s32_out(out);
			norm0s_out(out);
			break;
		case Pos0f3_Tex0s32_Norm0s:
			pos0f3_out(out);
			tex0s32_out(out);
			norm0s_out(out);
			break;
		case Pos0f3_Norm0s:
			pos0f3_out(out);
			norm0s_out(out);
			break;
		case Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s:
			pos0_out(out);
			tex0s32_out(out);
			col0_out(out);
			out.putInt(0); //skip blendindices
			norm0s_out(out);
			tan0s_out(out);
			break;
		case Pos0s10_Tex0s32_Col0_Norm0s_Tan0s: //car
			pos0s10_out(out);
			tex0s32_out(out);
			col0_out(out);
			norm0s_out(out);
			tan0s_out(out);
			break;

		case Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2: //X360 car
			pos0h4_out(out);
			norm0d4n_out(out);
			tan0d4n_out(out);
			col0ARGB_out(out);
			tex0h2_out(out);
			break;
		}	
	}

	private void pos0_out(ByteBuffer out) {
		out.putFloat((float) posX);
		out.putFloat((float) posY);
		out.putFloat((float) posZ);
		out.putFloat(1.0f);
	}
	private void pos0f3_out(ByteBuffer out) {
		out.putFloat((float) posX);
		out.putFloat((float) posY);
		out.putFloat((float) posZ);
	}
	private void pos0s10_out(ByteBuffer out) {
		out.putShort((short) (posX*posFactor)); //short4n_10x
		out.putShort((short) (posY*posFactor));
		out.putShort((short) (posZ*posFactor));
		out.putShort((short) (0x0CCD));
	}
	private void pos0h4_out(ByteBuffer out) {
		out.order(ByteOrder.BIG_ENDIAN);
		out.putShort(new HalfPrecisionFloat(posX).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(posY).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(posZ).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(1.0f).getHalfPrecisionAsShort());
		out.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void col0_out(ByteBuffer out) {
		out.put(colorR);
		out.put(colorG);
		out.put(colorB);
		out.put(colorA);
	}
	private void col0ARGB_out(ByteBuffer out) {
		out.put(colorA);
		out.put(colorR);
		out.put(colorG);
		out.put(colorB);
	}
	private void col0f4_out(ByteBuffer out) {
		out.putFloat((float) colorR/255);
		out.putFloat((float) colorG/255);
		out.putFloat((float) colorB/255);
		out.putFloat((float) colorA/255);
	}

	private void tex0_out(ByteBuffer out) {
		out.putFloat((float) tex0U);
		out.putFloat((float) (1-tex0V));
	}
	private void tex1_out(ByteBuffer out) {
		out.putFloat((float) tex1U);
		out.putFloat((float) (1-tex1V));
	}
	private void tex0s32_out(ByteBuffer out) {
		out.putShort((short) (tex0U*UVFactor));	//short2n_32x
		out.putShort((short) ((1-tex0V)*UVFactor));
	}
	private void tex1s32_out(ByteBuffer out) {
		out.putShort((short) (tex1U*UVFactor));	//short2n_32x
		out.putShort((short) ((1-tex1V)*UVFactor));
	}
	private void tex2s32_out(ByteBuffer out) {
		out.putShort((short) (tex2U*UVFactor));	//short2n_32x
		out.putShort((short) ((1-tex2V)*UVFactor));
	}
	private void tex0h2_out(ByteBuffer out) {
		out.order(ByteOrder.BIG_ENDIAN);
		out.putShort(new HalfPrecisionFloat(tex0U).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(1-tex0V).getHalfPrecisionAsShort());
		out.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void norm0_out(ByteBuffer out) {
		out.putFloat((float) normX);
		out.putFloat((float) normY);
		out.putFloat((float) normZ);
		out.putFloat((float) normW);
	}
	private void norm0s_out(ByteBuffer out) {
		out.putShort((short) (normX*vecFactor)); //short4n
		out.putShort((short) (normY*vecFactor));
		out.putShort((short) (normZ*vecFactor));
		out.putShort((short) (normW*vecFactor));
	}
	private void norm0d4n_out(ByteBuffer out) {
		int x = Math.round(normX * 511);
        x = (x > 0) ? x : (x + 1024);
		int y = Math.round(normY * 511);
        y = (y > 0) ? y : (y + 1024);
		int z = Math.round(normZ * 511);
        z = (z > 0) ? z : (z + 1024);
		
        int bits = (1 << 30) | (z << 20) | (y << 10) | x;
		out.putInt(Integer.reverseBytes(bits));
	}
	
	private void tan0_out(ByteBuffer out) {
		out.putFloat((float) tanX);
		out.putFloat((float) tanY);
		out.putFloat((float) tanZ);
		out.putFloat(1.0f);
	}
	private void tan0s_out(ByteBuffer out) {
		out.putShort((short) (tanX*vecFactor)); //short4n
		out.putShort((short) (tanY*vecFactor));
		out.putShort((short) (tanZ*vecFactor));
		out.putShort((short) (tanW*vecFactor));
	}
	private void tan0d4n_out(ByteBuffer out) {
		int x = Math.round(tanX * 511);
        x = (x > 0) ? x : (x + 1024);
		int y = Math.round(tanY * 511);
        y = (y > 0) ? y : (y + 1024);
		int z = Math.round(tanZ * 511);
        z = (z > 0) ? z : (z + 1024);
        
        int bits = (1 << 30) | (z << 20) | (y << 10) | x;
		out.putInt(Integer.reverseBytes(bits));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(colorA, colorB, colorG, colorR, normX, normY, normZ, posX, posY, posZ, tex0U, tex0V);
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
				&& doubleEquals(tex0U, other.tex0U, 1000)
				&& doubleEquals(tex0V, other.tex0V, 1000)
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
				&& doubleEquals(tex0U, other.tex0U, 1000)
				&& doubleEquals(tex0V, other.tex0V, 1000);
	}
	
	public static boolean doubleEquals(double a, double b, int precision) {
		//takes in account floating point errors, we don't need more than millimeter precision
		return (int)(a*precision) == (int)(b*precision);
	}
	
	@Override
	public String toString() {
		return "Vertex [posX=" + posX + ", posY=" + posY + ", posZ=" + posZ + //", posW=" + posW + 
				", texU=" + tex0U
				+ ", texV=" + tex0V + ", colorR=" + colorR + ", colorG=" + colorG + ", colorB=" + colorB + ", colorA="
				+ colorA + ", normX=" + normX + ", normY=" + normY + ", normZ=" + normZ + //", normW=" + normW + 
				", tanX="
				+ tanX + ", tanY=" + tanY + ", tanZ=" + tanZ + ", tanW=" + tanW + "]";
	}
}
