package fr.ni240sx.ucgt.geometryFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.collisionsEditor.OrbitCameraViewport;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.stage.Stage;

public class PartVisualizer extends Stage{

	public Group viewportGroup = new Group();
	public OrbitCameraViewport viewport;
	
//	public static ArrayList<MeshView> partMeshView = new ArrayList<MeshView>();
	
	public List<Part> partsList = new ArrayList<>();
	public ArrayList<Material> materials = new ArrayList<>();
	public ArrayList<Color> materialColors = new ArrayList<>();
		
	public PartVisualizer() {
		super();
		viewport = new OrbitCameraViewport(viewportGroup, 1024, 600);

		viewport.rotationX.setAngle(90);
		viewport.rotationY.setAngle(60);
		viewport.rotationZ.setAngle(180);
		viewport.translation.setY(-0.8);
		
        viewportGroup.getChildren().clear();
		for (Part part : partsList) {
			for (var m : part.mesh.materials.materials) {
				TriangleMesh matMesh = new TriangleMesh();
				
				matMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
				
				for (var v : m.verticesBlock.vertices) {
					matMesh.getPoints().addAll((float)v.posX, (float)v.posY, (float)v.posZ);
					matMesh.getNormals().addAll((float)v.normX, (float)v.normY, (float)v.normZ);
					matMesh.getTexCoords().addAll((float)v.tex0U, 1-(float)v.tex0V);
				}
				
				for (var tr : m.triangles) {
					matMesh.getFaces().addAll(
							// points normals   texcoords
							tr.vert0, tr.vert0, tr.vert0, //v1
							tr.vert1, tr.vert1, tr.vert1, //v2
							tr.vert2, tr.vert2, tr.vert2);//v3
				}
				
				var mv = new MeshView(matMesh);
	
				mv.setMaterial(new PhongMaterial(materialColors.get(materials.indexOf(m))));
				
	//			mv.setMaterial(new PhongMaterial() {{
	//				setDiffuseMap(new Image("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\textures\\AUD_RS4_STK_08_INTERIOR.png"));
	//				setDiffuseMap(new Image("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\textures\\AUD_RS4_STK_08_ENGINE.png"));
	//				setBumpMap(new Image("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\textures\\AUD_RS4_STK_08_ENGINE_N.png"));
	//			}});
				
				viewportGroup.getChildren().add(mv);
			}
			
			//position markers
			if (part.mpoints != null) for (var mp : part.mpoints.mpoints) {
				var mpMesh = new TriangleMesh();
				mpMesh.getTexCoords().addAll(0, 0);
				//2d losange
//				mpMesh.getPoints().addAll(
//						0.05f, 0, 0,
//						-0.05f, 0, 0,
//						0, 0.05f, 0,
//						0, -0.05f, 0);
//				mpMesh.getFaces().addAll(
//						0,0, 2,0, 3,0, 
//						3,0, 2,0, 1,0);
				
				//3d pyramid without base
				mpMesh.getPoints().addAll(
						0, 		0, 		0,
						-0.05f,	0.05f,	-0.05f,
						-0.05f,	-0.05f,	-0.05f,


						0, 		0, 		0,
						-0.05f,	-0.05f,	-0.05f,
						0.05f,	-0.05f,	-0.05f,

						0, 		0, 		0,
						0.05f,	-0.05f,	-0.05f,
						0.05f, 	0.05f,	-0.05f,

						0, 		0, 		0,
						0.05f,	0.05f,	-0.05f,
						-0.05f,	0.05f,	-0.05f
						);
				mpMesh.getFaces().addAll(
						0,0, 1,0, 2,0,
						3,0, 4,0, 5,0,
						6,0, 7,0, 8,0,
						9,0, 10,0, 11,0
						);
				
				var mpointShape = new MeshView(mpMesh);//new Box(0.1, 0.1, 0.1);
				mpointShape.setMaterial(new PhongMaterial(Color.rgb(255, 0, 0, 0.3)));
				mpointShape.setTranslateX(mp.positionX);
				mpointShape.setTranslateY(mp.positionY);
				mpointShape.setTranslateZ(mp.positionZ);
				var m = mp.matrix;
				double d = Math.acos((m[0][0] + m[1][1] + m[2][2] - 1d)/2d);
			    if(d!=0d){
			        double den=2d*Math.sin(d);
			        Point3D p= new Point3D((m[1][2] - m[2][1])/den,
			        		(m[2][0] - m[0][2])/den,
			        		(m[0][1] - m[1][0])/den);
			        mpointShape.setRotationAxis(p);
			        mpointShape.setRotate(Math.toDegrees(d));
			    }
			    viewportGroup.getChildren().add(mpointShape);
			}
		}
		viewport.buildAxes();
//		updateRender();
		
		BorderPane root = new BorderPane();
        root.setCenter(viewport);

//		viewport.widthProperty().addListener(new InvalidationListener() {
//			@Override
//			public void invalidated(Observable evt) {
//				updateRender();
//			}
//		});
//        viewport.heightProperty().addListener(new InvalidationListener() {
//			@Override
//			public void invalidated(Observable evt) {
//				updateRender();
//			}
//		});
		viewport.widthProperty().bind(root.widthProperty());
		viewport.heightProperty().bind(root.heightProperty());
		
        
        Scene scene = new Scene(root, 1024, 600);
        this.setScene(scene);
        this.show();
        
	}
	
	
//    public static void updateRender() {
//    	viewport.viewportGroup.getChildren().clear();
////    	viewport.buildAxes();
//    	viewport.viewportGroup.getChildren().addAll(partMeshView);
//    }

    public void setPartsFromFile(String geomFile, String parts) {
    	partsList.clear();
    	materials.clear();
		materialColors.clear();
    	addPartsFromFile(geomFile, parts);
    }
    
    public void addPartsFromFile(String geomFile, String parts) {
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("File loaded in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
	
			addParts(geom, parts);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void addParts(Geometry geom, String parts) {
    	for (Part p : geom.parts) {
			if (parts.contains(p.header.partName.replace(geom.carname+"_", ""))) partsList.add(p);
		}
		materials.addAll(geom.materials);
		for (int i=0; i<geom.materials.size(); i++) { //TODO match existing materials with new ones loaded with this method
			double colorR = Math.random();
			double colorG = Math.random();
			double colorB = Math.random();				
			materialColors.add(Color.color(colorR, colorG, colorB, 1));
		}
    }
    
//    public void run() {
//    	launch();
//    }
//    public void run(String[] args) {
//    	launch(args);
//    }
//	
//	public static void main(String[] args) {
//		try {
//			launch(args);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}	
//	}
}