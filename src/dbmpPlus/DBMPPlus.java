package dbmpPlus;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DBMPPlus extends Application {
	
	public static DBMP mainDBMP = new DBMP();
	
//	public static VBox partsDisplay;
	public static ListView<Part>  partsDisplay;
	public static VBox attributesDisplay;
	
	public static ArrayList<CarPartListCell> carPartListCells = new ArrayList<CarPartListCell>();

	public static void main(String[] args) {

		mainDBMP = DBMP.loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"));
        launch(args);
	}
	

    public void start(Stage primaryStage) {
        primaryStage.setTitle("fire");

        VBox windowTop = new VBox();
        
        // MenuBar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(e -> primaryStage.close());
        MenuItem fileLoad = new MenuItem("Load");
        fileLoad.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
				//TODO proper file dialog lol
//				mainDBMP = DBMP.loadDBMP(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\dbmp step 8.bin"));
				mainDBMP = DBMP.loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"));
//				System.out.println(mainDBMP);
				updateAllPartsDisplay();
			}
        });
        fileMenu.getItems().addAll(fileLoad, fileExit);
        menuBar.getMenus().addAll(fileMenu);

        // Custom Shortcuts Bar (You can expand this as needed)
        HBox shortcutsBar = new HBox();
        Button buttonCopyPart = new Button("Copy");
        Button buttonCopyPartAdvanced = new Button("Copy...");
        shortcutsBar.getChildren().addAll(buttonCopyPart, buttonCopyPartAdvanced, new Button("Shortcut 2"));

        buttonCopyPart.setOnAction(e -> {
        	System.out.println("not implemented");
        	e.consume();
        });
        buttonCopyPartAdvanced.setOnAction(e -> {
        	System.out.println("not implemented");
        	e.consume();
        });
        
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
        
        
        attributesDisplay = new VBox();
        ScrollPane scrollPaneAttrib = new ScrollPane(attributesDisplay);
        scrollPaneAttrib.setFitToWidth(true);
        scrollPaneAttrib.setFitToHeight(true);

        // Add sample items to the special zone (you can dynamically add items later)
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
	
    public void updateAllPartsDisplay() {
    	partsDisplay.getItems().clear();
    	for (Part p : mainDBMP.parts) {
       	partsDisplay.getItems().add(p);
       	
       }
    }
/*    	partsDisplay.getChildren().clear();
    	for (Part p : mainDBMP.parts) {
    		partsDisplay.getChildren().add(p);
    	}
    }*/
}