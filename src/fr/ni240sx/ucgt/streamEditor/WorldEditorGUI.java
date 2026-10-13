package fr.ni240sx.ucgt.streamEditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;
import fr.ni240sx.ucgt.geometryFile.gui.CustomProgressBar;
import fr.ni240sx.ucgt.geometryFile.gui.SearchableListView;
import fr.ni240sx.ucgt.shared.Hash;
import fr.ni240sx.ucgt.shared.OrbitCameraViewport;
import fr.ni240sx.ucgt.shared.Utils;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.eTrackPathZoneType;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.CARP;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.CARP.UDataID;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.CARP.WRoad;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tech.dashman.dashman.StageSizer;

public class WorldEditorGUI extends Application {
	
	public static boolean debug = false;

	//stuff to put in a config file
	public static String lastDirectoryLoaded = Paths.get("").toAbsolutePath().toString();
	public static String lastFileLoaded = "";
	public static String lastFileSaved = Paths.get("").toAbsolutePath().toString(); //now unused
    public static CheckMenuItem useDarkMode = new CheckMenuItem("Dark mode");
    public static CheckMenuItem disableWarnings = new CheckMenuItem("Disable warning pop-ups");
//	public static boolean widebodyAutoCorrect = true;
	
	public static Group viewportGroup = new Group();
	public static OrbitCameraViewport viewport;

	public static Group boundariesGroup = new Group();	
	public static Group trackPathZonesGroup = new Group();	
	public static Group markersGroup = new Group();	
	public static Group elevationGroup = new Group();	
	public static Group barrierSplinesGroup = new Group();
	public static Group worldGridGroup = new Group();
	public static Group modelsGroup = new Group();	

    public static StageSizer stageSizer = new StageSizer();
	
    public static SearchableListView<StreamController> listViewStream = new SearchableListView<>();
    public static SearchableListView<VisualController> listViewVisual = new SearchableListView<>();
    public static StreamInfo streamInfo = new StreamInfo();
    
    public static CheckMenuItem simplifiedPartsList = new CheckMenuItem("Simplified parts list");
    public static CheckMenuItem loadTextures = new CheckMenuItem("Load textures");
    public static CheckMenuItem trackpadMode = new CheckMenuItem("Trackpad mode");
    public static CheckMenuItem centerOnPart = new CheckMenuItem("Center camera on selected item");

    public static CheckMenuItem showChunksOnClick = new CheckMenuItem("Show chunks on click");
    public static CheckMenuItem cameraBasedStreaming = new CheckMenuItem("Show chunks from camera position");

    private static CheckMenuItem regionBoundaries = new CheckMenuItem("Show drivable visible section boundaries");
    private static CheckMenuItem regionPanoramaBoundaries = new CheckMenuItem("Show panorama visible section boundaries");
//    private static CheckMenuItem regionTrackPathZones = new CheckMenuItem("Show track path zones");
    private static CheckMenuItem regionTrackMarkers = new CheckMenuItem("Show track position markers");
    private static CheckMenuItem regionRoadNetwork = new CheckMenuItem("Show road network (simple)");
    private static CheckMenuItem regionRoadNetworkDetailed = new CheckMenuItem("Show road network (detailed)");
    private static CheckMenuItem regionBarrierSplines = new CheckMenuItem("Show track path barrier splines");

    public static CheckMenuItem visibleScenery = new CheckMenuItem("Show visible section scenery"); //TODO hide barriers -> scenery groups ?
    public static CheckMenuItem visibleHeliSheet = new CheckMenuItem("Show visible section heli sheet");
    public static CheckMenuItem visibleSmokeSpawners = new CheckMenuItem("Show visible section smoke spawners");
    public static CheckMenuItem visibleVisCurtains = new CheckMenuItem("Show visible section visibility curtains");
    public static CheckMenuItem visibleCollisions = new CheckMenuItem("Show visible section collisions"); //TODO hide barriers

    
    public static CheckMenuItem forceLoadY = new CheckMenuItem("Force load Y chunks (highest LOD)");
    
    public static CheckMenuItem doDiffuse = new CheckMenuItem("Show Diffuse");
    public static CheckMenuItem doSpecular = new CheckMenuItem("Show Specular");
    public static CheckMenuItem doNormal = new CheckMenuItem("Show Normal");
    public static CheckMenuItem doSelfIllum = new CheckMenuItem("Show Illumination");
    public static CheckMenuItem doVertexColors = new CheckMenuItem("Show Vertex Colors");

    public static ComboBox<String> displayLod = new ComboBox<>();
    public static ComboBox<eTrackPathZoneType> trackPathZoneFilter = new ComboBox<>();
    
    public static ArrayList<String> textureLookupFolders = new ArrayList<>();
    public static ArrayList<String> recentFiles = new ArrayList<>();
    public static final int maxRecentFiles = 24;
    public static ArrayList<Stage> openWindows = new ArrayList<>();
    
    public static CustomProgressBar status = new CustomProgressBar();
    
    public static boolean unsavedChanges = false;
    public static boolean firstBoot = true;
    
    public static WorldController mainStreamer = null;
    public static StreamController currentChunk;
    public static StreamController currentChunk_elev;
    int chunkListType = 0;
    
//    static HashMap<Integer,String> barrierSplineHashes = new HashMap<>();
    static HashMap<Integer,Integer> barrierSplineHashes = new HashMap<>();

	static ExecutorService streamer = Executors.newSingleThreadExecutor(r -> {
	    Thread t = new Thread(r);
	    t.setUncaughtExceptionHandler((thread, throwable) -> {
	        throwable.printStackTrace();
	    });
	    return t;
	});
	
	static ExecutorService texturesBuilder = Executors.newSingleThreadExecutor(r -> {
	    Thread t = new Thread(r);
	    t.setUncaughtExceptionHandler((thread, throwable) -> {
	        throwable.printStackTrace();
	    });
	    return t;
	});

	public static void main(String[] args) {
		try {
			readSettings();
		} catch (@SuppressWarnings("unused") Exception e) {
			simplifiedPartsList.setSelected(true);
			loadTextures.setSelected(true);
	        showChunksOnClick.setSelected(true);
	        cameraBasedStreaming.setSelected(false);
	        regionBoundaries.setSelected(true);
	        regionPanoramaBoundaries.setSelected(false);
	        doDiffuse.setSelected(true);
	        doSpecular.setSelected(true);
	        doNormal.setSelected(true);
	        doSelfIllum.setSelected(true);
	        doVertexColors.setSelected(true);
	        forceLoadY.setSelected(true);
		}
		
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
		    System.err.println("Exception in thread: " + thread.getName());
		    throwable.printStackTrace();
		});
		

//		Hash.addHashes(new File("data/stuff"));
		Hash.addHashes(new File("data/worlddata"));
//		for (int i=0; i<10000; i++) Hash.addBinHash("BARRIER_SPLINE_%04d".formatted(i), barrierSplineHashes);
		for (int i=0; i<10000; i++) barrierSplineHashes.put(Hash.calcBin("BARRIER_SPLINE_%04d".formatted(i)), i);
        launch(args);
		streamer.shutdownNow();
        
        try {
			writeSettings();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Could not save config");
		}
	}

	public static void writeSettings() throws IOException {
		Files.createDirectories(Paths.get(Utils.settingsFolder));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Utils.settingsWrldEd)));
		writeSetting(bw, "DO NOT MANUALLY EDIT THIS FILE");
		writeSetting(bw, lastDirectoryLoaded);
		writeSetting(bw, lastFileLoaded);
		writeSetting(bw, lastFileSaved);
		writeSetting(bw, stageSizer.getHeight());
		writeSetting(bw, stageSizer.getWidth());
		writeSetting(bw, stageSizer.getX());
		writeSetting(bw, stageSizer.getY());
		writeSetting(bw, stageSizer.getMaximized());
		writeSetting(bw, useDarkMode);
		writeSetting(bw, disableWarnings);
		writeSetting(bw, simplifiedPartsList);
		writeSetting(bw, loadTextures);
		writeSetting(bw, textureLookupFolders);
		writeSetting(bw, trackpadMode);
		writeSetting(bw, centerOnPart);
		writeSetting(bw, firstBoot);
		writeSetting(bw, recentFiles.size() > maxRecentFiles ? recentFiles.subList(0, maxRecentFiles) : recentFiles);
		writeSetting(bw, showChunksOnClick);
		writeSetting(bw, cameraBasedStreaming);
		writeSetting(bw, regionBoundaries);
		writeSetting(bw, regionPanoramaBoundaries);
		writeSetting(bw, doDiffuse);
		writeSetting(bw, doSpecular);
		writeSetting(bw, doNormal);
		writeSetting(bw, doSelfIllum);
		writeSetting(bw, doVertexColors);
		writeSetting(bw, forceLoadY);
		writeSetting(bw, regionTrackMarkers);
		
		bw.close();
	}

	public static void writeSetting(BufferedWriter bw, Object setting) throws IOException {
		if (setting.getClass() == ArrayList.class) {
			@SuppressWarnings("rawtypes")
			var list = (ArrayList)setting;
			for (var o : list) bw.write("%s>".formatted(o));
			bw.write("\n");
		} else if (setting.getClass() == CheckMenuItem.class) {
			bw.write("%s\n".formatted(((CheckMenuItem)setting).isSelected()));
		} else if (setting.getClass() == CheckBox.class) {
			bw.write("%s\n".formatted(((CheckBox)setting).isSelected()));
		} else {
			bw.write("%s\n".formatted(setting));
		}
	}
	
	public static void readSettings() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(Utils.settingsWrldEd)));
		br.readLine(); //="DO NOT MANUALLY EDIT THIS FILE"
		lastDirectoryLoaded = br.readLine();
		lastFileLoaded = br.readLine();
		lastFileSaved = br.readLine();
		stageSizer.setHeight(Double.valueOf(br.readLine()));
		stageSizer.setWidth(Double.valueOf(br.readLine()));
		stageSizer.setX(Double.valueOf(br.readLine()));
		stageSizer.setY(Double.valueOf(br.readLine()));
		stageSizer.setMaximized(Boolean.valueOf(br.readLine()));
		useDarkMode.setSelected( Boolean.valueOf(br.readLine()));
		disableWarnings.setSelected( Boolean.valueOf(br.readLine()));
		simplifiedPartsList.setSelected(Boolean.valueOf(br.readLine()));
		loadTextures.setSelected(Boolean.valueOf(br.readLine()));
		for (var s : br.readLine().split(">")) if (s != null && !s.isBlank()) textureLookupFolders.add(s);
		trackpadMode.setSelected(Boolean.valueOf(br.readLine()));
		centerOnPart.setSelected(Boolean.valueOf(br.readLine()));
		firstBoot = Boolean.valueOf(br.readLine());
		for (var s : br.readLine().split(">")) if (s != null && !s.isBlank()) recentFiles.add(s);
		//String s;
		//if(!(s=br.readLine()).isBlank())
        showChunksOnClick.setSelected(Boolean.valueOf(br.readLine()));
        cameraBasedStreaming.setSelected(Boolean.valueOf(br.readLine()));
        regionBoundaries.setSelected(Boolean.valueOf(br.readLine()));
        regionPanoramaBoundaries.setSelected(Boolean.valueOf(br.readLine()));
        doDiffuse.setSelected(Boolean.valueOf(br.readLine()));
        doSpecular.setSelected(Boolean.valueOf(br.readLine()));
        doNormal.setSelected(Boolean.valueOf(br.readLine()));
        doSelfIllum.setSelected(Boolean.valueOf(br.readLine()));
        doVertexColors.setSelected(Boolean.valueOf(br.readLine()));
        forceLoadY.setSelected(Boolean.valueOf(br.readLine()));
        regionTrackMarkers.setSelected(Boolean.valueOf(br.readLine()));
		br.close();
	}
	
    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle(Utils.worldEditor_Name);

        VBox windowTop = new VBox();
        
        //
        // MENUS BAR
        //
        
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("File");
        MenuItem fileNew = new MenuItem("New");
        MenuItem fileLoad = new MenuItem("Load");
        MenuItem fileReload = new MenuItem("Reload");
        Menu fileLoadRecent = new Menu("Load recent...");
        MenuItem fileSave = new MenuItem("Save");
        MenuItem fileExit = new MenuItem("Exit");

        menuFile.getItems().addAll(fileNew, fileLoad, fileReload, fileLoadRecent, fileSave, new SeparatorMenuItem(), new SeparatorMenuItem(), fileExit);
        menuFile.setAccelerator(KeyCombination.keyCombination("Alt+F"));

        fileNew.setOnAction(e -> {
    		new Thread(() -> {
				try {
					postLoadUpdate(primaryStage);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}).start();
        });
        fileNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        
        fileLoad.setOnAction(handleFileLoad(primaryStage));
        fileLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileReload.setOnAction(handleFileReload(primaryStage));
        fileReload.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
        menuFile.setOnShowing(e -> {
        	fileLoadRecent.getItems().clear();
        	for (var f : recentFiles) {
        		var i = new MenuItem(f);
        		i.setOnAction(evt ->{
        	        ButtonType sure = ButtonType.YES;
        	        if (!disableWarnings.isSelected() && unsavedChanges) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to load a new model ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
        			if (ButtonType.YES.equals(sure)) {

        				File selected = new File(f);
        				if (selected.isFile()) {
        					try {
        						loadByFileType(primaryStage, selected);
        						
        					} catch (Exception ex) {
        						new Alert(Alert.AlertType.ERROR, "Error loading the world:\n"+ex.getMessage(), ButtonType.OK).show();
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
        fileSave.setOnAction(handleFileSave());
        fileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        fileExit.setOnAction(handleFileExit(primaryStage));
        fileExit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
        

        Menu menuRegion = new Menu("Region");
        menuRegion.getItems().addAll(regionBoundaries, regionPanoramaBoundaries, //regionTrackPathZones, 
        		regionTrackMarkers, regionRoadNetwork, regionRoadNetworkDetailed, regionBarrierSplines);

        regionBoundaries.setOnAction(e -> updateBoundaries());
        regionPanoramaBoundaries.setOnAction(e -> updateBoundaries());
//        regionTrackPathZones.setOnAction(e -> updateTrackPathZones());
        regionTrackMarkers.setOnAction(e -> updateMarkers());
        regionRoadNetwork.setOnAction(e -> updateWorldGridRoadNetwork()); //TODO
        regionRoadNetworkDetailed.setOnAction(e -> updateWorldGridRoadNetwork()); //TODO
        regionBarrierSplines.setOnAction(e -> updateBarrierSplines());
        
        Menu menuVisibleSections = new Menu("Visible sections");
        menuVisibleSections.getItems().addAll(visibleScenery, visibleHeliSheet, visibleSmokeSpawners, visibleVisCurtains, visibleCollisions);

        visibleScenery.setOnAction(e -> renderWorldFromChunk(currentChunk));
        visibleHeliSheet.setOnAction(e -> renderWorldFromChunk(currentChunk));
        visibleSmokeSpawners.setOnAction(e -> renderWorldFromChunk(currentChunk));
        visibleVisCurtains.setOnAction(e -> renderWorldFromChunk(currentChunk));
        visibleCollisions.setOnAction(e -> renderWorldFromChunk(currentChunk));
        
        
        Menu menuDisplay = new Menu("Display");
        MenuItem textureFolders = new MenuItem("Texture folders...");
        menuDisplay.getItems().addAll(simplifiedPartsList, centerOnPart, showChunksOnClick, cameraBasedStreaming, forceLoadY);
        menuDisplay.setAccelerator(KeyCombination.keyCombination("Alt+D"));
        simplifiedPartsList.setAccelerator(KeyCombination.keyCombination("Alt+L"));


        showChunksOnClick.setOnAction(e -> cameraBasedStreaming.setSelected(false));
        cameraBasedStreaming.setOnAction(e -> showChunksOnClick.setSelected(false));
        
        Menu menuRendering = new Menu("Rendering");
        menuRendering.getItems().addAll(loadTextures, textureFolders, new SeparatorMenuItem(), doDiffuse, doSpecular, doNormal, doSelfIllum, doVertexColors);
        menuRendering.setAccelerator(KeyCombination.keyCombination("Alt+R"));
        
        doDiffuse.setAccelerator(KeyCombination.keyCombination("Alt+D"));
        doSpecular.setAccelerator(KeyCombination.keyCombination("Alt+S"));
        doNormal.setAccelerator(KeyCombination.keyCombination("Alt+N"));
        doSelfIllum.setAccelerator(KeyCombination.keyCombination("Alt+I"));
        doVertexColors.setAccelerator(KeyCombination.keyCombination("Alt+V"));
        
        doDiffuse.setOnAction(e -> renderWorldFromChunk(currentChunk));
        doSpecular.setOnAction(e -> renderWorldFromChunk(currentChunk));
        doNormal.setOnAction(e -> renderWorldFromChunk(currentChunk));
        doSelfIllum.setOnAction(e -> renderWorldFromChunk(currentChunk));
        doVertexColors.setOnAction(e -> {
			new Alert(Alert.AlertType.WARNING, "Currently unsupported!", ButtonType.OK).show();

//        	for (var p : parts) if (p.vertexColorMeshes != null) {
//        		if (doVertexColors.isSelected()) p.mesh.getChildren().addAll(p.vertexColorMeshes);
//        		else p.mesh.getChildren().removeAll(p.vertexColorMeshes);
//        	}
        });

        forceLoadY.setOnAction(e -> renderWorldFromChunk(currentChunk));

        textureFolders.setOnAction(handleTextureFolderSettings());
        
        Menu menuTools = new Menu("Tools");

        menuTools.getItems().addAll();
        menuTools.setAccelerator(KeyCombination.keyCombination("Alt+T"));
        
        Menu menuSettings = new Menu("Settings");
        MenuItem settingsAbout = new MenuItem("About " + Utils.worldEditor_Name + "...");
        MenuItem settingsUpdates = new MenuItem("Check for updates...");
        menuSettings.getItems().addAll(useDarkMode, disableWarnings, trackpadMode, settingsAbout, settingsUpdates);
        menuSettings.setAccelerator(KeyCombination.keyCombination("Alt+S"));

        settingsAbout.setOnAction(e -> showInfoWindow());
        settingsUpdates.setOnAction(e -> {
        	//TODO use shared stuff
//	    	getHostServices().showDocument("https://github.com/NI240SX/UCGT/releases");
//			new Alert(Alert.AlertType.INFORMATION, "Current versions:\n- Geometry Editor v"+GeometryEditorCLI.programVersion+" built on "+GeometryEditorCLI.programBuild+"\n"
//					+ "- DBModelParts Editor v"+DBMPPlus.programVersion +"\n- Collisions Editor v"+CollisionsEditor.programVersion, ButtonType.OK).showAndWait();
        });
                
        menuBar.getMenus().addAll(menuFile, menuRegion, menuVisibleSections, menuDisplay, menuRendering, menuTools, menuSettings);
        		
        
        //
        // SHORTCUTS BAR
        //
        
        
        
        HBox shortcutsBar = new HBox();
        
        Separator infSep = new Separator();
        HBox.setHgrow(infSep, Priority.ALWAYS);
        var switchChunkList = new Button("Streamer chunks");
        StackPane chunkList = new StackPane();
        chunkList.getChildren().add(listViewStream);
        switchChunkList.setOnAction(e -> {
        	chunkListType++;
        	chunkListType %= 2;
        	switch (chunkListType) {
        	case 0:
        		switchChunkList.setText("Streamer chunks");
        		chunkList.getChildren().clear();
                chunkList.getChildren().add(listViewStream);
        		break;
        	case 1:
        		switchChunkList.setText("Model chunks");
        		chunkList.getChildren().clear();
                chunkList.getChildren().add(listViewVisual);
        		break;
    		default:
        	}
        });
        
        displayLod.getItems().addAll("A","B","C","Z");
        displayLod.getSelectionModel().select(0);
        displayLod.setOnAction(e -> renderWorldFromChunk(currentChunk));
        trackPathZoneFilter.getItems().addAll(eTrackPathZoneType.values());
        trackPathZoneFilter.getSelectionModel().select(eTrackPathZoneType.NONE);
        trackPathZoneFilter.setOnAction(e -> updateTrackPathZones());
        
		var coordX = new Label();
		var coordY = new Label();
		var coordZ = new Label();

        shortcutsBar.getChildren().addAll(
        		new Label("LOD"), displayLod, switchChunkList, 
        		new Label("TrackPathZone filter"), trackPathZoneFilter, 
        		infSep, 
        		new Label("Camera pivot coordinates"), new Separator(), coordX, new Separator(), coordY, new Separator(), coordZ);
        
        windowTop.getChildren().addAll(menuBar, shortcutsBar);
        
        viewport = new OrbitCameraViewport(viewportGroup, 0, 0);
        viewportGroup.getChildren().addAll(
        		boundariesGroup,
				trackPathZonesGroup,
				markersGroup,
				elevationGroup,
				barrierSplinesGroup,
				modelsGroup,
				worldGridGroup
        		);
        viewport.trackpadMode.bind(trackpadMode.selectedProperty());
		coordX.textProperty().bind(viewport.cameraPivotPosition.xProperty().asString("%.2f"));
		coordY.textProperty().bind(viewport.cameraPivotPosition.yProperty().asString("%.2f"));
		coordZ.textProperty().bind(viewport.cameraPivotPosition.zProperty().asString("%.2f"));

        var partsPane = new SplitPane();
        partsPane.getItems().addAll(streamInfo);
        partsPane.setOrientation(Orientation.VERTICAL);
        
//        var rightPane = new SplitPane();
//        rightPane.getItems().addAll(partsPane);
//        rightPane.setDividerPosition(0,1);
        
        BorderPane sp = new BorderPane();
        sp.setCenter(viewport);
        sp.setBottom(status);
        
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(chunkList, sp//, rightPane
        		,partsPane
        		);

        
//        listViewStream.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        listViewStream.setRoot(partsRoot);
        listViewStream.getSelectionModel().selectedItemProperty().addListener((obs, was, is) -> {
        	for (var c : mainStreamer.streamControllers) c.resetMeshColor();
        	if (is != null) {
            	if (is.visualChunks != null) for (var c : mainStreamer.streamControllers) {
            		if (is.visualChunks.relatedChunkIDs.contains(c.ID)) {
            			c.brightMeshColor();
            		}
            	}
        		is.setMeshColor(Color.RED); //is contained in its related chunk ids
				if (centerOnPart.isSelected() && viewport.keys.isEmpty()) viewport.moveCameraNoListener(is.boundary.Centre[X], is.boundary.Centre[Y]);
        	}
        	streamInfo.update(is, mainStreamer.visualControllers, mainStreamer.streaml8r);
        	
        	if (showChunksOnClick.isSelected()) {
				renderWorldFromChunk(is);
				if (is != null && centerOnPart.isSelected()) {
					viewport.moveCameraNoListener(is.boundary.Centre[X], is.boundary.Centre[Y]);
            	}
        	}
        });
        listViewVisual.getSelectionModel().selectedItemProperty().addListener((obs, was, is) -> {
        	streamInfo.update(is, mainStreamer.streaml8r);
        	for (var st : mainStreamer.streamControllers) if (st.ID == is.ID) {
        		st.setMeshColor(Color.RED);
        	} else st.resetMeshColor();
			if (centerOnPart.isSelected()) viewport.moveCameraNoListener(is.chunk.Centre[X], is.chunk.Centre[Y]);

        });
        
        listViewStream.list.setOnKeyPressed(e -> {
        	if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER|| e.getCode() == KeyCode.H) {
        		listViewStream.getSelectionModel().getSelectedItems().forEach(itm -> {
        			if (itm != null) {
//            			itm.getValue().display.set(!listViewStream.getSelectionModel().getSelectedItem().getValue().display.get());
//            			itm.getChildren().forEach(itm2 -> {
//                			itm2.getValue().display.set(!listViewStream.getSelectionModel().getSelectedItem().getValue().display.get());
//                			itm2.getChildren().forEach(itm3 -> {
//                    			itm3.getValue().display.set(!listViewStream.getSelectionModel().getSelectedItem().getValue().display.get());
////                    			itm3.getChildren().forEach(null);
//                    		});
//                		});
        			}
        		});
        		e.consume();
        	}
        });
        
        
        BorderPane root = new BorderPane();
        root.setTop(windowTop);
        root.setCenter(centerPane);
        status.progress.setMinHeight(30);
        sp.widthProperty().addListener((obs, o, n) -> status.progress.setMinWidth((double)n));
		status.progress.setProgress(0);
        

        root.setOnKeyPressed(e -> {
        	switch (e.getCode()) {
        	case H:
//        		if (e.isAltDown()) listViewStream.getSelectionModel().getSelectedItems().forEach(itm -> itm.getValue().display.set(true));
//        		else listViewStream.getSelectionModel().getSelectedItems().forEach(itm -> itm.getValue().display.set(false));
        		break;
        	case A:
//        		partsRoot.getChildren().forEach(p -> {
//        			if (p.getValue().display.get()) listViewStream.getSelectionModel().select(p);
//        		});
        		break;
        	case R:
        		if (e.isAltDown())
        			viewport.resetCamera();
        		break;
        	case NUMPAD5:
        		//reset camera
        		viewport.resetCamera();
        		break;
    		default:
        	}        	
        });
        
		viewport.widthProperty().bind(sp.widthProperty());
		viewport.heightProperty().bind(sp.heightProperty());
		viewport.setAxes(Rotate.X_AXIS, Rotate.Z_AXIS);
		viewport.setAngles(0, 0);
		viewport.setDistance(15000);
		viewport.camera.setNearClip(1);
		
		
		viewport.cameraPivotPosition.setOnTransformChanged(e -> doChunkStreaming());
		

//		updateAllPartsDisplay();
//        updateRender();
        
        
        Scene scene = new Scene(root, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
	        ButtonType sure = ButtonType.YES;
	        if (!disableWarnings.isSelected() && unsavedChanges) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
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
    	
        if (useDarkMode.isSelected()) scene.getRoot().setStyle("-fx-base:black");
        useDarkMode.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) scene.getRoot().setStyle("-fx-base:black");
            else scene.getRoot().setStyle("");
        });
        stageSizer.setStage(primaryStage);
        
        

        centerPane.setDividerPosition(0, 0.25);
        centerPane.setDividerPosition(1, 0.75);
    	sp.setMinWidth(0);
    	sp.setMinHeight(0);
        
        scene.widthProperty().addListener((obs, previous, current) -> {    	
            centerPane.setDividerPosition(0, centerPane.getDividerPositions()[0]*((Double)previous/(Double)current));
            centerPane.setDividerPosition(1, 1+(centerPane.getDividerPositions()[1]-1)*((Double)previous/(Double)current));
        });
//        scene.heightProperty().addListener(c -> {
//        });
        
        primaryStage.show();
        primaryStage.getScene().getRoot().mouseTransparentProperty().bind(primaryStage.focusedProperty().not()); //fix tooltips

		try {
			if (new File(lastDirectoryLoaded + lastFileLoaded).isFile())
				loadByFileType(primaryStage, new File(lastDirectoryLoaded + lastFileLoaded));
		} catch (Exception e){
			System.out.println("Error loading the previous file.");
			e.printStackTrace();
		}
		if (firstBoot) {
			showInfoWindow();
			firstBoot = false;
		}
//		TestingFunctions.collisionDisplayTest(viewportGroup);
    }

	public static void doChunkStreaming() {
		if (cameraBasedStreaming.isSelected()) {
			var x = viewport.cameraPivotPosition.getX();
			var y = viewport.cameraPivotPosition.getY();
			var z = viewport.cameraPivotPosition.getZ();
			
			if (currentChunk != null && !(new Point(x, y).isInside(currentChunk.boundary.points))) currentChunk = null;
			if (currentChunk != null) {				
				var chunkf = figureElevation(x, y, z, currentChunk);
				if (chunkf != currentChunk_elev) renderWorldFromChunk(chunkf);
				currentChunk_elev = chunkf;
			}
			if (currentChunk == null) {
				for (var c : mainStreamer.streamControllers) {
					if ((!c.isPanorama() && c.boundary != null) && 
							c.boundary.boundsMin[X] < x && x < c.boundary.boundsMax[X] &&
							c.boundary.boundsMin[Y] < y && y < c.boundary.boundsMax[Y]) {
						if (new Point((float)x,(float)y).isInside(c.boundary.points)) {
							currentChunk = c;
							var chunkf = figureElevation(x, y, z, currentChunk);
							renderWorldFromChunk(chunkf);
							currentChunk_elev = chunkf;
							break;
						}						
					}
				}
				if (currentChunk == null) {
//						chunksGroups.forEach((c,g) -> {
//							javafx.application.Platform.runLater(() -> g.getChildren().clear());
//						});
				}
			}
		}
	}

	public static StreamController figureElevation(double x, double y, double z, StreamController chunk) {
		if (currentChunk.elevPolies0 != null) {
			float f1 = Float.POSITIVE_INFINITY;
			for (var r : currentChunk.elevPolies0) if (new Point(x,y).isInside(new float[] {r.vertices[0][X],r.vertices[1][X],r.vertices[2][X]}, new float[]{r.vertices[0][Y], r.vertices[1][Y], r.vertices[2][Y]})) {
				f1 = r.vertices[0][Z];
			}
			if (z > f1) for (var c2 : mainStreamer.streamControllers) if (c2.ID == currentChunk.boundary.DepthSectionNumbers[0]){
				chunk = c2;
			}
		}
		if (currentChunk.elevPolies1 != null) {
			float f1 = Float.POSITIVE_INFINITY;
			for (var r : currentChunk.elevPolies1) if (new Point(x,y).isInside(new float[] {r.vertices[0][X],r.vertices[1][X],r.vertices[2][X]}, new float[]{r.vertices[0][Y], r.vertices[1][Y], r.vertices[2][Y]})) {
				f1 = r.vertices[0][Z];
			}
			if (z > f1) for (var c2 : mainStreamer.streamControllers) if (c2.ID == currentChunk.boundary.DepthSectionNumbers[1]){
				chunk = c2;
			}
		}
		return chunk;
	}

	public static AtomicInteger phaser = new AtomicInteger();
	
	private static void renderWorldFromChunk(StreamController chunk) {
		final var phase = phaser.incrementAndGet();
		status.text.setText("Updating render...");
		status.progress.setProgress(-1);

		listViewStream.getSelectionModel().select(chunk);

		streamer.submit(() -> {

			if (phaser.get() != phase) return;
			int result = mainStreamer.renderWorldFromChunk(chunk, modelsGroup);
			
			if (result == 1) return;
			if (result == -1) {
				javafx.application.Platform.runLater(() -> {
					status.text.setText("Out of memory, attempting to clean up!");
					status.progress.setProgress(-1);
				});
				mainStreamer.doMemoryCleanup();
				mainStreamer.renderWorldFromChunk(chunk, modelsGroup); //try again
			}
			if (result == -2 | result == -3) {
				javafx.application.Platform.runLater(() -> {
					status.text.setText("An error occured! See the console.");
					status.progress.setProgress(0);
				});
				return;
			}
			
//        	streamInfo.update(chunk, mainStreamer.visualControllers, mainStreamer.streaml8r);
			
			javafx.application.Platform.runLater(() -> {
				status.text.setText(null);
				status.progress.setProgress(0);
			});
		});
	}

	public void showInfoWindow() {
    	Stage st = new Stage();
		openWindows.add(st);
		st.setTitle("About " + Utils.worldEditor_Name);
		
		var v = new VBox();
		
		Label l = new Label("""
				Undercover Geometry Tool is a work-in-progress passion project built during my free time.
				As such, and like any program, it can be prone to all sorts of issues and possible expansions.
				Report them on my Discord or on GitHub.
				
				If you wish to support UCGT and its future developments, consider buying me a coffee.
				Supporters will get early access to Undercover Geometry Tool and the Undercover Exposed mod, a special role and access to a private channel in my Discord server, and their suggestions will be taken in priority. I'm open to commissions as well.
				""");
				
		Label versions = new Label("""
				%s version %s built %s
				needeka 2026
				""".formatted(Utils.worldEditor_Name, Utils.worldEditor_Version, Utils.geom_build));
		l.setWrapText(true);
		l.setTextAlignment(TextAlignment.JUSTIFY);

		var git = new Hyperlink("UCGT on GitHub");
		git.setOnAction(e -> getHostServices().showDocument("https://github.com/NI240SX/UCGT"));
		var dc = new Hyperlink("Undercover Exposed on Discord");
		dc.setOnAction(e -> getHostServices().showDocument("https://discord.gg/e4gnuWajNm"));
		var kofi = new Hyperlink("Buy me a coffee!");
		kofi.setOnAction(e -> getHostServices().showDocument("https://ko-fi.com/needeka"));
		
		v.getChildren().addAll(l,new Label(), kofi,git,dc, new Label(), versions);
		v.setMaxWidth(500);
		v.setPadding(new Insets(5));
		
		Scene sc = new Scene(v);
		st.setScene(sc);
		if (useDarkMode.isSelected()) sc.getRoot().setStyle("-fx-base:black");
		st.setResizable(false);
		st.setAlwaysOnTop(true);
//		sc.setOnMouseClicked(evt -> {
//			evt.consume();
//			openWindows.remove(st);
//			st.close();
//		});
		st.setOnCloseRequest(e2 -> openWindows.remove(st));
		st.show();
		st.requestFocus();
    }
	
	public static EventHandler<ActionEvent> handleFileLoad(Stage primaryStage) {
		return evt -> {
			
	        ButtonType sure = ButtonType.YES;
	        if (!disableWarnings.isSelected() && unsavedChanges) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to load a new model ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {

			
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File(lastDirectoryLoaded).isDirectory() ? new File(lastDirectoryLoaded) : new File("."));
				fc.setInitialFileName("L8R_MW2.BUN");
				fc.getExtensionFilters().addAll(
				        new FileChooser.ExtensionFilter("Black Box NFS World Bundle File", "*.bun"),
				        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load a world");
				File selected = fc.showOpenDialog(null);
				if (selected != null) {
					try {
						loadByFileType(primaryStage, selected);
						
					} catch (Exception e) {
						new Alert(Alert.AlertType.ERROR, "Error loading the world:\n"+e.getMessage(), ButtonType.OK).show();
						e.printStackTrace();
			        	System.gc();
					}
				}
	        	System.gc();
			}
        };
	}
	
	public static EventHandler<ActionEvent> handleFileReload(Stage primaryStage) {
		return e -> {

	        ButtonType sure = ButtonType.YES;
	        if (!disableWarnings.isSelected() && unsavedChanges) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reload the world ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {

				if (new File(lastDirectoryLoaded+lastFileLoaded).isFile())
				try {
					loadByFileType(primaryStage, new File(lastDirectoryLoaded+lastFileLoaded));
					System.gc();
				} catch (Exception e1) {
					e1.printStackTrace();
					new Alert(Alert.AlertType.ERROR, "Error reloading the world:\n"+e1.getMessage(), ButtonType.OK).show();
				} else
					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Nothing to reload!", ButtonType.OK).show();
			}
        };
	}

	public static EventHandler<ActionEvent> handleFileSave() {
		return e -> {
			new Alert(Alert.AlertType.ERROR, "Not implemented!", ButtonType.OK).show();
//			if (mainGeometry == null) {
//				if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Nothing to save!", ButtonType.OK).show();
//				return;
//			}
			
    		FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastFileSaved).getParentFile().isDirectory() ? new File(lastFileSaved).getParentFile() : new File("."));
			fc.setInitialFileName(lastFileLoaded);
//			fc.setInitialFileName(lastFileLoaded.replace(lastFileLoaded.split("\\.")[lastFileLoaded.split("\\.").length-1], "BIN"));
//			fc.setInitialFileName(mainGeometry.carname == Geometry.UNDETERMINED_CARNAME ? mainGeometry.geomHeader.geomInfo.filename : "GEOMETRY.BIN");
//			FileChooser.ExtensionFilter TYPE_GEOM;
//			FileChooser.ExtensionFilter TYPE_OBJ;
//			FileChooser.ExtensionFilter TYPE_Z3D;
//			FileChooser.ExtensionFilter TYPE_GLTF;
//			FileChooser.ExtensionFilter TYPE_GLB;
			fc.getExtensionFilters().addAll(
//			        TYPE_GEOM = 
			        new FileChooser.ExtensionFilter("Black Box NFS World Bundle", "*.bun")
//			        ,TYPE_GLTF = new FileChooser.ExtensionFilter("glTF", "*.gltf"),
//			        TYPE_OBJ = new FileChooser.ExtensionFilter("Wavefront OBJ", "*.obj"),
//			        TYPE_Z3D = new FileChooser.ExtensionFilter("ZModeler 2 Z3D", "*.z3d")
			        );
			
//			        TYPE_GLB = new FileChooser.ExtensionFilter("glTF Binary", "*.glb"));
			fc.setTitle("Save %s".formatted(""));
			

			try {
				File f = fc.showSaveDialog(null);
				if (f != null) {
					lastFileSaved = f.getAbsolutePath();
					if (f.isFile()) {
						File f_old = new File(lastFileSaved);
						
//						f_old.renameTo(new File(f_old.getAbsoluteFile() + ".bak_" + DateTimeFormatter.ofPattern("uuMMdd-HHmmss").format(LocalDateTime.ofEpochSecond(f_old.lastModified(), 0, ZoneOffset.UTC))));
						f_old.renameTo(new File(f_old.getAbsolutePath() + ".bak_" + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date(f_old.lastModified()))));
					}
					
					new Thread() {
		                @Override
						public void run() {
							try {
								//TODO
							} catch (Exception e2) {
								e2.printStackTrace();
								javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error saving world!\n"+e2.getMessage()).show());
							}
		                }
		            }.start();
					
					
		    		e.consume();        
				}
			} catch (Exception e1) {
				e1.printStackTrace();
            	new Alert(Alert.AlertType.ERROR, "Error saving world!\n"+e1.getMessage()).show();
			}
        };
	}
	
	public static EventHandler<ActionEvent> handleFileExit(Stage primaryStage) {
		return e -> {
	        ButtonType sure = ButtonType.YES;
	        if (!disableWarnings.isSelected() && unsavedChanges)  sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {
				primaryStage.close();
			}
        };
	}
	
	public static EventHandler<ActionEvent> handleTextureFolderSettings() {
		return e -> {
        	
        	var folders = new ListView<String>();
        	folders.setEditable(true);
        	folders.setCellFactory(TextFieldListCell.forListView());
        	folders.getItems().addAll(textureLookupFolders);
        	folders.getItems().addAll("", "");
        	
        	Stage st = new Stage();
			openWindows.add(st);
			st.setTitle("Manage texture lookup folders");
			Scene sc = new Scene(folders);
			if (useDarkMode.isSelected()) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			ContextMenu cm = new ContextMenu();
			MenuItem del = new MenuItem("Delete");
			del.setOnAction(e2 -> {
				folders.getItems().remove(folders.getSelectionModel().getSelectedIndex());
			});
			cm.getItems().add(del);
			folders.setContextMenu(cm);
			folders.setOnKeyPressed(e2 -> {
				if (e2.getCode() == KeyCode.DELETE) 
					folders.getItems().remove(folders.getSelectionModel().getSelectedIndex());
			});
			folders.addEventHandler(ListView.editCommitEvent(), e2 -> {
				for (var s : folders.getItems()) if (!s.isBlank() && !s.endsWith("\\")) s += "\\";
				if (!folders.getItems().get(folders.getItems().size()-2).isBlank()) folders.getItems().add("");
			});
			st.setOnCloseRequest(e2 -> {
				textureLookupFolders.clear();
				openWindows.remove(st);
				folders.getItems().forEach(itm -> {
					if (!itm.isBlank()) {
						if (!itm.endsWith("\\")) itm += "\\";
						textureLookupFolders.add(itm);
					}
				});
			});
			st.setWidth(600);
			st.setHeight(370);
			st.show();
        };
	}
	
	public static void loadByFileType(Stage primaryStage, File sel) throws Exception {
		File selected;
		if (sel.getName().contains("STREAM")) {
			selected = new File(sel.getParent() + File.separator + sel.getName().replace("STREAM", ""));
		} else selected = sel;
		
		currentChunk = null; //reset streaming
		
		new Thread(() -> {
			try {
				unsavedChanges = false;
				javafx.application.Platform.runLater(() -> {
					status.text.setText("Loading file...");
					status.progress.setProgress(-1);
				});
				
				mainStreamer = new WorldController(selected);
				
				lastFileLoaded = selected.getName();
				lastDirectoryLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
				if (recentFiles.contains(selected.getAbsolutePath())) recentFiles.remove(selected.getAbsolutePath());
				recentFiles.add(0, selected.getAbsolutePath());

				postLoadUpdate(primaryStage);
				
			} catch (Exception e1) {
				e1.printStackTrace();

				javafx.application.Platform.runLater(() -> {
					new Alert(Alert.AlertType.ERROR, "Error loading the world:\n"+e1.getMessage(), ButtonType.OK).show();
					status.text.setText(null);
					status.progress.setProgress(0);
				});
			}
		}).start();
	}

	public static int getNextInt(FileInputStream fis) throws IOException {
		return fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
	}

	public static void postLoadUpdate(@SuppressWarnings("unused") Stage primaryStage) {
		javafx.application.Platform.runLater(() -> {
//			primaryStage.setTitle(mainGeometry == null ? programName : programName + " - " + mainGeometry.carname);
			listViewStream.filter.setText("");

//			tpkImagesList.getItems().clear();
//			if (mainTPK != null && mainTPK.header!=null) {
//				tpkImagesList.getItems().addAll(mainTPK.textures);
//				if (mainTPK.textures.size() != mainTPK.header.texlist.textures.size()) {
//					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Could not load all textures!", ButtonType.OK).show();
//				}
//			}
//			
//			if (mainGeometry != null && !mainGeometry.isImported)
//				if (mainGeometry.parts.size() != mainGeometry.geomHeader.partsList.partKeys.size()) {
//					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Could not load all parts!", ButtonType.OK).show();
//			}

//			viewport.resetCamera();
			listViewStream.getItems().addAll(mainStreamer.streamControllers);
			listViewVisual.getItems().addAll(mainStreamer.visualControllers);
		});
		
		javafx.application.Platform.runLater(() -> {
			status.text.setText("Preparing materials display...");
			status.progress.setProgress(-1);
		});

//		listViewStream.updateTreeView();

		javafx.application.Platform.runLater(() -> {
			trackPathZonesGroup.getChildren().clear();
			markersGroup.getChildren().clear();
			elevationGroup.getChildren().clear();
			barrierSplinesGroup.getChildren().clear();
			modelsGroup.getChildren().clear();
			worldGridGroup.getChildren().clear();
			
			updateBoundaries();
			updateMarkers();
			updateBarrierSplines();
			updateWorldGridRoadNetwork();
			
			//TODO render all sorts of shit + filters
			
//			updateAllPartsDisplay();
//			displaySelectedKit();
			status.text.setText(null);
			status.progress.setProgress(0);
		});

	}

	private static void updateBoundaries() {
		boundariesGroup.getChildren().clear();
		for (var s : mainStreamer.streamControllers) {
			var m = s.getFlatMesh();
			if (m != null) {
				if ((!s.isPanorama() && regionBoundaries.isSelected()) ||
						s.isPanorama() && regionPanoramaBoundaries.isSelected()) {
					boundariesGroup.getChildren().add(m);
					m.setOnMouseClicked(e -> {
						listViewStream.getSelectionModel().select(s);
						listViewStream.list.scrollTo(s);
					});
				}
			}
		}
	}

	private static void updateTrackPathZones() {
		trackPathZonesGroup.getChildren().clear();
		for (var z : mainStreamer.zones1) 
			if (
					//regionTrackPathZones.isSelected() && 
					(trackPathZoneFilter.getValue() == eTrackPathZoneType.ALL || trackPathZoneFilter.getValue() == z.type)) {
			var m = z.getFlatMesh();
			if (m != null) trackPathZonesGroup.getChildren().add(m);
		}
	}
	
	public static double MARKER_SPHERE_SIZE = 5;
	private static void updateMarkers() {
		
		markersGroup.getChildren().clear();
		
		if (regionTrackMarkers.isSelected()) for (var m : mainStreamer.trackPositionMarkers) {
			var s = new Sphere(MARKER_SPHERE_SIZE);
			s.setTranslateX(m.pos[X]);
			s.setTranslateY(m.pos[Y]);
			s.setTranslateZ(m.pos[Z]);
			s.setMaterial(new PhongMaterial(Color.hsb(m.NameHash, 1, 1)));
			s.setOnMouseClicked(e -> {
				if (centerOnPart.isSelected() && e.isStillSincePress()) viewport.moveCameraNoListener(m.pos[X], m.pos[Y], m.pos[Z]);
			});
			markersGroup.getChildren().add(s);
			
			var t = new Tooltip("TrackPositionMarker\n"+
					"\nNameHash = "+Hash.getBIN(m.NameHash)+
					"\nParam = "+Hash.getBIN(m.Param)+
					"\nTrack Number = "+m.TrackNumber+
					"\nScenery Groups Count = "+m.NumSceneryGroups+
					"\nAngle = "+m.Angle);
			s.setOnMouseEntered(e -> t.show(s, e.getScreenX()+10, e.getScreenY()+10));
			s.setOnMouseExited(e -> t.hide());

		}
	}
	
	private static void updateBarrierSplines() {
		
		barrierSplinesGroup.getChildren().clear();
		
		if (regionBarrierSplines.isSelected()) for (var spline : mainStreamer.trackPathBarriers) {
			var line = new MeshView(doVerticalRectangle(spline.from[X], spline.from[Y], 0, spline.to[X], spline.to[Y], 200));
			line.setCullFace(CullFace.NONE);
			
			line.setMaterial(new PhongMaterial(Color.hsb(spline.GroupHash*20, 1, 1)));
			barrierSplinesGroup.getChildren().add(line);

			var t = new Tooltip(barrierSplineHashes.get(spline.GroupHash) != null ? "BARRIER_SPLINE_%04d".formatted(barrierSplineHashes.get(spline.GroupHash)) : Hash.getBIN(spline.GroupHash));
			line.setOnMouseEntered(e -> t.show(line, e.getScreenX()+10, e.getScreenY()+10));
			line.setOnMouseExited(e -> t.hide());	

		}
	}
	
	private static void updateWorldGridRoadNetwork() {
		new Thread(() -> {
			try {
				var mtl_road = new PhongMaterial(Color.BLACK);
				var mtl_shortcut = new PhongMaterial(Color.GRAY);
				var mtl_unknown = new PhongMaterial(Color.MAGENTA);
				var nodes = new ArrayList<Node>();
				
				if (regionRoadNetwork.isSelected() || regionRoadNetworkDetailed.isSelected()) {
					if (mainStreamer != null && mainStreamer.worldGrid != null && mainStreamer.worldGrid.root.hasEntry(UDataID.RoadNetworkGroup)) {
						var rn = mainStreamer.worldGrid.root.getEntry(UDataID.RoadNetworkGroup);
						
						var rnNodes = ((CARP.RoadNetworkNodes)rn.getData(UDataID.RoadNetworkNodes)).nodes;
						var rnSegments = ((CARP.RoadNetworkSegments)rn.getData(UDataID.RoadNetworkSegments)).segments;
						var rnRoads = ((CARP.RoadNetworkRoads)rn.getData(UDataID.RoadNetworkRoads)).wRoads;
						var rnProfiles = ((CARP.RoadNetworkProfiles)rn.getData(UDataID.RoadNetworkProfiles)).profiles;
						
//						for (var n : rnNodes) {
//							var s = new Sphere(0.5);
//							s.setTranslateX(n.position[Z]);
//							s.setTranslateY(-n.position[X]);
//							s.setTranslateZ(n.position[Y]);
//							s.setMaterial(mat);
//							worldGridGroup.getChildren().add(s);
//						}
						
						
						for (var s : rnSegments) {
							var n1 = rnNodes.get(s.nodeStart);
							var n2 = rnNodes.get(s.nodeEnd);

							WRoad road = null;
							if (s.roadID != -1) road = rnRoads.get(s.roadID);
//							if (s.roadID == -1) continue;

							if (regionRoadNetworkDetailed.isSelected()) {
//								var road = rnRoads.get(s.roadID);
								var profile1 = rnProfiles.get(n1.profileIndex);
								var profile2 = rnProfiles.get(n2.profileIndex);

								var pos1 = new float[]{n1.position[Z], -n1.position[X], n1.position[Y]};
								var pos2 = new float[]{n2.position[Z], -n2.position[X], n2.position[Y]};
								
								var vec01 = new float[] {pos2[0]-pos1[0], pos2[1]-pos1[1], pos2[2]-pos1[2]};
								normalize(vec01);
								
								var lateral = new float[3];
								var vertical = new float[] {0, 0, 1};
								crossProduct(vec01, vertical, lateral);
								
								for (int i=0; i<profile1.numZones && i<profile2.numZones; i++) {
									var mv = new MeshView(VisualController.doLineMesh(
											pos1[0] + profile1.getLaneOffset(i)*lateral[0],
											pos1[1] + profile1.getLaneOffset(i)*lateral[1],
											pos1[2] + profile1.getLaneOffset(i)*lateral[2],

											pos2[0] + profile2.getLaneOffset(i)*lateral[0],
											pos2[1] + profile2.getLaneOffset(i)*lateral[1],
											pos2[2] + profile2.getLaneOffset(i)*lateral[2]
											));
									mv.setDrawMode(DrawMode.LINE);
//									if (road.shortcut == 1) mv.setMaterial(mtl_shortcut);
//									else mv.setMaterial(mtl_road);
									mv.setMaterial(profile1.getLaneType(i).mat);
									
									nodes.add(mv);
								}					
							} else {
								var mv = new MeshView(VisualController.doLineMesh(n1.position[Z], -n1.position[X], n1.position[Y],
								n2.position[Z], -n2.position[X], n2.position[Y]));
								mv.setDrawMode(DrawMode.LINE);
								if (road != null && road.shortcut == -1)
									mv.setMaterial(mtl_road);
								else if (road != null)
									mv.setMaterial(mtl_unknown);
//								else mv.setMaterial(mtl_unknown);
								else mv.setMaterial(mtl_shortcut);
								
								nodes.add(mv);
							}
						}
					}
				}
				
				javafx.application.Platform.runLater(() -> {
					worldGridGroup.getChildren().clear();
					worldGridGroup.getChildren().addAll(nodes);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

	}
	
	static void crossProduct(float vect_A[], float vect_B[], float cross_P[])	{
		cross_P[0] = vect_A[1] * vect_B[2] - vect_A[2] * vect_B[1];
		cross_P[1] = vect_A[2] * vect_B[0] - vect_A[0] * vect_B[2];
		cross_P[2] = vect_A[0] * vect_B[1] - vect_A[1] * vect_B[0];
	}
	static void normalize(float[] vec) {
		var prod = Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
		vec[0] /= prod;
		vec[1] /= prod;
		vec[2] /= prod;
	}

	private static TriangleMesh doVerticalRectangle(float x1, float y1, float z1, float x2, float y2, float z2) {
		return VisualController.doVerticalRectangle(x1, y1, z1, x2, y2, z2);
	}
	
	static class Point{
		float x;
		float y;
		Point(float x, float y){
			this.x = x;
			this.y = y;
		}
		Point(double x, double y){
			this.x = (float)x;
			this.y = (float)y;
		}
		
		public static boolean onSegment(Point p, Point q, Point r)
	    {
	        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x)
	                && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
	            return true;
	        return false;
	    }
	 
	    public static int orientation(Point p, Point q, Point r)
	    {
	        float val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
	 
	        if (val == 0)
	            return 0;
	        return (val > 0) ? 1 : 2;
	    }
	 
	    public static boolean doIntersect(Point p1, Point q1, Point p2, Point q2)
	    {
	 
	        var o1 = orientation(p1, q1, p2);
	        var o2 = orientation(p1, q1, q2);
	        var o3 = orientation(p2, q2, p1);
	        var o4 = orientation(p2, q2, q1);
	 
	        if (o1 != o2 && o3 != o4)
	            return true;
	 
	        if (o1 == 0 && onSegment(p1, p2, q1))
	            return true;
	 
	        if (o2 == 0 && onSegment(p1, q2, q1))
	            return true;
	 
	        if (o3 == 0 && onSegment(p2, p1, q2))
	            return true;
	 
	        if (o4 == 0 && onSegment(p2, q1, q2))
	            return true;
	 
	        return false;
	    }
	 
	    public static boolean isInside(Point polygon[], int n, Point p)
	    {
	        int INF = 10000;
	        if (n < 3)
	            return false;
	 
	        Point extreme = new Point(INF, p.y);
	 
	        int count = 0, i = 0;
	        do
	        {
	            int next = (i + 1) % n;
	            if (doIntersect(polygon[i], polygon[next], p, extreme))
	            {
	                if (orientation(polygon[i], p, polygon[next]) == 0)
	                    return onSegment(polygon[i], p, polygon[next]);
	 
	                count++;
	            }
	            i = next;
	        } while (i != 0);
	 
	        return (count & 1) == 1 ? true : false;
	    }
	    public boolean isInside(Point[] polygon) {
	    	return isInside(polygon, polygon.length, this); 
	    }
	    public boolean isInside(float[][] p) {
			Point[] poly = new Point[p.length];
			for (int i=0;i<p.length;i++) poly[i] = new Point(p[i][X], p[i][Y]);
	    	var b = isInside(poly, p.length, this);
	    	poly = null;
	    	return b;
	    }
	    public boolean isInside(float[] ptX, float[] ptY) {
			Point[] poly = new Point[ptX.length];
			for (int i=0;i<ptX.length;i++) poly[i] = new Point(ptX[i], ptY[i]);
	    	var b = isInside(poly, ptX.length, this);
	    	poly = null;
	    	return b;
	    }
	}
}