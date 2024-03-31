package fr.ni240sx.ucgt.dbmpPlus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;

import fr.ni240sx.ucgt.binstuff.Hash;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DBMPPlus extends Application {
	
	public static DBMP mainDBMP = new DBMP();

	public static ListView<DBMPPart> partsDisplay;
	public static VBox attributesDisplay;
	
	public static ArrayList<CarPartListCell> carPartListCells = new ArrayList<CarPartListCell>();

	public static boolean debug = false;

	private static Menu menuDBMP;

	//stuff to put in a config file
	public static String lastDirectoryLoaded = Paths.get("").toAbsolutePath().toString();
	public static String lastFileLoaded = "";
//	  public static String lastFileLoaded = "C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"
	public static String lastFileSaved = Paths.get("").toAbsolutePath().toString(); //now unused
	public static String lastGeomRepLoaded = Paths.get("").toAbsolutePath().toString();
	public static boolean useDarkMode = false;
	public static boolean disableWarnings = false;
	public static boolean widebodyAutoCorrect = true;

	static String currentKitTemplate = "UC-KIT00";
	static String currentKitASZones = "11";
	static String currentExhRange = "00-05";
	static String currentExhMainPart = "MUFFLER";
	static String currentExhLeftTip = "EXHAUST_TIPS_LEFT";
	static boolean currentExhLeftTipEnabled = true;
	static String currentExhRightTip = "EXHAUST_TIPS_RIGHT";
	static boolean currentExhRightTipEnabled = true;
	static String currentExhCenterTip = "EXHAUST_TIPS_CENTER";
	static boolean currentExhCenterTipEnabled = true;
	static String currentExhASZones = "11";
	
	public static final String programName = "fire";
	public static final String programVersion = "1.1.1";
	
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("fire.dat")));
			br.readLine(); //="DO NOT MANUALLY EDIT THIS FILE"
			lastDirectoryLoaded = br.readLine();
			lastFileLoaded = br.readLine();
			lastFileSaved = br.readLine();
			useDarkMode = Boolean.valueOf(br.readLine());
			widebodyAutoCorrect = Boolean.valueOf(br.readLine());
			String s;
			if(!(s=br.readLine()).isBlank()) lastGeomRepLoaded = s;
			disableWarnings = Boolean.valueOf(br.readLine());
			
			br.close();
		} catch (Exception e) {}
		
		try {
			mainDBMP = DBMP.loadDBMP(new File(lastDirectoryLoaded + lastFileLoaded));
		} catch (Exception e){}
		
		
        launch(args);
        try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("fire.dat")));
			bw.write("DO NOT MANUALLY EDIT THIS FILE\n" 
					+ lastDirectoryLoaded + "\n" 
					+ lastFileLoaded + "\n" 
					+ lastFileSaved + "\n" 
					+ Boolean.toString(useDarkMode) + "\n"
					+ Boolean.toString(widebodyAutoCorrect) + "\n"
					+ lastGeomRepLoaded + "\n"
					+ Boolean.toString(disableWarnings) + "\n");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Could not save config");
		}
	}
	
    public void start(Stage primaryStage) {
        primaryStage.setTitle(programName + " - " + mainDBMP.carname.label);

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
			public void handle(ActionEvent arg0) {
		        ButtonType sure = null;
		        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to load a new DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure) || disableWarnings) {
					FileChooser fc = new FileChooser();
					fc.setInitialDirectory(new File(lastDirectoryLoaded));
					fc.setInitialFileName(lastFileLoaded);
					fc.getExtensionFilters().addAll(
				        new FileChooser.ExtensionFilter("BIN files", "*.bin"),
				        new FileChooser.ExtensionFilter("All files", "*.*"));
					fc.setTitle("Load an existing DBModelParts");
					DBMP loadDBMP;
					File selected = fc.showOpenDialog(null);
					if ((loadDBMP = DBMP.loadDBMP(selected))!=null) {
						mainDBMP = loadDBMP;
						lastFileLoaded = selected.getName();
						lastDirectoryLoaded = selected.getAbsolutePath().replace(lastFileLoaded, "");
//						  System.out.println(mainDBMP);
						updateAllPartsDisplay();
						primaryStage.setTitle(programName + " - " + mainDBMP.carname.label);
						menuDBMP.setText(mainDBMP.carname.label);
						if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Database loaded successfully.", ButtonType.OK).show();
					} else {
//						new Alert(Alert.AlertType.INFORMATION, "Nothing to load", ButtonType.OK).show();
					}
				}
			}
        });
        fileLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileNew.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create a blank DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				mainDBMP = new DBMP();
				updateAllPartsDisplay();
				primaryStage.setTitle("fire - "+mainDBMP.carname.label);
				menuDBMP.setText(mainDBMP.carname.label);
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
			fc.setInitialFileName(mainDBMP.carname.label);
			fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("BIN files", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
			fc.setTitle("Save " + mainDBMP.carname.label + "");
			

			try {
				File f = fc.showSaveDialog(null);
				lastFileSaved = f.getAbsolutePath().replace(lastFileLoaded, "");
				File f_old = f;
				f_old.renameTo(new File(f_old.getAbsoluteFile() + ".bak_" + DateTimeFormatter.ofPattern("uuMMdd-HHmmss").format(LocalDateTime.ofEpochSecond(f_old.lastModified(), 0, ZoneOffset.UTC))));
				mainDBMP.saveToFile(f);
				if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Database saved successfully.", ButtonType.OK).show();
	    		e.consume();        
			} catch (IOException e1) {
            	new Alert(Alert.AlertType.WARNING, "Error saving DBModelParts, please try again !").show();
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
        
        editUndo.setOnAction(e -> {
        	Undo.undo();
        });
        editUndo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        editRedo.setOnAction(e -> {
        	Undo.redo();
        });
        editRedo.setAccelerator(KeyCombination.keyCombination("Ctrl+Y"));
        
        menuEdit.getItems().addAll(editUndo, editRedo);
        
        
        menuDBMP = new Menu(mainDBMP.carname.label);

        MenuItem dbSortPartsByName = new MenuItem("Sort parts by kit + name");
        MenuItem dbSortPartsByName2 = new MenuItem("Sort parts by name");
        MenuItem dbSortAttributes = new MenuItem("Sort attributes");
//        CheckMenuItem dbSortPartsByNameExport = new CheckMenuItem("Sort parts by name at export");
//        CheckMenuItem dbSortAttributesExport = new CheckMenuItem("Sort attributes at export");
        MenuItem dbChangeName = new MenuItem("Change car name");
        MenuItem dbFixNameOffsets = new MenuItem("Fix NAME_OFFSETS based on the PART_NAME_OFFSETS");
        MenuItem dbFixCVs = new MenuItem("Fix CVs for BODY parts based on the car's name");
        MenuItem dbTrimToGeom = new MenuItem("Trim to a GEOMETRY...");
        
        dbSortPartsByName.setOnAction(e -> {
        	mainDBMP.dBMPParts.sort(new PartSorterByNameAsc());
	        if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "All parts sorted alphabetically.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });        
        dbSortPartsByName2.setOnAction(e -> {
        	mainDBMP.dBMPParts.sort(new PartSorterByName2Asc());
	        if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "All parts sorted.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });
        dbSortAttributes.setOnAction(e -> {
        	for(DBMPPart p: mainDBMP.dBMPParts) {
        		int s = p.attributes.size();
        		//most wack ass sorting method but it works
        		p.addAttribute(p.getAttribute("PART_NAME_SELECTOR"));
        		p.addAttribute(p.getAttribute("LOD_NAME_PREFIX_SELECTOR"));
        		p.addAttribute(p.getAttribute("MAX_LOD"));
        		p.addAttribute(p.getAttribute("LOD_CHARACTERS_OFFSET"));
        		p.addAttribute(p.getAttribute("NAME_OFFSET"));
        		if (p.getAttribute("MORPHTARGET_NUM")!=null) p.addAttribute(p.getAttribute("MORPHTARGET_NUM"));
        		p.addAttribute(p.getAttribute("PARTID_UPGRADE_GROUP"));
        		p.addAttribute(p.getAttribute("PART_NAME_OFFSETS"));
        		p.addAttribute(p.getAttribute("LOD_BASE_NAME"));
        		if (p.getAttribute("CV")!=null) p.addAttribute(p.getAttribute("CV"));
        		for (int i=0; i<s; i++) p.attributes.remove(0);
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        	if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "All attributes sorted.").show();
        });
//        dbSortPartsByNameExport.setOnAction(e -> {
//        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
//        });
//        dbSortAttributesExport.setOnAction(e -> {
//        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
//        });
        dbChangeName.setOnAction(e -> {
        	TextInputDialog td = new TextInputDialog();
        	td.setHeaderText("Enter a new car name");
        	try {
        		String s = td.showAndWait().get().strip();
        		if (!s.isBlank()) {
            		mainDBMP.carname = new Hash(s);
            		if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Car name changed.").show();
        			primaryStage.setTitle("fire - "+mainDBMP.carname.label);
        			menuDBMP.setText(mainDBMP.carname.label);
        			e.consume();
            	}
        	}catch (NoSuchElementException ex) {
        	}
        });
        dbFixNameOffsets.setOnAction(e -> {
        	for (DBMPPart p : mainDBMP.dBMPParts) {
        		((AttributeString)p.getAttribute("NAME_OFFSET")).value1 = ((AttributeTwoString)p.getAttribute("PART_NAME_OFFSETS")).value1.replace("KIT", "").replace("W", "");
        	}
        	if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "All name offsets fixed.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });
        dbFixCVs.setOnAction(e -> {
        	for (DBMPPart p : mainDBMP.dBMPParts) {
        		if (((AttributeCarPartID)p.getAttribute("PARTID_UPGRADE_GROUP")).ID == PartUndercover.BODY) {
        			if (((AttributeKey)p.getAttribute("CV")) == null) p.addAttribute(new AttributeKey("CV", DBMPPlus.mainDBMP.carname.label + "_CV"));
        			else ((AttributeKey)p.getAttribute("CV")).value = new Hash(DBMPPlus.mainDBMP.carname.label + "_CV");
        		}
        	}
        	if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "All CVs fixed.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });
        dbTrimToGeom.setOnAction(e -> {
	        ButtonType sure = null;
	        if (!disableWarnings) sure = new Alert(Alert.AlertType.WARNING, "Are you sure you want to trim this DB ? This can take a while; backup recommended.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure) || disableWarnings) {
				FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File(lastGeomRepLoaded));
				fc.setInitialFileName("GEOMETRY.BIN");
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("BIN files", "*.bin"),
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load a geometry file");
				File selected = fc.showOpenDialog(null);
				try {
					FileInputStream fis = new FileInputStream(selected);
					byte [] fileToBytes = new byte[(int)selected.length()];
					fis.read(fileToBytes);
					fis.close();
					
					ArrayList<DBMPPart> toRemove = new ArrayList<DBMPPart>(mainDBMP.dBMPParts);
		        	ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
		        	bb.position(184);
		        	int i;
		        	while ((i = bb.getInt()) != 71308032) {	//stop searching when 0x04401300 is found
						for (DBMPPart p : mainDBMP.dBMPParts) { //optimization has to be done probably
							Hash part = new Hash( mainDBMP.carname.label + "_" + ((AttributeTwoString)p.getAttribute("LOD_BASE_NAME")).value1 + "_" + ((AttributeTwoString)p.getAttribute("LOD_BASE_NAME")).value2 + "_A");
							if (i == part.reversedBinHash) {
//								System.out.println("Part " + p.displayName + " found in geom file");
								toRemove.remove(p);
							}
						}
						bb.getInt(); //jumps the blank 4 bytes between each part
					}
					
					for (DBMPPart p : toRemove) {
//						System.out.println("Removing part : "+p.displayName);
						if ((((AttributeCarPartID) p.getAttribute("PARTID_UPGRADE_GROUP")).ID != PartUndercover.EXHAUST_TIPS_CENTER) &&
							(((AttributeCarPartID) p.getAttribute("PARTID_UPGRADE_GROUP")).ID != PartUndercover.EXHAUST_TIPS_LEFT) &&
							(((AttributeCarPartID) p.getAttribute("PARTID_UPGRADE_GROUP")).ID != PartUndercover.EXHAUST_TIPS_RIGHT)){
							mainDBMP.dBMPParts.remove(p);
						}
					}
					
					lastGeomRepLoaded = selected.getAbsolutePath().replace(selected.getName(), "");
					updateAllPartsDisplay();
					if (!disableWarnings) new Alert(Alert.AlertType.INFORMATION, "Database trimmed successfully", ButtonType.OK).show();
				} catch (Exception ex) {
					new Alert(Alert.AlertType.ERROR, "An error occured.", ButtonType.OK).show();
				}
	        	e.consume();
        	}
        });
        
        menuDBMP.getItems().addAll(dbSortPartsByName, dbSortPartsByName2, dbSortAttributes, dbFixNameOffsets, dbFixCVs, dbChangeName, dbTrimToGeom/*, dbSortPartsByNameExport, dbSortAttributesExport*/);
        
        
        Menu menuAttributes = new Menu("Attributes");
        
        MenuItem attributeAddAS = new MenuItem("Add MORPHTARGET_NUM");
        MenuItem attributeRemoveAS = new MenuItem("Remove MORPHTARGET_NUM");
        MenuItem attributeAddCV = new MenuItem("Add CV (for BODY only)");
        MenuItem attributeRemoveCV = new MenuItem("Remove CV (for BODY only)");
        MenuItem attributeSort = new MenuItem("Sort");

        attributeAddAS.setOnAction(e -> {
        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
        		if (p.getAttribute("MORPHTARGET_NUM") == null) {
        			p.addAttribute(new AttributeInteger("MORPHTARGET_NUM", Integer.valueOf(currentKitASZones)));
        			p.update();
        		}
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        });
        attributeRemoveAS.setOnAction(e -> {
        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
        		if (p.getAttribute("MORPHTARGET_NUM") != null) {
        			p.attributes.remove(p.getAttribute("MORPHTARGET_NUM"));
        			p.update();
        		}
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        });
        attributeAddCV.setOnAction(e -> {
        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
        		if (p.getAttribute("CV") == null) {
        			p.addAttribute(new AttributeKey("CV", mainDBMP.carname.label + "_CV"));
        			p.update();
        		}
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        });
        attributeRemoveCV.setOnAction(e -> {
        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
        		if (p.getAttribute("CV") != null) {
        			p.attributes.remove(p.getAttribute("CV"));
        			p.update();
        		}
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        });
        attributeSort.setOnAction(e -> {
        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
        		int s = p.attributes.size();
        		//most wack ass sorting method but it works
        		p.addAttribute(p.getAttribute("PART_NAME_SELECTOR"));
        		p.addAttribute(p.getAttribute("LOD_NAME_PREFIX_SELECTOR"));
        		p.addAttribute(p.getAttribute("MAX_LOD"));
        		p.addAttribute(p.getAttribute("LOD_CHARACTERS_OFFSET"));
        		p.addAttribute(p.getAttribute("NAME_OFFSET"));
        		if (p.getAttribute("MORPHTARGET_NUM")!=null) p.addAttribute(p.getAttribute("MORPHTARGET_NUM"));
        		p.addAttribute(p.getAttribute("PARTID_UPGRADE_GROUP"));
        		p.addAttribute(p.getAttribute("PART_NAME_OFFSETS"));
        		p.addAttribute(p.getAttribute("LOD_BASE_NAME"));
        		if (p.getAttribute("CV")!=null) p.addAttribute(p.getAttribute("CV"));
        		for (int i=0; i<s; i++) p.attributes.remove(0);
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        });
        
        menuAttributes.getItems().addAll(attributeAddAS, attributeRemoveAS, attributeAddCV, attributeRemoveCV, attributeSort);
        
        Menu menuSettings = new Menu(programName);
        
        CheckMenuItem settingsDark = new CheckMenuItem("Dark mode");
        CheckMenuItem settingsWarnings = new CheckMenuItem("Disable warning pop-ups");
        CheckMenuItem settingsWidebodyAutoCorrect = new CheckMenuItem("Auto-correct widebody part names");
        MenuItem settingsAbout = new MenuItem("About " + programName + "...");
        
        if (widebodyAutoCorrect) settingsWidebodyAutoCorrect.setSelected(true);
        settingsWidebodyAutoCorrect.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
        	if (isSelected) widebodyAutoCorrect = true; 
        	else widebodyAutoCorrect = false;
        });

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
					+ "Advanced DBModelParts editor for NFS Undercover.\n"
					+ "Aims to make the creation of such data less of a hassle for modders.\n"
					+ "This software has been originally created for the mod Undercover Exposed.\n\n"
					+ "Not affiliated with EA, MaxHwoy, nfsu360, etc.\n\n"
					+ "NI240SX 2023-2024 - No rights reserved"));
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
        
        menuSettings.getItems().addAll(settingsDark, settingsWarnings, settingsWidebodyAutoCorrect, settingsAbout);
        
        
        menuBar.getMenus().addAll(menuFile, menuEdit, menuDBMP, menuAttributes, menuSettings);
        
        
        
        //
        // SHORTCUTS BAR
        //
        
        
        
        HBox shortcutsBar = new HBox();
        Button partAdd = new Button("Add...");
        Button partCopy = new Button("Copy");
        Button partCopyAdvanced = new Button("Copy...");
        Button partDelete = new Button("Delete");
        
        Button kitGenerate = new Button("Generate kit...");
        Button kitCopy = new Button("Copy kit...");
        Button kitExhausts = new Button("Exhausts...");
        Button kitSaveTemplate = new Button("Save kit as template...");
        Button kitDelete = new Button("Delete kit");

        Button partsHoods = new Button("Hoods...");
        Button partsSpoilers = new Button("Spoilers...");
        Button partsRims = new Button("Rims...");

        Separator infSep = new Separator();
        HBox.setHgrow(infSep, Priority.ALWAYS);
        shortcutsBar.getChildren().addAll(partAdd, partCopy, partCopyAdvanced, partDelete, 
        		new Separator(), kitGenerate, kitCopy, kitExhausts, kitSaveTemplate, kitDelete,
        		new Separator(), partsHoods, partsSpoilers, partsRims);

        partAdd.setOnAction(e-> {
        	TextInputDialog td = new TextInputDialog("KIT00_BASE");
        	td.setHeaderText("Enter a part name");
        	try {
        		String s = td.showAndWait().get().strip();
        		if (!s.isBlank()) {
        			String kit = s.split("_")[0];
        			String part = s.substring(kit.length()+1);
        			if (part.contains("FRONT")) {
        				mainDBMP.dBMPParts.add(new DBMPPart(kit, part));
        				mainDBMP.dBMPParts.add(new DBMPPart(kit, part.replace("FRONT", "REAR")));
        				if(part.contains("LEFT")) {
            				mainDBMP.dBMPParts.add(new DBMPPart(kit, part.replace("LEFT", "RIGHT")));
            				mainDBMP.dBMPParts.add(new DBMPPart(kit, part.replace("FRONT", "REAR").replace("LEFT", "RIGHT")));
        				}
        			} else {
        				if(part.contains("LEFT")) {
            				mainDBMP.dBMPParts.add(new DBMPPart(kit, part));
            				mainDBMP.dBMPParts.add(new DBMPPart(kit, part.replace("LEFT", "RIGHT")));
            				if(part.contains("LIGHT")) {
            					mainDBMP.dBMPParts.add(new DBMPPart(kit, part.replace("LIGHT", "LIGHT_GLASS")));
                				mainDBMP.dBMPParts.add(new DBMPPart(kit, part.replace("LIGHT", "LIGHT_GLASS").replace("LEFT", "RIGHT")));	
            				}
        				} else {
            				mainDBMP.dBMPParts.add(new DBMPPart(kit, part));
        				}
        			}
        			updateAllPartsDisplay();
        			e.consume();
            	}
        	}catch (NoSuchElementException ex) {
        	}
        });
        partCopy.setOnAction(e -> {
        	DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
            	DBMPPart p2 = new DBMPPart(p);
            	mainDBMP.dBMPParts.add(mainDBMP.dBMPParts.indexOf(p)+1, p2);
            }
        	updateAllPartsDisplay();
        	partsDisplay.getSelectionModel().select(sel);
        	e.consume();
        });
//    	buttonCopy.setTooltip(new Tooltip("Simply duplicate selected part(s) (Ctrl+C)"));
    	partCopyAdvanced.setOnAction(e -> {
        	Stage st = new Stage();
			st.setTitle("Advanced copying");
			
			CheckBox copyTo = new CheckBox("Copy to kit(s) : ");
			TextField copyToInput = new TextField("");
			
			CheckBox toAnotherKitReference = new CheckBox("Point to model parts from kit : ");
			TextField referenceKitInput = new TextField("KIT00");
			CheckBox autosculptZones = new CheckBox("Harmonize Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentKitASZones);
			
			copyToInput.setDisable(true);
			referenceKitInput.setDisable(true);
			autosculptZonesInput.setDisable(true);
			toAnotherKitReference.setDisable(true);

			copyTo.setOnAction(evh -> {
				if(copyTo.isSelected()) {
					copyToInput.setDisable(false);
					toAnotherKitReference.setDisable(false);					
				}
				else {
					copyToInput.setDisable(true);
					toAnotherKitReference.setDisable(true);
					referenceKitInput.setDisable(true);
				}
				evh.consume();
			});
			toAnotherKitReference.setOnAction(evh -> {
				if(toAnotherKitReference.isSelected()) referenceKitInput.setDisable(false); else referenceKitInput.setDisable(true);
				evh.consume();
			});
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			
			GridPane gp = new GridPane();
			gp.add(copyTo, 0, 0);
			gp.add(copyToInput, 1, 0);
			gp.add(toAnotherKitReference, 0, 1);
			gp.add(referenceKitInput, 1, 1);
			gp.add(autosculptZones, 0, 2);
			gp.add(autosculptZonesInput, 1, 2);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");
			ok.setOnAction(evh -> {
				currentKitASZones = autosculptZonesInput.getText();
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
	        	for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
	            	if(copyTo.isSelected()) {
	            		ArrayList<String> copyToKits = new ArrayList<String>();
	    				for (String s : copyToInput.getText().split(",")) {
	    					if (s.contains("-")) {
	    						for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
	    								i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
	    							if (i<10) copyToKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
	    							else copyToKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
	    						}
	    					} else if (!s.strip().isBlank()) copyToKits.add(s.strip());
	    				}
	            		
	            		for (String k : copyToKits) {
	            			DBMPPart tp;
		            		mainDBMP.dBMPParts.add(tp = new DBMPPart(p));
		            		((AttributeTwoString)tp.getAttribute("PART_NAME_OFFSETS")).value1 = k;
		            		((AttributeString)tp.getAttribute("NAME_OFFSET")).value1 = k.replace("KIT", "").replace("W", "");
		            		if(toAnotherKitReference.isSelected()) {
			            		((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = referenceKitInput.getText().trim();
		            		}else {
			            		((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = k;
		            		}
		            		if(autosculptZones.isSelected()) {
			            		int morphnum = Integer.parseInt(autosculptZonesInput.getText().strip());
			            		if (tp.getAttribute("MORPHTARGET_NUM") == null) {
			            			if (morphnum > 0) tp.addAttribute(new AttributeInteger("MORPHTARGET_NUM", morphnum));
			            		} else {
			            			if (morphnum > 0) ((AttributeInteger)tp.getAttribute("MORPHTARGET_NUM")).value = morphnum;
			            			else tp.attributes.remove(tp.getAttribute("MORPHTARGET_NUM"));
			            		}
			            	}
		            		
	            		}	
	            	} else {
		            	DBMPPart tp;
		            	mainDBMP.dBMPParts.add(mainDBMP.dBMPParts.indexOf(p)+1, tp = new DBMPPart(p));
		            	if(autosculptZones.isSelected()) {
		            		int morphnum = Integer.parseInt(autosculptZonesInput.getText().strip());
		            		if (tp.getAttribute("MORPHTARGET_NUM") == null) {
		            			if (morphnum > 0) tp.addAttribute(new AttributeInteger("MORPHTARGET_NUM", morphnum));
		            		} else {
		            			if (morphnum > 0) ((AttributeInteger)tp.getAttribute("MORPHTARGET_NUM")).value = morphnum;
		            			else tp.attributes.remove(tp.getAttribute("MORPHTARGET_NUM"));
		            		}
		            	}
	            	}
	            }
	        	updateAllPartsDisplay();
	        	partsDisplay.getSelectionModel().select(sel);
				st.close();
				evh.consume();
			});
			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
			vb.getChildren().addAll(gp, ok);
			st.show();
        	e.consume();
        });
        partDelete.setOnAction(e -> {
        	deleteSelectedParts();
        });
        
        kitGenerate.setOnAction(e -> {
        	Stage st = new Stage();
			st.setTitle("Full kit generation");
			
			Button kitTemplate = new Button("Use template");
			TextField kitTemplateInput = new TextField(currentKitTemplate);
			Label kitsToGenerate = new Label("Generate kit(s) : ");
			TextField kitsToGenerateInput = new TextField("");
			CheckBox autosculptZones = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentKitASZones);
			
			autosculptZonesInput.setDisable(true);
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			GridPane gp = new GridPane();
			gp.add(kitTemplate, 0, 0);
			gp.add(kitTemplateInput, 1, 0);
			gp.add(kitsToGenerate, 0, 1);
			gp.add(kitsToGenerateInput, 1, 1);
			gp.add(autosculptZones, 0, 2);
			gp.add(autosculptZonesInput, 1, 2);
			
			Label range = new Label("Range of exhausts to add : ");
			TextField rangeInput = new TextField(currentExhRange);
			Label partsToGen = new Label("Parts to generate");
			Label muffler = new Label("Main part : ");
			TextField mufflerInput = new TextField(currentExhMainPart);
			CheckBox rightTips = new CheckBox("Right tips : ");
			TextField rightTipsInput = new TextField(currentExhRightTip);
			CheckBox leftTips = new CheckBox("Left tips : ");
			TextField leftTipsInput = new TextField(currentExhLeftTip);
			CheckBox centerTips = new CheckBox("Center tips : ");
			TextField centerTipsInput = new TextField(currentExhCenterTip);

			CheckBox pointToKit = new CheckBox("Point to model kit : ");
			TextField pointToKitInput = new TextField("KIT00");
			CheckBox autosculptZonesExhaust = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesExhaustInput = new TextField(currentExhASZones);
			CheckBox removePreExisting = new CheckBox("Remove pre-existing parts");

			rightTips.setSelected(currentExhRightTipEnabled);
			rightTips.setOnAction(evh -> {
				if(rightTips.isSelected()) rightTipsInput.setDisable(false); else rightTipsInput.setDisable(true);
				evh.consume();
			});
			leftTips.setSelected(currentExhLeftTipEnabled);
			leftTips.setOnAction(evh -> {
				if(leftTips.isSelected()) leftTipsInput.setDisable(false); else leftTipsInput.setDisable(true);
				evh.consume();
			});
			centerTips.setSelected(currentExhCenterTipEnabled);
			centerTips.setOnAction(evh -> {
				if(centerTips.isSelected()) centerTipsInput.setDisable(false); else centerTipsInput.setDisable(true);
				evh.consume();
			});
			pointToKitInput.setDisable(true);
			pointToKit.setOnAction(evh -> {
				if(pointToKit.isSelected()) pointToKitInput.setDisable(false); else pointToKitInput.setDisable(true);
				evh.consume();
			});
			autosculptZonesExhaustInput.setDisable(true);
			autosculptZonesExhaust.setOnAction(evh -> {
				if(autosculptZonesExhaust.isSelected()) autosculptZonesExhaustInput.setDisable(false); else autosculptZonesExhaustInput.setDisable(true);
				evh.consume();
			});
			GridPane gpEx = new GridPane();
			gpEx.add(range, 0, 1);
			gpEx.add(rangeInput, 1, 1);
			gpEx.add(new Separator(Orientation.HORIZONTAL), 0, 2);
			gpEx.add(partsToGen, 0, 3);
			gpEx.add(muffler, 0, 4);
			gpEx.add(mufflerInput, 1, 4);
			gpEx.add(leftTips, 0, 5);
			gpEx.add(leftTipsInput, 1, 5);
			gpEx.add(rightTips, 0, 6);
			gpEx.add(rightTipsInput, 1, 6);
			gpEx.add(centerTips, 0, 7);
			gpEx.add(centerTipsInput, 1, 7);
			gpEx.add(new Separator(Orientation.HORIZONTAL), 0, 8);
			gpEx.add(pointToKit, 0, 9);
			gpEx.add(pointToKitInput, 1, 9);
			gpEx.add(autosculptZonesExhaust, 0, 10);
			gpEx.add(autosculptZonesExhaustInput, 1, 10);
			
			gp.add(removePreExisting, 0, 3);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");

			Label titleGeneral = new Label("General kit settings");
			Label titleExhausts = new Label("Exhaust generation settings");

			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
						
			vb.getChildren().addAll(titleGeneral, new Separator(Orientation.HORIZONTAL), gp, 
					new Separator(Orientation.HORIZONTAL), titleExhausts, new Separator(Orientation.HORIZONTAL), gpEx,
					new Separator(Orientation.HORIZONTAL), ok);
			st.show();
        	e.consume();
        	
        	kitTemplate.setOnAction(evh -> {
        		FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("kits"));
				fc.setInitialFileName(kitTemplateInput.getText());
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Load a kit template");
				kitTemplateInput.setText(fc.showOpenDialog(null).getName());
        		evh.consume();
        	});
        	
        	
        	ok.setOnAction(evh -> {
        		currentKitTemplate = kitTemplateInput.getText();
        		currentKitASZones = autosculptZonesInput.getText();
        		currentExhRange = rangeInput.getText();
        		currentExhMainPart = mufflerInput.getText();
        		currentExhLeftTip = leftTipsInput.getText();
        		currentExhLeftTipEnabled = leftTips.isSelected();
        		currentExhRightTip = rightTipsInput.getText();
        		currentExhRightTipEnabled = rightTips.isSelected();
        		currentExhCenterTip = centerTipsInput.getText();
        		currentExhCenterTipEnabled = centerTips.isSelected();
        		currentExhASZones = autosculptZonesExhaustInput.getText();
        		
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
        		try {
					ArrayList<String> generateKits = new ArrayList<String>();
					for (String s : kitsToGenerateInput.getText().split(",")) {
						if (s.contains("-")) {
							for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
									i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
								if (i<10) generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
								else generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
							}
						} else if (!s.strip().isBlank()) generateKits.add(s.strip());
					}
					
					ArrayList<String> exhausts = new ArrayList<String>();
					for (String s : rangeInput.getText().split(",")) {
						if (s.contains("-")) {
							for(int i= Integer.parseInt(s.split("-")[0].strip()); i<=Integer.parseInt(s.split("-")[1].strip()); i++) {
								if (i<10) exhausts.add("0" + i); else exhausts.add(""+i);
							}
						} else if (!s.strip().isBlank()) exhausts.add(s.strip());
					}				
					
					//first load kit parts from template
					ArrayList<String> parts = new ArrayList<String>();
					BufferedReader br = new BufferedReader(new FileReader(new File("kits\\" + kitTemplateInput.getText())));
					String line;
					while((line = br.readLine()) != null) {
						parts.add(line);
					}
					br.close();
					
					int size = mainDBMP.dBMPParts.size();
					for(int i=0; i<size; i++) {
						if (generateKits.contains(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value1)) {
							if(removePreExisting.isSelected()) { // remove any preexisting
								mainDBMP.dBMPParts.remove(i);
								size--;
								i--;
							} else { // remove conflicting preexisting
								boolean val=false;
								for (String part : parts) {
									if (part.split("/")[0].equals(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value2)) {
										val = true;
									}
								}
								if (val) { 
									mainDBMP.dBMPParts.remove(i);
									size--;
									i--;
								}
							}
						}
					}


					for (String kit : generateKits) {
						for (String part : parts) {
							DBMPPart tp;
							mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, part.split("/")[0]));
							if(part.contains("/")) {
								((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value2 = part.split("/")[1];
							}
							if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						}
						for (String exh : exhausts) {
							DBMPPart tp;
							mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, mufflerInput.getText().strip() + "_" + exh));
							((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST;
							if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
							if(autosculptZonesExhaust.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesExhaustInput.getText().strip())));
							
							if (leftTips.isSelected()) {
								mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, leftTipsInput.getText().strip() + "_" + exh));
								((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_LEFT;
								if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
								if (autosculptZonesExhaust.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesExhaustInput.getText().strip())));
							}
							if (rightTips.isSelected()) {
								mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, rightTipsInput.getText().strip() + "_" + exh));
								((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_RIGHT;
								if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
								if (autosculptZonesExhaust.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesExhaustInput.getText().strip())));
							}
							if (centerTips.isSelected()) {
								mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, centerTipsInput.getText().strip() + "_" + exh));
								((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_CENTER;
								if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
								if (autosculptZonesExhaust.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesExhaustInput.getText().strip())));
							}
						}
					}
		        	updateAllPartsDisplay();
		        	partsDisplay.getSelectionModel().select(sel);
					st.close();
					evh.consume();
	        	}catch (IOException exception) {
	            	new Alert(Alert.AlertType.WARNING, "Error loading template, please try again !").show();
	        	}
			});
        	
        	e.consume();
        });
        kitCopy.setOnAction(e -> {
        	Stage st = new Stage();
			st.setTitle("Full kit copying");
			
			Label copyFrom = new Label("Copy from kit : ");
			TextField copyFromInput;
			
			if(partsDisplay.getSelectionModel().getSelectedItem() != null) {
				copyFromInput = new TextField(((AttributeTwoString)partsDisplay.getSelectionModel().getSelectedItem().getAttribute("PART_NAME_OFFSETS")).value1);
			} else {
				copyFromInput = new TextField("KIT00");
			}
			Label copyTo = new Label("Copy to kit(s) : ");
			TextField copyToInput = new TextField("");
			CheckBox autosculptZones = new CheckBox("Harmonize Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentKitASZones);
			
			autosculptZonesInput.setDisable(true);
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			GridPane gp = new GridPane();
			gp.add(copyFrom, 0, 0);
			gp.add(copyFromInput, 1, 0);
			gp.add(copyTo, 0, 1);
			gp.add(copyToInput, 1, 1);
			gp.add(autosculptZones, 0, 2);
			gp.add(autosculptZonesInput, 1, 2);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");
			ok.setOnAction(evh -> {
        		currentKitASZones = autosculptZonesInput.getText();
				String copyFromKit = copyFromInput.getText().trim();
				ArrayList<String> copyToKits = new ArrayList<String>();
				for (String s : copyToInput.getText().split(",")) {
					if (s.contains("-")) {
						for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
								i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
							if (i<10) copyToKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
							else copyToKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
						}
					} else if (!s.strip().isBlank()) copyToKits.add(s.strip());
				}
				
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
	        	for(int i=0; i<mainDBMP.dBMPParts.size(); i++) { //looping on Part p: mainDBMP.parts throws an exception for whatever reason
	        		if (((AttributeTwoString)mainDBMP.dBMPParts.get(i) .getAttribute("PART_NAME_OFFSETS")).value1.equals(copyFromKit)) {
		        		for(String s : copyToKits) {
		        			DBMPPart p2 = new DBMPPart(mainDBMP.dBMPParts.get(i));
		            		((AttributeTwoString)p2.getAttribute("PART_NAME_OFFSETS")).value1 = s;
		            		((AttributeString)p2.getAttribute("NAME_OFFSET")).value1 = s.replace("KIT", "").replace("W", "");
		            		((AttributeTwoString)p2.getAttribute("LOD_BASE_NAME")).value1 = s;
	
			            	if(autosculptZones.isSelected()) {
			            		int morphnum = Integer.parseInt(autosculptZonesInput.getText().strip());
			            		if (p2.getAttribute("MORPHTARGET_NUM") == null) {
			            			if (morphnum > 0) p2.addAttribute(new AttributeInteger("MORPHTARGET_NUM", morphnum));
			            		} else {
			            			if (morphnum > 0) ((AttributeInteger)p2.getAttribute("MORPHTARGET_NUM")).value = morphnum;
			            			else p2.attributes.remove(p2.getAttribute("MORPHTARGET_NUM"));
			            		}
			            	}
			            	p2.update();
			            	mainDBMP.dBMPParts.add(p2);
		        		}
	        		}
	            }
	        	updateAllPartsDisplay();
	        	partsDisplay.getSelectionModel().select(sel);
				st.close();
				evh.consume();
			});

			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
						
			vb.getChildren().addAll(gp, ok);
			
			st.show();
			
        	e.consume();
        });
		kitExhausts.setOnAction(e -> {
			Stage st = new Stage();
			st.setTitle("Kit exhausts generation");
			
			Label workOnKits = new Label("Kits to work on : ");
			TextField workOnKitsInput;
			
			if(partsDisplay.getSelectionModel().getSelectedItem() != null) {
				workOnKitsInput = new TextField(((AttributeTwoString)partsDisplay.getSelectionModel().getSelectedItem().getAttribute("PART_NAME_OFFSETS")).value1);
			} else {
				workOnKitsInput = new TextField("");
			}
			Label range = new Label("Range of exhausts to add : ");
			TextField rangeInput = new TextField(currentExhRange);
			Label partsToGen = new Label("Parts to generate");
			Label muffler = new Label("Main part : ");
			TextField mufflerInput = new TextField(currentExhMainPart);
			CheckBox rightTips = new CheckBox("Right tips : ");
			TextField rightTipsInput = new TextField(currentExhRightTip);
			CheckBox leftTips = new CheckBox("Left tips : ");
			TextField leftTipsInput = new TextField(currentExhLeftTip);
			CheckBox centerTips = new CheckBox("Center tips : ");
			TextField centerTipsInput = new TextField(currentExhCenterTip);

			CheckBox pointToKit = new CheckBox("Point to model kit : ");
			TextField pointToKitInput = new TextField("KIT00");
			CheckBox autosculptZones = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentExhASZones);
			CheckBox removePreExisting = new CheckBox("Remove pre-existing exhausts");

			rightTips.setSelected(currentExhRightTipEnabled);
			rightTips.setOnAction(evh -> {
				if(rightTips.isSelected()) rightTipsInput.setDisable(false); else rightTipsInput.setDisable(true);
				evh.consume();
			});
			leftTips.setSelected(currentExhLeftTipEnabled);
			leftTips.setOnAction(evh -> {
				if(leftTips.isSelected()) leftTipsInput.setDisable(false); else leftTipsInput.setDisable(true);
				evh.consume();
			});
			centerTips.setSelected(currentExhCenterTipEnabled);
			centerTips.setOnAction(evh -> {
				if(centerTips.isSelected()) centerTipsInput.setDisable(false); else centerTipsInput.setDisable(true);
				evh.consume();
			});
			pointToKitInput.setDisable(true);
			pointToKit.setOnAction(evh -> {
				if(pointToKit.isSelected()) pointToKitInput.setDisable(false); else pointToKitInput.setDisable(true);
				evh.consume();
			});
			autosculptZonesInput.setDisable(true);
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			GridPane gp = new GridPane();
			gp.add(workOnKits, 0, 0);
			gp.add(workOnKitsInput, 1, 0);
			gp.add(range, 0, 1);
			gp.add(rangeInput, 1, 1);
			gp.add(new Separator(Orientation.HORIZONTAL), 0, 2);
			gp.add(partsToGen, 0, 3);
			gp.add(muffler, 0, 4);
			gp.add(mufflerInput, 1, 4);
			gp.add(leftTips, 0, 5);
			gp.add(leftTipsInput, 1, 5);
			gp.add(rightTips, 0, 6);
			gp.add(rightTipsInput, 1, 6);
			gp.add(centerTips, 0, 7);
			gp.add(centerTipsInput, 1, 7);
			gp.add(new Separator(Orientation.HORIZONTAL), 0, 8);
			gp.add(pointToKit, 0, 9);
			gp.add(pointToKitInput, 1, 9);
			gp.add(autosculptZones, 0, 10);
			gp.add(autosculptZonesInput, 1, 10);
			gp.add(removePreExisting, 0, 11);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");
			ok.setOnAction(evh -> {
        		currentExhRange = rangeInput.getText();
        		currentExhMainPart = mufflerInput.getText();
        		currentExhLeftTip = leftTipsInput.getText();
        		currentExhLeftTipEnabled = leftTips.isSelected();
        		currentExhRightTip = rightTipsInput.getText();
        		currentExhRightTipEnabled = rightTips.isSelected();
        		currentExhCenterTip = centerTipsInput.getText();
        		currentExhCenterTipEnabled = centerTips.isSelected();
        		currentExhASZones = autosculptZonesInput.getText();
				ArrayList<String> kits = new ArrayList<String>();
				for (String s : workOnKitsInput.getText().split(",")) {
					if (s.contains("-")) {
						for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
								i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
							if (i<10) kits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
							else kits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
						}
					} else if (!s.strip().isBlank()) kits.add(s.strip());
				}
				
				ArrayList<String> exhausts = new ArrayList<String>();
				for (String s : rangeInput.getText().split(",")) {
					if (s.contains("-")) {
						for(int i= Integer.parseInt(s.split("-")[0].strip()); i<=Integer.parseInt(s.split("-")[1].strip()); i++) {
							if (i<10) exhausts.add("0" + i); else exhausts.add(""+i);
						}
					} else if (!s.strip().isBlank()) exhausts.add(s.strip());
				}				
				
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
	        	
				if(removePreExisting.isSelected()) {
					int size = mainDBMP.dBMPParts.size();
					for(int i=0; i<size; i++) {
						if (kits.contains(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value1) 
								&& ((AttributeCarPartID)mainDBMP.dBMPParts.get(i).getAttribute("PARTID_UPGRADE_GROUP")).ID.getText().contains("EXHAUST")) {
							mainDBMP.dBMPParts.remove(i);
							size--;
							i--;
						}
					}
				}

				for (String kit : kits) {
					for (String exh : exhausts) {
						DBMPPart tp;
						mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, mufflerInput.getText().strip() + "_" + exh));
						((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST;
						if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
						if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						
						if (leftTips.isSelected()) {
							mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, leftTipsInput.getText().strip() + "_" + exh));
							((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_LEFT;
							if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
							if (autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						}
						if (rightTips.isSelected()) {
							mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, rightTipsInput.getText().strip() + "_" + exh));
							((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_RIGHT;
							if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
							if (autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						}
						if (centerTips.isSelected()) {
							mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, centerTipsInput.getText().strip() + "_" + exh));
							((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_CENTER;
							if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
							if (autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						}
					}
				}
	        	updateAllPartsDisplay();
	        	partsDisplay.getSelectionModel().select(sel);
				st.close();
				evh.consume();
			});

			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
						
			vb.getChildren().addAll(gp, ok);
			
			st.show();
			
        	e.consume();
		});
        kitSaveTemplate.setOnAction(e -> {
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) {	
        		String kit = ((AttributeTwoString)partsDisplay.getSelectionModel().getSelectedItem().getAttribute("PART_NAME_OFFSETS")).value1;
	    		FileChooser fc = new FileChooser();
				fc.setInitialDirectory(new File("kits"));
				fc.setInitialFileName("Template");
				fc.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("All files", "*.*"));
				fc.setTitle("Save " + kit + " as template");

				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(fc.showSaveDialog(null)));
					for (DBMPPart p : mainDBMP.dBMPParts) {
						if (((AttributeTwoString)p.getAttribute("PART_NAME_OFFSETS")).value1.equals(kit) 
								&& !((AttributeCarPartID)p.getAttribute("PARTID_UPGRADE_GROUP")).ID.getText().contains("EXHAUST")) {
							bw.write(((AttributeTwoString)p.getAttribute("PART_NAME_OFFSETS")).value2);
							if (!((AttributeTwoString)p.getAttribute("PART_NAME_OFFSETS")).value2.equals(((AttributeTwoString)p.getAttribute("LOD_BASE_NAME")).value2)) {
								bw.write("/" + ((AttributeTwoString)p.getAttribute("LOD_BASE_NAME")).value2);
							}
							bw.write("\n");
						}
					}
					bw.close();
		    		e.consume();        
				} catch (IOException e1) {
	            	new Alert(Alert.AlertType.WARNING, "Error saving template, please try again !").show();
				}
        	}
        });
        kitDelete.setOnAction(e -> {
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) {
	        	String kit = ((AttributeTwoString)partsDisplay.getSelectionModel().getSelectedItem().getAttribute("PART_NAME_OFFSETS")).value1;
		        ButtonType sure = null;
		        if (!disableWarnings) sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to delete all parts from " + kit + " ?", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure) || disableWarnings) {
					int size = mainDBMP.dBMPParts.size();
					for(int i=0; i<size; i++) {
						if (kit.equals(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value1)) {
							mainDBMP.dBMPParts.remove(i);
							size--;
							i--;
						}
					}
		        	updateAllPartsDisplay();
				}
        	}
        });

        partsHoods.setOnAction(e -> {
        	Stage st = new Stage();
			st.setTitle("Add hoods");
			
			Label addTo = new Label("Add to kit(s) : ");
			TextField addToInput = new TextField("");
			CheckBox autosculptZones = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentKitASZones);
			
			autosculptZonesInput.setDisable(true);
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			GridPane gp = new GridPane();
			gp.add(addTo, 0, 1);
			gp.add(addToInput, 1, 1);
			gp.add(autosculptZones, 0, 2);
			gp.add(autosculptZonesInput, 1, 2);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");
			ok.setOnAction(evh -> {
        		currentKitASZones = autosculptZonesInput.getText();
				ArrayList<String> generateKits = new ArrayList<String>();
				for (String s : addToInput.getText().split(",")) {
					if (s.contains("-")) {
						for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
								i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
							if (i<10) generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
							else generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
						}
					} else if (!s.strip().isBlank()) generateKits.add(s.strip());
				}
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
				
				int size = mainDBMP.dBMPParts.size();
				for(int i=0; i<size; i++) {
					if (generateKits.contains(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value1) 
							&& ((AttributeCarPartID)mainDBMP.dBMPParts.get(i).getAttribute("PARTID_UPGRADE_GROUP")).ID == PartUndercover.HOOD) {
					// remove conflicting preexisting
						mainDBMP.dBMPParts.remove(i);
						size--;
						i--;
					}
				}
				for (String kit : generateKits) {
						DBMPPart tp;
						mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, "HOOD"));
						if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
				}
	        	updateAllPartsDisplay();
	        	partsDisplay.getSelectionModel().select(sel);
				st.close();
				evh.consume();
			});
			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
			vb.getChildren().addAll(gp, ok);
			st.show();
        	e.consume();
        });
        partsSpoilers.setOnAction(e -> {Stage st = new Stage();
			st.setTitle("Add spoilers");
			
			Label addTo = new Label("Add to kit(s) : ");
			TextField addToInput = new TextField("");
			CheckBox autosculptZones = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentKitASZones);
			
			autosculptZonesInput.setDisable(true);
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			GridPane gp = new GridPane();
			gp.add(addTo, 0, 1);
			gp.add(addToInput, 1, 1);
			gp.add(autosculptZones, 0, 2);
			gp.add(autosculptZonesInput, 1, 2);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");
			ok.setOnAction(evh -> {
        		currentKitASZones = autosculptZonesInput.getText();
				ArrayList<String> generateKits = new ArrayList<String>();
				for (String s : addToInput.getText().split(",")) {
					if (s.contains("-")) {
						for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
								i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
							if (i<10) generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
							else generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
						}
					} else if (!s.strip().isBlank()) generateKits.add(s.strip());
				}
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
				
				int size = mainDBMP.dBMPParts.size();
				for(int i=0; i<size; i++) {
					if (generateKits.contains(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value1) 
							&& ((AttributeCarPartID)mainDBMP.dBMPParts.get(i).getAttribute("PARTID_UPGRADE_GROUP")).ID == PartUndercover.SPOILER) {
					// remove conflicting preexisting
						mainDBMP.dBMPParts.remove(i);
						size--;
						i--;
					}
				}
				for (String kit : generateKits) {
						DBMPPart tp;
						mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, "SPOILER"));
						if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
				}
	        	updateAllPartsDisplay();
	        	partsDisplay.getSelectionModel().select(sel);
				st.close();
				evh.consume();
			});
			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
			vb.getChildren().addAll(gp, ok);
			st.show();
	    	e.consume();
        });
        partsRims.setOnAction(e -> {
        	Stage st = new Stage();
			st.setTitle("Add rims");
			
			Label addTo = new Label("Add to kit(s) : ");
			TextField addToInput = new TextField("");
			CheckBox autosculptZones = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField(currentKitASZones);
			
			autosculptZonesInput.setDisable(true);
			autosculptZones.setOnAction(evh -> {
				if(autosculptZones.isSelected()) autosculptZonesInput.setDisable(false); else autosculptZonesInput.setDisable(true);
				evh.consume();
			});
			GridPane gp = new GridPane();
			gp.add(addTo, 0, 1);
			gp.add(addToInput, 1, 1);
			gp.add(autosculptZones, 0, 2);
			gp.add(autosculptZonesInput, 1, 2);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			
			Button ok = new Button("OK");
			ok.setOnAction(evh -> {
        		currentKitASZones = autosculptZonesInput.getText();
				ArrayList<String> generateKits = new ArrayList<String>();
				for (String s : addToInput.getText().split(",")) {
					if (s.contains("-")) {
						for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
								i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
							if (i<10) generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
							else generateKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
						}
					} else if (!s.strip().isBlank()) generateKits.add(s.strip());
				}
				DBMPPart sel = partsDisplay.getSelectionModel().getSelectedItem();
				
				int size = mainDBMP.dBMPParts.size();
				for(int i=0; i<size; i++) {
					if (generateKits.contains(((AttributeTwoString)mainDBMP.dBMPParts.get(i).getAttribute("PART_NAME_OFFSETS")).value1) 
							&& (((AttributeCarPartID)mainDBMP.dBMPParts.get(i).getAttribute("PARTID_UPGRADE_GROUP")).ID == PartUndercover.WHEEL
							|| ((AttributeCarPartID)mainDBMP.dBMPParts.get(i).getAttribute("PARTID_UPGRADE_GROUP")).ID == PartUndercover.WHEEL_REAR)) {
					// remove conflicting preexisting
						mainDBMP.dBMPParts.remove(i);
						size--;
						i--;
					}
				}
				for (String kit : generateKits) {
						DBMPPart tp;
						mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, "WHEEL"));
						((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value2 = "WHEEL_TIRE_FRONT";
						if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						mainDBMP.dBMPParts.add(tp = new DBMPPart(kit, "WHEEL_REAR"));
						((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value2 = "WHEEL_TIRE_REAR";
						if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
				}
	        	updateAllPartsDisplay();
	        	partsDisplay.getSelectionModel().select(sel);
				st.close();
				evh.consume();
			});
			Scene sc = new Scene(vb);
			if (useDarkMode) sc.getRoot().setStyle("-fx-base:black");
			st.setScene(sc);
			st.setResizable(false);
			vb.getChildren().addAll(gp, ok);
			st.show();
        	e.consume();
        });
        
        if(debug) {
        	Button debugListCheckedParts = new Button("LIST CHECKED PARTS");
        	Button debugInfoCheckedParts = new Button("INFO ABOUT CHECKED PARTS");
        	Button debugDBMPDisplay = new Button("DISPLAY DBMP");
            shortcutsBar.getChildren().addAll(debugListCheckedParts, debugInfoCheckedParts, debugDBMPDisplay);
        	
        	debugListCheckedParts.setOnAction(e -> {
        		System.out.println("[DEBUG] CHECKED PARTS :");
                for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
                	System.out.println(p.displayName);
                }
        		e.consume();
        	});
        	debugInfoCheckedParts.setOnAction(e -> {
        		for(DBMPPart p: partsDisplay.getSelectionModel().getSelectedItems()) {
                	System.out.println("- "+p.displayName);
                	for (Attribute a:p.attributes) {
                		System.out.println(a);
                	}
                }
        		e.consume();
        	});
        	debugDBMPDisplay.setOnAction(e->{
        		System.out.println(mainDBMP);
        		e.consume();
        	});
        	
        }
        
        windowTop.getChildren().addAll(menuBar, shortcutsBar);
        
        partsDisplay = new ListView<DBMPPart>();
        partsDisplay.setCellFactory( lv -> {
        	 CarPartListCell cell = new CarPartListCell() {
                 @Override
                 protected void updateItem(DBMPPart item, boolean empty) {
                     super.updateItem(item, empty);
                 }
             };
             return cell;
        });
        partsDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        partsDisplay.setOnKeyPressed(e -> {
        	if (e.getCode().equals(KeyCode.DELETE)) deleteSelectedParts();
        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));

        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	
        	e.consume();
        });
        
//        attributesDisplay = new ListView<Attribute>();
//        attributesDisplay.setCellFactory( lv -> {
//        	  AttributeListCell cell = new AttributeListCell() {
//                 @Override
//                 protected void updateItem(Attribute item, boolean empty) {
//                     super.updateItem(item, empty);
//                 }
//             };
//             return cell;
//        });
//        attributesDisplay.setOnKeyPressed(e -> {
//        	if (e.getCode().equals(KeyCode.Z) && e.isControlDown()) Undo.undo();
//        	e.consume();
//        });
//        
        attributesDisplay = new VBox();
        ScrollPane scrollPaneAttrib = new ScrollPane(attributesDisplay);
        scrollPaneAttrib.setFitToWidth(true);
        
        
        for (DBMPPart p : mainDBMP.dBMPParts) {
        	partsDisplay.getItems().add(p);
        }

//        Label statusBar = new Label("Status: Ready");

        BorderPane root = new BorderPane();
        root.setTop(windowTop);
        root.setCenter(partsDisplay);
//        root.setRight(attributesDisplay);
        root.setRight(scrollPaneAttrib);
//        root.setBottom(statusBar);
        

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

    public static void updateAllPartsDisplay() { //broken for undo only for some weird reason
    	DBMPPart selected = partsDisplay.getSelectionModel().getSelectedItem();
    	mainDBMP.updateAll();
    	partsDisplay.getItems().clear();
    	for (DBMPPart p : mainDBMP.dBMPParts) {
       	partsDisplay.getItems().add(p);
    	}
    	//while we're at it
    	partsDisplay.getSelectionModel().select(selected);
    	DBMPPlus.attributesDisplay.getChildren().clear();
    	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
    }
    
    public static void deleteSelectedParts() {
		DBMPPart[] toDelete =  (DBMPPart[]) partsDisplay.getSelectionModel().getSelectedItems().toArray(new DBMPPart[0]);
		
		for(int i=0; i<toDelete.length; i++) {
			new UndoPartDelete(toDelete[i], DBMPPlus.mainDBMP.dBMPParts.indexOf(toDelete[i]));
			partsDisplay.getItems().remove(toDelete[i]);
    		if (DBMPPlus.debug) System.out.println("Part " + toDelete[i].displayName + " deleted");
    		DBMPPlus.mainDBMP.dBMPParts.remove(toDelete[i]);
		}
		for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
	}
}


class PartSorterByNameAsc implements Comparator<DBMPPart>{
	public int compare(DBMPPart a, DBMPPart b) {
		return (((AttributeTwoString)a.getAttribute("PART_NAME_OFFSETS")).value1 + "_" + ((AttributeTwoString)a.getAttribute("PART_NAME_OFFSETS")).value2)
				.compareTo((((AttributeTwoString)b.getAttribute("PART_NAME_OFFSETS")).value1 + "_" + ((AttributeTwoString)b.getAttribute("PART_NAME_OFFSETS")).value2));
	}
}

class PartSorterByName2Asc implements Comparator<DBMPPart>{
	public int compare(DBMPPart a, DBMPPart b) {
		String stringA = ((AttributeTwoString)a.getAttribute("LOD_BASE_NAME")).value2 + "0" + ((AttributeTwoString)a.getAttribute("LOD_BASE_NAME")).value1.replace("KIT0", "KIT").replace("KITW0", "KITW");
		String stringB = ((AttributeTwoString)b.getAttribute("LOD_BASE_NAME")).value2 + "0" + ((AttributeTwoString)b.getAttribute("LOD_BASE_NAME")).value1.replace("KIT0", "KIT").replace("KITW0", "KITW");

		//brake fix
		stringA = stringA.replace("BRAKE_FRONT", "BRAKE0FRONT").replace("BRAKE_REAR", "BRAKE0REAR");
		stringB = stringB.replace("BRAKE_FRONT", "BRAKE0FRONT").replace("BRAKE_REAR", "BRAKE0REAR");
		
		//doorhandles fix
		stringA = stringA.replace("DOORHANDLE", "DOOR_ZHANDLE").replace("HANDLE_REAR", "HANDLE_ZREAR");
		stringB = stringB.replace("DOORHANDLE", "DOOR_ZHANDLE").replace("HANDLE_REAR", "HANDLE_ZREAR");

		//exhausts fix
		stringA = stringA.replace("MUFFLER_", "EXHAUST0");
		stringB = stringB.replace("MUFFLER_", "EXHAUST0");
		
		return (stringA.compareTo(stringB));
	}
}