package collisionsEditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import binstuff.Hash;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.shape.Shape;

public class Collisions {

	public Hash carname = new Hash("UNKNOWN");

	public boolean isResolved = false;
	
	public int NumberOfBounds = 0;
	public int NumberOfBoxShapes = 0;
	public int NumberOfConvexTransformShapes = 0;
	public int NumberOfConvexTranslateShapes = 0;
	public int NumberOfConvexVerticesShapes = 0;
	public int NumberOfSphereShapes = 0;
	
	public ArrayList<CollisionBound> bounds = new ArrayList<CollisionBound>();
	public ArrayList<CollisionBoxShape> boxShapes = new ArrayList<CollisionBoxShape>();
	public ArrayList<CollisionConvexTransform> convexTransformShapes = new ArrayList<CollisionConvexTransform>();
	public ArrayList<CollisionConvexTranslate> convexTranslateShapes = new ArrayList<CollisionConvexTranslate>();
	public ArrayList<CollisionConvexVertice> convexVerticesShapes = new ArrayList<CollisionConvexVertice>();
	public ArrayList<CollisionSphereShape> sphereShapes = new ArrayList<CollisionSphereShape>();

	public float X = 0;
	public float Y = 0;
	public float Z = 0;
	public float W = 0;

	public int NumberOfLocalFixUps = 0;
	public int NumberOfVirtualFixUps = 0;

	public ArrayList<LocalFixUp> LocalFixUps = new ArrayList<LocalFixUp>();
	public ArrayList<VirtualFixUp> VirtualFixUps = new ArrayList<VirtualFixUp>();
	
	public static ArrayList<Hash> commonCollisionHashes = Hash.loadHashes(new File("data/collisionshashes"));
	
	public Collisions() {
		// TODO Auto-generated constructor stub
	}

	public static Collisions load(File file) {
		// TODO Auto-generated method stub
		if (file == null) return null;
		
		Collisions loadCol = null;
		
		//File f = new File("...");
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			byte [] fileToBytes = new byte[(int)file.length()];
			fis.read(fileToBytes);
			fis.close();
			
			ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.position(36);
			loadCol = new Collisions(); //reads the plain text car name
			loadCol.carname = new Hash(readString(bb));
			loadCol.NumberOfBounds = bb.getInt();
			loadCol.NumberOfBoxShapes = bb.getInt();
			loadCol.NumberOfConvexTransformShapes = bb.getInt();
			loadCol.NumberOfConvexTranslateShapes = bb.getInt();
			loadCol.NumberOfConvexVerticesShapes = bb.getInt();
			loadCol.NumberOfSphereShapes = bb.getInt();
			loadCol.NumberOfLocalFixUps = bb.getInt();
			loadCol.NumberOfVirtualFixUps = bb.getInt();

			
			
			
			for(int i =0; i<loadCol.NumberOfBounds; i++) {
				loadCol.bounds.add(CollisionBound.load(bb));
			}
			for(int i =0; i<loadCol.NumberOfBoxShapes; i++) {
				loadCol.boxShapes.add(CollisionBoxShape.load(bb));
			}
			for(int i =0; i<loadCol.NumberOfConvexTransformShapes; i++) {
				loadCol.convexTransformShapes.add(CollisionConvexTransform.load(bb));
			}
			for(int i =0; i<loadCol.NumberOfConvexTranslateShapes; i++) {
				loadCol.convexTranslateShapes.add(CollisionConvexTranslate.load(bb));
			}
			for(int i =0; i<loadCol.NumberOfConvexVerticesShapes; i++) {
				loadCol.convexVerticesShapes.add(CollisionConvexVertice.load(bb));
			}
			for(int i =0; i<loadCol.NumberOfSphereShapes; i++) {
				loadCol.sphereShapes.add(CollisionSphereShape.load(bb));
			}
			for(int i =0; i<loadCol.NumberOfLocalFixUps; i++) {
				loadCol.LocalFixUps.add(new LocalFixUp(bb));
			}
			for(int i =0; i<loadCol.NumberOfVirtualFixUps; i++) {
				loadCol.VirtualFixUps.add(new VirtualFixUp(bb));
			}
			
			
//			for (int i=0; i<partsAmount; i++) { 
//				if (CollisionsEditor.debug)System.out.println("something idk");
//			}			
//			
//			//at the very end, find the correct offset
			loadCol.isResolved = bb.get()==1;
			loadCol.X = bb.getFloat();
			loadCol.Y = bb.getFloat();
			loadCol.Z = bb.getFloat();
			loadCol.W = bb.getFloat();
			
		} catch (FileNotFoundException e) {
			//dbmp to load not found
			// TODO Auto-generated catch block
			new Alert(Alert.AlertType.ERROR, "File not found", ButtonType.OK).show();
			e.printStackTrace();
			loadCol = null;
		} catch (Exception e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Error while loading file", ButtonType.OK).show();
			loadCol = null;
		}
		return loadCol;
	}

	public void saveToFile(File f) {
		// TODO Auto-generated method stub
		
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
	
	public String toString() {
		return ("Collisions (car : "+this.carname.label+")\n"+this.NumberOfBounds+" bounds, "+this.NumberOfBoxShapes+" boxShapes, "+this.NumberOfConvexTransformShapes+" convexTransformShapes, "
				+this.NumberOfConvexTranslateShapes+" convexTranslateShapes, "+this.NumberOfConvexVerticesShapes+" convexVerticeShapes, "+this.NumberOfSphereShapes
				+" sphereShapes\nResolved : "+this.isResolved+", X="+this.X+", Y="+this.Y+", Z="+this.Z+", W="+this.W
				+"\nBounds : "+this.bounds
				+"\nBoxShapes : "+this.boxShapes
				+"\nConvTransforms : "+this.convexTransformShapes
				+"\nConvTranslates : "+this.convexTranslateShapes
				+"\nConvVertices : "+this.convexVerticesShapes
				+"\nSphereShapes : "+this.sphereShapes);
	}
}
