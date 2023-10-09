package dbmpPlus;

import java.io.File;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DBMPPlus extends Application {
	
	public static DBMP mainDBMP = new DBMP();
	
	public static VBox partsDisplay;
	public static VBox attributesDisplay;

	public static void main(String[] args) {
		/*
		System.out.println(new AttributeTwoString("PART_NAME_OFFSETS"));
		System.out.println(new AttributeCarPartID("PARTID_UPGRADE_GROUP"));
		System.out.println(new AttributeKey("CV"));
		
		Part p = new Part();
		p.attributes.add(new AttributeTwoString("PART_NAME_OFFSETS","KIT00","BUMPER_FRONT"));
		p.attributes.add(new AttributeTwoString("LOD_BASE_NAME","KIT00","BUMPER_FRONT"));
		System.out.println(p);
		p.update();
		System.out.println(p);
		((AttributeTwoString)p.editAttribute("PART_NAME_OFFSETS")).value2 = "BUMP";
		System.out.println(p);
		p.update();
		System.out.println(p);

		Part p2 = new Part();
		System.out.println(p2);
		p2.addAttribute(new AttributeTwoString("PART_NAME_OFFSETS","KIT00","BODY"));
		System.out.println(p2);
		p2.addAttribute(new AttributeTwoString("LOD_BASE_NAME","KIT00","BODY"));
		System.out.println(p2);
		((AttributeTwoString)p2.editAttribute("PART_NAME_OFFSETS")).value1 = "KIT11";
		p2.update();
		System.out.println(p2);
		
		DBMP testDBMP = new DBMP("NIS_240_SX_89");
		testDBMP.parts.add(p);
		testDBMP.parts.add(p2);
		System.out.println(testDBMP); */

		//DBMP loadTest = DBMP.loadDBMP(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\dbmp step 8.bin"));
		/*
		DBMP loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\LOT_ELI_111_06.bin"));
		System.out.println(loadTest.displayName());
		loadTest.saveToFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\DBMP EXPORT LOT_ELI_111_06.bin"));
		
		loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\MIT_EVO_X_08.bin"));
		loadTest.saveToFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\DBMP EXPORT MIT_EVO_X_08.bin"));
		
		loadTest = loadDBMP(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\BMW_M3_E92_08.bin"));
		loadTest.saveToFile(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\DBMP EXPORT BMW_M3_E92_08.bin")); */

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
				mainDBMP = DBMP.loadDBMP(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\voitures\\z done\\car bmw e92\\dbmp step 8.bin"));
				System.out.println(mainDBMP);
				updateAllPartsDisplay();
			}
        });
        fileMenu.getItems().addAll(fileLoad, fileExit);
        menuBar.getMenus().addAll(fileMenu);

        // Custom Shortcuts Bar (You can expand this as needed)
        HBox shortcutsBar = new HBox();
        shortcutsBar.getChildren().addAll(new Button("Shortcut 1"), new Button("Shortcut 2"));

        windowTop.getChildren().addAll(menuBar, shortcutsBar);
        
        // Car parts zone
        partsDisplay = new VBox();
        ScrollPane scrollPaneParts = new ScrollPane(partsDisplay);
        scrollPaneParts.setFitToWidth(true);
        scrollPaneParts.setFitToHeight(true);
        
        attributesDisplay = new VBox();
        ScrollPane scrollPaneAttrib = new ScrollPane(attributesDisplay);
        scrollPaneAttrib.setFitToWidth(true);
        scrollPaneAttrib.setFitToHeight(true);

        // Add sample items to the special zone (you can dynamically add items later)
        for (Part p : mainDBMP.parts) {
            partsDisplay.getChildren().add(p);
        }

        // Status Bar
        Label statusBar = new Label("Status: Ready");

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(windowTop);
        root.setCenter(scrollPaneParts);
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
    	partsDisplay.getChildren().clear();
    	for (Part p : mainDBMP.parts) {
    		partsDisplay.getChildren().add(p);
    	}
    }
}