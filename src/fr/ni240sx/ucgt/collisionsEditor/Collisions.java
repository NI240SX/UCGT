package fr.ni240sx.ucgt.collisionsEditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	public static double floatingPointError = 0.001;

	public Hash carname = new Hash("UNKNOWN");

	public boolean isResolved = false;
	
//	public int NumberOfBounds = 0;
//	public int NumberOfBoxShapes = 0;
//	public int NumberOfConvexTransformShapes = 0;
//	public int NumberOfConvexTranslateShapes = 0;
//	public int NumberOfConvexVerticesShapes = 0;
//	public int NumberOfSphereShapes = 0;
	
//	public ArrayList<CollisionBound> bounds = new ArrayList<CollisionBound>();
//	public ArrayList<CollisionBoxShape> boxShapes = new ArrayList<CollisionBoxShape>();
//	public ArrayList<CollisionConvexTransform> convexTransformShapes = new ArrayList<CollisionConvexTransform>();
//	public ArrayList<CollisionConvexTranslate> convexTranslateShapes = new ArrayList<CollisionConvexTranslate>();
//	public ArrayList<CollisionConvexVertice> convexVerticesShapes = new ArrayList<CollisionConvexVertice>();
//	public ArrayList<CollisionSphereShape> sphereShapes = new ArrayList<CollisionSphereShape>();

	//proper bounds sorting
	public CollisionBound mainBound = new CollisionBound();
	public ArrayList<CollisionBound> childBounds = new ArrayList<CollisionBound>();
	
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

	public Collisions(File file) {
		//Binary's shit structure
		int NumberOfBounds = 0;
		int NumberOfBoxShapes = 0;
		int NumberOfConvexTransformShapes = 0;
		int NumberOfConvexTranslateShapes = 0;
		int NumberOfConvexVerticesShapes = 0;
		int NumberOfSphereShapes = 0;
		
		ArrayList<CollisionBound> bounds = new ArrayList<CollisionBound>();
		ArrayList<CollisionBoxShape> boxShapes = new ArrayList<CollisionBoxShape>();
		ArrayList<CollisionConvexTransform> convexTransformShapes = new ArrayList<CollisionConvexTransform>();
		ArrayList<CollisionConvexTranslate> convexTranslateShapes = new ArrayList<CollisionConvexTranslate>();
		ArrayList<CollisionConvexVertice> convexVerticesShapes = new ArrayList<CollisionConvexVertice>();
		ArrayList<CollisionSphereShape> sphereShapes = new ArrayList<CollisionSphereShape>();
		
		
		
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
			this.carname = new Hash(readString(bb));
			NumberOfBounds = bb.getInt();
			NumberOfBoxShapes = bb.getInt();
			NumberOfConvexTransformShapes = bb.getInt();
			NumberOfConvexTranslateShapes = bb.getInt();
			NumberOfConvexVerticesShapes = bb.getInt();
			NumberOfSphereShapes = bb.getInt();
			this.NumberOfLocalFixUps = bb.getInt();
			this.NumberOfVirtualFixUps = bb.getInt();

			
			
			
			for(int i =0; i<NumberOfBounds; i++) {
				bounds.add(CollisionBound.load(bb));
			}
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
			for(int i =0; i<this.NumberOfLocalFixUps; i++) {
				this.LocalFixUps.add(new LocalFixUp(bb));
			}
			for(int i =0; i<this.NumberOfVirtualFixUps; i++) {
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
			
			
			
			//hierarchy detection
			for (CollisionBound b : bounds) {
				if (b.NumberOfChildren!=0 && b.RenderHierarchyIndex==0) {
					this.mainBound = b;
					System.out.println("Main bound detected : \n" + b+"\n");
				} else {
					this.childBounds.add(b);
				}
			}
			
			//TODO rule out sub-hierarchy (expl joints on trailers that have a main joint and two child ones)

			//boxshapes + convexverticeshapes + sphereshapes = childbounds
			//a child bound is either convex vertice or box (or sphere/no geometry but rare)
			//now gotta associate shit with each other
			for (CollisionBound b : this.childBounds) {
				switch(b.Shape) {
				case KSHAPE_BOX:
					for(CollisionBoxShape s : boxShapes) {
						if (similarEnough(b.HalfDimensionX/2, s.HalfExtentsX)
								&& similarEnough(b.HalfDimensionY/2,s.HalfExtentsY)
								&& similarEnough(b.HalfDimensionZ/2,s.HalfExtentsZ)) {
							b.collisionShape = s;
						}
					}
					if (b.collisionShape==null) System.out.println("Warning : boxShape not found for " + b);
					break;
				case KSHAPE_CYLINDER:
					break;
				case KSHAPE_INVALID:
					break;
				case KSHAPE_MESH:
					for(CollisionConvexVertice s : convexVerticesShapes) {
						if (similarEnough(b.PositionX, s.CenterX)
								&& similarEnough(b.PositionY, s.CenterY)
								&& similarEnough(b.PositionZ, s.CenterZ)) {
							b.collisionShape = s;
						}
					}
					if (b.collisionShape==null) System.out.println("Warning : convexVerticesShape not found for " + b);
					break;
				case KSHAPE_SPHERE:
					for(CollisionSphereShape s : sphereShapes) {
						if (similarEnough(b.HalfDimensionX, s.unknownFloat)
								&& similarEnough(b.HalfDimensionY, s.unknownFloat)
								&& similarEnough(b.HalfDimensionZ, s.unknownFloat)) {
							b.collisionShape = s;
						}
					}
					if (b.collisionShape==null) System.out.println("Warning : sphereShape not found for " + b);
					break;
				case KSHAPE_TYPE_COUNT:
					break;
				default:
					break;				
				}
				
				//transforms
				//transforms + translates = boxshapes
				for(CollisionConvexTranslate t : convexTranslateShapes) {
					if (similarEnough(b.PositionX, t.TranslationX)
							&& similarEnough(b.PositionY, t.TranslationY)
							&& similarEnough(b.PositionZ, t.TranslationZ)) {
						b.shapeTranslate = t;
					}
				}
				for(CollisionConvexTransform t : convexTransformShapes) {
					if (similarEnough(b.PositionX, t.TranslationX)
							&& similarEnough(b.PositionY, t.TranslationY)
							&& similarEnough(b.PositionZ, t.TranslationZ)
							&& b.OrientationX != 0) {
						b.shapeTransform = t;
					}
				}
				
				
			}
			
			
			
			
		} catch (FileNotFoundException e) {
			//dbmp to load not found
			// TODO Auto-generated catch block
			new Alert(Alert.AlertType.ERROR, "File not found", ButtonType.OK).show();
			e.printStackTrace();
		} catch (NullPointerException e) {
//			e.printStackTrace();
			//no file selected by the user
		} catch (Exception e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Error while loading file", ButtonType.OK).show();
		}
	}

	public void saveToFile(File f) {
		// TODO Auto-generated method stub
		
	}
	
	public static boolean similarEnough(float a, float b) {
		return (Math.abs(a-b) < floatingPointError);
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

	@Override
	public String toString() {
		return "Collisions [carname=" + carname + ", isResolved=" + isResolved + ", mainBound=" + mainBound
				+ ", \nchildBounds=" + childBounds + ", \nX=" + X + ", Y=" + Y + ", Z=" + Z + ", W=" + W
				+ ", \nNumberOfLocalFixUps=" + NumberOfLocalFixUps + ", NumberOfVirtualFixUps=" + NumberOfVirtualFixUps
				+ ", LocalFixUps=" + LocalFixUps + ", VirtualFixUps=" + VirtualFixUps + "]";
	}
}
