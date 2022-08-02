package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import binstuff.Hash;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GeomDump extends Application {

	TextArea hex = new TextArea();
	TextArea dmp = new TextArea();
	
	String hexSearch = "";
	String dmpSearch = "";
	
	File f = new File("C:\\Program Files (x86)\\EA Games\\Need for Speed Undercover\\CARS\\BMW_M3_E92_08\\GEOMETRY.BIN");
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setLeft(hex);
		root.setCenter(dmp);
		Scene scene = new Scene(root,1280,720);
		primaryStage.setScene(scene);
		hex.setPrefWidth(400);
		

		hex.setOnKeyPressed(new EventHandler<KeyEvent>() {
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
			public void handle(MouseEvent event) {
				dmp.setScrollTop(hex.getScrollTop());
			}
		});
		hex.setOnScroll(new EventHandler<ScrollEvent>() {
			public void handle(ScrollEvent event) {
				dmp.setScrollTop(hex.getScrollTop());
			}
			
		});
		
		
		dmp.setOnKeyPressed(new EventHandler<KeyEvent>() {
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
				
			}
		});
		dmp.setOnMouseMoved(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				hex.setScrollTop(dmp.getScrollTop());
			}
		});
		dmp.setOnScroll(new EventHandler<ScrollEvent>() {
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
			ByteBuffer bb = ByteBuffer.wrap(arr);
			
			
			
			ArrayList<Hash> Hashlist = generateHashes("BMW_M3_E92_08", new int[]{0, 1, 4, 6, 11}, new int[]{1});
			
			while (bb.position()<65536) {//65536
/*				for (int i=0; i<4; i++) {
					int current = bb.getInt();
					
					hex.appendText(Integer.toHexString(current)+" ");
					
					String s;
					if ((s = decodeSimple4B(current, Hashlist)) != null) dmp.appendText(s+ " ");
					else dmp.appendText(Integer.toHexString(current)+ " ");
					
				}
*/				
				byte off = 0;
				for (int i=0; i<4; i++) {
					
					//TODO work on an array of 7 or 8 bytes !!!!!!!!!!! this is dogshit !!!
					int current = bb.getInt();
					hex.appendText(Integer.toHexString(current)+" ");
					
					String s;
					if ((s = decodeSimple4B(current, Hashlist)) != null) dmp.appendText(s+ " ");
					else {
						//if off alignment hash
						String t = "";
						bb.position(bb.position()-4);
						if (off<1) t=t+Integer.toHexString(bb.get());
						if ((s = decodeSimple4B(bb.getInt(), Hashlist)) != null) {
							dmp.appendText(t+s);
							bb.position(bb.position()-1);
							off=1;
						}else {
							bb.position(bb.position()-4);
							if (off<2) t=t+Integer.toHexString(bb.get());
							if ((s = decodeSimple4B(bb.getInt(), Hashlist)) != null) {
								dmp.appendText(t+s);
								bb.position(bb.position()-2);
								off=2;
							}else {
								bb.position(bb.position()-4);
								if (off<3) t=t+Integer.toHexString(bb.get());
								if ((s = decodeSimple4B(bb.getInt(), Hashlist)) != null) {
									dmp.appendText(t+s);
									bb.position(bb.position()-3);
									off=3;
								}else {
									off=0;
									bb.position(bb.position()-7);
									dmp.appendText(Integer.toHexString(bb.getInt())+" ");

//									bb.position(bb.position()-3);
//									byte[] bytes = new byte[4];
//									bb.get(bb.position()-4, bytes);
//									dmp.appendText(new String(bytes, StandardCharsets.ISO_8859_1)+" ");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static ArrayList<Hash> generateHashes(String carname, int[] kits, int[] widebodies) {
		
		ArrayList<Hash> l = new ArrayList<Hash>();
		l.add(new Hash(carname));
		try {
			for (int k : kits) {
				BufferedReader br = new BufferedReader(new FileReader(new File("data/parts")));

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
				BufferedReader br = new BufferedReader(new FileReader(new File("data/parts")));

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
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
			if (h.binHash == Integer.reverseBytes(integer)) {
				r = h.label;
				v=true;
			}
		}
		if (!v) {
			byte[] bytes = new byte[4];
			bb.get(bb.position()-4, bytes);
			String s = new String(bytes, StandardCharsets.ISO_8859_1);
			System.out.println(s);
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
			if (h.binHash == Integer.reverseBytes(integer)) {
				return h.label;
			}
		}
		return null;
	}
}
