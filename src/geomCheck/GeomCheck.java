package geomCheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import binstuff.Hash;

public class GeomCheck {
	
	public static String carname = "";

	public static void main(String[] args) {
		
		try {
			long t = System.currentTimeMillis();
			BufferedWriter log = new BufferedWriter(new FileWriter(new File("GeomCheck.log")));
			log.write("Initializing GeomCheck...\n");
			BufferedReader br = new BufferedReader(new FileReader(new File("GeomCheck.ini")));

			//Config read loop
			String l;
			File f = new File("");
			boolean file = false;
			boolean car = false;
			ArrayList<String> autosculptKits = new ArrayList<String>();
			ArrayList<String> widebodyKits = new ArrayList<String>();
			ArrayList<String> fullReplacementKits = new ArrayList<String>();
			int exhausts = 99;
			
			//checks
			boolean checkLODs = false; //check useless/missing LODs
			boolean checkMaterials = false; //check some part materials
			
			while ((l = br.readLine()) != null) {
				l = l.split("//")[0].split("#")[0]; // comments removal
				if (!l.isBlank() && l.contains("=")) { // reading a proper config
					// file config, mandatory
					if(l.split("=")[0].strip().toLowerCase().equals("file")
							|| l.split("=")[0].strip().toLowerCase().equals("path") 
							|| l.split("=")[0].strip().toLowerCase().equals("file path")) {
						f = new File(l.split("=")[1].strip());
						file = true;
					}
					if(l.split("=")[0].strip().toLowerCase().equals("car")
							|| l.split("=")[0].strip().toLowerCase().equals("car name") 
							|| l.split("=")[0].strip().toLowerCase().equals("xname")) {
						carname = l.split("=")[1].strip();
						car = true;
					}
					// customization config, facultative
					if(l.split("=")[0].strip().toLowerCase().equals("autosculpt kits")
							|| l.split("=")[0].strip().toLowerCase().equals("autosculpt")) {
						for (String s : l.split("=")[1].split(",")) {
							if (s.contains("-")) {
								for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
										i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
									if (i<10) autosculptKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
									else autosculptKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
								}
							} else if (!s.strip().isBlank()) autosculptKits.add(s.strip());
						}
					}
					if(l.split("=")[0].strip().toLowerCase().equals("widebody kits")
							|| l.split("=")[0].strip().toLowerCase().equals("widebody")
							|| l.split("=")[0].strip().toLowerCase().equals("widebodies")) {
						for (String s : l.split("=")[1].split(",")) {
							if (s.contains("-")) {
								for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
										i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
									if (i<10) widebodyKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
									else widebodyKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
								}
							} else if (!s.strip().isBlank()) widebodyKits.add(s.strip());
						}
					}
					if(l.split("=")[0].strip().toLowerCase().equals("full replacement kits")
							|| l.split("=")[0].strip().toLowerCase().equals("full replacement widebody")
							|| l.split("=")[0].strip().toLowerCase().equals("full replacement widebodies")
							|| l.split("=")[0].strip().toLowerCase().equals("replacement")
							|| l.split("=")[0].strip().toLowerCase().equals("base")) {
						for (String s : l.split("=")[1].split(",")) {
							if (s.contains("-")) {
								for(int i= Integer.parseInt(s.split("-")[0].strip().substring(s.split("-")[0].strip().length() -2));
										i<=Integer.parseInt(s.split("-")[1].strip().substring(s.split("-")[0].strip().length() -2)); i++) {
									if (i<10) fullReplacementKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + "0" + i  );
									else fullReplacementKits.add(  s.split("-")[0].strip().substring(0, s.split("-")[0].strip().length() -2) + i  );
								}
							} else if (!s.strip().isBlank()) fullReplacementKits.add(s.strip());
						}
					}
					if(l.split("=")[0].strip().toLowerCase().equals("exhausts") 
							|| l.split("=")[0].strip().toLowerCase().equals("exhausts amount") 
							|| l.split("=")[0].strip().toLowerCase().equals("amount of exhausts")) {
						exhausts = Integer.parseInt(l.split("=")[1].strip());
					}
					// checks
					if(l.split("=")[0].strip().toLowerCase().equals("missing/useless lods")
							|| l.split("=")[0].strip().toLowerCase().equals("lods") 
							|| l.split("=")[0].strip().toLowerCase().equals("useless")) {
						if (l.split("=")[1].strip().equals("yes")
								|| l.split("=")[1].strip().equals("true")
								|| l.split("=")[1].strip().equals("1")) checkLODs = true;
					}
					if(l.split("=")[0].strip().toLowerCase().equals("materials")
							|| l.split("=")[0].strip().toLowerCase().equals("parts materials") 
							|| l.split("=")[0].strip().toLowerCase().equals("part materials")) {
						if (l.split("=")[1].strip().equals("yes")
								|| l.split("=")[1].strip().equals("true")
								|| l.split("=")[1].strip().equals("1")) checkMaterials = true;
					}
				}
			}

			if (!file) throw new Exception("File input not specified !");
			if (!car) throw new Exception("Car name not specified !");
			

			if(autosculptKits.isEmpty() && widebodyKits.isEmpty() && fullReplacementKits.isEmpty()) {
				for(int i=1;i<10;i++) fullReplacementKits.add("KIT0"+i);
				for(int i=10;i<100;i++) fullReplacementKits.add("KIT"+i);
				for(int i=1;i<10;i++) fullReplacementKits.add("KITW0"+i);
				for(int i=10;i<100;i++) fullReplacementKits.add("KITW"+i);
			}
			
			if (autosculptKits.contains("KIT00")) autosculptKits.remove("KIT00");
			if (widebodyKits.contains("KIT00")) widebodyKits.remove("KIT00");
			if (fullReplacementKits.contains("KIT00")) fullReplacementKits.remove("KIT00");
			
			for (String k : fullReplacementKits) {
				if (autosculptKits.contains(k)) autosculptKits.remove(k);
				if (widebodyKits.contains(k)) widebodyKits.remove(k);
			}

			log.write("Configuration loaded in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			FileInputStream fis = new FileInputStream(f);
			byte [] fileToBytes = new byte[(int)f.length()];
			fis.read(fileToBytes);
			fis.close();

			log.write("Geometry " + f.getPath() + " cached in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			ArrayList<Hash> hashes = generateHashes(carname, autosculptKits, widebodyKits, fullReplacementKits, exhausts);
			log.write("Part and texture hashes generated in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			
			
        	ByteBuffer bb = ByteBuffer.wrap(fileToBytes);
        	bb.position(184);
        	int i;
        	while ((i = bb.getInt()) != 71308032) {	//stop searching when 0x04401300 is found
        		boolean found = false;
        		for (Hash h : hashes) {
        			if (i == h.reversedBinHash) {
        				found = true;

//            			System.out.println("Part found : Ox" + Integer.toHexString(i) + " = " + h.label);
//            			log.write("Part found : Ox" + Integer.toHexString(i) + " = " + h.label + "\n");
//        				System.out.print("Part found : "+h.label + " | ");
        				//part found
        				String partname = h.label.substring(0, h.label.length() - 2).replace(carname + "_", "");
        				boolean existing = false;
        				Part part = null;
        				for(Part p : Part.allParts) {
        					if ((p.kit + "_" + p.name).equals(partname)){
        						part = p;
        						existing = true;
        						break;
        					}
        				}
        				if (!existing) part = new Part(partname.split("_")[0], partname.replace(partname.split("_")[0] + "_", ""));
        				switch (h.label.split("_")[h.label.split("_").length-1]) {
        				case "A":
        					part.lodAExists = true;
        					break;
        				case "B":
        					part.lodBExists = true;
        					break;
        				case "C":
        					part.lodCExists = true;
        					break;
        				case "D":
        					part.lodDExists = true;
        					break;
        				case "E":
        					part.lodEExists = true;
        				}
//        				System.out.println(part);
        				break;
        			}
        		}
        		if (!found) {
        			System.out.println("Part not found : Ox" + Integer.toHexString(i));
        			log.write("Warning : unable to guess part Ox" + Integer.toHexString(i) + " at " + bb.position() + "\n");
        		}
				bb.getInt(); //jumps the blank 4 bytes between each part
			}
        	
        	Part.allParts.sort(new Comparator<Part>() {
				public int compare(Part p1, Part p2) {
					return (p1.kit + "_" + p1.name).compareTo(p2.kit + "_" + p2.name);
				}
        	});
//        	for(Part p : Part.allParts) {
//        		System.out.println(p);
//        	}
        	
			log.write("Parts guessed in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			hashes = new ArrayList<Hash>();
			for (Part p : Part.allParts) {
				if (p.lodAExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_A"));
				if (p.lodBExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_B"));
				if (p.lodCExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_C"));
				if (p.lodDExists) hashes.add(new Hash(carname+"_"+p.kit+"_"+p.name+"_D"));
			}

			log.write("Part hashes optimized in " + (System.currentTimeMillis()-t) + " ms.\n");
			t = System.currentTimeMillis();
			
			// CHECKS
			
			if (checkLODs) { // useless/missing LODs
				System.out.println("Checking LODs");
				boolean noD = false;
				boolean dupeD = false;
				for (Part p : Part.allParts) {
					if (p.lodDExists && !p.kit.equals("KIT00")) log.write("[LODCHK] Useless LOD : " + p.kit + "_" + p.name + "_D\n");
					if (p.lodEExists) log.write("[LODCHK] Useless LOD : " + p.kit + "_" + p.name + "_E\n");
				}
				for (Part p : Part.allParts) {
					if (!p.lodAExists) log.write("[LODCHK] Missing LOD : " + p.kit + "_" + p.name + "_A\n");
					if (!p.lodBExists) log.write("[LODCHK] Missing LOD : " + p.kit + "_" + p.name + "_B\n");
					if (!p.lodCExists) log.write("[LODCHK] Missing LOD : " + p.kit + "_" + p.name + "_C\n");

					if (!p.lodDExists && p.kit.equals("KIT00") && p.name.equals("BODY")) noD = true;
					if (p.lodDExists && p.kit.equals("KIT00") && !p.name.equals("BODY")) dupeD = true;
				}
				if (noD) log.write("[LODCHK] Critical : missing KIT00_BODY_D \n");
				if (dupeD) log.write("[LODCHK] Warning : several KIT00 LOD D parts detected, please check that this is not caused by duplicated LOD C parts (potential clipping and optimization loss)\n");
				log.write("LODs checked in " + (System.currentTimeMillis()-t) + " ms.\n");
				t = System.currentTimeMillis();
			}
			
			if (checkMaterials) {
				System.out.println("Checking some common materials");
				
				//after the end of the parts list
				// something        first part offset    ?        ?        ?        ?
				//04401300 E0130000 FE39C300 801B0000 67340000 404A0000 00020000 00000000 then cycles with parts
				//second part       FF39C300 00500000 82110000 201B0000 00020000 00000000
				//etc               003AC300 00620000 82110000 201B0000 00020000 00000000 <- kit00 door left btw
				//                  013AC300 00740000 82110000 201B0000 00020000 00000000
				//
				//                  3F9D74FD 802A2900 E5160000 20240000 00020000 00000000 <- kitw01 brakelight left
				//                  409D74FD 80412900 53160000 A0220000 00020000 00000000
				// last part        419D74FD 00582900 52160000 A0220000 00020000 00000000 
				//                  00000000 38000000 00000000 00000000 00000000 00000000 00000000 00000000 
				//                  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
				bb.getInt();
				bb.order(ByteOrder.LITTLE_ENDIAN);
				
				while ((i = bb.getInt()) != 0) {	//stop searching when 0x00000000 is found
					boolean val = false;
	        		for (Part p : Part.allParts) {
	        			if (i == p.lodAHash.binHash) {
	        				p.lodAPosition = bb.getInt();
//	        				System.out.println(p.name + " lod A at : " + p.lodAPosition);
	        				val = true;
	        				break;
	        			}
	        			if (i == p.lodBHash.binHash) {
	        				p.lodBPosition = bb.getInt();
//	        				System.out.println(p.name + " lod B at : " + p.lodBPosition);
	        				val = true;
	        				break;
	        			}
	        			if (i == p.lodCHash.binHash) {
	        				p.lodCPosition = bb.getInt();
//	        				System.out.println(p.name + " lod C at : " + p.lodCPosition);
	        				val = true;
	        				break;
	        			}
	        			if (i == p.lodDHash.binHash) {
	        				p.lodDPosition = bb.getInt();
//	        				System.out.println(p.name + " lod D at : " + p.lodDPosition);
	        				val = true;
	        				break;
	        			}
	        		}
	        		if (!val) bb.getInt();
					bb.position(bb.position()+16); //jumps the 4x4 bytes
				}
				bb.order(ByteOrder.BIG_ENDIAN);
				
				log.write("Part offsets scanned in " + (System.currentTimeMillis()-t) + " ms.\n");
				t = System.currentTimeMillis();
				
				// WINDOW_FRONT : check it has WINDOW_FRONT and doesn't have the rest
				// WINDOW_FRONT/REAR_LEFT/RIGHT : check the correct material
				// WINDOW_REAR : check it has WINDOW_FRONT and REAR_DEFROSTER and not the others
				
				// BODY_D : check there's no "fancy" shaders besides dullplastic and carskin eg magsilver
				
				for (Part p : Part.allParts) {
					ArrayList<Hash> search;
					ArrayList<ArrayList<Boolean>> result;
					ArrayList<Boolean> resultLOD;

					if (p.lodDExists) { //lod D materials
						search = new ArrayList<Hash>();
						search.add(new Hash("BRAKEDISC"));
						search.add(new Hash("BRAKELIGHT"));
						search.add(new Hash("BRAKELIGHT_ALUMINUM"));
						search.add(new Hash("BRAKELIGHTGLASS"));
						search.add(new Hash("BRAKELIGHTGLASSRED"));
						search.add(new Hash("CALIPER"));
						search.add(new Hash("DEFROSTER"));
						search.add(new Hash("DOORLINE"));
						search.add(new Hash("ENGINE"));
						search.add(new Hash("EXHAUST_ALUMINUM"));
						search.add(new Hash("EXHAUST_CHROME"));
						search.add(new Hash("GRILL"));
						search.add(new Hash("GRILLCHROME"));
						search.add(new Hash("HEADLIGHTCHROME"));
						search.add(new Hash("HEADLIGHTGLASS"));
						search.add(new Hash("HEADLIGHTREFLECTOR"));
						search.add(new Hash("INTERIOR"));
						search.add(new Hash("MAGCHROME"));
						search.add(new Hash("MAGLIP"));
						search.add(new Hash("MAGMATTE"));
						search.add(new Hash("MAGSILVER"));
						search.add(new Hash("RAD"));
						search.add(new Hash("RUBBER"));
						search.add(new Hash("WINDOWMASK"));
						search.add(new Hash("WINDSHIELD"));
						resultLOD = p.scanLOD(fileToBytes, search, 3);
						boolean val = false;
						for (Boolean b : resultLOD) {
							if (b) {
								val = true;
								break;
							}
						}
						if (val) {
							log.write("[MATCHK] \"Fancy\" shader(s) on " + p.kit + "_" + p.name + "_D");
							for (i=0; i<resultLOD.size(); i++) {
								if (resultLOD.get(i)) {
									log.write(", "+search.get(i).label);
								}
							}
							log.write("\n");
						}
					}
					
					if (p.name.equals("WINDOW_FRONT")) {
						search = new ArrayList<Hash>();
						search.add(new Hash("WINDOW_FRONT"));
						result = p.scan(fileToBytes, search);
						for (i=0; i<3; i++) { //i<3 to not check lod D
							if(result.get(i) != null) {
								if (!result.get(i).get(0)) log.write("[MATCHK] Texture WINDOW_FRONT not found in " + p.kit + "_" + p.name + "_" + lodconv(i) + "\n");
							}
						}
					}

					if (p.name.equals("WINDOW_FRONT_LEFT")) {
						search = new ArrayList<Hash>();
						search.add(new Hash("WINDOW_LEFT_FRONT"));
						search.add(new Hash("WINDOW_FRONT"));
						result = p.scan(fileToBytes, search);
						for (i=0; i<3; i++) { //i<3 to not check lod D
							if(result.get(i) != null) {
								if (!result.get(i).get(0)) {
									log.write("[MATCHK] Texture WINDOW_LEFT_FRONT not found in " + p.kit + "_" + p.name + "_" + lodconv(i));
									if (result.get(i).get(1)) log.write(" (mismatched with WINDOW_FRONT)");
									log.write("\n");
								}
							}
						}
					}

					if (p.name.equals("WINDOW_FRONT_RIGHT")) {
						search = new ArrayList<Hash>();
						search.add(new Hash("WINDOW_RIGHT_FRONT"));
						search.add(new Hash("WINDOW_FRONT"));
						result = p.scan(fileToBytes, search);
						for (i=0; i<3; i++) { //i<3 to not check lod D
							if(result.get(i) != null) {
								if (!result.get(i).get(0)) {
									log.write("[MATCHK] Texture WINDOW_RIGHT_FRONT not found in " + p.kit + "_" + p.name + "_" + lodconv(i));
									if (result.get(i).get(1)) log.write(" (mismatched with WINDOW_FRONT)");
									log.write("\n");
								}
							}
						}
					}
					
					if (p.name.equals("WINDOW_REAR")) {
						search = new ArrayList<Hash>();
						search.add(new Hash("WINDOW_REAR"));
						search.add(new Hash("REAR_DEFROSTER"));
						search.add(new Hash("WINDOW_FRONT"));
						result = p.scan(fileToBytes, search);
						for (i=0; i<3; i++) { //i<3 to not check lod D
							if(result.get(i) != null) {
								if (!result.get(i).get(0)) {
									log.write("[MATCHK] Texture WINDOW_REAR not found in " + p.kit + "_" + p.name + "_" + lodconv(i));
									if (result.get(i).get(2)) log.write(" (mismatched with WINDOW_FRONT)");
									log.write("\n");
								}
								if (!result.get(i).get(1)) log.write("[MATCHK] Texture REAR_DEFROSTER not found in " + p.kit + "_" + p.name + "_" + lodconv(i) + "\n");
							}
						}
					}

					if (p.name.equals("WINDOW_REAR_LEFT")) {
						search = new ArrayList<Hash>();
						search.add(new Hash("WINDOW_LEFT_REAR"));
						search.add(new Hash("WINDOW_FRONT"));
						result = p.scan(fileToBytes, search);
						for (i=0; i<3; i++) { //i<3 to not check lod D
							if(result.get(i) != null) {
								if (!result.get(i).get(0)) {
									log.write("[MATCHK] Texture WINDOW_LEFT_REAR not found in " + p.kit + "_" + p.name + "_" + lodconv(i));
									if (result.get(i).get(1)) log.write(" (mismatched with WINDOW_FRONT)");
									log.write("\n");
								}
							}
						}
					}

					if (p.name.equals("WINDOW_REAR_RIGHT")) {
						search = new ArrayList<Hash>();
						search.add(new Hash("WINDOW_RIGHT_REAR"));
						search.add(new Hash("WINDOW_FRONT"));
						result = p.scan(fileToBytes, search);
						for (i=0; i<3; i++) { //i<3 to not check lod D
							if(result.get(i) != null) {
								if (!result.get(i).get(0)) {
									log.write("[MATCHK] Texture WINDOW_RIGHT_REAR not found in " + p.kit + "_" + p.name + "_" + lodconv(i));
									if (result.get(i).get(1)) log.write(" (mismatched with WINDOW_FRONT)");
									log.write("\n");
								}
							}
						}
					}
					
					
					
				}
				
				log.write("Common materials checked in " + (System.currentTimeMillis()-t) + " ms.\n");
				t = System.currentTimeMillis();
			}
			
			
			
			
			log.close();
			
		} catch (FileNotFoundException e) {
			try {
				if (!new File("GeomCheck.ini").exists()) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File("GeomCheck.ini")));
					
					bw.write( "File settings (mandatory)\r\n"
							+ "File =  C:\\Program Files (x86)\\EA Games\\Need for Speed Undercover\\CARS\\AAA_AAA_AAA_01\\GEOMETRY.BIN\\r\\n"
							+ "Car =   AAA_AAA_AAA_01\r\n"
							+ "\r\n"
							+ "Customization settings (optional)\r\n"
							+ "Autosculpt kits =               KIT01-KIT11\r\n"
							+ "Widebody kits =                 KITW01-KITW05\r\n"
							+ "Full replacement widebodies =   #none\r\n"
							+ "Exhausts amount =               5\r\n"
							+ "\r\n"
							+ "Checks (optional)\r\n"
							+ "Missing/useless LODs =          yes\r\n"
							+ "Parts materials =               yes\r\n"
							+ "\r\n");
					bw.close();
					System.out.println("Missing configuration, it has been generated.");
				} else System.out.println("Invalid configuration, check the car's path.");
				
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String lodconv(int i) {
		switch(i) {
		case 0:
			return "A";
		case 1:
			return "B";
		case 2:
			return "C";
		case 3:
			return "D";
		default:
			return "?";
		}
	}
	
	public static ArrayList<Hash> generateHashes(String carname, ArrayList<String> autosculptKits, ArrayList<String> widebodyKits, ArrayList<String> fullReplacementKits, int exhausts) {
		
		ArrayList<Hash> l = new ArrayList<Hash>();
		l.add(new Hash(carname));
		try {
			String part;
			BufferedReader br = new BufferedReader(new FileReader(new File("data/Parts_KIT00")));
			while ((part = br.readLine())!=null){
				l.add(new Hash(carname + "_KIT00_" + part + "_A"));
				l.add(new Hash(carname + "_KIT00_" + part + "_B"));
				l.add(new Hash(carname + "_KIT00_" + part + "_C"));
				l.add(new Hash(carname + "_KIT00_" + part + "_D"));
				l.add(new Hash(carname + "_KIT00_" + part + "_E"));
				for (int as=0; as<11; as++) {
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_D"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_E"));
				}
			}
			br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
			while ((part = br.readLine())!=null){
				l.add(new Hash(carname + "_KIT00_" + part + "_A"));
				l.add(new Hash(carname + "_KIT00_" + part + "_B"));
				l.add(new Hash(carname + "_KIT00_" + part + "_C"));
				l.add(new Hash(carname + "_KIT00_" + part + "_D"));
				l.add(new Hash(carname + "_KIT00_" + part + "_E"));
				for (int as=0; as<11; as++) {
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_D"));
					l.add(new Hash(carname + "_KIT00_" + part + "_T" + as + "_E"));
				}
			}
			br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
			while ((part = br.readLine())!=null){
				for (int as=0; as<exhausts+1; as++) {
					String nstr;
					if(as<10)nstr = "_0"+as;
					else nstr = "_"+as;
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_A"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_B"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_C"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_D"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_E"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_A"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_B"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_C"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_D"));
					l.add(new Hash(carname + "_KIT00_" + part + nstr + "_T0_E"));
				}
			}
			
			for (String k : autosculptKits) {
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));
					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}
				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
				while ((part = br.readLine())!=null){
					for (int as=0; as<exhausts+1; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_D"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_D"));
					}
				}
			}

			for (String k : widebodyKits) {
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));
					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}
				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
				while ((part = br.readLine())!=null){
					for (int as=0; as<exhausts+1; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_D"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_D"));
					}
				}
			}
			
			for (String k : fullReplacementKits) {
				br = new BufferedReader(new FileReader(new File("data/Parts_KIT00")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));
					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}
				
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));
				while ((part = br.readLine())!=null){
					l.add(new Hash(carname + "_" + k + "_" + part + "_A"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_B"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_C"));
					l.add(new Hash(carname + "_" + k + "_" + part + "_D"));

					for (int as=0; as<11; as++) {
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + "_T" + as + "_D"));
					}
				}

				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));
				while ((part = br.readLine())!=null){
					for (int as=0; as<exhausts+1; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_D"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_A"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_B"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_C"));
						l.add(new Hash(carname + "_" + k + "_" + part + nstr + "_T0_D"));
					}
				}
			}
		
//			br = new BufferedReader(new FileReader(new File("data/textures")));
//			String tex;
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(carname + "_" + tex));
//			}
//			
//			br = new BufferedReader(new FileReader(new File("data/gentextures")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex));
//			}
//
//			br = new BufferedReader(new FileReader(new File("data/shaders")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex));
//			}
//
//			br = new BufferedReader(new FileReader(new File("data/mpoints")));
//			while ((tex = br.readLine())!=null){
//				l.add(new Hash(tex));
//				for (int as=0; as<11; as++) {
//					l.add(new Hash(tex + "_T" + as));
//				}
//			}
			
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
}