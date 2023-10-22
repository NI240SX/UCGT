package dbmpPlus;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class CarPartListCell extends ListCell<Part> {
    CheckBox checkBox;
    Label partDisplayName;
    Label autosculptMorphZonesLabel;
    TextField autosculptMorph;
    BorderPane cellPane = new BorderPane();
    HBox asHBox;
    HBox partHBox;

	public static Part previousPartClicked = null;

    public CarPartListCell() {
        checkBox = new CheckBox();
        partDisplayName = new Label();
        autosculptMorphZonesLabel = new Label("Autosculpt zones : ");
        autosculptMorph = new TextField();
        DBMPPlus.carPartListCells.add(this);

        asHBox = new HBox(10, autosculptMorphZonesLabel, autosculptMorph);
        partHBox = new HBox(10, checkBox, partDisplayName);
        cellPane.setLeft(partHBox);
//        cellPane.setRight(asHBox);
        autosculptMorph.setMaxWidth(40);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        
        this.setOnMouseClicked(e -> {
            if (!this.isEmpty()) {
            	//Attributes display
//            	DBMPPlus.attributesDisplay.getItems().clear();
//            	for (Attribute a: this.getItem().attributes) DBMPPlus.attributesDisplay.getItems().add(a);
            	DBMPPlus.attributesDisplay.getChildren().clear();
            	for (Attribute a: this.getItem().attributes) DBMPPlus.attributesDisplay.getChildren().add(a.dataHBox);
            	
				for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
				e.consume();
            }
        });
        
        this.setOnScroll(e -> {
        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
        });
        this.setOnMouseMoved(e -> {
        	if (!e.isShiftDown()) for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
        });
        
        autosculptMorph.setOnAction(e -> {
        	getItem().getAttributeInteger("MORPHTARGET_NUM").value = Integer.valueOf(autosculptMorph.getText());
        	if (DBMPPlus.debug) System.out.println("Part " + getItem() + " morphtarget number changed to " + getItem().getAttributeInteger("MORPHTARGET_NUM").value);
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
//            if (getItem().getAttributeInteger("MORPHTARGET_NUM") == null) {
//            	autosculptMorph.setText("0");
//            	autosculptMorph.setDisable(true);
//            } else {
//            	autosculptMorph.setDisable(false);
//            	autosculptMorph.setText(Integer.toString(getItem().getAttributeInteger("MORPHTARGET_NUM").value));
//            }
            setGraphic(cellPane);
        }
    }
}