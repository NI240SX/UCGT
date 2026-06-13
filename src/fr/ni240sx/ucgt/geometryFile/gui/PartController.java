package fr.ni240sx.ucgt.geometryFile.gui;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.GeometryEditorGUI;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.shared.Hash;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

public class PartController{
	public Part part;
	public BooleanProperty display = new SimpleBooleanProperty(false);
	public Group mpMesh;
	public Group mesh;
	public Group mesh2;
	public List<Node> vertexColorMeshes;
	public String name;
	
	public static final PhongMaterial mpMaterial = new PhongMaterial(Color.rgb(255, 255, 255, 0.3));
	
	public static PhongMaterial vertexColorMaterial = new PhongMaterial();
	public static WritableImage vertexColorImage;
	
	static{
		vertexColorImage = new WritableImage(256, 1);
		for (int i=0; i<vertexColorImage.getWidth(); i++) {
			vertexColorImage.getPixelWriter().setColor(i,0, Color.rgb(0, 0, 0, (255-i)/255.0));
		}
		vertexColorMaterial.setDiffuseMap(vertexColorImage);
//		vertexColorMaterial.setSpecularPower(0);
	}
	
	public PartController(Part p) {
		this.part = p;
		mesh = new Group();
		this.display.addListener((obs, was, is) ->{
			if (was != is) this.updateRender();
		});
//		this.mesh.setOnMouseClicked(e -> {
//			selectPart(e);
//		});
	}

	public void selectPart(MouseEvent e) {
		//fast method for car models
		for (var chi : GeometryEditorGUI.partsRoot.getChildren()) {
			if (chi.getValue().name != null && chi.getValue().name.equals(this.part.kit)) {

				for (var chi2 : chi.getChildren()) {
					if (chi2.getValue() == this) {
						if (!e.isControlDown() && !e.isShiftDown()) GeometryEditorGUI.partsDisplay.getSelectionModel().clearSelection();
						GeometryEditorGUI.partsDisplay.getSelectionModel().select(chi2);
						
						GeometryEditorGUI.partsDisplay.scrollTo(GeometryEditorGUI.partsDisplay.getSelectionModel().getSelectedIndex());
						return;
					}
				}
				
				return;
			}
		}
		
		//world models
		for (var chi2 : GeometryEditorGUI.partsRoot.getChildren()) {
			if (chi2.getValue() == this) {
				if (!e.isControlDown() && !e.isShiftDown()) GeometryEditorGUI.partsDisplay.getSelectionModel().clearSelection();
				GeometryEditorGUI.partsDisplay.getSelectionModel().select(chi2);
				
				GeometryEditorGUI.partsDisplay.scrollTo(GeometryEditorGUI.partsDisplay.getSelectionModel().getSelectedIndex());
				return;
			}
		}
	}
	
	public PartController(String s) {
		this.name = s;
	}
	
	public void updateRender() {
		mesh.getChildren().clear();
		if (mesh2 != null) mesh2.getChildren().clear();
		if (mpMesh != null) mpMesh.getChildren().clear();
		if (display.get() && part != null && part.mesh != null) {
			
			
			if (!GeometryEditorGUI.allowConflictingParts.isSelected()) for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.part.equals(this.part.part) && p != this) {
				p.display.set(false);
				break;
			}
			
			
			
//			mesh2 = null; //might cause crasj
			
			updateWheelsPos();

			//TODO manage markers also when part.mpoints != null
			
			if (part.part.contains("DRIVER")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("DRIVER_POSITION")) {
						setPosition(mp);
						break partsSearch;
					}
				}
			}
			if (part.part.equals("SEAT_LEFT")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("SEAT_LEFT")) {
						setPosition(mp);
						break partsSearch;
					}
				}
			}
			if (part.part.equals("SEAT_RIGHT")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("SEAT_RIGHT")) {
						setPosition(mp);
						break partsSearch;
					}
				}
			}

			//prostreet
			if (part.part.equals("EXHAUST") && !part.kit.equals("KIT00")) {
				boolean already = false;
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("EXHAUST_LEFT")) {
						setPosition(mp);
						already = true;
						break partsSearch;
					}
				}
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("EXHAUST_RIGHT")) {
						if (already) {
							if (mesh2==null) {
								mesh2 = new Group();
//								mesh2.setOnMouseClicked(mesh.getOnMouseClicked());
							}
							mesh2.setTranslateX(mp.positionY);
							mesh2.setTranslateY(mp.positionZ);
							mesh2.setTranslateZ(mp.positionX);

						} else {
							setPosition(mp);
						}
						break partsSearch;
					}
				}
			}
			
			
			if (part.mesh.materials != null) {
				for (var m : part.mesh.materials.materials) 
					if (m.isOpaque()) {
					drawMaterial(m); //opaque pass
				}
				for (var m : part.mesh.materials.materials) 
					if (m.hasTransparency()) {
					drawMaterial(m); //alpha pass
				}
			}
			
			
			if (!GeometryEditorGUI.modelGroup.getChildren().contains(mesh)) {
				GeometryEditorGUI.modelGroup.getChildren().add(mesh);
				if (mesh2 != null) GeometryEditorGUI.modelGroup.getChildren().add(mesh2);
			}
			

			var t = new Tooltip(toString());
			mesh.setOnMouseEntered(e -> {
				t.show(mesh, e.getScreenX()+10, e.getScreenY()+10);
//				mesh.requestFocus();
			});
			mesh.setOnMouseMoved(e -> {
				t.setAnchorX(e.getScreenX()+10);
				t.setAnchorY(e.getScreenY()+10);
			});
			mesh.setOnMouseExited(e -> {
				t.hide();
			});
			if (mesh2 != null) {
				mesh2.setOnMouseEntered(e -> {
					t.show(mesh2, e.getScreenX()+10, e.getScreenY()+10);
//					mesh2.requestFocus();
				});
				mesh2.setOnMouseMoved(e -> {
					t.setAnchorX(e.getScreenX()+10);
					t.setAnchorY(e.getScreenY()+10);
				});
				mesh2.setOnMouseExited(e -> {
					t.hide();
				});
			}
			
			
			updateMarkersRender();
		} else {
			GeometryEditorGUI.modelGroup.getChildren().remove(mesh);
			if (mesh2 != null) {
				GeometryEditorGUI.modelGroup.getChildren().remove(mesh2);
				mesh2 = null;
			}
			if (mpMesh != null) {
				GeometryEditorGUI.modelGroup.getChildren().remove(mpMesh);
				mpMesh = null;
			}
		}
	}

	private void setPosition(MPoint mp) {
		mesh.setTranslateX(mp.positionY);
		mesh.setTranslateY(mp.positionZ);
		mesh.setTranslateZ(mp.positionX);
	}

	public boolean isOpaque() {
		return !hasTransparency() || (part.mpoints != null);
	}
	public boolean hasTransparency() {
		if (part.mesh != null && part.mesh.materials != null) {
			for (var m : part.mesh.materials.materials) 
				if (m.hasTransparency()) return true;
		}
		return false;
	}
	
	private void drawMaterial(Material m) {
		TriangleMesh matMesh = new TriangleMesh();
		
		if (m.verticesBlock.vertexFormat.hasNormals()) matMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
		
		if (m.verticesBlock != null) {
			for (var v : m.verticesBlock.vertices) {
				matMesh.getPoints().addAll(v.pos[Y], v.pos[Z], v.pos[X]);
			}
			if (m.verticesBlock.vertexFormat.hasNormals()) for (var v : m.verticesBlock.vertices) {
				matMesh.getNormals().addAll(v.norm[Y], v.norm[Z], v.norm[X]);
			}
			if (m.verticesBlock.vertexFormat.getNumTexChannels() > 0) for (var v : m.verticesBlock.vertices) {
				matMesh.getTexCoords().addAll(v.tex[0][U], 1-v.tex[0][V]);
			}
		}
		
		if (m.verticesBlock.vertexFormat.hasNormals()) for (var tr : m.triangles) {
			matMesh.getFaces().addAll(
					// points normals   texcoords
					tr.vert0, tr.vert0, tr.vert0, //v1
					tr.vert1, tr.vert1, tr.vert1, //v2
					tr.vert2, tr.vert2, tr.vert2);//v3
		} else for (var tr : m.triangles) {
			matMesh.getFaces().addAll(
					// points texcoords
					tr.vert0, tr.vert0, //v1
					tr.vert1, tr.vert1, //v2
					tr.vert2, tr.vert2);//v3
		}
		
		var mv = new MeshView(matMesh);

		try {
			mv.setMaterial(GeometryEditorGUI.visualModelMaterials.get(GeometryEditorGUI.mainGeometry.materials.indexOf(m)));
		} catch (Exception e) {
			System.out.println("Cannot display material on part "+this.toString()+": "+e.getMessage());
		}
		mv.setOnMouseClicked(e -> {
			selectPart(e);
			for (var itm : GeometryEditorGUI.partInfo.materials.getChildren()) {
				if (itm.getValue().equals(m.generateName())) {
					GeometryEditorGUI.partInfo.getSelectionModel().select(itm);
					itm.setExpanded(true);
				}
			}
		});
						
		mv.setDepthTest(DepthTest.ENABLE);
		if (m.hasTransparency()) {
			mv.setViewOrder(-1);
			mv.setCullFace(CullFace.NONE);
		}

		mesh.getChildren().add(mv);
		
		
				
		
		
		if (m.verticesBlock.vertexFormat.hasColor()) {
			if (vertexColorMeshes == null) vertexColorMeshes = new ArrayList<>();
			var vcolMesh = new TriangleMesh();
			
			if (m.verticesBlock.vertexFormat.hasNormals()) vcolMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
			
			if (m.verticesBlock != null) {
				for (var v : m.verticesBlock.vertices) {
					vcolMesh.getPoints().addAll(v.pos[Y], v.pos[Z], v.pos[X]);
				}
				for (var v : m.verticesBlock.vertices) {
					vcolMesh.getTexCoords().addAll(v.color[R]*255.0f/256.0f/3.0f
							+v.color[G]*255.0f/256.0f/3.0f
							+v.color[B]*255.0f/256.0f/3.0f, 0);
				}
				if (m.verticesBlock.vertexFormat.hasNormals()) for (var v : m.verticesBlock.vertices) {
					vcolMesh.getNormals().addAll(v.norm[Y], v.norm[Z], v.norm[X]);
				}
			}
			
			if (m.verticesBlock.vertexFormat.hasNormals()) for (var tr : m.triangles) {
				vcolMesh.getFaces().addAll(
						// points normals   texcoords
						tr.vert0, tr.vert0, tr.vert0, //v1
						tr.vert1, tr.vert1, tr.vert1, //v2
						tr.vert2, tr.vert2, tr.vert2);//v3
			} else for (var tr : m.triangles) {
				vcolMesh.getFaces().addAll(
						// points texcoords
						tr.vert0, tr.vert0, //v1
						tr.vert1, tr.vert1, //v2
						tr.vert2, tr.vert2);//v3
			}
			var mvCol = new MeshView(vcolMesh);
			mvCol.setDepthTest(DepthTest.ENABLE);
			mvCol.setMaterial(vertexColorMaterial);
//			mvCol.setBlendMode(BlendMode.MULTIPLY);
			mvCol.setOnMouseClicked(mv.getOnMouseClicked());
			if (GeometryEditorGUI.doVertexColors.isSelected()) mesh.getChildren().add(mvCol);
			vertexColorMeshes.add(mvCol);
		}
		
		if (mesh2 != null) {
			var mv2 = new MeshView(matMesh);
			try {
				mv2.setMaterial(GeometryEditorGUI.visualModelMaterials.get(GeometryEditorGUI.mainGeometry.materials.indexOf(m)));
			} catch (@SuppressWarnings("unused") Exception e) {}
			mv2.setOnMouseClicked(mv.getOnMouseClicked());
			
			mv2.setDepthTest(DepthTest.ENABLE);
			if (m.hasTransparency()) {
				mv2.setViewOrder(-1);
				mv2.setCullFace(CullFace.NONE);
			}
			mesh2.getChildren().add(mv2);
		}
	}

	public void updateMarkersRender() {
		if (GeometryEditorGUI.displayMarkers.isSelected() && display.get() && part != null && part.mpoints != null) {
			if (mpMesh == null) mpMesh = new Group();
			if (!GeometryEditorGUI.modelGroup.getChildren().contains(mpMesh))
				GeometryEditorGUI.modelGroup.getChildren().add(mpMesh);
			for (var mp : part.mpoints.mpoints) {
				var c = new Box(0.1, 0.1, 0.1);
				c.setTranslateX(mesh.getTranslateX() + mp.positionY);
				c.setTranslateY(mesh.getTranslateY() + mp.positionZ);
				c.setTranslateZ(mesh.getTranslateZ() + mp.positionX);

				var m = mp.matrix;
				double d = Math.acos((m[0][0] + m[1][1] + m[2][2] - 1d)/2d);
			    if(d!=0d){
			        double den=2d*Math.sin(d);
			        Point3D p= new Point3D(
			        		(m[2][0] - m[0][2])/den,
			        		(m[0][1] - m[1][0])/den,
			        		(m[1][2] - m[2][1])/den
			        		);
			        c.setRotationAxis(p);
			        c.setRotate(Math.toDegrees(d));
			    }
			    c.setMaterial(mpMaterial);
			    mpMesh.getChildren().add(c);
			    
				var t = new Tooltip(toString() + " > " + Hash.getBIN(mp.nameHash));
				
				c.setOnMouseEntered(e -> {
					t.show(mpMesh, e.getScreenX()+10, e.getScreenY()+10);
//					mpMesh.requestFocus();
				});
				c.setOnMouseMoved(e -> {
					t.setAnchorX(e.getScreenX()+10);
					t.setAnchorY(e.getScreenY()+10);
				});
				c.setOnMouseExited(e -> {
					t.hide();
				});
				c.setOnMouseClicked(e -> {
					selectPart(e);
					for (var itm : GeometryEditorGUI.partInfo.mpoints.getChildren()) {
						if (itm.getValue().equals(Hash.getBIN(mp.nameHash))) {
							GeometryEditorGUI.partInfo.getSelectionModel().select(itm);
							itm.setExpanded(true);
						}
					}
				});
			}
		} else {
			if (mpMesh != null) {
				GeometryEditorGUI.modelGroup.getChildren().remove(mpMesh);
				mpMesh = null;
			}
		}
	}

	public void updateWheelsPos() {
		if (part.part.contains("WHEEL")) {
			if (part.part.contains("FRONT")) {
				mesh.setTranslateZ(GeometryEditorGUI.wheelPosFrontX.getValue());
				mesh.setTranslateX(-GeometryEditorGUI.wheelPosFrontY.getValue());
				mesh.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue());
				
				if (mesh2==null) {
					mesh2 = new Group();
//					mesh2.setOnMouseClicked(mesh.getOnMouseClicked());
				}
				mesh2.setTranslateZ(GeometryEditorGUI.wheelPosFrontX.getValue());
				mesh2.setTranslateX(GeometryEditorGUI.wheelPosFrontY.getValue()-part.header.boundsYmax);
				mesh2.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue());
				mesh2.setScaleX(-1);
				mesh2.setScaleZ(-1);
				
				if (part.mpoints != null) {
					for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.part.contains("BRAKE") && p.part.part.contains("FRONT")) {
						for (var mp : part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("BRAKE_FRONT")) {
							setBrakePos(this, p, mp);
						}
					}
				}
				
			} else if (part.part.contains("REAR")) {
				mesh.setTranslateZ(GeometryEditorGUI.wheelPosRearX.getValue());
				mesh.setTranslateX(-GeometryEditorGUI.wheelPosRearY.getValue());
				mesh.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue());
				
				if (mesh2==null) {
					mesh2 = new Group();
//					mesh2.setOnMouseClicked(mesh.getOnMouseClicked());
				}
				mesh2.setTranslateZ(GeometryEditorGUI.wheelPosRearX.getValue());
				mesh2.setTranslateX(GeometryEditorGUI.wheelPosRearY.getValue()-part.header.boundsYmax);
				mesh2.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue());
				mesh2.setScaleX(-1);
				mesh2.setScaleZ(-1);

				if (part.mpoints != null) {
					for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.part.contains("BRAKE") && p.part.part.contains("REAR")) {
						for (var mp : part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("BRAKE_FRONT")) {
							setBrakePos(this, p, mp);
						}
					}
				}
			}
		}
		if (part.part.contains("BRAKE") && (part.part.contains("FRONT") || part.part.contains("REAR"))) {
			if (mesh2==null) {
				mesh2 = new Group();
//				mesh2.setOnMouseClicked(mesh.getOnMouseClicked());
			}
			mesh2.setScaleX(-1);
			if (part.part.contains("FRONT")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null && p.part.part.contains("WHEEL_TIRE_FRONT")) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("BRAKE_FRONT")) {
						setBrakePos(p, this, mp);
						break partsSearch;
					}
				}
			} else {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null && p.part.part.contains("WHEEL_TIRE_REAR")) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("BRAKE_FRONT")) {
						setBrakePos(p, this, mp);
						break partsSearch;
					}
				}
			}
		}
	}

	private static void setBrakePos(PartController wheel, PartController brake, MPoint mp) {
		brake.mesh.setTranslateZ(wheel.mesh.getTranslateZ() +mp.positionX);
		brake.mesh.setTranslateX(wheel.mesh.getTranslateX()+mp.positionY);
		brake.mesh.setTranslateY(wheel.mesh.getTranslateY()+mp.positionZ);

		brake.mesh2.setTranslateZ(wheel.mesh2.getTranslateZ()+mp.positionX);
		brake.mesh2.setTranslateX(-wheel.mesh.getTranslateX()-brake.part.header.boundsYmax-mp.positionY);
		brake.mesh2.setTranslateY(wheel.mesh2.getTranslateY()+mp.positionZ);
	}

	public void updateAutosculpt(//AutosculptControls autosculpt
			ArrayList<Double> sliders
			) {
		
		if (part.asZones.zones == null) return;
		
		ArrayList<PartController> zones = new ArrayList<>();
		
		zones: for (var z : part.asZones.zones) {
			for (var pc : GeometryEditorGUI.parts) {
				if (pc.part != null && pc.part.header.binKey == z) {
					zones.add(pc);
					continue zones;
				}
			}
			zones.add(null);
		}
		
		if (zones.get(0) == null) return;
		
		for (var z : zones) {
			if (z != null) z.display.set(true);
		}
		
		for (int meshViewIndex = 0; meshViewIndex < mesh.getChildren().size(); meshViewIndex++) {
			
			for (int pointIndex = 0; pointIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().size(); pointIndex++) {
				((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().set(pointIndex, 
							((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().get(pointIndex)
						);
			}

			for (int normalsIndex = 0; normalsIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().size(); normalsIndex++) {
				((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().set(normalsIndex, 
							((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex)
						);
			}
			
			for (int texcoordIndex = 0; texcoordIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().size(); texcoordIndex++) {
				((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().set(texcoordIndex, 
							((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordIndex)
						);
			}
		}
		
		
		
		
		
		
		for (int zoneIndex = 1; zoneIndex < part.asZones.zones.size(); zoneIndex++) if (zones.get(zoneIndex) != null) {
		
			for (int meshViewIndex = 0; meshViewIndex < mesh.getChildren().size(); meshViewIndex++) {

				for (int pointIndex = 0; pointIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().size(); pointIndex++) {
					((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().set(pointIndex, 
							(float) (((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().get(pointIndex) +
							(	((TriangleMesh)((MeshView)zones.get(zoneIndex).mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().get(pointIndex) -				
								((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getPoints().get(pointIndex)
									) * sliders.get(zoneIndex))); //autosculpt.autosculptSliders.get(zoneIndex).slider.getValue()
				}
				
				for (int normalsIndex = 0; normalsIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().size(); normalsIndex++) {
					((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().set(normalsIndex, 
							(float) (((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex) +
							(	((TriangleMesh)((MeshView)zones.get(zoneIndex).mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex) -				
								((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex)
									) * sliders.get(zoneIndex))); //autosculpt.autosculptSliders.get(zoneIndex).slider.getValue()
				}
				
				for (int texcoordsIndex = 0; texcoordsIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().size(); texcoordsIndex++) {
					((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().set(texcoordsIndex, 
							(float) (((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordsIndex) +
							(	((TriangleMesh)((MeshView)zones.get(zoneIndex).mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordsIndex) -				
								((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordsIndex)
									) * sliders.get(zoneIndex))); //autosculpt.autosculptSliders.get(zoneIndex).slider.getValue()
				}
			}
			
			
			
			
			
		}


		for (var z : zones) {
			if (z != null) z.display.set(false);
		}
		
		if (part.asLinking != null) {
			var partZones = new HashMap<PartController, ArrayList<Double>>();
			
			for (var l : part.asLinking.links) {
				for (var pc : GeometryEditorGUI.parts) {
					if (pc.part != null && pc.part.header.binKey == l.partKey) {
						if (partZones.get(pc) == null) {
							var linkedZones = new ArrayList<Double>();
							for (int i=0; i< pc.part.asZones.zones.size(); i++) linkedZones.add(0.0);
							partZones.put(pc, linkedZones);
						}
						
						partZones.get(pc).set(l.toZone3, sliders.get(l.fromZone1));
						break;
					}
				}
			}
			for (var pc : partZones.keySet()) {
				pc.updateAutosculpt(partZones.get(pc));
			}
		}
	}
	
	@Override
	public String toString() {
		if (part != null) return GeometryEditorGUI.simplifiedPartsList.isSelected() ? part.part : part.name;
		if (name != null) return name;
		return "PartController";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PartController other = (PartController) obj;
		return this.toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}