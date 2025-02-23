package fr.ni240sx.ucgt.collisionsEditor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionBoxShape;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTransform;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTranslate;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionSphereShape;
import fr.ni240sx.ucgt.collisionsEditor.CollisionMesh.CollisionConvexVertice;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Collisions {
	
	public static double floatingPointError = 0.1;

	public String carname = "UNKNOWN";

	public boolean isResolved = false;
	
	// bin block ID for collisions 01b90300

	//proper bounds sorting
	public CollisionBound mainBound = null;
	
	public float X = 0;
	public float Y = 0;
	public float Z = 0;
	public float W = 0;

	public ArrayList<LocalFixUp> LocalFixUps = new ArrayList<>();
	public ArrayList<VirtualFixUp> VirtualFixUps = new ArrayList<>();
	
//	public static ArrayList<Hash> commonCollisionHashes = Hash.loadHashes(new File("data/collisionshashes"));
	
	public Collisions() {
		Hash.addBinHashes(new File("data/collisionshashes"));
		Hash.addVltHashes(new File("data/collisionshashes"));
	}

	public Collisions(File file) {
		Hash.addBinHashes(new File("data/collisionshashes"));
		Hash.addVltHashes(new File("data/collisionshashes"));
		
		//Binary's shit structure
		int NumberOfBounds = 0;
		int NumberOfBoxShapes = 0;
		int NumberOfConvexTransformShapes = 0;
		int NumberOfConvexTranslateShapes = 0;
		int NumberOfConvexVerticesShapes = 0;
		int NumberOfSphereShapes = 0;
		int NumberOfLocalFixUps = 0;
		int NumberOfVirtualFixUps = 0;
		
//		ArrayList<CollisionBound> bounds = new ArrayList<>();
		ArrayList<CollisionBoxShape> boxShapes = new ArrayList<>();
		ArrayList<CollisionConvexTransform> convexTransformShapes = new ArrayList<>();
		ArrayList<CollisionConvexTranslate> convexTranslateShapes = new ArrayList<>();
		ArrayList<CollisionConvexVertice> convexVerticesShapes = new ArrayList<>();
		ArrayList<CollisionSphereShape> sphereShapes = new ArrayList<>();
		
		
		
		//File f = new File("...");
		try {
			var fis = new FileInputStream(file);
			byte [] fileToBytes = new byte[(int)file.length()];
			fis.read(fileToBytes);
			fis.close();
			
			ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(36);
			this.carname = Hash.addVltHash(Hash.addBinHash(readString(bb)));
			NumberOfBounds = bb.getInt();
			NumberOfBoxShapes = bb.getInt();
			NumberOfConvexTransformShapes = bb.getInt();
			NumberOfConvexTranslateShapes = bb.getInt();
			NumberOfConvexVerticesShapes = bb.getInt();
			NumberOfSphereShapes = bb.getInt();
			NumberOfLocalFixUps = bb.getInt();
			NumberOfVirtualFixUps = bb.getInt();

			
			
			
			mainBound = new CollisionBound(bb);
			while (mainBound.getChildrenRecursively().size() < NumberOfBounds) mainBound.childBounds.add(new CollisionBound(bb));
			
			for(int i =0; i<NumberOfBoxShapes; i++) {
				boxShapes.add(CollisionBoxShape.load(bb));
			}
			for(int i =0; i<NumberOfConvexTransformShapes; i++) {
				convexTransformShapes.add(CollisionConvexTransform.load(bb));
			}
			for(int i =0; i<NumberOfConvexTranslateShapes; i++) {
				convexTranslateShapes.add(CollisionConvexTranslate.load(bb));
			}
			for(int i =0; i<NumberOfConvexVerticesShapes; i++) {
				convexVerticesShapes.add(CollisionConvexVertice.load(bb));
			}
			for(int i =0; i<NumberOfSphereShapes; i++) {
				sphereShapes.add(CollisionSphereShape.load(bb));
			}
			for(int i =0; i<NumberOfLocalFixUps; i++) {
				this.LocalFixUps.add(new LocalFixUp(bb));
			}
			for(int i =0; i<NumberOfVirtualFixUps; i++) {
				this.VirtualFixUps.add(new VirtualFixUp(bb));
			}
			
			
//			for (int i=0; i<partsAmount; i++) { 
//				if (CollisionsEditor.debug)System.out.println("something idk");
//			}			
//			
//			//at the very end, find the correct offset
			this.isResolved = bb.get()==1;
			this.X = bb.getFloat();
			this.Y = bb.getFloat();
			this.Z = bb.getFloat();
			this.W = bb.getFloat();

//			System.out.println("Number of local fix ups: "+LocalFixUps.size());
//			System.out.println("Number of virtual fix ups: "+VirtualFixUps.size());

			//boxshapes + convexverticeshapes + sphereshapes = childbounds
			//a child bound is either convex vertice or box (or sphere/no geometry but rare)
			//now gotta associate shit with each other
			
			int ibox=0,imesh=0, isphere=0;
			for (CollisionBound b : mainBound.getChildrenRecursively()) {
				switch (b.Shape) {
				case KSHAPE_BOX:
					b.collisionShape = boxShapes.get(ibox);
					boxShapes.get(ibox).bound = b;
					ibox++;
					break;
				case KSHAPE_CYLINDER:
					break;
				case KSHAPE_INVALID:
					break;
				case KSHAPE_MESH:
					b.collisionShape = convexVerticesShapes.get(imesh);
					convexVerticesShapes.get(imesh).bound = b;
					convexVerticesShapes.get(imesh).PlaneEquations.forEach(p -> {
						p.colorR = randomizeColor(b.colorR, 0.4);
						p.colorG = randomizeColor(b.colorG, 0.4);
						p.colorB = randomizeColor(b.colorB, 0.4);
					});
					imesh++;
					break;
				case KSHAPE_SPHERE:
					b.collisionShape = sphereShapes.get(isphere);
					sphereShapes.get(isphere).bound = b;
					isphere++;
					break;
				case KSHAPE_TYPE_COUNT:
					break;
				}
			
				//transforms
				//transforms + translates = boxshapes
				for(CollisionConvexTranslate t : convexTranslateShapes) {
					if (boundCenterCloseEnough(b.PositionX, t.TranslationX)
							&& boundCenterCloseEnough(b.PositionY, t.TranslationY)
							&& boundCenterCloseEnough(b.PositionZ, t.TranslationZ)) {
						b.shapeTranslate = t;
					}
				}
				for(CollisionConvexTransform t : convexTransformShapes) {
					if (boundCenterCloseEnough(b.PositionX, t.TranslationX)
							&& boundCenterCloseEnough(b.PositionY, t.TranslationY)
							&& boundCenterCloseEnough(b.PositionZ, t.TranslationZ)
							&& b.OrientationX != 0) {
						b.shapeTransform = t;
					}
				}
				
				
			}
			
			
			
			
		} catch (FileNotFoundException e) {
			//dbmp to load not found
			try {
				new Alert(Alert.AlertType.ERROR, "File not found", ButtonType.OK).show();
			} catch (Exception e2) {
				System.out.println("File not found.");
			}
//			e.printStackTrace();
		} catch (@SuppressWarnings("unused") NullPointerException e) {
//			e.printStackTrace();
			//no file selected by the user
		} catch (Exception e) {
			e.printStackTrace();
			try {
				new Alert(Alert.AlertType.ERROR, "Error while loading file", ButtonType.OK).show();
			} catch (Exception e2) {
				System.out.println("Error while loading file.");
			}
		}
	}

	public void saveToFile(File f) throws IOException {

		ArrayList<CollisionBoxShape> boxShapes = new ArrayList<>();
		ArrayList<CollisionConvexVertice> convexVerticesShapes = new ArrayList<>();
		ArrayList<CollisionSphereShape> sphereShapes = new ArrayList<>();
		
		ArrayList<CollisionConvexTransform> convexTransformShapes = new ArrayList<>();
		ArrayList<CollisionConvexTranslate> convexTranslateShapes = new ArrayList<>();
		
		for (var b : mainBound.getChildrenRecursively()) {
			// assuming the enum is kept updated in case the shape changes
			switch (b.Shape) {
			case KSHAPE_BOX:
				boxShapes.add((CollisionBoxShape) b.collisionShape);
				break;
			case KSHAPE_CYLINDER:
				break;
			case KSHAPE_INVALID:
				break;
			case KSHAPE_MESH:
				convexVerticesShapes.add((CollisionConvexVertice) b.collisionShape);
				break;
			case KSHAPE_SPHERE:
				sphereShapes.add((CollisionSphereShape) b.collisionShape);
				break;
			case KSHAPE_TYPE_COUNT:
				break;
			}
			if (b.shapeTransform != null) convexTransformShapes.add(b.shapeTransform);
			if (b.shapeTranslate != null) convexTranslateShapes.add(b.shapeTranslate);
		}
		
		ByteBuffer header = ByteBuffer.wrap(new byte[36 + carname.length()+1 + 32]);
		header.order(ByteOrder.LITTLE_ENDIAN);

		header.putInt(1114656103); //blockID - binary serialized
		header.putInt(-1); 
		header.putInt(6);
		header.putInt(771611838); // sub block (probably collisions)
		header.putInt(-1);
		header.putInt(1465336146);
		header.putInt(4097);
		header.putInt(-1);
		header.putInt(-1);
		//byte 36
		writeString(carname, header);
		header.putInt(mainBound.getChildrenRecursively().size()); //NumberOfBounds
		header.putInt(boxShapes.size());
		header.putInt(convexTransformShapes.size());
		header.putInt(convexTranslateShapes.size());
		header.putInt(convexVerticesShapes.size());
		header.putInt(sphereShapes.size());
		header.putInt(LocalFixUps.size());
		header.putInt(VirtualFixUps.size());

		var data = new ByteArrayOutputStream();
		data.write(mainBound.saveHierarchy());
		for (var b : boxShapes) data.write(b.save());
		for (var b : convexTransformShapes) data.write(b.save());
		for (var b : convexTranslateShapes) data.write(b.save());
		for (var b : convexVerticesShapes) data.write(b.save());
		for (var b : sphereShapes) data.write(b.save());
		for (var d : LocalFixUps) data.write(d.save());
		for (var d : VirtualFixUps) data.write(d.save());
			
		data.write(this.isResolved ? 1 : 0);
		
		var bb = ByteBuffer.wrap(new byte[16]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat(X);
		bb.putFloat(Y);
		bb.putFloat(Z);
		bb.putFloat(W);
		
		data.write(bb.array());
		bb = null;
		
		var dataArray = data.toByteArray();
		data = null;
		var totalFileSize = header.capacity() + dataArray.length;

		header.putInt(4, totalFileSize-8);
		header.putInt(16, totalFileSize-20);
		header.putInt(28, totalFileSize-36);
		header.putInt(32, totalFileSize-20);
		
		var fos = new FileOutputStream(f);
		fos.write(header.array());
		fos.write(dataArray);
		fos.close();
	
	}

	public static boolean boundCenterCloseEnough(float a, float b) {
		return (Math.abs(a-b) < floatingPointError);
	}
	public static boolean similarEnough(float a, float b) {
		return (Math.abs(a-b) < 0.05);
	}
	
	public static String readString(ByteBuffer bb) {
		byte[] stringBytesOversize = new byte[64];
		byte b;
		int i = 0;
		while ((b=bb.get())!=0) {
			stringBytesOversize[i] = b;
			i++;
		}
		return new String(Arrays.copyOf(stringBytesOversize, i));
	}
	
	public static void writeString(String s, ByteBuffer bb) {
		bb.put(s.getBytes());
		bb.put((byte)0);
	}
	
	public static double randomizeColor(double color, double factor) {
		color +=  (Math.random() - 0.5)*factor;
		if (color < 0) return 0;
		if (color > 1) return 1;
		return color;
	}

	@Override
	public String toString() {
		return "Collisions [carname=" + carname + ", isResolved=" + isResolved + ", mainBound=" + mainBound
				+ ", \nX=" + X + ", Y=" + Y + ", Z=" + Z + ", W=" + W
				+ ", LocalFixUps=" + LocalFixUps + ", VirtualFixUps=" + VirtualFixUps + "]";
	}
	
	public void printInfo() {
		System.out.println(" === COLLISIONS === ");
		System.out.println("Car name: " + carname + ", isResolved: " + isResolved+ " | (" + X + ", " + Y + ", " + Z + ", " + W+")");
		System.out.println(" --- BOUNDS --- ");
		mainBound.getChildrenRecursively().forEach(b -> System.out.println(b));
		System.out.println(" --- LOCAL FIXUPS --- ");
		LocalFixUps.forEach(f -> System.out.println(f));
		System.out.println(" --- VIRTUAL FIXUPS --- ");
		VirtualFixUps.forEach(f -> System.out.println(f));
	}

}