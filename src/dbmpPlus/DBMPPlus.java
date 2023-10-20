package dbmpPlus;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DBMPPlus extends Application {
	
	public static DBMP mainDBMP = new DBMP();
	
//	public static VBox partsDisplay;
	public static ListView<Part>  partsDisplay;
	public static VBox attributesDisplay;
	
	public static ArrayList<CarPartListCell> carPartListCells = new ArrayList<CarPartListCell>();

	public static boolean debug = true;
	
	
	public static void main(String[] args) {

		//if wrong that will throw an exception due to trying to spawn an error window without having initialized javafx 
		mainDBMP = DBMP.loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"));
//		mainDBMP = DBMP.loadDBMP(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\dbmp step 8.bin"));
        launch(args);
	}
	

    public void start(Stage primaryStage) {
        primaryStage.setTitle("fire");

        VBox windowTop = new VBox();
        
        // MenuBar
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        
        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(e -> primaryStage.close());
        MenuItem fileLoad = new MenuItem("Load");
        fileLoad.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
		        ButtonType sure = new Alert(Alert.AlertType.INFORMATION, "Are you sure you want to load a new DBMP ? Any changes will be lost.", ButtonType.NO, ButtonType.YES).showAndWait().orElse(ButtonType.NO);
				if (ButtonType.YES.equals(sure)) {
					FileChooser fc = new FileChooser();
//					fc.setInitialDirectory(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\"));
					fc.setInitialDirectory(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\"));
//					fc.setInitialDirectory(new File(Paths.get(".").toAbsolutePath().toString()));
					fc.setInitialFileName("DBModelParts.bin");
					fc.getExtensionFilters().addAll(
				        new FileChooser.ExtensionFilter("BIN files", "*.bin"),
				        new FileChooser.ExtensionFilter("All files", "*.*"));
					fc.setTitle("Load an existing DBModelParts");
					DBMP loadDBMP;
					if ((loadDBMP = DBMP.loadDBMP(fc.showOpenDialog(null)))!=null) {
						mainDBMP = loadDBMP;
//						  System.out.println(mainDBMP);
						updateAllPartsDisplay();
						new Alert(Alert.AlertType.INFORMATION, "Database loaded successfully", ButtonType.OK).show();
					} else {
//						new Alert(Alert.AlertType.INFORMATION, "Nothing to load", ButtonType.OK).show();
					}
				}
			}
        });
        menuFile.getItems().addAll(fileLoad, fileExit);

        Menu menuEdit = new Menu("Edit");
        
        MenuItem editUndo = new MenuItem("Undo");
        editUndo.setOnAction(e -> {
        	Undo.undo();
        });
        menuEdit.getItems().addAll(editUndo);
        
        menuBar.getMenus().addAll(menuFile, menuEdit);

        HBox shortcutsBar = new HBox();
        Button buttonCopy = new Button("Copy");
        Button buttonCopyAdvanced = new Button("Copy...");
        Button buttonDelete = new Button("Delete");
        shortcutsBar.getChildren().addAll(buttonCopy, buttonCopyAdvanced, buttonDelete);

        buttonCopy.setOnAction(e -> {
        	Part sel = partsDisplay.getSelectionModel().getSelectedItem();
        	for(Part p: partsDisplay.getSelectionModel().getSelectedItems()) {
            	Part p2 = new Part(p);
            	mainDBMP.parts.add(mainDBMP.parts.indexOf(p)+1, p2);
            }
        	updateAllPartsDisplay();
        	partsDisplay.getSelectionModel().select(sel);
        	e.consume();
        });
        
        buttonCopyAdvanced.setOnAction(e -> {
        	Stage st = new Stage();
			st.setTitle("Advanced copying");
			
			Label label = new Label("not implemented");
			
			HBox hb = new HBox();
			hb.getChildren().addAll(label);
			
			Scene sc = new Scene(hb);
			st.setScene(sc);
			st.setResizable(false);
			st.show();
			
        	e.consume();
        });
        
        buttonDelete.setOnAction(e -> {
        	deleteSelectedParts();
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
        
        // Car parts zone
//        partsDisplay = new VBox();
//        ScrollPane scrollPaneParts = new ScrollPane(partsDisplay);
//        scrollPaneParts.setFitToWidth(true);
//        scrollPaneParts.setFitToHeight(true);

        partsDisplay = new ListView<Part>();
        partsDisplay.setCellFactory( lv -> {//new Callback<ListView<Part>, ListCell<Part>>() {
        	 CarPartListCell cell = new CarPartListCell() {
                 @Override
                 protected void updateItem(Part item, boolean empty) {
                     super.updateItem(item, empty);
//                     setText(item);
                 }
             };
             return cell;
//            @Override
//            public CarPartListCell call(ListView<Part> listView) {
//                return new CarPartListCell();
//            }
        });
        
        partsDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        partsDisplay.setOnKeyPressed(e -> {
        	if (e.getCode().equals(KeyCode.DELETE)) {
        		deleteSelectedParts();
        	}
        	if (e.getCode().equals(KeyCode.Z) && e.isControlDown()) Undo.undo();
        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
        	e.consume();
        });
        
        
        attributesDisplay = new VBox();
        ScrollPane scrollPaneAttrib = new ScrollPane(attributesDisplay);
        scrollPaneAttrib.setFitToWidth(true);
        scrollPaneAttrib.setFitToHeight(true);

        for (Part p : mainDBMP.parts) {
//            partsDisplay.getChildren().add(p);
        	partsDisplay.getItems().add(p);
        	
        }

        // Status Bar
        Label statusBar = new Label("Status: Ready");

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(windowTop);
//        root.setCenter(scrollPaneParts);
        root.setCenter(partsDisplay);
        root.setRight(scrollPaneAttrib);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Create a custom list item with a checkbox, name, and editable text field
/*    private HBox createListItem(String itemName) {
        CheckBox checkBox = new CheckBox();
        TextField textField = new TextField(itemName);

        HBox listItem = new HBox(10);
        listItem.getChildren().addAll(checkBox, textField);

        return listItem;
    }*/
	
    public static void updateAllPartsDisplay() {
    	partsDisplay.getItems().clear();
    	for (Part p : mainDBMP.parts) {
       	partsDisplay.getItems().add(p);
       	
       }
    	
    	
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
/*    	partsDisplay.getChildren().clear();
    	for (Part p : mainDBMP.parts) {
    		partsDisplay.getChildren().add(p);
    	}
    }*/
}