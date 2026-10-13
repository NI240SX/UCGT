package fr.ni240sx.ucgt.collisionsEditor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fr.ni240sx.ucgt.collisionsEditor.BoundEnums.*;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Hash;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
//import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;

public class Collisions implements Block{
	
	public static double floatingPointError = 0.1;

	public String carname = "UNKNOWN";

	//public boolean isResolved = false;
	
	// bin block ID for collisions 01b90300

	//proper bounds sorting
	//public CollisionBound mainBound = null;
	
	public scgHeader scg;
	
	//public float X = 0;
	//public float Y = 0;
	//public float Z = 0;
	//public float W = 0;

//	public ArrayList<LocalFixUp> LocalFixUps = new ArrayList<>();
//	public ArrayList<VirtualFixUp> VirtualFixUps = new ArrayList<>();
	
//	public static ArrayList<Hash> commonCollisionHashes = Hash.loadHashes(new File("data/collisionshashes"));

	@Override
	public BlockType getBlockID() {return BlockType.CarCollisions;}
	
	public Collisions() {
		scg = new scgHeader();
	}
	
	public static Collisions readSerialized(byte[] arr) {
		var cols = new Collisions();
		
		int NumberOfBounds = 0;
		int NumberOfBoxShapes = 0;
		int NumberOfConvexTransformShapes = 0;
		int NumberOfConvexTranslateShapes = 0;
		int NumberOfConvexVerticesShapes = 0;
		int NumberOfSphereShapes = 0;
		int NumberOfLocalFixUps = 0;
		int NumberOfVirtualFixUps = 0;
		
		ArrayList<hkpBoxShape> boxShapes = new ArrayList<>();
		ArrayList<hkpConvexTransformShape> convexTransformShapes = new ArrayList<>();
		ArrayList<hkpConvexTranslateShape> convexTranslateShapes = new ArrayList<>();
		ArrayList<hkpConvexVerticesShape> convexVerticesShapes = new ArrayList<>();
		ArrayList<hkpSphereShape> sphereShapes = new ArrayList<>();

		
		ByteBuffer bb = ByteBuffer.wrap(arr);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(36);
		cols.carname = Hash.addVltHash(Hash.addBinHash(Block.readString(bb)));
		NumberOfBounds = bb.getInt();
		NumberOfBoxShapes = bb.getInt();
		NumberOfConvexTransformShapes = bb.getInt();
		NumberOfConvexTranslateShapes = bb.getInt();
		NumberOfConvexVerticesShapes = bb.getInt();
		NumberOfSphereShapes = bb.getInt();
		NumberOfLocalFixUps = bb.getInt();
		NumberOfVirtualFixUps = bb.getInt();

		//make empty fixup lists for read
		var LocalFixUps = new ArrayList<LocalFixUp>();
		var VirtualFixUps = new ArrayList<VirtualFixUp>();
		
		var renderNodes = new hkArray<scgRenderNode>(); 
		renderNodes.add(new scgRenderNode());
		
		cols.scg = cols.new scgHeader(cols.carname);
		cols.scg.setRoot(cols.new scgBounds(bb, null, LocalFixUps, VirtualFixUps, renderNodes));
		
		while (cols.scg.root.getChildrenRecursively().size() < NumberOfBounds) cols.scg.root.childBounds.add(cols.new scgBounds(bb, cols.scg.root, LocalFixUps, VirtualFixUps, renderNodes));
		
		for(int i =0; i<NumberOfBoxShapes; i++) {
			boxShapes.add(cols.new hkpBoxShape(bb));
		}
		for(int i =0; i<NumberOfConvexTransformShapes; i++) {
			convexTransformShapes.add(cols.new hkpConvexTransformShape(bb, LocalFixUps, VirtualFixUps));
		}
		for(int i =0; i<NumberOfConvexTranslateShapes; i++) {
			convexTranslateShapes.add(cols.new hkpConvexTranslateShape(bb, LocalFixUps, VirtualFixUps));
		}
		for(int i =0; i<NumberOfConvexVerticesShapes; i++) {
			convexVerticesShapes.add(cols.new hkpConvexVerticesShape(bb, LocalFixUps));
		}
		for(int i =0; i<NumberOfSphereShapes; i++) {
			sphereShapes.add(cols.new hkpSphereShape(bb));
		}
//		for(int i =0; i<NumberOfLocalFixUps; i++) {
//			cols.LocalFixUps.add(new LocalFixUp(bb));
//		}
//		for(int i =0; i<NumberOfVirtualFixUps; i++) {
//			cols.VirtualFixUps.add(new VirtualFixUp(bb));
//		}
		//skip FixUps
		bb.position(bb.position()+NumberOfLocalFixUps*8+NumberOfVirtualFixUps*8);
		
		cols.scg.isResolved = bb.get()==1;
		renderNodes.get(0).X = bb.getFloat();
		renderNodes.get(0).Y = bb.getFloat();
		renderNodes.get(0).Z = bb.getFloat();
		renderNodes.get(0).W = bb.getFloat();

		//boxshapes + convexverticeshapes + sphereshapes = childbounds
		//a child bound is either convex vertice or box (or sphere/no geometry but rare)
		//now gotta associate shit with each other
		
		int ibox=0,imesh=0, isphere=0;
		for (scgBounds b : cols.scg.root.getChildrenRecursively()) {
			switch (b.Shape) {
			case KSHAPE_BOX:
				b.collisionShape = boxShapes.get(ibox);
//				boxShapes.get(ibox).bound = b;
				ibox++;
				break;
			case KSHAPE_CYLINDER:
				break;
			case KSHAPE_INVALID:
				break;
			case KSHAPE_MESH:
				b.collisionShape = convexVerticesShapes.get(imesh);
//				convexVerticesShapes.get(imesh).bound = b;
				convexVerticesShapes.get(imesh).PlaneEquations.forEach(p -> {
					p.colorR = randomizeColor(b.colorR, 0.4);
					p.colorG = randomizeColor(b.colorG, 0.4);
					p.colorB = randomizeColor(b.colorB, 0.4);
				});
				imesh++;
				break;
			case KSHAPE_SPHERE:
				b.collisionShape = sphereShapes.get(isphere);
//				sphereShapes.get(isphere).bound = b;
				isphere++;
				break;
			case KSHAPE_TYPE_COUNT:
				break;
			}
		
			//transforms
			//transforms + translates = boxshapes
			for(hkpConvexTranslateShape t : convexTranslateShapes) {
				if (boundCenterCloseEnough(b.PositionX, t.TranslationX)
						&& boundCenterCloseEnough(b.PositionY, t.TranslationY)
						&& boundCenterCloseEnough(b.PositionZ, t.TranslationZ)) {
					t.childShape = b.collisionShape;
					b.collisionShape = t;
				}
			}
			for(hkpConvexTransformShape t : convexTransformShapes) {
				if (boundCenterCloseEnough(b.PositionX, t.TranslationX)
						&& boundCenterCloseEnough(b.PositionY, t.TranslationY)
						&& boundCenterCloseEnough(b.PositionZ, t.TranslationZ)
						&& b.OrientationX != 0) {
					t.childShape = b.collisionShape;
					b.collisionShape = t;
				}
			}
			
			
		}
		
		return cols;
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {

		var LocalFixUps = new ArrayList<LocalFixUp>();
		var VirtualFixUps = new ArrayList<VirtualFixUp>();

		var header = ByteBuffer.wrap(new byte[8 + Block.findAlignment(currentPosition+8, 16) + 48]);
		//bChunk header
		header.order(ByteOrder.LITTLE_ENDIAN);
		header.putInt(BlockType.CarCollisions.getKey());
		header.putInt(-1); //length for later
		Block.makeAlignment(header, Block.findAlignment(currentPosition+8, 16), (byte)0x11);
		
		//Havok packfile header
		header.putInt(0x67E0E067);	//m_magic
		header.putInt(0x20C0C020);	//m_magic
		header.putInt(0);			//resolveAddress
		header.putInt(16);			//m_fileVersion
		header.putInt(0x01000104);	//m_layoutRules
		header.putInt(0);			//m_contentsSectionOffset
		header.putInt(scg.CRC32());	//m_contentsClassNameCRC
		header.putInt(0x00050500);	//m_contentsVersion
		header.putInt(48);			//m_absoluteDataStart
		
		var contents = new ByteArrayOutputStream();
		scg.save(contents, LocalFixUps, VirtualFixUps);
		var contentsArr = contents.toByteArray();
		contents.close();
		contents = null;
		
		header.putInt(contentsArr.length);	//m_localFixupsOffset

		var localFixUps = ByteBuffer.allocate(8*LocalFixUps.size() + (LocalFixUps.size()%2 == 0 ? 0 : 8));
		localFixUps.order(ByteOrder.LITTLE_ENDIAN);
		for (var f : LocalFixUps) {
			localFixUps.put(f.save());
		}
		while (localFixUps.hasRemaining()) localFixUps.putInt(0xffffffff);
		header.putInt(contentsArr.length + localFixUps.capacity());	//m_virtualFixupsOffset

		var virtualFixUps = ByteBuffer.allocate(8*VirtualFixUps.size() + (VirtualFixUps.size()%2 == 0 ? 0 : 8));
		virtualFixUps.order(ByteOrder.LITTLE_ENDIAN);
		for (var f : VirtualFixUps) virtualFixUps.put(f.save());
		while (virtualFixUps.hasRemaining()) virtualFixUps.putInt(0xffffffff);
		header.putInt(contentsArr.length + localFixUps.capacity() + virtualFixUps.capacity());	//m_endOffset
		header.putInt(4, contentsArr.length + localFixUps.capacity() + virtualFixUps.capacity() + header.capacity() - 8);
		
		var out = new ByteArrayOutputStream();
		out.write(header.array());
		out.write(contentsArr);
		out.write(localFixUps.array());
		out.write(virtualFixUps.array());
		
		return out.toByteArray();
	}
	
	/**
	 * DO NOT USE UNTIL ITS FIXED
	 * @throws IOException 
	 */
//	public byte[] saveSerialized() throws IOException {
//
//		ArrayList<hkpBoxShape> boxShapes = new ArrayList<>();
//		ArrayList<hkpConvexVerticesShape> convexVerticesShapes = new ArrayList<>();
//		ArrayList<CollisionSphereShape> sphereShapes = new ArrayList<>();
//		
//		ArrayList<hkpConvexTransformShape> convexTransformShapes = new ArrayList<>();
//		ArrayList<hkpConvexTranslateShape> convexTranslateShapes = new ArrayList<>();
//		
//		for (var b : mainBound.getChildrenRecursively()) {
//			// assuming the enum is kept updated in case the shape changes
//			switch (b.Shape) {
//			case KSHAPE_BOX:
//				boxShapes.add((hkpBoxShape) b.collisionShape);
//				break;
//			case KSHAPE_CYLINDER:
//				break;
//			case KSHAPE_INVALID:
//				break;
//			case KSHAPE_MESH:
//				convexVerticesShapes.add((hkpConvexVerticesShape) b.collisionShape);
//				break;
//			case KSHAPE_SPHERE:
//				sphereShapes.add((CollisionSphereShape) b.collisionShape);
//				break;
//			case KSHAPE_TYPE_COUNT:
//				break;
//			}
//			if (b.shapeTransform != null) convexTransformShapes.add(b.shapeTransform);
//			if (b.shapeTranslate != null) convexTranslateShapes.add(b.shapeTranslate);
//		}
//		
//		ByteBuffer header = ByteBuffer.wrap(new byte[36 + carname.length()+1 + 32]);
//		header.order(ByteOrder.LITTLE_ENDIAN);
//
//		header.putInt(1114656103); //blockID - binary serialized
//		header.putInt(-1); 
//		header.putInt(6);
//		header.putInt(771611838); // sub block (probably collisions)
//		header.putInt(-1);
//		header.putInt(1465336146);
//		header.putInt(4097);
//		header.putInt(-1);
//		header.putInt(-1);
//		//byte 36
//		writeString(carname, header);
//		header.putInt(mainBound.getChildrenRecursively().size()); //NumberOfBounds
//		header.putInt(boxShapes.size());
//		header.putInt(convexTransformShapes.size());
//		header.putInt(convexTranslateShapes.size());
//		header.putInt(convexVerticesShapes.size());
//		header.putInt(sphereShapes.size());
//		header.putInt(LocalFixUps.size());
//		header.putInt(VirtualFixUps.size());
//
//		var data = new ByteArrayOutputStream();
//		data.write(mainBound.saveHierarchy());
//		for (var b : boxShapes) data.write(b.save());
//		for (var b : convexTransformShapes) data.write(b.save());
//		for (var b : convexTranslateShapes) data.write(b.save());
//		for (var b : convexVerticesShapes) data.write(b.save());
//		for (var b : sphereShapes) data.write(b.save());
//		for (var d : LocalFixUps) data.write(d.save());
//		for (var d : VirtualFixUps) data.write(d.save());
//			
//		data.write(this.isResolved ? 1 : 0);
//		
//		var bb = ByteBuffer.wrap(new byte[16]);
//		bb.order(ByteOrder.LITTLE_ENDIAN);
//		bb.putFloat(X);
//		bb.putFloat(Y);
//		bb.putFloat(Z);
//		bb.putFloat(W);
//		
//		data.write(bb.array());
//		bb = null;
//		
//		var dataArray = data.toByteArray();
//		data = null;
//		var totalFileSize = header.capacity() + dataArray.length;
//
//		header.putInt(4, totalFileSize-8);
//		header.putInt(16, totalFileSize-20);
//		header.putInt(28, totalFileSize-36);
//		header.putInt(32, totalFileSize-20);
//		
////		var fos = new FileOutputStream(f);
////		fos.write(header.array());
////		fos.write(dataArray);
////		fos.close();
//		
//		bb = ByteBuffer.allocate(totalFileSize);
//		bb.put(header.array());
//		bb.put(dataArray);
//		return bb.array();
//		return null;
//	}

	public static Collisions read(byte[] arr) {
		var in = ByteBuffer.wrap(arr);
		in.order(ByteOrder.LITTLE_ENDIAN);
		return new Collisions(in);
	}
	public Collisions(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {} //always 2x -> align 16?
		in.position(in.position()-4);

		
		in.getInt(); //0x67E0E067	m_magic
		in.getInt(); //0x20C0C020	m_magic
		in.getInt(); //0			resolveAddress, should stay zero
		in.getInt(); //16			m_fileVersion
		//if (in.getInt() != 16) System.err.println("Bounds: Havok file is not version 16!");
		//in.getInt(); //0x01000104	m_layoutRules [bytes per pointer, isLittleEndian, reusePaddingOptimization, emptyBaseClassOptimization]
		if (in.get() != 4) System.err.println("Bounds: Havok file is not 4 bytes per pointer!");
		ByteOrder endianness = ByteOrder.LITTLE_ENDIAN;
		if (in.get() != 1) {
//			System.out.println("Bounds: Havok file is big-endian!");
			in.order(ByteOrder.BIG_ENDIAN);
			endianness = ByteOrder.BIG_ENDIAN;
		}
		in.get();
		in.get();
		//in.getInt(); //0			m_contentsSectionOffset
		if (in.getInt() != 0) System.err.println("Bounds: Havok file m_contentsSectionOffset is non-zero!");
		//in.getInt(); //0x554DF6C5	m_contentsClassNameCRC
		if (in.getInt() != 0x554DF6C5) System.err.println("Bounds: Wrong m_contentsClassNameCRC in Havok file!");
		//in.getInt(); //0x00050500	m_contentsVersion
		if (in.getInt() != 0x00050500) System.err.println("Bounds: Wrong m_contentsVersion in Havok file!");
		//in.getInt(); //48			m_absoluteDataStart
		if (in.getInt() != 48) System.err.println("Bounds: Havok file m_absoluteDataStart not equal to 48!");
		
		var m_localFixupsOffset = in.getInt(); //	m_localFixupsOffset
		var m_virtualFixupsOffset = in.getInt(); //	m_virtualFixupsOffset	count2 = count1+112
		var m_endOffset = in.getInt(); //			m_endOffset				count3 = count2+48
		

		ByteBuffer data = in.slice(in.position(), m_endOffset);
		data.order(endianness);


		data.position(m_localFixupsOffset);
		var LocalFixUps = new ArrayList<LocalFixUp>();
		while (data.position() < m_virtualFixupsOffset) {
			var f = new LocalFixUp(data);
			if (f.isValid()) LocalFixUps.add(f);
		}
		data.position(m_virtualFixupsOffset);
		var VirtualFixUps = new ArrayList<VirtualFixUp>();
		while (data.position() < m_endOffset) {
			var f = new VirtualFixUp(data);
			if (f.isValid()) VirtualFixUps.add(f);
		}
		
		data.position(0);
//		collisionShape = new CARP_HavokCollisionShape(data);
		
		scg = new scgHeader(data, LocalFixUps, VirtualFixUps);
		carname = scg.name;

		in.order(ByteOrder.LITTLE_ENDIAN);
		in.position(blockStart+blockLength);
	}
	
	public class scgHeader{
		public int nameHash;
		public boolean isResolved;
		public String name;
		public scgBounds root;
//		public float X, Y, Z, W;
		public hkArray<Integer> bones;
		//public hkArray<RenderNode> renderNodes;
		
		public scgHeader() {}
		
		public scgHeader(String n) {
			nameHash = Hash.findVLT(n);
			name = n;
		}
		
		public void setRoot(scgBounds r) {
			root = r;
			bones = new hkArray<>();
		}
		
		public scgHeader(ByteBuffer in, ArrayList<LocalFixUp> LocalFixUps, ArrayList<VirtualFixUp> VirtualFixUps) {
			if (findVirtualFixUpCRC(in.position(), VirtualFixUps) != this.CRC32()) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(in.position(), VirtualFixUps)));
			
			/*
			 * 295DD20F 0D000000 00000000 00000000 
			 * xxxxxxxx xxxxxxxx 00000000 00000000 
			 * 000000C0 xxxxxxxx 01000000 010000C0 
			 * 4D583500 00000000 00000000 00000000 
			 * xxxxxxxx 00000000 00000000 00000000 
			 * 30E817BE 000080B3 E7CE07BF 00000000
			 */
			nameHash = in.getInt(); //vlt key of collection name
			//var numBounds = 
					in.getInt();
			isResolved = in.getInt() == 1;
			in.getInt(); //0, RigHash
			
			int pRootBound = findLocalFixUpToOffset(in.position(), LocalFixUps); in.getInt(); // pointer to root @96
			//int pName = findLocalFixUpToOffset(in.position()); 
				in.getInt(); // pointer to name string @48

			bones = new hkArray<>(in, () -> 0, LocalFixUps); //empty
			var renderNodes = new hkArray<>(in, () -> new scgRenderNode(in, LocalFixUps), LocalFixUps); //renderNodes
			
//			System.out.println("Number of render nodes: %d".formatted(renderNodes.count));
			
			//renderNode
			//	pBound	0	0	0
			//		X	Y	Z	W
			
//			int backup = in.position();
			name = Block.readString(in);
			carname = name;
			Hash.findVLT(name);
			Block.skipAlignment(in, 16);
			
			for (int i=renderNodes.size(); i<renderNodes.count; i++) {
				renderNodes.add(new scgRenderNode(in, LocalFixUps));
			}
			
//			in.position(in.position()+16); // renderNodes[0]
//			X = in.getFloat(); 
//			Y = in.getFloat();
//			Z = in.getFloat();
//			W = in.getFloat();
			
//			in.position(pName);
//			name = Block.readString(in);
			if (pRootBound != -1) {
				in.position(pRootBound);
				root = new scgBounds(in, null, LocalFixUps, VirtualFixUps, renderNodes);
			}
//			in.position(backup);
		}

		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {

			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));
			
			var bb = ByteBuffer.allocate(48 + align(name.length()));
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(nameHash);
			bb.putInt(root.getChildrenRecursively().size());
			bb.putInt(isResolved ? 1 : 0);
			bb.putInt(0);
			bb.putInt(0); //pRoot
			bb.putInt(0); //pDebugName
			bones.saveObject(bb, contents.size());
			
//			if (renderNodes == null) {
//				renderNodes = new hkArray<>();
//				renderNodes.add(0);
//			}
			var renderNodes = root.getRenderNodesRecursively();
			renderNodes.saveObject(bb, contents.size()); //pRenderNodes[]

			
			Block.putString(bb, name); //debugName
//			//renderNodes
//			bb.position(80);
//			bb.putFloat(X);
//			bb.putFloat(Y);
//			bb.putFloat(Z);
//			bb.putFloat(W);

			//fixup order in vanilla files
			//pDebugName
			//pRenderNodes
			//all file contents...
			//renderNodes[0]->root
			//root
			
			//NO
			//THE STRING IS ALREADY A POINTER
			
			var pRoot = contents.size() + 16;
			var pDebugName = contents.size() + 20;
			
			localFixUps.add(new LocalFixUp(pDebugName, contents.size() + 48));
			contents.write(bb.array());
			
//			var pRenderNodes0Bound = contents.size();
			
			bones.saveArray(contents, i -> {}, localFixUps); //empty //contents.write(i); contents.write(i >>> 8); contents.write(i >>> 16); contents.write(i >>> 24);
			var renderNodesFixups = new HashMap<scgRenderNode,LocalFixUp>();
			renderNodes.saveArray(contents, n -> {
				renderNodesFixups.put(n, new LocalFixUp(contents.size(), 0));
				try {
					contents.write(n.save());
				} catch (IOException e) {e.printStackTrace();}
			}, localFixUps);
			
//			assert (contents.size() == pRenderNodes0Bound);
			
//			bb = ByteBuffer.allocate(32); 
//			bb.order(ByteOrder.LITTLE_ENDIAN);
//			bb.position(16); //TODO save multiple renderNodes
//			bb.putFloat(X);
//			bb.putFloat(Y);
//			bb.putFloat(Z);
//			bb.putFloat(W);
//			contents.write(bb.array()); //renderNodes[0]

			var offRoot = contents.size();
			root.save(contents, localFixUps, virtualFixUps, renderNodesFixups); //-1 as parent offset

//			localFixUps.add(new LocalFixUp(pRenderNodes0Bound, offRoot));
			localFixUps.add(new LocalFixUp(pRoot, offRoot));
			localFixUps.addAll(renderNodesFixups.values());
		}

		public int CRC32() {return 0x554DF6C5;} //every class with a CRC32 needs a VirtualFixUp
	}
	
	public hkpShape getShapeByCRC32(ByteBuffer in, ArrayList<LocalFixUp> LocalFixUps, ArrayList<VirtualFixUp> VirtualFixUps) {
		var crc32 = findVirtualFixUpCRC(in.position(), VirtualFixUps);
		switch (crc32) {
//		case 0x480F1A07:
//			return new scgBounds(in);
		case 0x0B0F481C:
			return new hkpBoxShape(in); //only needs its CRC32 VirtualFixUp
		case 0x371A2F2B:
			return new hkpConvexVerticesShape(in, LocalFixUps);
		case 0x509EA8D4:
			return new hkpConvexTranslateShape(in, LocalFixUps, VirtualFixUps);
		case 0xC957B0DA:
			return new hkpSphereShape(in); //only needs its CRC32 VirtualFixUp
		case 0xFA0272A2:
			return new hkpConvexTransformShape(in, LocalFixUps, VirtualFixUps);
		case 0x27F89FED:
			return new hkpCylinderShape(in);
		case 0x3AE85B6F:
			//what is this weird ass thing, node ?
			/*
			 	00000000 00000000 00000000 00000000 
			 	00000000 00000000 xxxxxxxx 01000000 -> pointer to list of shape pointers
			 	010000C0 00000000 00000000 00000000 
			 	9A60C63E 062D233F 9A60C63E CDCC4C3D 
			 	00000000 0000AC39 00000000 00000000 
			 	FFFFFFFF FFFFFFFF FFFFFFFF FFFFFFFF 
			 	FFFFFFFF FFFFFFFF FFFFFFFF FFFFFFFF
			 */
		}
		if (crc32 != -1) System.err.println("Unknown hkpShape class CRC32: 0x%08X".formatted(crc32));
		return null;
	}
	
	public interface hkpShape{
		public int CRC32();
		public void updateShape(double R, double G, double B, double A);
		public Node getDisplayShape();
		public hkpShape deepCopy();
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException;
	}
	
	public static class hkArray<t> extends ArrayList<t>{
		private static final long serialVersionUID = -8222962201258319415L;
		int count, capacityAndFlags;
		LocalFixUp fixup;

		//LocalFixUp to data
		
		public hkArray() {
			super();
		}
		
		public hkArray(ByteBuffer in, ReaderMethod<t> r, ArrayList<LocalFixUp> LocalFixUps){
			super();
			int data = findLocalFixUpToOffset(in.position(), LocalFixUps); in.getInt();
			count = in.getInt();
			capacityAndFlags = in.getInt();
			
			if (data == -1) {
				if (count > 0) {
					int backup = in.position();
					System.out.println("Cannot read hkArray<%s>".formatted(r.read().getClass().toString()));
					in.position(backup);
				}
				return;
			}

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
		
		public void saveObject(ByteBuffer out, int offset) {
			fixup = new LocalFixUp();
			fixup.fromOffset = offset + out.position();
			out.putInt(0); //dataPointer
			out.putInt(size()); //count
			out.putInt((size() & 0xffffff) | 0xC0000000); //capacity & flags
		}
		
		public void saveArray(ByteArrayOutputStream out, SaverMethod<t> s, ArrayList<LocalFixUp> LocalFixUps) {
			fixup.toOffset = out.size();
			for (var d : this) {
				s.save(d);
			}
			if (size() != 0) LocalFixUps.add(fixup);
			Block.skipAlignment(out, 16, 0);
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
		public LocalFixUp(int from, int to) {
			fromOffset = from;
			toOffset = to;
		}
		public boolean isValid() {
			return fromOffset != -1 && toOffset != -1;
		}
		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[8]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(fromOffset);
			bb.putInt(toOffset);
			return bb.array();
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
		public VirtualFixUp(int offset, int crc32) {
			this.objectOffset = offset;
			this.classCRC = crc32;
		}
		public boolean isValid() {
			return objectOffset != -1 && classCRC != -1;
		}
		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[8]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(objectOffset);
			bb.putInt(classCRC);
			return bb.array();
		}
	}
	
	public static class scgRenderNode{
		int pBound;
		float X, Y, Z, W;
		public scgRenderNode(ByteBuffer in, ArrayList<LocalFixUp> LocalFixUps) {
			pBound = findLocalFixUpToOffset(in.position(), LocalFixUps);
			in.position(in.position()+16);
			X = in.getFloat();
			Y = in.getFloat();
			Z = in.getFloat();
			W = in.getFloat();
		}
		public scgRenderNode() {
			// TODO Auto-generated constructor stub
		}
		public byte[] save() {
			var bb = ByteBuffer.allocate(32); 
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(16); //TODO save multiple renderNodes
			bb.putFloat(X);
			bb.putFloat(Y);
			bb.putFloat(Z);
			bb.putFloat(W);
			return bb.array();
		}
	}
	
	public static int findLocalFixUpToOffset(int fromOffset, ArrayList<LocalFixUp> LocalFixUps) {
		for (var f : LocalFixUps) {
			if (f.fromOffset == fromOffset) return f.toOffset;
		}
		return -1;
	}

	public static int findVirtualFixUpCRC(int offset, ArrayList<VirtualFixUp> VirtualFixUps) {
		for (var f : VirtualFixUps) {
			if (f.objectOffset == offset) return f.classCRC;
		}
		return -1;
	}

	public class scgBounds {

		public int AttributeName = 0; //hashes
		public int NameHash = 0;
		public int SurfaceName;
		
//		public ArrayList<BoundFlags> ChildrenFlags;
		public BoundConstraintType ConstraintType;
		public ArrayList<BoundFlags> Flags = new ArrayList<>();
		public BoundJointType JointType;
		public BoundNodeType NodeType;
		public BoundShape Shape;
		public BoundType Type;
		
		public int BoneIndex = -1;
		public float BoneOffsetX = -0;
		public float BoneOffsetY = -0;
		public float BoneOffsetZ = -0;
		
		public float HalfDimensionX = 0;
		public float HalfDimensionY = 0;
		public float HalfDimensionZ = 0;
		
//		public byte NumberOfChildren = 0;
		public int RenderHierarchyIndex = 0;

		public float OrientationX = 0;
		public float OrientationY = 0;
		public float OrientationZ = 0;
		public float OrientationW = 0;

		public float PivotX = 0;
		public float PivotY = 0;
		public float PivotZ = 0;

		public float PositionX = 0;
		public float PositionY = 0;
		public float PositionZ = 0;

		hkpShape collisionShape = null;
//		CollisionConvexTransform shapeTransform = null;
//		CollisionConvexTranslate shapeTranslate = null;
		
		public scgBounds parent = null;
		public ArrayList<scgBounds> childBounds = new ArrayList<>();
		
		public scgRenderNode scgRenderNode;
		
		//visual stuff that really shouldn't be there but im not gonna fix all that shit
		public Group displayShape = new Group();
		public OpenBox displayBound = new OpenBox(1,1,1, 0.001);
		public Sphere displayPivot = new Sphere(0.02);
		public BooleanProperty render = new SimpleBooleanProperty(false);
		
		//why isnt this a color already instead of creating a new Color object everytime it updates
		public double colorR = Math.random();
		public double colorG = Math.random();
		public double colorB = Math.random();
		
		public scgBounds(ByteBuffer bb, scgBounds parent, ArrayList<LocalFixUp> LocalFixUps, ArrayList<VirtualFixUp> VirtualFixUps, hkArray<scgRenderNode> scgRenderNodes) {
			if (findVirtualFixUpCRC(bb.position(), VirtualFixUps) != this.CRC32() && findVirtualFixUpCRC(bb.position(), VirtualFixUps) != -1) 
				System.err.println("Havok parser: expected class CRC 0x%08X, got 0x%08X!\nThe wrong class object will be created!".formatted(this.CRC32(), findVirtualFixUpCRC(bb.position(), VirtualFixUps)));

			for (var n : scgRenderNodes) {
				if (n.pBound == bb.position()) scgRenderNode = n;
			}
			if (parent == null && scgRenderNode == null) {
				System.out.println("Recovering RenderNodes[0]");
				scgRenderNode = scgRenderNodes.get(0);
			}
			
			OrientationX = bb.getFloat();
			OrientationY = bb.getFloat();
			OrientationZ = bb.getFloat();
			OrientationW = bb.getFloat();
			
			PositionX = bb.getFloat();
			PositionY = bb.getFloat();
			PositionZ = bb.getFloat();
			bb.getInt(); //PositionW
			
			HalfDimensionX = bb.getFloat();
			HalfDimensionY = bb.getFloat();
			HalfDimensionZ = bb.getFloat();
			bb.getInt(); //HalfDimensionW
			
			PivotX = bb.getFloat();
			PivotY = bb.getFloat();
			PivotZ = bb.getFloat();
			bb.getInt(); //PivotW
			
			BoneOffsetX = bb.getFloat();
			BoneOffsetY = bb.getFloat();
			BoneOffsetZ = bb.getFloat();
			bb.getInt(); //BoneOffsetW
			
			Type = BoundType.get(bb.get()); //BoundType
			JointType = BoundJointType.get(bb.get());
			Shape = BoundShape.get(bb.get()); //BoundShape
			Flags = BoundFlags.get(bb.get()); //BoundFlags
			AttributeName = bb.getInt(); //AttributeName VLT HASH
			SurfaceName = bb.getInt(); //SurfaceName VLT HASH
			NameHash = bb.getInt(); //NameHash VLT HASH
			
			//Hash.getVLT(AttributeName);
			//Hash.getVLT(SurfaceName);
			//Hash.getVLT(NameHash);
			BoneIndex = bb.getShort();
			RenderHierarchyIndex = bb.getShort();
			bb.getInt(); //fCollection
			bb.getInt(); //pParentBounds
			bb.getInt(); //fConstraintInfo
			
			var pShape = bb.position(); bb.getInt(); //fShape
			var pDebugName = bb.position(); bb.getInt(); //fDebugName
			var children = new hkArray<Integer>(bb, () -> {
				var pos = bb.position();
				bb.position(pos+4);
				return pos; //get pointers
			}, LocalFixUps);
			
			bb.position(bb.position()+12); //binary serialized read
			for (int i=children.size(); i<children.count; i++) {
				childBounds.add(new scgBounds(bb, this, LocalFixUps, VirtualFixUps, scgRenderNodes));
			}
			
			if (findLocalFixUpToOffset(pShape, LocalFixUps) != -1) {
				bb.position(findLocalFixUpToOffset(pShape, LocalFixUps));
				collisionShape = getShapeByCRC32(bb, LocalFixUps, VirtualFixUps);
				if (collisionShape == null) System.out.println("No shape for bound %s %s %s".formatted(Hash.getVLT(NameHash), Type.name(), Shape.name()));
			}
			
			if (findLocalFixUpToOffset(pDebugName, LocalFixUps) != -1) {
				bb.position(findLocalFixUpToOffset(pDebugName, LocalFixUps));
				var s = Block.readString(bb);
				if (!s.isBlank()) Hash.findVLT(s);
			}
			
			for (var c : children) if (findLocalFixUpToOffset(c, LocalFixUps) != -1) {
				bb.position(findLocalFixUpToOffset(c, LocalFixUps));
				childBounds.add(new scgBounds(bb, this, LocalFixUps, VirtualFixUps, scgRenderNodes));
			}
			this.parent = parent;
			
			setRenderListener();
		}
		
		public hkArray<scgRenderNode> getRenderNodesRecursively() {
			var renderNodes = new hkArray<scgRenderNode>();
			getRenderNodesRecursively(renderNodes);
			return renderNodes;
		}

		private void getRenderNodesRecursively(hkArray<scgRenderNode> scgRenderNodes) {
			if (scgRenderNode != null) scgRenderNodes.add(scgRenderNode);
			for (var c : this.childBounds) c.getRenderNodesRecursively(scgRenderNodes);
		}

		public scgRenderNode getRenderNode() {
			if (scgRenderNode != null) return scgRenderNode;
			if (parent == null) return null; //shouldn't happen
			return this.parent.getRenderNode();
		}

		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps, HashMap<scgRenderNode, LocalFixUp> renderNodesFixups) throws IOException {

			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));
			
			var pThis = contents.size();
			
			if (scgRenderNode != null) renderNodesFixups.get(scgRenderNode).toOffset = pThis;
			
			var bb = ByteBuffer.wrap(new byte[144]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(OrientationX);
			bb.putFloat(OrientationY);
			bb.putFloat(OrientationZ);
			bb.putFloat(OrientationW);
			bb.putFloat(PositionX);
			bb.putFloat(PositionY);
			bb.putFloat(PositionZ);
			bb.putInt(0); //PositionW
			bb.putFloat(HalfDimensionX);
			bb.putFloat(HalfDimensionY);
			bb.putFloat(HalfDimensionZ);
			bb.putInt(0); //HalfDimensionW
			bb.putFloat(PivotX);
			bb.putFloat(PivotY);
			bb.putFloat(PivotZ);
			bb.putInt(0);
			bb.putFloat(BoneOffsetX);
			bb.putFloat(BoneOffsetY);
			bb.putFloat(BoneOffsetZ);
			bb.putInt(0); //BoneOffsetW
			// byte 80
			if (Type == null) Type = BoundType.KBOUNDS_INVALID;
			bb.put(Type.getValue()); //BoundType
			if (JointType == null) JointType = BoundJointType.KBOUNDS_JOINT_INVALID;
			bb.put(JointType.getValue());
			if (Shape == null) Shape = BoundShape.KSHAPE_INVALID;
			bb.put(Shape.getValue());
			bb.put(BoundFlags.getFlags(Flags));
			bb.putInt(AttributeName);
			bb.putInt(SurfaceName);
			bb.putInt(NameHash);
			bb.putShort((short) BoneIndex);
			bb.putShort((short) RenderHierarchyIndex);

			bb.putInt(0); //fCollection
			//var pParentBounds = contents.size() + bb.position(); 
			bb.putInt(0); //pParentBounds
			bb.putInt(0); //fConstraintInfo

			var pShape = contents.size() + bb.position(); bb.putInt(0); //fShape
			bb.putInt(0); //fDebugName TODO add for world object collisions ? or unnecessary ?
			var children = new hkArray<Integer>();
			for (@SuppressWarnings("unused") var c : childBounds) children.add(0);
			children.saveObject(bb, contents.size());
			
			contents.write(bb.array());
			bb = null;
			
//			if (offParent != -1) {
//				localFixUps.add(new LocalFixUp(pParentBounds, offParent)); //probably not necessary
//			}
			
			if (collisionShape != null) {
				localFixUps.add(new LocalFixUp(pShape, contents.size()));
				collisionShape.save(contents, localFixUps, virtualFixUps);
			}
			
			var childrenFixups = new ArrayList<LocalFixUp>();
			children.saveArray(contents, i -> {
				childrenFixups.add(new LocalFixUp(contents.size(), 0));
				contents.write(0); contents.write(0); contents.write(0); contents.write(0);
			}, localFixUps);
			
			for (int i=0; i<childBounds.size(); i++) {
				childrenFixups.get(i).toOffset = contents.size();
				childBounds.get(i).save(contents, localFixUps, virtualFixUps, renderNodesFixups); //pThis as parent offset
				Block.skipAlignment(contents, 16, 0);
			}
			localFixUps.addAll(childrenFixups);
		}

		public scgBounds() {
			setRenderListener();
		}

		public void setRenderListener() {
			render.addListener((obs, was, is) -> {
//				updateShape();
//				if (CollisionsEditor.viewport != null) {
					if (was) {
						CollisionsEditor.shapesGroup.getChildren().remove(this.displayShape);
						CollisionsEditor.boundsGroup.getChildren().remove(this.displayBound);
						CollisionsEditor.pivotsGroup.getChildren().remove(this.displayPivot);
					}
					if (is) {
						CollisionsEditor.shapesGroup.getChildren().add(this.displayShape);
						CollisionsEditor.boundsGroup.getChildren().add(this.displayBound);
						CollisionsEditor.pivotsGroup.getChildren().add(this.displayPivot);
					}
					BoundCell.allCells.forEach(c -> {
						if (c.getItem() != null) c.checkBox.setSelected(c.getItem().render.get());
					});
//				}
			});
		}

		@Override
		public String toString() {
			return "- CollisionBound [AttributeName=" + AttributeName + ", NameHash=" + NameHash + ", SurfaceName="
					+ SurfaceName + ", Flags=" + Flags + ","
					+ "\n ConstraintType=" + ConstraintType + " JointType=" + JointType + ", NodeType=" + NodeType + ", Shape=" + Shape + ", Type=" + Type + ",\n"
					+ " BoneIndex=" + BoneIndex + ", BoneOffsetX=" + BoneOffsetX + ", BoneOffsetY=" + BoneOffsetY
					+ ", BoneOffsetZ=" + BoneOffsetZ + ", HalfDimensionX=" + HalfDimensionX + ", HalfDimensionY=" + HalfDimensionY + ", HalfDimensionZ=" + HalfDimensionZ + ","
//					+ "\n NumberOfChildren=" + NumberOfChildren	
					+ ", RenderHierarchyIndex=" + RenderHierarchyIndex + ", OrientationX=" + OrientationX
					+ ", OrientationY=" + OrientationY + ", OrientationZ=" + OrientationZ + ", OrientationW=" + OrientationW + ","
					+ "\n PivotX=" + PivotX + ", PivotY=" + PivotY + ", PivotZ=" + PivotZ + ", PositionX=" + PositionX
					+ ", PositionY=" + PositionY + ", PositionZ=" + PositionZ + "]";
		}

		public void updateShape() {
			try {
				hkpShape colShape = getShape();
				hkpShape colTransform = getTransform();
				
				displayShape.getChildren().clear();

				updateBoundAndPivot();
				
//				if (this.collisionShape == null) System.out.println("WARNING: null collision shape for bound %s".formatted(Hash.getVLT(this.NameHash)));
				if (colShape != null) {
					colShape.updateShape(colorR, colorG, colorB, 1.0);
					displayShape.getChildren().add(colShape.getDisplayShape());
					displayShape.setScaleX(-1);
					
					if (colTransform != null && colTransform.getClass() == hkpConvexTransformShape.class) {
						var shapeTransform = (hkpConvexTransformShape) colTransform;
//						displayShape.setTranslateX(-shapeTransform.TranslationX);
//						displayShape.setTranslateY(- (Collisions.this).scg.Z + shapeTransform.TranslationY);
//						displayShape.setTranslateZ(- (Collisions.this).scg.X + shapeTransform.TranslationZ);
						
						double d = Math.acos((shapeTransform.XRotationX+shapeTransform.YRotationY+shapeTransform.ZRotationZ-1d)/2d);
					    if(d!=0d){
					        double den=2d*Math.sin(d);
					        Point3D p= new Point3D((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den,
					        		(shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den,
					        		(-shapeTransform.XRotationY+shapeTransform.YRotationX)/den);
					        displayShape.setRotationAxis(p);
					        displayShape.setRotate(Math.toDegrees(d));
					        displayBound.setRotationAxis(p);
					        displayBound.setRotate(Math.toDegrees(d));
					    }
//					} else if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class) {
//						var shapeTranslate = (hkpConvexTranslateShape) colTransform;
//				        displayShape.setRotate(0);
//				        displayBound.setRotate(0);
//						displayShape.setTranslateX(-shapeTranslate.TranslationX);
//						displayShape.setTranslateY(- (Collisions.this).scg.Z + shapeTranslate.TranslationY);
//						displayShape.setTranslateZ(- (Collisions.this).scg.X + shapeTranslate.TranslationZ);
//					} else {
//				        displayShape.setRotate(0);
//				        displayBound.setRotate(0);
//						displayShape.setTranslateX(0);
//						displayShape.setTranslateY(- (Collisions.this).scg.Z); 
//						displayShape.setTranslateZ(- (Collisions.this).scg.X);
					} else {
				        displayShape.setRotate(0);
				        displayBound.setRotate(0);
					}
					if (colShape.getClass() != hkpConvexVerticesShape.class) {
						displayShape.setTranslateX(-getPositionXFromParent());
						displayShape.setTranslateY(getPositionYFromParent()); 
						displayShape.setTranslateZ(getPositionZFromParent());
					} else {
						displayShape.setTranslateX(+ getRenderNode().Y);
						displayShape.setTranslateY(- getRenderNode().Z); 
						displayShape.setTranslateZ(- getRenderNode().X);
					}
				} //collisionShape is null, show the boundary only


				//tooltips are currently quite annoying with gizmos
//				var t = new Tooltip(Hash.getVLT(this.NameHash));
//				
//				this.displayShape.setOnMouseEntered(e -> {
////				System.out.println("CollisionBound | Name : "+Hash.getVLT(this.NameHash)+", SurfaceName : "+Hash.getVLT(this.SurfaceName)+", AttributeName : "+Hash.getVLT(this.AttributeName)
////						+"\nType : "+this.Type+", Shape : "+Shape+", Node : "+NodeType+", Constraint : "+ConstraintType+", Joint : "+JointType);			
//					
//					t.show(displayShape, e.getScreenX()+10, e.getScreenY()+10);
//					
//					displayShape.requestFocus();
//				});
//				displayShape.setOnMouseMoved(e -> {
//					t.setAnchorX(e.getScreenX()+10);
//					t.setAnchorY(e.getScreenY()+10);
//				});
//				displayShape.setOnMouseExited(e -> {
//					t.hide();
//				});
				this.displayShape.setOnMousePressed(e->{
					expandTreeToItem(CollisionsEditor.hierarchy, CollisionsEditor.hierarchy.getRoot(), this);
		    		CollisionsEditor.boundControls.setBound(this);
				});
				this.displayShape.setOnContextMenuRequested(e -> {
					if (this.Shape != BoundShape.KSHAPE_MESH) {
						var cm = new ContextMenu();
						addBoundToContextMenu(cm);
						cm.show(this.displayShape, e.getScreenX(), e.getScreenY());
					}
				});
				this.displayShape.setOnKeyPressed(e -> {
					switch (e.getCode()) {
					case H:
						render.set(false);
						break;
					default:
					}
				});
				this.displayPivot.setOnMousePressed(e -> {
					if (e.isPrimaryButtonDown()) {
						render.set(true);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void addBoundToContextMenu(ContextMenu cm) {
			var info = new MenuItem("Bound "+Hash.getVLT(NameHash));
			var sep = new SeparatorMenuItem();
			var hide = new MenuItem(render.get() ? "Hide" : "Show");
			var delete = new MenuItem("Delete");
			var add = new MenuItem("Add");
			var duplicate = new MenuItem("Duplicate");
			hide.setOnAction(e2 -> {
				this.render.set(!render.get());
			});
			delete.setOnAction(e2 -> {
				if (parent != null) {
					parent.childBounds.remove(this);
					CollisionsEditor.updateAllBoundsDisplay();
					CollisionsEditor.boundsGroup.getChildren().remove(this.displayBound);
					CollisionsEditor.pivotsGroup.getChildren().remove(this.displayPivot);
					CollisionsEditor.shapesGroup.getChildren().remove(this.displayShape);
//					CollisionsEditor.updateRender();
					CollisionsEditor.planeControls.setPlane(null);
				} else {
					if (!CollisionsEditor.disableWarnings) new Alert(Alert.AlertType.WARNING, "Cannot do this on the main bound!", ButtonType.OK).show();
				}
			});
			add.setOnAction(evt -> {
				scgBounds c;
				this.childBounds.add(c = new scgBounds());
				c.parent = this;
				c.render.set(true);
				CollisionsEditor.updateAllBoundsDisplay();
//				CollisionsEditor.updateRender();
			});
			duplicate.setOnAction(evt -> {
				
				scgBounds dup = this.deepCopy();
				dup.render.set(true);
				if (parent != null) parent.childBounds.add(dup);
				else this.childBounds.add(dup);

				CollisionsEditor.updateAllBoundsDisplay();
//				CollisionsEditor.updateRender();
			});
			cm.getItems().addAll(info, sep, hide, add, delete, duplicate);
		}

		public scgBounds deepCopy() {
			var b = new scgBounds();
			
			b.Flags = new ArrayList<>();
//			b.ChildrenFlags = new ArrayList<>();
			
			b.AttributeName = this.AttributeName;
			b.BoneIndex = this.BoneIndex;
			b.BoneOffsetX = this.BoneOffsetX;
			b.BoneOffsetY = this.BoneOffsetY;
			b.BoneOffsetZ = this.BoneOffsetZ;
			this.childBounds.forEach(b2 -> b.childBounds.add(b2.deepCopy()));
//			this.ChildrenFlags.forEach(f -> b.ChildrenFlags.add(f)); // should work for an enum ?
			b.ConstraintType = this.ConstraintType;
			this.Flags.forEach(f -> b.Flags.add(f)); // should work for an enum ?
			b.HalfDimensionX = this.HalfDimensionX;
			b.HalfDimensionY = this.HalfDimensionY;
			b.HalfDimensionZ = this.HalfDimensionZ;
			b.JointType = this.JointType;
			b.NameHash = this.NameHash;
			b.NodeType = this.NodeType;
			b.OrientationX = this.OrientationX;
			b.OrientationY = this.OrientationY;
			b.OrientationZ = this.OrientationZ;
			b.OrientationW = this.OrientationW;
			b.parent = this.parent;
			b.PositionX = this.PositionX;
			b.PositionY = this.PositionY;
			b.PositionZ = this.PositionZ;
			b.PivotX = this.PivotX;
			b.PivotY = this.PivotY;
			b.PivotZ = this.PivotZ;
			b.RenderHierarchyIndex = this.RenderHierarchyIndex;
			b.Shape = this.Shape;
			b.SurfaceName = this.SurfaceName;
			b.Type = this.Type;
			
			switch (b.Shape) {
			case KSHAPE_BOX:
				b.collisionShape = collisionShape.deepCopy();
				break;
			case KSHAPE_MESH:
				b.collisionShape = collisionShape.deepCopy();
				break;
			case KSHAPE_SPHERE:
				b.collisionShape = collisionShape.deepCopy();
				break;
			default:
				break;
			}
			
			b.updateShape();
			return b;
		}

		public void updateBoundAndPivot() {
			displayBound.setFocusTraversable(true);
			// THIS IS THE BOUNDARY OF THE BOUND how ironic
			this.displayBound.setScaleX(-getScaleXFromParent());
			this.displayBound.setScaleY(getScaleYFromParent());
			this.displayBound.setScaleZ(getScaleZFromParent());
			this.displayBound.setTranslateX(-getPositionXFromParent());
			this.displayBound.setTranslateY(getPositionYFromParent()); 
			this.displayBound.setTranslateZ(getPositionZFromParent());
			
				
			this.displayPivot.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, 1)));
			this.displayPivot.setTranslateX(-PivotX);
			this.displayPivot.setTranslateY(PivotY);
			this.displayPivot.setTranslateZ(PivotZ);
			this.displayPivot.setViewOrder(0);
		}
		
		public void compute() {
			hkpShape colShape = getShape();

			if (colShape != null) switch(Shape) {
			case KSHAPE_BOX:
				hkpBoxShape box = (hkpBoxShape) colShape;
				HalfDimensionX = box.HalfExtentsX + box.radius;
				HalfDimensionY = box.HalfExtentsY + box.radius;
				HalfDimensionZ = box.HalfExtentsZ + box.radius;

				// ! transforms
				
				break;
				
			case KSHAPE_MESH:
				hkpConvexVerticesShape mesh = (hkpConvexVerticesShape)colShape;
				HalfDimensionX = mesh.HalfExtentsX; // + boundDistance
				HalfDimensionY = mesh.HalfExtentsY;
				HalfDimensionZ = mesh.HalfExtentsZ;
				PositionX = mesh.CenterX;
				PositionY = mesh.CenterY;
				PositionZ = mesh.CenterZ;
				PivotX = mesh.CenterX + getRenderNode().Y;
				PivotY = mesh.CenterY - getRenderNode().Z;
				PivotZ = mesh.CenterZ - getRenderNode().X;
				break;
			case KSHAPE_SPHERE:
				hkpSphereShape sphere = (hkpSphereShape)colShape;
				HalfDimensionX = sphere.radius;
				HalfDimensionY = sphere.radius;
				HalfDimensionZ = sphere.radius;

				// ! transforms
				
				break;
			default:
			}

			hkpShape colTransform = getTransform();
			if (colTransform != null) {
				if (colTransform.getClass() == hkpConvexTransformShape.class) {
					var shapeTransform = (hkpConvexTransformShape)colTransform;
					PositionX = shapeTransform.TranslationX;
					PositionY = shapeTransform.TranslationY;
					PositionZ = shapeTransform.TranslationZ;

					PivotX = shapeTransform.TranslationX + getRenderNode().Y;
					PivotY = shapeTransform.TranslationY - getRenderNode().Z;
					PivotZ = shapeTransform.TranslationZ - getRenderNode().X;
					
					double d = Math.acos((shapeTransform.XRotationX+shapeTransform.YRotationY+shapeTransform.ZRotationZ-1d)/2d);
				    if(d!=0d){
				        double den=2d*Math.sin(d);
//				        Point3D p= new Point3D((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den,
//				        		(shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den,
//				        		(shapeTransform.XRotationY-shapeTransform.YRotationX)/den);
//				        displayShape.setRotationAxis(p);
//				        displayShape.setRotate(Math.toDegrees(d));
				        

				        // d = 2*sqrt(x²+y²+z²)
				        // d² = 4x² + 4y² + 4z²
				        // x² = d²/4 - y² - z²
				        
				        var x = ((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den);
				        var y = ((shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den);
				        var z = ((shapeTransform.XRotationY-shapeTransform.YRotationX)/den);
				        OrientationX = (float) (x*d/2.0);
				        OrientationY = (float) (y*d/2.0);
				        OrientationZ = (float) (z*d/2.0);
				        OrientationW = 1.0f;

				        
//				        Point3D p2 = new Point3D(OrientationX, OrientationY, OrientationZ);

//				        displayBound.setRotationAxis(p2);
//				        displayBound.setRotate(Math.toDegrees(2.0 * Math.sqrt(OrientationX*OrientationX + OrientationY*OrientationY + OrientationZ*OrientationZ)));
				    }
					
				} else if (colTransform.getClass() == hkpConvexTranslateShape.class) {
					var shapeTranslate = (hkpConvexTranslateShape) colTransform;
					PositionX = shapeTranslate.TranslationX;
					PositionY = shapeTranslate.TranslationY;
					PositionZ = shapeTranslate.TranslationZ;
					
					PivotX = shapeTranslate.TranslationX + getRenderNode().Y;
					PivotY = shapeTranslate.TranslationY - getRenderNode().Z;
					PivotZ = shapeTranslate.TranslationZ - getRenderNode().X;
				} 
				else {

					PositionX = 0;
					PositionY = 0;
					PositionZ = 0;
					
					PivotX = + getRenderNode().Y;
					PivotY = - getRenderNode().Z;
					PivotZ = - getRenderNode().X;
				}
				
				
			}
		}

		public hkpShape getShape() {
			hkpShape colShape = collisionShape;
			if (colShape != null && (colShape.getClass() == hkpConvexTransformShape.class || colShape.getClass() == hkpConvexTranslateShape.class)) {
				colShape = ((hkpConvexTranslateShape)colShape).childShape;
				if (colShape != null && (colShape.getClass() == hkpConvexTransformShape.class || colShape.getClass() == hkpConvexTranslateShape.class)) {
					System.out.println("what");
					colShape = ((hkpConvexTranslateShape)colShape).childShape;
				}
			}
			return colShape;
		}
		public hkpShape getTransform() {
			hkpShape colShape = collisionShape;
			hkpShape colTransform = null;
			if (colShape != null && (colShape.getClass() == hkpConvexTransformShape.class || colShape.getClass() == hkpConvexTranslateShape.class)) {
				colTransform = colShape;
			}
			return colTransform;
		}

		public float getScaleXFromParent() {
//			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getScaleXFromParent()*HalfDimensionX*2;
			return HalfDimensionX*2;
		}
		public float getScaleYFromParent() {
//			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getScaleYFromParent()*HalfDimensionY*2;
			return HalfDimensionY*2;
		}
		public float getScaleZFromParent() {
//			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getScaleZFromParent()*HalfDimensionZ*2;
			return HalfDimensionZ*2;
		}

		public float getPositionXFromParent() {
			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPositionXFromParent()+PositionX;
			return PositionX + getRenderNode().Y;
		}
		public float getPositionYFromParent() {
			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPositionYFromParent()+PositionY;
			return PositionY - getRenderNode().Z;
		}
		public float getPositionZFromParent() {
			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPositionZFromParent()+PositionZ;
			return PositionZ - getRenderNode().X;
		}

		public float getPivotXFromParent() {
			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPivotXFromParent()+PivotX;
			return PivotX + getRenderNode().Y;
		}
		public float getPivotYFromParent() {
			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPivotYFromParent()+PivotY;
			return PivotY - getRenderNode().Z;
		}
		public float getPivotZFromParent() {
			if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPivotZFromParent()+PivotZ;
			return PivotZ - getRenderNode().X;
		}
		
		public ArrayList<scgBounds> getChildrenRecursively() {
			ArrayList<scgBounds> list = new ArrayList<>();
			list.add(this);
			for (var c : this.childBounds) {
				c.getChildrenRecursively(list);
				c.parent = this;
			}
			return list;
		}
		
		public void getChildrenRecursively(ArrayList<scgBounds> list) {
			list.add(this);
			for (var c : this.childBounds) {
				c.getChildrenRecursively(list);
				c.parent = this;
			}
		}
		
		public void renderSetRecursively(boolean b) {
			this.render.setValue(b);
			for (var c : this.childBounds) c.renderSetRecursively(b);
		}
		
		public void renderSetChildren(boolean b) {
			for (var c : this.childBounds) c.renderSetRecursively(b);
		}

		public byte[] saveHierarchy() throws IOException {

			var bb = ByteBuffer.wrap(new byte[144]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(OrientationX);
			bb.putFloat(OrientationY);
			bb.putFloat(OrientationZ);
			bb.putFloat(OrientationW);
			bb.putFloat(PositionX);
			bb.putFloat(PositionY);
			bb.putFloat(PositionZ);
			bb.putInt(0); //PositionW
			bb.putFloat(HalfDimensionX);
			bb.putFloat(HalfDimensionY);
			bb.putFloat(HalfDimensionZ);
			bb.putInt(0); //HalfDimensionW
			bb.putFloat(PivotX);
			bb.putFloat(PivotY);
			bb.putFloat(PivotZ);
			bb.putInt(0);
			bb.putFloat(BoneOffsetX);
			bb.putFloat(BoneOffsetY);
			bb.putFloat(BoneOffsetZ);
			bb.putInt(0); //BoneOffsetW
			// byte 80
			bb.put(Type.getValue()); //BoundType
			bb.put(JointType.getValue());
			bb.put(Shape.getValue());
			bb.put(BoundFlags.getFlags(Flags));
			bb.putInt(AttributeName);
			bb.putInt(SurfaceName);
			bb.putInt(NameHash);
			bb.putShort((short) BoneIndex);
			bb.putShort((short) RenderHierarchyIndex);
			// byte 100
			bb.put(new byte[24]);
			bb.put((byte) childBounds.size()); //NumberOfChildren
			bb.put(new byte[3]);
			bb.put((byte) childBounds.size()); 
			bb.put(new byte[2]);
//			bb.put(BoundFlags.getFlags(ChildrenFlags)); //FIXME
			bb.put(new byte[12]);

			if (this.childBounds.size() == 0) return bb.array();
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(bb.array());
			bb = null;
			for (var c : childBounds) bos.write(c.saveHierarchy());		
			return bos.toByteArray();
		}
		
		public TreeItem<scgBounds> makeTreeViewRecursively() {
			TreeItem<scgBounds> thisBound = new TreeItem<>(this);

			for (var c : this.childBounds) thisBound.getChildren().add(c.makeTreeViewRecursively());
			// return the item for this collision bound
			return thisBound;
		}
	
		public int CRC32() {return 0x480F1A07;}

		public Node getDisplayShape() {return displayShape;}
	}

	public class hkpConvexTranslateShape implements hkpShape{
		public float TranslationX = 0;
		public float TranslationY = 0;
		public float TranslationZ = 0;
		public float TranslationW = 0;
		public float radius = (float) 0.05;
		
		public hkpShape childShape;
		
		public hkpConvexTranslateShape() {
		}

		@Override
		public String toString() {
			return "\n -CollisionConvexTranslate [TranslationX=" + TranslationX + ", TranslationY=" + TranslationY
					+ ", TranslationZ=" + TranslationZ + ", TranslationW="+TranslationW+", unknownFloat=" + radius + "]";
		}

		public hkpConvexTranslateShape(ByteBuffer bb, ArrayList<LocalFixUp> LocalFixUps, ArrayList<VirtualFixUp> VirtualFixUps) {
			bb.position(bb.position() + 0x10);
			radius = bb.getFloat();
			bb.getInt(); //ShapeContainer vftable
			var pChildShape = bb.position(); bb.getInt(); //childShape
			bb.getInt(); //childShapeSize
			TranslationX = bb.getFloat();
			TranslationY = bb.getFloat();
			TranslationZ = bb.getFloat();
			TranslationW = bb.getFloat();
			
			if (findLocalFixUpToOffset(pChildShape, LocalFixUps) != -1) {
				bb.position(findLocalFixUpToOffset(pChildShape, LocalFixUps));
				childShape = getShapeByCRC32(bb, LocalFixUps, VirtualFixUps);
				if (childShape == null) System.out.println("No shape for bound ??? (translate)");
			}
		}
		
		@Override
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {
			
			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));			
			
			var bb = ByteBuffer.wrap(new byte[20+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(bb.position()+0x10);
			bb.putFloat(radius);
			bb.putInt(0);
			var pChildShape = contents.size() + bb.position(); bb.putInt(0);
			bb.putInt(0);
			bb.putFloat(TranslationX);
			bb.putFloat(TranslationY);
			bb.putFloat(TranslationZ);
			bb.putFloat(TranslationW);
			contents.write(bb.array());
			
			if (childShape != null) {
				Block.skipAlignment(contents, 16, 0);
				localFixUps.add(new LocalFixUp(pChildShape, contents.size()));
				childShape.save(contents, localFixUps, virtualFixUps);
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[20+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(new byte[0x10]);
			bb.putFloat(radius);
			bb.put(new byte[0x0C]);
			bb.putFloat(TranslationX);
			bb.putFloat(TranslationY);
			bb.putFloat(TranslationZ);
			bb.putFloat(TranslationW);
			return bb.array();
		}

		@Override
		public int CRC32() {return 0x509EA8D4;}

		@Override
		public void updateShape(double R, double G, double B, double A) {
			if (childShape != null) childShape.updateShape(R, G, B, A);
		}

		@Override
		public Node getDisplayShape() {
			if (childShape != null) return childShape.getDisplayShape();
			return null;
		}

		@Override
		public hkpShape deepCopy() {
			var copy = new hkpConvexTranslateShape();
			copy.TranslationX = this.TranslationX;
			copy.TranslationY = this.TranslationY;
			copy.TranslationZ = this.TranslationZ;
			copy.TranslationW = this.TranslationW;
			copy.radius = this.radius;
			if (this.childShape != null) copy.childShape = this.childShape.deepCopy(); 
			return copy;
		}

	}
	
	public class hkpConvexTransformShape extends hkpConvexTranslateShape {
		// int or float ???
		public int NumVertices = 0;
		
		public float XRotationX = 0;
		public float XRotationY = 0;
		public float XRotationZ = 0;
		public float XRotationW = 0;

		public float YRotationX = 0;
		public float YRotationY = 0;
		public float YRotationZ = 0;
		public float YRotationW = 0;

		public float ZRotationX = 0;
		public float ZRotationY = 0;
		public float ZRotationZ = 0;
		public float ZRotationW = 0;
		
		public hkpConvexTransformShape() {}

		@Override
		public String toString() {
			return "\n -CollisionConvexTransform [NumVertices=" + NumVertices + ", TranslationX=" + TranslationX
					+ ", TranslationY=" + TranslationY + ", TranslationZ=" + TranslationZ + ", TranslationW=" + TranslationW
					+ ", XRotationX=" + XRotationX + ", XRotationY=" + XRotationY + ", XRotationZ=" + XRotationZ
					+ ", XRotationW=" + XRotationW + ", YRotationX=" + YRotationX + ", YRotationY=" + YRotationY
					+ ", YRotationZ=" + YRotationZ + ", YRotationW=" + YRotationW + ", ZRotationX=" + ZRotationX
					+ ", ZRotationY=" + ZRotationY + ", ZRotationZ=" + ZRotationZ + ", ZRotationW=" + ZRotationW
					+ ", unknownFloat=" + radius + "]";
		}

		public hkpConvexTransformShape(ByteBuffer bb, ArrayList<LocalFixUp> LocalFixUps, ArrayList<VirtualFixUp> VirtualFixUps) {
			bb.position(bb.position() + 0x10);
			radius = bb.getFloat();
			bb.getInt(); //ShapeContainer vftable
			var pChildShape = bb.position(); bb.getInt(); //childShape
			bb.getInt(); //childShapeSize
			XRotationX = bb.getFloat();
			XRotationY = bb.getFloat();
			XRotationZ = bb.getFloat();
			XRotationW = bb.getFloat();
			YRotationX = bb.getFloat();
			YRotationY = bb.getFloat();
			YRotationZ = bb.getFloat();
			YRotationW = bb.getFloat();
			ZRotationX = bb.getFloat();
			ZRotationY = bb.getFloat();
			ZRotationZ = bb.getFloat();
			ZRotationW = bb.getFloat();
			TranslationX = bb.getFloat();
			TranslationY = bb.getFloat();
			TranslationZ = bb.getFloat();
			TranslationW = bb.getFloat();

			if (findLocalFixUpToOffset(pChildShape, LocalFixUps) != -1) {
				bb.position(findLocalFixUpToOffset(pChildShape, LocalFixUps));
				childShape = getShapeByCRC32(bb, LocalFixUps, VirtualFixUps);
				if (childShape == null) System.out.println("No shape for bound ??? (translate)");
			}
		}
		
		@Override
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {
			
			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));			
			
			var bb = ByteBuffer.wrap(new byte[68+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(bb.position()+0x10);
			bb.putFloat(radius);
			bb.putInt(0);
			var pChildShape = contents.size() + bb.position(); bb.putInt(0);
			bb.putInt(0);
			bb.putFloat(XRotationX);
			bb.putFloat(XRotationY);
			bb.putFloat(XRotationZ);
			bb.putFloat(XRotationW);
			bb.putFloat(YRotationX);
			bb.putFloat(YRotationY);
			bb.putFloat(YRotationZ);
			bb.putFloat(YRotationW);
			bb.putFloat(ZRotationX);
			bb.putFloat(ZRotationY);
			bb.putFloat(ZRotationZ);
			bb.putFloat(ZRotationW);
			bb.putFloat(TranslationX);
			bb.putFloat(TranslationY);
			bb.putFloat(TranslationZ);
			bb.putFloat(TranslationW);
			contents.write(bb.array());
			
			if (childShape != null) {
				Block.skipAlignment(contents, 16, 0);
				localFixUps.add(new LocalFixUp(pChildShape, contents.size()));
				childShape.save(contents, localFixUps, virtualFixUps);
			}
		}
		
		@Override
		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[68+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(new byte[0x10]);
			bb.putFloat(radius);
			bb.put(new byte[0x0C]);
			bb.putFloat(XRotationX);
			bb.putFloat(XRotationY);
			bb.putFloat(XRotationZ);
			bb.putFloat(XRotationW);
			bb.putFloat(YRotationX);
			bb.putFloat(YRotationY);
			bb.putFloat(YRotationZ);
			bb.putFloat(YRotationW);
			bb.putFloat(ZRotationX);
			bb.putFloat(ZRotationY);
			bb.putFloat(ZRotationZ);
			bb.putFloat(ZRotationW);
			bb.putFloat(TranslationX);
			bb.putFloat(TranslationY);
			bb.putFloat(TranslationZ);
			bb.putFloat(TranslationW);
			return bb.array();
		}

		public double[] getEulerAngles() {
		    double sy = -Math.asin(XRotationZ);
		    double cy = Math.cos(sy);

		    double x, y, z;
		    if (Math.abs(cy) > 1e-6) { // Avoid division by zero
		        x = Math.atan2(YRotationZ, ZRotationZ);
		        y = sy;
		        z = Math.atan2(XRotationY, XRotationX);
		    } else { // Gimbal lock situation
		        x = 0.0; 
//		        z = 0.0;
//		    	System.out.println("Gymbal lock "+this.nameHash.label+" on "+this.part.name);
		        if (XRotationZ < 0) {
//		        	x = Math.atan2(R[1][0], R[2][0] );
		            y = Math.PI / 2;
		            z = Math.atan2(YRotationX, YRotationY);
		        } else {
//		        	x = Math.atan2( -R[1][0], -R[2][0] );
		            y = -Math.PI / 2;
		            z = Math.atan2(-YRotationX, -YRotationY);
		        }
		    }
		    return new double[]{Math.toDegrees(x), Math.toDegrees(y), Math.toDegrees(z)};
		}
		
		public void fromEulerAngles(double u, double v, double w) {
			//first calculation
			u = Math.toRadians(u);
		    v = Math.toRadians(v);
		    w = Math.toRadians(w);

		    double cu = Math.cos(u);
		    double su = Math.sin(u);
		    double cv = Math.cos(v);
		    double sv = Math.sin(v);
		    double cw = Math.cos(w);
		    double sw = Math.sin(w);

		    var R = new double[3][3];
		    R[0][0] = (cv * cw);
		    R[0][1] = (cv * sw);
		    R[0][2] = -sv;
		    R[1][0] = (su * sv * cw - cu * sw);
		    R[1][1] = (su * sv * sw + cu * cw);
		    R[1][2] = (su * cv);
		    R[2][0] = (cu * sv * cw + su * sw);
		    R[2][1] = (cu * sv * sw - su * cw);
		    R[2][2] = (cu * cv);
		    
		    //inversion
		    double sy = -Math.asin(R[0][2]);
		    double cy = Math.cos(sy);

		    double x, y, z;
		    if (Math.abs(cy) > 1e-6) { // Avoid division by zero
		        x = Math.atan2(R[1][2], R[2][2]);
		        y = sy;
		        z = Math.atan2(R[0][1], R[0][0]);
		    } else { // Gimbal lock situation
		        x = 0.0; 
//		        z = 0.0;
		        if (R[0][2] < 0) {
//		        	x = Math.atan2(R[1][0], R[2][0] );
		            y = Math.PI / 2;
		            z = Math.atan2(R[1][0], R[1][1]);
		        } else {
//		        	x = Math.atan2( -R[1][0], -R[2][0] );
		            y = -Math.PI / 2;
		            z = Math.atan2(-R[1][0], -R[1][1]);
		        }
		    }
		    
		    //second calculation
		    cu = Math.cos(x);
		    su = Math.sin(x);
		    cv = Math.cos(y);
		    sv = Math.sin(y);
		    cw = Math.cos(z);
		    sw = Math.sin(z);
		    

		   	XRotationX = (float) (cv * cw);
		    XRotationY = (float) (cv * sw);
		    XRotationZ = (float) -sv;
		    YRotationX = (float) (su * sv * cw - cu * sw);
		    YRotationY = (float) (su * sv * sw + cu * cw);
		    YRotationZ = (float) (su * cv);
		    ZRotationX = (float) (cu * sv * cw + su * sw);
		    ZRotationY = (float) (cu * sv * sw - su * cw);
		    ZRotationZ = (float) (cu * cv);
		}
		
		@Override
		public int CRC32() {return 0xFA0272A2;}

		@Override
		public void updateShape(double R, double G, double B, double A) {
			if (childShape != null) childShape.updateShape(R, G, B, A);
		}

		@Override
		public Node getDisplayShape() {
			if (childShape != null) return childShape.getDisplayShape();
			return null;
		}

		@Override
		public hkpShape deepCopy() {
			var copy = new hkpConvexTransformShape();
			copy.TranslationX = this.TranslationX;
			copy.TranslationY = this.TranslationY;
			copy.TranslationZ = this.TranslationZ;
			copy.TranslationW = this.TranslationW;

			copy.XRotationX = this.XRotationX;
			copy.XRotationY = this.XRotationY;
			copy.XRotationZ = this.XRotationZ;
			copy.XRotationW = this.XRotationW;

			copy.YRotationX = this.YRotationX;
			copy.YRotationY = this.YRotationY;
			copy.YRotationZ = this.YRotationZ;
			copy.YRotationW = this.YRotationW;

			copy.ZRotationX = this.ZRotationX;
			copy.ZRotationY = this.ZRotationY;
			copy.ZRotationZ = this.ZRotationZ;
			copy.ZRotationW = this.ZRotationW;
			copy.radius = this.radius;
			if (this.childShape != null) copy.childShape = this.childShape.deepCopy(); 
			return copy;
		}

		public void fromTransform(Affine a) { //-angles[2]
			XRotationX = (float) a.getMxx();
			XRotationY = (float) a.getMxy();
			XRotationZ = (float) a.getMxz();
			YRotationX = (float) a.getMyx();
			YRotationY = (float) a.getMyy();
			YRotationZ = (float) a.getMyz();
			ZRotationX = (float) a.getMzx();
			ZRotationY = (float) a.getMzy();
			ZRotationZ = (float) a.getMzz();
			
		}
	}

	public class hkpBoxShape implements hkpShape {
		public float HalfExtentsX = (float) 0.06;
		public float HalfExtentsY = (float) 0.06;
		public float HalfExtentsZ = (float) 0.06;
		public float HalfExtentsW = (float) 0.06;
		
		public float radius = (float) 0.05;

		public Shape3D displayShape = new Box(1,1,1);
		//public scgBounds bound = null;
		
		public hkpBoxShape() {}

		@Override
		public String toString() {
			return "\n -CollisionBoxShape [HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY + ", HalfExtentsZ="
					+ HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW + ", unknownFloat=" + radius + "]";
		}

		public hkpBoxShape(ByteBuffer bb) {
			bb.position(bb.position() + 0x10);
			radius = bb.getFloat();
			bb.position(bb.position() + 0x0C);
			HalfExtentsX = bb.getFloat();
			HalfExtentsY = bb.getFloat();
			HalfExtentsZ = bb.getFloat();
			HalfExtentsW = bb.getFloat();
		}

		@Override
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {
			
			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));			
			
			var bb = ByteBuffer.wrap(new byte[20+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(bb.position()+0x10);
			bb.putFloat(radius);
			bb.putInt(0);
			bb.putInt(0);
			bb.putInt(0);
			bb.putFloat(HalfExtentsX);
			bb.putFloat(HalfExtentsY);
			bb.putFloat(HalfExtentsZ);
			bb.putFloat(HalfExtentsW);
			contents.write(bb.array());
		}
		
		@Override
		public void updateShape(double colorR, double colorG, double colorB, double d) {
			this.displayShape.setScaleX(HalfExtentsX*2);
			this.displayShape.setScaleY(HalfExtentsY*2);
			this.displayShape.setScaleZ(HalfExtentsZ*2);
			this.displayShape.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, d)));
		}

		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[20+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(new byte[0x10]);
			bb.putFloat(radius);
			bb.put(new byte[0x0C]);
			bb.putFloat(HalfExtentsX);
			bb.putFloat(HalfExtentsY);
			bb.putFloat(HalfExtentsZ);
			bb.putFloat(HalfExtentsW);
			return bb.array();
		}

		@Override
		public int CRC32() {return 0x0B0F481C;}

		@Override
		public javafx.scene.Node getDisplayShape() {
			return displayShape;
		}

		@Override
		public hkpShape deepCopy() {
			var copy = new hkpBoxShape();
			copy.HalfExtentsX = this.HalfExtentsX;
			copy.HalfExtentsY = this.HalfExtentsY;
			copy.HalfExtentsZ = this.HalfExtentsZ;
			copy.HalfExtentsW = this.HalfExtentsW;
			copy.radius = this.radius;
			return copy;
		}
	}
	
	public class hkpSphereShape implements hkpShape {

		public float radius = (float) 0.05;
		public Shape3D displayShape = new Sphere(0.5);
//		public scgBounds bound = null;
		
		public hkpSphereShape() {}

		public hkpSphereShape(ByteBuffer bb) {
			bb.position(bb.position() + 0x10);
			radius = bb.getFloat();
			bb.position(bb.position() + 0x0C);
		}
		
		@Override
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {
			
			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));			
			
			var bb = ByteBuffer.wrap(new byte[4+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(bb.position()+0x10);
			bb.putFloat(radius);
			bb.putInt(0);
			bb.putInt(0);
			bb.putInt(0);
			contents.write(bb.array());
		}
		
		@Override
		public void updateShape(double colorR, double colorG, double colorB, double d) {
			this.displayShape.setScaleX(radius*2);
			this.displayShape.setScaleY(radius*2);
			this.displayShape.setScaleZ(radius*2);
			this.displayShape.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, d)));
		}

		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[4+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(new byte[0x10]);
			bb.putFloat(radius);
			bb.put(new byte[0x0C]);
			return bb.array();
		}

		@Override
		public int CRC32() {return 0xC957B0DA;}

		@Override
		public Node getDisplayShape() {
			return displayShape;
		}

		@Override
		public hkpShape deepCopy() {
			var copy = new hkpSphereShape();
			copy.radius = this.radius;
			return copy;
		}
	}
	
	public class hkpConvexVerticesShape implements hkpShape {

		public float CenterX = 0;
		public float CenterY = 0;
		public float CenterZ = 0;
		public float CenterW = 0;

		public float HalfExtentsX = 0;
		public float HalfExtentsY = 0;
		public float HalfExtentsZ = 0;
		public float HalfExtentsW = 0;

//		public int NumberOfPlaneEquations = 0;
//		public int NumberOfRotatedVertices = 0;
//		public int NumVertices = 0;
		
		public float radius = (float) 0.05;

//		public hkArray<FourVectors> rotatedVertices;
		public hkArray<PlaneEquation> PlaneEquations;
		
//		public ArrayList<LineEquation> LineEquations = new ArrayList<>();
		public ArrayList<Vertex> Vertices = new ArrayList<>();
		
//		public ArrayList<RotatedVertice> RotatedVertices = new ArrayList<>();
		
		public Group displayShape = new Group();
		private ArrayList<Shape3D> shapes = new ArrayList<>();
//		private scgBounds bound = null;

//		public static float carHalfWidth = 0f;
//		public static float carHalfLength = 0f;
//		public static float carHalfHeight = 0f;
		
//		public hkpConvexVerticesShape() {}

		public hkpConvexVerticesShape() { //scgBounds bound2
//			bound = bound2;
			PlaneEquations.add(new PlaneEquation(this, 0.0f, 1.0f, 0.0f, 0.1f));
			PlaneEquations.add(new PlaneEquation(this, 0.0f, -1.0f, 0.0f, 0.1f));
			PlaneEquations.add(new PlaneEquation(this, 1.0f, 0.0f, 0.0f, 0.1f));
			PlaneEquations.add(new PlaneEquation(this, -1.0f, 0.0f, 0.0f, 0.1f));
			PlaneEquations.add(new PlaneEquation(this, 0.0f, 0.0f, 1.0f, 0.1f));
			PlaneEquations.add(new PlaneEquation(this, 0.0f, 0.0f, -1.0f, 0.1f));
//			updateShape();
		}

		public scgBounds getBound() {
			for (var b : CollisionsEditor.mainCollisions.scg.root.getChildrenRecursively()) if (b.getShape() == this) return b;
			return null;
		}
		
		@Override
		public String toString() {
			return "\n -CollisionConvexVertice [CenterX=" + CenterX + ", CenterY=" + CenterY + ", CenterZ=" + CenterZ
					+ ", CenterW=" + CenterW + ", HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY
					+ ", HalfExtentsZ=" + HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW 
//					+ ", NumberOfPlaneEquations=" + NumberOfPlaneEquations + ", NumberOfRotatedVertices=" + NumberOfRotatedVertices 
//					+ ", NumVertices=" + NumVertices 
					+ ", unknownFloat=" + radius + ",\n PlaneEquations=" + PlaneEquations
//					+ ",\n RotatedVertices=" + RotatedVertices 
					+ "]";
		}
		
		void sortForRender(ArrayList<Vertex> facePoints, PlaneEquation p) {
		    if (facePoints.size() < 3) return; //not enough vertices

		    
		    // ax + by + cz + d = 0 => x', y'
		    // z = (ax+by+d)/c
		    
		    
		    // U = -b, a, 0
		    // V = c, 0, -a
		    
//	   	    double v = (P.x - centre.x) * base.V.x + (P.y - centre.y) * base.V.y + (P.z - centre.z) * base.V.z;	    
//		    double u = (P.x - centre.x) * base.U.x + (P.y - centre.y) * base.U.y + (P.z - centre.z) * base.U.z;

		    // barycenter
		    Vertex centre = Vertex.getCenter(facePoints);

		    // sort by 2D angle around the center
		    facePoints.sort((p1, p2) -> {

		        double angle1 = Math.atan2((p1.x - centre.x) * p.c + (p1.z - centre.z) * -p.a, (p1.x - centre.x) * -p.b + (p1.y - centre.y) * p.a);
		        double angle2 = Math.atan2((p2.x - centre.x) * p.c + (p2.z - centre.z) * -p.a, (p2.x - centre.x) * -p.b + (p2.y - centre.y) * p.a);
		    	
		        return Double.compare(angle1, angle2);
		    });
		}
		
		public void updateShape() {
			displayShape.getChildren().clear();
			this.shapes.clear();
			this.Vertices.clear();
			
			Vertex center = new Vertex(CenterX, CenterY, CenterZ); //center of the bounds, to recalculate after any edit !
			
			for (PlaneEquation p : this.PlaneEquations) p.containedVertices.clear();
			
			for (PlaneEquation p : this.PlaneEquations) {
				for (PlaneEquation p2 : this.PlaneEquations) {
					for (PlaneEquation p3 : this.PlaneEquations) {
						PlaneEquation.intersect3(p,p2,p3);
					}
				}
			}
			
			
			// take all points in a plane and make a straight between that point and the origin, intersect the straight with all other planes and check the distance every time, 
			// if you can't find closer then the point is valid
			for (PlaneEquation p : this.PlaneEquations) {
				ArrayList<Vertex> valid = new ArrayList<>();

				for (var v : p.containedVertices) if (v != null) {
					var dist = center.distanceTo(v) - 0.01;
					boolean validated = true;
					for (var p2 : this.PlaneEquations) if (!p2.equals(p)) {
						Vertex intersect = v.findOnPlaneBetweenVertices(p2, center);
						if (intersect != null && center.distanceTo(intersect) < dist) {
							validated = false;
							break;
						}
					}
					if (validated && !valid.contains(v)) valid.add(v);
				}
				
//				sortForRender(valid, p);
				
				TriangleMesh planeMesh = new TriangleMesh();
				planeMesh.getTexCoords().addAll(0, 0);
				
				for (var v : valid) {
		            planeMesh.getPoints().addAll(v.x, v.y, v.z);
		            this.Vertices.add(v);
//		            if (valid.indexOf(v)>2) {
//				        planeMesh.getFaces().addAll(
//				                planeMesh.getPoints().size()/3-3, 0,  
//				                planeMesh.getPoints().size()/3-2, 0,  
//				                planeMesh.getPoints().size()/3-1, 0);
//		            }
				}
//				for (int i=2; i<valid.size(); i++) {
//			        planeMesh.getFaces().addAll(
//			                planeMesh.getPoints().size()/3-valid.size()+i-2, 0,  
//			                planeMesh.getPoints().size()/3-valid.size()+i-1, 0,  
//			                planeMesh.getPoints().size()/3-valid.size()+i, 0);
//					
//				}
				for (int i=0; i<valid.size(); i++) for (int j=0; j<valid.size(); j++) for (int k=0; k<valid.size(); k++) {
			        planeMesh.getFaces().addAll(
			                planeMesh.getPoints().size()/3-valid.size()+i, 0,  
			                planeMesh.getPoints().size()/3-valid.size()+j, 0,  
			                planeMesh.getPoints().size()/3-valid.size()+k, 0);
					
				}
				
				if (valid.size() < 3) {
					//not enough vertices, fall back to rendering the plane entirely
					if (Math.abs(p.b)>0.05) {
			            planeMesh.getPoints().addAll(
		            		CenterX+ -HalfExtentsX, 	(-p.a*-HalfExtentsX - p.c*-HalfExtentsZ -p.d)/p.b, CenterZ+ 	-HalfExtentsZ,	//0
		            		CenterX+ HalfExtentsX, 	(-p.a*HalfExtentsX -  p.c*-HalfExtentsZ -p.d)/p.b, 	CenterZ+ -HalfExtentsZ,	//1
		            		CenterX+ -HalfExtentsX, 	(-p.a*-HalfExtentsX - p.c*HalfExtentsZ  -p.d)/p.b, CenterZ+ 	HalfExtentsZ,	//2
		            		CenterX+ HalfExtentsX, 	(-p.a*HalfExtentsX -  p.c*HalfExtentsZ  -p.d)/p.b, 	CenterZ+ HalfExtentsZ);	//3
					} else if (Math.abs(p.c)>0.05) {
		                planeMesh.getPoints().addAll(
		                		CenterX+ -HalfExtentsX, CenterY+ 	-HalfExtentsY,		(-p.a*-HalfExtentsX - p.b*-HalfExtentsY -p.d)/p.c,	//0
		                		CenterX+ HalfExtentsX, 	CenterY+ -HalfExtentsY,		(-p.a*HalfExtentsX - p.b*-HalfExtentsY -p.d)/p.c,	//1
		                		CenterX+ -HalfExtentsX, CenterY+ 	HalfExtentsY,		(-p.a*-HalfExtentsX - p.b*HalfExtentsY -p.d)/p.c,	//2
		                		CenterX+ HalfExtentsX, 	CenterY+ HalfExtentsY,		(-p.a*HalfExtentsX - p.b*HalfExtentsY -p.d)/p.c);	//3
					} else {
		                planeMesh.getPoints().addAll(
		                		(-p.b*-HalfExtentsY - p.c*-HalfExtentsZ -p.d)/p.a,	CenterY+ -HalfExtentsY, 	CenterZ+ -HalfExtentsZ,	//0
		                		(-p.b*HalfExtentsY -  p.c*-HalfExtentsZ -p.d)/p.a, 	CenterY+ HalfExtentsY,		CenterZ+ -HalfExtentsZ,	//1
		                		(-p.b*-HalfExtentsY - p.c*HalfExtentsZ  -p.d)/p.a, 	CenterY+ -HalfExtentsY,	CenterZ+ HalfExtentsZ,	//2
		                		(-p.b*HalfExtentsY -  p.c*HalfExtentsZ  -p.d)/p.a, 	CenterY+ HalfExtentsY,		CenterZ+ HalfExtentsZ);	//3
					}
					
					
			        // Define the faces of the plane
			        planeMesh.getFaces().addAll(
			                planeMesh.getPoints().size()/3-4, 0,  planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-2, 0,  // Triangle 1 (Vertices 0, 1, 2)
			                planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-1, 0,  planeMesh.getPoints().size()/3-2, 0,   // Triangle 2 (Vertices 1, 2, 3) should be 1,3,2
			                planeMesh.getPoints().size()/3-4, 0,  planeMesh.getPoints().size()/3-2, 0,  planeMesh.getPoints().size()/3-3, 0,  // Triangle 1 (Vertices 0, 1, 2)
			                planeMesh.getPoints().size()/3-3, 0,  planeMesh.getPoints().size()/3-2, 0,  planeMesh.getPoints().size()/3-1, 0   // Triangle 2 (Vertices 1, 2, 3) should be 1,3,2
			        );
				}

				var shape = new MeshView(planeMesh);
		        shape.setDrawMode(DrawMode.FILL);
		        shape.setCullFace(CullFace.BACK); // Ensure both sides of the plane are visible
		        shape.setMaterial(new PhongMaterial(Color.color(p.colorR, p.colorG, p.colorB)));
				this.shapes.add(shape);
				
				shape.setOnMouseEntered(e -> {
//					((PhongMaterial)shape.getMaterial()).setDiffuseColor(Color.color(1.0, 0.0, 0.0));
//					((PhongMaterial)shape.getMaterial()).setSpecularPower(0.0);
//			        shape.setMaterial(new PhongMaterial(Color.color(1.0, 0.0, 0.0)));
			        shape.requestFocus();
				});
				shape.setOnMouseExited(e -> {
//					((PhongMaterial)shape.getMaterial()).setDiffuseColor(Color.color(p.colorR, p.colorG, p.colorB));
//			        shape.setMaterial(new PhongMaterial(Color.color(p.colorR, p.colorG, p.colorB)));
				});
				shape.setOnMousePressed(e -> {
//					((PhongMaterial)shape.getMaterial()).setDiffuseColor(Color.color(1.0, 0.0, 0.0));
					CollisionsEditor.planeControls.setPlane(p);
//					this.updateShape();
				});
				
				shape.setOnContextMenuRequested(e -> {
					var cm = new ContextMenu();
					
					this.getBound().addBoundToContextMenu(cm);

					var planeInfo = new MenuItem("Plane #"+PlaneEquations.indexOf(p));
					var planeDelete = new MenuItem("Delete");
					var planeAdd = new MenuItem("Add");
					var planeDuplicate = new MenuItem("Duplicate");
					planeDelete.setOnAction(e2 -> {
						PlaneEquations.remove(CollisionsEditor.planeControls.getPlane());
						if (CollisionsEditor.planeControls.getPlaneSym() != null) PlaneEquations.remove(CollisionsEditor.planeControls.getPlaneSym());
						this.updateShape();
						CollisionsEditor.planeControls.setPlane(null);
					});
					planeAdd.setOnAction(evt -> {
						PlaneEquation eq;
						PlaneEquations.add(eq = new PlaneEquation());
						eq.convexVerticeShape = this;
						eq.colorR = Collisions.randomizeColor(getBound().colorR, 0.4);
						eq.colorG = Collisions.randomizeColor(getBound().colorG, 0.4);
						eq.colorB = Collisions.randomizeColor(getBound().colorB, 0.4);
						updateShape();
					});
					planeDuplicate.setOnAction(evt -> {
						PlaneEquations.add(CollisionsEditor.planeControls.getPlane().deepCopyOffset(this));
						if (CollisionsEditor.planeControls.getPlaneSym() != null) PlaneEquations.add(CollisionsEditor.planeControls.getPlaneSym().deepCopyOffset(this));
						updateShape();
					});
					cm.getItems().addAll(new SeparatorMenuItem(), planeInfo, new SeparatorMenuItem(), planeAdd, planeDelete, planeDuplicate);
					cm.show(shape, e.getScreenX(), e.getScreenY());
					
				});
				
				shape.setOnKeyReleased(e -> {
//					System.out.println("KEY TYPED");
					switch (e.getCode()) {
					case DELETE:
//						System.out.println("DELETE PLANE");
						PlaneEquations.remove(p);
						this.updateShape();
						CollisionsEditor.planeControls.setPlane(null);
//						CollisionsEditor.updateRender();
						break;
					default:
					}
				});
			}

			for (int i=0; i<Vertices.size(); i++) for (int j=0; j<Vertices.size(); j++) {
				if (i != j && Vertices.get(i).equals(Vertices.get(j))) {
					if (i<j) {
						Vertices.remove(j);
						j--;
					}
					if (i>j) {
						Vertices.remove(i);
						i--;
					}
				}
			}

//			System.out.println("Number of calculated vertices : "+this.Vertices.size()+" vs actual number of vertices : "+this.NumVertices);
			

			for (Vertex v : this.Vertices) {
				Sphere s;
				shapes.add(s = new Sphere(0.01));
				s.setTranslateX(v.x);
				s.setTranslateY(v.y);
				s.setTranslateZ(v.z);
//				System.out.println(v);
			}

			displayShape.getChildren().addAll(shapes);
			
			//recalculate bounds
			float xmin = Float.POSITIVE_INFINITY, ymin = Float.POSITIVE_INFINITY, zmin = Float.POSITIVE_INFINITY;
			float xmax = Float.NEGATIVE_INFINITY, ymax = Float.NEGATIVE_INFINITY, zmax = Float.NEGATIVE_INFINITY;
			for (var v : Vertices) {
				if (v.x < xmin) xmin = v.x;
				if (v.y < ymin) ymin = v.y;
				if (v.z < zmin) zmin = v.z;
				if (v.x > xmax) xmax = v.x;
				if (v.y > ymax) ymax = v.y;
				if (v.z > zmax) zmax = v.z;
			}

			CenterX = (xmin + xmax)/2;
			CenterY = (ymin + ymax)/2;
			CenterZ = (zmin + zmax)/2;

			HalfExtentsX = xmax - CenterX;
			HalfExtentsY = ymax - CenterY;
			HalfExtentsZ = zmax - CenterZ;
			
			if (getBound() != null) getBound().updateBoundAndPivot();
		}
		
		public hkpConvexVerticesShape(ByteBuffer bb, ArrayList<LocalFixUp> LocalFixUps) {
			bb.position(bb.position() + 0x10);
			radius = bb.getFloat();
			bb.position(bb.position() + 0x0C);
			
			HalfExtentsX = bb.getFloat();
			HalfExtentsY = bb.getFloat();
			HalfExtentsZ = bb.getFloat();
			HalfExtentsW = bb.getFloat();
			CenterX = bb.getFloat();
			CenterY = bb.getFloat();
			CenterZ = bb.getFloat();
			CenterW = bb.getFloat();

			var rotatedVertices = new hkArray<FourVectors>(bb, () -> new FourVectors(bb), LocalFixUps);		
			//var NumVertices = 
					bb.getInt();
			PlaneEquations = new hkArray<PlaneEquation>(bb, () -> new PlaneEquation(bb, this), LocalFixUps);
			//var pConnectivity = 
					bb.getInt(); //ignored, no fixup

			for (var fv : rotatedVertices) {
				Vertices.add(fv.v1);
				Vertices.add(fv.v2);
				Vertices.add(fv.v3);
				Vertices.add(fv.v4);
			}
			
			
			if (PlaneEquations.size() < PlaneEquations.count) {
				// ignore the """rotated""" vertices, they will be recalculated from the planes and welded by the way
				bb.position(bb.position()+48*rotatedVertices.count);

				// should only happen on a binary serialized file
				for (int loop = PlaneEquations.size(); loop < PlaneEquations.count; loop++) {
					PlaneEquations.add(new PlaneEquation(bb, this));
				}
			}
		}

		@Override
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {
			
			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));			
			
			var bb = ByteBuffer.wrap(new byte[68+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(bb.position()+0x10);
			bb.putFloat(radius);
			bb.putInt(0);
			bb.putInt(0);
			bb.putInt(0);
			bb.putFloat(HalfExtentsX);
			bb.putFloat(HalfExtentsY);
			bb.putFloat(HalfExtentsZ);
			bb.putFloat(HalfExtentsW);
			bb.putFloat(CenterX);
			bb.putFloat(CenterY);
			bb.putFloat(CenterZ);
			bb.putFloat(CenterW);
			
			var rotatedVertices = new hkArray<FourVectors>();
			for (int i=0; i<Vertices.size(); i+=4) {
				//fill up the "rotatedvertices"
				Vertex v1 = Vertices.get(i);
				Vertex v2 = Vertices.size() > i+1 ? Vertices.get(i+1) : v1;
				Vertex v3 = Vertices.size() > i+2 ? Vertices.get(i+2) : v2;
				Vertex v4 = Vertices.size() > i+3 ? Vertices.get(i+3) : v3;
				rotatedVertices.add(new FourVectors(v1, v2, v3, v4));
			}
			rotatedVertices.saveObject(bb, contents.size());
			bb.putInt(Vertices.size());
			PlaneEquations.saveObject(bb, contents.size());
			bb.putInt(0); //pConnectivity
			contents.write(bb.array());

			rotatedVertices.saveArray(contents, fv -> {
				try {
					contents.write(fv.save());
				} catch (IOException e) {e.printStackTrace();}
			}, localFixUps);
			PlaneEquations.saveArray(contents, pe -> {
				try {
					contents.write(pe.save());
				} catch (IOException e) {e.printStackTrace();}
			}, localFixUps);
		}
		
		public byte[] save() throws IOException {
			
			var bb = ByteBuffer.wrap(new byte[68+0x10+0x0C]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(new byte[0x10]);
			bb.putFloat(radius);
			bb.put(new byte[0x0C]);
			bb.putFloat(HalfExtentsX);
			bb.putFloat(HalfExtentsY);
			bb.putFloat(HalfExtentsZ);
			bb.putFloat(HalfExtentsW);
			bb.putFloat(CenterX);
			bb.putFloat(CenterY);
			bb.putFloat(CenterZ);
			bb.putFloat(CenterW);
			bb.putInt(0);
			//40 + 0x1C
			bb.putShort((short) (Vertices.size()%4 == 0 ? Vertices.size()/4 : Vertices.size()/4+1)); //number of "rotatedvertices"
			bb.putShort((short) 0);
			bb.putShort((short) (Vertices.size()%4 == 0 ? Vertices.size()/4 : Vertices.size()/4+1)); //capacity
			bb.put((byte) 0);
			bb.put((byte) 192); // flags, yeah sure let it be 192
			bb.putInt(Vertices.size());
			bb.putInt(0);
			bb.putShort((short) PlaneEquations.size());
			bb.putShort((short) 0);
			bb.putShort((short) PlaneEquations.size()); //Capacity
			bb.put((byte) 0);
			bb.put((byte) 192); // flags
			bb.putInt(0);
			//68
			
			var data = new ByteArrayOutputStream();
			data.write(bb.array());
			bb = null;

//			for (var rv : RotatedVertices) data.write(rv.save());
			int i=0;
			for (i=0; i<Vertices.size(); i+=4) {
				//fill up the "rotatedvertices"
				Vertex v1 = Vertices.get(i);
				Vertex v2 = Vertices.size() > i+1 ? Vertices.get(i+1) : v1;
				Vertex v3 = Vertices.size() > i+2 ? Vertices.get(i+2) : v2;
				Vertex v4 = Vertices.size() > i+3 ? Vertices.get(i+3) : v3;
				
				var bb2 = ByteBuffer.wrap(new byte[48]);
				bb2.order(ByteOrder.LITTLE_ENDIAN);
				bb2.putFloat(v1.x);
				bb2.putFloat(v2.x);
				bb2.putFloat(v3.x);
				bb2.putFloat(v4.x);
				bb2.putFloat(v1.y);
				bb2.putFloat(v2.y);
				bb2.putFloat(v3.y);
				bb2.putFloat(v4.y);
				bb2.putFloat(v1.z);
				bb2.putFloat(v2.z);
				bb2.putFloat(v3.z);
				bb2.putFloat(v4.z);
				data.write(bb2.array());
			}
			
			
			for (var pe : PlaneEquations) data.write(pe.save());

			return data.toByteArray();
		}

		@Override
		public int CRC32() {return 0x371A2F2B;}

		@Override
		public void updateShape(double R, double G, double B, double A) {
			updateShape();
		}

		@Override
		public Group getDisplayShape() {
			return displayShape;
		}

		@Override
		public hkpShape deepCopy() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public class FourVectors{
		Vertex v1, v2, v3, v4;
		public FourVectors(ByteBuffer in) {
			v1 = new Vertex();
			v2 = new Vertex();
			v3 = new Vertex();
			v4 = new Vertex();
			v1.x = in.getFloat();
			v2.x = in.getFloat();
			v3.x = in.getFloat();
			v4.x = in.getFloat();
			v1.y = in.getFloat();
			v2.y = in.getFloat();
			v3.y = in.getFloat();
			v4.y = in.getFloat();
			v1.z = in.getFloat();
			v2.z = in.getFloat();
			v3.z = in.getFloat();
			v4.z = in.getFloat();
		}
		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[48]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(v1.x);
			bb.putFloat(v2.x);
			bb.putFloat(v3.x);
			bb.putFloat(v4.x);
			bb.putFloat(v1.y);
			bb.putFloat(v2.y);
			bb.putFloat(v3.y);
			bb.putFloat(v4.y);
			bb.putFloat(v1.z);
			bb.putFloat(v2.z);
			bb.putFloat(v3.z);
			bb.putFloat(v4.z);
			return bb.array();
		}
		public FourVectors(Vertex v12, Vertex v22, Vertex v32, Vertex v42) {
			v1 = v12;
			v2 = v22;
			v3 = v32;
			v4 = v42;
		}
	}
	
	public static class Vertex {

		public float x = 0;
		public float y = 0;
		public float z = 0;

		public hkpConvexVerticesShape convexVerticeShape;

		public Vertex() {}
		
		public Vertex(PlaneEquation p, String axis) {
			
			// ax + by + cz + d = 0
			// axe x : y et z fixés (ici égaux au centre de gravité)
			
			switch (axis) {
			case "x":
				y = p.convexVerticeShape.CenterY;
				z = p.convexVerticeShape.CenterZ;
				x = (-p.b*y - p.c*z -p.d)/p.a;
				break;
			case "y":
				x = p.convexVerticeShape.CenterX;
				z = p.convexVerticeShape.CenterZ;
				y = (-p.a*x - p.c*z -p.d)/p.b;
				break;
			case "z":
				x = p.convexVerticeShape.CenterX;
				y = p.convexVerticeShape.CenterY;
				z = (-p.a*x - p.b*y -p.d)/p.c;
				break;
			}
//			this.convexVerticeShape = p.convexVerticeShape;
		}

		public Vertex(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public double distanceTo(Vertex v) {
			return Math.sqrt((x+v.x)*(x+v.x) + (y+v.y)*(y+v.y) + (z+v.z)*(z+v.z));
			
		}
		
		public Vertex findOnPlaneBetweenVertices(PlaneEquation plane, Vertex B) { // this = vertex, B = barycenter
		    float dx = B.x - x, dy = B.y - y, dz = B.z - z;
		    float denom = plane.a * dx + plane.b * dy + plane.c * dz;

		    if (Math.abs(denom) < 1e-6) return null; // line parallel to plane which might happen

		    float t = -(plane.a * x + plane.b * y + plane.c * z + plane.d) / denom;
		    
		    if (t < 0 || t > 1) return null;
		    
		    return new Vertex(x + t * dx, y + t * dy, z + t * dz);
		}
		
		public static Vertex getCenter(List<Vertex> verts) {
		    float sx = 0, sy = 0, sz = 0;
		    int n = verts.size();
		    for (Vertex v : verts) {
		        sx += v.x;
		        sy += v.y;
		        sz += v.z;
		    }
		    return new Vertex(sx / n, sy / n, sz / n);
		}
		
		@Override
		public String toString() {
			return "Vertex x=" + x + ", y=" + y + ", z=" + z 
//					+ ", " + intersectedLines.size() + " intersected lines"
					;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(x);
			result = prime * result + Float.floatToIntBits(y);
			result = prime * result + Float.floatToIntBits(z);
			return result;
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
			return Collisions.similarEnough(this.x, other.x) && Collisions.similarEnough(this.y, other.y) && Collisions.similarEnough(this.z, other.z);
		}
	}
	
	public static class PlaneEquation {
		// ax + by + cz + d = 0
		
		public float a = 0.0f;
		public float b = 1.0f;
		public float c = 0.0f;
		public float d = 1.0f;
		
		public hkpConvexVerticesShape convexVerticeShape;

//		public ArrayList<LineEquation> containedLines = new ArrayList<>();
		public ArrayList<Vertex> containedVertices = new ArrayList<>();
		
		public double colorR = 0;
		public double colorG = 0;
		public double colorB = 0;
		
		public PlaneEquation() {
		}
		
		public PlaneEquation(hkpConvexVerticesShape v, float a, float b, float c, float d) {
			convexVerticeShape = v;
			colorR = Collisions.randomizeColor(v.getBound().colorR, 0.4);
			colorG = Collisions.randomizeColor(v.getBound().colorG, 0.4);
			colorB = Collisions.randomizeColor(v.getBound().colorB, 0.4);
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		public PlaneEquation(ByteBuffer bb, hkpConvexVerticesShape load) {
			this.a = bb.getFloat();
			this.b = bb.getFloat();
			this.c = bb.getFloat();
			this.d = bb.getFloat();
			this.convexVerticeShape = load;
		}
		
//		@SuppressWarnings("hiding")
//		public LineEquation intersect(PlaneEquation p) {
//			float a = this.a*p.c - p.a*this.c;
//			float b = this.b*p.c - p.b*this.c;
//			float c = this.d*p.c - p.d*this.c;
//			
//			for (LineEquation li : convexVerticeShape.LineEquations) {
//				if(Collisions.boundCenterCloseEnough(li.a, a) && Collisions.boundCenterCloseEnough(li.b, b) && Collisions.boundCenterCloseEnough(li.c, c)) {
//					// the if statements below should never be entered
//					if (!this.containedLines.contains(li)) this.containedLines.add(li);
//					if (!p.containedLines.contains(li)) p.containedLines.add(li);
//					if (!li.intersectedPlanes.contains(this)) li.intersectedPlanes.add(this);
//					if (!li.intersectedPlanes.contains(p)) li.intersectedPlanes.add(p);
//					return li;
//				}
//			}
//			
//			return new LineEquation(this, p);
//		}

		@Override
		public String toString() {
			return "\n    -PlaneEquation : " + a + "x + " + b + "y + " + c + " + " + d + " = 0";
		}

		public static void intersect3(PlaneEquation p1, PlaneEquation p2, PlaneEquation p3) {
			double[][] A = {
			        {p1.a, p1.b, p1.c},
			        {p2.a, p2.b, p2.c},
			        {p3.a, p3.b, p3.c}
			    };
			    
			    double[] D = {-p1.d, -p2.d, -p3.d};

			    double detA = det3x3(A);
			    if (Math.abs(detA) < 1e-6) return; // planes are parallel

			    double[][] Ax = { {D[0], A[0][1], A[0][2]}, {D[1], A[1][1], A[1][2]}, {D[2], A[2][1], A[2][2]} };
			    double[][] Ay = { {A[0][0], D[0], A[0][2]}, {A[1][0], D[1], A[1][2]}, {A[2][0], D[2], A[2][2]} };
			    double[][] Az = { {A[0][0], A[0][1], D[0]}, {A[1][0], A[1][1], D[1]}, {A[2][0], A[2][1], D[2]} };

			    double x = det3x3(Ax) / detA;
			    double y = det3x3(Ay) / detA;
			    double z = det3x3(Az) / detA;

			    var v = new Vertex((float)x, (float)y, (float)z);
			    p1.containedVertices.add(v);
			    p2.containedVertices.add(v);
			    p3.containedVertices.add(v);
			    
		}

		static double det3x3(double[][] m) {
		    return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
		         - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
		         + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
		}

		public byte[] save() {
			var bb = ByteBuffer.wrap(new byte[16]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(a);
			bb.putFloat(b);
			bb.putFloat(c);
			bb.putFloat(d);
			return bb.array();
		}


		public PlaneEquation deepCopy(hkpConvexVerticesShape v) {
			var copy = new PlaneEquation();
			copy.a = this.a;
			copy.b = this.b;
			copy.c = this.c;
			copy.d = this.d;
			copy.convexVerticeShape = v;
			copy.colorR = Collisions.randomizeColor(v.getBound().colorR, 0.4);
			copy.colorG = Collisions.randomizeColor(v.getBound().colorG, 0.4);
			copy.colorB = Collisions.randomizeColor(v.getBound().colorB, 0.4);
			return copy;
		}

		public PlaneEquation deepCopyOffset(hkpConvexVerticesShape v) {
			var copy = new PlaneEquation();
			copy.a = this.a;
			copy.b = this.b;
			copy.c = this.c;
			copy.d = this.d-0.05f;
			copy.convexVerticeShape = v;
			copy.colorR = Collisions.randomizeColor(v.getBound().colorR, 0.4);
			copy.colorG = Collisions.randomizeColor(v.getBound().colorG, 0.4);
			copy.colorB = Collisions.randomizeColor(v.getBound().colorB, 0.4);
			return copy;
		}
	}
	
	public class hkpCylinderShape implements hkpShape {
		public float radius = 0.05f;
		public float cylRadius, cylBaseRadiusFactorForHeightFieldCollisions;
		public hkVector4 v1, v2, perpendicular1, perpendicular2;

		public Shape3D displayShape = new javafx.scene.shape.Cylinder(0.5,1);
		//public scgBounds bound = null;
		
		public hkpCylinderShape() {}

		public hkpCylinderShape(ByteBuffer bb) {
			bb.position(bb.position() + 0x10);
			radius = bb.getFloat();
			cylRadius = bb.getFloat();
			cylBaseRadiusFactorForHeightFieldCollisions = bb.getFloat();
			bb.position(bb.position() + 4);
			v1 = new hkVector4(bb);
			v2 = new hkVector4(bb);
			perpendicular1 = new hkVector4(bb);
			perpendicular2 = new hkVector4(bb);
		}

		@Override
		public void save(ByteArrayOutputStream contents, ArrayList<LocalFixUp> localFixUps,
				ArrayList<VirtualFixUp> virtualFixUps) throws IOException {
			
			virtualFixUps.add(new VirtualFixUp(contents.size(), CRC32()));			
			var bb = ByteBuffer.wrap(new byte[96]);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(bb.position()+0x10);
			bb.putFloat(radius);
			bb.putFloat(cylRadius);
			bb.putFloat(cylBaseRadiusFactorForHeightFieldCollisions);
			bb.putInt(0);
			bb.put(v1.save());
			bb.put(v2.save());
			bb.put(perpendicular1.save());
			bb.put(perpendicular2.save());
			contents.write(bb.array());
		}
		
		@Override
		public void updateShape(double colorR, double colorG, double colorB, double d) {
			this.displayShape.setScaleX(cylRadius*2);
			this.displayShape.setScaleY(v1.Y*2);
			this.displayShape.setScaleZ(cylRadius*2);
			this.displayShape.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, d)));
		}
		
		@Override
		public int CRC32() {return 0x27F89FED;}

		@Override
		public javafx.scene.Node getDisplayShape() {
			return displayShape;
		}

		@Override
		public hkpShape deepCopy() {
			var copy = new hkpCylinderShape();
			copy.radius = radius;
			copy.cylBaseRadiusFactorForHeightFieldCollisions = this.cylBaseRadiusFactorForHeightFieldCollisions;
			copy.cylRadius = this.cylRadius;
			copy.v1 = new hkVector4(v1);
			copy.v2 = new hkVector4(v2);
			copy.perpendicular1 = new hkVector4(perpendicular1);
			copy.perpendicular2 = new hkVector4(perpendicular2);
			return copy;
		}
	}
	
	public static class hkVector4{
		public float X,Y,Z,W;
		public hkVector4(ByteBuffer in) {
			X = in.getFloat();
			Y = in.getFloat();
			Z = in.getFloat();
			W = in.getFloat();
		}
		public hkVector4(hkVector4 v1) {
			X = v1.X;
			Y = v1.Y;
			Z = v1.Z;
			W = v1.W;
		}
		public byte[] save() {
			var bb = ByteBuffer.allocate(16);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(X);
			bb.putFloat(Y);
			bb.putFloat(Z);
			bb.putFloat(W);
			return bb.array();
		}
	}
	
	
	
	
	
	public static int align(int i) {
		while (i%16 != 0) i++;
		return i;
	}
	
	public static boolean boundCenterCloseEnough(float a, float b) {
		return (Math.abs(a-b) < floatingPointError);
	}
	
	public boolean expandTreeToItem(TreeView<scgBounds> treeView, TreeItem<scgBounds> item, scgBounds scgBounds) {
		if (item.getValue() == scgBounds) {
			treeView.getSelectionModel().select(item);
			return true;
		}
		
		for (var c : item.getChildren()) {
			if (expandTreeToItem(treeView, c, scgBounds)) {
				item.setExpanded(true);
				return true;
			}
		}
		return false;
	}

	public static boolean similarEnough(float a, float b) {
		return (Math.abs(a-b) < 0.05);
	}
	
	public static double randomizeColor(double color, double factor) {
		color +=  (Math.random() - 0.5)*factor;
		if (color < 0) return 0;
		if (color > 1) return 1;
		return color;
	}


//	@Override
//	public String toString() {
//		return "Collisions [carname=" + carname + ", isResolved=" + isResolved + ", mainBound=" + mainBound
//				+ ", \nX=" + X + ", Y=" + Y + ", Z=" + Z + ", W=" + W
//				+ ", LocalFixUps=" + LocalFixUps + ", VirtualFixUps=" + VirtualFixUps + "]";
//	}
	
//	public void printInfo() {
//		System.out.println(" === COLLISIONS === ");
//		System.out.println("Car name: " + carname + ", isResolved: " + isResolved+ " | (" + X + ", " + Y + ", " + Z + ", " + W+")");
//		System.out.println(" --- BOUNDS --- ");
//		mainBound.getChildrenRecursively().forEach(b -> System.out.println(b));
//		System.out.println(" --- LOCAL FIXUPS --- ");
//		LocalFixUps.forEach(f -> System.out.println(f));
//		System.out.println(" --- VIRTUAL FIXUPS --- ");
//		VirtualFixUps.forEach(f -> System.out.println(f));
//	}

}