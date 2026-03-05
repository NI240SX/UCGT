package fr.ni240sx.ucgt.geometryFile.gui;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.GeometryEditorGUI;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class PartInfo extends TreeView<String>{
	TreeItem<String> root = new TreeItem<>();

	TreeItem<String> counts = new TreeItem<>("Counts");
	TreeItem<String> materials = new TreeItem<>("Materials");
	TreeItem<String> bounds = new TreeItem<>("Bounds");
	TreeItem<String> mpoints = new TreeItem<>("Markers");
	TreeItem<String> asZ = new TreeItem<>("Autosculpt Zones");
	TreeItem<String> asL = new TreeItem<>("Autosculpt Links");
	
	public PartInfo() {
		super();
		this.setRoot(root);
		root.setExpanded(true);

		counts.setExpanded(true);
		materials.setExpanded(true);
		bounds.setExpanded(true);
		mpoints.setExpanded(true);
		asZ.setExpanded(true);
		asL.setExpanded(true);
		this.update(null);
		
		this.setCellFactory(tv -> {
			TreeCell<String> cell = new TreeCell<>() {
		        @Override
		        protected void updateItem(String item, boolean empty) {
		            super.updateItem(item, empty);
		            setText(item);
		            
		            if (this.getTreeItem() != null && this.getTreeItem().getClass() == FormattedTreeItem.class) {
		            	 setStyle(((FormattedTreeItem<String>)this.getTreeItem()).style);
		             }
		        }
		    };
		    return cell ;
        });

	}
	
	public void update(PartController controller) {
		root.getChildren().clear();
		
		counts.getChildren().clear();
		materials.getChildren().clear();
		bounds.getChildren().clear();
		mpoints.getChildren().clear();
		asZ.getChildren().clear();
		asL.getChildren().clear();
		
		var p = controller == null ? null : controller.part;
		
		try {
			if (p == null) {		
				root.setValue(GeometryEditorGUI.mainGeometry == null ? "" : GeometryEditorGUI.mainGeometry.carname);
			} else {
				root.setValue(p.name);
				if (p.header != null) root.getChildren().add(counts);
				if (p.header != null) root.getChildren().add(bounds);
				if (p.mpoints != null) root.getChildren().add(mpoints);
				if (p.mesh != null && p.mesh.materials != null) root.getChildren().add(materials); else root.getChildren().add(new TreeItem<>("Unsupported mesh data"));
				if (p.asZones != null) root.getChildren().add(asZ);
				if (p.asLinking != null) root.getChildren().add(asL);
				
				GeometryEditorGUI.autosculptControls.update(controller);
				if (GeometryEditorGUI.centerOnPart.isSelected()) GeometryEditorGUI.viewport.moveCamera(
						(p.header.boundsYmax+p.header.boundsYmin)/2, 
						(p.header.boundsZmax+p.header.boundsZmin)/2, 
						(p.header.boundsXmax+p.header.boundsXmin)/2);
				
				if (p.header != null) {
					counts.getChildren().add(new FormattedTreeItem<>(p.header.shadersCount+" material shaders"));
					counts.getChildren().add(new FormattedTreeItem<>(p.header.texturesCount+" textures"));
					counts.getChildren().add(new FormattedTreeItem<>(p.header.trianglesCount+" triangles"));
				}
				if (p.mesh != null) {
					if (p.mesh.materials != null) {
//						FormattedTreeItem<String> vert; //custom thing that's quite buggy
//						FormattedTreeItem<String> tri;
						if (p.mesh.info.numVertices != 0)
						counts.getChildren().add(
								//vert = 
								new FormattedTreeItem<>(p.mesh.info.numVertices+" vertices"));
						else {
							var verts = 0;
							for (int i=0; i<p.mesh.verticesBlocks.size(); i++) if (i==0 || p.mesh.verticesBlocks.get(i) != p.mesh.verticesBlocks.get(i-1)) verts += p.mesh.verticesBlocks.get(i).vertices.size();
							counts.getChildren().add(
									//vert = 
									new FormattedTreeItem<>(verts+" vertices"));
							
						}
//						counts.getChildren().add(
//								//tri =
//								new FormattedTreeItem<>(p.mesh.info.numTriangles+" triangles"));
						counts.getChildren().add(new TreeItem<>(p.mesh.info.numMaterials+" materials"));
						counts.getChildren().add(new TreeItem<>("Mesh version "+p.mesh.info.version));
						counts.getChildren().add(new TreeItem<>("Mesh platform "+p.mesh.platform.getName()));
	
						
//						if (p.mesh.info.numVertices > 3500) {
//							if (p.mesh.info.numVertices < 5000) {
//								vert.style = "-fx-text-fill:orange;";
//							} else {
//								vert.style = "-fx-text-fill:red;";
//							}
//						}
//						
//						if (p.mesh.info.numTriangles > 3500) {
//							if (p.mesh.info.numTriangles < 5000) {
//								tri.style = "-fx-text-fill:orange;";
//							} else {
//								tri.style = "-fx-text-fill:red;";
//							}
//						}
						
						
						for (var m : p.mesh.materials.materials) {
							var mat = new TreeItem<>(m.generateName());
							materials.getChildren().add(mat);
							
							var usa = new TreeItem<>("Shader");
							mat.getChildren().add(usa);
							usa.getChildren().add(new TreeItem<>(m.shaderUsage.getName()));
							usa.setExpanded(true);
							
							var sha = new TreeItem<>("Material");
							mat.getChildren().add(sha);
							sha.getChildren().add(new TreeItem<>(Hash.getBIN(m.ShaderHash)));
							sha.setExpanded(true);
	
							var tex = new TreeItem<>("Textures");
							mat.getChildren().add(tex);
							tex.setExpanded(true);
							for (int i=0; i< m.TextureHashes.size(); i++) {
								if (m.textureUsages.size() > i && m.TextureHashes.size() > i) {
									var it = new TreeItem<>(Hash.getBIN(m.TextureHashes.get(i))+" as "+m.textureUsages.get(i).getName());
									tex.getChildren().add(it);
								} else if (m.TextureHashes.size() > i) {
									var it = new TreeItem<>(Hash.getBIN(m.TextureHashes.get(i)));
									tex.getChildren().add(it);
								}
							}

							var flags = new TreeItem<>("Flags: "+String.format("0x%08X", 
									Byte.toUnsignedInt(m.flags[0]) << 24 | 
									Byte.toUnsignedInt(m.flags[1]) << 16 | 
									Byte.toUnsignedInt(m.flags[2]) << 8 | 
									Byte.toUnsignedInt(m.flags[3]) << 0));
							mat.getChildren().add(flags);
							
							flags = new TreeItem<>("Combined texture hash: "+Hash.getBIN(m.textureHash));
							mat.getChildren().add(flags);

						}
	
						
						
					}
				}

				if (p.mpoints != null) {
					counts.getChildren().add(new TreeItem<>(p.mpoints.mpoints.size()+" markers"));
					for (MPoint mp : p.mpoints.mpoints) {
						var mpoint = new TreeItem<>(Hash.getBIN(mp.nameHash));
						mpoints.getChildren().add(mpoint);

						var pos = new TreeItem<>("Position");
						mpoint.getChildren().add(pos);
						pos.getChildren().add(new TreeItem<>("X="+mp.positionX));
						pos.getChildren().add(new TreeItem<>("Y="+mp.positionY));
						pos.getChildren().add(new TreeItem<>("Z="+mp.positionZ));
						pos.getChildren().add(new TreeItem<>("W="+mp.positionW));
						pos.setExpanded(true);

						var scl = new TreeItem<>("Scale");
						mpoint.getChildren().add(scl);
						scl.getChildren().add(new TreeItem<>("X="+mp.scaleX));
						scl.getChildren().add(new TreeItem<>("Y="+mp.scaleY));
						scl.getChildren().add(new TreeItem<>("Z="+mp.scaleZ));
						scl.setExpanded(true);

						var rot = new TreeItem<>("Rotation");
						mpoint.getChildren().add(rot);
						rot.getChildren().add(new TreeItem<>("X="+MPoint.rotationMatrixToEulerAngles(mp.matrix)[0]));
						rot.getChildren().add(new TreeItem<>("Y="+MPoint.rotationMatrixToEulerAngles(mp.matrix)[1]));
						rot.getChildren().add(new TreeItem<>("Z="+MPoint.rotationMatrixToEulerAngles(mp.matrix)[2]));
						rot.setExpanded(true);
					}
				}
				
				bounds.getChildren().add(new TreeItem<>("X="+p.header.boundsXmin+" to X="+p.header.boundsXmax));
				bounds.getChildren().add(new TreeItem<>("Y="+p.header.boundsYmin+" to Y="+p.header.boundsYmax));
				bounds.getChildren().add(new TreeItem<>("Z="+p.header.boundsZmin+" to Z="+p.header.boundsZmax));
				
				if (p.asZones != null) {
					counts.getChildren().add(new TreeItem<>(p.asZones.zones.size()+" Autosculpt zones"));
					for (var z : p.asZones.zones) {
						asZ.getChildren().add(new TreeItem<>(Hash.getBIN(z)));
					}
				}
				
				if (p.asLinking != null) {
					counts.getChildren().add(new TreeItem<>(p.asLinking.links.size()+" Autosculpt links"));
					for (var l : p.asLinking.links) {
						var link = new TreeItem<>(Hash.getBIN(l.partKey));
						asL.getChildren().add(link);
						link.getChildren().add(new TreeItem<>("From Zone "+l.passZone1));
						link.getChildren().add(new TreeItem<>("From Zone "+l.passZone2));
						link.getChildren().add(new TreeItem<>("To Zone "+l.passZone3));
						link.getChildren().add(new TreeItem<>("To Zone "+l.passZone4));

					}
				}
				
//				p.asLinking;
//				p.asZones;
				
			}
		}catch (Exception e) {e.printStackTrace();}
	}
}

class FormattedTreeItem<T> extends TreeItem<T>{

	public String style = null;
	
	public FormattedTreeItem (T var){
		super(var);
	}
}