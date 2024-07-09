package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;


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
		polyFormat = 3; //triangles
		short off = 0;
		//vrite material data one after the other
		int matID = 0;
		for (var m : p.mesh.materials.materials) {
//			var m = p.mesh.materials.materials.get(0);
			for (var v : m.verticesBlock.vertices) {
				verts.add(new ZVertex(v));
			}
			//material
			for (var zmat : mats) if (zmat.name.equals(m.uniqueName)) {
				matID = zmat.UID;
//				System.out.println("Using zmaterial "+zmat.name+" for binmat "+m.uniqueName);
				break;
			}
			if (matID == 0) System.out.println("Z3D export error : cannot identify material "+m.uniqueName);
			for (var t : m.triangles) {
				polys.add(new Polygon(matID, (short)(off+t.vert2), (short)(off+t.vert1), (short)(off+t.vert0)));
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
	public void readData(ByteBuffer in) {
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
		final var length = 32 + 56*verts.size() + 30*polys.size();
		
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
		
		public float x=0, y=0, z=0, nx=0, ny=0, nz=0, u=0, v=0;
		public byte r=(byte)255, g=(byte)255, b=(byte)255, a=(byte)255;

		public ZVertex(ByteBuffer in) {
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

				u = in.getFloat();
				v = in.getFloat();
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

				u = in.getFloat();
				v = in.getFloat();
				in.getInt(); //0
				break;
				
			case 51: //normal, 2 UV
				x = in.getFloat();
				y = in.getFloat();
				z = in.getFloat();

				nx = in.getFloat();
				ny = in.getFloat();
				nz = in.getFloat();

				u = in.getFloat();
				v = in.getFloat();
				
				in.getInt(); //second UV channel
				in.getInt();
				in.getInt(); //0
				break;
				
			default:
//				System.out.println("Unknown vertex format : "+format);
				for (var b1 : ZModelerZ3D.blocks.values()) if (b1.getClass() == MeshNode.class) if (((MeshNode)b1).meshUID == UID) {
					System.out.println("Unknown vertex format for mesh "+((MeshNode)b1).embeddedNode.name+" ("+String.format("0x%08X",Integer.reverseBytes(UID))+") : "+vertexFormat+" ("+String.format("0x%08X",Integer.reverseBytes(vertexFormat))+")");
					break;
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
	
				block.putFloat(u);
				block.putFloat(v);
				block.putInt(0);
				return;
			default:
				System.out.println("Wrong vertex format for export !");
			}
		}

		public ZVertex(fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex v) {
			format = 23;
			x = (float) v.posX;
			y = (float) v.posY;
			z = (float) v.posZ;
			nx = (float) v.normX;
			ny = (float) v.normY;
			nz = (float) v.normZ;
			u = (float) v.texU;
			this.v = (float) (1-v.texV);
			r = v.colorR;
			g = v.colorG;
			b = v.colorB;
			a = v.colorA;
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
		public short[] vertIDs;
		
		public Polygon(ByteBuffer in) {
			
			viewStatus = in.getInt();
			int2 = in.getInt();
			int3 = in.getInt();
			
			materialUID = in.getInt();
			
			var numVerts = in.getInt();
			
			vertIDs = new short[numVerts];
			for (int i=0; i<numVerts; i++) {
				vertIDs[i] = in.getShort();
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
			for (var v : vertIDs) block.putShort(v);
			block.putInt(0);
		}

		public Polygon(int matUID, short i, short j, short k) {
			materialUID = matUID;
			vertIDs = new short[]{i, j, k};
		}
		
		public Polygon(int matUID, short i, short j, short k, short l) {
			materialUID = matUID;
			vertIDs = new short[]{i, j, k, l};
		}
	}
}