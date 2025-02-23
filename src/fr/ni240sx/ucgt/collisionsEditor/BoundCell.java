package fr.ni240sx.ucgt.collisionsEditor;

import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Hash;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

public class BoundCell extends TreeCell<CollisionBound> {
    CheckBox checkBox = new CheckBox();
    Label partDisplayName = new Label();
    HBox partHBox = new HBox(10, checkBox, partDisplayName);
    
    public static ArrayList<BoundCell> allCells = new ArrayList<>();

    public BoundCell() {
        allCells.add(this);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setMinWidth(0);
        
//        this.setOnKeyPressed(e -> {
////        	System.out.println("key pressed on bound cell");
//        	switch (e.getCode()) {
//        	case ENTER:
//        	case SPACE:
//	        	getItem().render.set(!getItem().render.get());
//	        	checkBox.setSelected(getItem().render.get());
//        		break;
//        	default:
//        	}
//        });
//        this.setOnMouseClicked(e -> {
//        	if (e.isControlDown()) {
//	        	getItem().render.set(!getItem().render.get());
//	        	checkBox.setSelected(getItem().render.get());
//        	}
////        	CollisionsEditor.boundControls.setBound(getItem());
////        	this.requestFocus();
//        });
        checkBox.setOnAction(e -> {
        	getItem().render.set(checkBox.isSelected());
        });
    }

    @Override
    protected void updateItem(CollisionBound item, boolean empty) {
        super.updateItem(item, empty);

        update(item, empty);
    }

	public void update(CollisionBound item, boolean empty) {
		if (item == null || empty) {
            setGraphic(null);
        } else {
            if (item.NameHash != 0) partDisplayName.setText(Hash.getVLT(item.NameHash) + ", " + item.Type + ", "+item.Shape);
            else partDisplayName.setText("[ROOT]" + ", " + item.Type + ", "+item.Shape);
//            checkBox.selectedProperty().bindBidirectional(item.render);
            checkBox.setSelected(item.render.get());
            setGraphic(partHBox);
        }
	}
}