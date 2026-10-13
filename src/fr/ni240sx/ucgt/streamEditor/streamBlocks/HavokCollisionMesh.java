package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.CARP.UData;

public class HavokCollisionMesh implements UData{ //not only a CARP block
	
	//TODO merge Havok data with Collisions (Bounds) from the collisions editor!
	
	public short instanceID;
	
	public CARP_HavokCollisionShape collisionShape;
	public ArrayList<LocalFixUp> localFixUps;
	public ArrayList<VirtualFixUp> virtualFixUps;
	
//	public int id;
//	public int[] surfaces;
//	public short[] triStuff;
//	public Vertex[] vertices;
//	public Triangle[] triangles;
//	public byte[] triSurfaceID;
//	public Data4[] data4;

//	public hkArray<Integer> surfaces;
//	public hkArray<Byte> data1, materialIndices;
//	public hkArray<Short> triStuff, indices;
//	public hkArray<Vertex> vertices;
//	public hkArray<Data4> data4;
//	
//	public float f1;
//	public float f2;
//	public float f3;
//	public float f4;
//
//	public float[] float4_1;
//	public float[] float4_2;
//	
//	public int surfaces_v1;
	
	public HavokCollisionMesh(ByteBuffer in, int num) {
		
		/*
		 * hkArray = [m_data*, m_size, m_capacityAndFlags] 
		 */
		
		//hkSizeOf(hkNFSPackfileHeader) = 48
		
		instanceID = (short) num;
//		var headerStart = in.position();
		//header (48)

		//TODO merge Havok data with Collisions (Bounds) from the collisions editor!
		
//		in.getInt(); //0x67E0E067	m_magic
//		in.getInt(); //0x20C0C020	m_magic
//		in.getInt(); //0			resolveAddress, should stay zero
//		in.getInt(); //16			m_fileVersion
//		in.getInt(); //0x01000104	m_layoutRules [bytes per pointer, isLittleEndian, reusePaddingOptimization, emptyBaseClassOptimization]
//		in.getInt(); //0			m_contentsSectionOffset
//		in.getInt(); //0x70C4C47A	m_contentsClassNameCRC
//		in.getInt(); //0x00050500	m_contentsVersion
//		in.getInt(); //48			m_absoluteDataStart

		ByteOrder endianness = in.order();

		in.getInt(); //0x67E0E067	m_magic
		in.getInt(); //0x20C0C020	m_magic
		in.getInt(); //0			resolveAddress, should stay zero
		in.getInt(); //16			m_fileVersion
		//if (in.getInt() != 16) System.err.println("WCollision: Havok file is not version 16!");
		//in.getInt(); //0x01000104	m_layoutRules [bytes per pointer, isLittleEndian, reusePaddingOptimization, emptyBaseClassOptimization]
		if (in.get() != 4) System.err.println("WCollision: Havok file is not 4 bytes per pointer!");
		if (in.get() != 1) {
			System.out.println("WCollision: Havok file is big-endian!");
			in.order(ByteOrder.BIG_ENDIAN);
			endianness = ByteOrder.BIG_ENDIAN;
		}
		in.get();
		in.get();
		//in.getInt(); //0			m_contentsSectionOffset
		if (in.getInt() != 0) System.err.println("WCollision: Havok file m_contentsSectionOffset is non-zero!");
		//in.getInt(); //0x554DF6C5	m_contentsClassNameCRC
		if (in.getInt() != 0x70C4C47A) System.err.println("WCollision: Wrong m_contentsClassNameCRC in Havok file!");
		//in.getInt(); //0x00050500	m_contentsVersion
		if (in.getInt() != 0x00050500) System.err.println("WCollision: Wrong m_contentsVersion in Havok file!");
		//in.getInt(); //48			m_absoluteDataStart
		if (in.getInt() != 48) System.err.println("WCollision: Havok file m_absoluteDataStart not equal to 48!");
		

		var m_localFixupsOffset = in.getInt(); //	m_localFixupsOffset
		var m_virtualFixupsOffset = in.getInt(); //	m_virtualFixupsOffset	count2 = count1+112
		var m_endOffset = in.getInt(); //			m_endOffset				count3 = count2+48
		

		ByteBuffer data = in.slice(in.position(), m_endOffset);
		data.order(endianness);


		data.position(m_localFixupsOffset);
		localFixUps = new ArrayList<>();
		while (data.position() < m_virtualFixupsOffset) {
			var f = new LocalFixUp(data);
			if (f.isValid()) localFixUps.add(f);
		}
		data.position(m_virtualFixupsOffset);
		virtualFixUps = new ArrayList<>();
		while (data.position() < m_endOffset) {
			var f = new VirtualFixUp(data);
			if (f.isValid()) virtualFixUps.add(f);
		}
		
		data.position(0);
		collisionShape = new CARP_HavokCollisionShape(data);
		
//		var dataStart = in.position();
//		//SAVING BYTE BUFFER MUST START HERE
//		//(length count3 till EOF)
//		//data block 1
//
//		
////		id = in.getInt(); //not unique		//CLASS CRC32 0x70C4C47A
////		in.getInt(); //xxxxxxxx								---> POINTER6 TO ...				pShape hkpShape
////		surfaces = new hkArray<Integer>(in); //fixup is: arrayPtr - dataStart -> actualArrayPos - dataStart
////		surfaces_v1 = in.getInt(); //1, often 2
//
//		in.getInt(); //alignment
//		in.getInt();
//		
//		shape.surfaces.readArray(() -> in.getInt());
////		surfaces = new int[numSurfaces];
////		for (int i=0; i<numSurfaces; i++) {
////			surfaces[i] = in.getInt();
//////				System.out.println(Hash.getVLT(block1Hashes[i]));
////		}
//		//align 16 byte: skip table end
//		while ((in.position()-dataStart)%16 != 0) {
//			in.getInt();
//		}
//		in.position(in.position()+80);
//		/* skip
//		 * 00000000 										<--- ... TO HERE6	//CLASS CRC32 0xAF2EA797
//		 * 			00000000 00000000 00000000 
//		 * xxxxxxxx 													---> POINTER5 TO ...
//		 * 			00000000 00000000 00000000 
//		 * 00000000 00000000 00000000 00000000 
//		 * 00000000 xxxxxxxx								---> POINTER4 TO ... 
//		 * 						00000000 00000000 
//		 * 00000000 													<--- ... TO HERE5	//CRC32 0x2132DF59
//		 * 			00000000 00000000 00000000
//		 */
//		
//		f1 = in.getFloat(); //neg small
//		f2 = in.getFloat(); //neg small
//		f3 = in.getFloat(); //neg small
//		f4 = in.getFloat(); //pos very large
//		
////			System.out.println("""
////					- CARP hk record -
////					f1 %f
////					f2 %f
////					f3 %f
////					f4 %f""".formatted(block1_f1, block1_f2, block1_f3, block1_f4));
//
//		
//		data1 = new hkArray<Byte>(in);
//		
//		var data_v1 = in.getInt(); //1
//		//what the fuck is this
//		data1.readArray(() -> in.get());
//		while ((in.position()-dataStart)%16 != 0) {
//			in.get();
//		}
//		
//		in.position(in.position()+48);
//		/*skip
//		 * 00000000 										<--- ... TO HERE4	//CLASS CRC32 0x571B8546
//		 * 			00000000 00000000 00000000 
//		 * 00000000 01000000 0C000000 00000000 
//		 * 0000803F 0000803F 0000803F 0000803F
//		 */
//
//		float4_1 = new float[4];
//		float4_1[0] = in.getFloat();
//		float4_1[1] = in.getFloat();
//		float4_1[2] = in.getFloat();
//		float4_1[3] = in.getFloat();
//
//		float4_2 = new float[4];
//		float4_2[0] = in.getFloat();
//		float4_2[1] = in.getFloat();
//		float4_2[2] = in.getFloat();
//		float4_2[3] = in.getFloat();
//
////			System.out.println("""
////					(%f, %f, %f, %f)
////					(%f, %f, %f, %f)""".formatted(
////							block1_float4_1[0], block1_float4_1[1], block1_float4_1[2], block1_float4_1[3],
////							block1_float4_2[0], block1_float4_2[1], block1_float4_2[2], block1_float4_2[3]));
//
//		
//		in.getInt(); //xxxxxxxx					---> POINTER1 TO...
//		in.getInt(); //1
//		in.getInt(); //0
//		in.getInt(); //0
//
//		triStuff = new hkArray<Short>(in);
////		in.getInt(); //0
////		var numTriangles = in.getInt();
////		in.getInt(); //numData2 | 0xC0000000
//		//var data2_v1 = 
//				in.getInt(); //5
//		
//		in.position(in.position()+16);
//		//skip 00010400							<--- ...TO HERE1	(TriangleSubpart ?)
//		// 00000000 
//		//01000100 / 01000200 / 01000500 TODO
//		//00000000
//		in.getInt(); //numTriangles
//		in.getInt(); //0
//		in.getInt(); //16
//		//var numVertices = 
//				in.getInt();
//		
//		in.position(in.position()+144);
//		/*skip
//		 * 00000000 00000000 00000000 00000000 
//		 * 00000000 08000000 01000000 00000000 
//		 * CDCC4C3D 00000000 00000000 00000000 
//		 * xxxxxxxx 							---> POINTER2 TO...		(hkArray of mesh data ???)
//		 * 			01000000 010000C0 00000000 
//		 * 00000000 000000C0 00000000 00000000 
//		 * 00010400 00000000 
//		 * 
//		 * 01000100 / 01000200 / 01000500 same as above
//		 * 
//		 * 00000000 
//		 * numData2 00000000 10000000 numVertices 
//		 * 00000000 00000000 00000000 00000000 
//		 * 00000000 08000000 01000000 00000000
//		 */
//		
//		triStuff.readArray(() -> in.getShort());
//		//byte surface, byte flags ?
//		//or just a short ?
//		
//		//align 16 byte: skip table end
//		while ((in.position()-dataStart)%16 != 0) {
//			in.getShort();
//		}
//		
//		in.position(in.position()+24);
//		/*skip
//		 * xxxxxxxx 							<--- ...TO HERE2 		---> POINTER3 TO...
//		 * 			00000000 00000000 00000000 
//		 * 00000000 00000000 											<--- ... TO HERE3	//CLASS CRC32 0x23418529
//		 * 
//		 * 
//		 * 00000000 numVertices numVertices | 0xC0000000  -> hkArray
//		 * 00000000
//		 */
//		vertices = new hkArray<Vertex>(in);
//		indices = new hkArray<Short>(in); //actually this is triangles ????? what ?????
//		
////		var numData3 = in.getInt(); //numTriangles *3
////		in.getInt();//numData3 | 0xC0000000
//
//		in.position(in.position()+12); //no fixup data if empty
//		/*skip
//		 * 00000000 00000000 000000C0 
//		 * 
//		 * xxxxxxxx numTriangles numTriangles | 0xC0000000 -> hkArray
//		 * 00000000 
//		 * 
//		 */
////		var triangles = new hkArray<Triangle>(in);
//		materialIndices = new hkArray<Byte>(in);
//		data4 = new hkArray<Data4>(in);	
//		var empty5 = new hkArray<Byte>(in); 
//		
////		var numData4 = in.getInt(); //1-5
//		/* skip
//		 * 010000C0 / 020000C0 / 050000C0	-> hkArray data4
//		 * 00000000 00000000 000000C0		-> hkArray empty
//		 */
////		in.position(in.position()+16);
//		
//		vertices.readArray(() -> new Vertex(in));
////		triangles.readArray(() -> new Triangle(in));
//		indices.readArray(() -> in.getShort());
//		while ((in.position()-dataStart)%16 != 0) {
//			in.getShort();
//		}
//		
//		materialIndices.readArray(() -> in.get());
////		triSurfaceID = new byte[numTriangles];
////		for (int i=0; i<numTriangles; i++) triSurfaceID[i] = in.get();
//		//align 16 byte: skip table end
//		while ((in.position()-dataStart)%16 != 0) {
//			in.get();
//		}
//		
//		data4.readArray(() -> new Data4(in));
//		//align 16 byte: skip table end
//		while ((in.position()-dataStart)%16 != 0) {
//			in.getInt();
//		}
//
//		
//		
//		var localFixUpsStart = in.position();
//		//(length count1 since start of block 1)
//		//data block 2 (112)
//		
//		//if this is fixup bullshit im kimming mydelf
//
//		var virtualFixUpsStart = in.position();
//		//(length count2 since start of block 1, 48 till EOF)
//		//data block 3 (48)
	}
	
	public int findLocalFixUpToOffset(int fromOffset) {
		for (var f : localFixUps) {
			if (f.fromOffset == fromOffset) return f.toOffset;
		}
		return -1;
	}

	public int findVirtualFixUpCRC(int offset) {
		for (var f : virtualFixUps) {
			if (f.objectOffset == offset) return f.classCRC;
		}
		return -1;
	}
	
	public class CARP_HavokCollisionShape{
		public int iID; 
		public hkpCollisionMeshShape shape; //shape pointer
		public hkArray<Integer> simSurfaces;
		public eWORLD_SHAPE_TYPE shapeType;
//		LocalFixUp pShapeFixup;

		//VirtualFixUp at class start
		//LocalFixUp to pShape
		
		public CARP_HavokCollisionShape(ByteBuffer in) {
			if (findVirtualFixUpCRC(in.position()) != this.CRC32()) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(in.position())));
			
			iID = in.getInt();
			System.out.println("iID %d".formatted(iID));
			int pShape = findLocalFixUpToOffset(in.position()); in.getInt();
			simSurfaces = new hkArray<>(in, () -> in.getInt());
			shapeType = eWORLD_SHAPE_TYPE.get(in.getInt());
			
			if (pShape == -1) return;

			int backup = in.position();
			in.position(pShape);
			shape = new hkpCollisionMeshShape(in);
			in.position(backup);
		}
		
		public int CRC32() {return 0x70C4C47A;}
	}
	
	public static enum eWORLD_SHAPE_TYPE{
		kWORLD_SHAPE_UNKNOWN(0),
		kWORLD_SHAPE_GROUND(1),
		kWORLD_SHAPE_BARRIER(2);

		int index;
		eWORLD_SHAPE_TYPE(int i) {
			index = i;
		}

		public static eWORLD_SHAPE_TYPE get(int i) {
			for (var v : values()) if (v.index == i) return v;
			return null;
		}
		public int get() {
			return index;
		}
	}
	
	public class hkpCollisionMeshShape {
		public hkpObject1 object1;
		public hkpGoofyExtendedMeshShape object2;
		public hkpCollisionMeshShape(ByteBuffer in) {
			if (findVirtualFixUpCRC(in.position()) != this.CRC32()) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(in.position())));
			in.position(in.position()+16);
			var obj1 = findLocalFixUpToOffset(in.position()); in.getInt();
			in.position(in.position()+32);
			var obj2 = findLocalFixUpToOffset(in.position()); in.getInt();
			in.position(in.position()+8);
			
			int backup = in.position();
			in.position(obj1);
			object1 = new hkpObject1(in);
			in.position(obj2);
			object2 = new hkpGoofyExtendedMeshShape(in);
			in.position(backup);
		}
		
		public int CRC32() {return 0xAF2EA797;}
	}
	
	public class hkpObject1 {
		public float f1, f2, f3, f4;
		public hkArray<Byte> data1;
		public int data1_v1;
		public hkpObject1(ByteBuffer in) {
			if (findVirtualFixUpCRC(in.position()) != this.CRC32()) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(in.position())));
			in.position(in.position()+16);
			f1 = in.getFloat();
			f2 = in.getFloat();
			f3 = in.getFloat();
			f4 = in.getFloat();
			data1 = new hkArray<Byte>(in, () -> in.get());
			data1_v1 = in.getInt();
			int backup = in.position();
			in.position(backup);
		}
		
		public int CRC32() {return 0x2132DF59;}
	}
	
	public class hkpGoofyExtendedMeshShape {
		public float[] scaling, aabbHalfExtents, aabbCenter;
		public hkArray<Short> weldingInfo;
		public hkpWeldingType weldingType;
		public hkpGoofySubpartHeader subpartListed, subpartEmbedded;
		public hkArray<MeshData> meshes;
		
		public hkpGoofyExtendedMeshShape(ByteBuffer in) {
			if (findVirtualFixUpCRC(in.position()) != this.CRC32()) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(in.position())));
			in.position(in.position()+32);
			/*skip
			 * 00000000 										<--- ... TO HERE4	//CLASS CRC32 0x571B8546
			 * 			00000000 00000000 00000000 
			 * 00000000 01000000 0C000000 00000000 
			 */
			scaling = new float[4];
			scaling[0] = in.getFloat();
			scaling[1] = in.getFloat();
			scaling[2] = in.getFloat();
			scaling[3] = in.getFloat();

	
			aabbHalfExtents = new float[4];
			aabbHalfExtents[0] = in.getFloat();
			aabbHalfExtents[1] = in.getFloat();
			aabbHalfExtents[2] = in.getFloat();
			aabbHalfExtents[3] = in.getFloat();
	
			aabbCenter = new float[4];
			aabbCenter[0] = in.getFloat();
			aabbCenter[1] = in.getFloat();
			aabbCenter[2] = in.getFloat();
			aabbCenter[3] = in.getFloat();
			
			var triangleSubparts = findLocalFixUpToOffset(in.position()); in.getInt(); //xxxxxxxx	triangleSubparts					---> POINTER1 TO...
			in.getInt(); //1		numTriangleSubparts
			in.getInt(); //0		shapeSubparts
			in.getInt(); //0		numShapeSubparts
	
			weldingInfo = new hkArray<Short>(in, () -> in.getShort());
			weldingType = hkpWeldingType.get(in.getInt()); //5
			
			//SUBPART 64-BYTE HEADER 
			subpartEmbedded = new hkpGoofySubpartHeader(in);
			
			//CDCC4C3D 00000000 00000000 00000000 float
			in.position(in.position() + 16);
			meshes = new hkArray<>(in, () -> new MeshData(in));
			//new hkArray(in, () -> in.getInt()); //empty array
			//padding

			int backup = in.position();
			in.position(triangleSubparts);
			subpartListed = new hkpGoofySubpartHeader(in); //what the fuck even
			in.position(backup);
		}
		
		public int CRC32() {return 0x571B8546;}
	}
	
	public static enum hkpWeldingType{
		ANTICLOCKWISE(0),
		CLOCKWISE(4),
		TWO_SIDED(5),
		NONE(6);

		int index;
		hkpWeldingType(int i) {
			index = i;
		}

		public static hkpWeldingType get(int i) {
			for (var v : values()) if (v.index == i) return v;
			return null;
		}
		public int get() {
			return index;
		}
	}	
	
	public class hkpGoofySubpartHeader{
		public hkpGoofySubpartHeader(ByteBuffer in) {
//			//skip 00010400							<--- ...TO HERE1	(TriangleSubpart ?)
//			// 00000000 
//			//01000100 / 01000200 / 01000500 TODO
//			//00000000
//			in.getInt(); //numTriangles
//			in.getInt(); //0
//			in.getInt(); //16
//			//var numVertices = in.getInt();

			var type = in.get(); //subPart type (0=triangles, 1=shape)
			var mIndexStridingType = in.get(); //material index striding type (0=invalid, 1=int8, 2=int16)
			var mStriding = in.getShort(); //material striding
			in.getInt(); //materialIndexBase, leave empty
			var mIndexStriding = in.getShort(); //material index striding
			var nMats = in.getShort(); //num materials (actually changes)
			in.getInt(); //material base pointer, leave empty

			var nTris = in.getInt(); //numTriangles
			in.getInt(); //vertex base pointer, leave empty
			var vertStriding = in.getInt(); //vertex striding, 16
			var nVerts = in.getInt(); //numVertices
			
//			System.out.println("""
//					Subpart
//					type = %d (0)
//					mat index striding type = %d (1)
//					mat striding = %d (4)
//					mIndexStriding = %d (1)
//					nMaterials = %d
//					triangles = %d
//					vertices = %d
//					verts striding = %d (16)""".formatted(type, mIndexStridingType, mStriding, mIndexStriding, nMats, nTris, nVerts, vertStriding));
			/* skip
			 * 00000000 00000000 00000000 00000000 
			 * 00000000 08000000 01000000 00000000
			 */
			in.position(in.position()+32);
		}
	}
	
	public class MeshData{
		public hkArray<Vertex> vertices;
		public hkArray<Short> indices;
		public hkArray<Byte> materialIndices;
		public hkArray<Data4> havokMatToSimSurface;
		
		public MeshData(ByteBuffer in) {
			System.out.println("meshdata");
			//read directly at pointer location
			var baseAddress = findLocalFixUpToOffset(in.position()); in.getInt();
			
			int backup = in.position();
			in.position(baseAddress);
			if (findVirtualFixUpCRC(in.position()) != this.CRC32()) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(in.position())));
			
			in.position(in.position()+8);
			vertices = new hkArray<>(in, () -> new Vertex(in));
			indices = new hkArray<>(in, () -> in.getShort());
			new hkArray<>(in, () -> in.getInt()); //empty array
			materialIndices = new hkArray<>(in, () -> in.get());
			havokMatToSimSurface = new hkArray<Data4>(in, () -> new Data4(in));	
			new hkArray<Byte>(in, () -> in.get()); 
			
//			 * 00000000 00000000 											<--- ... TO HERE3	//CLASS CRC32 0x23418529
//			 * 
//			 * 
//			 * 00000000 numVertices numVertices | 0xC0000000  -> hkArray
//			 * 00000000
//			 */
//			vertices = new hkArray<Vertex>(in);
//			indices = new hkArray<Short>(in); //actually this is triangles ????? what ?????
//			
////			var numData3 = in.getInt(); //numTriangles *3
////			in.getInt();//numData3 | 0xC0000000
	//
//			in.position(in.position()+12); //no fixup data if empty
//			/*skip
//			 * 00000000 00000000 000000C0 
//			 * 
//			 * xxxxxxxx numTriangles numTriangles | 0xC0000000 -> hkArray
//			 * 00000000 
//			 * 
//			 */
////			var triangles = new hkArray<Triangle>(in);
//			materialIndices = new hkArray<Byte>(in);
//			data4 = new hkArray<Data4>(in);	
//			var empty5 = new hkArray<Byte>(in); 
//			
////			var numData4 = in.getInt(); //1-5
//			/* skip
//			 * 010000C0 / 020000C0 / 050000C0	-> hkArray data4
//			 * 00000000 00000000 000000C0		-> hkArray empty
//			 */
////			in.position(in.position()+16);
//			
//			vertices.readArray(() -> new Vertex(in));
////			triangles.readArray(() -> new Triangle(in));
//			indices.readArray(() -> in.getShort());
//			while ((in.position()-dataStart)%16 != 0) {
//				in.getShort();
//			}
//			
//			materialIndices.readArray(() -> in.get());
////			triSurfaceID = new byte[numTriangles];
////			for (int i=0; i<numTriangles; i++) triSurfaceID[i] = in.get();
//			//align 16 byte: skip table end
//			while ((in.position()-dataStart)%16 != 0) {
//				in.get();
//			}
//			
//			data4.readArray(() -> new Data4(in));
//			//align 16 byte: skip table end
//			while ((in.position()-dataStart)%16 != 0) {
//				in.getInt();
//			}

			
			
			
			in.position(backup);
		}
		
		public int CRC32() {return 0x23418529;}
	}
	
	
	
	
	
	
	
	public class Vertex{
		public float x,y,z;
		public Vertex(ByteBuffer in) {
			x = in.getFloat();
			y = in.getFloat();
			z = in.getFloat();
			in.getFloat(); //0
		}
	}
	public class Data4{
//		public byte v1, v2, v3, v4;
		public int val;
		public Data4(ByteBuffer in) {
//			v1 = in.getShort(); //vertical or horizontal, two values mostly
//			v2 = in.getShort();
//			v1 = in.get();
//			v2 = in.get();
//			v3 = in.get();
//			v4 = in.get();
			val = in.getInt();
		}
	}
	
	public class hkArray<t> extends ArrayList<t>{
		private static final long serialVersionUID = -8222962201258319415L;
		int count, capacityAndFlags;
		LocalFixUp fixup;

		//LocalFixUp to data
		
		public hkArray(ByteBuffer in, ReaderMethod<t> r){
			super();
			int data = findLocalFixUpToOffset(in.position()); in.getInt();
			count = in.getInt();
			capacityAndFlags = in.getInt();
			
			if (data == -1) return;

			int backup = in.position();
			in.position(data);
			readArray(r);
			in.position(backup);
		}
		
		public void readArray(ReaderMethod<t> r) {
			clear();
			for (int i=0; i<count; i++) {
				add(r.read());
			}
			for (int i=count; i<(capacityAndFlags & 0x00ffffff); i++) {
				r.read();
			}
		}
		
		public void saveObject(ByteBuffer out) {
			fixup = new LocalFixUp();
			fixup.fromOffset = out.position(); //ASSUMING THE BYTE BUFFER STARTS IN THE CORRECT PLACE
			out.putInt(0); //dataPointer
			out.putInt(size()); //count
			out.putInt((size() & 0xffffff) | 0xC0000000); //capacity & flags
		}
		
		public void saveArray(ByteBuffer out, SaverMethod<t> s) {
			fixup.toOffset = out.position();
			for (var d : this) {
				s.save(d);
			}
		}
	}
	
	
	public static interface ReaderMethod<t>{
		public abstract t read();
	}
	public static interface SaverMethod<t>{
		public abstract void save(t dat);
	}
	
	public static class LocalFixUp{
		int fromOffset, toOffset;
		
		public LocalFixUp(ByteBuffer data) {
			fromOffset = data.getInt();
			toOffset = data.getInt();
		}
		public LocalFixUp() {
		}
		public boolean isValid() {
			return fromOffset != -1 && toOffset != -1;
		}
	}
	
	public static class GlobalFixUp{
		int fromOffset, toSection, toOffset;
	}
	
	public static class VirtualFixUp{
		int objectOffset, classCRC;
		public VirtualFixUp(ByteBuffer data) {
			objectOffset = data.getInt();
			classCRC = data.getInt();
		}
		public boolean isValid() {
			return objectOffset != -1 && classCRC != -1;
		}
	}
}