package fr.ni240sx.ucgt.collisionsEditor;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.U;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.V;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.X;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Y;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Z;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.ni240sx.ucgt.collisionsEditor.BoundEnums.BoundShape;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.hkpBoxShape;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.hkpConvexTransformShape;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.hkpConvexTranslateShape;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.hkpConvexVerticesShape;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.hkpShape;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.scgBounds;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.geometry.Padding;
import fr.ni240sx.ucgt.geometryFile.gui.FileOffset;
import fr.ni240sx.ucgt.geometryFile.gui.SearchableListView;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Hash;
import fr.ni240sx.ucgt.shared.OrbitCameraViewport;
import fr.ni240sx.ucgt.shared.RandomAccessFileLE;
import fr.ni240sx.ucgt.shared.Utils;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.BoundsPack;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import tech.dashman.dashman.StageSizer;

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
	
	public static Group viewportGroup = new Group();
	public static OrbitCameraViewport viewport;

	public static Group boundsGroup = new Group();
	public static Group pivotsGroup = new Group();
	public static Group shapesGroup = new Group();
	
	public static Geometry visualModel = null;
	public static ArrayList<Color> visualModelMatColors = null;
	public static Group modelGroup = new Group();
	
	public static BooleanProperty renderVisualModel = new SimpleBooleanProperty(true);
	public static BooleanProperty renderBounds = new SimpleBooleanProperty(true);
	public static BooleanProperty renderPivots = new SimpleBooleanProperty(true);
    public static CheckMenuItem displayShapes = new CheckMenuItem("Show bound shapes");
	
    public static TreeView<scgBounds> hierarchy = new TreeView<>();

    public static PlaneControls planeControls = new PlaneControls();
    public static BoundControls boundControls = new BoundControls();
    
    public static StageSizer stageSizer = new StageSizer();
    public static CheckMenuItem trackpadMode = new CheckMenuItem("Trackpad mode");
    public static ArrayList<String> recentFiles = new ArrayList<>();
    public static final int maxRecentFiles = 24;
    public static ArrayList<Stage> openWindows = new ArrayList<>();

	public static void main(String[] args) {
		try {
			readSettings();
		} catch (@SuppressWarnings("unused") Exception e) {}
				
        launch(args);
        try {
			writeSettings();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Could not save config");
		}
	}

	public static void writeSettings() throws IOException {
		Files.createDirectories(Paths.get(Utils.settingsFolder));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Utils.settingsColsEd)));
		bw.write("DO NOT MANUALLY EDIT THIS FILE\n" 
				+ lastDirectoryLoaded + "\n" 
				+ lastFileLoaded + "\n" 
				+ lastFileSaved + "\n" 
				+ Boolean.toString(useDarkMode) + "\n"
				+ lastGeomRepLoaded + "\n"
				+ Boolean.toString(disableWarnings) + "\n"
				+ stageSizer.getHeight() + "\n"
				+ stageSizer.getWidth() + "\n"
				+ stageSizer.getX() + "\n"
				+ stageSizer.getY() + "\n"
				+ stageSizer.getMaximized() + "\n"
				+ Boolean.toString(trackpadMode.isSelected()) + "\n");
		for (var f : (recentFiles.size() > maxRecentFiles ? recentFiles.subList(0, maxRecentFiles) : recentFiles)) bw.write(f+">");
		bw.write("\n");
		bw.close();
	}

	public static void readSettings() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(Utils.settingsColsEd)));
		br.readLine(); //="DO NOT MANUALLY EDIT THIS FILE"
		lastDirectoryLoaded = br.readLine();
		lastFileLoaded = br.readLine();
		lastFileSaved = br.readLine();
		useDarkMode = Boolean.valueOf(br.readLine());
//			widebodyAutoCorrect = Boolean.valueOf(br.readLine());
		String s;
		if(!(s=br.readLine()).isBlank()) lastGeomRepLoaded = s;
		disableWarnings = Boolean.valueOf(br.readLine());
		stageSizer.setHeight(Double.valueOf(br.readLine()));
		stageSizer.setWidth(Double.valueOf(br.readLine()));
		stageSizer.setX(Double.valueOf(br.readLine()));
		stageSizer.setY(Double.valueOf(br.readLine()));
		stageSizer.setMaximized(Boolean.valueOf(br.readLine()));
		trackpadMode.setSelected(Boolean.valueOf(br.readLine()));
		for (var s2 : br.readLine().split(">")) if (s2 != null && !s2.isBlank()) recentFiles.add(s2);
		
		br.close();
	}
	
    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle(Utils.collisions_Name + " - " + mainCollisions.carname);

        VBox windowTop = new VBox();
        
        //
        // MENUS BAR
        //
        
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("File");
        MenuItem fileNew = new MenuItem("New");
        MenuItem fileLoad = new MenuItem("Open...");
        MenuItem fileReload = new MenuItem("Reload");
        Menu fileLoadRecent = new Menu("Open recent...");
        MenuItem fileSave = new MenuItem("Save");
        MenuItem fileSaveAs = new MenuItem("Save as...");
        MenuItem fileLoadVisual = new MenuItem("Load visual model...");
        MenuItem fileExit = new MenuItem("Exit");
        menuFile.getItems().addAll(fileNew, fileLoad, fileReload, fileLoadRecent, fileSave, fileSaveAs, fileLoadVisual, fileExit);

        Menu menuDisplay = new Menu("Display");
        CheckMenuItem displayBounds = new CheckMenuItem("Show bounds");
        CheckMenuItem displayVisual = new CheckMenuItem("Show visual model");
        CheckMenuItem displayPivots = new CheckMenuItem("Show bound pivots");
        menuDisplay.getItems().addAll(displayShapes, displayBounds, displayPivots, displayVisual);
        displayShapes.setSelected(true);
        
        Menu menuSettings = new Menu(Utils.collisions_Name);
        CheckMenuItem settingsDark = new CheckMenuItem("Dark mode");
        CheckMenuItem settingsWarnings = new CheckMenuItem("Disable warning pop-ups");
        MenuItem settingsAbout = new MenuItem("About " + Utils.collisions_Name + "...");
        menuSettings.getItems().addAll(settingsDark, trackpadMode, settingsWarnings, settingsAbout);

        menuBar.getMenus().addAll(menuFile, menuDisplay, menuSettings);
        
        renderVisualModel.addListener(c -> updateVisualModel());
		renderBounds.addListener(e -> updateBoundsView());
		renderPivots.addListener(e -> updatePivotsView());
		displayShapes.selectedProperty().addListener((obs, was, is) -> {
			if (is && !viewportGroup.getChildren().contains(shapesGroup)) viewportGroup.getChildren().add(shapesGroup);
			if (!is && viewportGroup.getChildren().contains(shapesGroup)) viewportGroup.getChildren().remove(shapesGroup);
		});
		
        menuFile.setOnShowing(e -> {
        	fileLoadRecent.getItems().clear();
        	for (var f : recentFiles) {
        		var i = new MenuItem(f);
        		i.setOnAction(evt ->{
        	        ButtonType sure = ButtonType.YES;
        	        if (!disableWarnings) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to load new collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
        			if (ButtonType.YES.equals(sure)) {

        				File selected = new File(f);
        				if (selected.isFile()) {
        					try {
        						loadByFileType(primaryStage, selected);
        						
        					} catch (Exception ex) {
        						new Alert(Alert.AlertType.ERROR, "Error loading the collisions:\n"+ex.getMessage(), ButtonType.OK).show();
        						ex.printStackTrace();
        			        	System.gc();
        					}
        				}
        	        	System.gc();
        			}

        		});
        		fileLoadRecent.getItems().add(i);
        	}
        });
        handleFileLoad(primaryStage, fileLoad);
        handleFileNew(primaryStage, fileNew);
        fileReload.setOnAction(e -> {
        	if (new File(lastDirectoryLoaded + lastFileLoaded).isFile()) loadByFileType(primaryStage, new File(lastDirectoryLoaded + lastFileLoaded));
        });
        fileReload.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
//        fileGenerateNew.setOnAction(e -> {
////        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create a new DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
////			if (ButtonType.YES.equals(sure)) {
////				
////			}
//        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
//        });
//        fileGenerateNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        fileSave.setOnAction(e -> {
        	if (new File(lastDirectoryLoaded + lastFileLoaded).isFile()) saveFile(new File(lastDirectoryLoaded + lastFileLoaded));
        });
        fileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        handleFileSaveAs(fileSaveAs);
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
        viewport.trackpadMode.bind(trackpadMode.selectedProperty());
//        viewport.setOnContextMenuRequested(e -> {
//			var cm = new ContextMenu();
//			var update = new MenuItem("Force update render");
//			update.setOnAction(e2 -> {
//				this.updateRender();
//			});
//			cm.getItems().addAll(update);
//			cm.show(viewport, e.getScreenX(), e.getScreenY());
//		});
        viewport.buildAxes();
        viewportGroup.getChildren().addAll(boundsGroup, shapesGroup, pivotsGroup);
        
        hierarchy.setCellFactory(lv -> {
        	BoundCell cell = new BoundCell() {
              @Override
            protected void updateItem(scgBounds item, boolean empty) {
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
        	
        	if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.H) {
        		hierarchy.getSelectionModel().getSelectedItem().getValue().render.set(!hierarchy.getSelectionModel().getSelectedItem().getValue().render.get());
        		BoundCell.allCells.forEach(c -> c.update(c.getItem(), false));
        	}
        	if (e.getCode() == KeyCode.ESCAPE) {
        		hierarchy.getSelectionModel().clearSelection();
        		CollisionsEditor.boundControls.setBound(null);
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
        		if (mainCollisions != null) mainCollisions.scg.root.renderSetRecursively(true);
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
        viewport.movementEnabled = false;
		viewport.widthProperty().bind(root.widthProperty());
		viewport.heightProperty().bind(root.heightProperty());
		

		updateAllBoundsDisplay();
//        updateRender();
        
        
        Scene scene = new Scene(root, 1024, 600);
        stageSizer.setStage(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.NO.equals(sure)) {
				e.consume();
			} else {
				openWindows.forEach(w -> w.close());
			}
        });
        
        scene.setOnDragOver(e -> {
        	if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.ANY);
        	e.consume();
        });
    	scene.setOnDragDropped(e -> {
    		try {
	    		if (e.getDragboard().hasFiles()) {
	    			loadByFileType(primaryStage, e.getDragboard().getFiles().get(0));
	    		}
	    		e.consume();
    		} catch (Exception ex) {
    			ex.printStackTrace();
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
        primaryStage.getScene().getRoot().mouseTransparentProperty().bind(primaryStage.focusedProperty().not()); //fix tooltips

        try {
			if (new File(lastDirectoryLoaded + lastFileLoaded).isFile())
				loadByFileType(primaryStage, new File(lastDirectoryLoaded + lastFileLoaded));
		} catch (Exception e){
			new Alert(AlertType.ERROR, "Error loading the previous collisions:\n%s".formatted(e.getMessage()), ButtonType.OK).show();
			e.printStackTrace();
		}
        
        try {
    		Hash.addBinHashes(new File("data/collisionshashes"));
    		Hash.addVltHashes(new File("data/collisionshashes"));
        } catch(Exception e) {e.printStackTrace();}
        
        handleSettingsDark(settingsDark, scene);
    }


	public static void handleSettingsBoundsFromModel(Button boundsFromModel) {
		boundsFromModel.setOnAction(e -> {
			try {
			if (visualModel != null) {
				for (var b : mainCollisions.scg.root.getChildrenRecursively()) {
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
						if (b.collisionShape == null || b.collisionShape.getClass() != hkpConvexTranslateShape.class ) {
							if (b.collisionShape != null && b.collisionShape.getClass() == hkpConvexTransformShape.class ) {
								var s = (hkpConvexTransformShape)b.collisionShape;
								b.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTranslateShape();
								((hkpConvexTranslateShape)b.collisionShape).childShape = s;
							} else {
								var s = b.collisionShape;
								b.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTranslateShape();
								((hkpConvexTranslateShape)b.collisionShape).childShape = s;
							}
						}
						var shapeTranslate = ((hkpConvexTranslateShape)b.collisionShape);
						
						shapeTranslate.TranslationX = -(correspPart.header.boundsYmax + correspPart.header.boundsYmin)/2 - b.getRenderNode().Y;
						shapeTranslate.TranslationY = (correspPart.header.boundsZmax + correspPart.header.boundsZmin)/2 + b.getRenderNode().Z;
						shapeTranslate.TranslationZ = (correspPart.header.boundsXmax + correspPart.header.boundsXmin)/2 + b.getRenderNode().X;
						shapeTranslate.TranslationW = 1.0f;
												
						hkpShape colShape = b.getShape();
						((hkpBoxShape)colShape).HalfExtentsX = .9f * (correspPart.header.boundsYmax - correspPart.header.boundsYmin - 0.02f)/2;
						((hkpBoxShape)colShape).HalfExtentsY = .9f *  (correspPart.header.boundsZmax - correspPart.header.boundsZmin - 0.02f)/2;
						((hkpBoxShape)colShape).HalfExtentsZ = .9f *  (correspPart.header.boundsXmax - correspPart.header.boundsXmin - 0.02f)/2;
						((hkpBoxShape)colShape).HalfExtentsW = 1.0f;
					}
					
					b.compute();
					b.updateShape();
				}
			} else {
				
			}
			} catch (Exception ex) {
				System.out.println("Error setting bounds from model");
				ex.printStackTrace();
			}
    	});
	}


	private static void updateBoundsView() {
		if (renderBounds.get()) {
			if (!viewportGroup.getChildren().contains(boundsGroup)) viewportGroup.getChildren().add(boundsGroup);
		} else {
			if (viewportGroup.getChildren().contains(boundsGroup)) viewportGroup.getChildren().remove(boundsGroup);
		}
	}


	private static void updatePivotsView() {
		if (renderPivots.get()) {
			if (!viewportGroup.getChildren().contains(pivotsGroup)) viewportGroup.getChildren().add(pivotsGroup);
		} else {
			if (viewportGroup.getChildren().contains(pivotsGroup)) viewportGroup.getChildren().remove(pivotsGroup);
		}
	}


	private static void handleSettingsDark(CheckMenuItem settingsDark, Scene scene) {
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


	private static void handleSettingsAbout(MenuItem settingsAbout) {
		settingsAbout.setOnAction(e -> {
			Stage st = new Stage();
			st.setTitle("About " + Utils.collisions_Name);
			Scene sc = new Scene(new Label(Utils.collisions_Name + " version " + Utils.collisions_Version + "\n\n"
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


	private static void handleSettingsWarnings(CheckMenuItem settingsWarnings) {
		if (disableWarnings) settingsWarnings.setSelected(true);
        settingsWarnings.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                disableWarnings = true;
            } else {
            	disableWarnings= false;
                }
        });
	}


	private static void handleFileLoadVisual(MenuItem fileLoadVisual) {
		fileLoadVisual.setOnAction(evt -> {
				FileChooser fc = new FileChooser();
				if (new File(lastGeomRepLoaded).isFile()) fc.setInitialDirectory(new File(lastGeomRepLoaded));
				fc.setInitialFileName("GEOMETRY.BIN");
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("BIN Geometry file", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load a visual model");
				File selected = fc.showOpenDialog(null);
				if (selected != null) {
					try {
						loadVisualModel(selected);

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


	public static void loadVisualModel(File f) throws Exception {
		Geometry load;
		load = Geometry.load(f);
		visualModel = load;
		visualModelMatColors = new ArrayList<>();
//							lastFileLoaded = selected.getName();
			lastGeomRepLoaded = f.getAbsolutePath().replace(f.getName(), "");
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
//		if (viewportGroup.getChildren().contains(modelGroup)) viewportGroup.getChildren().remove(modelGroup);
		
		for (var p : visualModel.parts) if (p.kit.equals("KIT00") && p.lod.equals("A") && !(p.part.contains("WHEEL") || p.part.contains("BRAKE") || p.part.contains("DRIVER") 
				|| p.part.contains("SEAT") || p.part.contains("SPOILER_LIP") || p.part.contains("SPOILER_EVO") || p.part.contains("SPOILER_DRAG") || p.part.contains("SPOILER_BASE")
				|| p.part.contains("ROLL_CAGE") || (p.part.charAt(p.part.length()-3) == '_' && !p.part.endsWith("00")) )) {
			for (var m : p.mesh.materials.materials) {
				TriangleMesh matMesh = new TriangleMesh();
				
				matMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
				
				for (var v : m.verticesBlock.vertices) {
					matMesh.getPoints().addAll(v.pos[X], v.pos[Y], v.pos[Z]);
				}
				if (m.verticesBlock.vertexFormat.hasNormals()) for (var v : m.verticesBlock.vertices) {
					matMesh.getNormals().addAll(v.norm[X], v.norm[Y], v.norm[Z]);
				}
				if (m.verticesBlock.vertexFormat.getNumTexChannels() > 0) for (var v : m.verticesBlock.vertices) {
					matMesh.getTexCoords().addAll(v.tex[0][U], 1-v.tex[0][V]);
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
		modelGroup.getTransforms().clear();
		modelGroup.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		modelGroup.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		modelGroup.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
		
		renderVisualModel.set(true);
		updateVisualModel();
//						updateRender();	
	}
	

	private static void handleFileExit(Stage primaryStage, MenuItem fileExit) {
		fileExit.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings)  sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				primaryStage.close();
			}
        });
        fileExit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
	}


	private static void handleFileSaveAs(MenuItem fileSave) {
		fileSave.setOnAction(e -> {
//			new Alert(Alert.AlertType.WARNING, "Saving files not supported!", ButtonType.OK).show();
			
    		FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastFileSaved).exists() ? new File(lastFileSaved) : new File("."));
			fc.setInitialFileName(mainCollisions.carname);
			fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("Suitable files", "*.bin", "*.bun", "*.lzc"),
			        new FileChooser.ExtensionFilter("Collisions block", "*.bin"),
//			        new FileChooser.ExtensionFilter("Binary serialized collisions", "*.bin"),
			        new FileChooser.ExtensionFilter("Decompressed bundle", "*.bun", "*.lzc"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
			fc.setTitle("Save " + mainCollisions.carname+ "");

			File f = fc.showSaveDialog(null);
//			File f = fc.showOpenDialog(null);
			if (f!= null) {
				lastFileSaved = f.getParentFile().getAbsolutePath();
				saveFile(f);
			}
        });
        fileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
	}

	public static void saveFile(File f) {
		new Thread(() -> {
			try {						
				if (f.isFile()) {
					FileInputStream fis = new FileInputStream(f);
					int signature = getNextInt(fis);
					int firstBlockLength = getNextInt(fis);
					fis.close();
					
					if (firstBlockLength+8 < f.length()) { //bundle
						//FIXME crashes the game
						javafx.application.Platform.runLater(() -> openWindows.forEach(w -> w.close())); //close any bundle reader that would become invalid
						
						
						var stream = new RandomAccessFileLE(f, "rw");
						
						while (stream.getFilePointer() < stream.length()) {
							var bType = BlockType.get(stream.getInt());

							if (bType == BlockType.BoundsPack) {
								var blockLength = stream.getInt(); //+8 for outer length on disk
								var blockStartNoHeader = stream.getFilePointer()-8;
								stream.seek(blockStartNoHeader+4);
								
								var bb = ByteBuffer.allocate(blockLength+4);
								bb.order(ByteOrder.LITTLE_ENDIAN);
								stream.read(bb.array());
								
								var bp = new BoundsPack(bb);
								
								boolean replaced = false;
								for (int i=0; i<bp.subBlocks.size(); i++) {
									if (bp.subBlocks.get(i).scg.nameHash == mainCollisions.scg.nameHash) {
										bp.subBlocks.set(i, mainCollisions);
										replaced = true;
										break;
									}
								}
								if (!replaced) {
									bp.subBlocks.add(mainCollisions);
								}
								
								var bpArray = bp.save((int) (blockStartNoHeader));
								bp = null;
								
								
								
								//reference all remaining bin blocks
								var remainingBinBlocks = new ArrayList<Pair<Integer,Integer>>();
								stream.seek(blockStartNoHeader+blockLength+8);
								
								int previousPaddingEnd;
								int previousPaddingLength;
								
								//next block is a padding block, fix padding length to be modulo 128
								{
									var pos = (int) stream.getFilePointer();
									stream.getInt();
									var len = stream.getInt() + 8;
									stream.skipBytes(len-8);
									
									previousPaddingEnd = pos + len - 128;
									previousPaddingLength = len;
								}
								
								
								while (stream.getFilePointer() < stream.length()) {
									var pos = (int) stream.getFilePointer();
									stream.getInt();
									var len = stream.getInt() + 8;
									stream.skipBytes(len-8);
									remainingBinBlocks.add(new Pair<>(pos, len));
								}
								
								var padding = Padding.makePadding(blockStartNoHeader + bpArray.length - previousPaddingEnd, 128);
								
								var offset = bpArray.length - (blockLength+8) + padding.length - previousPaddingLength;

								
								//pump all remaining bin blocks
								if (offset > 0) {
									stream.setLength(stream.length() + offset);
									for (int i = remainingBinBlocks.size()-1; i>=0; i--) {
										var bytes = new byte[remainingBinBlocks.get(i).getValue()];
										stream.seek(remainingBinBlocks.get(i).getKey());
										stream.read(bytes);
										stream.seek(remainingBinBlocks.get(i).getKey()+offset);
										stream.write(bytes);
									}
								} else if (offset < 0) {
									for (int i = 0; i<remainingBinBlocks.size(); i++) {
										var bytes = new byte[remainingBinBlocks.get(i).getValue()];
										stream.seek(remainingBinBlocks.get(i).getKey());
										stream.read(bytes);
										stream.seek(remainingBinBlocks.get(i).getKey()+offset);
										stream.write(bytes);
									}
									stream.setLength(stream.length() + offset);
								}
								
								//save new bounds pack
								stream.seek(blockStartNoHeader);
								stream.write(bpArray);
								stream.write(padding);
								break;
								
							}
							stream.skipBytes(stream.getInt());
						}
						stream.close();
						
						
						
						
						
						
						
						
						
						
						
					} else if (signature == 0x42704D67 || signature == BlockType.CarCollisions.getKey() || f.length() == 0) { //Binary serialized/raw game data/empty file
						File f_old = f;
						f_old.renameTo(new File(f_old.getAbsolutePath() + ".bak_" + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date(f_old.lastModified()))));

						//TODO make serialized saving by first saving normally, gathering fixups, then saving serialized?
						var fos = new FileOutputStream(f);
						fos.write(mainCollisions.save(0));
						fos.close();
					} else {
						javafx.application.Platform.runLater(() -> new Alert(AlertType.ERROR, "Could not recognize file type!", ButtonType.OK).show());
						return;
					}
				} else {
					var fos = new FileOutputStream(f);
					fos.write(mainCollisions.save(0));
					fos.close();
				}
				
				
				//if (!disableWarnings) 
					javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Collisions saved successfully.", ButtonType.OK).show());
			} catch(Exception e1) {
		    	javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Error saving collisions:\n%s".formatted(e1.getMessage())).show());
		    	e1.printStackTrace();
			}
		}).start();
	}

	private static void handleFileNew(Stage primaryStage, MenuItem fileNew) {
		fileNew.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create blank collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				mainCollisions = new Collisions();
				updateAllBoundsDisplay();
				primaryStage.setTitle("fire - "+mainCollisions.carname);
				renderVisualModel.set(false);
				cleanupGroups();
//				updateRender();	
//				menuDBMP.setText(mainCollisions.carname.label);
			}
        });
        fileNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
	}
	
	private static void handleFileLoad(Stage primaryStage, MenuItem fileLoad) {
		fileLoad.setOnAction(evt -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to load new collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File(lastDirectoryLoaded).exists() ? new File(lastDirectoryLoaded) : new File("."));
				fc.setInitialFileName(lastFileLoaded);
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("Suitable files", "*.bin", "*.bun", "*.lzc"),
			        new FileChooser.ExtensionFilter("Collisions block", "*.bin"),
			        new FileChooser.ExtensionFilter("Binary serialized collisions", "*.bin"),
			        new FileChooser.ExtensionFilter("Decompressed bundle", "*.bun", "*.lzc"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load existing Collisions");
				File selected = fc.showOpenDialog(null);
				if (selected != null) {
					try {
						loadByFileType(primaryStage, selected);
					} catch(Exception e) {
						new Alert(AlertType.ERROR, "Error loading the collisions:\n%s".formatted(e.getMessage()), ButtonType.OK).show();
						e.printStackTrace();
					}
				}
			}
        	System.gc();
        });
        fileLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
	}

	public static void loadByFileType(Stage primaryStage, File selected) {
		new Thread(() -> {
			try {
				FileInputStream fis = new FileInputStream(selected);
				int signature = getNextInt(fis);
				int firstBlockLength = getNextInt(fis);
				fis.close();
				
				if (firstBlockLength+8 < selected.length()) { //bundle

					fis = new FileInputStream(selected);
					int inPos = 0;

					try {
					
						var colOffsets = FXCollections.<FileOffset>observableArrayList();
						while (fis.available()>0) {
							var bType = BlockType.get(getNextInt(fis));
							var bSize = getNextInt(fis);
//											System.out.println(bType + " " + bSize);
							if (bType.equals(BlockType.BoundsPack)) {
								var arr = new byte[bSize];
								fis.read(arr);
								var bb = ByteBuffer.wrap(arr);
								bb.order(ByteOrder.LITTLE_ENDIAN);
								while (bb.hasRemaining()) {
									int containerOffset = bb.position();
									bb.getInt();
									var cols = new Collisions(bb);
									int size = bb.position()-containerOffset;
									colOffsets.add(new FileOffset(inPos+8+containerOffset,size,null,Hash.getVLT(cols.scg.nameHash)));
								}
								
							} else {
								fis.skipNBytes(bSize);
							}
							inPos+=bSize+8;
						}
						fis.close();

//						javafx.application.Platform.runLater(() -> {
//							status.text.setText("Waiting on user input...");
//							status.progress.setProgress(-1);
//						});

						javafx.application.Platform.runLater(() -> {

							Stage st = new Stage();
							st.setTitle("Select files in "+selected.getName());
							openWindows.add(st);
							var sp = new SplitPane();
							var cols = new SearchableListView<>(colOffsets);
							if (colOffsets.size()>0) sp.getItems().add(cols);
							sp.setOrientation(Orientation.VERTICAL);
							Scene sc = new Scene(sp);
							st.setHeight(600);
							st.setWidth(400);
							if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
							st.setScene(sc);
							st.setOnCloseRequest(e -> {
								openWindows.remove(st);
//								javafx.application.Platform.runLater(() -> {
//									status.text.setText(null);
//									status.progress.setProgress(0);
//								});
							});
							cols.list.setOnMouseClicked(e -> {
								if (e.getClickCount() >1) {
//									st.close();
									loadBundledCollisions(primaryStage, selected, cols);
								}
							});
							cols.list.setOnKeyPressed(e -> {
								if (e.getCode() == KeyCode.ENTER)
									loadBundledCollisions(primaryStage, selected, cols);
							});
							st.show();
						});
					
					}catch (Exception e) {
						e.printStackTrace();
						javafx.application.Platform.runLater(() -> {
							new Alert(Alert.AlertType.ERROR, "Error loading the bundle:\n"+e.getMessage(), ButtonType.OK).show();
//							status.text.setText(null);
//							status.progress.setProgress(0);
						});
						fis.close();
					}
					return;
					
					
				} else if (signature == 0x42704D67) { //Binary serialized
					fis = new FileInputStream(selected);
					byte [] fileToBytes = new byte[(int)selected.length()];
					fis.read(fileToBytes);
					fis.close();
					
					mainCollisions = Collisions.readSerialized(fileToBytes);

				} else if (signature == BlockType.CarCollisions.getKey()) {
					fis = new FileInputStream(selected);
					byte [] fileToBytes = new byte[(int)selected.length()-4];
					fis.skipNBytes(4);
					fis.read(fileToBytes);
					fis.close();
					
					mainCollisions = Collisions.read(fileToBytes);

				} else if (signature == BlockType.Geometry.getKey()){
					loadVisualModel(selected);
					renderVisualModel.set(true);
					return;
					
				} else {
					new Alert(AlertType.ERROR, "Could not recognize file type!", ButtonType.OK).show();
					return;
				}

				lastFileLoaded = selected.getName();
				lastDirectoryLoaded = selected.getAbsolutePath().replace(lastFileLoaded, "");

				if (recentFiles.contains(selected.getAbsolutePath())) recentFiles.remove(selected.getAbsolutePath());
				recentFiles.add(0, selected.getAbsolutePath());

				updateAllBoundsDisplay();
				primaryStage.setTitle(Utils.collisions_Name + " - " + mainCollisions.carname);
				
				renderVisualModel.set(false);
	//				menuDBMP.setText(mainCollisions.carname.label);
				for (var b : mainCollisions.scg.root.getChildrenRecursively()) {
					if (b.getShape() != null && b.getShape().getClass() == hkpConvexVerticesShape.class) {
						((hkpConvexVerticesShape)b.getShape()).PlaneEquations.forEach(p -> {
							p.colorR = Collisions.randomizeColor(b.colorR, 0.4);
							p.colorG = Collisions.randomizeColor(b.colorG, 0.4);
							p.colorB = Collisions.randomizeColor(b.colorB, 0.4);
						});
					}
				}
				cleanupGroups();
				updateBoundsRender();
				boundControls.setBound(null);
				mainCollisions.scg.root.renderSetRecursively(true);
				if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Collisions loaded successfully.", ButtonType.OK).show();

			
			} catch (Exception ex) {
				new Alert(AlertType.ERROR, "Error loading the collisions:\n%s".formatted(ex.getMessage()), ButtonType.OK).show();
				ex.printStackTrace();
			}
		}).run();
	}

	public static void loadBundledCollisions(Stage primaryStage, File selected, SearchableListView<FileOffset> cols) {
		try {
			var chunkData = ByteBuffer.allocate(cols.list.getSelectionModel().getSelectedItem().size-4);
			chunkData.order(ByteOrder.LITTLE_ENDIAN);
			{
				var stream = new RandomAccessFile(selected, "r");
				stream.seek(cols.list.getSelectionModel().getSelectedItem().offset+4); //skip bChunkID
				stream.read(chunkData.array());
				
				stream.close();
			}
			mainCollisions = new Collisions(chunkData);
			
			lastFileLoaded = selected.getName();
			lastDirectoryLoaded = selected.getAbsolutePath().replace(lastFileLoaded, "");

			if (recentFiles.contains(selected.getAbsolutePath())) recentFiles.remove(selected.getAbsolutePath());
			recentFiles.add(0, selected.getAbsolutePath());

			updateAllBoundsDisplay();
			primaryStage.setTitle(Utils.collisions_Name + " - " + mainCollisions.carname);
			
			renderVisualModel.set(false);
//				menuDBMP.setText(mainCollisions.carname.label);
			for (var b : mainCollisions.scg.root.getChildrenRecursively()) {
				if (b.getShape() != null && b.getShape().getClass() == hkpConvexVerticesShape.class) {
					((hkpConvexVerticesShape)b.getShape()).PlaneEquations.forEach(p -> {
						p.colorR = Collisions.randomizeColor(b.colorR, 0.4);
						p.colorG = Collisions.randomizeColor(b.colorG, 0.4);
						p.colorB = Collisions.randomizeColor(b.colorB, 0.4);
					});
				}
			}
			cleanupGroups();
			updateBoundsRender();
			boundControls.setBound(null);
			mainCollisions.scg.root.renderSetRecursively(true);
			if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Collisions loaded successfully.", ButtonType.OK).show();
		} catch (Exception e1) {
			e1.printStackTrace();
			javafx.application.Platform.runLater(() -> {
				new Alert(Alert.AlertType.ERROR, "Error loading the collisions:\n"+e1.getMessage(), ButtonType.OK).show();
//											status.text.setText(null);
//											status.progress.setProgress(0);
			});
		}
	}
	
	public static int getNextInt(FileInputStream fis) throws IOException {
		return fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
	}
	
	public static void updateVisualModel() {
		if (renderVisualModel.get()) {
			if (!viewportGroup.getChildren().contains(modelGroup)) viewportGroup.getChildren().add(modelGroup);
		} else {
			if (viewportGroup.getChildren().contains(modelGroup)) viewportGroup.getChildren().remove(modelGroup);
		}
	}

	public static void cleanupGroups() {
    	boundsGroup.getChildren().clear();
    	pivotsGroup.getChildren().clear();
    	shapesGroup.getChildren().clear();
	}
	
	public static void updateBoundsRender() {
		if (mainCollisions != null) for (var b : mainCollisions.scg.root.getChildrenRecursively()) {
			b.updateShape();
		}
	}

    public static void updateRender() {
//    	viewport.viewportGroup.getChildren().clear();
//    	viewport.buildAxes();
    	
//    	if (CollisionsEditor.mainCollisions.scg.root != null) for(scgBounds b : CollisionsEditor.mainCollisions.scg.root.getChildrenRecursively()) {
//			System.out.println("Updating "+Hash.getVLT(b.NameHash) + ", " + b.Type + ", "+b.Shape);
//        	b.updateShape();
//    	}
//    	System.gc();
    	
    }
    
    public static void updateAllBoundsDisplay() {
    	if (mainCollisions.scg != null && mainCollisions.scg.root != null) {
        	hierarchy.setRoot(mainCollisions.scg.root.makeTreeViewRecursively());
        	hierarchy.getRoot().setExpanded(true);    		
    	}
    }
}
