package fr.ni240sx.ucgt.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Hash;
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

public class GeomDumpTest extends Application {

	TextArea hex = new TextArea();
	TextArea dmp = new TextArea();
	
	String hexSearch = "";
	String dmpSearch = "";
	
	static String car = "UCGT";
//	static String car = "240SX";
	static int[] art = new int[]{0};
	static int[] wart = new int[]{};

	
	static int startFrom = 0;
	static int length = 6384;//2048 recommended

	File f = new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT.BIN");
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setLeft(hex);
		root.setCenter(dmp);
		Scene scene = new Scene(root,1280,720);
		primaryStage.setScene(scene);
		hex.setPrefWidth(400);
		

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
					System.out.println(hexSearch = Integer.toHexString(Integer.reverseBytes(new Hash(dmp.getSelectedText()).binHash)));
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
	}
	
	public static void dumpGeom(File f, TextArea hex, TextArea dmp) {
		
		try {
			FileInputStream fis = new FileInputStream(f);
			byte [] arr = new byte[(int)f.length()];
			fis.read(arr);
			fis.close();
			ByteBuffer bb = ByteBuffer.wrap(arr);
			
			ArrayList<Hash> Hashlist = generateHashes(car, art, wart);
			bb.position(startFrom);
			while (bb.position()<startFrom+length) {//65536
				byte off = 0;
				for (int i=0; i<4; i++) {
					int current = bb.getInt();
					hex.appendText(Integer.toHexString(current)+" ");
					
					String s;
					if ((s = decodeSimple4B(current, Hashlist)) != null) dmp.appendText(s+ " ");
					else {
						//if off alignment hash
						String t = "";
						bb.position(bb.position()-4);
						if (off<1) t=t+Integer.toHexString(bb.get() & 0xFF);
						if ((s = decodeSimple4B(bb.getInt(), Hashlist)) != null) {
							dmp.appendText(t+s);
							bb.position(bb.position()-1);
							off=1;
						}else {
							bb.position(bb.position()-4);
							if (off<2) t=t+Integer.toHexString(bb.get() & 0xFF);
							if ((s = decodeSimple4B(bb.getInt(), Hashlist)) != null) {
								dmp.appendText(t+s);
								bb.position(bb.position()-2);
								off=2;
							}else {
								bb.position(bb.position()-4);								
								if (off<3) t=t+Integer.toHexString(bb.get() & 0xFF);
								if ((s = decodeSimple4B(bb.getInt(), Hashlist)) != null) {
									dmp.appendText(t+s);
									bb.position(bb.position()-3);
									off=3;
								}else {
									off=0;
									bb.position(bb.position()-3);
									boolean v=true;
									byte[] bytes = new byte[4];
									bb.get(bb.position()-4, bytes);
									String str = new String(bytes, StandardCharsets.ISO_8859_1);
//									System.out.println(str);
									for (char c :str.toCharArray()) {
										if (!(Character.isLetterOrDigit(c) || c =='_' || c==' ' || c=='.' || c=='\00')) v=false;
									}
									if(v) {
										dmp.appendText(str+" ");
									}else {
										dmp.appendText(Integer.toHexString(current)+" ");
									}
								}
								
								
								
							}
							
							
							
						}
					}
					
					
					
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
	
	public static ArrayList<Hash> generateHashes(String carname, int[] kits, int[] widebodies) {
		
		ArrayList<Hash> l = new ArrayList<>();
		l.add(new Hash(carname));
		
		try {
			for (int k : kits) {
				BufferedReader br = new BufferedReader(new FileReader(new File("data/parts_test")));

				String part;
				while ((part = br.readLine())!=null){
				
					String kstr;
					if(k<10)kstr = "KIT0"+k;
					else kstr = "KIT"+k;
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_D"));

					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_D"));
					}
				}
			}
			
			for (int k : widebodies) {
				BufferedReader br = new BufferedReader(new FileReader(new File("data/parts_test")));

				String part;
				while ((part = br.readLine())!=null){
				
					String kstr;
					if(k<10)kstr = "KITW0"+k;
					else kstr = "KITW"+k;
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + kstr + "_" + part + "_D"));

					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + kstr + "_" + part + "_T" + as + "_D"));
					}
				}
			}
		
			BufferedReader br = new BufferedReader(new FileReader(new File("data/textures")));
			String tex;
			while ((tex = br.readLine())!=null){
				l.add(new Hash(carname + "_" + tex));
			}
			
			br = new BufferedReader(new FileReader(new File("data/gentextures")));
			while ((tex = br.readLine())!=null){
				l.add(new Hash(tex));
			}

			br = new BufferedReader(new FileReader(new File("data/shaders")));
			while ((tex = br.readLine())!=null){
				l.add(new Hash(tex));
			}

			br = new BufferedReader(new FileReader(new File("data/mpoints")));
			while ((tex = br.readLine())!=null){
				l.add(new Hash(tex));
				for (int as=0; as<11; as++) {
					l.add(new Hash(tex + "_T" + as));
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		l.add(new Hash("[texture]",1311995566));
		l.add(new Hash("[normalmap]",43354773));
		l.add(new Hash("[PART DECLARATION]",4194329)); // 19004000
		l.add(new Hash("[RFPK/BEGINNING OF ???]",1263552082)); // 5246504b
//		l.add(new Hash("[before carskin]",2499937291));
//		l.add(new Hash("[???]",3943825699));
		
		
		return l;
	}
	
	
	public static String tryDecode4B(ByteBuffer bb, int integer, ArrayList<Hash> Hashlist){
		/*
		 * hash
		 * number
		 * letters
		 * hex
		 */
		boolean v=false;
		String r = Integer.toHexString(integer);
		for (Hash h: Hashlist) {
			if (h.reversedBinHash == integer) {
				r = h.label;
				v=true;
			}
		}
		if (!v) {
			byte[] bytes = new byte[4];
			bb.get(bb.position()-4, bytes);
			String s = new String(bytes, StandardCharsets.ISO_8859_1);
//			System.out.println(s);
			r=s;
			v=true;
			for (char c :s.toCharArray()) {
				if (!(Character.isLetterOrDigit(c) || c =='_' || c==' ' || c=='.' || c=='\00')) v=false;
			}
		}
		if (!v) {
			float f = bb.getFloat(bb.position()-4);
			if (Math.abs(f)>0.001 && Math.abs(f)<1000000) {
				r =  Float.toString(f);
				v=true;
			}
		}
		if (!v)r = Integer.toHexString(integer);
		
		return r;
	}
	
	public static String decodeSimple4B(int integer, ArrayList<Hash> Hashlist) {
		for (Hash h: Hashlist) {
			if (h.reversedBinHash == integer) {
				return h.label;
			}
		}
		return null;
	}
}
