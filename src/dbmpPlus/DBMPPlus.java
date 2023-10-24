package dbmpPlus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import binstuff.Hash;
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

	public static ListView<Part> partsDisplay;
	public static VBox attributesDisplay;
	
	public static ArrayList<CarPartListCell> carPartListCells = new ArrayList<CarPartListCell>();

	public static boolean debug = false;

	private static Menu menuDBMP;

	//stuff to put in a config file
	public static String lastDirectoryLoaded = Paths.get("").toAbsolutePath().toString();
	public static String lastFileLoaded = "";
//	  public static String lastFileLoaded = "C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"
	public static String lastFileSaved = Paths.get("").toAbsolutePath().toString();
	public static boolean useDarkMode = false;
	public static boolean widebodyAutoCorrect = true;
	
	public static final String programName = "fire";
	public static final String programVersion = "indev";
	
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("fire.dat")));
			br.readLine(); //="DO NOT MANUALLY EDIT THIS FILE"
			lastDirectoryLoaded = br.readLine();
			lastFileLoaded = br.readLine();
			lastFileSaved = br.readLine();
			useDarkMode = Boolean.valueOf(br.readLine());
			widebodyAutoCorrect = Boolean.valueOf(br.readLine());
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
					+ Boolean.toString(widebodyAutoCorrect) + "\n");
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
        MenuItem fileGenerateNew = new MenuItem("Generate new");
        MenuItem fileSave = new MenuItem("Save");
        MenuItem fileExit = new MenuItem("Exit");
        
        fileLoad.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
		        ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to load a new DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure)) {
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
						new Alert(Alert.AlertType.INFORMATION, "Database loaded successfully.", ButtonType.OK).show();
					} else {
//						new Alert(Alert.AlertType.INFORMATION, "Nothing to load", ButtonType.OK).show();
					}
				}
			}
        });
        fileLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileNew.setOnAction(e -> {
        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create a blank DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {
				mainDBMP = new DBMP();
				updateAllPartsDisplay();
				primaryStage.setTitle("fire - "+mainDBMP.carname.label);
				menuDBMP.setText(mainDBMP.carname.label);
			}
        });
        fileGenerateNew.setOnAction(e -> {
//        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to create a new DBMP ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
//			if (ButtonType.YES.equals(sure)) {
//				
//			}
        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
        });
        fileGenerateNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        fileSave.setOnAction(e -> {
        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
        });
        fileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        fileExit.setOnAction(e -> {
        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (ButtonType.YES.equals(sure)) {
				primaryStage.close();
			}
        });
        
        menuFile.getItems().addAll(fileLoad, fileNew, fileGenerateNew, fileSave, fileExit);

        
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

        MenuItem dbSortPartsByName = new MenuItem("Sort parts by name");
        MenuItem dbSortAttributes = new MenuItem("Sort attributes");
//        CheckMenuItem dbSortPartsByNameExport = new CheckMenuItem("Sort parts by name at export");
//        CheckMenuItem dbSortAttributesExport = new CheckMenuItem("Sort attributes at export");
        MenuItem dbChangeName = new MenuItem("Change car name");
        MenuItem dbFixNameOffsets = new MenuItem("Fix NAME_OFFSETS based on the PART_NAME_OFFSETS");
        MenuItem dbFixCVs = new MenuItem("Fix CVs for BODY parts based on the car's name");
        
        dbSortPartsByName.setOnAction(e -> {
        	mainDBMP.parts.sort(new PartSorterByNameAsc());
        	new Alert(Alert.AlertType.INFORMATION, "All parts sorted alphabetically.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });
        dbSortAttributes.setOnAction(e -> {
        	for(Part p: mainDBMP.parts) {
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
        	new Alert(Alert.AlertType.INFORMATION, "All attributes sorted.").show();
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
                	new Alert(Alert.AlertType.INFORMATION, "Car name changed.").show();
        			primaryStage.setTitle("fire - "+mainDBMP.carname.label);
        			menuDBMP.setText(mainDBMP.carname.label);
        			e.consume();
            	}
        	}catch (NoSuchElementException ex) {
        	}
        });
        dbFixNameOffsets.setOnAction(e -> {
        	for (Part p : mainDBMP.parts) {
        		((AttributeString)p.getAttribute("NAME_OFFSET")).value1 = ((AttributeTwoString)p.getAttribute("PART_NAME_OFFSETS")).value1.replace("KIT", "").replace("W", "");
        	}
        	new Alert(Alert.AlertType.INFORMATION, "All name offsets fixed.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });
        dbFixCVs.setOnAction(e -> {
        	for (Part p : mainDBMP.parts) {
        		if (((AttributeCarPartID)p.getAttribute("PARTID_UPGRADE_GROUP")).ID == PartUndercover.BODY) {
        			if (((AttributeKey)p.getAttribute("CV")) == null) p.addAttribute(new AttributeKey("CV", DBMPPlus.mainDBMP.carname.label + "_CV"));
        			else ((AttributeKey)p.getAttribute("CV")).value = new Hash(DBMPPlus.mainDBMP.carname.label + "_CV");
        		}
        	}
        	new Alert(Alert.AlertType.INFORMATION, "All CVs fixed.").show();
        	updateAllPartsDisplay();
        	e.consume();
        });
        
        menuDBMP.getItems().addAll(dbSortPartsByName, dbSortAttributes, dbFixNameOffsets, dbFixCVs, dbChangeName/*, dbSortPartsByNameExport, dbSortAttributesExport*/);
        
        
        Menu menuAttributes = new Menu("Attributes");
        
        MenuItem attributeAddAS = new MenuItem("Add MORPHTARGET_NUM");
        MenuItem attributeRemoveAS = new MenuItem("Remove MORPHTARGET_NUM");
        MenuItem attributeAddCV = new MenuItem("Add CV (for BODY only)");
        MenuItem attributeRemoveCV = new MenuItem("Remove CV (for BODY only)");
        MenuItem attributeSort = new MenuItem("Sort");

        attributeAddAS.setOnAction(e -> {
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
        		if (p.getAttribute("MORPHTARGET_NUM") == null) {
        			p.addAttribute(new AttributeInteger("MORPHTARGET_NUM", 11));
        			p.update();
        		}
        	}
        	DBMPPlus.attributesDisplay.getChildren().clear();
        	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
        	e.consume();
        });
        attributeRemoveAS.setOnAction(e -> {
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
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
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
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
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
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
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
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
        CheckMenuItem settingsWidebodyAutoCorrect = new CheckMenuItem("Auto-correct widebody part names");
        MenuItem settingsAbout = new MenuItem("About " + programName + "...");
        
        if (widebodyAutoCorrect) settingsWidebodyAutoCorrect.setSelected(true);
        settingsWidebodyAutoCorrect.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
        	if (isSelected) widebodyAutoCorrect = true; 
        	else widebodyAutoCorrect = false;
        });
        
        
        settingsAbout.setOnAction(e -> {
			Stage st = new Stage();
			st.setTitle("About " + programName);
			Scene sc = new Scene(new Label(programName + " version " + programVersion + "\n\n"
					+ "Advanced DBModelParts editor for NFS Undercover.\n"
					+ "Aims to make the creation of such data less of a hassle for modders.\n"
					+ "This software has been originally created for the mod Undercover Exposed.\n\n"
					+ "Not affiliated with EA, MaxHwoy, nfsu360, etc.\n\n"
					+ "NI240SX 2023 - No rights reserved"));
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
        
        menuSettings.getItems().addAll(settingsDark, settingsWidebodyAutoCorrect, settingsAbout);
        
        
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
        Button kitSavePreset = new Button("Save kit preset...");
        Button kitDelete = new Button("Delete kit");

        Separator infSep = new Separator();
        HBox.setHgrow(infSep, Priority.ALWAYS);
        shortcutsBar.getChildren().addAll(partAdd, partCopy, partCopyAdvanced, partDelete, new Separator(), kitGenerate, kitCopy, kitExhausts, kitSavePreset, kitDelete);

        partAdd.setOnAction(e-> {
        	TextInputDialog td = new TextInputDialog("KIT00_BASE");
        	td.setHeaderText("Enter a part name");
        	try {
        		String s = td.showAndWait().get().strip();
        		if (!s.isBlank()) {
        			String kit = s.split("_")[0];
        			String part = s.substring(kit.length()+1);
        			if (part.contains("FRONT")) {
        				mainDBMP.parts.add(new Part(kit, part));
        				mainDBMP.parts.add(new Part(kit, part.replace("FRONT", "REAR")));
        				if(part.contains("LEFT")) {
            				mainDBMP.parts.add(new Part(kit, part.replace("LEFT", "RIGHT")));
            				mainDBMP.parts.add(new Part(kit, part.replace("FRONT", "REAR").replace("LEFT", "RIGHT")));
        				}
        			} else {
        				if(part.contains("LEFT")) {
            				mainDBMP.parts.add(new Part(kit, part));
            				mainDBMP.parts.add(new Part(kit, part.replace("LEFT", "RIGHT")));
            				if(part.contains("LIGHT")) {
            					mainDBMP.parts.add(new Part(kit, part.replace("LIGHT", "LIGHT_GLASS")));
                				mainDBMP.parts.add(new Part(kit, part.replace("LIGHT", "LIGHT_GLASS").replace("LEFT", "RIGHT")));	
            				}
        				} else {
            				mainDBMP.parts.add(new Part(kit, part));
        				}
        			}
        			updateAllPartsDisplay();
        			e.consume();
            	}
        	}catch (NoSuchElementException ex) {
        	}
        });
        partCopy.setOnAction(e -> {
        	Part sel = partsDisplay.getSelectionModel().getSelectedItem();
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
            	Part p2 = new Part(p);
            	mainDBMP.parts.add(mainDBMP.parts.indexOf(p)+1, p2);
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
			TextField copyToInput = new TextField("KITW01, KIT03-KIT04");
			
			CheckBox toAnotherKitReference = new CheckBox("Point to model parts from kit : ");
			TextField referenceKitInput = new TextField("KIT00");
			CheckBox autosculptZones = new CheckBox("Harmonize Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField("11");
			
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
				Part sel = partsDisplay.getSelectionModel().getSelectedItem();
	        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
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
	            			Part tp;
		            		mainDBMP.parts.add(tp = new Part(p));
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
		            	Part tp;
		            	mainDBMP.parts.add(mainDBMP.parts.indexOf(p)+1, tp = new Part(p));
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
        	//TODO
        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
        	//load parts from the stuff in .\\kits\\
        	//and think to add exhausts
        	//for generic dbmp generation, pay attention to spoilers and hoods that are already included in kits
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
			TextField copyToInput = new TextField("KITW01, KIT03-KIT04");
			CheckBox autosculptZones = new CheckBox("Harmonize Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField("11");
			
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
				
				Part sel = partsDisplay.getSelectionModel().getSelectedItem();
	        	for(int i=0; i<mainDBMP.parts.size(); i++) { //looping on Part p: mainDBMP.parts throws an exception for whatever reason
	        		if (((AttributeTwoString)mainDBMP.parts.get(i) .getAttribute("PART_NAME_OFFSETS")).value1.equals(copyFromKit)) {
		        		for(String s : copyToKits) {
		        			Part p2 = new Part(mainDBMP.parts.get(i));
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
			            	mainDBMP.parts.add(p2);
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
				workOnKitsInput = new TextField("KIT00");
			}
			Label range = new Label("Range of exhausts to add : ");
			TextField rangeInput = new TextField("00-05");
			Label partsToGen = new Label("Parts to generate");
			Label muffler = new Label("Main part : ");
			TextField mufflerInput = new TextField("MUFFLER");
			CheckBox rightTips = new CheckBox("Right tips : ");
			TextField rightTipsInput = new TextField("EXHAUST_TIPS_RIGHT");
			CheckBox leftTips = new CheckBox("Left tips : ");
			TextField leftTipsInput = new TextField("EXHAUST_TIPS_LEFT");
			CheckBox centerTips = new CheckBox("Center tips : ");
			TextField centerTipsInput = new TextField("EXHAUST_TIPS_CENTER");

			CheckBox pointToKit = new CheckBox("Point to model kit : ");
			TextField pointToKitInput = new TextField("KIT00");
			CheckBox autosculptZones = new CheckBox("Add Autosculpt zones : ");
			TextField autosculptZonesInput = new TextField("11");
			CheckBox removePreExisting = new CheckBox("Remove pre-existing exhausts");

			rightTips.setSelected(true);
			rightTips.setOnAction(evh -> {
				if(rightTips.isSelected()) rightTipsInput.setDisable(false); else rightTipsInput.setDisable(true);
				evh.consume();
			});
			leftTips.setSelected(true);
			leftTips.setOnAction(evh -> {
				if(leftTips.isSelected()) leftTipsInput.setDisable(false); else leftTipsInput.setDisable(true);
				evh.consume();
			});
			centerTips.setSelected(true);
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
				
				Part sel = partsDisplay.getSelectionModel().getSelectedItem();
	        	
				if(removePreExisting.isSelected()) {
					int size = mainDBMP.parts.size();
					for(int i=0; i<size; i++) {
						if (kits.contains(((AttributeTwoString)mainDBMP.parts.get(i).getAttribute("PART_NAME_OFFSETS")).value1) 
								&& ((AttributeCarPartID)mainDBMP.parts.get(i).getAttribute("PARTID_UPGRADE_GROUP")).ID.getText().contains("EXHAUST")) {
							mainDBMP.parts.remove(i);
							size--;
							i--;
						}
					}
				}

				for (String kit : kits) {
					for (String exh : exhausts) {
						Part tp;
						mainDBMP.parts.add(tp = new Part(kit, mufflerInput.getText().strip() + "_" + exh));
						((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST;
						if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
						if(autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						
						if (leftTips.isSelected()) {
							mainDBMP.parts.add(tp = new Part(kit, leftTipsInput.getText().strip() + "_" + exh));
							((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_LEFT;
							if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
							if (autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						}
						if (rightTips.isSelected()) {
							mainDBMP.parts.add(tp = new Part(kit, rightTipsInput.getText().strip() + "_" + exh));
							((AttributeCarPartID)tp.getAttribute("PARTID_UPGRADE_GROUP")).ID = PartUndercover.EXHAUST_TIPS_RIGHT;
							if (pointToKit.isSelected()) ((AttributeTwoString)tp.getAttribute("LOD_BASE_NAME")).value1 = pointToKitInput.getText().strip();
							if (autosculptZones.isSelected()) tp.attributes.add(new AttributeInteger("MORPHTARGET_NUM", Integer.parseInt(autosculptZonesInput.getText().strip())));
						}
						if (centerTips.isSelected()) {
							mainDBMP.parts.add(tp = new Part(kit, centerTipsInput.getText().strip() + "_" + exh));
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
        kitSavePreset.setOnAction(e -> {
        	//TODO
        	new Alert(Alert.AlertType.WARNING, "Not implemented").show();
        });
        kitDelete.setOnAction(e -> {
        	if (((AttributeTwoString)partsDisplay.getSelectionModel().getSelectedItem().getAttribute("PART_NAME_OFFSETS"))!=null) {
	        	String kit = ((AttributeTwoString)partsDisplay.getSelectionModel().getSelectedItem().getAttribute("PART_NAME_OFFSETS")).value1;
	        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to delete all parts from " + kit + " ?", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure)) {
					int size = mainDBMP.parts.size();
					for(int i=0; i<size; i++) {
						if (kit.equals(((AttributeTwoString)mainDBMP.parts.get(i).getAttribute("PART_NAME_OFFSETS")).value1)) {
							mainDBMP.parts.remove(i);
							size--;
							i--;
						}
					}
		        	updateAllPartsDisplay();
				}
        	}
        });
        
        if(debug) {
        	Button debugListCheckedParts = new Button("LIST CHECKED PARTS");
        	Button debugInfoCheckedParts = new Button("INFO ABOUT CHECKED PARTS");
        	Button debugDBMPDisplay = new Button("DISPLAY DBMP");
            shortcutsBar.getChildren().addAll(debugListCheckedParts, debugInfoCheckedParts, debugDBMPDisplay);
        	
        	debugListCheckedParts.setOnAction(e -> {
        		System.out.println("[DEBUG] CHECKED PARTS :");
                for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
                	System.out.println(p.displayName);
                }
        		e.consume();
        	});
        	debugInfoCheckedParts.setOnAction(e -> {
        		for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
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
        
        partsDisplay = new ListView<Part>();
        partsDisplay.setCellFactory( lv -> {
        	 CarPartListCell cell = new CarPartListCell() {
                 @Override
                 protected void updateItem(Part item, boolean empty) {
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
        
        
        for (Part p : mainDBMP.parts) {
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
        	ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to exit ? Any unsaved changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
			if (!ButtonType.YES.equals(sure)) {
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
    	Part selected = partsDisplay.getSelectionModel().getSelectedItem();
    	mainDBMP.updateAll();
    	partsDisplay.getItems().clear();
    	for (Part p : mainDBMP.parts) {
       	partsDisplay.getItems().add(p);
    	}
    	//while we're at it
    	partsDisplay.getSelectionModel().select(selected);
    	DBMPPlus.attributesDisplay.getChildren().clear();
    	if (partsDisplay.getSelectionModel().getSelectedItem()!=null) for (Attribute a: partsDisplay.getSelectionModel().getSelectedItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
    }
    
    public static void deleteSelectedParts() {
		Part[] toDelete =  (Part[]) partsDisplay.getSelectionModel().getSelectedItems().toArray(new Part[0]);
		
		for(int i=0; i<toDelete.length; i++) {
			new UndoPartDelete(toDelete[i], DBMPPlus.mainDBMP.parts.indexOf(toDelete[i]));
			partsDisplay.getItems().remove(toDelete[i]);
    		if (DBMPPlus.debug) System.out.println("Part " + toDelete[i].displayName + " deleted");
    		DBMPPlus.mainDBMP.parts.remove(toDelete[i]);
		}
		for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
	}
}


class PartSorterByNameAsc implements Comparator<Part>{
	public int compare(Part a, Part b) {
		return (((AttributeTwoString)a.getAttribute("PART_NAME_OFFSETS")).value1 + "_" + ((AttributeTwoString)a.getAttribute("PART_NAME_OFFSETS")).value2)
				.compareTo((((AttributeTwoString)b.getAttribute("PART_NAME_OFFSETS")).value1 + "_" + ((AttributeTwoString)b.getAttribute("PART_NAME_OFFSETS")).value2));
	}
}