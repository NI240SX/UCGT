package fr.ni240sx.ucgt.collisionsEditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionBoxShape;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTranslate;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundShape;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CollisionsEditor extends Application {
	
	public static Collisions mainCollisions = new Collisions();
	
	//	public static ListView<DBMPPart> partsDisplay;
//	public static VBox attributesDisplay;
	
//	public static ArrayList<CarPartListCell> carPartListCells = new ArrayList<CarPartListCell>();

	public static boolean debug = true;

//	private static Menu menuDBMP;

	//stuff to put in a config file
	public static String lastDirectoryLoaded = Paths.get("").toAbsolutePath().toString();
	public static String lastFileLoaded = "";
//	  public static String lastFileLoaded = "C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"
	public static String lastFileSaved = Paths.get("").toAbsolutePath().toString(); //now unused
	public static String lastGeomRepLoaded = Paths.get("").toAbsolutePath().toString();
	public static boolean useDarkMode = false;
	public static boolean disableWarnings = false;
//	public static boolean widebodyAutoCorrect = true;
	
	public static final String programName = "UCGT Collisions Editor";
	public static final String programVersion = "1.0.0";
	public static final String settingsFolder = "settings";
	public static final String settingsFile = "settings/colseditor.dat";
	
	public static Group viewportGroup = new Group();
	public static OrbitCameraViewport viewport;

	public static Geometry visualModel = null;
	public static ArrayList<Color> visualModelMatColors = null;
	public static Group modelGroup = new Group();
	
	public static BooleanProperty renderVisualModel = new SimpleBooleanProperty(true);
	public static BooleanProperty renderBounds = new SimpleBooleanProperty(true);
	public static BooleanProperty renderPivots = new SimpleBooleanProperty(true);
	
    public static TreeView<CollisionBound> hierarchy = new TreeView<>();

    public static PlaneControls planeControls = new PlaneControls();
    public static BoundControls boundControls = new BoundControls();
    
	public static void main(String[] args) {
		try {
			readSettings();
		} catch (@SuppressWarnings("unused") Exception e) {}
		
		try {
			mainCollisions = new Collisions(new File(lastDirectoryLoaded + lastFileLoaded));
		} catch (Exception e){
			System.out.println("Error loading the previous file.");
			e.printStackTrace();
		}
		
		
        launch(args);
        try {
			writeSettings();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Could not save config");
		}
	}

	public static void writeSettings() throws IOException {
		Files.createDirectories(Paths.get(settingsFolder));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(settingsFile)));
		bw.write("DO NOT MANUALLY EDIT THIS FILE\n" 
				+ lastDirectoryLoaded + "\n" 
				+ lastFileLoaded + "\n" 
				+ lastFileSaved + "\n" 
				+ Boolean.toString(useDarkMode) + "\n"
				+ lastGeomRepLoaded + "\n"
				+ Boolean.toString(disableWarnings) + "\n");
		bw.close();
	}

	public static void readSettings() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(settingsFile)));
		br.readLine(); //="DO NOT MANUALLY EDIT THIS FILE"
		lastDirectoryLoaded = br.readLine();
		lastFileLoaded = br.readLine();
		lastFileSaved = br.readLine();
		useDarkMode = Boolean.valueOf(br.readLine());
//			widebodyAutoCorrect = Boolean.valueOf(br.readLine());
		String s;
		if(!(s=br.readLine()).isBlank()) lastGeomRepLoaded = s;
		disableWarnings = Boolean.valueOf(br.readLine());
		
		br.close();
	}
	
    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle(programName + " - " + mainCollisions.carname);

        VBox windowTop = new VBox();
        
        //
        // MENUS BAR
        //
        
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("File");
        MenuItem fileLoad = new MenuItem("Load");
        MenuItem fileNew = new MenuItem("New");
//        MenuItem fileGenerateNew = new MenuItem("Generate new");
        MenuItem fileSave = new MenuItem("Save");
        MenuItem fileLoadVisual = new MenuItem("Load visual model");
        MenuItem fileExit = new MenuItem("Exit");
        menuFile.getItems().addAll(fileLoad, fileNew, /*fileGenerateNew,*/ fileSave, fileLoadVisual, fileExit);

        Menu menuDisplay = new Menu("Display");
        CheckMenuItem displayBounds = new CheckMenuItem("Show bounds");
        CheckMenuItem displayVisual = new CheckMenuItem("Show visual model");
        CheckMenuItem displayPivots = new CheckMenuItem("Show pivot points");
        menuDisplay.getItems().addAll(displayBounds, displayPivots, displayVisual);
        
        Menu menuSettings = new Menu(programName);
        CheckMenuItem settingsDark = new CheckMenuItem("Dark mode");
        CheckMenuItem settingsWarnings = new CheckMenuItem("Disable warning pop-ups");
        MenuItem settingsAbout = new MenuItem("About " + programName + "...");
        menuSettings.getItems().addAll(settingsDark, settingsWarnings, settingsAbout);

        menuBar.getMenus().addAll(menuFile, menuDisplay, menuSettings);
        
        renderVisualModel.addListener(c -> updateVisualModel());
		renderBounds.addListener(e -> updateBoundsView());
		renderPivots.addListener(e -> updatePivotsView());
		
        handleFileLoad(primaryStage, fileLoad);
        handleFileNew(primaryStage, fileNew);
//        fileGenerateNew.setOnAction(e -> {
////        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create a new DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
////			if (ButtonType.YES.equals(sure)) {
////				
////			}
//        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
//        });
//        fileGenerateNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        handleFileSave(fileSave);
        handleFileExit(primaryStage, fileExit);
        handleFileLoadVisual(fileLoadVisual);

        displayBounds.selectedProperty().bindBidirectional(renderBounds);
        displayVisual.selectedProperty().bindBidirectional(renderVisualModel);
        displayPivots.selectedProperty().bindBidirectional(renderPivots);
        
        handleSettingsWarnings(settingsWarnings);
        handleSettingsAbout(settingsAbout);

        
        
        //
        // SHORTCUTS BAR
        //
        
        
        
        HBox shortcutsBar = new HBox();
        
        Separator infSep = new Separator();
        HBox.setHgrow(infSep, Priority.ALWAYS);
        
    	Button boundsFromModel = new Button("Update bounds from visual model");
        
    	handleSettingsBoundsFromModel(boundsFromModel);
        
//        shortcutsBar.getChildren().addAll(new Separator());

//        if(debug) {
//        	Button debugDBMPDisplay = new Button("DISPLAY COLLISIONS");
//        	Button debug3D = new Button("3D DEBUG");
////        	Slider debugColPlaneCarWidth = new Slider(0, 5, 0);
////        	Slider debugColPlaneCarLength = new Slider(0, 15, 0);
//            shortcutsBar.getChildren().addAll(debugDBMPDisplay, debug3D);
//        	
//        	debugDBMPDisplay.setOnAction(e->{	
//        		mainCollisions.printInfo();
//        		e.consume();
//        	});
//        	debug3D.setOnAction(e->{
////        		System.out.println("Viewport :\nRotation X : "+viewport.rotationX+"\nRotation Y : "+viewport.rotationY+"\nRotation Z : "+viewport.rotationZ);
//        		e.consume();
//        	});
//        }
        
        shortcutsBar.getChildren().addAll(boundsFromModel, infSep, planeControls);
        
        windowTop.getChildren().addAll(menuBar, shortcutsBar);
        
        viewport = new OrbitCameraViewport(viewportGroup, 1024, 600);

        viewport.setOnMouseReleased(e -> {
        	if (e.isDragDetect()) {
        		planeControls.setPlane(null);
        	}
        	System.gc();
        });
//        viewport.setOnContextMenuRequested(e -> {
//			var cm = new ContextMenu();
//			var update = new MenuItem("Force update render");
//			update.setOnAction(e2 -> {
//				this.updateRender();
//			});
//			cm.getItems().addAll(update);
//			cm.show(viewport, e.getScreenX(), e.getScreenY());
//		});
        
        hierarchy.setCellFactory(lv -> {
        	BoundCell cell = new BoundCell() {
              @Override
            protected void updateItem(CollisionBound item, boolean empty) {
                  super.updateItem(item, empty);
              }
          };
          return cell;
        });
        
        
        hierarchy.setOnKeyPressed(e -> {
        	if (hierarchy.getSelectionModel().getSelectedItem() != null)
        		CollisionsEditor.boundControls.setBound(hierarchy.getSelectionModel().getSelectedItem().getValue());
        	else
        		CollisionsEditor.boundControls.setBound(null);
        	
        	if (e.getCode() == KeyCode.ENTER) {
        		hierarchy.getSelectionModel().getSelectedItem().getValue().render.set(!hierarchy.getSelectionModel().getSelectedItem().getValue().render.get());
        		BoundCell.allCells.forEach(c -> c.update(c.getItem(), false));
        	}
        });
        hierarchy.setOnMouseClicked(e -> {
        	if (hierarchy.getSelectionModel().getSelectedItem() != null) 
        		CollisionsEditor.boundControls.setBound(hierarchy.getSelectionModel().getSelectedItem().getValue());
        	else
        		CollisionsEditor.boundControls.setBound(null);
//        	if (e.isControlDown()) {
//	        	getItem().render.set(!getItem().render.get());
//	        	checkBox.setSelected(getItem().render.get());
//        	}
//        	CollisionsEditor.boundControls.setBound(getItem());
        });
        hierarchy.setOnContextMenuRequested(e -> {
        	ContextMenu cm = new ContextMenu();
        	if (hierarchy.getSelectionModel().getSelectedItem() != null) {
        		hierarchy.getSelectionModel().getSelectedItem().getValue().addBoundToContextMenu(cm);
        	}
        	cm.show(hierarchy, e.getScreenX(), e.getScreenY());
        });
        hierarchy.setOnMouseMoved(e -> hierarchy.requestFocus());
        
        
        
        StackPane sp = new StackPane();
        sp.getChildren().add(viewport);
        
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(hierarchy, sp, boundControls);
        
        BorderPane root = new BorderPane();
        root.setTop(windowTop);
//        root.setCenter(viewport);
        root.setCenter(centerPane);

//        root.setCenter(partsDisplay);
//        root.setRight(attributesDisplay);
//        root.setRight(scrollPaneAttrib);
//        root.setBottom(statusBar);
        
        root.setOnKeyPressed(e -> {
        	if (e.getCode() == KeyCode.A) {
        		CollisionsEditor.mainCollisions.mainBound.renderSetRecursively(true);
    			renderVisualModel.set(true);
//        		CollisionsEditor.updateRender();
        	}
        });

//		viewport.widthProperty().addListener(i -> {
//			updateRender();
//		});
//        viewport.heightProperty().addListener(i -> {
//			updateRender();
//		});
		viewport.widthProperty().bind(root.widthProperty());
		viewport.heightProperty().bind(root.heightProperty());
		

		updateAllBoundsDisplay();
        updateRender();
        
        
        Scene scene = new Scene(root, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.NO.equals(sure)) {
				e.consume();
			}
        });

//        hierarchy.setMinWidth(0);
//        hierarchy.setPrefWidth(400);
//        hierarchy.setMaxWidth(400);
//        boundControls.setMinWidth(0);
//        boundControls.setPrefWidth(100);
//        boundControls.setMaxWidth(400);
        hierarchy.setMaxWidth(scene.getWidth()*0.4);
//        boundControls.setMaxWidth(scene.getWidth()*0.4);
        boundControls.setMaxWidth(350);
        sp.setMinWidth(scene.getWidth()*0.4);
        sp.setPrefWidth(scene.getWidth()*0.8);
//        sp.setPrefWidth(1000);
        
        primaryStage.show();
        
        handleSettingsDark(settingsDark, scene);
    }

	public void handleSettingsBoundsFromModel(Button boundsFromModel) {
		boundsFromModel.setOnAction(e -> {
			try {
			if (visualModel != null) {
				for (var b : mainCollisions.mainBound.getChildrenRecursively()) {
					Part correspPart = null;
					
					for (var p : visualModel.parts) if (p.kit.equals("KIT00") && p.lod.equals("A") && p.part.contains(Hash.getVLT(b.NameHash))) {
						correspPart = p;
						break;
					}
					if (correspPart == null) for (var p : visualModel.parts) if (p.lod.equals("A") && p.part.contains(Hash.getVLT(b.NameHash))) {
						correspPart = p;
						break;
					}
					
					if (b.Shape == BoundShape.KSHAPE_BOX && correspPart != null) {
						if (b.shapeTranslate == null) b.shapeTranslate = new CollisionConvexTranslate();
						
						b.shapeTranslate.TranslationX = -(correspPart.header.boundsYmax + correspPart.header.boundsYmin)/2;
						b.shapeTranslate.TranslationY = (correspPart.header.boundsZmax + correspPart.header.boundsZmin)/2 + CollisionsEditor.mainCollisions.Z;
						b.shapeTranslate.TranslationZ = (correspPart.header.boundsXmax + correspPart.header.boundsXmin)/2 + CollisionsEditor.mainCollisions.X;
						b.shapeTranslate.TranslationW = 1.0f;
						
						b.shapeTransform = null;
						
						((CollisionBoxShape)b.collisionShape).HalfExtentsX = .9f * (correspPart.header.boundsYmax - correspPart.header.boundsYmin - 0.02f)/2;
						((CollisionBoxShape)b.collisionShape).HalfExtentsY = .9f *  (correspPart.header.boundsZmax - correspPart.header.boundsZmin - 0.02f)/2;
						((CollisionBoxShape)b.collisionShape).HalfExtentsZ = .9f *  (correspPart.header.boundsXmax - correspPart.header.boundsXmin - 0.02f)/2;
						((CollisionBoxShape)b.collisionShape).HalfExtentsW = 1.0f;
					}
					
					b.updateShape();
					b.updateBoundAndPivot();
				}
			} else {
				
			}
			} catch (Exception ex) {
				System.out.println("Error setting bounds from model");
				ex.printStackTrace();
			}
    	});
	}

	private void updateBoundsView() {
		if (renderBounds.get()) {
			mainCollisions.mainBound.getChildrenRecursively().forEach(b -> {
				if (!viewportGroup.getChildren().contains(b.displayBound)) viewport.viewportGroup.getChildren().add(b.displayBound);
			});
		} else {
			mainCollisions.mainBound.getChildrenRecursively().forEach(b -> {
				if (viewportGroup.getChildren().contains(b.displayBound)) viewport.viewportGroup.getChildren().remove(b.displayBound);
			});
		}
	}

	private void updatePivotsView() {
		if (renderPivots.get()) {
			mainCollisions.mainBound.getChildrenRecursively().forEach(b -> {
				if (!viewportGroup.getChildren().contains(b.displayPivot)) viewport.viewportGroup.getChildren().add(b.displayPivot);
			});
		} else {
			mainCollisions.mainBound.getChildrenRecursively().forEach(b -> {
				if (viewportGroup.getChildren().contains(b.displayPivot)) viewport.viewportGroup.getChildren().remove(b.displayPivot);
			});
		}
	}

	private void handleSettingsDark(CheckMenuItem settingsDark, Scene scene) {
		//minor inconvenience
        if (useDarkMode) {
            scene.getRoot().setStyle("-fx-base:black");
            settingsDark.setSelected(true);
        }
        settingsDark.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                scene.getRoot().setStyle("-fx-base:black");
                useDarkMode = true;
            } else {
                scene.getRoot().setStyle("");
                useDarkMode = false;
                }
        });
	}

	private void handleSettingsAbout(MenuItem settingsAbout) {
		settingsAbout.setOnAction(e -> {
			Stage st = new Stage();
			st.setTitle("About " + programName);
			Scene sc = new Scene(new Label(programName + " version " + programVersion + "\n\n"
					+ "Advanced Collisions editor for NFS Undercover.\n"
					+ "Aims to make it possible to edit such data.\n"
					+ "This software has been originally created for the mod Undercover Exposed.\n\n"
					+ "Not affiliated with EA, MaxHwoy, nfsu360, etc.\n\n"
					+ "needeka 2025 - No rights reserved"));
			st.setScene(sc);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setResizable(false);
			st.setAlwaysOnTop(true);
			sc.setOnMouseClicked(evt -> {
				evt.consume();
				st.close();
			});
			st.show();
        });
	}

	private void handleSettingsWarnings(CheckMenuItem settingsWarnings) {
		if (disableWarnings) settingsWarnings.setSelected(true);
        settingsWarnings.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                disableWarnings = true;
            } else {
            	disableWarnings= false;
                }
        });
	}

	private void handleFileLoadVisual(MenuItem fileLoadVisual) {
		fileLoadVisual.setOnAction(evt -> {
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File(lastGeomRepLoaded));
				fc.setInitialFileName("GEOMETRY.BIN");
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("BIN Geometry file", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load a visual model");
				Geometry load;
				File selected = fc.showOpenDialog(null);
				if (selected != null) {
					try {
						load = Geometry.load(selected);
						visualModel = load;
						visualModelMatColors = new ArrayList<>();
//							lastFileLoaded = selected.getName();
							lastGeomRepLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
//							  System.out.println(mainDBMP);
//							menuDBMP.setText(mainCollisions.carname.label);

			    		for (var m :visualModel.materials) {
			    			if (Hash.getBIN(m.ShaderHash).contains("WINDSHIELD")) {
				    			visualModelMatColors.add(Color.color(0.2, 0.2, 0.2, 0.3));
			    			} else {
				    			double colorR = Math.random();
				    			double colorG = Math.random();
				    			double colorB = Math.random();				
				    			visualModelMatColors.add(Color.color(colorR, colorG, colorB, 1));
			    			}
			    		}
			    		modelGroup.getChildren().clear();
			    		if (viewportGroup.getChildren().contains(modelGroup)) viewportGroup.getChildren().remove(modelGroup);
			    		
						for (var p : visualModel.parts) if (p.kit.equals("KIT00") && p.lod.equals("A") && !(p.part.contains("WHEEL") || p.part.contains("BRAKE") || p.part.contains("DRIVER") 
								|| p.part.contains("SEAT") || p.part.contains("SPOILER_LIP") || p.part.contains("SPOILER_EVO") || p.part.contains("SPOILER_DRAG") || p.part.contains("SPOILER_BASE")
								|| p.part.contains("ROLL_CAGE") || (p.part.charAt(p.part.length()-3) == '_' && !p.part.endsWith("00")) )) {
							for (var m : p.mesh.materials.materials) {
								TriangleMesh matMesh = new TriangleMesh();
								
								matMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
								
								for (var v : m.verticesBlock.vertices) {
									matMesh.getPoints().addAll((float)v.posY, (float)v.posZ, (float)v.posX);
									matMesh.getNormals().addAll((float)v.normY, (float)v.normZ, (float)v.normX);
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
					
								mv.setMaterial(new PhongMaterial(visualModelMatColors.get(visualModel.materials.indexOf(m))));
								modelGroup.getChildren().add(mv);
							}
						}
						modelGroup.setOnMousePressed(e -> {
							if (e.isPrimaryButtonDown()) modelGroup.getChildren().forEach(c -> {
								((PhongMaterial) ((Shape3D) c).getMaterial()).setDiffuseColor(
										Color.color( ((PhongMaterial) ((Shape3D) c).getMaterial()).getDiffuseColor().getRed(), 
												((PhongMaterial) ((Shape3D) c).getMaterial()).getDiffuseColor().getGreen(), 
												((PhongMaterial) ((Shape3D) c).getMaterial()).getDiffuseColor().getBlue(), 0.2));
							});
							if (e.isSecondaryButtonDown()) modelGroup.getChildren().forEach(c -> {
								((PhongMaterial) ((Shape3D) c).getMaterial()).setDiffuseColor(
										Color.color( ((PhongMaterial) ((Shape3D) c).getMaterial()).getDiffuseColor().getRed(), 
												((PhongMaterial) ((Shape3D) c).getMaterial()).getDiffuseColor().getGreen(), 
												((PhongMaterial) ((Shape3D) c).getMaterial()).getDiffuseColor().getBlue(), 1.0));
							});
						});
						
			    		renderVisualModel.set(true);
			    		updateVisualModel();
//						updateRender();	
						if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Visual model loaded successfully.", ButtonType.OK).show();
					} catch (Exception e) {
						new Alert(Alert.AlertType.ERROR, "Error loading the visual model:\n"+e.getMessage(), ButtonType.OK).show();
						e.printStackTrace();
			        	System.gc();
					}
				}
	        	System.gc();
        });
        fileLoadVisual.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+O"));
	}

	private void handleFileExit(Stage primaryStage, MenuItem fileExit) {
		fileExit.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings)  sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				primaryStage.close();
			}
        });
        fileExit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
	}

	private void handleFileSave(MenuItem fileSave) {
		fileSave.setOnAction(e -> {
    		FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastDirectoryLoaded));
			fc.setInitialFileName(mainCollisions.carname);
			fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("Binary serialized collisions", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
			fc.setTitle("Save " + mainCollisions.carname+ "");
			

			try {
				File f = fc.showSaveDialog(null);
				lastFileSaved = f.getAbsolutePath().replace(lastFileLoaded, "");
				File f_old = f;
				f_old.renameTo(new File(f_old.getAbsoluteFile() + ".bak_" + DateTimeFormatter.ofPattern("uuMMdd-HHmmss").format(LocalDateTime.ofEpochSecond(f_old.lastModified(), 0, ZoneOffset.UTC))));
				mainCollisions.saveToFile(f);
				if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Database saved successfully.", ButtonType.OK).show();
	    		e.consume();        
			} catch (@SuppressWarnings("unused") Exception e1) {
            	new Alert(Alert.AlertType.WARNING, "Error saving collisions, please try again !").show();
			}
        });
        fileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
	}

	private void handleFileNew(Stage primaryStage, MenuItem fileNew) {
		fileNew.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create blank collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				mainCollisions = new Collisions();
				updateAllBoundsDisplay();
				primaryStage.setTitle("fire - "+mainCollisions.carname);
				renderVisualModel.set(false);
				updateRender();	
//				menuDBMP.setText(mainCollisions.carname.label);
			}
        });
        fileNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
	}

	private void handleFileLoad(Stage primaryStage, MenuItem fileLoad) {
		fileLoad.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to load new collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File(lastDirectoryLoaded));
				fc.setInitialFileName(lastFileLoaded);
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("Binary serialized collisions", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load existing Collisions");
				Collisions loadDBMP;
				File selected = fc.showOpenDialog(null);
				if (selected != null) {
					loadDBMP = new Collisions(selected);
					mainCollisions = loadDBMP;
					lastFileLoaded = selected.getName();
					lastDirectoryLoaded = selected.getAbsolutePath().replace(lastFileLoaded, "");
//						  System.out.println(mainDBMP);
					updateAllBoundsDisplay();
					primaryStage.setTitle(programName + " - " + mainCollisions.carname);
					
					renderVisualModel.set(false);
//						menuDBMP.setText(mainCollisions.carname.label);
					updateRender();	
					if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Collisions loaded successfully.", ButtonType.OK).show();
				}
			}
        	System.gc();
        });
        fileLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
	}

	public void updateVisualModel() {
		if (renderVisualModel.get()) {
			if (modelGroup != null && !viewportGroup.getChildren().contains(modelGroup)) viewportGroup.getChildren().add(modelGroup);
		} else {
			if (modelGroup != null && viewportGroup.getChildren().contains(modelGroup)) viewportGroup.getChildren().remove(modelGroup);
		}
	}
    
    public static void updateRender() {
    	viewport.viewportGroup.getChildren().clear();
    	viewport.buildAxes();
    	
    	if (CollisionsEditor.mainCollisions.mainBound != null) for(CollisionBound b : CollisionsEditor.mainCollisions.mainBound.getChildrenRecursively()) {
        	b.updateShape();
        	if (renderPivots.get()) viewport.viewportGroup.getChildren().addAll(b.displayPivot);
    		if (b.render.get()) viewport.viewportGroup.getChildren().addAll(b.displayShape);
    		if (renderBounds.get()) viewport.viewportGroup.getChildren().addAll(b.displayBound);
    	}
    	System.gc();
    	
    }
    
    public static void updateAllBoundsDisplay() {
    	if (mainCollisions.mainBound != null) {
        	hierarchy.setRoot(mainCollisions.mainBound.makeTreeViewRecursively());
        	hierarchy.getRoot().setExpanded(true);    		
    	}
    }
}
