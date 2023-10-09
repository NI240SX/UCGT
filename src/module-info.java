module UCGT {
	requires javafx.controls;
	requires javafx.base;
	requires javafx.graphics;
	
	opens application to javafx.graphics, javafx.fxml;
	opens testing to javafx.graphics, javafx.fxml;
	opens dbmpPlus to javafx.graphics, javafx.fxml;
}
