module UCGT {
	requires javafx.controls;
	
	opens application to javafx.graphics, javafx.fxml;
	opens testing to javafx.graphics, javafx.fxml;
}
