package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import fr.ni240sx.ucgt.geometryFile.part.mesh.X360.HalfPrecisionFloat;

public class Vertex {
	public static int X=0;
	public static int Y=1;
	public static int Z=2;
	public static int W=3;

	public static int U=0;
	public static int V=1;

	public static int R=0;
	public static int G=1;
	public static int B=2;
	public static int A=3;
	
	public float[] pos = null; //pos[x], pos[y] etc	
	public float[][] tex = null; //tex[0][u], tex[0][v]
	public float[] color = null; //color[r], color[g] etc
	public float[] norm = null;
	public float[] tan = null;
	
//	public float pos[x]=0.0f; //position
//	public float pos[y]=0.0f;
//	public float pos[z]=0.0f;
//	public float posW=0x0CCD/posFactor;
	
//	public float tex[0][u]=0.0f; //texcoord
//	public float tex[0][v]=0.0f;

//	public float tex[1][u]=0.0f;
//	public float tex[1][v]=0.0f;
//	public float tex[2][u]=0.0f;
//	public float tex[2][v]=0.0f;
		
//	public byte colorR=(byte) 255; //color
//	public byte colorG=(byte) 255;
//	public byte colorB=(byte) 255;
//	public byte colorA=(byte) 255;

//	public float norm[x]=1.0f; //normals
//	public float norm[y]=0.0f;
//	public float norm[z]=0.0f;
//	public float norm[w]=1.0f;

//	public float tan[x]=0.0f; //tangents, usually no data included
//	public float tan[y]=0.0f;
//	public float tan[z]=0.0f;
//	public float tan[w]=0.0f;

//	public static final float posFactor = 32767.0f/10f;
//	public static final float UVFactor = 1024.0f; //32767.0f/32f;
	private static final float vecFactor = 32767.0f;

//	public static final float short4n_10x_min = Short.MIN_VALUE/posFactor;
//	public static final float short4n_10x_max = Short.MAX_VALUE/posFactor;
//	public static final float short2n_32x_min = Short.MIN_VALUE/UVFactor;
//	public static final float short2n_32x_max = Short.MAX_VALUE/UVFactor;
	
	public Vertex() {
		this(4, true, true, true); //send it
	}
	
	public Vertex(int numTexChannels, boolean doColor, boolean doNormals, boolean doTangents) {
		pos = new float[3];
		if (numTexChannels > 0) tex = new float[numTexChannels][2];
		if (doColor) color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
		if (doNormals) norm = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
		if (doTangents) tan = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
	}

	public Vertex(Vertex v) {
		pos = new float[3];
		pos[X] = v.pos[X];
		pos[Y] = v.pos[Y];
		pos[Z] = v.pos[Z];
//		posW = v.posW;
		if (v.hasTexcoord()) {
			tex = new float[v.numTexChannels()][2];
			for (int i=0; i<v.numTexChannels(); i++) {
				tex[i][U] = v.tex[i][U];
				tex[i][V] = v.tex[i][V];
			}
		}
		if (v.hasColor()) {
			color = new float[4];
			color[R] = v.color[R];
			color[G] = v.color[G];
			color[B] = v.color[B];
			color[A] = v.color[A];
		}
		if (v.hasNormals()) {
			norm = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
			norm[X] = v.norm[X];
			norm[Y] = v.norm[Y];
			norm[Z] = v.norm[Z];
			norm[W] = v.norm[W];
		}
		if (v.hasTangents()) {
			tan = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
			tan[X] = v.tan[X];
			tan[Y] = v.tan[Y];
			tan[Z] = v.tan[Z];
			tan[W] = v.tan[W];
		}
	}
	
	public Vertex(ByteBuffer in, VertexFormat vf) {
		pos = new float[3];
		if (vf.getNumTexChannels() > 0) tex = new float[vf.getNumTexChannels()][2];
		if (vf.hasColor()) color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
		if (vf.hasNormals()) norm = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
		if (vf.hasTangents()) tan = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
		
		int curTexChannel = 0;
		for (var comp : vf.components) {
			switch(comp) {
			case blendindices:
				in.getInt(); //TODO
				break;
			case blendweight:
				col_in(in); //TODO
				break;
				
			case col_argb:
				col_ARGB_in(in);
				break;
			case col_f4:
				col_f4_in(in);
				break;
			case col_rgba:
				col_in(in);
				break;
				
			case norm_cmp3n:
				norm_d4n_in(in); //TODO
				break;
			case norm_d4n:
				norm_d4n_in(in);
				break;
			case norm_f3:
				norm_f3_in(in);
				break;
			case norm_f4:
				norm_f4_in(in);
				break;
			case norm_s:
				norm_s_in(in);
				break;
				
			case pos_f3:
				pos_f3_in(in);
				break;
			case pos_f4:
				pos_f4_in(in);
				break;
			case pos_h4:
				pos_h4_in(in);
				break;
			case pos_s:
				pos_s_in(in, vf.positionFactor);
				break;
				
			case tan_cmp3n:
				tan_d4n_in(in); //TODO
				break;
			case tan_d4n:
				tan_d4n_in(in);
				break;
			case tan_f3:
				tan_f3_in(in);
				break;
			case tan_f4:
				tan_f4_in(in);
				break;
			case tan_s:
				tan_s_in(in);
				break;
				
			case tex_f2:
				tex_in(in, curTexChannel);
				curTexChannel++;
				break;
			case tex_h2:
				tex_h2_in(in, curTexChannel);
				curTexChannel++;
				break;
			case tex_s:
				tex_s_in(in, curTexChannel, vf.texcoordFactor);
				curTexChannel++;
				break;
				
			case unknown_b:
				in.get();
				break;
			case unknown_f:
				in.getFloat();
				break;	
				
			case invalid:
				System.out.println("Error in parsing vertex format "+vf.getName()+"! Invalid component.");
				break;
			}
		}
//		switch(vf) {
//		case Pos0_Col0_Tex0s32:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			break;
//		case Pos0_Col0_Tex0s32_Norm0s:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			norm_s_in(in);
//			break;
//		case Pos0_Col0_Tex0s32_Norm0s_Tan0s:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			norm_s_in(in);
//			tan_s_in(in);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			tex_s_in(in, 1, vf.texcoordFactor);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			tex_s_in(in, 1, vf.texcoordFactor);
//			norm_s_in(in);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			tex_s_in(in, 1, vf.texcoordFactor);
//			norm_s_in(in);
//			tan_s_in(in);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s:
//			pos_f4_in(in);
//			col_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			tex_s_in(in, 1, vf.texcoordFactor);
//			tex_s_in(in, 2, vf.texcoordFactor);
//			norm_s_in(in);
//			tan_s_in(in);
//			break;
//		case Pos0_Col0f4_Tex0:
//			pos_f4_in(in);
//			col_f4_in(in);
//			tex_in(in,0);
//			break;
//		case Pos0_Col0f4_Tex0_Norm0s:
//			pos_f4_in(in);
//			col_f4_in(in);
//			tex_in(in,0);
//			norm_s_in(in);
//			break;
//		case Pos0_Tex0:
//			pos_f4_in(in);
//			tex_in(in,0);
//			break;
//		case Pos0_Tex0_Norm0_Tan0:
//			pos_f4_in(in);
//			tex_in(in,0);
//			norm_f4_in(in);
//			tan_f4_in(in);
//			break;
//		case Pos0_Tex0_Tex1:
//			pos_f4_in(in);
//			tex_in(in,0);
//			tex_in(in,1);
//			break;
//		case Pos0_Tex0s32:
//			pos_f4_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			break;
//		case Pos0_Tex0s32_Norm0s:
//			pos_f4_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			norm_s_in(in);
//			break;
//		case Pos0f3_Tex0s32_Norm0s:
//			pos_f3_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			norm_s_in(in);
//			break;
//		case Pos0f3_Norm0s:
//			pos_f3_in(in);
//			norm_s_in(in);
//			break;
//		case Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s:
//			pos_f4_in(in);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			col_in(in);
//			in.getInt(); //skips blendindices
//			norm_s_in(in);
//			tan_s_in(in);
//			break;
//
//		case Pos0s10_Tex0s32_Col0_Norm0s_Tan0s: //car
//		case Pos0s10_Tex0s1_Col0_Norm0s_Tan0s:
//		case Pos0s10_Tex0s8_Col0_Norm0s_Tan0s:
//			pos_s_in(in, vf.positionFactor);
//			tex_s_in(in, 0, vf.texcoordFactor);
//			col_in(in);
//			norm_s_in(in);
//			tan_s_in(in);
//			break;
//			
//		case Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2: //X360 car
//			pos_h4_in(in);
//			norm_d4n_in(in);
//			tan_d4n_in(in);
//			col_ARGB_in(in);
//			tex_h2_in(in,0);
//			break;
//			
//		case Pos0h4_Norm0cmp3n_Tan0cmp3n_Col0_Tex0h2: //PS3 car
//			pos_h4_in(in);
//			norm_d4n_in(in); //TODO
//			tan_d4n_in(in); //TODO
//			col_ARGB_in(in);
//			tex_h2_in(in,0);
//			break;
//			
////		case Proshit:
////			pos0s_in(in, vf.positionFactor);
////			texs_in(in, 0, vf.texcoordFactor);
////			col0_in(in);
////			norm0s_in(in);
////			tan0s_in(in);
////			break;
//			
//		case Pos0f3_Norm0f3_Col0_Tex0_Tan0f3:
//			//Pos0f3_Norm0f3_Col0_Tex0_Tan0f3
//			pos_f3_in(in);
//			norm_f3_in(in);
//			col_in(in);
//			tex_in(in,0);
//			tan_f3_in(in);
//			break;
//
//		case Pos0f3_Col0_Norm0f4_Tan0f3_Tex0:
//			pos_f3_in(in);
//			col_in(in);
//			norm_f3_in(in);
//			in.getFloat();
//			tan_f3_in(in);
//			tex_in(in,0);
//			break;
//		case Pos0f3_Norm0f3_Col0_Tex0_Tex1:
//			pos_f3_in(in);
//			norm_f3_in(in);
//			col_in(in);
//			tex_in(in,0);
//			tex_in(in,1);
//			break;
//		case Pos0f3_Norm0f3_Col0_Tex0_Tex1_xf4_xb8:
//			pos_f3_in(in);
//			norm_f3_in(in);
//			col_in(in);
//			tex_in(in,0);
//			tex_in(in,1);
//			in.position(in.position()+24);
//			break;
//		case Pos0f3_Col0_Tex0_Tex1_Tan0s_Norm0s:
//			pos_f3_in(in);
//			col_in(in);
//			tex_in(in,0);
//			tex_in(in,1);
//			tan_s_in(in);
//			norm_s_in(in);
//			break;
//		case Pos0f3_Norm0f3_Col0_Tex0:
//			pos_f3_in(in);
//			norm_f3_in(in);
//			col_in(in);
//			tex_in(in,0);
//			break;
//		}	
	}
	
	public void pos_f4_in(ByteBuffer in) {
		pos[X] = in.getFloat();
		pos[Y] = in.getFloat();
		pos[Z] = in.getFloat();
		in.getFloat();
	}
	private void pos_f3_in(ByteBuffer in) {
		pos[X] = in.getFloat();
		pos[Y] = in.getFloat();
		pos[Z] = in.getFloat();
	}
	private void pos_s_in(ByteBuffer in, float factor) {
		pos[X] = (in.getShort())/factor; //short4n_10x
		pos[Y] = (in.getShort())/factor;
		pos[Z] = (in.getShort())/factor;
		in.getShort();
	}
	private void pos_h4_in(ByteBuffer in) {
		in.order(ByteOrder.BIG_ENDIAN);
		pos[X] = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		pos[Y] = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		pos[Z] = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		in.getShort();
		in.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void col_in(ByteBuffer in) {
		color[R] = Byte.toUnsignedInt(in.get())/255.0f;	//color
		color[G] = Byte.toUnsignedInt(in.get())/255.0f;
		color[B] = Byte.toUnsignedInt(in.get())/255.0f;
		color[A] = Byte.toUnsignedInt(in.get())/255.0f;
	}
	private void col_ARGB_in(ByteBuffer in) {
		color[A] = Byte.toUnsignedInt(in.get())/255.0f;
		color[R] = Byte.toUnsignedInt(in.get())/255.0f;	//color
		color[G] = Byte.toUnsignedInt(in.get())/255.0f;
		color[B] = Byte.toUnsignedInt(in.get())/255.0f;
	}
	private void col_f4_in(ByteBuffer in) {
		color[R] = in.getFloat();
		color[G] = in.getFloat();
		color[B] = in.getFloat();
		color[A] = in.getFloat();
	}

	private void tex_in(ByteBuffer in, int i) {
		tex[i][U] = in.getFloat();
		tex[i][V] = 1-in.getFloat();
	}
	private void tex_s_in(ByteBuffer in, int i, float factor) {
		tex[i][U] = (in.getShort())/factor;
		tex[i][V] = 1-(in.getShort())/factor;
	}
	private void tex_h2_in(ByteBuffer in, int i) {
		in.order(ByteOrder.BIG_ENDIAN);
		tex[i][U] = new HalfPrecisionFloat(in.getShort()).getFullFloat();
		tex[i][V] = 1 - new HalfPrecisionFloat(in.getShort()).getFullFloat();
		in.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private void norm_f4_in(ByteBuffer in) {
		norm[X] = in.getFloat();
		norm[Y] = in.getFloat();
		norm[Z] = in.getFloat();
		norm[W] = in.getFloat();
	}
	private void norm_f3_in(ByteBuffer in) {
		norm[X] = in.getFloat();
		norm[Y] = in.getFloat();
		norm[Z] = in.getFloat();
	}
	private void norm_s_in(ByteBuffer in) {
		norm[X] = (in.getShort())/vecFactor; //short4n
		norm[Y] = (in.getShort())/vecFactor;
		norm[Z] = (in.getShort())/vecFactor;
		norm[W] = (in.getShort())/vecFactor;
	}
	private void norm_d4n_in(ByteBuffer in) {
		var bits = Integer.reverseBytes(in.getInt());
		
//		int w = (bits >> 30) & 0b11;
		int z = (bits >> 20) & 0b1111111111; // Bits 30-21
        int y = (bits >> 10) & 0b1111111111; // Bits 20-11
        int x = bits & 0b1111111111;         // Bits 10-1

        norm[x] = x < 512 ? x/511.0f : (x-1024)/511.0f;
        norm[y] = y < 512 ? y/511.0f : (y-1024)/511.0f;
        norm[z] = z < 512 ? z/511.0f : (z-1024)/511.0f;
        
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

	private void tan_f4_in(ByteBuffer in) {
		tan[X] = in.getFloat();
		tan[Y] = in.getFloat();
		tan[Z] = in.getFloat();
		in.getFloat();
	}
	private void tan_f3_in(ByteBuffer in) {
		tan[X] = in.getFloat();
		tan[Y] = in.getFloat();
		tan[Z] = in.getFloat();
	}
	private void tan_s_in(ByteBuffer in) {
		tan[X] = (in.getShort())/vecFactor; //short4n
		tan[Y] = (in.getShort())/vecFactor;
		tan[Z] = (in.getShort())/vecFactor;
		tan[W] = (in.getShort())/vecFactor;
	}
	private void tan_d4n_in(ByteBuffer in) {
		var bits = Integer.reverseBytes(in.getInt());
		
		int w = (bits >> 30) & 0b11;
		int z = (bits >> 20) & 0b1111111111; // Bits 30-21
        int y = (bits >> 10) & 0b1111111111; // Bits 20-11
        int x = bits & 0b1111111111;         // Bits 10-1

        tan[x] = x < 512 ? x/511.0f : (x-1024)/511.0f;
        tan[y] = y < 512 ? y/511.0f : (y-1024)/511.0f;
        tan[z] = z < 512 ? z/511.0f : (z-1024)/511.0f;
        tan[w] = w;
	}
	

	public void save(ByteBuffer out, VertexFormat vf) {
		
		int curTexChannel = 0;
		for (var comp : vf.components) {
			switch(comp) {
			case blendindices:
				out.putInt(0); //TODO
				break;
			case blendweight:
				col_out(out); //TODO
				break;
				
			case col_argb:
				col_ARGB_out(out);
				break;
			case col_f4:
				col_f4_out(out);
				break;
			case col_rgba:
				col_out(out);
				break;
				
			case norm_cmp3n:
				norm0d4n_out(out); //TODO
				break;
			case norm_d4n:
				norm0d4n_out(out);
				break;
			case norm_f3:
				norm0f3_out(out);
				break;
			case norm_f4:
				norm0_out(out);
				break;
			case norm_s:
				norm0s_out(out);
				break;
				
			case pos_f3:
				pos_f3_out(out);
				break;
			case pos_f4:
				pos_out(out);
				break;
			case pos_h4:
				pos_h4_out(out);
				break;
			case pos_s:
				pos_s_out(out, vf.positionFactor);
				break;
				
			case tan_cmp3n:
				tan0d4n_out(out); //TODO
				break;
			case tan_d4n:
				tan0d4n_out(out);
				break;
			case tan_f3:
				tan0f3_out(out);
				break;
			case tan_f4:
				tan0_out(out);
				break;
			case tan_s:
				tan0s_out(out);
				break;
				
			case tex_f2:
				tex_out(out, curTexChannel);
				curTexChannel++;
				break;
			case tex_h2:
				tex_h2_out(out, curTexChannel);
				curTexChannel++;
				break;
			case tex_s:
				tex_s_out(out, curTexChannel, vf.texcoordFactor);
				curTexChannel++;
				break;
				
			case unknown_b:
				out.put((byte) 0);
				break;
			case unknown_f:
				out.putFloat(0);
				break;	
				
			case invalid:
				System.out.println("Error in parsing vertex format "+vf.getName()+"! Invalid component.");
				break;
			}
		}

		
//		switch(vf) {
//		case Pos0_Col0_Tex0s32:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			break;
//		case Pos0_Col0_Tex0s32_Norm0s:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			norm0s_out(out);
//			break;
//		case Pos0_Col0_Tex0s32_Norm0s_Tan0s:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			norm0s_out(out);
//			tan0s_out(out);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			tex_s_out(out, 1, vf.texcoordFactor);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			tex_s_out(out, 1, vf.texcoordFactor);
//			norm0s_out(out);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32_Norm0s_Tan0s:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			tex_s_out(out, 1, vf.texcoordFactor);
//			norm0s_out(out);
//			tan0s_out(out);
//			break;
//		case Pos0_Col0_Tex0s32_Tex1s32_Tex2s32_Norm0s_Tan0s:
//			pos_out(out);
//			col_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			tex_s_out(out, 1, vf.texcoordFactor);
//			tex_s_out(out, 2, vf.texcoordFactor);
//			norm0s_out(out);
//			tan0s_out(out);
//			break;
//		case Pos0_Col0f4_Tex0:
//			pos_out(out);
//			col_f4_out(out);
//			tex_out(out, 0);
//			break;
//		case Pos0_Col0f4_Tex0_Norm0s:
//			pos_out(out);
//			col_f4_out(out);
//			tex_out(out, 0);
//			norm0s_out(out);
//			break;
//		case Pos0_Tex0:
//			pos_out(out);
//			tex_out(out, 0);
//			break;
//		case Pos0_Tex0_Norm0_Tan0:
//			pos_out(out);
//			tex_out(out, 0);
//			norm0_out(out);
//			tan0_out(out);
//			break;
//		case Pos0_Tex0_Tex1:
//			pos_out(out);
//			tex_out(out, 0);
//			tex_out(out, 1);
//			break;
//		case Pos0_Tex0s32:
//			pos_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			break;
//		case Pos0_Tex0s32_Norm0s:
//			pos_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			norm0s_out(out);
//			break;
//		case Pos0f3_Tex0s32_Norm0s:
//			pos_f3_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			norm0s_out(out);
//			break;
//		case Pos0f3_Norm0s:
//			pos_f3_out(out);
//			norm0s_out(out);
//			break;
//		case Pos0_Tex0s32_BlendWeight0_BlendIndices0_Norm0s_Tan0s:
//			pos_out(out);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			col_out(out);
//			out.putInt(0); //skip blendindices
//			norm0s_out(out);
//			tan0s_out(out);
//			break;
//
//		case Pos0s10_Tex0s32_Col0_Norm0s_Tan0s: //car
//		case Pos0s10_Tex0s1_Col0_Norm0s_Tan0s:
//		case Pos0s10_Tex0s8_Col0_Norm0s_Tan0s:
//			pos_s_out(out, vf.positionFactor);
//			tex_s_out(out, 0, vf.texcoordFactor);
//			col_out(out);
//			norm0s_out(out);
//			tan0s_out(out);
//			break;
//
//		case Pos0h4_Norm0d4n_Tan0d4n_Col0_Tex0h2: //X360 car
//			pos_h4_out(out);
//			norm0d4n_out(out);
//			tan0d4n_out(out);
//			col_ARGB_out(out);
//			tex_h2_out(out, 0);
//			break;
//			
//		case Pos0h4_Norm0cmp3n_Tan0cmp3n_Col0_Tex0h2: //PS3 car
//			pos_h4_out(out);
//			norm0d4n_out(out); //TODO
//			tan0d4n_out(out); //TODO
//			col_ARGB_out(out);
//			tex_h2_out(out, 0);
//			break;
//			
////		case Proshit:
////			pos0s_out(out, vf.positionFactor);
////			tex_s_out(out, 0, vf.texcoordFactor);
////			col0_out(out);
////			norm0s_out(out);
////			tan0s_out(out);
////			break;
//			
//		case Pos0f3_Norm0f3_Col0_Tex0_Tan0f3:
//			//Pos0f3_Norm0f3_Col0_Tex0_Tan0f3
//			pos_f3_out(out);
//			norm0f3_out(out);
//			col_out(out);
//			tex_out(out, 0);
//			tan0f3_out(out);
//			break;
//
//		case Pos0f3_Col0_Norm0f4_Tan0f3_Tex0:
//			pos_f3_out(out);
//			col_out(out);
//			norm0f3_out(out);
//			out.putFloat(0.0f);
//			tan0f3_out(out);
//			tex_out(out, 0);
//			break;
//		case Pos0f3_Norm0f3_Col0_Tex0_Tex1:
//			pos_f3_out(out);
//			norm0f3_out(out);
//			col_out(out);
//			tex_out(out, 0);
//			tex_out(out, 1);
//			break;
//		case Pos0f3_Norm0f3_Col0_Tex0_Tex1_xf4_xb8:
//			pos_f3_out(out);
//			norm0f3_out(out);
//			col_out(out);
//			tex_out(out, 0);
//			tex_out(out, 1);
//			out.position(out.position()+24);
//			break;
//		case Pos0f3_Col0_Tex0_Tex1_Tan0s_Norm0s:
//			pos_f3_out(out);
//			col_out(out);
//			tex_out(out, 0);
//			tex_out(out, 1);
//			tan0s_out(out);
//			norm0s_out(out);
//			break;
//		case Pos0f3_Norm0f3_Col0_Tex0:
//			pos_f3_out(out);
//			norm0f3_out(out);
//			col_out(out);
//			tex_out(out, 0);
//			break;
//
//		}	
	}

	private void pos_out(ByteBuffer out) {
		out.putFloat(pos[X]);
		out.putFloat(pos[Y]);
		out.putFloat(pos[Z]);
		out.putFloat(1.0f);
	}
	private void pos_f3_out(ByteBuffer out) {
		out.putFloat(pos[X]);
		out.putFloat(pos[Y]);
		out.putFloat(pos[Z]);
	}
	private void pos_s_out(ByteBuffer out, float factor) {
		out.putShort((short) (pos[X]*factor)); //short4n_10x
		out.putShort((short) (pos[Y]*factor));
		out.putShort((short) (pos[Z]*factor));
		out.putShort((short) (0x0CCD));
	}
	private void pos_h4_out(ByteBuffer out) {
		out.order(ByteOrder.BIG_ENDIAN);
		out.putShort(new HalfPrecisionFloat(pos[X]).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(pos[Y]).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(pos[Z]).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(1.0f).getHalfPrecisionAsShort());
		out.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void col_out(ByteBuffer out) {
		out.put((byte) (color[R]*255));
		out.put((byte) (color[G]*255));
		out.put((byte) (color[B]*255));
		out.put((byte) (color[A]*255));
	}
	private void col_ARGB_out(ByteBuffer out) {
		out.put((byte) (color[A]*255));
		out.put((byte) (color[R]*255));
		out.put((byte) (color[G]*255));
		out.put((byte) (color[B]*255));
	}
	private void col_f4_out(ByteBuffer out) {
		out.putFloat(color[R]);
		out.putFloat(color[G]);
		out.putFloat(color[B]);
		out.putFloat(color[A]);
	}

	private void tex_out(ByteBuffer out, int i) {
		out.putFloat(tex[i][U]);
		out.putFloat(1-tex[i][V]);
	}
	private void tex_s_out(ByteBuffer out, int i, float factor) {
		out.putShort((short) (tex[i][U]*factor));	//short2n_32x
		out.putShort((short) ((1-tex[i][V])*factor));
	}
	private void tex_h2_out(ByteBuffer out, int i) {
		out.order(ByteOrder.BIG_ENDIAN);
		out.putShort(new HalfPrecisionFloat(tex[i][U]).getHalfPrecisionAsShort());
		out.putShort(new HalfPrecisionFloat(1-tex[i][V]).getHalfPrecisionAsShort());
		out.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void norm0_out(ByteBuffer out) {
		out.putFloat(norm[X]);
		out.putFloat(norm[Y]);
		out.putFloat(norm[Z]);
		out.putFloat(norm[W]);
	}
	private void norm0f3_out(ByteBuffer out) {
		out.putFloat(norm[X]);
		out.putFloat(norm[Y]);
		out.putFloat(norm[Z]);
	}
	private void norm0s_out(ByteBuffer out) {
		out.putShort((short) (norm[X]*vecFactor)); //short4n
		out.putShort((short) (norm[Y]*vecFactor));
		out.putShort((short) (norm[Z]*vecFactor));
		out.putShort((short) (norm[W]*vecFactor));
	}
	private void norm0d4n_out(ByteBuffer out) {
		int x = Math.round(norm[X] * 511);
        x = (x > 0) ? x : (x + 1024);
		int y = Math.round(norm[Y] * 511);
        y = (y > 0) ? y : (y + 1024);
		int z = Math.round(norm[Z] * 511);
        z = (z > 0) ? z : (z + 1024);
		
        int bits = (1 << 30) | (z << 20) | (y << 10) | x;
		out.putInt(Integer.reverseBytes(bits));
	}

	private void tan0_out(ByteBuffer out) {
		out.putFloat(tan[X]);
		out.putFloat(tan[Y]);
		out.putFloat(tan[Z]);
		out.putFloat(1.0f);
	}
	private void tan0f3_out(ByteBuffer out) {
		out.putFloat(tan[X]);
		out.putFloat(tan[Y]);
		out.putFloat(tan[Z]);
	}
	private void tan0s_out(ByteBuffer out) {
		out.putShort((short) (tan[X]*vecFactor)); //short4n
		out.putShort((short) (tan[Y]*vecFactor));
		out.putShort((short) (tan[Z]*vecFactor));
		out.putShort((short) (tan[W]*vecFactor));
	}
	private void tan0d4n_out(ByteBuffer out) {
		int x = Math.round(tan[X] * 511);
        x = (x > 0) ? x : (x + 1024);
		int y = Math.round(tan[Y] * 511);
        y = (y > 0) ? y : (y + 1024);
		int z = Math.round(tan[Z] * 511);
        z = (z > 0) ? z : (z + 1024);
        
        int bits = (1 << 30) | (z << 20) | (y << 10) | x;
		out.putInt(Integer.reverseBytes(bits));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(color[A], color[B], color[G], color[R], norm[X], norm[Y], norm[Z], pos[X], pos[Y], pos[Z], tex[0][U], tex[0][V]);
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
		return 
//				&& Double.doubleToLongBits(norm[w]) == Double.doubleToLongBits(other.norm[w])
				doubleEquals(pos[X], other.pos[X], 1000)
				&& doubleEquals(pos[Y], other.pos[Y], 1000)
				&& doubleEquals(pos[Z], other.pos[Z], 1000)
				
				&& (!hasNormals() || !other.hasNormals() || 
						(  doubleEquals(norm[X], other.norm[X], 1000)
						&& doubleEquals(norm[Y], other.norm[Y], 1000)
						&& doubleEquals(norm[Z], other.norm[Z], 1000)) )
				
				&& (!hasTexcoord() || !other.hasTexcoord() || 
						(  doubleEquals(tex[0][U], other.tex[0][U], 1000)
								&& doubleEquals(tex[0][V], other.tex[0][V], 1000)) )
				
				&& (!hasColor() || !other.hasColor() || 
						(  doubleEquals(color[R], other.color[R], 1000)
								&& doubleEquals(color[G], other.color[G], 1000)
								&& doubleEquals(color[B], other.color[B], 1000)
								&& doubleEquals(color[A], other.color[A], 1000)) )

//				&& Double.doubleToLongBits(posW) == Double.doubleToLongBits(other.posW)
				;
	}
	
	public boolean hasColor() {
		return color != null;
	}
	public boolean hasNormals() {
		return norm != null;
	}
	public boolean hasTangents() {
		return tan != null;
	}
	public boolean hasTexcoord() {
		return tex != null;
	}
	public int numTexChannels() {
		return tex.length/2;
	}

	public boolean positionEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		return doubleEquals(pos[X], other.pos[X], 1000)
				&& doubleEquals(pos[Y], other.pos[Y], 1000)
				&& doubleEquals(pos[Z], other.pos[Z], 1000)
				&& (tex == null || (doubleEquals(tex[0][U], other.tex[0][U], 1000)
						&& doubleEquals(tex[0][V], other.tex[0][V], 1000)));
	}
	public boolean positionNormalEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		return doubleEquals(pos[X], other.pos[X], 1000)
				&& doubleEquals(pos[Y], other.pos[Y], 1000)
				&& doubleEquals(pos[Z], other.pos[Z], 1000)
				&& (tex == null || (
						doubleEquals(tex[0][U], other.tex[0][U], 1000)
						&& doubleEquals(tex[0][V], other.tex[0][V], 1000)
						))
				&& (norm == null || (
						doubleEquals(norm[X], other.norm[X], 1000)
						&& doubleEquals(norm[Y], other.norm[Y], 1000)
						&& doubleEquals(norm[Z], other.norm[Z], 1000)
						))
				;
	}
	
	public static boolean doubleEquals(double d1, double d2, int precision) {
		//takes in account floating point errors, we don't need more than millimeter precision
		return (int)(d1*precision) == (int)(d2*precision);
	}
	
//	@Override
//	public String toString() {
//		return "Vertex [pos[x]=" + pos[X] + ", pos[y]=" + pos[Y] + ", pos[z]=" + pos[Z] + //", posW=" + posW + 
//				", texU=" + tex[0][U]
//				+ ", texV=" + tex[0][V] + ", colorR=" + colorR + ", colorG=" + colorG + ", colorB=" + colorB + ", colorA="
//				+ colorA + ", norm[x]=" + norm[X] + ", norm[y]=" + norm[Y] + ", norm[z]=" + norm[Z] + //", norm[w]=" + norm[w] + 
//				", tan[x]="
//				+ tan[X] + ", tan[y]=" + tan[Y] + ", tan[z]=" + tan[Z] + ", tan[w]=" + tan[W] + "]";
//	}
}
