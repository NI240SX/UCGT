package fr.ni240sx.ucgt.geometryFile.gui;

import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.GeometryEditorGUI;
import fr.ni240sx.ucgt.geometryFile.Part;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

public class PartController{
	public Part part;
	public BooleanProperty display = new SimpleBooleanProperty(false);
	public Group mpMesh;
	public Group mesh;
	public Group mesh2;
	public String name;
	
	public static final PhongMaterial mpMaterial = new PhongMaterial(Color.rgb(255, 255, 255, 0.3));
	
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

			
			if (part.part.contains("DRIVER")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("DRIVER_POSITION")) {
						mesh.setTranslateX(mp.positionY);
						mesh.setTranslateY(mp.positionZ);
						mesh.setTranslateZ(mp.positionX);
						break partsSearch;
					}
				}
			}
			if (part.part.equals("SEAT_LEFT")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("SEAT_LEFT")) {
						mesh.setTranslateX(mp.positionY);
						mesh.setTranslateY(mp.positionZ);
						mesh.setTranslateZ(mp.positionX);
						break partsSearch;
					}
				}
			}
			if (part.part.equals("SEAT_RIGHT")) {
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("SEAT_RIGHT")) {
						mesh.setTranslateX(mp.positionY);
						mesh.setTranslateY(mp.positionZ);
						mesh.setTranslateZ(mp.positionX);
						break partsSearch;
					}
				}
			}

			//prostreet
			if (part.part.equals("EXHAUST") && !part.kit.equals("KIT00")) {
				boolean already = false;
				partsSearch: for (var p : GeometryEditorGUI.parts) if (p.display.get() && p.part != null && p.part.mpoints != null) {
					for (var mp : p.part.mpoints.mpoints) if (mp.nameHash == Hash.findBIN("EXHAUST_LEFT")) {
						mesh.setTranslateX(mp.positionY);
						mesh.setTranslateY(mp.positionZ);
						mesh.setTranslateZ(mp.positionX);
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
							mesh.setTranslateX(mp.positionY);
							mesh.setTranslateY(mp.positionZ);
							mesh.setTranslateZ(mp.positionX);
						}
						break partsSearch;
					}
				}
			}
			
			
			if (part.mesh.materials != null) for (var m : part.mesh.materials.materials) {
				TriangleMesh matMesh = new TriangleMesh();
				
				matMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
				
				if (m.verticesBlock != null) for (var v : m.verticesBlock.vertices) {
					matMesh.getPoints().addAll(v.posY, v.posZ, v.posX);
					matMesh.getNormals().addAll(v.normY, v.normZ, v.normX);
					matMesh.getTexCoords().addAll(v.tex0U, 1-v.tex0V);
				}
				
				for (var tr : m.triangles) {
					matMesh.getFaces().addAll(
							// points normals   texcoords
							tr.vert0, tr.vert0, tr.vert0, //v1
							tr.vert1, tr.vert1, tr.vert1, //v2
							tr.vert2, tr.vert2, tr.vert2);//v3
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
								
				mesh.getChildren().add(mv);
				if (mesh2 != null) {
					var mv2 = new MeshView(matMesh);
					try {
						mv2.setMaterial(GeometryEditorGUI.visualModelMaterials.get(GeometryEditorGUI.mainGeometry.materials.indexOf(m)));
					} catch (@SuppressWarnings("unused") Exception e) {}
					mv2.setOnMouseClicked(mv.getOnMouseClicked());
					mesh2.getChildren().add(mv2);
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
							p.mesh.setTranslateZ(GeometryEditorGUI.wheelPosFrontX.getValue()+mp.positionX);
							p.mesh.setTranslateX(-GeometryEditorGUI.wheelPosFrontY.getValue()+mp.positionY);
							p.mesh.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue()+mp.positionZ);

							p.mesh2.setTranslateZ(GeometryEditorGUI.wheelPosFrontX.getValue()+mp.positionX);
							p.mesh2.setTranslateX(GeometryEditorGUI.wheelPosFrontY.getValue()-p.part.header.boundsYmax-mp.positionY);
							p.mesh2.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue()+mp.positionZ);
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
							p.mesh.setTranslateZ(GeometryEditorGUI.wheelPosRearX.getValue()+mp.positionX);
							p.mesh.setTranslateX(-GeometryEditorGUI.wheelPosRearY.getValue()+mp.positionY);
							p.mesh.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue()+mp.positionZ);

							p.mesh2.setTranslateZ(GeometryEditorGUI.wheelPosRearX.getValue()+mp.positionX);
							p.mesh2.setTranslateX(GeometryEditorGUI.wheelPosRearY.getValue()-p.part.header.boundsYmax-mp.positionY);
							p.mesh2.setTranslateY(GeometryEditorGUI.wheelPosHeight.getValue()+mp.positionZ);
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
		}
	}

	public void updateAutosculpt(AutosculptControls autosculpt) {
		
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
									) * autosculpt.autosculptSliders.get(zoneIndex).slider.getValue()));
				}
				
				for (int normalsIndex = 0; normalsIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().size(); normalsIndex++) {
					((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().set(normalsIndex, 
							(float) (((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex) +
							(	((TriangleMesh)((MeshView)zones.get(zoneIndex).mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex) -				
								((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getNormals().get(normalsIndex)
									) * autosculpt.autosculptSliders.get(zoneIndex).slider.getValue()));
				}
				
				for (int texcoordsIndex = 0; texcoordsIndex < ((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().size(); texcoordsIndex++) {
					((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().set(texcoordsIndex, 
							(float) (((TriangleMesh)((MeshView)mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordsIndex) +
							(	((TriangleMesh)((MeshView)zones.get(zoneIndex).mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordsIndex) -				
								((TriangleMesh)((MeshView)zones.get(0).mesh.getChildren().get(meshViewIndex)).getMesh()).getTexCoords().get(texcoordsIndex)
									) * autosculpt.autosculptSliders.get(zoneIndex).slider.getValue()));
				}
			}
			
			
			
			
			
		}


		for (var z : zones) {
			if (z != null) z.display.set(false);
		}
		
		//TODO proper use of part linking
		if (part.asLinking != null) {
			for (var l : part.asLinking.links) {
				for (var pc : GeometryEditorGUI.parts) {
					if (pc.part != null && pc.part.header.binKey == l.partKey) {
						pc.updateAutosculpt(autosculpt);
						break;
					}
				}
				
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