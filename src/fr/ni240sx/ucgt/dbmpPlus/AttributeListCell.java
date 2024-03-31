package fr.ni240sx.ucgt.dbmpPlus;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class AttributeListCell extends ListCell<Attribute> {
    Label attribType;
    Label attribName;
    
    //code this shit like it was before to get around the width issue and keep the hboxes !!! (compare with the version on the desktop)
    
    BorderPane cellPane;
    
    HBox nameHBox;
    HBox dataHBox;
    
    public AttributeListCell() {
        attribType = new Label();
        attribName = new Label();
//        DBMPPlus.attributeListCells.add(this);

        nameHBox = new HBox(10, attribType, attribName);
        dataHBox = new HBox();

        cellPane = new BorderPane();
        cellPane.setLeft(nameHBox);
        cellPane.setRight(dataHBox);
                
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        
//        autosculptMorph.setOnAction(e -> {
//        	getItem().getAttributeInteger("MORPHTARGET_NUM").value = Integer.valueOf(autosculptMorph.getText());
//        	if (DBMPPlus.debug) System.out.println("Part " + getItem() + " morphtarget number changed to " + getItem().getAttributeInteger("MORPHTARGET_NUM").value);
//        	e.consume();
//        });
        
    }

    @Override
    protected void updateItem(Attribute item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
        	attribType.setText(item.getAttribType());
        	attribName.setText(item.displayName);
            cellPane.setRight(item.dataHBox);
//        	dataHBox = item.dataHBox;
            setGraphic(cellPane);
        }
    }
    
    protected void update() {
        if (getItem() == null) {
            setGraphic(null);
        } else {
        	attribType.setText(getItem().getAttribType());
        	attribName.setText(getItem().displayName);
            cellPane.setRight(getItem().dataHBox);
//        	dataHBox = getItem().dataHBox;
            setGraphic(cellPane);
        }
    }
}