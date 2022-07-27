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
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GeomDump extends Application {

	TextArea hex = new TextArea();
	TextArea dmp = new TextArea();
	ScrollBar s = new ScrollBar();
	
	File f = new File("C:\\Program Files (x86)\\EA Games\\Need for Speed Undercover\\CARS\\BMW_M3_E92_08\\GEOMETRY.BIN");
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setLeft(hex);
		root.setCenter(s);
		root.setRight(dmp);
		s.setOrientation(Orientation.VERTICAL);
		Scene scene = new Scene(root,1280,720);
		primaryStage.setScene(scene);
		hex.setPrefWidth(scene.getWidth()/2-10);
		dmp.setPrefWidth(scene.getWidth()/2-10);
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
			
			while (bb.position()<65536) {
				for (int i=0; i<4; i++) {
					int current = bb.getInt();
					
					hex.appendText(Integer.toHexString(current)+" ");					
					dmp.appendText(tryDecode4B(bb,current, Hashlist)+ " ");
					
					
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
			if (h.binHash == integer) {
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
}
