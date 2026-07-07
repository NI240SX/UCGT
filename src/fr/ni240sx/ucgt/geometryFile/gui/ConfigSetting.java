package fr.ni240sx.ucgt.geometryFile.gui;

import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.compression.CompressionType;
import fr.ni240sx.ucgt.geometryFile.settings.SettingsImport_Tangents;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

@SuppressWarnings({"unchecked","hiding"})
public abstract class ConfigSetting extends HBox {

	Label label;
	public String name;
	public Node setting;
	public Button delete;
	public boolean isChanged = false;
	public int flags;
	
	public ConfigSetting(String name, Node set) {
		this.name = name;
		label = new Label(name);
		setting = set;
		delete = new Button("X");
		var sep = new Separator();
		var sep2 = new Separator();
        HBox.setHgrow(sep, Priority.SOMETIMES);
		this.getChildren().addAll(label, sep, set, sep2, delete);
	}
	
	public ConfigSetting(String name, Node set, int f) {
		this(name, set);
		this.flags = f;
		var cbAlways = new CheckBox("Always");
		var cbNonDefault = new CheckBox("NonDefault");
		var cbOverride = new CheckBox("Override");
		
		cbAlways.setTooltip(new Tooltip("Always include in generated configs."));
		cbNonDefault.setTooltip(new Tooltip("Include in generated configs if it doesn't correspond to this default value."));
		cbOverride.setTooltip(new Tooltip("Upon compiling, override any corresponding setting from a model-specific configuration file."));
		
		cbAlways.setSelected((flags & 0x1) != 0);
		cbNonDefault.setSelected((flags & 0x2) != 0);
		cbOverride.setSelected((flags & 0x4) != 0);
		cbAlways.setOnAction(e -> {
			if (cbAlways.isSelected()) flags = flags | 0x1;
			else flags = flags & ~0x1;
		});
		cbNonDefault.setOnAction(e -> {
			if (cbNonDefault.isSelected()) flags = flags | 0x2;
			else flags = flags & ~0x2;
		});
		cbOverride.setOnAction(e -> {
			if (cbOverride.isSelected()) flags = flags | 0x4;
			else flags = flags & ~0x4;
		});
		this.getChildren().addAll(new Separator(), new Label("Flags"), new Separator(), cbAlways, new Separator(), cbNonDefault, new Separator(), cbOverride);
	}
	
	public String getName() {
		return name;
	}
	
	public abstract String getStringValue();

	public static class Text extends ConfigSetting{
		public TextField setting;
		
		public Text(String name, String set) {
			super(name, new TextField(set));
			this.setting = (TextField) super.setting;
			this.setting.textProperty().addListener(l -> isChanged = true);
		}
		
		public Text(String name, String set, int flags) {
			super(name, new TextField(set), flags);
			this.setting = (TextField) super.setting;
			this.setting.textProperty().addListener(l -> isChanged = true);
		}
		
		@Override
		public String getStringValue() {
			return setting.getText().strip();
		}
	}

	public static class Platform extends ConfigSetting{
		public ComboBox<fr.ni240sx.ucgt.geometryFile.Platform> setting;
		
		public Platform(String name, fr.ni240sx.ucgt.geometryFile.Platform t) {
			super(name, new ComboBox<fr.ni240sx.ucgt.geometryFile.Platform>());
			this.setting = (ComboBox<fr.ni240sx.ucgt.geometryFile.Platform>) super.setting;
			this.setting.getItems().addAll(fr.ni240sx.ucgt.geometryFile.Platform.values());
			this.setting.getSelectionModel().select(t);
			this.setting.getSelectionModel().selectedItemProperty().addListener(l -> isChanged = true);
		}

		@Override
		public String getStringValue() {
			return setting.getValue().toString();
		}
	}
	
	public static class CompType extends ConfigSetting{
		public ComboBox<CompressionType> setting;
		
		public CompType(String name, CompressionType t) {
			super(name, new ComboBox<CompressionType>());
			this.setting = (ComboBox<CompressionType>) super.setting;
			this.setting.getItems().addAll(CompressionType.values());
			this.setting.getSelectionModel().select(t);
			this.setting.getSelectionModel().selectedItemProperty().addListener(l -> isChanged = true);
		}

		@Override
		public String getStringValue() {
			return setting.getValue().toString();
		}
}

	public static class CompLevel extends ConfigSetting{
		public ComboBox<CompressionLevel> setting;
		
		public CompLevel(String name, CompressionLevel l) {
			super(name, new ComboBox<CompressionLevel>());
			this.setting = (ComboBox<CompressionLevel>) super.setting;
			this.setting.getItems().addAll(CompressionLevel.values());
			this.setting.getSelectionModel().select(l);
			this.setting.getSelectionModel().selectedItemProperty().addListener(ls -> isChanged = true);
		}

		@Override
		public String getStringValue() {
			return setting.getValue().toString();
		}
	}

	public static class Tangents extends ConfigSetting{
		public ComboBox<SettingsImport_Tangents> setting;
		
		public Tangents(String name, SettingsImport_Tangents t) {
			super(name, new ComboBox<SettingsImport_Tangents>());
			this.setting = (ComboBox<SettingsImport_Tangents>) super.setting;
			this.setting.getItems().addAll(SettingsImport_Tangents.values());
			this.setting.getSelectionModel().select(t);
			this.setting.getSelectionModel().selectedItemProperty().addListener(l -> isChanged = true);
		}

		@Override
		public String getStringValue() {
			return setting.getValue().toString();
		}
	}

	public static class Boolean extends ConfigSetting{
		public ComboBox<java.lang.Boolean> setting;
		
		public Boolean(String name, java.lang.Boolean b) {
			super(name, new ComboBox<java.lang.Boolean>());
			this.setting = (ComboBox<java.lang.Boolean>) super.setting;
			this.setting.getItems().addAll(true, false);
			this.setting.getSelectionModel().select(b);
			this.setting.getSelectionModel().selectedItemProperty().addListener(l -> isChanged = true);
		}

		public Boolean(String name, boolean b, int flags) {
			super(name, new ComboBox<java.lang.Boolean>(), flags);
			this.setting = (ComboBox<java.lang.Boolean>) super.setting;
			this.setting.getItems().addAll(true, false);
			this.setting.getSelectionModel().select(b);
			this.setting.getSelectionModel().selectedItemProperty().addListener(l -> isChanged = true);
		}

		@Override
		public String getStringValue() {
			return setting.getValue().toString();
		}
	}

}
