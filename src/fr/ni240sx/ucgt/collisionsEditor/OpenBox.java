package fr.ni240sx.ucgt.collisionsEditor;

import javafx.scene.Group;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;

public class OpenBox extends Group {


	public OpenBox(double x, double y, double z, double t) {
		
		var XLine1 = new Box(1, t, t);
		var XLine2 = new Box(1, t, t);
		var XLine3 = new Box(1, t, t);
		var XLine4 = new Box(1, t, t);

		XLine1.setTranslateY(y/2);
		XLine1.setTranslateZ(z/2);
		XLine2.setTranslateY(-y/2);
		XLine2.setTranslateZ(z/2);
		XLine3.setTranslateY(y/2);
		XLine3.setTranslateZ(-z/2);
		XLine4.setTranslateY(-y/2);
		XLine4.setTranslateZ(-z/2);
		
		var YLine1 = new Box(t, 1, t);
		var YLine2 = new Box(t, 1, t);
		var YLine3 = new Box(t, 1, t);
		var YLine4 = new Box(t, 1, t);

		YLine1.setTranslateX(y/2);
		YLine1.setTranslateZ(z/2);
		YLine2.setTranslateX(-y/2);
		YLine2.setTranslateZ(z/2);
		YLine3.setTranslateX(y/2);
		YLine3.setTranslateZ(-z/2);
		YLine4.setTranslateX(-y/2);
		YLine4.setTranslateZ(-z/2);

		var ZLine1 = new Box(t, t, 1);
		var ZLine2 = new Box(t, t, 1);
		var ZLine3 = new Box(t, t, 1);
		var ZLine4 = new Box(t, t, 1);

		ZLine1.setTranslateY(y/2);
		ZLine1.setTranslateX(z/2);
		ZLine2.setTranslateY(-y/2);
		ZLine2.setTranslateX(z/2);
		ZLine3.setTranslateY(y/2);
		ZLine3.setTranslateX(-z/2);
		ZLine4.setTranslateY(-y/2);
		ZLine4.setTranslateX(-z/2);
		
		this.getChildren().addAll(XLine1, XLine2, XLine3, XLine4, YLine1, YLine2, YLine3, YLine4, ZLine1, ZLine2, ZLine3, ZLine4);
		
	}

	public void setDrawMode(DrawMode line) {
		this.getChildren().forEach(c -> ((Box) c).setDrawMode(line));
	}

}
