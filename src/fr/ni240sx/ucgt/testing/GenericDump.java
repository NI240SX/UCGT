package fr.ni240sx.ucgt.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GenericDump extends Application {

	TextArea hex = new TextArea();
	TextArea dmp = new TextArea();
	
	String hexSearch = "";
	String dmpSearch = "";

	static String car = "AUD_RS4_STK_08";
	static int[] art = new int[]{0, 1, 2, 3, 4, 5, 6, 11, 12};
	static int[] wart = new int[]{1, 2, 3, 4, 5};
	
	
	static int startFrom = 0; //63400
	static int length = 8192;//8192 recommended
	
	File f = new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\binary skinregiondb\\SkinRegionDB-1.18.bin");
		
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setLeft(hex);
		root.setCenter(dmp);
		Scene scene = new Scene(root,1280,720);
		primaryStage.setScene(scene);
		hex.setPrefWidth(400);

        scene.getRoot().setStyle("-fx-base:black");

		hex.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				dmp.setScrollTop(hex.getScrollTop());
				if(event.isControlDown() && event.getCode().equals(KeyCode.F)) {
					hexSearch = hex.getSelectedText();
					System.out.println("new search : "+hexSearch);
				}
				if(event.getCode().equals(KeyCode.F3)) {
					int search;
					System.out.println("searched "+hexSearch);
					if ((search = hex.getText(hex.getSelection().getEnd(), hex.getLength()).indexOf(hexSearch)) != -1 ) {
						hex.selectRange(hex.getSelection().getEnd()+search, hex.getSelection().getEnd()+search+hexSearch.length());
					}
				}
				
			}
		});
		hex.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				dmp.setScrollTop(hex.getScrollTop());
			}
		});
		hex.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				dmp.setScrollTop(hex.getScrollTop());
			}
			
		});
		
		
		dmp.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				hex.setScrollTop(dmp.getScrollTop());
				if(event.isControlDown() && event.getCode().equals(KeyCode.F)) {
					dmpSearch = dmp.getSelectedText();
					System.out.println("new search : "+dmpSearch);
				}
				if(event.getCode().equals(KeyCode.F3)) {
					int search;
					System.out.println("searched "+dmpSearch);
					if ((search = dmp.getText(dmp.getSelection().getEnd(), dmp.getLength()).indexOf(dmpSearch)) != -1 ) {
						dmp.selectRange(dmp.getSelection().getEnd()+search, dmp.getSelection().getEnd()+search+dmpSearch.length());
					}
				}
				if(event.getCode().equals(KeyCode.F4)) {
					System.out.println(hexSearch = Integer.toHexString(Integer.reverseBytes(Hash.findBIN(dmp.getSelectedText()))));
					System.out.println("new search : "+hexSearch);
				}
				
			}
		});
		dmp.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				hex.setScrollTop(dmp.getScrollTop());
			}
		});
		dmp.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				hex.setScrollTop(dmp.getScrollTop());
			}
		});
		
		
		
		
		primaryStage.show();	
		dumpGeom(f,hex,dmp);
	}

	public static void main(String[] args) {
		launch(args);
//		System.out.println(tryDecode4B(-869258610, generateHashes("BMW_M3_E92_08", new int[]{0, 1, 4, 6, 11}, new int[]{1})));
	}
	
	public static void dumpGeom(File f, TextArea hex, TextArea dmp) {
		
		try {
			FileInputStream fis = new FileInputStream(f);
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			ByteBuffer bb = ByteBuffer.wrap(arr);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
//			generateHashes(car, art, wart);
			generateHashes(null,null,null);
			
//			Hashlist.add(new Hash(car + "_KIT00_TRIANGLE_A"));
//			Hashlist.add(new Hash(car + "_KIT00_CUBE_A"));
			
			bb.position(startFrom);
			while (bb.position()<startFrom+length && bb.position()<arr.length-16) {//65536
				for (int i=0; i<4; i++) {
					hex.appendText(Integer.toHexString(Integer.reverseBytes(bb.getInt(bb.position())))+" ");
					dmp.appendText(tryDecode4B(bb)+" ");
				}
				hex.appendText("\n");
				dmp.appendText("\n");
			}
			System.out.println("Dumping finished.");
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * @param carname
	 * @param kits
	 * @param widebodies
	 * @return
	 */
	public static void generateHashes(String carname, int[] kits, int[] widebodies) {
		
		try {
			if (carname != null) {
				Hash.findBIN(carname);
				for (int k : kits) {
					BufferedReader br = new BufferedReader(new FileReader(new File("data/parts")));
	
					String part;
					while ((part = br.readLine())!=null){
					
						String kstr;
						if(k<10)kstr = "KIT0"+k;
						else kstr = "KIT"+k;
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_A");
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_B");
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_C");
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_D");
	
						for (int as=0; as<11; as++) {
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_A");
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_B");
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_C");
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_D");
						}
					}
				}
				
				for (int k : widebodies) {
					BufferedReader br = new BufferedReader(new FileReader(new File("data/parts")));
	
					String part;
					while ((part = br.readLine())!=null){
					
						String kstr;
						if(k<10)kstr = "KITW0"+k;
						else kstr = "KITW"+k;
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_A");
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_B");
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_C");
						Hash.findBIN(carname + "_" + kstr + "_" + part + "_D");
	
						for (int as=0; as<11; as++) {
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_A");
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_B");
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_C");
							Hash.findBIN(carname + "_" + kstr + "_" + part + "_T" + as + "_D");
						}
					}
				}
			
				BufferedReader br = new BufferedReader(new FileReader(new File("data/textures")));
				String tex;
				while ((tex = br.readLine())!=null){
					Hash.findBIN(carname + "_" + tex);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		Hash.addBinHashes(new File("data/gentextures"));
		Hash.addBinHashes(new File("data/shaders"));
		Hash.addBinHashes(new File("data/mpoints"));
		Hash.addBinHashes(new File("data/stuff"));
		Hash.addBinHashes(new File("data/collisionshashes"));

		addSpecificHashes();
		
	}

	private static void addSpecificHashes() {
		
		for (var b : BlockType.values()) {
			Hash.findBIN(b.getName());
		}
		for (var b : TextureUsage.values()) {
			Hash.findBIN(b.getName());
		}
		for (var b : ShaderUsage.values) {
			Hash.findBIN(b.getName());
		}
		
//		
//		l.add(new Hash("BLOCK:GEOM",Integer.reverseBytes(0x00401380))); 
//		l.add(new Hash("BLOCK:GEOM_HEADER",Integer.reverseBytes(0x01401380))); 
//		l.add(new Hash("BLOCK:GEOM_INFO",Integer.reverseBytes(0x02401300))); 
//		l.add(new Hash("BLOCK:GEOM_PARTSLIST",Integer.reverseBytes(0x03401300))); 
//		l.add(new Hash("BLOCK:GEOM_PARTSOFSSETS",Integer.reverseBytes(0x04401300))); 
//		l.add(new Hash("BLOCK:GEOM_???",Integer.reverseBytes(0x08401380))); 
//		l.add(new Hash("BLOCK:COMPRESSED",Integer.reverseBytes(0x22114455))); 
//		l.add(new Hash("BLOCK:PART",Integer.reverseBytes(0x10401380))); 
//		l.add(new Hash("BLOCK:PART_HEADER",Integer.reverseBytes(0x11401300)));
//		l.add(new Hash("BLOCK:PART_TEXUSAGE",Integer.reverseBytes(0x12401300)));
//		l.add(new Hash("BLOCK:PART_STRINGS",Integer.reverseBytes(0x15401300)));
//		l.add(new Hash("BLOCK:PART_SHADERLIST",Integer.reverseBytes(0x13401300)));
//		l.add(new Hash("BLOCK:PART_MPOINTS",Integer.reverseBytes(0x1A401300)));
//		l.add(new Hash("BLOCK:PART_MESH",Integer.reverseBytes(0x00411380)));
//		l.add(new Hash("BLOCK:PART_MESH_?",Integer.reverseBytes(0x00491300)));
//		l.add(new Hash("BLOCK:PART_MESH_SHADERS",Integer.reverseBytes(0x014F1300)));
//		l.add(new Hash("BLOCK:PART_MESH_MATERIALS",Integer.reverseBytes(0x02491300)));
//		l.add(new Hash("BLOCK:PART_MESH_PADDING?",Integer.reverseBytes(0x024C1300)));
//		l.add(new Hash("BLOCK:PART_MESH_TRIANGLES?",Integer.reverseBytes(0x01491300)));
//		l.add(new Hash("BLOCK:PART_MESH_INDICES?",Integer.reverseBytes(0x03491300)));
//		l.add(new Hash("BLOCK:PART_PADDING?",Integer.reverseBytes(0x17401300)));
//		l.add(new Hash("BLOCK:PART_HASHLIST?",Integer.reverseBytes(0x18401300)));
//		l.add(new Hash("BLOCK:PART_HASHASSIGN?",Integer.reverseBytes(0x19401300)));

//		l.add(new Hash("SHADER:Diffuse",Integer.reverseBytes(0xeb5337a1))); //diffuse tex
//		l.add(new Hash("SHADER:DiffuseNormal",Integer.reverseBytes(0xf5bbcc3f))); //diffuse tex
//		l.add(new Hash("SHADER:DiffuseAlpha",Integer.reverseBytes(0xe12db62e))); //diffuse tex (in BRAKEDISC) OR diffuse tex, alpha tex (in HEADLIGHTGLASS)
//		l.add(new Hash("SHADER:DiffuseAlphaNormal",Integer.reverseBytes(0xd70a7771))); //diffuse tex, alpha tex, normalmap ; used for DOORLINE, seems a bit weird
//		l.add(new Hash("SHADER:DiffuseGlow",Integer.reverseBytes(0x88155cb2))); //diffuse tex, glow tex
//		l.add(new Hash("SHADER:DiffuseGlowAlpha",Integer.reverseBytes(0x5c6cdd0d))); //diffuse tex, glow tex
//		l.add(new Hash("SHADER:DiffuseNormalPaintable",Integer.reverseBytes(0xc52da6a8))); //diffuse tex, normal tex, swatch tex
		
//		l.add(new Hash("TEXUSAGE_DIFFUSE",1311995566));	//AE76334E
//		l.add(new Hash("TEXUSAGE_NORMAL",43354773));	//958A9502
//		l.add(new Hash("TEXUSAGE_ALPHA",Integer.reverseBytes(0x5aff315c)));	
//		l.add(new Hash("TEXUSAGE_GLOW",3150905));		//39143000²
//		l.add(new Hash("TEXUSAGE_SWATCH",-698446836)); 	//0C8C5ED6
		
		
//		Hash.findBIN("[PART DECLARATION]",4194329); // 19004000
//		l.add(new Hash("[before carskin]",2499937291));
//		l.add(new Hash("[???]",3943825699));
	}
	
	
	public static String tryDecode4B(ByteBuffer bb){
		int in = bb.getInt();
		// Padding lookup
		if (in == 0) return "        ";
		if (in == 0x11111111) return "--------";
		if (in == 0xFFFFFFFF) return "////////";
		// Hash lookup
		if (!Hash.getBIN(in).startsWith("0x")) return Hash.getBIN(in);
		if (!Hash.getVLT(in).startsWith("0x")) return Hash.getVLT(in);

		// Plain string lookup
		boolean valString = true;
		byte[] bytes = new byte[4];
		bb.get(bb.position()-4, bytes);
		String s = new String(bytes, StandardCharsets.ISO_8859_1);
		for (char c :s.toCharArray()) {
			if (!(Character.isLetterOrDigit(c) || c =='_' || c==' ' || c=='.' || c=='\00')) {
				valString = false;
			}
		}
		if (valString) return s;
		
		// Float lookup
		boolean valFloat = false;
		float f = bb.getFloat(bb.position()-4);
		if (Math.abs(f)>0.0001 && Math.abs(f)<10000 || f == 0) {
			valFloat=true;
		}
		if (valFloat) return Float.toString(f);
				
		// Default to integer
		return Integer.toString(in);
	}
	
	public static String decodeSimple4B(int integer) {
		return Hash.getBIN(Integer.reverseBytes(integer));
	}
}
