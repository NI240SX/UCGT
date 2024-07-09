package fr.ni240sx.ucgt.collisionsEditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import fr.ni240sx.ucgt.collisionsEditor.CollisionMesh.CollisionConvexVertice;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
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
	
	public static final String programName = "collisions editor or some shit idk";
	public static final String programVersion = "dev";
	public static final String settingsFile = "colseditor.dat";
	
	public static Group viewportGroup = new Group();
	public static OrbitCameraViewport viewport;
	
	public static void main(String[] args) {
		try {
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
		} catch (@SuppressWarnings("unused") Exception e) {}
		
		try {
			mainCollisions = new Collisions(new File(lastDirectoryLoaded + lastFileLoaded));
		} catch (@SuppressWarnings("unused") Exception e){}
		
		
        launch(args);
        try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(settingsFile)));
			bw.write("DO NOT MANUALLY EDIT THIS FILE\n" 
					+ lastDirectoryLoaded + "\n" 
					+ lastFileLoaded + "\n" 
					+ lastFileSaved + "\n" 
					+ Boolean.toString(useDarkMode) + "\n"
					+ lastGeomRepLoaded + "\n"
					+ Boolean.toString(disableWarnings) + "\n");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Could not save config");
		}
	}
	
    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle(programName + " - " + mainCollisions.carname.label);

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
        MenuItem fileExit = new MenuItem("Exit");
        
        fileLoad.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
		        ButtonType sure = null;
		        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to load new collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure) || disableWarnings) {
					FileChooser fc = new FileChooser();
					fc.setInitialDirectory(new File(lastDirectoryLoaded));
					fc.setInitialFileName(lastFileLoaded);
					fc.getExtensionFilters().addAll(
				        new FileChooser.ExtensionFilter("BIN files", "*.bin"),
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
						updateAllPartsDisplay();
						primaryStage.setTitle(programName + " - " + mainCollisions.carname.label);
//						menuDBMP.setText(mainCollisions.carname.label);
						updateRender();	
						if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Collisions loaded successfully.", ButtonType.OK).show();
					}
				}
			}
        });
        fileLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileNew.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create blank collisions ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				mainCollisions = new Collisions();
				updateAllPartsDisplay();
				primaryStage.setTitle("fire - "+mainCollisions.carname.label);
				updateRender();	
//				menuDBMP.setText(mainCollisions.carname.label);
			}
        });
        fileNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
//        fileGenerateNew.setOnAction(e -> {
////        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create a new DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
////			if (ButtonType.YES.equals(sure)) {
////				
////			}
//        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
//        });
//        fileGenerateNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        fileSave.setOnAction(e -> {
    		FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File(lastDirectoryLoaded));
			fc.setInitialFileName(mainCollisions.carname.label);
			fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("BIN files", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
			fc.setTitle("Save " + mainCollisions.carname.label + "");
			

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
        fileExit.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings)  sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				primaryStage.close();
			}
        });
        
        menuFile.getItems().addAll(fileLoad, fileNew, /*fileGenerateNew,*/ fileSave, fileExit);

        
        Menu menuEdit = new Menu("Edit");
        MenuItem editUndo = new MenuItem("Undo");
        MenuItem editRedo = new MenuItem("Redo");
        
//        editUndo.setOnAction(e -> {
//        	Undo.undo();
//        });
        editUndo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
//        editRedo.setOnAction(e -> {
//        	Undo.redo();
//        });
        editRedo.setAccelerator(KeyCombination.keyCombination("Ctrl+Y"));
        
        menuEdit.getItems().addAll(editUndo, editRedo);
        
        
        Menu menuSettings = new Menu(programName);
        
        CheckMenuItem settingsDark = new CheckMenuItem("Dark mode");
        CheckMenuItem settingsWarnings = new CheckMenuItem("Disable warning pop-ups");
        MenuItem settingsAbout = new MenuItem("About " + programName + "...");
        
        if (disableWarnings) settingsWarnings.setSelected(true);
        settingsWarnings.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                disableWarnings = true;
            } else {
            	disableWarnings= false;
                }
        });
        
        settingsAbout.setOnAction(e -> {
			Stage st = new Stage();
			st.setTitle("About " + programName);
			Scene sc = new Scene(new Label(programName + " version " + programVersion + "\n\n"
					+ "Advanced Collisions editor for NFS Undercover.\n"
					+ "Aims to make the creation of such data less of a hassle for modders.\n"
					+ "This software has been originally created for the mod Undercover Exposed.\n\n"
					+ "Not affiliated with EA, MaxHwoy, nfsu360, etc.\n\n"
					+ "NI240SX 2024 - No rights reserved"));
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
        
        menuSettings.getItems().addAll(settingsDark, settingsWarnings, settingsAbout);
        
        
        menuBar.getMenus().addAll(menuFile, menuEdit, menuSettings);
        
        
        
        //
        // SHORTCUTS BAR
        //
        
        
        
        HBox shortcutsBar = new HBox();
        
        Separator infSep = new Separator();
        HBox.setHgrow(infSep, Priority.ALWAYS);
        
        shortcutsBar.getChildren().addAll(new Separator());

        
        
        
        if(debug) {
//        	Button debugListCheckedParts = new Button("LIST CHECKED PARTS");
//        	Button debugInfoCheckedParts = new Button("INFO ABOUT CHECKED PARTS");
        	Button debugDBMPDisplay = new Button("DISPLAY COLLISIONS");
        	Button debug3D = new Button("3D DEBUG");
        	Slider debugColPlaneCarWidth = new Slider(0, 5, 1);
        	Slider debugColPlaneCarLength = new Slider(0, 15, 2);
            shortcutsBar.getChildren().addAll(debugDBMPDisplay, debug3D, debugColPlaneCarWidth, debugColPlaneCarLength);
        	
//        	debugListCheckedParts.setOnAction(e -> {
//        		System.out.println("[DEBUG] CHECKED PARTS :");
//                for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
//                	System.out.println(p.displayName);
//                }
//        		e.consume();
//        	});
//        	debugInfoCheckedParts.setOnAction(e -> {
//        		for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
//                	System.out.println("- "+p.displayName);
//                	for (Attribute a:p.attributes) {
//                		System.out.println(a);
//                	}
//                }
//        		e.consume();
//        	});
        	debugDBMPDisplay.setOnAction(e->{
        		System.out.println(mainCollisions+"\n");
        		e.consume();
        	});
        	debug3D.setOnAction(e->{
        		System.out.println("Viewport :\nRotation X : "+viewport.rotationX+"\nRotation Y : "+viewport.rotationY+"\nRotation Z : "+viewport.rotationZ);
        		e.consume();
        	});
        	debugColPlaneCarWidth.valueProperty().addListener(i -> {
        		CollisionConvexVertice.carHalfWidth = (float) debugColPlaneCarWidth.getValue();
        		updateRender();
        	});
        	debugColPlaneCarLength.valueProperty().addListener(i -> {
        		CollisionConvexVertice.carHalfLength = (float) debugColPlaneCarLength.getValue();
        		updateRender();
        	});

        }
        
        windowTop.getChildren().addAll(menuBar, shortcutsBar);
        
        

        viewport = new OrbitCameraViewport(viewportGroup, 1024, 600);
        updateRender();

        
		

//        
//        partsDisplay = new ListView<DBMPPart>();
//        partsDisplay.setCellFactory( lv -> {
//        	 CarPartListCell cell = new CarPartListCell() {
//                 @Override
//                 protected void updateItem(DBMPPart item, boolean empty) {
//                     super.updateItem(item, empty);
//                 }
//             };
//             return cell;
//        });
//        partsDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        partsDisplay.setOnKeyPressed(e -> {
//        	if (e.getCode().equals(KeyCode.DELETE)) deleteSelectedParts();
//        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
//
//        	DBMPPlus.attributesDisplay.getChildren().clear();
//        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
//        	
//        	e.consume();
//        });

        
        
//        attributesDisplay = new VBox();
//        ScrollPane scrollPaneAttrib = new ScrollPane(attributesDisplay);
//        scrollPaneAttrib.setFitToWidth(true);
//        
//        
//        for (DBMPPart p : mainDBMP.dBMPParts) {
//        	partsDisplay.getItems().add(p);
//        }

//        Label statusBar = new Label("Status: Ready");

        BorderPane root = new BorderPane();
        root.setTop(windowTop);
        root.setCenter(viewport);
//        root.setCenter(partsDisplay);
//        root.setRight(attributesDisplay);
//        root.setRight(scrollPaneAttrib);
//        root.setBottom(statusBar);
        

        root.setOnKeyPressed(e -> {
        	if (e.getCode() == KeyCode.A) {
        		CollisionsEditor.mainCollisions.mainBound.render = true;
        		for (CollisionBound b : CollisionsEditor.mainCollisions.childBounds) {
        			b.render = true;
        		}
        		CollisionsEditor.updateRender();
        	}
        });
        
        

		viewport.widthProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable evt) {
				updateRender();
			}
		});
        viewport.heightProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable evt) {
				updateRender();
			}
		});
		viewport.widthProperty().bind(root.widthProperty());
		viewport.heightProperty().bind(root.heightProperty());
		
        

//        attributesDisplay.setMinWidth(root.getWidth()/2);
        
        
        Scene scene = new Scene(root, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.NO.equals(sure)) {
				e.consume();
			}
        });
        
        primaryStage.show();
        
        
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
    
    public static void updateRender() {
    	viewport.viewportGroup.getChildren().clear();
    	viewport.buildAxes();
    	for(CollisionBound b : CollisionsEditor.mainCollisions.childBounds) {
        	b.updateShape();
        	viewport.viewportGroup.getChildren().addAll(b.displayPivot);
    		if (b.render) {
            	viewport.viewportGroup.getChildren().addAll(b.displayShape);
        	}
    	}
//        for (CollisionBound b : CollisionsEditor.mainCollisions.bounds) {
//        	if (b.render) {
//	        	b.updateShape();
//	        	viewport.viewportGroup.getChildren().addAll(b.displayPivot, b.displayShape);
//        	}
//        }
//        for (CollisionBoxShape b : CollisionsEditor.mainCollisions.boxShapes) {
//        	if (b.render) {
//	        	b.updateShape();
//	        	viewport.viewportGroup.getChildren().addAll(b.displayShape);
//        	}
//        }

        
    }
    
    public void updateAllPartsDisplay() {
    	//
    }
}
