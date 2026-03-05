package fr.ni240sx.ucgt.geometryFile.gui;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

public class CustomProgressBar extends StackPane {

	public Label text = new Label();
	public ProgressBar progress = new ProgressBar();
	
	public CustomProgressBar() {
		super();
		this.getChildren().addAll(progress, text);
	}
	
	public void setSize(double width, double height) {
		progress.setMinSize(width, height);
		progress.setMaxSize(width, height);
	}

}
