package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
import fr.ni240sx.ucgt.geometryFile.part.mesh.VertexFormat;


public class ZMesh extends ZModBlock {

	public int vertexFormat;
	public int polyFormat;
	public ArrayList<ZVertex> verts = new ArrayList<>();
	public ArrayList<Polygon> polys = new ArrayList<>();
	
	public ZMesh(int ID) {
		super(ID);
	}

	public ZMesh(Part p, List<ZMaterial> mats) {
		vertexFormat = 196631; //0x17000300
		/*
		 * needed				global vertex format	per vertex
		 * pos, 1uv				11000000	17			19 (pos, norm, uv0)
		 * pos, 1uv, norm		13000000	19			19
		 * pos, 1uv, color		15000000	21			23 (pos, norm, col, uv0)
		 * pos, 1uv, col, norm	17000000	23			23
		 * 
		 * pos, 2uv				31000000	49			51 (pos, norm, uv0, uv1)
		 * pos, 2uv, norm		33000000	51			51
		 * pos, 2uv, col		35000000	53			55 (pos, norm, col, uv0, uv1)
		 * pos, 2uv, col, norm	37000000	55			55
		 * 
		 * pos, 3uv				71000000	113			83 (pos, norm, uv0 ????????????
		 * pos, 3uv, norm		73000000	115			83 
		 * pos, 3uv, col		75000000	117			87 (pos, norm, col, uv0 ??????????????????? zmod broke as usual
		 * pos, 3uv, col, norm	77000000	119			87
		 * 
		 * pos, 4uv, col, norm	f7000000	247			183 (pos, norm, col, uv0, uv1 ????????? its still broken
		 */
		polyFormat = 3; //triangles
		short off = 0;
		//vrite material data one after the other
		int matID = 0;
		for (var m : p.mesh.materials.materials) {
//			var m = p.mesh.materials.materials.get(0);

			if (m.verticesBlock.vertexFormat.getNumTexChannels() == 2) {
				vertexFormat = 55;
			} else if (m.verticesBlock.vertexFormat.getNumTexChannels() > 2) {
				vertexFormat = 119;
			}
			for (var v : m.verticesBlock.vertices) {
				verts.add(new ZVertex(v, m.verticesBlock.vertexFormat));
			}
			//material
			for (var zmat : mats) if (zmat.name.equals(m.uniqueName)) {
				matID = zmat.UID;
//				System.out.println("Using zmaterial "+zmat.name+" for binmat "+m.uniqueName);
				break;
			}
			if (matID == 0) System.out.println("Z3D export error : cannot identify material "+m.uniqueName);
			for (var t : m.triangles) {
				polys.add(new Polygon(matID, off+t.vert2, off+t.vert1, off+t.vert0));
			}
			off += m.verticesBlock.vertices.size();
//			var t = m.triangles.get(0);
//			off += 3; //this is wrong but whatever
		}
	}

	/**
	 * Generates a cube of size 0.1 around the given point
	 * @param x 
	 * @param y 
	 * @param z the coordinates of the center of the cube
	 */
	public ZMesh(float x, float y, float z) {
		vertexFormat = 3; // only coordinates and normals
		//24 verts in total
		polyFormat = 4; //quads
		//6 polys in total
		final float s = 0.05f; //cube half size 
		verts.add(new ZVertex(x+	s,	y+ -s,	z+ -s,	1,	0,	0));
		verts.add(new ZVertex(x+	s,	y+ s,	z+ -s,	1,	0,	0));
		verts.add(new ZVertex(x+	s,	y+ s,	z+ s,	1,	0,	0));
		verts.add(new ZVertex(x+	s,	y+ -s,	z+ s,	1,	0,	0));

		verts.add(new ZVertex(x+	-s,	y+ -s,	z+ s,	0,	0,	1));
		verts.add(new ZVertex(x+	s,	y+ -s,	z+ s,	0,	0,	1));
		verts.add(new ZVertex(x+	s,	y+ s,	z+ s,	0,	0,	1));
		verts.add(new ZVertex(x+	-s,	y+ s,	z+ s,	0,	0,	1));

		verts.add(new ZVertex(x+	-s,	y+ s,	z+ -s,	0,	1,	0));
		verts.add(new ZVertex(x+	-s,	y+ s,	z+ s,	0,	1,	0));
		verts.add(new ZVertex(x+	s,	y+ s,	z+ s,	0,	1,	0));
		verts.add(new ZVertex(x+	s,	y+ s,	z+ -s,	0,	1,	0));

		verts.add(new ZVertex(x+	-s,	y+ -s,	z+ -s,	-1,	0,	0));
		verts.add(new ZVertex(x+	-s,	y+ -s,	z+ s,	-1,	0,	0));
		verts.add(new ZVertex(x+	-s,	y+ s,	z+ s,	-1,	0,	0));
		verts.add(new ZVertex(x+	-s,	y+ s,	z+ -s,	-1,	0,	0));

		verts.add(new ZVertex(x+	-s,	y+ -s,	z+ -s,	0,	0,	-1));
		verts.add(new ZVertex(x+	-s,	y+ s,	z+ -s,	0,	0,	-1));
		verts.add(new ZVertex(x+	s,	y+ s,	z+ -s,	0,	0,	-1));
		verts.add(new ZVertex(x+	s,	y+ -s,	z+ -s,	0,	0,	-1));

		verts.add(new ZVertex(x+	-s,	y+ -s,	z+ -s,	0,	-1,	0));
		verts.add(new ZVertex(x+	-s,	y+ -s,	z+ s,	0,	-1,	0));
		verts.add(new ZVertex(x+	s,	y+ -s,	z+ s,	0,	-1,	0));
		verts.add(new ZVertex(x+	s,	y+ -s,	z+ -s,	0,	-1,	0));

		polys.add(new Polygon(0, (short)3, (short)2, (short)1, (short)0));
		polys.add(new Polygon(0, (short)7, (short)6, (short)5, (short)4));
		polys.add(new Polygon(0, (short)11, (short)10, (short)9, (short)8));
		polys.add(new Polygon(0, (short)15, (short)14, (short)13, (short)12));
		polys.add(new Polygon(0, (short)19, (short)18, (short)17, (short)16));
		polys.add(new Polygon(0, (short)23, (short)22, (short)21, (short)20));
		
}

	@Override
	public void readData(ByteBuffer in) throws Exception {
		vertexFormat = in.getInt();
		var numVerts = in.getInt();
		polyFormat = in.getInt(); //gotta triangulate quads if there's some, clueful | tri 3, quad 4, both 1
		var numPolys = in.getInt();
		
		for (int i=0;i<numVerts;i++) verts.add(new ZVertex(in));
		for (int i=0;i<numPolys;i++) polys.add(new Polygon(in));
		
	}

	@Override
	public String getName() {
		return "scene::CPolyMesh";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * something : 4B
		 * nameLength+5B
		 * blank : 4B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(18);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * stuff : 16B
		 * each vert : 56B
		 * each poly : 30B
		 */
		//precompute length
		var length = 32 + (24 + polyFormat*2)*polys.size();
		if (vertexFormat == 196631) length += 56*verts.size();
		else if (vertexFormat == 55) length += 64*verts.size();
		else if (vertexFormat == 119) length += 72*verts.size();
		else if (vertexFormat == 3) length += 44*verts.size();
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(515); //version
		block.putInt(length-16);

		block.putInt(vertexFormat);
		block.putInt(verts.size());
		block.putInt(polyFormat);
		block.putInt(polys.size());
		
		for (var v : verts) v.write(block);
		for (var p : polys) p.write(block);
		
		fos.write(block.array());
	}

	public class ZVertex{
		
		public int viewStatus=1, int2=0, int3=3, format=23;
		
		public float x=0, y=0, z=0, 
				nx=1, ny=0, nz=0, 
				u0=0, v0=0, u1=0, v1=0, u2=0, v2=0;
		public byte r=(byte)255, g=(byte)255, b=(byte)255, a=(byte)255;

		public ZVertex(ByteBuffer in) throws Exception {
			viewStatus = in.getInt();
			int2 = in.getInt();
			int3 = in.getInt();
			format = in.getInt();
			
			switch (format) {
			case 3:
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();
				in.getInt();
				break;
				
			case 19: //normal, 1 UV
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();

				u0 = in.getFloat();
				v0 = in.getFloat();
				in.getInt(); //0
				break;
				
			case 23: //normal, diffuse color, 1 UV, S- and T-Normals
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();

				r = in.get();
				g = in.get();
				b = in.get();
				a = in.get();

				u0 = in.getFloat();
				v0 = in.getFloat();
				in.getInt(); //0
				break;
				
			case 55: //normal, diffuse color, 2 UV
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();

				r = in.get();
				g = in.get();
				b = in.get();
				a = in.get();

				u0 = in.getFloat();
				v0 = in.getFloat();
				u1 = in.getFloat();
				v1 = in.getFloat();
				in.getInt(); //0
				break;
				
			case 119: //normal, diffuse color, 2 UV
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();

				r = in.get();
				g = in.get();
				b = in.get();
				a = in.get();

				u0 = in.getFloat();
				v0 = in.getFloat();
				u1 = in.getFloat();
				v1 = in.getFloat();
				u2 = in.getFloat();
				v2 = in.getFloat();
				in.getInt(); //0
				break;
				
			case 51: //normal, 2 UV
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();

				u0 = in.getFloat();
				v0 = in.getFloat();
				
				in.getInt(); //second UV channel
				in.getInt();
				in.getInt(); //0
				break;
				
			default:
//				System.out.println("Unknown vertex format : "+format);
				for (var b1 : ZModelerZ3D.blocks.values()) if (b1.getClass() == MeshNode.class) if (((MeshNode)b1).meshUID == UID) {
					throw new Exception("Unknown vertex format for mesh "+((MeshNode)b1).embeddedNode.name+" ("+String.format("0x%08X",Integer.reverseBytes(UID))+") : "+vertexFormat+" ("+String.format("0x%08X",Integer.reverseBytes(vertexFormat))+")");
				}
			}
		}

		public void write(ByteBuffer block) {
			block.putInt(viewStatus);
			block.putInt(int2);
			block.putInt(int3);
			block.putInt(format);
			switch (format) {
			case 3:
				block.putFloat(x);
				block.putFloat(y);
				block.putFloat(z);
				
				block.putFloat(nx);
				block.putFloat(ny);
				block.putFloat(nz);
				block.putInt(0);
				return;
				
			case 23: //position, normal, vertex color, uv
				block.putFloat(x);
				block.putFloat(y);
				block.putFloat(z);
				
				block.putFloat(nx);
				block.putFloat(ny);
				block.putFloat(nz);
	
				block.put(r);
				block.put(g);
				block.put(b);
				block.put(a);
	
				block.putFloat(u0);
				block.putFloat(v0);
				block.putInt(0);
				return;
				
			case 55: //position, normal, vertex color, uv0, uv1
				block.putFloat(x);
				block.putFloat(y);
				block.putFloat(z);
				
				block.putFloat(nx);
				block.putFloat(ny);
				block.putFloat(nz);
	
				block.put(r);
				block.put(g);
				block.put(b);
				block.put(a);

				block.putFloat(u0);
				block.putFloat(v0);
				block.putFloat(u1);
				block.putFloat(v1);
				block.putInt(0);
				return;
				
			case 119: //position, normal, vertex color, uv0, uv1, uv2
				block.putFloat(x);
				block.putFloat(y);
				block.putFloat(z);
				
				block.putFloat(nx);
				block.putFloat(ny);
				block.putFloat(nz);
	
				block.put(r);
				block.put(g);
				block.put(b);
				block.put(a);

				block.putFloat(u0);
				block.putFloat(v0);
				block.putFloat(u1);
				block.putFloat(v1);
				block.putFloat(u2);
				block.putFloat(v2);
				block.putInt(0);
				return;
				
			default:
				System.out.println("Wrong vertex format for export !");
			}
		}

		public ZVertex(fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex v, VertexFormat vf) {
			/*
			 * needed				global vertex format	per vertex
			 * pos, 1uv				11000000	17			19 (pos, norm, uv0)
			 * pos, 1uv, norm		13000000	19			19
			 * pos, 1uv, color		15000000	21			23 (pos, norm, col, uv0)
			 * pos, 1uv, col, norm	17000000	23			23
			 * 
			 * pos, 2uv				31000000	49			51 (pos, norm, uv0, uv1)
			 * pos, 2uv, norm		33000000	51			51
			 * pos, 2uv, col		35000000	53			55 (pos, norm, col, uv0, uv1)
			 * pos, 2uv, col, norm	37000000	55			55
			 * 
			 * pos, 3uv				71000000	113			83 (pos, norm, uv0 ????????????
			 * pos, 3uv, norm		73000000	115			83 
			 * pos, 3uv, col		75000000	117			87 (pos, norm, col, uv0 ??????????????????? zmod broke as usual
			 * pos, 3uv, col, norm	77000000	119			87
			 * 
			 * pos, 4uv, col, norm	f7000000	247			183 (pos, norm, col, uv0, uv1 ????????? its still broken
			 */
			if (vf.getNumTexChannels()<2) format = 23; //TODO does this have a negative impact on performance
			else if (vf.getNumTexChannels()==2) format = 55;
			else if (vf.getNumTexChannels()>2) format = 119;
			
			x = (float) v.posX;
			y = (float) v.posY;
			z = (float) v.posZ;
			if (vf.hasNormals()) {
				nx = (float) v.normX;
				ny = (float) v.normY;
				nz = (float) v.normZ;
			}
			if (vf.getNumTexChannels()>0) {
				u0 = (float) v.tex0U;
				v0 = (float) (1-v.tex0V);
			}
			if (vf.getNumTexChannels()>1) {
				u1 = (float) v.tex1U;
				v1 = (float) (1-v.tex1V);
			}
			if (vf.getNumTexChannels()>2) {
				u2 = (float) v.tex2U;
				v2 = (float) (1-v.tex2V);
			}
			if (vf.hasColor()) {
				r = v.colorR;
				g = v.colorG;
				b = v.colorB;
				a = v.colorA;
			}
		}
	
		public ZVertex(float x, float y, float z, float nx, float ny, float nz) {
			format = 3;
			this.x = x;
			this.y = y;
			this.z = z;
			this.nx = nx;
			this.ny = ny;
			this.nz = nz;
		}
	}

	public class Polygon{
		
		public int viewStatus=1, int2=0, int3=0;
		
		public int materialUID;
		public int[] vertIDs;
		
		public Polygon(ByteBuffer in) {
			
			viewStatus = in.getInt();
			int2 = in.getInt();
			int3 = in.getInt();
			
			materialUID = in.getInt();
			
			var numVerts = in.getInt();
			
			vertIDs = new int[numVerts];
			for (int i=0; i<numVerts; i++) {
				vertIDs[i] = Short.toUnsignedInt(in.getShort());
			}
			in.getInt(); //0

//			if (numVerts != 3) {
//				System.out.println("Too many vertices per polygon : "+numVerts);
//			}
		}

		public void write(ByteBuffer block) {
			block.putInt(viewStatus);
			block.putInt(int2);
			block.putInt(int3);
			block.putInt(materialUID);
			block.putInt(vertIDs.length);
			for (var v : vertIDs) {
				if (v>65535) System.out.println("[Z3DExporter] Vertex index overflow !");
				block.putShort((short) v);
			}
			block.putInt(0);
		}

		public Polygon(int matUID, int i, int j, int k) {
			materialUID = matUID;
			vertIDs = new int[]{i, j, k};
		}
		
		public Polygon(int matUID, int i, int j, int k, int l) {
			materialUID = matUID;
			vertIDs = new int[]{i, j, k, l};
		}
	}
}