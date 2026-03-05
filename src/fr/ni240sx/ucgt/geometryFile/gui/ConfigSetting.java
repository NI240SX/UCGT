package fr.ni240sx.ucgt.geometryFile.gui;

import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.settings.SettingsImport_Tangents;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

@SuppressWarnings({"unchecked","hiding"})
public abstract class ConfigSetting extends HBox {

	Label label;
	public Node setting;
	
	public ConfigSetting(String name, Node set) {

		label = new Label(name);
		setting = set;
		this.getChildren().addAll(label, set);
		
	}

	public static class Text extends ConfigSetting{
		public TextField setting;
		
		public Text(String name, String set) {
			super(name, new TextField(set));
			this.setting = (TextField) super.setting;
		}
	}

	public static class Platform extends ConfigSetting{
		public ComboBox<fr.ni240sx.ucgt.geometryFile.Platform> setting;
		
		public Platform(String name, fr.ni240sx.ucgt.geometryFile.Platform t) {
			super(name, new ComboBox<fr.ni240sx.ucgt.geometryFile.Platform>());
			this.setting = (ComboBox<fr.ni240sx.ucgt.geometryFile.Platform>) super.setting;
			this.setting.getItems().addAll(fr.ni240sx.ucgt.geometryFile.Platform.values());
			this.setting.getSelectionModel().select(t);
		}
	}
	
	public static class CompType extends ConfigSetting{
		public ComboBox<CompressionType> setting;
		
		public CompType(String name, CompressionType t) {
			super(name, new ComboBox<CompressionType>());
			this.setting = (ComboBox<CompressionType>) super.setting;
			this.setting.getItems().addAll(CompressionType.values());
			this.setting.getSelectionModel().select(t);
		}
	}

	public static class CompLevel extends ConfigSetting{
		public ComboBox<CompressionLevel> setting;
		
		public CompLevel(String name, CompressionLevel l) {
			super(name, new ComboBox<CompressionLevel>());
			this.setting = (ComboBox<CompressionLevel>) super.setting;
			this.setting.getItems().addAll(CompressionLevel.values());
			this.setting.getSelectionModel().select(l);
		}
	}

	public static class Tangents extends ConfigSetting{
		public ComboBox<SettingsImport_Tangents> setting;
		
		public Tangents(String name, SettingsImport_Tangents t) {
			super(name, new ComboBox<SettingsImport_Tangents>());
			this.setting = (ComboBox<SettingsImport_Tangents>) super.setting;
			this.setting.getItems().addAll(SettingsImport_Tangents.values());
			this.setting.getSelectionModel().select(t);
		}
	}

	public static class Boolean extends ConfigSetting{
		public ComboBox<java.lang.Boolean> setting;
		
		public Boolean(String name, java.lang.Boolean b) {
			super(name, new ComboBox<java.lang.Boolean>());
			this.setting = (ComboBox<java.lang.Boolean>) super.setting;
			this.setting.getItems().addAll(true, false);
			this.setting.getSelectionModel().select(b);
		}
	}

}
