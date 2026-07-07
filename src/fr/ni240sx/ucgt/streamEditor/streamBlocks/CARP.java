package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.X;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Y;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Z;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.shared.Block;

public class CARP{
//	public ArrayList<CARPHeaderEntry> headerEntries;
	public HeaderEntry root;
//	public ArrayList<CARPBlock> blocks;
	
	public CollisionInstances collisionInstances;
	public Name name;
//	public int CARPType;
	/* 42  = WCollision
	 * 122 = WGrid
	 */
	
	public CARP(ByteBuffer in) {
//		if (in.getInt() != 0x43415250) System.err.println("CARP reader: Not a CARP block!");
//		//in.getInt(); //0x43415250 PRAC => CARP
//		CARPType = in.getInt(); //42
//		in.getInt(); //0
//		in.getInt(); //1
		
		root = new HeaderEntry(in);
		if (!root.isType("CARP")) System.err.println("CARP reader: Not a CARP block!");
		root.readRecursive(in);

//		
//		var magic1 = in.getInt();
//		if (CARPType == 42 && magic1 != 0x41727469) System.err.println("CARP reader: Expecting Arti in a collisions block!");
//		if (CARPType == 112 && magic1 != 0x43446174) System.err.println("CARP reader: Expecting CDat in a world grid block!");
//		//in.getInt(); //0x41727469 itrA => Arti
//		in.getInt(); //18
//		var AttriCount = in.getInt();
//		in.getInt(); //1
//		
//		headerEntries = new ArrayList<>();
//		for (int i=0; i<AttriCount; i++) {
//			headerEntries.add(new CARPHeaderEntry(in));
//		}
//		
//		//AAAAAAAA padding %256 relative to CARP block beginning
//		blocks = new ArrayList<>();
//		for (var e : headerEntries) try {
//			blocks.add(e.readEntry(in));
//		} catch(Exception | Error ex) {
//			try {
//				for (var b : blocks) if (b.getClass() == CARPName.class) {
//					System.err.println("Error reading CARP block 0x%08X in chunk %s".formatted(e.type, ((CARPName)b).name));
//				}
//			} catch (Exception ex2) {}
//			
//			ex.printStackTrace();
//		}
//		//one last padding block at the end
//		
//		//WARNING: the ByteBuffer ends up in an undefined place!
//		
//		for (var b : blocks) if (b != null) {
//			if (b.getClass() == CARPName.class) name = (CARPName) b;
//			if (b.getClass() == CARPCollisionInstances.class) {
//				collisionInstances = (CARPCollisionInstances) b;
//				collisionInstances.getInstances(blocks);
//			}
//		}
		if (root.hasEntry("Arti")) {
			if (root.getEntry("Arti").hasEntry("ci")) {
				collisionInstances = (CollisionInstances)(root.getEntry("Arti").getData("ci"));
				collisionInstances.getInstances(root.getEntry("Arti").children);
			}
			name = (Name) root.getEntry("Arti").getData("Name");				
		}
	}
	
	public byte[] save() {
		//TODO
		//prepare header
		//write header (first folders then data)
		//save data
		//fix header
		return null;
	}
	
	public static class HeaderEntry{
		int offset;
		int type; //inverted -> CARP, Arti, Name, ci, hk
		byte typeFlag;
		int dataLength;
		int numEntries;
		int dataOffset;
		
		public ArrayList<HeaderEntry> children;
		public CARPBlock data;
		
		public HeaderEntry(ByteBuffer in) {
			offset = in.position(); //needed later
			type = in.getInt();
			var v1 = in.getInt();
			typeFlag = (byte) (v1 & 0xff); //2=Name, 3=ci/hk
			dataLength = (v1 >>> 8) & 0xffffff;
			numEntries = in.getInt(); //ci contains all Havok blocks
										//but set to 1 for each Havok block
			dataOffset = in.getInt(); //in.position(offset + dataOffset);
		}
		
		public CARPBlock readEntry(ByteBuffer in) {
			in.position(offset + dataOffset);
			/*
			 * CARP types
			 * 
			 * 	CARP	0x43415250
			 * 
			 * 	Arti	0x41727469			//classic collision data
			 * 		Name	0x4E616D65	name
			 * 		ci		0x63690000	CollisionInstances
			 * 		hk		0x686B0000	Havok
			 * 		si		0x73690000	SurfaceTriggers
			 * 
			 * 		CGcn	0x4347636E		//in Y0 and Z0, island data
			 * 
			 * 	CDat	0x43446174			//group tag
			 * 		CGrd	0x43477264		//data tag
			 * 		cl		0x636C0000
			 * 
			 * 	"Map "	0x4D617020
			 * 	
			 * 	RNgp	0x524E6770		RoadNetworkGroup	//root world group
			 * 		RNhd	0x524E6864	Header
			 * 		RNnd	0x524E6E64	Nodes
			 * 		RNpf	0x524E7066	Profiles
			 * 		RNrd	0x524E7264	Roads
			 * 		RNsg	0x524E7367	Segments
			 * 								//Barriers, Shortcuts, RaceSegments, HighwayRamps
			 */
			switch(type) {
			case 0x4E616D65:
				return new Name(in);
			case 0x43477264: //Cgrd
				return new CGrid(in);
			case 0x636C0000:
				return new cl(in);
			case 0x524E6864:
				return new RoadNetworkHeader(in);
			case 0x524E6E64:
				return new RoadNetworkNodes(in, dataLength);
			case 0x524E7066:
				return new RoadNetworkProfiles(in, dataLength);
			case 0x524E7264:
				return new RoadNetworkRoads(in, dataLength);
			case 0x524E7367:
				return new RoadNetworkSegments(in, dataLength);
				
				
			default:
				switch(type & 0xffff0000) {
				//block num in type & 0xffff
				case 0x63690000: //ci
					return new CollisionInstances(in, dataLength); //should also be able to use numEntries
				case 0x686B0000: //hk
					return new HavokCollisionMesh(in, type & 0xffff);
				default:
					System.err.println("CARP reader: Unsupported type 0x%08X/%s"
							.formatted(type,getTypeString()));
					return null;
				}
				
				
				
			}
		}
		
		public String getTypeString() {
			return String.valueOf(new char[]{
					(char) ((type >>> 24)&0xff) , 
					(char) ((type >>> 16)&0xff) , 
					(char) ((type >>> 8)&0xff) , 
					(char) (type & 0xff)});
		}

		public void readRecursive(ByteBuffer in) {
//			System.out.println("Reading %s".formatted(getTypeString()));
			if (dataLength == 0) { //has children
				children = new ArrayList<>();
				int num = numEntries;
				if (isType("CARP")) switch (typeFlag) {
				case 42:
					num = 1;
					break;
				case 122:
					num = 3;
				}
				for (int i = 0; i < num; i++) {
//					System.out.println("Children of %s".formatted(getTypeString()));
					in.position(offset+16*(dataOffset+i));
					var he = new HeaderEntry(in);
					children.add(he);
					he.readRecursive(in);
				}
			} else { //has data
//				System.out.println("Data of %s".formatted(getTypeString()));
				data = readEntry(in);
			}
			in.position(offset+16);
		}
		
		public boolean isType(String s) {
			int i = stringToInt(s);
						
			if (type == i) return true;
			return false;
		}
		
		public HeaderEntry getEntry(CARPBlockID id) {
			return getEntry(id.key);
		}
		public HeaderEntry getEntry(String s) {
			return getEntry(stringToInt(s));
		}
		public HeaderEntry getEntry(int i) {
			if (children == null) return null;
			for (var itm : children) if (itm.type == i) return itm;
			return null;
		}
		
		public CARPBlock getData(CARPBlockID id) {
			return getData(id.key);
		}
 		public CARPBlock getData(String s) {
			return getData(stringToInt(s));
		}
 		public CARPBlock getData(int i) {
			if (children == null) return null;
			for (var itm : children) if (itm.type == i) return itm.data;
			return null;
		}

 		public boolean hasEntry(CARPBlockID id) {
 			return hasEntry(id.key);
		}
 		public boolean hasEntry(String s) {
 			return hasEntry(stringToInt(s));
		}
 		public boolean hasEntry(int i) {
			if (children == null) return false;
			for (var itm : children) if (itm.type == i) return true;
			return false;
		}

		private static int stringToInt(String s) {
			var arr = s.getBytes(StandardCharsets.US_ASCII);
			int i = 0;
			if (s.length() > 0) i |= arr[0] << 24;
			if (s.length() > 1) i |= arr[1] << 16;
			if (s.length() > 2) i |= arr[2] << 8;
			if (s.length() > 3) i |= arr[3] << 0;
			return i;
		}
	}
	
//	public static void main(String[] args) {
//		var s = "Arti";
//		var arr = s.getBytes(StandardCharsets.US_ASCII);
//		var i = arr[0] << 24 | arr[1] << 16 | arr[2] << 8 | arr[3] << 0;
//		
//		System.out.println("0x%08X".formatted(i));
//	}
	
	public static enum CARPBlockID{
		CARP(0x43415250),
		Arti(0x41727469),
		Name(0x4E616D65),
		ci(0x63690000),
		CollisionInstances(0x63690000),
		hk(0x686B0000),
		Havok(0x686B0000),
		CGcn(0x4347636E),
		
		CDat(0x43446174),
		CGrid(0x43477264),
		cl(0x636C0000),
		
		Map(0x4D617020),
		
		RNgp(0x524E6770),
		RoadNetworkGroup(0x524E6770),
		RNhd(0x524E6864),
		RoadNetworkHeader(0x524E6864),
		RNnd(0x524E6E64),
		RoadNetworkNodes(0x524E6E64),
		RNpf(0x524E7066),
		RoadNetworkProfiles(0x524E7066),
		RNrd(0x524E7264),
		RoadNetworkRoads(0x524E7264),
		RNsg(0x524E7367),
		RoadNetworkSegments(0x524E7367),
		;

		int key;
		CARPBlockID(int i) {key = i;}
	}
	
	public static interface CARPBlock{}
	
	public static class Name implements CARPBlock{
		public String name;
		public Name(ByteBuffer in) {
			name = Block.readString(in);
		}
	}
	
	public static class CollisionInstances implements CARPBlock{
		public List<CollisionInstance> instances = new ArrayList<>();
		public CollisionInstances(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				instances.add(new CollisionInstance(in)); //one record per Havok block
			}
		}
//		public void getInstances(ArrayList<CARPBlock> blocks) {
//			for (var b : blocks) if (b.getClass() == CARPHavok.class) {
//				var havok = (CARPHavok)b;
//				instances.get(havok.instanceID).collisionData = havok;
//			}
//		}
		public void getInstances(ArrayList<HeaderEntry> entries) {
			for (var e : entries) if (e.data != null && e.data.getClass() == HavokCollisionMesh.class) {
				var havok = (HavokCollisionMesh)e.data;
				instances.get(havok.instanceID).collisionData = havok;
			}
		}
	}
	public static class CollisionInstance{
		public short chunkID, flags, groupNumber, instanceID;
		public float width, height, length, posX, posY, posZ, radius;
		public HavokCollisionMesh collisionData;
		public CollisionInstance(ByteBuffer in) {
			/*
			0x0		0x10	UMath::Vector4	fInvMatRow0Width	//UMath::Vector4
			0x10	0x2	short	short	fIterStamp	//T_USHORT
			0x12	0x2	short	short	fFlags	//T_USHORT
			0x14	0x4	float	float	fHeight	//T_REAL32
			0x18	0x2	short	short	fGroupNumber	//T_USHORT
			0x1a	0x2	short	short	fRenderInstanceInd	//T_USHORT
			0x1c	0x4	MOD_const__CARP_HavokCollisionShape *	MOD_const__CARP_HavokCollisionShape *	fCollisionArticle	//MOD_const__CARP_HavokCollisionShape*
			0x20	0x4	MOD_const__CARP_HavokCollisionShape *	MOD_const__CARP_HavokCollisionShape *	fCollisionShape	//MOD_const__CARP_HavokCollisionShape*
			0x24	0x10	UMath::Vector4	/UMath::Vector4		fInvMatRow2Length	//UMath::Vector4
			0x34	0x10	UMath::Vector4	/UMath::Vector4		fInvPosRadius	//UMath::Vector4
			 */
			in.getFloat(); //1
			in.getFloat(); //0
			in.getFloat(); //0
			
			width = in.getFloat(); //small positive
			chunkID = in.getShort();
			flags = in.getShort();
			height = in.getFloat(); //small positive
			groupNumber = in.getShort();
			instanceID = in.getShort();
			in.position(in.position()+16); //skip zeroes
			in.getFloat(); //1
			length = in.getFloat(); //small positive
			posX = in.getFloat(); //~chunk pos Y
			posY = in.getFloat(); //small negative, could be -altitude
			posZ = in.getFloat(); //~ -chunk pos X
			radius = in.getFloat(); //medium positive
						
//			System.out.println("""
//					- CARP ci record -
//					Chunk ID %d
//					offset %d
//					v1 %f
//					v2 %f
//					v3 %f
//					v4 %f
//					v5 %f
//					v6 %f
//					v7 %f""".formatted(chunkID, offset, v1,v2,v3,posX,posY,posZ,v7));
		}
	}
	public static class CGrid implements CARPBlock{
		public float[] gridMin = new float[3];
		public float edgeSize, resolution;
		public int numRows, numCols;
		public short[] divisions = new short[3];
		
		public CGrid(ByteBuffer in) {
			gridMin[X] = in.getFloat(); //-5280
			gridMin[Y] = in.getFloat(); //0
			gridMin[Z] = in.getFloat(); //-7120
			in.getFloat(); //1, GridMinZ
			
			edgeSize = in.getFloat(); //80
			resolution = in.getFloat(); //0.0125
			numRows = in.getInt(); //158
			numCols = in.getInt(); //112
			divisions[X] = in.getShort(); //23360
			divisions[Y] = in.getShort(); //7244
			divisions[Z] = in.getShort(); //1
			in.getShort(); //0xCCCC
		}
	}
	
	public static class cl implements CARPBlock{
		public short[] shorts = new short[5];
		public cl(ByteBuffer in) { //even looking up the decompiled version i have no clue what this is for
			shorts[0] = in.getShort();
			shorts[1] = in.getShort();
			shorts[2] = in.getShort();
			shorts[3] = in.getShort();
			shorts[4] = in.getShort();
		}
	}

	public static class RoadNetworkHeader implements CARPBlock{
		public short numProfiles, numNodes, numSegments, numRoads;
		public RoadNetworkHeader(ByteBuffer in) {
			numProfiles = in.getShort(); //num road profiles
			numNodes = in.getShort(); //num road nodes
			numSegments = in.getShort(); //num road segments
			in.getShort(); //0
			numRoads = in.getShort(); //num roads
			in.getShort(); //0
			in.getShort(); //0
		}
	}
	
	public static class RoadNetworkNodes implements CARPBlock{
		public ArrayList<RoadNode> nodes = new ArrayList<>();
		public RoadNetworkNodes(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				nodes.add(new RoadNode(in));
			}
		}
	}
	public static class RoadNode{
		public float[] position = new float[3];
		public short index, profileIndex, numSegments;
		public short[] segmentIndex = new short[7];
		public RoadNode(ByteBuffer in) {
			position[X] = in.getFloat();
			position[Y] = in.getFloat();
			position[Z] = in.getFloat();
			index = in.getShort();
			profileIndex = in.getShort();
			numSegments = in.getShort();
			for (int i=0; i<7; i++) segmentIndex[i] = in.getShort();
		}
	}
	
	public static class RoadNetworkProfiles implements CARPBlock{
		public ArrayList<RoadProfile> profiles = new ArrayList<>();
		public RoadNetworkProfiles(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				profiles.add(new RoadProfile(in));
			}
		}
	}
	public static class RoadProfile{
		public byte numZones, middleZone;
		public int[] lanes = new int[15];
		public RoadProfile(ByteBuffer in) {
			numZones = in.get();
			middleZone = in.get();
			in.getShort(); //AAAA padding
			for (int i=0; i<15; i++) lanes[i] = in.getInt();
		}
		public int getLaneType(int laneIndex) {
			return lanes[laneIndex] & 0xf;
		}
		public float getLaneOffset(int laneIndex) {
			return (lanes[laneIndex] >>> 18) * 0.012208521f; //200/16384
		}
		public float getLaneWidth(int laneIndex) {
			return ((lanes[laneIndex] << 14 | lanes[laneIndex] >>> 18) >>> 18) * 0.012208521f; //100/8192
		}
	}
	
	public static class RoadNetworkRoads implements CARPBlock{
		public ArrayList<Road> roads = new ArrayList<>();
		public RoadNetworkRoads(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				roads.add(new Road(in));
			}
		}
	}
	public static class Road{
		public short scale, length, speechID;
		public byte shortcut, minWidth;
		public Road(ByteBuffer in) {
			scale = in.getShort();
			length = in.getShort();
			shortcut = in.get();
			minWidth = in.get();
			speechID = in.getShort();
		}
	}
	
	public static class RoadNetworkSegments implements CARPBlock{
		public ArrayList<RoadSegment> segments = new ArrayList<>();
		public RoadNetworkSegments(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				segments.add(new RoadSegment(in));
			}
		}
	}
	public static class RoadSegment{
		public short nodeStart, nodeEnd, length, roadID, index, flags, endHandleLength, startHandleLength;
		public byte[] startHandle = new byte[3];
		public byte[] endHandle = new byte[3];
		public RoadSegment(ByteBuffer in) {
			nodeStart = in.getShort();
			nodeEnd = in.getShort();
			length = in.getShort();
			roadID = in.getShort();
			index = in.getShort();
			flags = in.getShort();
			endHandleLength = in.getShort();
			startHandleLength = in.getShort();
			in.get(endHandle);
			in.get(startHandle);
		}
	}

}
