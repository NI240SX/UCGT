package fr.ni240sx.ucgt.geometryFile;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.CollisionsEditor;
import fr.ni240sx.ucgt.collisionsEditor.OrbitCameraViewport;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.dbmpPlus.DBMPPlus;
import fr.ni240sx.ucgt.geometryFile.geometry.GeomHeader;
import fr.ni240sx.ucgt.geometryFile.gui.AutosculptControls;
import fr.ni240sx.ucgt.geometryFile.gui.ConfigSetting;
import fr.ni240sx.ucgt.geometryFile.gui.CustomProgressBar;
import fr.ni240sx.ucgt.geometryFile.gui.FileOffset;
import fr.ni240sx.ucgt.geometryFile.gui.PartController;
import fr.ni240sx.ucgt.geometryFile.gui.PartInfo;
import fr.ni240sx.ucgt.geometryFile.gui.SearchableListView;
import fr.ni240sx.ucgt.geometryFile.gui.SearchableTreeView;
import fr.ni240sx.ucgt.geometryFile.io.GLTF;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;
import fr.ni240sx.ucgt.geometryFile.textures.NFSTexture;
import fr.ni240sx.ucgt.geometryFile.textures.TPK;
import fr.ni240sx.ucgt.geometryFile.textures.TPKHeader;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.nikr.dds.DDSImageReader;
import tech.dashman.dashman.StageSizer;

public class GeometryEditorGUI extends Application {
	
	public static boolean debug = false;

	//stuff to put in a config file
	public static String lastDirectoryLoaded = Paths.get("").toAbsolutePath().toString();
	public static String lastFileLoaded = "";
	public static String lastFileSaved = Paths.get("").toAbsolutePath().toString(); //now unused
    public static CheckMenuItem useDarkMode = new CheckMenuItem("Dark mode");
    public static CheckMenuItem disableWarnings = new CheckMenuItem("Disable warning pop-ups");
//	public static boolean widebodyAutoCorrect = true;
	
	public static final String programName = "UCGT Geometry Editor";
	public static final String programGUIVersion = "1.0.0";
	public static final String settingsFolder = "settings";
	public static final String settingsFile = "settings/geomGUI.dat";
	
	public static Group viewportGroup = new Group();
	public static OrbitCameraViewport viewport;

	public static Geometry mainGeometry = null;
	public static TPK mainTPK = null;
	public static ArrayList<PhongMaterial> visualModelMaterials = null;
	public static Group modelGroup = new Group();
	
    public static StageSizer stageSizer = new StageSizer();
	
    public static SearchableTreeView<PartController> partsDisplay = new SearchableTreeView<>();
    public static ArrayList<PartController> parts = new ArrayList<>();
    public static TreeItem<PartController> partsRoot = new TreeItem<>();
    public static AutosculptControls autosculptControls = new AutosculptControls();
    public static PartInfo partInfo = new PartInfo();
    public static SearchableListView<NFSTexture> tpkImagesList = new SearchableListView<>();
    
    public static ComboBox<String> displayLod;
    public static ComboBox<String> displayKit;

    public static Spinner<Double> wheelPosFrontX = new Spinner<>(0,5,1.5,0.01);
    public static Spinner<Double> wheelPosRearX = new Spinner<>(-5,0,-1.25,0.01);
    public static Spinner<Double> wheelPosFrontY = new Spinner<>(0,1.5,0.9,0.01);
    public static Spinner<Double> wheelPosRearY = new Spinner<>(0,1.5,0.9,0.01);
    public static Spinner<Double> wheelPosHeight = new Spinner<>(-0.5,1.5,0.2,0.01);
    
    public static CheckMenuItem allowConflictingParts = new CheckMenuItem("Allow conflicting parts");
    public static CheckMenuItem strictKitDisplay = new CheckMenuItem("Strict kit display");
    public static CheckMenuItem simplifiedPartsList = new CheckMenuItem("Simplified parts list");
    public static CheckMenuItem loadTextures = new CheckMenuItem("Load textures");
    public static CheckMenuItem displayMarkers = new CheckMenuItem("Display markers");
    public static CheckMenuItem trackpadMode = new CheckMenuItem("Trackpad mode");
    public static CheckMenuItem centerOnPart = new CheckMenuItem("Center camera on selected part");
    public static CheckMenuItem importExportTextures = new CheckMenuItem("Import and export textures along with geometry");
    
    public static ArrayList<String> textureLookupFolders = new ArrayList<>();
    public static ArrayList<Stage> openWindows = new ArrayList<>();
    
    public static CustomProgressBar status = new CustomProgressBar();
    
    public static CheckMenuItem showImportSettingsOnReload = new CheckMenuItem("Show import settings on reload");

    public static boolean unsavedChanges = false;
    public static boolean firstBoot = true;
    
	public static void main(String[] args) {
		try {
			readSettings();
		} catch (@SuppressWarnings("unused") Exception e) {
			simplifiedPartsList.setSelected(true);
			loadTextures.setSelected(true);
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
				+ stageSizer.getHeight() + "\n"
				+ stageSizer.getWidth() + "\n"
				+ stageSizer.getX() + "\n"
				+ stageSizer.getY() + "\n"
				+ stageSizer.getMaximized() + "\n"
				+ Boolean.toString(useDarkMode.isSelected()) + "\n"
				+ Boolean.toString(disableWarnings.isSelected()) + "\n"
				+ Boolean.toString(allowConflictingParts.isSelected()) + "\n"
				+ Boolean.toString(strictKitDisplay.isSelected()) + "\n"
				+ Boolean.toString(simplifiedPartsList.isSelected()) + "\n"
				+ Boolean.toString(loadTextures.isSelected()) + "\n");
		for (var f : textureLookupFolders) {
			bw.write(f+">");
		}
		bw.write("\n"
				+ Boolean.toString(trackpadMode.isSelected()) + "\n"
				+ Boolean.toString(centerOnPart.isSelected()) + "\n"
				+ Boolean.toString(importExportTextures.isSelected()) + "\n"
				+ Boolean.toString(firstBoot) + "\n");
		
		bw.close();
	}

	public static void readSettings() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(settingsFile)));
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
		allowConflictingParts.setSelected(Boolean.valueOf(br.readLine()));
		strictKitDisplay.setSelected(Boolean.valueOf(br.readLine()));
		simplifiedPartsList.setSelected(Boolean.valueOf(br.readLine()));
		loadTextures.setSelected(Boolean.valueOf(br.readLine()));
		for (var s : br.readLine().split(">")) if (s != null && !s.isBlank()) textureLookupFolders.add(s);
		trackpadMode.setSelected(Boolean.valueOf(br.readLine()));
		centerOnPart.setSelected(Boolean.valueOf(br.readLine()));
		importExportTextures.setSelected(Boolean.valueOf(br.readLine()));
		firstBoot = Boolean.valueOf(br.readLine());
		//String s;
		//if(!(s=br.readLine()).isBlank())
		br.close();
	}
	
    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle(mainGeometry == null ? programName : programName + " - " + mainGeometry.carname);

        VBox windowTop = new VBox();
        
        //
        // MENUS BAR
        //
        
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("File");
        MenuItem fileNew = new MenuItem("New");
        MenuItem fileLoad = new MenuItem("Load");
        MenuItem fileReload = new MenuItem("Reload");
        MenuItem fileSave = new MenuItem("Save");
        MenuItem fileExit = new MenuItem("Exit");
        MenuItem fileConvert = new MenuItem("Convert CTK config...");
        MenuItem fileChangeComp = new MenuItem("Change compression of an existing Geometry...");

        menuFile.getItems().addAll(fileNew, fileLoad, fileReload, fileSave, new SeparatorMenuItem(), fileConvert, fileChangeComp, new SeparatorMenuItem(), fileExit);
        menuFile.setAccelerator(KeyCombination.keyCombination("Alt+F"));

        fileNew.setOnAction(e -> {
    		new Thread(() -> {
				try {
		        	mainGeometry = null;
		        	mainTPK = null;
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
        fileSave.setOnAction(handleFileSave());
        fileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        fileExit.setOnAction(handleFileExit(primaryStage));
        fileExit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
        fileConvert.setOnAction(handleConvert());
        fileChangeComp.setOnAction(handleChangeComp());
        
        
        
        Menu menuDisplay = new Menu("Display");
        MenuItem textureFolders = new MenuItem("Texture folders...");
        menuDisplay.getItems().addAll(allowConflictingParts, strictKitDisplay, simplifiedPartsList, centerOnPart, displayMarkers, loadTextures, textureFolders);
        menuDisplay.setAccelerator(KeyCombination.keyCombination("Alt+D"));
        allowConflictingParts.setAccelerator(KeyCombination.keyCombination("Alt+C"));
        strictKitDisplay.setAccelerator(KeyCombination.keyCombination("Alt+K"));
        simplifiedPartsList.setAccelerator(KeyCombination.keyCombination("Alt+L"));
        displayMarkers.setAccelerator(KeyCombination.keyCombination("Alt+M"));
        
        allowConflictingParts.setOnAction(e ->{
        	if (!allowConflictingParts.isSelected()) updateRender();
        });
        strictKitDisplay.setOnAction(e ->{
        	if (strictKitDisplay.isSelected()) displaySelectedKit();
        });
        simplifiedPartsList.setOnAction(e -> {
        	updateAllPartsDisplay();
        });
        displayMarkers.setOnAction(e -> {
        	for (var p : parts) if (p.part != null && p.part.mpoints != null) p.updateMarkersRender(); 
        });
        textureFolders.setOnAction(handleTextureFolderSettings());
        
        Menu menuTools = new Menu("Tools");
        MenuItem toolCarName = new MenuItem("Change car name...");
        MenuItem toolPlatform = new MenuItem("Change platform...");
        MenuItem toolSaving = new MenuItem("Saving settings...");

        menuTools.getItems().addAll(toolCarName, toolPlatform, toolSaving);
        menuTools.setAccelerator(KeyCombination.keyCombination("Alt+T"));
        toolCarName.setOnAction(handleToolCarName(primaryStage));
        toolPlatform.setOnAction(handleToolPlatform(primaryStage));
        toolSaving.setOnAction(e -> new Thread(() -> {
			try {
	        	showGeometrySettings(true);
				postLoadUpdate(primaryStage);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        }).start());
        /*
         * TODO
         * more saving settings ?
         * prostreet to uc should work but doesn't
         */

        
        Menu menuSettings = new Menu("Settings");
        MenuItem settingsAbout = new MenuItem("About " + programName + "...");
        MenuItem settingsUpdates = new MenuItem("Check for updates...");
        menuSettings.getItems().addAll(importExportTextures, useDarkMode, disableWarnings, trackpadMode, showImportSettingsOnReload, settingsAbout, settingsUpdates);
        menuSettings.setAccelerator(KeyCombination.keyCombination("Alt+S"));

        settingsAbout.setOnAction(e -> showInfoWindow());
        settingsUpdates.setOnAction(e -> {
	    	getHostServices().showDocument("https://github.com/NI240SX/UCGT/releases");
			new Alert(Alert.AlertType.INFORMATION, "Current versions:\n- Geometry Editor v"+GeometryEditorCLI.programVersion+" built on "+GeometryEditorCLI.programBuild+"\n"
					+ "- DBModelParts Editor v"+DBMPPlus.programVersion +"\n- Collisions Editor v"+CollisionsEditor.programVersion, ButtonType.OK).showAndWait();
        });
                
        menuBar.getMenus().addAll(menuFile, menuDisplay, menuTools, menuSettings);
        		
        
        //
        // SHORTCUTS BAR
        //
        
        
        
        HBox shortcutsBar = new HBox();
        
        Separator infSep = new Separator();
        HBox.setHgrow(infSep, Priority.ALWAYS);

        displayLod = new ComboBox<>();        
        displayKit = new ComboBox<>();

        displayLod.setOnAction(e -> displaySelectedLod());
        displayKit.setOnAction(e -> displaySelectedKit());
        
        wheelPosFrontX.valueProperty().addListener(v -> {
        	for (var p : parts) {
        		if (p.display.get() && p.part != null && p.part.mesh != null) p.updateWheelsPos();
        	}
        });
        wheelPosFrontY.valueProperty().addListener(v -> {
        	for (var p : parts) {
        		if (p.display.get() && p.part != null && p.part.mesh != null) p.updateWheelsPos();
        	}
        });
        wheelPosRearX.valueProperty().addListener(v -> {
        	for (var p : parts) {
        		if (p.display.get() && p.part != null && p.part.mesh != null) p.updateWheelsPos();
        	}
        });
        wheelPosRearY.valueProperty().addListener(v -> {
        	for (var p : parts) {
        		if (p.display.get() && p.part != null && p.part.mesh != null) p.updateWheelsPos();
        	}
        });
        wheelPosHeight.valueProperty().addListener(v -> {
        	for (var p : parts) {
        		if (p.display.get() && p.part != null && p.part.mesh != null) p.updateWheelsPos();
        	}
        });
        wheelPosFrontX.setMaxWidth(70);
        wheelPosFrontY.setMaxWidth(70);
        wheelPosRearX.setMaxWidth(70);
        wheelPosRearY.setMaxWidth(70);
        wheelPosHeight.setMaxWidth(70);

        
        shortcutsBar.getChildren().addAll(
        		new Label("Kit"), displayKit, 
        		new Label("LOD"), displayLod, 
        		infSep, 
        		new Label("Front wheels"), wheelPosFrontX, wheelPosFrontY, 
        		new Label("Rear wheels"), wheelPosRearX, wheelPosRearY, 
        		new Label("Wheels height"), wheelPosHeight);
        
        
        
        
        
        
        
        windowTop.getChildren().addAll(menuBar, shortcutsBar);
        
        viewport = new OrbitCameraViewport(viewportGroup, 0, 0);
        viewportGroup.getChildren().add(modelGroup);
        viewport.trackpadMode.bind(trackpadMode.selectedProperty());
        
        var partsPane = new SplitPane();
        partsPane.getItems().addAll(partInfo, autosculptControls);
        partsPane.setOrientation(Orientation.VERTICAL);

        
        
        var tpkImagePreview = new ImageView();
        tpkImagePreview.setPreserveRatio(true);
        var tpkImagePreviewSP = new StackPane();
		tpkImagePreviewSP.getChildren().add(tpkImagePreview);
        var tpkDisplay = new BorderPane();
        tpkDisplay.setCenter(tpkImagesList);
        tpkDisplay.setBottom(tpkImagePreviewSP);
//        tpkDisplay.getItems().addAll(tpkImagesList, tpkImagePreviewSP);
//        tpkDisplay.setOrientation(Orientation.VERTICAL);
        
        
        var rightPane = new SplitPane();
        rightPane.getItems().addAll(partsPane, tpkDisplay);
        rightPane.setDividerPosition(0,1);
        
        tpkImagesList.getSelectionModel().selectedItemProperty().addListener((obs, was, is) -> {
        	if (is == null) {
        		tpkImagePreview.setImage(null);
				tpkImagePreviewSP.setMaxHeight(0);
        	}
        	else {
				try {
	        		BufferedImage image;
					var r = new DDSImageReader(null);
					var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(is.DDSImage));
					r.setInput(iis);
					image = r.read(0);
					tpkImagePreview.setImage(SwingFXUtils.toFXImage(image, null));
					iis.close();

					tpkImagePreviewSP.setMinHeight(tpkImagePreview.getImage().getHeight()*tpkImagePreviewSP.getWidth()/tpkImagePreview.getImage().getWidth());
					tpkImagePreviewSP.setMaxHeight(tpkImagePreview.getImage().getHeight()*tpkImagePreviewSP.getWidth()/tpkImagePreview.getImage().getWidth());
					
				} catch (IOException e1) {
					System.out.println("Unable to display TPK image: "+e1.getMessage());
//					e1.printStackTrace();
				}
        	}
        });
		tpkImagePreview.fitWidthProperty().bind(tpkDisplay.widthProperty());
		tpkImagesList.maxWidthProperty().bind(tpkDisplay.widthProperty());
		tpkImagesList.setMinWidth(0);
		tpkDisplay.widthProperty().addListener((obs, was, is) -> {
			if (tpkImagePreview.getImage() != null) {
				tpkImagePreviewSP.setMinHeight(tpkImagePreview.getImage().getRequestedHeight()*tpkImagePreviewSP.getWidth()/tpkImagePreview.getImage().getWidth());
				tpkImagePreviewSP.setMaxHeight(tpkImagePreview.getImage().getRequestedHeight()*tpkImagePreviewSP.getWidth()/tpkImagePreview.getImage().getWidth());
			} else {
				tpkImagePreviewSP.setMaxHeight(0);
			}
		});
        tpkImagePreview.setManaged(false);

        
        BorderPane sp = new BorderPane();
        sp.setCenter(viewport);
        sp.setBottom(status);
        
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(partsDisplay, sp, rightPane);
        partsDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        partsDisplay.setRoot(partsRoot);
        partsDisplay.getSelectionModel().selectedItemProperty().addListener(l -> {
        	if (partsDisplay.getSelectionModel().getSelectedItem() != null) GeometryEditorGUI.partInfo.update(partsDisplay.getSelectionModel().getSelectedItem().getValue());
        	else GeometryEditorGUI.partInfo.update(null);
        });
        partsDisplay.list.setOnKeyPressed(e -> {
        	if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER|| e.getCode() == KeyCode.H) {
        		partsDisplay.getSelectionModel().getSelectedItems().forEach(itm -> {
        			if (itm != null) {
            			itm.getValue().display.set(!partsDisplay.getSelectionModel().getSelectedItem().getValue().display.get());
            			itm.getChildren().forEach(itm2 -> {
                			itm2.getValue().display.set(!partsDisplay.getSelectionModel().getSelectedItem().getValue().display.get());
                			itm2.getChildren().forEach(itm3 -> {
                    			itm3.getValue().display.set(!partsDisplay.getSelectionModel().getSelectedItem().getValue().display.get());
//                    			itm3.getChildren().forEach(null);
                    		});
                		});
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
        		if (e.isAltDown()) partsDisplay.getSelectionModel().getSelectedItems().forEach(itm -> itm.getValue().display.set(true));
        		else partsDisplay.getSelectionModel().getSelectedItems().forEach(itm -> itm.getValue().display.set(false));
        		break;
        	case A:
        		partsRoot.getChildren().forEach(p -> {
        			if (p.getValue().display.get()) partsDisplay.getSelectionModel().select(p);
        		});
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
		

		updateAllPartsDisplay();
        updateRender();
        
        
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
				loadByFileType(primaryStage, new File(lastDirectoryLoaded + lastFileLoaded), true);
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

    public void showInfoWindow() {
    	Stage st = new Stage();
		openWindows.add(st);
		st.setTitle("About " + programName);
		
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
				GUI version %s
				needeka 2026
				""".formatted(programName, GeometryEditorCLI.programVersion, GeometryEditorCLI.programBuild, programGUIVersion));
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
    
	public static EventHandler<ActionEvent> handleToolPlatform(Stage primaryStage) {
		return e -> {
    		var td = new ChoiceDialog<Platform>();
    		td.getItems().addAll(Platform.values());
    		if (mainGeometry != null) td.setSelectedItem(mainGeometry.platform);
    		td.setHeaderText("Select a platform to convert the model to.");
    		var result = td.showAndWait();
    		if (result.isEmpty()) return;
			final var plat = result.get();
			new Thread (() -> {
	    		try {
	    			changePlatform(plat);
					postLoadUpdate(primaryStage);
					if (!disableWarnings.isSelected()) javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Platform changed.").show());
					e.consume();
	    		}catch (Exception ex) {
					javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error changing platform!\n"+ex.getMessage()).show());
					ex.printStackTrace();
	    		}

			}).start();

        };
	}

	public static void changePlatform(final Platform plat) {
		if (mainGeometry != null) {
			unsavedChanges = true;
			mainGeometry.changePlatform(plat);
			//	    				mainGeometry.rebuild();
	//	    				mainGeometry.geomHeader.refresh(mainGeometry);
			if (mainTPK != null) {
				mainTPK.version = switch(plat) {
				case PC:
				case X360:
					yield 9;
				default:
					yield 8;
				};
			}
		}
	}

	public static EventHandler<ActionEvent> handleToolCarName(Stage primaryStage) {
		return e -> {
			try {
	    		TextInputDialog td = new TextInputDialog();
	    		td.setHeaderText("Enter a new car/group name.");
	    		var result = td.showAndWait();
	    		if (result.isEmpty()) return;
				String s = result.get().strip();
				if (s.isBlank()) s = Geometry.UNDETERMINED_CARNAME;
				final var f = s;
				new Thread(() -> {
					try {
						changeCarName(mainGeometry.carname, f);
	    				postLoadUpdate(primaryStage);
	    				if (!disableWarnings.isSelected()) javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Car name changed.").show());    					
					}catch (Exception ex) {
						javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error changing car name!\n"+ex.getMessage()).show());
						ex.printStackTrace();					
					}
				}).start(); 					
			}catch (Exception ex) {
				javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error changing car name!\n"+ex.getMessage()).show());
				ex.printStackTrace();					
			}

        };
	}

	public static void changeCarName(String oldName, String newName) {
		if (mainGeometry != null) {
			unsavedChanges = true;
			mainGeometry.carname = newName;
			for (var p : mainGeometry.parts) {
				p.rename(newName, p.header.partName.replace(oldName, newName));
				if (p.asZones != null) p.asZones.zones = p.generateASZones();
				if (p.asLinking != null) for (var l : p.asLinking.links) {
					l.partKey = Hash.findBIN(Hash.getBIN(l.partKey).replace(oldName, newName));
				}
				if (p.mesh != null) for (var m : p.mesh.materials.materials) {
					for (int i=0; i<  m.TextureHashes.size(); i++) {
						m.TextureHashes.set(i, Hash.findBIN(Hash.getBIN(m.TextureHashes.get(i)).replace(oldName, newName)));
					}
				}
				if (p.texusage != null) for (int i=0; i< p.texusage.texusage.size(); i++) {
					p.texusage.texusage.set(i, new Pair<>(
							Hash.findBIN(Hash.getBIN(p.texusage.texusage.get(i).getKey()).replace(oldName, newName)), 
							p.texusage.texusage.get(i).getValue()));
				}
			}
			for (var m : mainGeometry.materials) {
				for (int i=0; i<  m.TextureHashes.size(); i++) {
					m.TextureHashes.set(i, Hash.findBIN(Hash.getBIN(m.TextureHashes.get(i)).replace(oldName, newName)));
				}
			}
			
			if (mainTPK != null) {
				for (var t : mainTPK.textures) {
					var n = Hash.getBIN(t.binKey).replace(oldName, newName);
					t.name = n;
					t.binKey = Hash.findBIN(n);
				}
			}
		}
	}
    
	public static EventHandler<ActionEvent> handleSettingsAbout() {
		return e -> {
			Stage st = new Stage();
			openWindows.add(st);
			st.setTitle("About " + programName);
			Scene sc = new Scene(new Label(programName + " version " + GeometryEditorCLI.programVersion + "; GUI version " + programGUIVersion + "\n\n"
					+ "Geometry editor for NFS Undercover.\n"
					+ "Aims to support the file format better.\n"
					+ "This software has been originally created for the mod Undercover Exposed.\n\n"
					+ "Not affiliated with EA, MaxHwoy, nfsu360, etc.\n\n"
					+ "needeka 2026 - No rights reserved"));
			st.setScene(sc);
			if (useDarkMode.isSelected()) sc.getRoot().setStyle("-fx-base:black");
			st.setResizable(false);
			st.setAlwaysOnTop(true);
			sc.setOnMouseClicked(evt -> {
				evt.consume();
				openWindows.remove(st);
				st.close();
			});
			st.setOnCloseRequest(e2 -> openWindows.remove(st));
			st.show();
        };
	}
	
	public static EventHandler<ActionEvent> handleFileLoad(Stage primaryStage) {
		return evt -> {
			
	        ButtonType sure = ButtonType.YES;
	        if (!disableWarnings.isSelected() && unsavedChanges) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to load a new model ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {

			
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File(lastDirectoryLoaded).isDirectory() ? new File(lastDirectoryLoaded) : new File("."));
				fc.setInitialFileName("GEOMETRY.BIN");
				fc.getExtensionFilters().addAll(
				        new FileChooser.ExtensionFilter("All supported files", "*.bin", "*.bun", "*.obj", "*.z3d", "*.gltf", "*.glb", "*.ini"),
				        new FileChooser.ExtensionFilter("Black Box NFS Geometry File", "*.bin"),
				        new FileChooser.ExtensionFilter("Black Box NFS Bundle File", "*.bun"),
				        new FileChooser.ExtensionFilter("Wavefront OBJ", "*.obj"),
				        new FileChooser.ExtensionFilter("ZModeler 2 Z3D", "*.z3d"),
				        new FileChooser.ExtensionFilter("glTF", "*.gltf", "*.glb"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load any model");
				File selected = fc.showOpenDialog(null);
				if (selected != null) {
					try {
						loadByFileType(primaryStage, selected, true);
						
					} catch (Exception e) {
						new Alert(Alert.AlertType.ERROR, "Error loading the model:\n"+e.getMessage(), ButtonType.OK).show();
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
	        if (!disableWarnings.isSelected() && unsavedChanges) sure = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reload the model ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {

				if (new File(lastDirectoryLoaded+lastFileLoaded).isFile())
				try {
					loadByFileType(primaryStage, new File(lastDirectoryLoaded+lastFileLoaded), showImportSettingsOnReload.isSelected());
					System.gc();
				} catch (Exception e1) {
					e1.printStackTrace();
					new Alert(Alert.AlertType.ERROR, "Error reloading the model:\n"+e1.getMessage(), ButtonType.OK).show();
				} else
					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Nothing to reload!", ButtonType.OK).show();
			}
        };
	}

	public static EventHandler<ActionEvent> handleFileSave() {
		return e -> {
			
			if (mainGeometry == null) {
				if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Nothing to save!", ButtonType.OK).show();
				return;
			}
			
    		FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastFileSaved).getParentFile().isDirectory() ? new File(lastFileSaved).getParentFile() : new File("."));
//			fc.setInitialFileName(lastFileLoaded.replace(lastFileLoaded.split("\\.")[lastFileLoaded.split("\\.").length-1], "BIN"));
			fc.setInitialFileName(mainGeometry.carname == Geometry.UNDETERMINED_CARNAME ? mainGeometry.geomHeader.geomInfo.filename : "GEOMETRY.BIN");
			FileChooser.ExtensionFilter TYPE_GEOM;
			FileChooser.ExtensionFilter TYPE_OBJ;
			FileChooser.ExtensionFilter TYPE_Z3D;
			FileChooser.ExtensionFilter TYPE_GLTF;
//			FileChooser.ExtensionFilter TYPE_GLB;
			fc.getExtensionFilters().addAll(
			        TYPE_GEOM = new FileChooser.ExtensionFilter("Black Box NFS Geometry File", "*.bin"),
			        TYPE_GLTF = new FileChooser.ExtensionFilter("glTF", "*.gltf"),
			        TYPE_OBJ = new FileChooser.ExtensionFilter("Wavefront OBJ", "*.obj"),
			        TYPE_Z3D = new FileChooser.ExtensionFilter("ZModeler 2 Z3D", "*.z3d"));
			
//			        TYPE_GLB = new FileChooser.ExtensionFilter("glTF Binary", "*.glb"));
			fc.selectedExtensionFilterProperty().addListener((obs, was, is) -> {
				if (obs.getValue() != TYPE_GEOM) fc.setInitialFileName(mainGeometry.carname == Geometry.UNDETERMINED_CARNAME ? mainGeometry.geomHeader.geomInfo.filename : mainGeometry.carname);
				else fc.setInitialFileName(mainGeometry.carname == Geometry.UNDETERMINED_CARNAME ? mainGeometry.geomHeader.geomInfo.filename : "GEOMETRY.BIN");
			});
			fc.setTitle("Save " + mainGeometry.carname+ "");
			

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
								if (fc.getSelectedExtensionFilter() == TYPE_GEOM) {
									mainGeometry.save(f, status);
									if (importExportTextures.isSelected()) { // && mainGeometry.isImported
										var fTex = new File(f.getParent()+File.separator+"TEXTURES.BIN");
										if (fTex.isFile()) {
											fTex.renameTo(new File(fTex.getAbsolutePath() + ".bak_" + new SimpleDateFormat("yyMMdd-HHmmss").format(new Date(fTex.lastModified()))));
										}
										mainTPK.defaultCompressionType = mainGeometry.defaultCompressionType;
										mainTPK.defaultCompressionLevel = mainGeometry.defaultCompressionLevel;
										mainTPK.save(new File(f.getParent()+File.separator+"TEXTURES.BIN"), status);
									}
									javafx.application.Platform.runLater(() -> {
										if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Model saved successfully.", ButtonType.OK).show();
										status.text.setText(null);
										status.progress.setProgress(0);
									});
									
								} else if (fc.getSelectedExtensionFilter() == TYPE_OBJ) {
									javafx.application.Platform.runLater(() -> {
										status.text.setText("Saving Wavefront OBJ...");
										status.progress.setProgress(-1);
									});
									WavefrontOBJ.save(mainGeometry, f.getAbsolutePath());
									writeConfigAndTextures(f);
									
								} else if (fc.getSelectedExtensionFilter() == TYPE_Z3D) {
									javafx.application.Platform.runLater(() -> {
										status.text.setText("Saving ZModeler2 scene...");
										status.progress.setProgress(-1);
									});
									ZModelerZ3D.save(mainGeometry, f.getAbsolutePath());
									writeConfigAndTextures(f);
								} else if (fc.getSelectedExtensionFilter() == TYPE_GLTF) {
									javafx.application.Platform.runLater(() -> {
										status.text.setText("Saving glTF...");
										status.progress.setProgress(-1);
									});
									GLTF.save(mainGeometry, f.getAbsolutePath(), false);
									writeConfigAndTextures(f);
								} 
//								else if (fc.getSelectedExtensionFilter() == TYPE_GLB) {
//									javafx.application.Platform.runLater(() -> {
//										status.text.setText("Saving glTF Binary...");
//										status.progress.setProgress(-1);
//									});
//									GLTF.save(mainGeometry, f.getAbsolutePath(), true);
//									writeConfigAndTextures(f);
//								}
							} catch (Exception e2) {
								e2.printStackTrace();
								javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error saving model!\n"+e2.getMessage()).show());
							}
		                }

						private void writeConfigAndTextures(@SuppressWarnings("hiding") File f) throws IOException, Exception {
							javafx.application.Platform.runLater(() -> {
								status.text.setText("Writing configuration file...");
								status.progress.setProgress(-1);
							});
							mainGeometry.writeConfig(new File(f.getParent() + File.separator + f.getName().replace(f.getName().split("\\.")[f.getName().split("\\.").length-1], "ini")));
							if (importExportTextures.isSelected() && loadTextures.isSelected() && mainTPK != null) {
								javafx.application.Platform.runLater(() -> {
									status.text.setText("Writing textures...");
									status.progress.setProgress(-1);
								});
								mainTPK.exportToFolder(f.getParent(), mainGeometry.carname);
							}
							
							javafx.application.Platform.runLater(() -> {
								if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Model exported successfully.", ButtonType.OK).show();
								status.text.setText(null);
								status.progress.setProgress(0);
							});
						}
		            }.start();
					
					
		    		e.consume();        
				}
			} catch (Exception e1) {
				e1.printStackTrace();
            	new Alert(Alert.AlertType.ERROR, "Error saving model!\n"+e1.getMessage()).show();
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
	
	public static EventHandler<ActionEvent> handleConvert() {
		return evt -> {
        	FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastDirectoryLoaded).isDirectory() ? new File(lastDirectoryLoaded) : new File("."));
			fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("CTK configuration", "*.txt"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
			fc.setTitle("Select a config to convert");
			File selected = fc.showOpenDialog(null);
			if (selected != null) {
				try {
					TextInputDialog td = new TextInputDialog();
					td.setHeaderText("Please enter the car model's name");
					td.getEditor().setPromptText("AAA_AAA_AAA_01");
					td.getEditor().setText(null);
					String s = td.showAndWait().get().strip();
					if (s == null || s.isBlank()) throw new Exception("Please indicate a name!");
					Geometry.ctkConfigToUCGTConfig(selected, new File(selected.getAbsolutePath().replace(".txt", ".ini")), s);
					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Config converted successfully. Please check the settings before using it!", ButtonType.OK).show();
				} catch (Exception e) {
					new Alert(Alert.AlertType.ERROR, "Error converting CTK config:\n"+e.getMessage(), ButtonType.OK).show();
					e.printStackTrace();
		        	System.gc();
				}
			}
        	System.gc();
        };
	}

	public static EventHandler<ActionEvent> handleChangeComp() {
		return evt -> {
        	FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastDirectoryLoaded).isDirectory() ? new File(lastDirectoryLoaded) : new File("."));
			fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("Black Box NFS Geometry/Texture File", "*.bin"));
			fc.setTitle("Select a file");
			File selected = fc.showOpenDialog(null);
			if (selected != null) {
				try {
					var type = new ComboBox<CompressionType>();
					var level = new ComboBox<CompressionLevel>();
					type.getItems().addAll(CompressionType.RawDecompressed, CompressionType.RefPack, CompressionType.JDLZ);
					level.getItems().addAll(CompressionLevel.values());
					type.getSelectionModel().select(0);
					level.getSelectionModel().select(0);
					var vb = new VBox();
					var valid = new Button("OK");
					Label labLevel = new Label("New compression level");
					vb.setAlignment(Pos.CENTER);
					vb.setFillWidth(true);
					vb.setSpacing(5);
					vb.getChildren().addAll(new Label("New compression type"),type,valid);

		        	Stage st = new Stage();
					openWindows.add(st);
					st.setOnCloseRequest(ev -> openWindows.remove(st));
					st.setTitle("Select new compression");
					Scene sc = new Scene(vb);
					if (useDarkMode.isSelected()) sc.getRoot().setStyle("-fx-base:black");
					st.setScene(sc);
					st.setHeight(200);
					st.setWidth(200);
					st.show();
					
					type.setOnAction(e -> {
						if (type.getValue() == CompressionType.RefPack && !vb.getChildren().contains(labLevel)) {
							vb.getChildren().add(2, labLevel);
							vb.getChildren().add(3, level);
						}
						else if (vb.getChildren().contains(labLevel)) vb.getChildren().removeAll(labLevel, level);
					});
					valid.setOnAction(button -> {
						openWindows.remove(st);
						st.close();
						new Thread(() -> {
								try {
									Block.doNotRead.put(BlockType.Part_MPoints, true);
									Block.doNotRead.put(BlockType.Part_Mesh, true);
									Block.doNotRead.put(BlockType.Part_HashList, true);
									Block.doNotRead.put(BlockType.Part_HashAssign, true);
									Block.doNotRead.put(BlockType.Part_Padding, true);

									javafx.application.Platform.runLater(() -> {
										status.text.setText("Loading file...");
										status.progress.setProgress(-1);
									});

									FileInputStream fis = new FileInputStream(selected);
									byte [] arr = new byte[(int)selected.length()-4];
									fis.read(arr);
									fis.close();
									var bb = ByteBuffer.wrap(arr);
									bb.order(ByteOrder.LITTLE_ENDIAN);
									var block = Block.read(bb);

									switch(block.getBlockID()) {
									case Geometry:
										Geometry geom = (Geometry)block;
										
										geom.defaultCompressionType = type.getValue();
										geom.defaultCompressionLevel = level.getValue();
										geom.SAVE_useOffsetsTable = true;
										geom.SAVE_removeUselessAutosculptParts = false;
										geom.SAVE_optimizeMaterials = false;
										geom.SAVE_sortEverythingByName = true;
										geom.SAVE_fixAutosculptNormals = false;
										geom.SAVE_removeInvalid = false;
										geom.SAVE_copyMissingLODs = false;
										geom.SAVE_copyLOD_D = false;
										geom.SAVE_protectModel = false;
										geom.SAVE_processParts = false;
										
										try {
											geom.save(selected, status);
											javafx.application.Platform.runLater(() -> {
												new Alert(Alert.AlertType.INFORMATION, "Compression changed successfully!", ButtonType.OK).show();
												status.text.setText(null);
												status.progress.setProgress(0);
											});
											
										} catch (Exception e) {
											javafx.application.Platform.runLater(() -> {
												new Alert(Alert.AlertType.ERROR, "Error saving Geometry:\n"+e.getMessage(), ButtonType.OK).show();
												status.text.setText(null);
												status.progress.setProgress(0);
											});
											e.printStackTrace();
										}
										

										break;
									case TPK:
										TPK tpk = (TPK)block;
										tpk.defaultCompressionType = type.getValue();
										tpk.defaultCompressionLevel = level.getValue();

										try {
											tpk.save(selected, status);
											javafx.application.Platform.runLater(() -> {
												new Alert(Alert.AlertType.INFORMATION, "Compression changed successfully!", ButtonType.OK).show();
												status.text.setText(null);
												status.progress.setProgress(0);
											});
											
										} catch (Exception e) {
											javafx.application.Platform.runLater(() -> {
												new Alert(Alert.AlertType.ERROR, "Error saving TPK:\n"+e.getMessage(), ButtonType.OK).show();
												status.text.setText(null);
												status.progress.setProgress(0);
											});
											e.printStackTrace();
										}
										
										break;
									default:
										javafx.application.Platform.runLater(() -> {
											new Alert(Alert.AlertType.WARNING, "No suitable data found!", ButtonType.OK).show();
											status.text.setText(null);
											status.progress.setProgress(0);
										});
									}
									Block.doNotRead.clear();
																		
								} catch (Exception e) {
									javafx.application.Platform.runLater(() -> {
										new Alert(Alert.AlertType.ERROR, "Error loading file:\n"+e.getMessage(), ButtonType.OK).show();
										status.text.setText(null);
										status.progress.setProgress(0);
									});
									e.printStackTrace();
								}
							}
						).start();
					});
					
					
					
				} catch (Exception e) {
					new Alert(Alert.AlertType.ERROR, "Error changing compression:\n"+e.getMessage(), ButtonType.OK).show();
					e.printStackTrace();
		        	System.gc();
				}
			}
        	System.gc();
        };
	}
	
	public static void loadByFileType(Stage primaryStage, File selected, boolean showSetup) throws Exception {
		new Thread() {
			@Override
			public void run() {
				try {
					unsavedChanges = false;
					javafx.application.Platform.runLater(() -> {
						status.text.setText("Loading file...");
						status.progress.setProgress(-1);
					});
					
				if (selected.getName().toLowerCase().equals("geometry.bin")) {							
					loadGeometry(selected);
					mainTPK = null;
					if (loadTextures.isSelected()) loadTPK(new File(selected.getAbsolutePath().replace(selected.getName(), "TEXTURES.BIN")));
					lastFileLoaded = selected.getName();
					lastDirectoryLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
					postLoadUpdate(primaryStage);
//					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Model loaded successfully.", ButtonType.OK).show();

				} else {
					//try guess signature
					
					FileInputStream fis = new FileInputStream(selected);
					int signature = getNextInt(fis);
					fis.close();
					
					if (signature == ZModelerZ3D.ZM2S || signature == ZModelerZ3D.Z3DM) {
						System.out.println("=== Z3D IMPORT ===");

						if (!doConfigStuff(selected, showSetup)) return;

						javafx.application.Platform.runLater(() -> {
							status.text.setText("Converting Z3D...");
							status.progress.setProgress(-1);
						});
						
						ZModelerZ3D.load(mainGeometry, selected);		
						
						doTPKStuffAndPostLoadUpdate(primaryStage, selected);		
						
					} else if (signature == WavefrontOBJ.OBJ_blender || signature == WavefrontOBJ.OBJ_UCGT || selected.getName().toLowerCase().endsWith(".obj")) {
						System.out.println("=== OBJ IMPORT ===");

						if (!doConfigStuff(selected, showSetup)) return;
						
						javafx.application.Platform.runLater(() -> {
							status.text.setText("Converting OBJ...");
							status.progress.setProgress(-1);
						});

						WavefrontOBJ.load(mainGeometry, selected);	

						doTPKStuffAndPostLoadUpdate(primaryStage, selected);
						
					}else if (signature == GLTF.glTF_blender || signature == GLTF.glTF_BINARY || selected.getName().toLowerCase().endsWith(".gltf")) {
						System.out.println("=== glTF IMPORT ===");

						if (!doConfigStuff(selected, showSetup)) return;
						
						javafx.application.Platform.runLater(() -> {
							status.text.setText("Converting glTF...");
							status.progress.setProgress(-1);
						});

						GLTF.load(mainGeometry, selected);	

						doTPKStuffAndPostLoadUpdate(primaryStage, selected);

					} else
//					if (selected.getName().toLowerCase().endsWith(".bin") || selected.getName().toLowerCase().endsWith(".bun")) 
						//try to read a BUN bundle in all other cases
					{
						fis = new FileInputStream(selected);
						int inPos = 0;
						
						javafx.application.Platform.runLater(() -> {
							status.text.setText("Reading bundle file...");
							status.progress.setProgress(-1);
						});

						try {
						
							var geomOffsets = FXCollections.<FileOffset>observableArrayList();
							var tpkOffsets = FXCollections.<FileOffset>observableArrayList();
							while (fis.available()>0) {
								var bType = BlockType.get(getNextInt(fis));
								var bSize = getNextInt(fis);
//												System.out.println(bType + " " + bSize);
								if (bType.equals(BlockType.Geometry)) {
									getNextInt(fis);
									var paddingLength = getNextInt(fis);
									fis.skipNBytes(paddingLength);
									getNextInt(fis); //BlockType.GeomHeader
									var headerLength = getNextInt(fis);
									
									var headerData = ByteBuffer.allocate(headerLength+4);
									headerData.order(ByteOrder.LITTLE_ENDIAN);
									headerData.putInt(0, headerLength);
									fis.read(headerData.array(), 4, headerLength);
									
									fis.skipNBytes(bSize-headerLength-paddingLength-16);
									
									var header = new GeomHeader(headerData);
//													System.out.println(header.geomInfo.filename);
									
									geomOffsets.add(new FileOffset(inPos,bSize,header.geomInfo.blockname,header.geomInfo.filename));
								} else if (bType.equals(BlockType.TPK)){
									getNextInt(fis);
									var paddingLength = getNextInt(fis);
									fis.skipNBytes(paddingLength);
									getNextInt(fis); //BlockType.GeomHeader
									var headerLength = getNextInt(fis);
									
									var headerData = ByteBuffer.allocate(headerLength+4);
									headerData.order(ByteOrder.LITTLE_ENDIAN);
									headerData.putInt(0, headerLength);
									fis.read(headerData.array(), 4, headerLength);
									
									fis.skipNBytes(bSize-headerLength-paddingLength-16);
									
									var header = new TPKHeader(headerData);
									
									tpkOffsets.add(new FileOffset(inPos,bSize,header.info.blockname,header.info.filename));
									
								} else {
									fis.skipNBytes(bSize);
								}
								inPos+=bSize+8;
							}
							fis.close();

							if (geomOffsets.size() == 1 && geomOffsets.get(0).offset == 0) {
								loadGeometry(selected);
								lastFileLoaded = selected.getName();
								lastDirectoryLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
								postLoadUpdate(primaryStage);
//								if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Model loaded successfully.", ButtonType.OK).show();
								return;

							}

							javafx.application.Platform.runLater(() -> {
								status.text.setText("Waiting on user input...");
								status.progress.setProgress(-1);
							});

							javafx.application.Platform.runLater(() -> {

								Stage st = new Stage();
								st.setTitle("Select files in "+selected.getName());
								openWindows.add(st);
								var sp = new SplitPane();
								var geom = new SearchableListView<>(geomOffsets);
								var tpk = new SearchableListView<>(tpkOffsets);
								if (geomOffsets.size()>0) sp.getItems().add(geom);
								sp.setOrientation(Orientation.VERTICAL);
								if (tpkOffsets.size()>0) sp.getItems().add(tpk);
								Scene sc = new Scene(sp);
								st.setHeight(600);
								st.setWidth(400);
								if (useDarkMode.isSelected()) sc.getRoot().setStyle("-fx-base:black");
								st.setScene(sc);
								st.setOnCloseRequest(e -> {
									openWindows.remove(st);
									javafx.application.Platform.runLater(() -> {
										status.text.setText(null);
										status.progress.setProgress(0);
									});
								});
								geom.list.setOnMouseClicked(e -> {
									if (e.getClickCount() >1) {
//										st.close();
										loadBundleGeometry(primaryStage, selected, geom);
									}
								});
								geom.list.setOnKeyPressed(e -> {
									if (e.getCode() == KeyCode.ENTER)
										loadBundleGeometry(primaryStage, selected, geom);
								});
								
								tpk.list.setOnMouseClicked(e -> {
									if (e.getClickCount() >1) {
//										st.close();
										loadBundleTPK(primaryStage, selected, tpk);	
									}
								});
								tpk.list.setOnKeyPressed(e -> {
									if (e.getCode() == KeyCode.ENTER)
										loadBundleTPK(primaryStage, selected, tpk);
								});
								st.show();
							});
							
							
							
						
						}catch (Exception e) {
							e.printStackTrace();
							javafx.application.Platform.runLater(() -> {
								new Alert(Alert.AlertType.ERROR, "Error loading the bundle:\n"+e.getMessage(), ButtonType.OK).show();
								status.text.setText(null);
								status.progress.setProgress(0);
							});
							fis.close();
						}
						
					}
					
				}
				} catch (Error | Exception e) {
					e.printStackTrace();
					javafx.application.Platform.runLater(() -> {
						new Alert(Alert.AlertType.ERROR, "Error loading the model:\n"+e.getMessage(), ButtonType.OK).show();
						status.text.setText(null);
						status.progress.setProgress(0);
					});
				}
			}

			@SuppressWarnings("hiding")
			private void doTPKStuffAndPostLoadUpdate(Stage primaryStage, File selected) throws InterruptedException {
				mainTPK = null;
				if (importExportTextures.isSelected()) {
					javafx.application.Platform.runLater(() -> {
						status.text.setText("Creating textures pack...");
						status.progress.setProgress(-1);
					});
					try {
						mainTPK = new TPK(selected.getParentFile(), mainGeometry);
						if (mainTPK.textures.size() == 0) mainTPK = null;
					} catch (Exception e) {
						javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error creating the texture pack:\n"+e.getMessage(), ButtonType.OK).show());
					}
				}
				
				lastFileLoaded = selected.getName();
				lastDirectoryLoaded = selected.getAbsolutePath().replace(selected.getName(), "");

				postLoadUpdate(primaryStage);
			}

			@SuppressWarnings("hiding")
			private boolean doConfigStuff(File selected, boolean showSetup) throws Exception, IOException {
				var valid = true;
				javafx.application.Platform.runLater(() -> {
					status.text.setText("Reading configuration file...");
					status.progress.setProgress(-1);
				});

				if (!new File(selected.getParent()+File.separator+selected.getName().replace(selected.getName().split("\\.")[selected.getName().split("\\.").length-1], "ini")).isFile())
					throw new Exception("Missing configuration file:\n" + selected.getParent()+File.separator+selected.getName().replace(selected.getName().split("\\.")[selected.getName().split("\\.").length-1], "ini"));

				var oldGeom = mainGeometry;
				mainGeometry = new Geometry();
				mainGeometry.readConfig(new File(selected.getParent()+File.separator+selected.getName().replace(selected.getName().split("\\.")[selected.getName().split("\\.").length-1], "ini")));

				if (showSetup) valid = showGeometrySettings(false);
				if (!valid) {
					mainGeometry = oldGeom;
					javafx.application.Platform.runLater(() -> {
						status.text.setText(null);
						status.progress.setProgress(0);
					});
				}
				return valid;
			}

			@SuppressWarnings("hiding")
			public void loadBundleTPK(Stage primaryStage, File selected, SearchableListView<FileOffset> tpk) {
				try {
					FileInputStream input = new FileInputStream(selected);
					input.skipNBytes(tpk.list.getSelectionModel().getSelectedItem().offset);
					byte [] arr = new byte[tpk.list.getSelectionModel().getSelectedItem().size+4];
					input.skipNBytes(4); //if this method is called on a file, we assume that the file is a geometry, therefore the first blockid can be skipped
					input.read(arr);
					input.close();
					mainTPK = new TPK(ByteBuffer.wrap(arr));
					postLoadUpdate(primaryStage);
//										if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Model loaded successfully.", ButtonType.OK).show();
					
				} catch (Exception e1) {
					e1.printStackTrace();
					javafx.application.Platform.runLater(() -> {
						new Alert(Alert.AlertType.ERROR, "Error loading the TPK:\n"+e1.getMessage(), ButtonType.OK).show();
						status.text.setText(null);
						status.progress.setProgress(0);
					});
				}
			}

			@SuppressWarnings("hiding")
			public void loadBundleGeometry(Stage primaryStage, File selected, SearchableListView<FileOffset> geom) {
				try {
					FileInputStream input = new FileInputStream(selected);
					input.skipNBytes(geom.list.getSelectionModel().getSelectedItem().offset);
					byte [] arr = new byte[geom.list.getSelectionModel().getSelectedItem().size+4];
					input.skipNBytes(4); //if this method is called on a file, we assume that the file is a geometry, therefore the first blockid can be skipped
					input.read(arr);
					input.close();
					loadGeometry(arr);
					lastFileLoaded = selected.getName();
					lastDirectoryLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
					postLoadUpdate(primaryStage);
//														lastFileLoaded = selected.getName();
//														lastDirectoryLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
//										if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.INFORMATION, "Model loaded successfully.", ButtonType.OK).show();
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();

					javafx.application.Platform.runLater(() -> {
						new Alert(Alert.AlertType.ERROR, "Error loading the model:\n"+e1.getMessage(), ButtonType.OK).show();
						status.text.setText(null);
						status.progress.setProgress(0);
					});
				}
			}
		}.start();
		
		
	}
		
	static boolean nextReturn = false;
	public static boolean showGeometrySettings(boolean restrictOptions) {
		final CountDownLatch latch = new CountDownLatch(1);
		nextReturn = false;
		unsavedChanges = true;
		
		javafx.application.Platform.runLater(() -> {
			if (mainGeometry == null) {
				if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.ERROR, "No Geometry loaded!", ButtonType.OK).show();
				latch.countDown();
				return;
			}
			
			var initialPlatform = mainGeometry.platform;
			var previousCarName = mainGeometry.carname;
			
			var hb = new VBox();
			
			var platform 						= new ConfigSetting.Platform("Platform ", mainGeometry.platform);
			var carname 						= new ConfigSetting.Text("Car name ", mainGeometry.carname); 
			var compType 						= new ConfigSetting.CompType("Compression type ", mainGeometry.defaultCompressionType);
			var compLevel 						= new ConfigSetting.CompLevel("Compression level ", mainGeometry.defaultCompressionLevel);
			var tangents 						= new ConfigSetting.Tangents("Tangents ", mainGeometry.IMPORT_Tangents);
			var vcolImport 						= new ConfigSetting.Boolean("Import vertex colors ", mainGeometry.IMPORT_importVertexColors);
			var vcolCalculate 					= new ConfigSetting.Boolean("Calculate vertex colors ", mainGeometry.IMPORT_calculateVertexColors);
			var copyMissingLods 				= new ConfigSetting.Boolean("Copy missing LODs ", mainGeometry.SAVE_copyMissingLODs);
			var makeLodD						= new ConfigSetting.Boolean("Copy LOD D ", mainGeometry.SAVE_copyLOD_D);
			var removeUselessAutosculptParts 	= new ConfigSetting.Boolean("Remove useless Autosculpt parts ", mainGeometry.SAVE_removeUselessAutosculptParts);
			var checkModel 						= new ConfigSetting.Boolean("Check model (see console output) ", mainGeometry.SAVE_checkModel);
			var fixAutosculptNormals 			= new ConfigSetting.Boolean("Attempt to fix Autosculpt normals ", mainGeometry.SAVE_fixAutosculptNormals);
			var removeInvalid 					= new ConfigSetting.Boolean("Remove invalid parts ", mainGeometry.SAVE_removeInvalid);
			
			
			// TODO continue...
			var show = new CheckBox("Show this menu on file reload");		
			var ok = new Button("OK");
			ok.setCenterShape(true);
			show.setSelected(showImportSettingsOnReload.isSelected());
			
			if (restrictOptions) //when a geom is already loaded
				hb.getChildren().addAll(platform,
						carname, 
						compType, compLevel, 
//						tangents, vcolImport, vcolCalculate, 
//						copyMissingLods, makeLodD, 
//						removeUselessAutosculptParts,
//						checkModel,
//						fixAutosculptNormals,
//						removeInvalid,
						
						show, ok);
			else  //when a geom is being imported
				hb.getChildren().addAll(platform,
					carname, 
					compType, compLevel, 
					tangents, vcolImport, vcolCalculate, 
					copyMissingLods, makeLodD, 
					removeUselessAutosculptParts,
					checkModel,
					fixAutosculptNormals,
					removeInvalid,
					
					show, ok);
			
			hb.setAlignment(Pos.CENTER);

			if (mainGeometry.defaultCompressionType != CompressionType.RefPack) hb.getChildren().remove(compLevel);
			
			//carname not bound or bugging out
			compType.setting.valueProperty().addListener((obs, o, n) -> {
				mainGeometry.defaultCompressionType = n;
				if (n == CompressionType.RefPack && !hb.getChildren().contains(compLevel)) {
					hb.getChildren().add(3, compLevel);
				} else if (hb.getChildren().contains(compLevel)){
					hb.getChildren().remove(compLevel);
				}
			});
			compLevel.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.defaultCompressionLevel = n);
			tangents.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.IMPORT_Tangents = n);
			vcolImport.setting.valueProperty().addListener((obs, o, n) -> {
				mainGeometry.IMPORT_importVertexColors = n;
				if (n) vcolCalculate.setting.setValue(false);
			});
			vcolCalculate.setting.valueProperty().addListener((obs, o, n) -> {
				mainGeometry.IMPORT_calculateVertexColors = n;
				if (n) vcolImport.setting.setValue(false);
			});
			copyMissingLods.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.SAVE_copyMissingLODs = n);
			makeLodD.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.SAVE_copyLOD_D = n);
			removeUselessAutosculptParts.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.SAVE_removeUselessAutosculptParts = n);
			checkModel.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.SAVE_checkModel = n);
			fixAutosculptNormals.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.SAVE_fixAutosculptNormals = n);
			removeInvalid.setting.valueProperty().addListener((obs, o, n) -> mainGeometry.SAVE_removeInvalid = n);

			show.selectedProperty().addListener((obs, o, n) -> showImportSettingsOnReload.setSelected(n));
			
			
			Stage st = new Stage();
			openWindows.add(st);
			st.setTitle("Geometry settings");
			Scene sc = new Scene(hb);
			if (useDarkMode.isSelected()) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.show();
			
			ok.setOnAction(e -> {
				if (platform.setting.getValue() != initialPlatform) changePlatform(platform.setting.getValue());
				if (!previousCarName.equals(carname.setting.getText())) changeCarName(mainGeometry.carname, carname.setting.getText().strip());
				nextReturn = true;
				st.close();

				latch.countDown();
				openWindows.remove(st);
			});
			st.setOnCloseRequest(e -> {
				latch.countDown();
				openWindows.remove(st);
			});
		});
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return nextReturn;
		
	}

	public static int getNextInt(FileInputStream fis) throws IOException {
		return fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
	}
	
	public static void loadGeometry(File input) throws Exception {
		FileInputStream fis = new FileInputStream(input);
		byte [] arr = new byte[(int)input.length()-4];
		fis.skipNBytes(4); //if this method is called on a file, we assume that the file is a geometry, therefore the first blockid can be skipped
		fis.read(arr);
		fis.close();
		loadGeometry(arr);
	}
		
	public static void loadTPK(File input) throws Exception {
		if (!input.isFile()) return;
		if (input.length() == 0) return;
		javafx.application.Platform.runLater(() -> {
			status.text.setText("Loading textures...");
			status.progress.setProgress(-1);
		});
		FileInputStream fis = new FileInputStream(input);
		byte [] arr = new byte[(int)input.length()-4];
		fis.skipNBytes(4); //if this method is called on a file, we assume that the file is a geometry, therefore the first blockid can be skipped
		fis.read(arr);
		fis.close();
		mainTPK = new TPK(ByteBuffer.wrap(arr));
	}
	
	public static void loadGeometry(byte[] arr) throws Exception {
		javafx.application.Platform.runLater(() -> {
			status.text.setText("Loading geometry...");
			status.progress.setProgress(-1);
		});
		mainGeometry = new Geometry(ByteBuffer.wrap(arr));
	}


	public static void postLoadUpdate(Stage primaryStage) throws InterruptedException {
		javafx.application.Platform.runLater(() -> {
			primaryStage.setTitle(mainGeometry == null ? programName : programName + " - " + mainGeometry.carname);
			partsDisplay.filter.setText("");

			tpkImagesList.getItems().clear();
			if (mainTPK != null && mainTPK.header!=null) {
				tpkImagesList.getItems().addAll(mainTPK.textures);
				if (mainTPK.textures.size() != mainTPK.header.texlist.textures.size()) {
					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Could not load all textures!", ButtonType.OK).show();
				}
			}
			
			if (mainGeometry != null && !mainGeometry.isImported)
				if (mainGeometry.parts.size() != mainGeometry.geomHeader.partsList.partKeys.size()) {
					if (!disableWarnings.isSelected()) new Alert(Alert.AlertType.WARNING, "Could not load all parts!", ButtonType.OK).show();
			}

//			viewport.resetCamera();
		});
		
		javafx.application.Platform.runLater(() -> {
			status.text.setText("Preparing materials display...");
			status.progress.setProgress(-1);
		});
		
		visualModelMaterials = new ArrayList<>();

		partsRoot.getChildren().clear();
		parts.clear();
		if (mainGeometry == null) partsRoot.setValue(null);
		else {
			partsRoot.setValue(new PartController(mainGeometry.carname));
		
			mainGeometry.materials.forEach(m -> visualModelMaterials.add(null));
	
			textureLookupFolders.add(lastDirectoryLoaded);
			
			ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			for (final var m :mainGeometry.materials){
			    pool.execute(() -> {
			    	
			    	for (var t : textureLookupFolders) { //unhash textures
			    		if (new File(t).list() != null) for (var f : new File(t).list()) {
			    			if (f.endsWith(".dds")) Hash.findBIN(f.replace(".dds", ""));
			    		}
			    	}
			    	
			    	PhongMaterial mat = new PhongMaterial();
					visualModelMaterials.set(mainGeometry.materials.indexOf(m), mat);
	
					if (loadTextures.isSelected()) {
//						if (mainGeometry.platform == Platform.PC || mainGeometry.platform == Platform.X360) {
							//diffuse color
							//diffuse: 
								for (int i=0; i< m.textureUsages.size(); i++) {
								if (m.textureUsages.get(i).getKey() == TextureUsage.DIFFUSE.getKey() || m.textureUsages.get(i).getKey() == TextureUsage.ROADBASE.getKey()
										|| ((m.shaderUsage == ShaderUsage.get("car_nm_v_s") || m.shaderUsage == ShaderUsage.get("car_t_nm") || m.shaderUsage.getClass() == ShaderUsage.Legacy.class) 
												&& m.textureUsages.get(i).getKey() == TextureUsage.AMBIENT.getKey())) {
			
									try {
										//fixes TPK priority since break statement is removed (for skin1)
										for (var s : textureLookupFolders) {
											var f = new File(s+Hash.getBIN(m.TextureHashes.get(i))+ ".dds");
											if (!f.exists()) f = new File(s+Hash.getBIN(m.TextureHashes.get(i)).replace(mainGeometry.carname, "%")+ ".dds");
											if (f.exists()) {
												BufferedImage image;
													var r = new DDSImageReader(null);
													var iis = new FileImageInputStream(f);
													r.setInput(iis);
													if (! (//m.shaderUsage == ShaderUsage.get("car_a") ||
															//m.shaderUsage == ShaderUsage.get("car_a_nzw") ||
															//m.shaderUsage == ShaderUsage.get("car_nm_a") ||
															//m.shaderUsage == ShaderUsage.get("car_nm_v_s_a") ||
															//m.shaderUsage == ShaderUsage.get("car_si_a") ||
															//m.shaderUsage == ShaderUsage.get("car_t_a") ||
															//m.shaderUsage.getClass() == ShaderUsage.Legacy.class ||
															m.shaderUsage.name.contains("Alpha") ||
															m.shaderUsage.name.contains("opacity") ||
															m.shaderUsage.name.contains("org_") ||
															m.shaderUsage.name.contains("overlay"))) {
														r.ignoreAlpha = true;										
													}
													image = r.read(0);
													
													mat.setDiffuseMap(SwingFXUtils.toFXImage(image, null));
													iis.close();
	//												break diffuse;
											}
										}
	//								break diffuse;
									
										if (mainTPK != null) for (var t : mainTPK.textures) if (t.binKey == m.TextureHashes.get(i)) {
											BufferedImage image;
											var r = new DDSImageReader(null);
											var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(t.DDSImage));
											r.setInput(iis);
											if (! (m.shaderUsage == ShaderUsage.get("car_a") ||
													m.shaderUsage == ShaderUsage.get("car_a_nzw") ||
													m.shaderUsage == ShaderUsage.get("car_nm_a") ||
													m.shaderUsage == ShaderUsage.get("car_nm_v_s_a") ||
													m.shaderUsage == ShaderUsage.get("car_si_a") ||
													m.shaderUsage == ShaderUsage.get("car_t_a") ||
													m.shaderUsage.getClass() == ShaderUsage.Legacy.class)) {
												r.ignoreAlpha = true;										
											}
											image = r.read(0);
											
											mat.setDiffuseMap(SwingFXUtils.toFXImage(image, null));
											iis.close();
	//										break diffuse;
										}

									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}//diffuse color search
							
							if (mat.getDiffuseMap() == null) {
								if (Hash.getBIN(m.ShaderHash).contains("WINDSHIELD") || Hash.getBIN(m.ShaderHash).contains("GLASS")) {
									mat.setDiffuseColor(Color.color(0.2, 0.2, 0.2, 0.3));
								} else {
									double colorR = Math.random();
									double colorG = Math.random();
									double colorB = Math.random();				
									mat.setDiffuseColor(Color.color(colorR, colorG, colorB, 1));				
								}
							}
							
							
							//bump map
							bump: for (int i=0; i< m.textureUsages.size(); i++) {
								if (!(m.shaderUsage == ShaderUsage.get("car_nm_v_s") || m.shaderUsage == ShaderUsage.get("car_t_nm") || m.TextureHashes.get(i) == Hash.findBIN("DAMAGE_N"))) 
									if (m.textureUsages.get(i).getKey() == TextureUsage.NORMALMAP.getKey() 
									|| m.textureUsages.get(i).getKey() == TextureUsage.NORMAL_SAMPLER.getKey() 
									|| m.textureUsages.get(i).getKey() == TextureUsage.BASENORMAL.getKey()) {
			
									try {
										if (mainTPK != null) for (var t : mainTPK.textures) if (t.binKey == m.TextureHashes.get(i)) {
											BufferedImage image;
											var r = new DDSImageReader(null);
											var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(t.DDSImage));
											r.setInput(iis);
											image = r.read(0);
											
											mat.setBumpMap(SwingFXUtils.toFXImage(image, null));
											iis.close();
											break bump;
										}
										for (var s : textureLookupFolders) {
											var f = new File(s+Hash.getBIN(m.TextureHashes.get(i))+ ".dds");
											if (!f.exists()) f = new File(s+Hash.getBIN(m.TextureHashes.get(i)).replace(mainGeometry.carname, "%")+ ".dds");
											if (f.exists()) {
												BufferedImage image;
													var r = new DDSImageReader(null);
													var iis = new FileImageInputStream(f);
													r.setInput(iis);
													image = r.read(0);
													
													mat.setBumpMap(SwingFXUtils.toFXImage(image, null));
													iis.close();
													break bump;
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									break bump;
								}
							}//bump map
							
							//selfillumination map
							si: for (int i=0; i< m.textureUsages.size(); i++) {
								if (m.textureUsages.get(i).getKey() == TextureUsage.SELFILLUMINATION.getKey() 
										|| m.textureUsages.get(i).getKey() == TextureUsage.ILLUMINATE.getKey()) {
			
									try {
										if (mainTPK != null) for (var t : mainTPK.textures) if (t.binKey == m.TextureHashes.get(i)) {
											BufferedImage image;
											var r = new DDSImageReader(null);
											var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(t.DDSImage));
											r.setInput(iis);
											image = r.read(0);
											
											mat.setSelfIlluminationMap(SwingFXUtils.toFXImage(image, null));
											iis.close();
											break si;
										}
										
										for (var s : textureLookupFolders) {
											var f = new File(s+Hash.getBIN(m.TextureHashes.get(i))+ ".dds");
											if (!f.exists()) f = new File(s+Hash.getBIN(m.TextureHashes.get(i)).replace(mainGeometry.carname, "%")+ ".dds");
											if (f.exists()) {
												BufferedImage image;
													var r = new DDSImageReader(null);
													var iis = new FileImageInputStream(f);
													r.setInput(iis);
													image = r.read(0);
													
													mat.setSelfIlluminationMap(SwingFXUtils.toFXImage(image, null));
													iis.close();
													break si;
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									break si;
								}
							}//bump map
							
							//specular
							spec: for (int i=0; i< m.textureUsages.size(); i++) {
								if (m.textureUsages.get(i) == TextureUsage.SPECULAR || m.textureUsages.get(i) == TextureUsage.BASESPEC) {
			
									try {
										if (mainTPK != null) for (var t : mainTPK.textures) if (t.binKey == m.TextureHashes.get(i)) {
											BufferedImage image;
											var r = new DDSImageReader(null);
											var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(t.DDSImage));
											r.setInput(iis);
											image = r.read(0);
											
											mat.setSpecularMap(SwingFXUtils.toFXImage(image, null));
											iis.close();
											break spec;
										}
										for (var s : textureLookupFolders) {
											var f = new File(s+Hash.getBIN(m.TextureHashes.get(i))+ ".dds");
											if (!f.exists()) f = new File(s+Hash.getBIN(m.TextureHashes.get(i)).replace(mainGeometry.carname, "%")+ ".dds");
											if (f.exists()) {
												BufferedImage image;
													var r = new DDSImageReader(null);
													var iis = new FileImageInputStream(f);
													r.setInput(iis);
													image = r.read(0);
													
													mat.setSpecularMap(SwingFXUtils.toFXImage(image, null));
													iis.close();
													break spec;
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									break spec;
								}
							}//specular
//						} else {
//							//legacy material display
//							
//							boolean ok = false;
//							try {							
//								var hash = m.TextureHashes.get(0);
//								if (mainTPK != null) for (var t : mainTPK.textures) if (t.binKey == hash) {
//									BufferedImage image;
//									var r = new DDSImageReader(null);
//									var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(t.DDSImage));
//									r.setInput(iis);
//									image = r.read(0);
//									
//									mat.setSelfIlluminationMap(SwingFXUtils.toFXImage(image, null));
//									iis.close();
//									ok = true;
//								}
//								
//								if (!ok) for (var s : textureLookupFolders) {
//									var f = new File(s+Hash.getBIN(hash)+ ".dds");
//									if (!f.exists()) f = new File(s+Hash.getBIN(hash).replace(mainGeometry.carname, "%")+ ".dds");
//									if (f.exists()) {
//										BufferedImage image;
//											var r = new DDSImageReader(null);
//											var iis = new FileImageInputStream(f);
//											r.setInput(iis);
//											if (! (m.shaderUsage == ShaderUsage.get("car_a") ||
//													m.shaderUsage == ShaderUsage.get("car_a_nzw") ||
//													m.shaderUsage == ShaderUsage.get("car_nm_a") ||
//													m.shaderUsage == ShaderUsage.get("car_nm_v_s_a") ||
//													m.shaderUsage == ShaderUsage.get("car_si_a") ||
//													m.shaderUsage == ShaderUsage.get("car_t_a"))) {
//												r.ignoreAlpha = true;										
//											}
//											image = r.read(0);
//											
//											mat.setDiffuseMap(SwingFXUtils.toFXImage(image, null));
//											ok = true;
//											iis.close();
//									}
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//							
//							if (!ok) if (Hash.getBIN(m.ShaderHash).contains("WINDSHIELD") || Hash.getBIN(m.ShaderHash).contains("GLASS")) {
//								mat.setDiffuseColor(Color.color(0.2, 0.2, 0.2, 0.3));
//							} else {
//								double colorR = Math.random();
//								double colorG = Math.random();
//								double colorB = Math.random();				
//								mat.setDiffuseColor(Color.color(colorR, colorG, colorB, 1));				
//							}
//							
//							
//						}
					} else {
						if (Hash.getBIN(m.ShaderHash).contains("WINDSHIELD") || Hash.getBIN(m.ShaderHash).contains("GLASS")) {
							mat.setDiffuseColor(Color.color(0.2, 0.2, 0.2, 0.3));
						} else {
							double colorR = Math.random();
							double colorG = Math.random();
							double colorB = Math.random();				
							mat.setDiffuseColor(Color.color(colorR, colorG, colorB, 1));				
						}
					}	
						
				});
			}
			pool.shutdown();
			pool.awaitTermination(10, TimeUnit.MINUTES);
	
			textureLookupFolders.remove(textureLookupFolders.size()-1);
	
			javafx.application.Platform.runLater(() -> {
				status.text.setText("Preparing parts...");
				status.progress.setProgress(-1);
			});
	
			
			for (var p : mainGeometry.parts) {
				var controller = new PartController(p);
				parts.add(controller);
			}
		}
		partsDisplay.updateTreeView();

		javafx.application.Platform.runLater(() -> {
			modelGroup.getChildren().clear();
			displayKit.getItems().clear();
			displayLod.getItems().clear();
			updateAllPartsDisplay();
			displaySelectedKit();
			status.text.setText(null);
			status.progress.setProgress(0);
		});

	}
    
	public static void updateRender() {
//    	viewport.viewportGroup.getChildren().clear();
    	
    	for (var p : parts) p.updateRender();
    	System.gc();
    	
    }
        
    public static void updateAllPartsDisplay() { //updates visual parts but shouldn't
		var selected = partsDisplay.getSelectionModel().getSelectedItem();
		partsRoot.getChildren().clear();
		partsDisplay.getRoot().getChildren().clear();
		partsDisplay.getRoot().setExpanded(true);
    	if (mainGeometry != null) {
        	for (var p : parts) {
        		TreeItem<PartController> t = new TreeItem<>(p);
        		        		
        		if (p.part.kit != null && !p.part.kit.equals("")) {

        			

            		if (!displayKit.getItems().contains(p.part.kit)) {
            			displayKit.getItems().add(p.part.kit);
            		}
            		if (!displayLod.getItems().contains(p.part.lod)) {
            			displayLod.getItems().add(p.part.lod);
            		}
            		
            		if (simplifiedPartsList.isSelected() && p.part.part != null && p.part.part.length()>3) {
        				if ((p.part.lod != null && !p.part.lod.equals(displayLod.getValue())) 
        						|| (p.part.part.charAt(p.part.part.length() -3) == '_' && (p.part.part.charAt(p.part.part.length() -2) == 'T'))) {
        					continue;
        				}
        			}

            		
            		TreeItem<PartController> kit = null;
            		for (var chi : partsRoot.getChildren()) {
            			if (chi.getValue().name != null && chi.getValue().name.equals(p.part.kit)) {
            				kit = chi;
            				break;
            			}
            		}
            		if (kit == null) {
            			kit = new TreeItem<>(new PartController(p.part.kit));
            			partsRoot.getChildren().add(kit);
            		}
            		
            		kit.getChildren().add(t);
        		} else {
        			//world models
        			partsRoot.getChildren().add(t);
        		}
        		
        		var cb = new CheckBox();
        		cb.setSelected(p.display.get());
        		cb.selectedProperty().bindBidirectional(p.display);
        		cb.setOnMouseClicked(e -> {
        			if (cb.isSelected() && !partsDisplay.getSelectionModel().getSelectedItems().contains(t)) {
        				if (!e.isShiftDown() && !e.isControlDown()) partsDisplay.getSelectionModel().clearSelection();
        				partsDisplay.getSelectionModel().select(t);
        			}
        			if (partsDisplay.getSelectionModel().getSelectedItems().contains(t))
        			partsDisplay.getSelectionModel().getSelectedItems().forEach(disp -> {
        				disp.getValue().display.set(cb.isSelected());
        			});
        		});
        		t.setGraphic(cb);
        		
        		if (selected != null && t.getValue() == selected.getValue()) partsDisplay.getSelectionModel().select(t);
        	}
        	if (displayLod.getValue() == null && displayLod.getItems().size()>0) displayLod.getSelectionModel().select(0);
        	if (displayKit.getValue() == null && displayKit.getItems().size()>0) displayKit.getSelectionModel().select(0);
    	}
		partsDisplay.updateTreeView();
    }
    
    public static void displaySelectedKit() {
    	for (var p : parts) {
    		if (p.part.kit == null && p.part.lod == null) {
    			p.display.set(true);
    			continue;
    		}
    		if (p.part.kit != null && p.part.kit.equals(displayKit.getValue()) && p.part.lod != null && p.part.lod.equals(displayLod.getValue())) {
    			
    			if (p.part.part != null && p.part.part.length()>3 && p.part.part.charAt(p.part.part.length() -3) == '_') {
    				if (p.part.part.charAt(p.part.part.length() -2) == 'T') {					
    					//autosculpt
    	    			p.display.set(false);
    					continue;
    				}
    				if (Character.isDigit(p.part.part.charAt(p.part.part.length() -2)) && Character.isDigit(p.part.part.charAt(p.part.part.length() -1))){
    					//exhaust
    					if (p.part.part.charAt(p.part.part.length() -2) == '0' && p.part.part.charAt(p.part.part.length() -1) == '0') {
    		    			p.display.set(true);
    					} else {
    		    			p.display.set(false);
    					}
    					continue;
    				}
    			}
    			p.display.set(true);
    			
    		} else {
    			//uncomment to strictly display kits, comment to display kits with leftover parts from the previous
    			if (strictKitDisplay.isSelected()) p.display.set(false);
    		}
    	}
    	System.gc();
    }
    
    public static void displaySelectedLod() {
		var selected = partsDisplay.getSelectionModel().getSelectedItem();
		partsDisplay.getSelectionModel().clearSelection();
		
    	if (simplifiedPartsList.isSelected()) updateAllPartsDisplay(); 
		
    	for (var p : parts) {
    		if (p.part.lod == null || p.part.lod.equals("")) {
    			p.display.set(true);
    			continue;
    		}
    		
    		if (p.display.get() && !p.part.lod.equals(displayLod.getValue())) {
    			for (var p2 : parts) if (p2.part.part.equals(p.part.part) && p2.part.lod.equals(displayLod.getValue()) && ((p2.part.kit == null && p.part.kit == null) || p2.part.kit.equals(p.part.kit))) {
    				if (selected != null && selected.getValue() == p) {
    					reselect: for (var n : partsRoot.getChildren()) {
    						if (n.getValue().name.equals(p.part.kit)) {
    							for (var n2 : n.getChildren()) {
    	    						if (n2.getValue() == p2) {
    	    	    					partsDisplay.getSelectionModel().select(n2);
    	    	    					break reselect;
    	    						}
    							}
    						}
    						
    						if (n.getValue() == p2) {
    	    					partsDisplay.getSelectionModel().select(n);
    	    					break reselect;
    						}
    					}
    				}
    				p2.display.set(true);
    				break;
    			}
    			p.display.set(false);    			
    		}
    	}
    	System.gc();
    }
}