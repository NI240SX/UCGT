package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.X;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Y;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Z;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.shared.Block;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

public class CARP{
//	public ArrayList<CARPHeaderEntry> headerEntries;
	public UDataHeader root;
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
		
		if (in.getInt(in.position()) == 0x50524143) {
			in.order(ByteOrder.BIG_ENDIAN);
		}
		root = new UDataHeader(in);
		if (!root.isType("CARP")) System.err.println("CARP reader: Not a CARP block!");
		root.readRecursive(in);
		root.readDataRecursive(in, root);
		in.order(ByteOrder.LITTLE_ENDIAN);

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
	
	public static class UDataHeader{
		int fOffset;
		int fTag; //inverted -> CARP, Arti, Name, ci, hk
		byte DataFlags;
		int fSize;
		int fCount;
		int fPointer;
		
		public ArrayList<UDataHeader> children;
		public UData data;
		
		public UDataHeader(ByteBuffer in) {
			fOffset = in.position(); //needed later
			fTag = in.getInt();
//			var v1 = in.getInt();
//			typeFlag = (byte) (v1 & 0xff); //2=Name, 3=ci/hk
//			dataLength = (v1 >>> 8) & 0xffffff;
			
			fSize = in.getInt();
			if (isType("CARP")) {
				//read UGroup flags and size
				if (in.order() == ByteOrder.LITTLE_ENDIAN) {
					DataFlags = (byte) (fSize & 0x1f);
					fSize >>>= 5;
				} else {
					DataFlags = (byte) (fSize >>> 27);
					fSize = fSize & 0x7ffffff;
				}
			} else {
				//read UData flags and size
				if (in.order() == ByteOrder.LITTLE_ENDIAN) {
					DataFlags = (byte) (fSize & 0xff);
					fSize >>>= 8;
				} else {
					DataFlags = (byte) (fSize >>> 24);
					fSize = fSize & 0xffffff;
				}
			}
			
			fCount = in.getInt(); //ci contains all Havok blocks
										//but set to 1 for each Havok block
			fPointer = in.getInt(); //in.position(offset + dataOffset);
			
			System.out.println(getTagString()+" size="+fSize);
		}
		
		public UData readEntry(ByteBuffer in, @SuppressWarnings("unused") UDataHeader CARP) {
			in.position(fOffset + fPointer);
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
			switch(fTag) {
			case 0x4E616D65:
				return new Name(in);
			case 0x43477264: //Cgrd
				return new CGrid(in);
			case 0x636C0000:
				return new cl(in);
			case 0x524E6864:
				return new WRoadNetworkInfo(in);
			case 0x524E6E64:
				return new RoadNetworkNodes(in, fSize);
			case 0x524E7066:
				return new RoadNetworkProfiles(in, fSize);
			case 0x524E7264:
				return new RoadNetworkRoads(in, fSize);
			case 0x524E7367:
				return new RoadNetworkSegments(in, fSize);

            case 0x43415250: //CARP, UGroup so no associated data block
                return null;
				
			default:
				switch(fTag & 0xffff0000) {
				//block num in type & 0xffff
				case 0x63690000: //ci
					return new CollisionInstances(in, fSize); //should also be able to use numEntries
				case 0x686B0000: //hk
					return new HavokCollisionMesh(in, fTag & 0xffff);
				default:
					System.err.println("CARP reader: Unsupported tag 0x%08X/%s"
							.formatted(fTag,getTagString()));
					return null;
				}
				
				
				
			}
		}
		
		public String getTagString() {
			return String.valueOf(new char[]{
					(char) ((fTag >>> 24)&0xff) , 
					(char) ((fTag >>> 16)&0xff) , 
					(char) ((fTag >>> 8)&0xff) , 
					(char) (fTag & 0xff)});
		}

		public void readRecursive(ByteBuffer in) {
//			System.out.println("Reading %s".formatted(getTypeString()));
			if (fSize == 0 || isType("CARP")) { //has children
				int num = fCount;
				if (isType("CARP")) num = fSize;
//				if (isType("CARP")) switch (DataFlags) {
//				case 42: //WCollision PC
//				case 80: //WCollision X360
//					num = 1;
//					break;
//				case 122: //WGrid PC
//				case 88: //X360
//					num = 3;
//				}
				if (num > 0) children = new ArrayList<>();
				for (int i = 0; i < num; i++) {
//					System.out.println("Children of %s".formatted(getTagString()));
					in.position(fOffset+16*(fPointer+i));
					var he = new UDataHeader(in);
					children.add(he);
					he.readRecursive(in);
				}
			} 
//			else { //has data
//				System.out.println("Data of %s".formatted(getTypeString()));
//				data = readEntry(in);
//			}
//			in.position(fOffset+16);
		}
		
        public void readDataRecursive(ByteBuffer in, UDataHeader CARP) {
            if ((DataFlags & (in.order() == ByteOrder.LITTLE_ENDIAN ? DataFlagsEnum.fEmbedded.value : DataFlagsEnum.fEmbeddedBE.value)) != 0) {
//            	System.out.println("Data of %s".formatted(getTagString()));
                data = readEntry(in, CARP);
            }
            if (children != null) for (var child : children) child.readDataRecursive(in, CARP);
        }
		
		public boolean isType(String s) {
			int i = stringToInt(s);
						
			if (fTag == i) return true;
			return false;
		}
		
		public UDataHeader getEntry(UDataID id) {
			return getEntry(id.key);
		}
		public UDataHeader getEntry(String s) {
			return getEntry(stringToInt(s));
		}
		public UDataHeader getEntry(int i) {
			if (children == null) return null;
			for (var itm : children) if (itm.fTag == i) return itm;
			return null;
		}
		
		public UData getData(UDataID id) {
			return getData(id.key);
		}
 		public UData getData(String s) {
			return getData(stringToInt(s));
		}
 		public UData getData(int i) {
			if (children == null) return null;
			for (var itm : children) if (itm.fTag == i) return itm.data;
			return null;
		}

 		public boolean hasEntry(UDataID id) {
 			return hasEntry(id.key);
		}
 		public boolean hasEntry(String s) {
 			return hasEntry(stringToInt(s));
		}
 		public boolean hasEntry(int i) {
			if (children == null) return false;
			for (var itm : children) if (itm.fTag == i) return true;
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
	
    public static enum DataFlagsEnum{
        //UData flags

        fNone(0),
        /** If set, the UData uses 2-byte magic and an index "AA\0\i". */
        fIndexed(1 << 0),
        /** If set, uses a pointer relative to the header entry offset. */
        fEmbedded(1 << 1),
        /** Unknown. */
        fAllocated(1 << 2),
        /** Unknown. */
        fAllocatedBE(0x20),
        /** If set, uses a pointer relative to the header entry offset. */
        fEmbeddedBE(0x40),
        /** If set, the UData uses 2-byte magic and an index "AA\0\i". */
        fIndexedBE(0x80),
        /** Mask for the Section argument. */
        fSection(0x8F),

        //UGroup flags
        /** UGroup flag. */
        fDataSorted(1 << 3), //additional flags for UGroup
        /** UGroup flag. */
        fGroupSorted(1 << 4),
        /** UGroup flag. */
        fGroupSortedBE(0x08),
        /** UGroup flag. */
        fDataSortedBE(0x10);
    	
    	int value;
    	DataFlagsEnum(int i) {this.value = i;}
    	public static DataFlagsEnum get(int i) {
    		for (var v : values()) if (v.value==i) return v;
    		return fNone;
    	}
    }
	
	public static enum UDataID{
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
		UDataID(int i) {key = i;}
	}
	
	public static interface UData{}
	
	public static class Name implements UData{
		public String name;
		public Name(ByteBuffer in) {
			name = Block.readString(in);
		}
	}
	
	public static class CollisionInstances implements UData{
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
		public void getInstances(ArrayList<UDataHeader> entries) {
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
	public static class CGrid implements UData{
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
	
	public static class cl implements UData{
		public short[] shorts = new short[5];
		public cl(ByteBuffer in) { //even looking up the decompiled version i have no clue what this is for
			shorts[0] = in.getShort();
			shorts[1] = in.getShort();
			shorts[2] = in.getShort();
			shorts[3] = in.getShort();
			shorts[4] = in.getShort();
		}
	}

	public static class WRoadNetworkInfo implements UData{
		public short numProfiles, numNodes, numSegments, numIntersections, numRoads, numJunctions;
		public WRoadNetworkInfo(ByteBuffer in) {
			numProfiles = in.getShort(); //num road profiles
			numNodes = in.getShort(); //num road nodes
			numSegments = in.getShort(); //num road segments
			numIntersections = in.getShort(); //0
			numRoads = in.getShort(); //num roads
			numJunctions = in.getShort(); //0
			in.getShort(); //0
			
			System.out.println("Road network info: %d profiles, %d nodes, %d segments, %d intersections, %d roads, %d junctions".formatted(numProfiles, numNodes, numSegments, numIntersections, numRoads, numJunctions));
		}
	}
	
	public static class RoadNetworkNodes implements UData{
		public ArrayList<WRoadNode> nodes = new ArrayList<>();
		public RoadNetworkNodes(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				nodes.add(new WRoadNode(in));
			}
		}
	}
	public static class WRoadNode{
		public float[] position = new float[3];
		public short index, profileIndex, numSegments;
		public short[] segmentIndex = new short[7];
		public WRoadNode(ByteBuffer in) {
			position[X] = in.getFloat();
			position[Y] = in.getFloat();
			position[Z] = in.getFloat();
			index = in.getShort();
			profileIndex = in.getShort();
			numSegments = in.getShort();
			for (int i=0; i<7; i++) segmentIndex[i] = in.getShort();
		}
	}
	
	public static class RoadNetworkProfiles implements UData{
		public ArrayList<WRoadProfile> profiles = new ArrayList<>();
		public RoadNetworkProfiles(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				profiles.add(new WRoadProfile(in));
			}
		}
	}
	public static class WRoadProfile{
		public byte numZones, middleZone;
		public int[] lanes = new int[15];
		public WRoadProfile(ByteBuffer in) {
			numZones = in.get();
			middleZone = in.get();
			in.getShort(); //AAAA padding
			for (int i=0; i<15; i++) lanes[i] = in.getInt();
		}
		public eLaneType getLaneType(int laneIndex) {
			return eLaneType.get(lanes[laneIndex] & 0xf);
		}
		public float getLaneOffset(int laneIndex) {
			return (lanes[laneIndex] >> 18) * 0.012208521f; //200/16384
		}
		public float getLaneWidth(int laneIndex) {
			return ((lanes[laneIndex] << 14 | lanes[laneIndex] >>> 18) >>> 18) * 0.012208521f; //100/8192
		}
	}
	public static enum eLaneType{
		INVALID(-1, new PhongMaterial(Color.MAGENTA)),
		kLaneRacing(0, new PhongMaterial(Color.RED)),
		kLaneTraffic(1, new PhongMaterial(Color.DARKGREY)),
		kLaneDrag(2, new PhongMaterial(Color.ORANGE)),
		kLaneCop(3, new PhongMaterial(Color.BLUE)),
		kLaneCopReckless(4, new PhongMaterial(Color.DARKBLUE)),
		kLaneReset(5, new PhongMaterial(Color.DARKOLIVEGREEN)),
		kLaneStartingGrid(6, new PhongMaterial(Color.INDIANRED)),
		kLaneAny(7, new PhongMaterial(Color.WHITE));
		
		public int id;
		public PhongMaterial mat;
		eLaneType(int i, PhongMaterial m) {
			id = i;
			mat = m;
		}
		
		public static eLaneType get(int i) {
			for (var e : values()) if (e.id == i) return e;
			System.out.println("Invalid lane type "+i);
			return INVALID;
		}
		
		@Override
		public String toString() {
			return name();
		}
	}
	
	public static class RoadNetworkRoads implements UData{
		public ArrayList<WRoad> wRoads = new ArrayList<>();
		public RoadNetworkRoads(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				wRoads.add(new WRoad(in));
			}
		}
	}
	public static class WRoad{
		public short scale, length, speechID;
		public byte shortcut, minWidth;
		public WRoad(ByteBuffer in) {
			scale = in.getShort();
			length = in.getShort();
			shortcut = in.get();
			minWidth = in.get();
			speechID = in.getShort();
		}
	}
	
	public static class RoadNetworkSegments implements UData{
		public ArrayList<WRoadSegment> segments = new ArrayList<>();
		public RoadNetworkSegments(ByteBuffer in, int length) {
			var start = in.position();
			while (in.position() < start + length) {
				segments.add(new WRoadSegment(in));
			}
		}
	}
	public static class WRoadSegment{
		public short nodeStart, nodeEnd, length, roadID, index, flags, endHandleLength, startHandleLength;
		public byte[] startHandle = new byte[3];
		public byte[] endHandle = new byte[3];
		public WRoadSegment(ByteBuffer in) {
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
