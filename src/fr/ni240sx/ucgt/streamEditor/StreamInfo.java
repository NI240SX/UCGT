package fr.ni240sx.ucgt.streamEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;
import fr.ni240sx.ucgt.shared.Hash;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class StreamInfo extends TreeView<String>{ //tried using SearchableTreeView but it's fucked
	TreeItem<String> root = new TreeItem<>();

	TreeItem<String> elevation = new TreeItem<>("Elevation toggle");
	TreeItem<String> linkedChunks = new TreeItem<>("Linked chunks");
	TreeItem<String> bounds = new TreeItem<>("Bounds");
	TreeItem<String> position = new TreeItem<>("Position");
	TreeItem<String> misc = new TreeItem<>("Miscellaneous");
	

	public AtomicInteger phaser = new AtomicInteger();
	
	public StreamInfo() {
		super();
		this.setRoot(root);
		root.setExpanded(true);

		elevation.setExpanded(true);
//		linkedChunks.setExpanded(true);
		bounds.setExpanded(true);
		position.setExpanded(true);
		misc.setExpanded(true);
		this.update(null, null, null);
		
		this.setCellFactory(tv -> {
			TreeCell<String> cell = new TreeCell<>() {
		        @Override
		        protected void updateItem(String item, boolean empty) {
		            super.updateItem(item, empty);
		            setText(item);
		            
		            if (this.getTreeItem() != null && this.getTreeItem().getClass() == FormattedTreeItem.class) {
		            	 setStyle(((FormattedTreeItem<String>)this.getTreeItem()).style);
		             }
		        }
		    };
		    return cell ;
        });

	}
	
	ArrayList<ChangeListener<? super TreeItem<String>>> oldTextureListeners = new ArrayList<>();
	
	public void update(StreamController controller, List<VisualController> allVisualControllers, File streamFile) {
		root.getChildren().clear();
		
		elevation.getChildren().clear();
		linkedChunks.getChildren().clear();
		bounds.getChildren().clear();
		position.getChildren().clear();
		misc.getChildren().clear();
		
		oldTextureListeners.forEach(l -> this.getSelectionModel().selectedItemProperty().removeListener(l));
		oldTextureListeners.clear();
		
		try {
			if (controller == null) {
				root.setValue("");
			} else {
				var b = controller.boundary;
				var rel = controller.visualChunks;
				var elev = controller.elevationRuleTriangles;

				root.setValue(controller.toString());
				root.getChildren().add(new TreeItem<>("ID: %s".formatted(controller.ID)));

				if (b != null) {
					root.getChildren().add(position);
					position.getChildren().add(new TreeItem<>("X="+b.pos[X]));
					position.getChildren().add(new TreeItem<>("Y="+b.pos[Y]));
					
					root.getChildren().add(bounds);
					bounds.getChildren().add(new TreeItem<>("X="+b.boundsMin[X]+" to X="+b.boundsMax[X]));
					bounds.getChildren().add(new TreeItem<>("Y="+b.boundsMin[Y]+" to Y="+b.boundsMax[Y]));

					root.getChildren().add(misc);
					misc.getChildren().add(new TreeItem<>("Type: %d".formatted(b.type)));
					misc.getChildren().add(new TreeItem<>("Unknown1: %d".formatted(b.type1)));
					misc.getChildren().add(new TreeItem<>("Unknown2: %d".formatted(b.type2)));
					misc.getChildren().add(new TreeItem<>("Unknown3: %d".formatted(b.unk2)));
					misc.getChildren().add(new TreeItem<>("Unknown4: %d".formatted(b.unk3)));
					
					if (b.elevationHash != 0) {
						root.getChildren().add(elevation);
						elevation.getChildren().add(new TreeItem<>("Toggle mesh hash: "+Hash.getBIN(b.elevationHash)));
						elevation.getChildren().add(new TreeItem<>("Toggle with chunk: "+IDToString(b.ID_over)));
					}
					
				}
				
				if (elev != null) {
				}
				
				if (rel != null) {
					root.getChildren().add(linkedChunks);
					linkedChunks.getChildren().add(new TreeItem<>("Type: %d".formatted(rel.type)));
					linkedChunks.getChildren().add(new TreeItem<>("Unknown1: %d".formatted(rel.type1)));
					linkedChunks.getChildren().add(new TreeItem<>("Unknown2: %d".formatted(rel.type2)));

					final var phase = phaser.incrementAndGet();

					new Thread(() -> {
						try {
							for (var chunk : controller.getVisualChunks(allVisualControllers)) {
								if (phaser.get() != phase) return;
								var t = new TreeItem<String>();
								makeChunkSummaryTree(t, chunk, streamFile);
								javafx.application.Platform.runLater(() -> linkedChunks.getChildren().add(t));
							}
						}catch(@SuppressWarnings("unused") Exception e) {
//							e.printStackTrace();
						}
					}).start();
				}
			}
		}catch (Exception e) {e.printStackTrace();}
	}

	public void update(VisualController controller, File streamFile) {
		root.getChildren().clear();
		try {
			makeChunkSummaryTree(root, controller, streamFile);
		}catch (@SuppressWarnings("unused") Exception e) {}
	}
	private static void makeChunkSummaryTree(TreeItem<String> t, VisualController chunk, File streamFile) throws Exception {
		var summaries = chunk.getContentSummary(streamFile);
		t.setValue(chunk.toString());
		t.getChildren().add(new TreeItem<>("ID: %s".formatted(chunk.ID)));
		for (var s : summaries) {
			var block = new TreeItem<>(s.getBlockID().toString());
			block.setExpanded(true);
			t.getChildren().add(block);
			if (s.getBlockName() != null) block.getChildren().add(new TreeItem<>(s.getBlockName()));
			if (s.getBlockChunk() != null) block.getChildren().add(new TreeItem<>(s.getBlockChunk()));
			if (s.getBlockContents() != null) {
				var content = new TreeItem<>("Contents");
				content.setExpanded(true);
				block.getChildren().add(content);
				for (var string : s.getBlockContents()) content.getChildren().add(new TreeItem<>(string));
			}
		}
	}
	
	public static String IDToString(short ID) {
		return ((char)('A'+(ID/1000))+""+ID%1000);
	}
}

class FormattedTreeItem<T> extends TreeItem<T>{

	public String style = null;
	
	public FormattedTreeItem (T var){
		super(var);
	}
}