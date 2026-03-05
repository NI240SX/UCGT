package fr.ni240sx.ucgt.geometryFile.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SearchableListView<t extends Object> extends VBox {

	public ListView<t> list = new ListView<>();
	public TextField filter = new TextField();
	
	private ObservableList<t> items;
	
	public SearchableListView() {
        this(FXCollections.<t>observableArrayList());
    }
	
	public SearchableListView(ObservableList<t> offsets) {
		this.items = offsets;
	    FilteredList<t> filteredData = new FilteredList<>(this.items,p -> true);

	    filter.textProperty().addListener((obs, oldValue, newValue) -> {
	        filteredData.setPredicate(obj ->{
	            if(newValue == null || newValue.isBlank()) return true;
	            
	            if (obj.getClass() == FileOffset.class) {
	            	FileOffset off = (FileOffset) obj;
		            if (off.chunk.toLowerCase().contains(newValue.toLowerCase())) return true;
		            if (off.file.toLowerCase().contains(newValue.toLowerCase())) return true;	            
		            return false;
	            }
	            
	            if (obj.toString().toLowerCase().contains(newValue.toLowerCase())) return true;
	            
	            return false;
	        });
	    });

	    //Wrap the FilteredList in a SortedList.
	    SortedList<t> sortedData = new SortedList<>(filteredData);

	    this.list.setItems(sortedData);
	    this.setMaxHeight(100000);
	    this.list.setMaxHeight(100000);
	    this.setPrefHeight(100000);
	    this.list.setPrefHeight(100000);
	    
	    this.getChildren().addAll(filter, this.list);
		
	}
	
	public MultipleSelectionModel<t> getSelectionModel() {
		return list.getSelectionModel();
	}
	
	public ObservableList<t> getItems(){
		return items;
	}
	
}
