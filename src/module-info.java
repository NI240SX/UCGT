module UCGT {
	requires javafx.controls;
	requires javafx.base;
	requires javafx.graphics;
	requires java.desktop;
	requires javafx.swing;
	requires org.json;
	
	opens fr.ni240sx.ucgt.application to javafx.graphics, javafx.fxml;
	opens fr.ni240sx.ucgt.testing to javafx.graphics, javafx.fxml;
	opens fr.ni240sx.ucgt.dbmpPlus to javafx.graphics, javafx.fxml;
	opens fr.ni240sx.ucgt.collisionsEditor to javafx.graphics, javafx.fxml;
	opens fr.ni240sx.ucgt.geometryFile to javafx.graphics, javafx.fxml;

	opens fr.ni240sx.ucgt.damageFile to javafx.graphics, javafx.fxml;
}
