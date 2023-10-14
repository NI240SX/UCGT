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

	public static Part previousPartClicked = null;

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
				
//				//Multiselect
//				if (e.isShiftDown()) {
//					if(previousPartClicked != null) {
//						int index0 = DBMPPlus.mainDBMP.parts.indexOf(previousPartClicked);
//						int index1 = DBMPPlus.mainDBMP.parts.indexOf(this.getItem());
//						if(index1<index0) {
//							index0=index1;
//							index1=DBMPPlus.mainDBMP.parts.indexOf(previousPartClicked);
//						}
//						if(!this.getItem().selected) {
//							for(int i=index0; i<index1+1; i++) DBMPPlus.mainDBMP.parts.get(i).selected = true;
//						} else {
//							for(int i=index0; i<index1+1; i++) DBMPPlus.mainDBMP.parts.get(i).selected = false;
//						}
//					}
//				} else if (!e.isControlDown()) {
//					for (Part p : DBMPPlus.mainDBMP.parts) p.selected = false;
//				}
				
				//link with the checkbox
				if (!checkBox.isSelected()) {
//					this.getItem().selected = true;
//					previousPartClicked = this.getItem();
				}
				else {
//					this.getItem().selected = false;
				}

				//checkbox display updating
//				for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(c.getItem().selected);

				for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
				e.consume();
            }
        });
        
        this.setOnScroll(e -> {
//        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(c.getItem().selected);
        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
        });
        this.setOnMouseMoved(e -> {
        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(DBMPPlus.partsDisplay.getSelectionModel().getSelectedItems().contains(c.getItem()));
//        	for (CarPartListCell c : DBMPPlus.carPartListCells) if(c!=null && c.getItem()!=null) c.checkBox.setSelected(c.getItem().selected);
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