package fr.ni240sx.ucgt.geometryFile.gui;

import java.util.function.Predicate;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public class SearchableTreeView<t extends Object> extends VBox {

	public TreeView<t> list = new TreeView<>();
	public TextField filter = new TextField();

	private TreeItem<t> root;

	private TreeItem<t> sortedRoot = null;
	
	private Predicate<t> predicate = obj -> true;
	
	private static final String resetter = "%";
	
	public SearchableTreeView() {
        this(null);
    }
	
	public SearchableTreeView(TreeItem<t> r) {
		this.setRoot(r);

	    filter.textProperty().addListener((obs, oldValue, newValue) -> {
	        predicate = (obj ->{
	            if(newValue == null || newValue.isBlank()) return true;
	            
	            if (obj.getClass() == PartController.class) {
	            	PartController pc = (PartController)obj;
	            	if (pc.part != null) return pc.part.name.toUpperCase().contains(newValue.toUpperCase());
	            }
	            
	            if (obj.toString().toLowerCase().contains(newValue.toLowerCase())) return true;
	            
	            return false;
	        });
//	        sortedRoot.getChildren().clear();
	        updateTreeView(root, sortedRoot);
	        if (newValue.isBlank() && !oldValue.equals(resetter)) { //to restore part names sorting
	        	filter.setText(resetter);
	        }
	        if (newValue.equals(resetter)) {
	        	filter.setText("");
	        }
	    });
	    
	    this.setMaxHeight(100000);
	    this.list.setMaxHeight(100000);
	    this.setPrefHeight(100000);
	    this.list.setPrefHeight(100000);
	    
	    this.getChildren().addAll(filter, this.list);
		
	}
	
	public void updateTreeView() {
		updateTreeView(root, sortedRoot);
	}
	
	public void updateTreeView(TreeItem<t> item, TreeItem<t> sorted) {
		if (item != null) loop: for (var c : item.getChildren()) {
			if (c.isLeaf()) {
				if (predicate.test(c.getValue())) {
					if (!sorted.getChildren().contains(c)) sorted.getChildren().add(c);
				} else {
					if (sorted.getChildren().contains(c)) sorted.getChildren().remove(c);
				}
			} else {
				for (var ch : sorted.getChildren()) if (ch.getValue().equals(c.getValue())) {
					updateTreeView(c, ch);
					continue loop;
				}
				
				var subNode = new TreeItem<t>();
				subNode.setValue(c.getValue());
				sorted.getChildren().add(subNode);
				updateTreeView(c, subNode);
			}
		}
	}
	
	public MultipleSelectionModel<TreeItem<t>> getSelectionModel() {
		return list.getSelectionModel();
	}

	public TreeItem<t> getRoot() {
		return sortedRoot;
	}

	public void setRoot(TreeItem<t> r) {
		this.root = r;
		if (sortedRoot != null) sortedRoot.getChildren().clear();
		if (root != null) {
			sortedRoot = new TreeItem<>(root.getValue());
			root.valueProperty().addListener(l -> {
	        	sortedRoot.setValue(root.getValue());
	        });
		} else sortedRoot = new TreeItem<>(null);
		list.setRoot(sortedRoot);
		predicate = obj -> true;
        updateTreeView(root, sortedRoot);
	}

	public void scrollTo(int selectedIndex) {
		list.scrollTo(selectedIndex);
	}
	
}
