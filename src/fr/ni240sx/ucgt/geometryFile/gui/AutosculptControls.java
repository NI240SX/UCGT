package fr.ni240sx.ucgt.geometryFile.gui;

import java.util.ArrayList;

import fr.ni240sx.ucgt.geometryFile.GeometryEditorGUI;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class AutosculptControls extends VBox{

	ArrayList<AutosculptSlider> autosculptSliders = new ArrayList<>();
	PartController part = null;
	
	public static final int SLIDER_HEIGHT = 32;
	
	public AutosculptControls() {
		super();
		for (int i=0; i<11; i++) {
			this.autosculptSliders.add(new AutosculptSlider(i, this));
		}
		this.setMaxHeight(0);
		this.setMinHeight(0);
	}
	
	public void update(PartController controller) {
		this.getChildren().clear();
		if (controller.part != null && controller.part.asZones != null) {
			part = controller;
			for (int i=1; i<controller.part.asZones.zones.size(); i++) {
				for (var p : GeometryEditorGUI.parts) if (p.part.header.binKey == controller.part.asZones.zones.get(i)) {
					this.getChildren().add(this.autosculptSliders.get(i));
					break;
				}
			}
		}
		autosculptSliders.forEach(s -> s.slider.setValue(0));
		this.setMaxHeight(this.getChildren().size()*SLIDER_HEIGHT);
		this.setMinHeight(this.getChildren().size()*SLIDER_HEIGHT);
//		this.setMinHeight(1);
		
	}

	public void updateAutosculpt() {
		if (part != null) part.updateAutosculpt(this);
		
	}

	class AutosculptSlider extends VBox{
		public int zone;
		Slider slider;

		public AutosculptSlider(int z, AutosculptControls parent) {
			this.zone = z;
			this.getChildren().add(new Label("Zone "+z));
			this.slider = new Slider(0,1, 0);
			slider.setBlockIncrement(0.01);
			this.getChildren().add(slider);
			slider.valueProperty().addListener((obs, was, is) -> {
				parent.updateAutosculpt();
			});
		}
	}
}
