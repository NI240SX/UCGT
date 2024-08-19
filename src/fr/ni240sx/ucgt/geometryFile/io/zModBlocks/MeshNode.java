package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.part.MPointPositionCube;

public class MeshNode extends ZModBlock {

	public Node embeddedNode = new Node(0);
	public int viewStatus=5, //0 disabled 1 displayed 3 focused 5 hidden 7 hidden&focused 13 hidden&selected
			int2=0, int3=3;
	
	public float Xmin, Ymin, Zmin, Wmin=1, Xmax, Ymax, Zmax, Wmax=1;
	
	public int renderTechniqueUID;
	public int meshUID;
	
	public MeshNodeSubData subData = new MeshNodeSubData();
	
	public Part binPart; //only used for saving
	public MeshRenderTechnique renderTechnique;
	public ZMesh mesh;
	
	public MeshNode(int ID) {
		super(ID);
	}

	public MeshNode(Part p) {
		if (p.kit.equals("KIT00") && p.lod.equals("A") && !p.part.contains("_T") 
				&& !p.part.contains("SPOILER_") && !p.part.contains("ROLL_CAGE_") 
				&& !p.part.contains("BRAKE_") && !p.part.contains("BRAKEROTOR") 
				&& !p.part.contains("DRIVER") && !p.part.contains("SEAT_")
				&& !p.part.contains("MUFFLER") && !p.part.contains("EXHAUST"))
			viewStatus = 1;
		else if (p.kit.equals("KIT00") && p.lod.equals("A") && ((p.part.contains("EXHAUST") && p.part.contains("00")) || 
				(p.part.contains("MUFFLER") && p.part.contains("00")))) viewStatus = 1;
		else if (p.name.startsWith("X") && p.name.contains("1A")) viewStatus = 1; //map models
		else if (p.name.contains("CHOP")) viewStatus = 1;
		binPart = p;
		embeddedNode.name = p.name;
		embeddedNode.matrix = new float[][]{	//rotate the mesh in zmod with minimal effort
				{-1.0f,	0,		0,		0},
				{0,		0,		-1.0f,	0},
				{0,		1.0f,	0,		0},
				{0,		0,		0,		1.0f}};
		Xmin = p.header.boundsXmin;
		Xmax = p.header.boundsXmax;
		Ymin = p.header.boundsYmin;
		Ymax = p.header.boundsYmax;
		Zmin = p.header.boundsZmin;
		Zmax = p.header.boundsZmax;
		
	}

	public MeshNode(MPointPositionCube mpc) {
		embeddedNode.name = mpc.mpoints.get(0).uniqueName;
		embeddedNode.matrix = new float[][]{	//rotate the mesh in zmod with minimal effort
				{-1.0f,	0,		0,		0},
				{0,		0,		-1.0f,	0},
				{0,		1.0f,	0,		0},
				{0,		0,		0,		1.0f}};
		Xmin = mpc.x - 0.1f;
		Xmax = mpc.x + 0.1f;
		Ymin = mpc.y - 0.1f;
		Ymax = mpc.y + 0.1f;
		Zmin = mpc.z - 0.1f;
		Zmax = mpc.z + 0.1f;
	}

	@Override
	public void readData(ByteBuffer in) {
//		var blockType = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
			in.getInt(); //DATA
//		var blockUID = 
			in.getInt(); //0
//		var blockVersion = 
			in.getInt(); //0
		var blockLength = in.getInt();
		var blockStart = in.position();
//		embeddedNode = new Node(0);
		embeddedNode.readData(in);
		in.position(blockStart+blockLength);
		
		viewStatus = in.getInt(); //1 displayed 3 focused 13 hidden
		int2 = in.getInt();
		int3 = in.getInt(); //extensions ? 0 on KIT00_DRIVER_B
		
		int boundType = in.getInt();
		if (boundType == 4) {
			Xmin = in.getFloat();
			Ymin = in.getFloat();
			Zmin = in.getFloat();
			Wmin = in.getFloat();

			Xmax = in.getFloat();
			Ymax = in.getFloat();
			Zmax = in.getFloat();
			Wmax = in.getFloat();
		} else System.out.println("Warning : unknown bound type. Won't be able to read part data properly for "+embeddedNode.name);
		
		renderTechniqueUID = in.getInt();
		meshUID = in.getInt();
		

//		var blockType = String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())});
			in.getInt(); //DATA
//		var blockUID = 
			in.getInt(); //0
//		var blockVersion = 
			in.getInt(); //545
		blockLength = in.getInt();
		blockStart = in.position();
		subData = new MeshNodeSubData();
		subData.readData(in);
		in.position(blockStart+blockLength);
		
	}

	@Override
	public String getName() {
		return "scene::CMeshNode";
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
		 * embedded node length : ?
		 * viewstatus, stuff : 12B
		 * box bound : 36B
		 * UIDS : 8B
		 * subdata : 16+4B
		 */
		//get embedded node to byte array
		var embToBytes = embeddedNode.writeData();
		//precompute length
		final var length = 72 + embToBytes.length + 20;
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.put(embToBytes);
		
		block.putInt(viewStatus);
		block.putInt(int2);
		block.putInt(int3);
		
		block.putInt(4); //bound type
		block.putFloat(Xmin);		block.putFloat(Ymin);		block.putFloat(Zmin);		block.putFloat(Wmin);
		block.putFloat(Xmax);		block.putFloat(Ymax);		block.putFloat(Zmax);		block.putFloat(Wmax);
		
		block.putInt(renderTechnique.UID);
		block.putInt(mesh.UID);

		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(0); //UID
		block.putInt(0); //version
		block.putInt(4);
		block.putInt(0);
		
		fos.write(block.array());
	}
	
	public class MeshNodeSubData {

		public ArrayList<Data> datas = new ArrayList<>();

		public void readData(ByteBuffer in) {
			int numData = in.getInt();
			// most parts have this list empty
			for (int i=0; i<numData; i++) datas.add(new Data(in)); //TODO
		}
		
		public class Data {
			String name;
			
			@SuppressWarnings("unused")
			public Data(ByteBuffer in) {
				//idk TODO check later
			}
		}
	}
}
