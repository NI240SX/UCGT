package fr.ni240sx.ucgt.collisionsEditor.CollisionMesh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.CollisionBound;
import fr.ni240sx.ucgt.collisionsEditor.Collisions;
import fr.ni240sx.ucgt.collisionsEditor.CollisionsEditor;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionShape;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

public class CollisionConvexVertice extends CollisionShape {

	public float CenterX = 0;
	public float CenterY = 0;
	public float CenterZ = 0;
	public float CenterW = 0;

	public float HalfExtentsX = 0;
	public float HalfExtentsY = 0;
	public float HalfExtentsZ = 0;
	public float HalfExtentsW = 0;

//	public int NumberOfPlaneEquations = 0;
//	public int NumberOfRotatedVertices = 0;
//	public int NumVertices = 0;
	
	public float boundDistance = (float) 0.05;

	public ArrayList<PlaneEquation> PlaneEquations = new ArrayList<>();
//	public ArrayList<LineEquation> LineEquations = new ArrayList<>();
	public ArrayList<Vertex> Vertices = new ArrayList<>();
	
//	public ArrayList<RotatedVertice> RotatedVertices = new ArrayList<>();
	
	public Group displayShape = new Group();
	private ArrayList<Shape3D> shapes = new ArrayList<>();
	public CollisionBound bound = null;

//	public static float carHalfWidth = 0f;
//	public static float carHalfLength = 0f;
//	public static float carHalfHeight = 0f;
	
	public CollisionConvexVertice() {
	}

	public CollisionConvexVertice(CollisionBound bound2) {
		bound = bound2;
		PlaneEquations.add(new PlaneEquation(this, 0.0f, 1.0f, 0.0f, 0.1f));
		PlaneEquations.add(new PlaneEquation(this, 0.0f, -1.0f, 0.0f, 0.1f));
		PlaneEquations.add(new PlaneEquation(this, 1.0f, 0.0f, 0.0f, 0.1f));
		PlaneEquations.add(new PlaneEquation(this, -1.0f, 0.0f, 0.0f, 0.1f));
		PlaneEquations.add(new PlaneEquation(this, 0.0f, 0.0f, 1.0f, 0.1f));
		PlaneEquations.add(new PlaneEquation(this, 0.0f, 0.0f, -1.0f, 0.1f));
//		updateShape();
	}

	@Override
	public String toString() {
		return "\n -CollisionConvexVertice [CenterX=" + CenterX + ", CenterY=" + CenterY + ", CenterZ=" + CenterZ
				+ ", CenterW=" + CenterW + ", HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY
				+ ", HalfExtentsZ=" + HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW 
//				+ ", NumberOfPlaneEquations=" + NumberOfPlaneEquations + ", NumberOfRotatedVertices=" + NumberOfRotatedVertices 
//				+ ", NumVertices=" + NumVertices 
				+ ", unknownFloat=" + boundDistance + ",\n PlaneEquations=" + PlaneEquations
//				+ ",\n RotatedVertices=" + RotatedVertices 
				+ "]";
	}
	
	static void sortForRender(ArrayList<Vertex> facePoints, PlaneEquation p) {
	    if (facePoints.size() < 3) return; //not enough vertices

	    
	    // ax + by + cz + d = 0 => x', y'
	    // z = (ax+by+d)/c
	    
	    
	    // U = -b, a, 0
	    // V = c, 0, -a
	    
//   	    double v = (P.x - centre.x) * base.V.x + (P.y - centre.y) * base.V.y + (P.z - centre.z) * base.V.z;	    
//	    double u = (P.x - centre.x) * base.U.x + (P.y - centre.y) * base.U.y + (P.z - centre.z) * base.U.z;

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
			
//			sortForRender(valid, p);
			
			TriangleMesh planeMesh = new TriangleMesh();
			planeMesh.getTexCoords().addAll(0, 0);
			
			for (var v : valid) {
	            planeMesh.getPoints().addAll(v.x, v.y, v.z);
	            this.Vertices.add(v);
//	            if (valid.indexOf(v)>2) {
//			        planeMesh.getFaces().addAll(
//			                planeMesh.getPoints().size()/3-3, 0,  
//			                planeMesh.getPoints().size()/3-2, 0,  
//			                planeMesh.getPoints().size()/3-1, 0);
//	            }
			}
//			for (int i=2; i<valid.size(); i++) {
//		        planeMesh.getFaces().addAll(
//		                planeMesh.getPoints().size()/3-valid.size()+i-2, 0,  
//		                planeMesh.getPoints().size()/3-valid.size()+i-1, 0,  
//		                planeMesh.getPoints().size()/3-valid.size()+i, 0);
//				
//			}
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
//				((PhongMaterial)shape.getMaterial()).setDiffuseColor(Color.color(1.0, 0.0, 0.0));
//				((PhongMaterial)shape.getMaterial()).setSpecularPower(0.0);
//		        shape.setMaterial(new PhongMaterial(Color.color(1.0, 0.0, 0.0)));
		        shape.requestFocus();
			});
			shape.setOnMouseExited(e -> {
//				((PhongMaterial)shape.getMaterial()).setDiffuseColor(Color.color(p.colorR, p.colorG, p.colorB));
//		        shape.setMaterial(new PhongMaterial(Color.color(p.colorR, p.colorG, p.colorB)));
			});
			shape.setOnMousePressed(e -> {
//				((PhongMaterial)shape.getMaterial()).setDiffuseColor(Color.color(1.0, 0.0, 0.0));
				CollisionsEditor.planeControls.setPlane(p);
//				this.updateShape();
			});
			
			shape.setOnContextMenuRequested(e -> {
				var cm = new ContextMenu();
				
				this.bound.addBoundToContextMenu(cm);

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
					eq.colorR = Collisions.randomizeColor(bound.colorR, 0.4);
					eq.colorG = Collisions.randomizeColor(bound.colorG, 0.4);
					eq.colorB = Collisions.randomizeColor(bound.colorB, 0.4);
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
//				System.out.println("KEY TYPED");
				switch (e.getCode()) {
				case DELETE:
//					System.out.println("DELETE PLANE");
					PlaneEquations.remove(p);
					this.updateShape();
					CollisionsEditor.planeControls.setPlane(null);
//					CollisionsEditor.updateRender();
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

//		System.out.println("Number of calculated vertices : "+this.Vertices.size()+" vs actual number of vertices : "+this.NumVertices);
		

		for (Vertex v : this.Vertices) {
			Sphere s;
			shapes.add(s = new Sphere(0.01));
			s.setTranslateX(v.x);
			s.setTranslateY(v.y);
			s.setTranslateZ(v.z);
//			System.out.println(v);
		}
//		
//		for (var v : RotatedVertices) {
//			Sphere s;
//			shapes.add(s = new Sphere(0.1));
//			s.setMaterial(new PhongMaterial(Color.RED));
//			s.setTranslateX(v.XRotationX);
//			s.setTranslateY(v.YRotationX);
//			s.setTranslateZ(v.ZRotationX);
//
//			shapes.add(s = new Sphere(0.1));
//			s.setMaterial(new PhongMaterial(Color.RED));
//			s.setTranslateX(v.XRotationY);
//			s.setTranslateY(v.YRotationY);
//			s.setTranslateZ(v.ZRotationY);
//
//			shapes.add(s = new Sphere(0.1));
//			s.setMaterial(new PhongMaterial(Color.RED));
//			s.setTranslateX(v.XRotationZ);
//			s.setTranslateY(v.YRotationZ);
//			s.setTranslateZ(v.ZRotationZ);
//
//			shapes.add(s = new Sphere(0.1));
//			s.setMaterial(new PhongMaterial(Color.RED));
//			s.setTranslateX(v.XRotationW);
//			s.setTranslateY(v.YRotationW);
//			s.setTranslateZ(v.ZRotationW);
//		}
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
		
		if (bound != null) bound.updateBoundAndPivot();
	}
	
	public static CollisionConvexVertice load(ByteBuffer bb) {
		CollisionConvexVertice load = new CollisionConvexVertice();
		bb.position(bb.position() + 0x10);
		load.boundDistance = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		
		load.HalfExtentsX = bb.getFloat();
		load.HalfExtentsY = bb.getFloat();
		load.HalfExtentsZ = bb.getFloat();
		load.HalfExtentsW = bb.getFloat();
		load.CenterX = bb.getFloat();
		load.CenterY = bb.getFloat();
		load.CenterZ = bb.getFloat();
		load.CenterW = bb.getFloat();

		bb.getInt();
		var NumberOfRotatedVertices = bb.getShort();
		bb.position(bb.position()+6);
		
//		var NumVertices = 
				bb.getInt();
		bb.getInt();
		var NumberOfPlaneEquations = bb.getShort();
		bb.position(bb.position()+10);

		// ignore the """rotated""" vertices, they will be recalculated from the planes and welded by the way
		bb.position(bb.position()+48*NumberOfRotatedVertices);

		// Get Plane Equations
		for (int loop = 0; loop < NumberOfPlaneEquations; loop++)
		{
			load.PlaneEquations.add(new PlaneEquation(bb, load));
		}

		return load;
	}

	public byte[] save() throws IOException {
		
		var bb = ByteBuffer.wrap(new byte[68+0x10+0x0C]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(new byte[0x10]);
		bb.putFloat(boundDistance);
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

//		for (var rv : RotatedVertices) data.write(rv.save());
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
	public CollisionShape deepCopy(CollisionBound bound) {
		var copy = new CollisionConvexVertice();
		copy.boundDistance = this.boundDistance;
		copy.HalfExtentsX  = this.HalfExtentsX;
		copy.HalfExtentsY  = this.HalfExtentsY;
		copy.HalfExtentsZ  = this.HalfExtentsZ;
		copy.HalfExtentsW  = this.HalfExtentsW;
		copy.CenterX = this.CenterX;
		copy.CenterY = this.CenterY;
		copy.CenterZ = this.CenterZ;
		copy.CenterW = this.CenterW;
		copy.bound = bound;
		this.PlaneEquations.forEach(pe -> copy.PlaneEquations.add(pe.deepCopy(copy)));
		copy.updateShape();
		return copy;
	}
}
