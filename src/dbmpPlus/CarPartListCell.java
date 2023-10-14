package dbmpPlus;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class CarPartListCell extends ListCell<Part> {
    CheckBox checkBox;
    Label partDisplayName;
    Label autosculptMorphZonesLabel;
    TextField autosculptMorph;
//    HBox hbox;
    BorderPane cellPane;
    HBox asHBox;
    HBox partHBox;
    

    public CarPartListCell() {
        checkBox = new CheckBox();
        partDisplayName = new Label();
        autosculptMorphZonesLabel = new Label("Autosculpt zones : ");
        autosculptMorph = new TextField();
        DBMPPlus.carPartListCells.add(this);

//        hbox = new HBox(10, checkBox, partDisplayName, autosculptMorphZonesLabel, autosculptMorph);
//        hbox.setPrefWidth(USE_COMPUTED_SIZE);
//        HBox.setHgrow(autosculptMorph, Priority.ALWAYS);
        asHBox = new HBox(10, autosculptMorphZonesLabel, autosculptMorph);
        partHBox = new HBox(10, checkBox, partDisplayName);
        cellPane = new BorderPane();
        cellPane.setLeft(partHBox);
        cellPane.setRight(asHBox);
        autosculptMorph.setMaxWidth(40);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        
        this.setOnMouseClicked(e -> {
            if (!this.isEmpty()) {
            	//Attributes display
            	DBMPPlus.attributesDisplay.getChildren().clear();
				for (Attribute a : getItem().attributes) DBMPPlus.attributesDisplay.getChildren().addAll(a);
				
				//Multiselect
				if (!e.isControlDown()) {
					for (CarPartListCell c : DBMPPlus.carPartListCells) c.checkBox.setSelected(false);
				}
				if (!checkBox.isSelected()) checkBox.setSelected(true);
				else checkBox.setSelected(false);
               e.consume();
            }
        });
        
        this.setOnKeyPressed(e -> {
        	//TODO doesnt worj
        	if (e.getCode().equals(KeyCode.DELETE)) {
        		System.out.println("Part " + getItem() + " deleted");
        		DBMPPlus.mainDBMP.parts.remove(getItem());
        	}
        	e.consume();
        });
        
        autosculptMorph.setOnAction(e -> {
        	getItem().getAttributeInteger("MORPHTARGET_NUM").value = Integer.valueOf(autosculptMorph.getText());
        	System.out.println("Part " + getItem() + " morphtarget number changed to " + getItem().getAttributeInteger("MORPHTARGET_NUM").value);
        	e.consume();
        });
        
    }

    @Override
    protected void updateItem(Part item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            partDisplayName.setText(item.displayName);
            if (item.getAttributeInteger("MORPHTARGET_NUM") == null) {
            	autosculptMorph.setText("0");
            	autosculptMorph.setDisable(true);
            } else {
            	autosculptMorph.setDisable(false);
            	autosculptMorph.setText(Integer.toString(item.getAttributeInteger("MORPHTARGET_NUM").value));
            }
            setGraphic(cellPane);
        }
    }
    
    protected void update() {
        if (getItem() == null) {
            setGraphic(null);
        } else {
            partDisplayName.setText(getItem().displayName);
            if (getItem().getAttributeInteger("MORPHTARGET_NUM") == null) {
            	autosculptMorph.setText("0");
            	autosculptMorph.setDisable(true);
            } else {
            	autosculptMorph.setDisable(false);
            	autosculptMorph.setText(Integer.toString(getItem().getAttributeInteger("MORPHTARGET_NUM").value));
            }
            setGraphic(cellPane);
        }
    }
}